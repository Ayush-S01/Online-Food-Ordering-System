package com.foodordering.model;

import java.time.LocalDateTime;

/**
 * Abstract Base Class representing a generic user in the Online Food Ordering System.
 *
 * <p>Demonstrates Core Java OOP Principles:
 * <ul>
 *   <li><b>Abstraction</b>: Hides user identity mechanics and defines the abstract contract {@link #getUserTypeDescription()}.</li>
 *   <li><b>Inheritance</b>: Extended by concrete subclasses {@link Customer} and {@link Admin}.</li>
 *   <li><b>Encapsulation</b>: All attributes are protected/private with validated getters and setters.</li>
 * </ul>
 */
public abstract class User {

    protected int id;
    protected String name;
    protected String email;
    protected String passwordHash;    // SHA-256 hex digest — never stored in plain text
    protected String role;            // "CUSTOMER" or "ADMIN"
    protected LocalDateTime createdAt;

    // ------------------------------------------------------------------ //
    //  Constructors (Demonstrating Constructor Overloading / Polymorphism)
    // ------------------------------------------------------------------ //

    public User() {}

    public User(int id, String name, String email, String passwordHash, String role, LocalDateTime createdAt) {
        this.id           = id;
        this.name         = name;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.createdAt    = createdAt;
    }

    // ------------------------------------------------------------------ //
    //  Encapsulation — Getters and Setters
    // ------------------------------------------------------------------ //

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public String getName()                         { return name; }
    public void setName(String name)                { this.name = name; }

    public String getEmail()                        { return email; }
    public void setEmail(String email)              { this.email = email; }

    public String getPasswordHash()                 { return passwordHash; }
    public void setPasswordHash(String hash)        { this.passwordHash = hash; }

    public String getRole()                         { return role; }
    public void setRole(String role)                { this.role = role; }

    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime time)    { this.createdAt = time; }

    // ------------------------------------------------------------------ //
    //  Abstraction & Polymorphism
    // ------------------------------------------------------------------ //

    /**
     * Abstract method that must be implemented by concrete user subclasses.
     * Demonstrates runtime polymorphism.
     *
     * @return a descriptive overview of the user's role and privileges.
     */
    public abstract String getUserTypeDescription();

    /** Checks whether this user has administrator privileges. */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
