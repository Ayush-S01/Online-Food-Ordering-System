package com.foodordering.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete Strategy computing a proportional percentage discount.
 *
 * <p>Formula: {@code discount = subtotal * (percentage / 100)}, capped at {@code subtotal}.
 */
public class PercentageDiscountStrategy implements DiscountStrategy {

    private final BigDecimal percentage;

    public PercentageDiscountStrategy(double percentage) {
        this(BigDecimal.valueOf(percentage));
    }

    public PercentageDiscountStrategy(BigDecimal percentage) {
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Percentage must be non-negative.");
        }
        this.percentage = percentage;
    }

    @Override
    public BigDecimal calculate(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rawDiscount = subtotal.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return rawDiscount.min(subtotal);
    }

    @Override
    public String getDescription() {
        return percentage.stripTrailingZeros().toPlainString() + "% OFF";
    }

    public BigDecimal getPercentage() {
        return percentage;
    }
}
