package com.foodordering.service;

import java.math.BigDecimal;

/**
 * Concrete Strategy for Unified Payments Interface (UPI) payments.
 *
 * <p>Demonstrates OOP:
 * <ul>
 *   <li><b>Polymorphism</b>: Implements {@link PaymentStrategy} to provide UPI-specific processing logic.</li>
 *   <li><b>Abstraction</b>: Hides simulated UPI protocol handshakes and VPA authorization behind the interface contract.</li>
 * </ul>
 */
public class UpiPaymentStrategy implements PaymentStrategy {

    private final String upiId;

    public UpiPaymentStrategy() {
        this("foodhub@upi");
    }

    public UpiPaymentStrategy(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public boolean processPayment(BigDecimal amount, int orderId) throws Exception {
        // Simulated UPI instant settlement
        System.out.println("[UPI] Processing ₹" + amount + " via " + upiId + " for Order #" + orderId);
        Thread.sleep(300);
        return true;
    }

    @Override
    public String getMethodName() {
        return "UPI Payment (" + upiId + ")";
    }

    public String getUpiId() {
        return upiId;
    }
}
