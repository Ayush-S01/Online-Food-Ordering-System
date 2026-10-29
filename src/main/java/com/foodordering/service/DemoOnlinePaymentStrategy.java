package com.foodordering.service;

import java.math.BigDecimal;

/**
 * Demo (simulated) online payment strategy.
 *
 * <p>OOP — Polymorphism: implements {@link PaymentStrategy}.
 *
 * <p>IMPORTANT: This is a SIMULATION ONLY.
 * No real card numbers, CVVs, or payment gateway credentials are used or stored.
 * This class exists purely for demonstration purposes in a college project.
 */
public class DemoOnlinePaymentStrategy implements PaymentStrategy {

    @Override
    public boolean processPayment(BigDecimal amount, int orderId) throws Exception {
        // Simulate a brief network delay (demo only)
        Thread.sleep(500);
        // In a real application this would call a payment gateway API.
        System.out.println("[DEMO ONLINE] Simulated payment of ₹" + amount
                           + " SUCCESSFUL for Order #" + orderId);
        return true;
    }

    @Override
    public String getMethodName() {
        return "Demo Online Payment";
    }
}
