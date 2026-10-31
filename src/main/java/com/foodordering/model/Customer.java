package com.foodordering.model;

import java.time.LocalDateTime;

/**
 * Represents a registered customer in the system.
 *
 * <p>Demonstrates Core Java OOP Principles:
 * <ul>
 *   <li><b>Inheritance</b>: Extends {@link User} to inherit identity, authentication, and role fields.</li>
 *   <li><b>Polymorphism</b>: Overrides {@link #getUserTypeDescription()} with customer-specific capabilities.</li>
 *   <li><b>Encapsulation</b>: Exposes type-safe accessors and ensures backward compatibility for {@code customerId}.</li>
 * </ul>
 */
public class Customer extends User {

    // ------------------------------------------------------------------ //
    //  Constructors (Demonstrating super() constructor chaining)
    // ------------------------------------------------------------------ //

    public Customer() {
        super();
        this.role = "CUSTOMER";
    }

    public Customer(int customerId, String name, String email,
                    String passwordHash, String role, LocalDateTime createdAt) {
        super(customerId, name, email, passwordHash, role, createdAt);
    }

    // ------------------------------------------------------------------ //
    //  Encapsulation — Backward Compatible Accessors for Customer ID
    // ------------------------------------------------------------------ //

    public int getCustomerId()                    { return getId(); }
    public void setCustomerId(int id)             { setId(id); }

    // ------------------------------------------------------------------ //
    //  Polymorphic Method Implementation (Method Overriding)
    // ------------------------------------------------------------------ //

    @Override
    public String getUserTypeDescription() {
        return "Customer: Can browse multi-cuisine dishes, manage real-time cart, apply discount coupons, and place orders.";
    }

    @Override
    public String toString() {
        return "Customer{id=" + getId() + ", name='" + getName() + "', email='" + getEmail() + "', role='" + getRole() + "'}";
    }
}
