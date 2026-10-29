package com.foodordering.dao;

import com.foodordering.database.DatabaseConnection;
import com.foodordering.model.Order;
import com.foodordering.model.OrderItem;
import com.foodordering.model.Payment;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Order}, {@link OrderItem}, and {@link Payment}.
 *
 * <p>Persists delivery address, contact phone, snapshot items, and payment transaction ref
 * inside a single ACID JDBC transaction.
 */
public class OrderDAO {

    // ------------------------------------------------------------------ //
    //  Order creation (transactional)
    // ------------------------------------------------------------------ //

    public void createOrderWithTransaction(Order order,
                                           List<OrderItem> items,
                                           Payment payment) throws SQLException {
        String insertOrder = "INSERT INTO orders (customer_id, subtotal, discount_amount, "
                           + "final_amount, order_status, delivery_address, contact_phone) "
                           + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertItem  = "INSERT INTO order_items (order_id, food_id, food_name_snapshot, "
                           + "unit_price, quantity, line_total) VALUES (?, ?, ?, ?, ?, ?)";
        String insertPay   = "INSERT INTO payments (order_id, payment_method, amount, "
                           + "payment_status, transaction_ref) VALUES (?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // 1. Insert order
            try (PreparedStatement ps = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, order.getCustomerId());
                ps.setBigDecimal(2, order.getSubtotal());
                ps.setBigDecimal(3, order.getDiscountAmount());
                ps.setBigDecimal(4, order.getFinalAmount());
                ps.setString(5, order.getOrderStatus().name());
                ps.setString(6, order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Standard Delivery");
                ps.setString(7, order.getContactPhone() != null ? order.getContactPhone() : "");
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Order insert returned no key.");
                    order.setOrderId(keys.getInt(1));
                }
            }

            // 2. Insert order items
            try (PreparedStatement ps = conn.prepareStatement(insertItem, Statement.RETURN_GENERATED_KEYS)) {
                for (OrderItem item : items) {
                    item.setOrderId(order.getOrderId());
                    ps.setInt(1, item.getOrderId());
                    ps.setInt(2, item.getFoodId());
                    ps.setString(3, item.getFoodNameSnapshot());
                    ps.setBigDecimal(4, item.getUnitPrice());
                    ps.setInt(5, item.getQuantity());
                    ps.setBigDecimal(6, item.getLineTotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 3. Insert payment
            try (PreparedStatement ps = conn.prepareStatement(insertPay, Statement.RETURN_GENERATED_KEYS)) {
                payment.setOrderId(order.getOrderId());
                ps.setInt(1, payment.getOrderId());
                ps.setString(2, payment.getPaymentMethod().name());
                ps.setBigDecimal(3, payment.getAmount());
                ps.setString(4, payment.getPaymentStatus().name());
                ps.setString(5, payment.getTransactionRef() != null ? payment.getTransactionRef() : "TXN-" + System.currentTimeMillis());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) payment.setPaymentId(keys.getInt(1));
                }
            }

            conn.commit(); // COMMIT

        } catch (SQLException ex) {
            conn.rollback(); // ROLLBACK on any failure
            throw ex;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    // ------------------------------------------------------------------ //
    //  Queries
    // ------------------------------------------------------------------ //

    public List<Order> findByCustomer(int customerId) throws SQLException {
        String sql = "SELECT o.order_id, o.customer_id, o.order_date, o.subtotal, o.discount_amount, "
                   + "o.final_amount, o.order_status, o.delivery_address, o.contact_phone, p.payment_method "
                   + "FROM orders o LEFT JOIN payments p ON o.order_id = p.order_id "
                   + "WHERE o.customer_id = ? ORDER BY o.order_date DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapOrder(rs));
            }
        }
        return list;
    }

    public List<Order> findAll() throws SQLException {
        String sql = "SELECT o.order_id, o.customer_id, o.order_date, o.subtotal, o.discount_amount, "
                   + "o.final_amount, o.order_status, o.delivery_address, o.contact_phone, p.payment_method "
                   + "FROM orders o LEFT JOIN payments p ON o.order_id = p.order_id "
                   + "ORDER BY o.order_date DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapOrder(rs));
        }
        return list;
    }

    public Optional<Order> findById(int orderId) throws SQLException {
        String sql = "SELECT o.order_id, o.customer_id, o.order_date, o.subtotal, o.discount_amount, "
                   + "o.final_amount, o.order_status, o.delivery_address, o.contact_phone, p.payment_method "
                   + "FROM orders o LEFT JOIN payments p ON o.order_id = p.order_id "
                   + "WHERE o.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapOrder(rs));
            }
        }
        return Optional.empty();
    }

    public List<OrderItem> findItemsByOrderId(int orderId) throws SQLException {
        String sql = "SELECT order_item_id, order_id, food_id, food_name_snapshot, "
                   + "unit_price, quantity, line_total FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.add(mapOrderItem(rs));
            }
        }
        return items;
    }

    public void updateStatus(int orderId, Order.Status newStatus) throws SQLException {
        String sql = "UPDATE orders SET order_status=? WHERE order_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ //
    //  Admin statistics
    // ------------------------------------------------------------------ //

    public int countAllOrders() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public BigDecimal totalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(final_amount), 0) FROM orders "
                   + "WHERE order_status NOT IN ('CANCELLED')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    public int todayOrderCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE DATE(order_date) = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ------------------------------------------------------------------ //
    //  Row mappers
    // ------------------------------------------------------------------ //

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setCustomerId(rs.getInt("customer_id"));
        Timestamp ts = rs.getTimestamp("order_date");
        if (ts != null) o.setOrderDate(ts.toLocalDateTime());
        o.setSubtotal(rs.getBigDecimal("subtotal"));
        o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        o.setFinalAmount(rs.getBigDecimal("final_amount"));
        o.setOrderStatus(Order.Status.valueOf(rs.getString("order_status")));

        try {
            o.setDeliveryAddress(rs.getString("delivery_address"));
            o.setContactPhone(rs.getString("contact_phone"));
            o.setPaymentMethod(rs.getString("payment_method"));
        } catch (SQLException ignored) {}

        return o;
    }

    private OrderItem mapOrderItem(ResultSet rs) throws SQLException {
        return new OrderItem(
            rs.getInt("order_item_id"),
            rs.getInt("order_id"),
            rs.getInt("food_id"),
            rs.getString("food_name_snapshot"),
            rs.getBigDecimal("unit_price"),
            rs.getInt("quantity"),
            rs.getBigDecimal("line_total")
        );
    }
}
