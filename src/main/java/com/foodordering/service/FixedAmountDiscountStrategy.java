package com.foodordering.service;

import java.math.BigDecimal;

/**
 * Concrete Strategy computing a flat/fixed monetary discount.
 *
 * <p>Formula: {@code discount = fixedAmount}, capped at {@code subtotal}.
 */
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    private final BigDecimal fixedAmount;

    public FixedAmountDiscountStrategy(double fixedAmount) {
        this(BigDecimal.valueOf(fixedAmount));
    }

    public FixedAmountDiscountStrategy(BigDecimal fixedAmount) {
        if (fixedAmount == null || fixedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fixed discount amount must be non-negative.");
        }
        this.fixedAmount = fixedAmount;
    }

    @Override
    public BigDecimal calculate(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return fixedAmount.min(subtotal);
    }

    @Override
    public String getDescription() {
        return "₹" + fixedAmount.stripTrailingZeros().toPlainString() + " OFF";
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }
}
