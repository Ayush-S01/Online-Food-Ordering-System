package com.foodordering;

import com.foodordering.model.Discount;
import com.foodordering.model.FoodItem;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Discount model business logic.
 *
 * <p>Tests percentage discounts, fixed discounts, invalid / expired coupons,
 * minimum order validation, and final-amount calculation.
 *
 * <p>Note: DiscountService.validateCoupon() requires a database connection,
 * so this test class tests the Discount model directly (pure unit tests, no DB).
 */
@DisplayName("Discount Calculation Tests")
class DiscountServiceTest {

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusYears(1);
    private static final LocalDate PAST_DATE   = LocalDate.now().minusDays(1);

    // ------------------------------------------------------------------ //
    //  Percentage discount
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("PERCENTAGE coupon should apply correct discount")
    void testPercentageDiscount() {
        Discount d = new Discount(1, "SAVE10", Discount.Type.PERCENTAGE,
            new BigDecimal("10.00"), BigDecimal.ZERO, FUTURE_DATE, true);
        BigDecimal subtotal = new BigDecimal("500.00");
        BigDecimal discount = d.calculateDiscount(subtotal);
        // 10% of 500 = 50  (use compareTo to ignore BigDecimal scale differences)
        assertEquals(0, discount.compareTo(new BigDecimal("50")),
            "Expected 50 but got " + discount);
    }

    @Test
    @DisplayName("PERCENTAGE coupon should not exceed subtotal")
    void testPercentageDiscountCappedAtSubtotal() {
        // 200% discount — should be capped at subtotal
        Discount d = new Discount(1, "MEGA", Discount.Type.PERCENTAGE,
            new BigDecimal("200.00"), BigDecimal.ZERO, FUTURE_DATE, true);
        BigDecimal subtotal = new BigDecimal("300.00");
        BigDecimal discount = d.calculateDiscount(subtotal);
        assertEquals(subtotal, discount);  // capped
    }

    // ------------------------------------------------------------------ //
    //  Fixed discount
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("FIXED coupon should apply flat discount")
    void testFixedDiscount() {
        Discount d = new Discount(2, "FLAT50", Discount.Type.FIXED,
            new BigDecimal("50.00"), new BigDecimal("200.00"), FUTURE_DATE, true);
        BigDecimal subtotal = new BigDecimal("400.00");
        BigDecimal discount = d.calculateDiscount(subtotal);
        assertEquals(new BigDecimal("50.00"), discount);
    }

    @Test
    @DisplayName("FIXED coupon should not exceed subtotal")
    void testFixedDiscountCappedAtSubtotal() {
        Discount d = new Discount(2, "BIG", Discount.Type.FIXED,
            new BigDecimal("500.00"), BigDecimal.ZERO, FUTURE_DATE, true);
        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal discount = d.calculateDiscount(subtotal);
        assertEquals(subtotal, discount);  // capped at 100
    }

    // ------------------------------------------------------------------ //
    //  Expiry validation
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Expired coupon isNotExpired() returns false")
    void testExpiredCoupon() {
        Discount d = new Discount(3, "EXP", Discount.Type.PERCENTAGE,
            new BigDecimal("10.00"), BigDecimal.ZERO, PAST_DATE, true);
        assertFalse(d.isNotExpired());
    }

    @Test
    @DisplayName("Valid coupon isNotExpired() returns true")
    void testValidCouponNotExpired() {
        Discount d = new Discount(3, "GOOD", Discount.Type.PERCENTAGE,
            new BigDecimal("10.00"), BigDecimal.ZERO, FUTURE_DATE, true);
        assertTrue(d.isNotExpired());
    }

    // ------------------------------------------------------------------ //
    //  Minimum order validation
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Coupon not applicable when subtotal below minimum")
    void testCouponBelowMinimumOrderAmount() {
        Discount d = new Discount(4, "MIN500", Discount.Type.PERCENTAGE,
            new BigDecimal("10.00"), new BigDecimal("500.00"), FUTURE_DATE, true);
        BigDecimal subtotal = new BigDecimal("300.00");
        assertFalse(d.isApplicable(subtotal));
    }

    @Test
    @DisplayName("Coupon applicable when subtotal meets minimum exactly")
    void testCouponAtMinimumOrderAmount() {
        Discount d = new Discount(4, "MIN500", Discount.Type.PERCENTAGE,
            new BigDecimal("10.00"), new BigDecimal("500.00"), FUTURE_DATE, true);
        BigDecimal subtotal = new BigDecimal("500.00");
        assertTrue(d.isApplicable(subtotal));
    }

    // ------------------------------------------------------------------ //
    //  Disabled coupon
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Disabled coupon is not applicable")
    void testDisabledCoupon() {
        Discount d = new Discount(5, "OFF", Discount.Type.FIXED,
            new BigDecimal("30.00"), BigDecimal.ZERO, FUTURE_DATE, false);
        assertFalse(d.isApplicable(new BigDecimal("100.00")));
    }

    // ------------------------------------------------------------------ //
    //  Final amount calculation
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Final amount = subtotal minus discount")
    void testFinalAmountCalculation() {
        Discount d = new Discount(6, "TENOFF", Discount.Type.PERCENTAGE,
            new BigDecimal("10.00"), BigDecimal.ZERO, FUTURE_DATE, true);
        BigDecimal subtotal  = new BigDecimal("200.00");
        BigDecimal discount  = d.calculateDiscount(subtotal);
        BigDecimal finalAmt  = subtotal.subtract(discount).max(BigDecimal.ZERO);
        // 200 - 20 = 180 (compareTo ignores scale)
        assertEquals(0, finalAmt.compareTo(new BigDecimal("180")),
            "Expected 180 but got " + finalAmt);
    }

    @Test
    @DisplayName("Final amount never goes below zero")
    void testFinalAmountNeverNegative() {
        Discount d = new Discount(7, "BIG", Discount.Type.FIXED,
            new BigDecimal("1000.00"), BigDecimal.ZERO, FUTURE_DATE, true);
        BigDecimal subtotal = new BigDecimal("50.00");
        BigDecimal discount = d.calculateDiscount(subtotal); // capped at 50
        BigDecimal finalAmt = subtotal.subtract(discount).max(BigDecimal.ZERO);
        // result should be 0 (compareTo ignores scale)
        assertEquals(0, finalAmt.compareTo(BigDecimal.ZERO),
            "Expected 0 but got " + finalAmt);
    }
}
