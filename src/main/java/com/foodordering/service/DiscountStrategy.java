package com.foodordering.service;

import java.math.BigDecimal;

/**
 * Strategy interface for promotional discount calculation algorithms.
 *
 * <p>Demonstrates OOP:
 * <ul>
 *   <li><b>Abstraction</b>: Defines the contract for discount calculation without exposing algorithm details.</li>
 *   <li><b>Polymorphism</b>: Concrete strategies implement different computational formulas.</li>
 * </ul>
 */
public interface DiscountStrategy {

    /**
     * Calculates the monetary discount amount on a given order subtotal.
     *
     * @param subtotal the subtotal before discount.
     * @return the discount amount (never negative, never exceeding subtotal).
     */
    BigDecimal calculate(BigDecimal subtotal);

    /** Returns the display label of this discount rule. */
    String getDescription();
}
