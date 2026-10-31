package com.foodordering.model;

import com.foodordering.service.DiscountStrategy;
import com.foodordering.service.FixedAmountDiscountStrategy;
import com.foodordering.service.PercentageDiscountStrategy;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a discount / coupon that can be applied at checkout.
 *
 * <p>Supports two types:
 * <ul>
 *   <li>{@code PERCENTAGE} — e.g., 10 % off the subtotal.</li>
 *   <li>{@code FIXED}      — e.g., ₹50 off the subtotal.</li>
 * </ul>
 *
 * <p>OOP: Encapsulation + meaningful domain methods for validation.
 */
public class Discount {

    /** Discount type constants. */
    public enum Type { PERCENTAGE, FIXED }

    private int discountId;
    private String couponCode;
    private Type discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderAmount;
    private LocalDate expiryDate;
    private boolean enabled;

    // ------------------------------------------------------------------ //
    //  Constructors
    // ------------------------------------------------------------------ //

    public Discount() {}

    public Discount(int discountId, String couponCode, Type discountType,
                    BigDecimal discountValue, BigDecimal minimumOrderAmount,
                    LocalDate expiryDate, boolean enabled) {
        this.discountId          = discountId;
        this.couponCode          = couponCode;
        this.discountType        = discountType;
        this.discountValue       = discountValue;
        this.minimumOrderAmount  = minimumOrderAmount;
        this.expiryDate          = expiryDate;
        this.enabled             = enabled;
    }

    // ------------------------------------------------------------------ //
    //  Getters & Setters
    // ------------------------------------------------------------------ //

    public int getDiscountId()                          { return discountId;          }
    public void setDiscountId(int id)                   { this.discountId = id;       }

    public String getCouponCode()                       { return couponCode;          }
    public void setCouponCode(String code)              { this.couponCode = code;     }

    public Type getDiscountType()                       { return discountType;        }
    public void setDiscountType(Type type)              { this.discountType = type;   }

    public BigDecimal getDiscountValue()                { return discountValue;       }
    public void setDiscountValue(BigDecimal value)      { this.discountValue = value; }

    public BigDecimal getMinimumOrderAmount()           { return minimumOrderAmount;          }
    public void setMinimumOrderAmount(BigDecimal min)   { this.minimumOrderAmount = min;       }

    public LocalDate getExpiryDate()                    { return expiryDate;          }
    public void setExpiryDate(LocalDate date)           { this.expiryDate = date;     }

    public boolean isEnabled()                          { return enabled;             }
    public void setEnabled(boolean enabled)             { this.enabled = enabled;     }

    // ------------------------------------------------------------------ //
    //  Domain validation helpers
    // ------------------------------------------------------------------ //

    /**
     * Returns a user-friendly display label for chips, buttons, and promo banners.
     * E.g. "WELCOME10 (10% OFF)" or "SAVE50 (₹50 OFF)".
     */
    public String getDisplayLabel() {
        String valStr = discountValue != null ? discountValue.stripTrailingZeros().toPlainString() : "0";
        if (discountType == Type.PERCENTAGE) {
            return couponCode + " (" + valStr + "% OFF)";
        } else {
            return couponCode + " (₹" + valStr + " OFF)";
        }
    }

    /** Returns {@code true} if the coupon has not yet expired. */
    public boolean isNotExpired() {
        return !expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Returns {@code true} if this coupon is fully valid and applicable.
     *
     * @param orderSubtotal the cart subtotal before discount.
     */
    public boolean isApplicable(BigDecimal orderSubtotal) {
        return enabled
            && isNotExpired()
            && orderSubtotal.compareTo(minimumOrderAmount) >= 0;
    }

    /**
     * Computes the discount amount for the given subtotal.
     *
     * <p>Algorithm:
     * <ul>
     *   <li>PERCENTAGE: amount = subtotal × (value / 100); capped at subtotal.</li>
     *   <li>FIXED: amount = discountValue; capped at subtotal so final &ge; 0.</li>
     * </ul>
     *
     * @param subtotal cart subtotal before discount.
     * @return computed discount amount (never exceeds subtotal).
     */
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        BigDecimal amount;
        if (discountType == Type.PERCENTAGE) {
            // percentage discount
            amount = subtotal.multiply(discountValue)
                             .divide(BigDecimal.valueOf(100));
        } else {
            // fixed discount
            amount = discountValue;
        }
        // cap: discount cannot exceed the subtotal
        return amount.min(subtotal);
    }

    /**
     * Returns a polymorphic {@link DiscountStrategy} instance for this discount.
     * Demonstrates the Strategy Pattern (OOP Polymorphism).
     */
    public DiscountStrategy toStrategy() {
        if (discountType == Type.PERCENTAGE) {
            return new PercentageDiscountStrategy(discountValue);
        } else {
            return new FixedAmountDiscountStrategy(discountValue);
        }
    }

    @Override
    public String toString() {
        return "Discount{code='" + couponCode + "', type=" + discountType
               + ", value=" + discountValue + '}';
    }
}
