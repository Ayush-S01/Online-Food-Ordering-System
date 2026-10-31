package com.foodordering.service;

import java.math.BigDecimal;

/**
 * Concrete Strategy for Credit & Debit Card payments.
 *
 * <p>Demonstrates OOP:
 * <ul>
 *   <li><b>Polymorphism</b>: Implements {@link PaymentStrategy} to handle tokenized card processing.</li>
 *   <li><b>Encapsulation</b>: Keeps cardholder and token metadata safely encapsulated.</li>
 * </ul>
 */
public class CardPaymentStrategy implements PaymentStrategy {

    private final String cardType; // "Credit Card" or "Debit Card"
    private final String last4Digits;

    public CardPaymentStrategy() {
        this("Credit Card", "4242");
    }

    public CardPaymentStrategy(String cardType, String last4Digits) {
        this.cardType    = cardType != null ? cardType : "Card";
        this.last4Digits = last4Digits != null ? last4Digits : "XXXX";
    }

    @Override
    public boolean processPayment(BigDecimal amount, int orderId) throws Exception {
        // Simulated card gateway authorization
        System.out.println("[" + cardType.toUpperCase() + "] Charging ₹" + amount
                + " to Card ending in " + last4Digits + " for Order #" + orderId);
        Thread.sleep(400);
        return true;
    }

    @Override
    public String getMethodName() {
        return cardType + " (ending in " + last4Digits + ")";
    }

    public String getCardType()    { return cardType; }
    public String getLast4Digits() { return last4Digits; }
}
