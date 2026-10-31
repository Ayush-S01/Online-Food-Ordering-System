package com.foodordering.model;

import java.time.LocalDateTime;

/**
 * Represents an administrative user with elevated system management privileges.
 *
 * <p>Demonstrates Core Java OOP Principles:
 * <ul>
 *   <li><b>Inheritance</b>: Extends {@link User} to share common credential and identity attributes.</li>
 *   <li><b>Polymorphism</b>: Implements {@link #getUserTypeDescription()} with admin-specific capabilities.</li>
 *   <li><b>Encapsulation</b>: Manages admin identifier accessors with encapsulation.</li>
 * </ul>
 */
public class Admin extends User {

    // ------------------------------------------------------------------ //
    //  Constructors (Demonstrating super() constructor chaining)
    // ------------------------------------------------------------------ //

    public Admin() {
        super();
        this.role = "ADMIN";
    }

    public Admin(int adminId, String name, String email,
                 String passwordHash, String role, LocalDateTime createdAt) {
        super(adminId, name, email, passwordHash, role, createdAt);
    }

    // ------------------------------------------------------------------ //
    //  Encapsulation — Accessors for Admin ID
    // ------------------------------------------------------------------ //

    public int getAdminId()                       { return getId(); }
    public void setAdminId(int id)                { setId(id); }

    // ------------------------------------------------------------------ //
    //  Polymorphic Method Implementation (Method Overriding)
    // ------------------------------------------------------------------ //

    @Override
    public String getUserTypeDescription() {
        return "Administrator: Has elevated authority to manage food menus, process customer orders, update delivery statuses, and configure coupons.";
    }

    @Override
    public String toString() {
        return "Admin{id=" + getId() + ", name='" + getName() + "', email='" + getEmail() + "', role='" + getRole() + "'}";
    }
}
