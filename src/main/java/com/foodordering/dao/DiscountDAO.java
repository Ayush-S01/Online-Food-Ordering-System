package com.foodordering.dao;

import com.foodordering.database.DatabaseConnection;
import com.foodordering.model.Discount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Discount} coupons.
 *
 * <p>All queries use PreparedStatement; resources closed via try-with-resources.
 */
public class DiscountDAO {

    /**
     * Finds a discount by coupon code (case-insensitive).
     *
     * @param couponCode the code entered by the user.
     * @return an Optional containing the Discount if found.
     * @throws SQLException on DB errors.
     */
    public Optional<Discount> findByCouponCode(String couponCode) throws SQLException {
        String sql = "SELECT discount_id, coupon_code, discount_type, discount_value, "
                   + "minimum_order_amount, expiry_date, enabled "
                   + "FROM discounts WHERE UPPER(coupon_code) = UPPER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, couponCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns all discount coupons.
     */
    public List<Discount> findAll() throws SQLException {
        String sql = "SELECT discount_id, coupon_code, discount_type, discount_value, "
                   + "minimum_order_amount, expiry_date, enabled "
                   + "FROM discounts ORDER BY coupon_code";
        List<Discount> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Returns all currently active and non-expired discount coupons.
     */
    public List<Discount> findActive() throws SQLException {
        String sql = "SELECT discount_id, coupon_code, discount_type, discount_value, "
                   + "minimum_order_amount, expiry_date, enabled "
                   + "FROM discounts WHERE enabled = 1 AND expiry_date >= CURRENT_DATE "
                   + "ORDER BY coupon_code";
        List<Discount> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Inserts a new discount coupon.
     *
     * @return the persisted Discount with its generated ID.
     */
    public Discount insert(Discount d) throws SQLException {
        String sql = "INSERT INTO discounts (coupon_code, discount_type, discount_value, "
                   + "minimum_order_amount, expiry_date, enabled) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            setParams(ps, d);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) d.setDiscountId(keys.getInt(1));
            }
        }
        return d;
    }

    /** Updates a discount record. */
    public void update(Discount d) throws SQLException {
        String sql = "UPDATE discounts SET coupon_code=?, discount_type=?, discount_value=?, "
                   + "minimum_order_amount=?, expiry_date=?, enabled=? WHERE discount_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParams(ps, d);
            ps.setInt(7, d.getDiscountId());
            ps.executeUpdate();
        }
    }

    /** Enables or disables a coupon. */
    public void setEnabled(int discountId, boolean enabled) throws SQLException {
        String sql = "UPDATE discounts SET enabled=? WHERE discount_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, enabled);
            ps.setInt(2, discountId);
            ps.executeUpdate();
        }
    }

    /** Deletes a discount coupon by ID. */
    public void delete(int discountId) throws SQLException {
        String sql = "DELETE FROM discounts WHERE discount_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, discountId);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void setParams(PreparedStatement ps, Discount d) throws SQLException {
        ps.setString(1, d.getCouponCode());
        ps.setString(2, d.getDiscountType().name());
        ps.setBigDecimal(3, d.getDiscountValue());
        ps.setBigDecimal(4, d.getMinimumOrderAmount());
        ps.setDate(5, Date.valueOf(d.getExpiryDate()));
        ps.setBoolean(6, d.isEnabled());
    }

    private Discount mapRow(ResultSet rs) throws SQLException {
        return new Discount(
            rs.getInt("discount_id"),
            rs.getString("coupon_code"),
            Discount.Type.valueOf(rs.getString("discount_type")),
            rs.getBigDecimal("discount_value"),
            rs.getBigDecimal("minimum_order_amount"),
            rs.getDate("expiry_date").toLocalDate(),
            rs.getBoolean("enabled")
        );
    }
}
