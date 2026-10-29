package com.foodordering.service;

import java.math.BigDecimal;

/**
 * Cash on Delivery payment strategy.
 *
 * <p>OOP — Polymorphism: implements {@link PaymentStrategy}.
 * Payment is always "accepted" immediately; actual collection happens at delivery.
 */
public class CashOnDeliveryStrategy implements PaymentStrategy {

    @Override
    public boolean processPayment(BigDecimal amount, int orderId) {
        // COD: no real processing — payment confirmed at doorstep
        System.out.println("[COD] Payment of ₹" + amount + " pending for Order #" + orderId);
        return true;
    }

    @Override
    public String getMethodName() {
        return "Cash on Delivery";
    }
}
