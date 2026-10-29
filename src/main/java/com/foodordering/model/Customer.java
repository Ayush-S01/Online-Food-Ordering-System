package com.foodordering.model;

import java.time.LocalDateTime;

/**
 * Represents a registered customer.
 *
 * <p>The {@code role} field is used for role-based access control in the service layer.
 * Possible values: "CUSTOMER", "ADMIN".
 *
 * <p>OOP: Encapsulation — fields are private, accessed via getters/setters.
 */
public class Customer {

    private int customerId;
    private String name;
    private String email;
    private String passwordHash;    // SHA-256 hex — never plain text
    private String role;            // "CUSTOMER" or "ADMIN"
    private LocalDateTime createdAt;

    // ------------------------------------------------------------------ //
    //  Constructors
    // ------------------------------------------------------------------ //

    public Customer() {}

    public Customer(int customerId, String name, String email,
                    String passwordHash, String role, LocalDateTime createdAt) {
        this.customerId   = customerId;
        this.name         = name;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.createdAt    = createdAt;
    }

    // ------------------------------------------------------------------ //
    //  Getters & Setters
    // ------------------------------------------------------------------ //

    public int getCustomerId()                    { return customerId;             }
    public void setCustomerId(int id)             { this.customerId = id;          }

    public String getName()                       { return name;                   }
    public void setName(String name)              { this.name = name;              }

    public String getEmail()                      { return email;                  }
    public void setEmail(String email)            { this.email = email;            }

    public String getPasswordHash()               { return passwordHash;           }
    public void setPasswordHash(String hash)      { this.passwordHash = hash;      }

    public String getRole()                       { return role;                   }
    public void setRole(String role)              { this.role = role;              }

    public LocalDateTime getCreatedAt()           { return createdAt;              }
    public void setCreatedAt(LocalDateTime t)     { this.createdAt = t;            }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    /** Returns {@code true} if this customer has the ADMIN role. */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "Customer{id=" + customerId + ", name='" + name + "', role=" + role + '}';
    }
}
