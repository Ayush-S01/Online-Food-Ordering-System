package com.foodordering.service;

import java.math.BigDecimal;

/**
 * Strategy interface for payment processing.
 *
 * <p>OOP — Polymorphism and Abstraction:
 * <ul>
 *   <li>Different payment methods share the same interface.</li>
 *   <li>The caller does not need to know which concrete strategy is used.</li>
 * </ul>
 *
 * <p>Implementations: {@link CashOnDeliveryStrategy}, {@link DemoOnlinePaymentStrategy}.
 */
public interface PaymentStrategy {

    /**
     * Processes the payment.
     *
     * @param amount   the amount to charge.
     * @param orderId  the ID of the order being paid.
     * @return {@code true} if the payment was successful.
     * @throws Exception if payment processing fails.
     */
    boolean processPayment(BigDecimal amount, int orderId) throws Exception;

    /** Returns a display-friendly name for this payment method. */
    String getMethodName();
}
