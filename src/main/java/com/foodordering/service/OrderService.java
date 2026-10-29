package com.foodordering.service;

import com.foodordering.dao.OrderDAO;
import com.foodordering.model.*;
import com.foodordering.util.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for order placement, retrieval, and status management.
 */
public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();

    /**
     * Backward-compatible placeOrder overload (used in tests).
     */
    public Order placeOrder(Cart cart,
                            BigDecimal discountAmount,
                            Payment.Method paymentMethod,
                            CartService cartService) throws Exception {
        return placeOrder(cart, discountAmount, paymentMethod, "Standard Delivery Address", "9876543210", "TXN-" + System.currentTimeMillis(), cartService);
    }

    /**
     * Places an order with full delivery details and payment reference.
     */
    public Order placeOrder(Cart cart,
                            BigDecimal discountAmount,
                            Payment.Method paymentMethod,
                            String deliveryAddress,
                            String contactPhone,
                            String transactionRef,
                            CartService cartService) throws Exception {
        // 1. Validate cart
        if (cart.isEmpty()) {
            throw new IllegalStateException("Your cart is empty. Add items before checking out.");
        }

        // 2. Re-validate availability
        for (CartItem item : cart.getItems()) {
            if (!item.getFoodItem().isAvailable()) {
                throw new IllegalStateException(
                    "'" + item.getFoodItem().getName()
                    + "' is no longer available. Remove it from your cart.");
            }
        }

        // 3. Build order
        Customer current = SessionManager.getInstance().getCurrentUser();
        int customerId = current != null ? current.getCustomerId() : 1;
        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal finalAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);
        order.setOrderStatus(Order.Status.PLACED);
        order.setDeliveryAddress((deliveryAddress != null && !deliveryAddress.isBlank()) ? deliveryAddress.trim() : "Standard Delivery");
        order.setContactPhone((contactPhone != null && !contactPhone.isBlank()) ? contactPhone.trim() : "");
        order.setPaymentMethod(paymentMethod.name());

        // 4. Build order items (snapshot name + price)
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setFoodId(ci.getFoodItem().getFoodId());
            oi.setFoodNameSnapshot(ci.getFoodItem().getName());
            oi.setUnitPrice(ci.getFoodItem().getPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setLineTotal(ci.getLineTotal());
            orderItems.add(oi);
        }

        // 5. Build payment
        Payment.PayStatus payStatus = (paymentMethod == Payment.Method.CASH_ON_DELIVERY)
            ? Payment.PayStatus.PENDING
            : Payment.PayStatus.COMPLETED;

        Payment payment = new Payment();
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(finalAmount);
        payment.setPaymentStatus(payStatus);
        payment.setTransactionRef(transactionRef != null ? transactionRef : "TXN-" + System.currentTimeMillis());

        // 6. Persist (transactional)
        orderDAO.createOrderWithTransaction(order, orderItems, payment);

        // 7. Clear cart on success
        cartService.clearCart();

        return order;
    }

    public List<Order> getMyOrders() throws SQLException {
        Customer me = SessionManager.getInstance().getCurrentUser();
        if (me == null) throw new SecurityException("Not logged in.");
        return orderDAO.findByCustomer(me.getCustomerId());
    }

    public List<OrderItem> getOrderItems(int orderId) throws SQLException {
        return orderDAO.findItemsByOrderId(orderId);
    }

    public void updateOrderStatus(int orderId, Order.Status newStatus)
            throws SQLException, IllegalAccessException {
        if (!SessionManager.getInstance().isAdmin()) {
            throw new IllegalAccessException("Only administrators can update order status.");
        }
        Order order = orderDAO.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        orderDAO.updateStatus(orderId, newStatus);
    }

    public List<Order> getAllOrders() throws SQLException {
        return orderDAO.findAll();
    }

    public int getTotalOrderCount()     throws SQLException { return orderDAO.countAllOrders(); }
    public BigDecimal getTotalRevenue() throws SQLException { return orderDAO.totalRevenue(); }
    public int getTodayOrderCount()     throws SQLException { return orderDAO.todayOrderCount(); }
}
