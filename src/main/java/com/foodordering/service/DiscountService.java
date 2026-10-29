package com.foodordering.service;

import com.foodordering.dao.DiscountDAO;
import com.foodordering.model.Discount;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service for discount coupon validation and calculation.
 *
 * <p>Algorithm — Discount validation:
 * <ol>
 *   <li>Look up the coupon code in the database.</li>
 *   <li>Reject if not found.</li>
 *   <li>Reject if disabled.</li>
 *   <li>Reject if expired (expiry_date &lt; today).</li>
 *   <li>Reject if the subtotal is below the minimum order amount.</li>
 *   <li>Compute the discount amount (percentage or fixed), capped at the subtotal.</li>
 * </ol>
 */
public class DiscountService {

    private final DiscountDAO discountDAO = new DiscountDAO();

    /**
     * Validates a coupon code and returns the Discount if it is applicable.
     *
     * @param couponCode    the code entered by the customer.
     * @param orderSubtotal the cart subtotal before discount.
     * @return the applicable {@link Discount}.
     * @throws Exception with a user-friendly message if the coupon is invalid.
     */
    public Discount validateCoupon(String couponCode, BigDecimal orderSubtotal)
            throws Exception {
        if (couponCode == null || couponCode.isBlank()) {
            throw new IllegalArgumentException("Please enter a coupon code.");
        }

        Optional<Discount> opt = discountDAO.findByCouponCode(couponCode.trim());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException(
                "Coupon '" + couponCode + "' does not exist.");
        }

        Discount discount = opt.get();

        if (!discount.isEnabled()) {
            throw new IllegalArgumentException(
                "Coupon '" + couponCode + "' is currently disabled.");
        }
        if (!discount.isNotExpired()) {
            throw new IllegalArgumentException(
                "Coupon '" + couponCode + "' has expired.");
        }
        if (orderSubtotal.compareTo(discount.getMinimumOrderAmount()) < 0) {
            throw new IllegalArgumentException(
                "Minimum order amount for this coupon is ₹"
                + discount.getMinimumOrderAmount().toPlainString()
                + ". Your subtotal is ₹" + orderSubtotal.toPlainString() + ".");
        }

        return discount;
    }

    /**
     * Calculates the discount amount for a given subtotal.
     *
     * @param discount      a validated Discount object.
     * @param orderSubtotal the cart subtotal.
     * @return discount amount (never negative, never exceeds subtotal).
     */
    public BigDecimal calculateDiscountAmount(Discount discount, BigDecimal orderSubtotal) {
        return discount.calculateDiscount(orderSubtotal);
    }

    // ------------------------------------------------------------------ //
    //  Admin CRUD
    // ------------------------------------------------------------------ //

    public List<Discount> getAllCoupons() throws SQLException {
        return discountDAO.findAll();
    }

    public Discount addCoupon(Discount d) throws SQLException {
        return discountDAO.insert(d);
    }

    public void updateCoupon(Discount d) throws SQLException {
        discountDAO.update(d);
    }

    public void setCouponEnabled(int discountId, boolean enabled) throws SQLException {
        discountDAO.setEnabled(discountId, enabled);
    }
}
