package com.foodordering.util;

import com.foodordering.model.Customer;

/**
 * Singleton session manager that holds the currently logged-in customer.
 *
 * <p>Provides a simple, application-wide, in-memory session.
 * Cleared on logout.
 */
public final class SessionManager {

    private static SessionManager instance;
    private Customer currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /** Sets the currently logged-in user. */
    public void login(Customer customer) {
        this.currentUser = customer;
    }

    /** Clears the session (logout). */
    public void logout() {
        this.currentUser = null;
    }

    /** Returns the currently logged-in customer, or {@code null} if none. */
    public Customer getCurrentUser() {
        return currentUser;
    }

    /** Returns {@code true} if a user is logged in. */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Returns {@code true} if the logged-in user has the ADMIN role. */
    public boolean isAdmin() {
        return isLoggedIn() && currentUser.isAdmin();
    }
}
