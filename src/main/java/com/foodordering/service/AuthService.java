package com.foodordering.service;

import com.foodordering.dao.CustomerDAO;
import com.foodordering.model.Customer;
import com.foodordering.util.SessionManager;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service for authentication and registration.
 *
 * <p>Business rules:
 * <ul>
 *   <li>Email must be non-blank and contain '@'.</li>
 *   <li>Password must be at least 6 characters.</li>
 *   <li>Duplicate emails are rejected.</li>
 *   <li>Role-based session is set after successful login.</li>
 * </ul>
 */
public class AuthService {

    private final CustomerDAO customerDAO = new CustomerDAO();

    /**
     * Authenticates a customer and stores them in the session.
     *
     * @param email    login email.
     * @param password plain-text password.
     * @return the logged-in Customer.
     * @throws Exception if credentials are wrong or a DB error occurs.
     */
    public Customer login(String email, String password) throws Exception {
        validateEmail(email);
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        Optional<Customer> opt = customerDAO.authenticate(email.trim(), password);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        Customer customer = opt.get();
        SessionManager.getInstance().login(customer);
        return customer;
    }

    /**
     * Registers a new customer account.
     *
     * @param name     full name.
     * @param email    unique email.
     * @param password plain-text password (at least 6 characters).
     * @return the newly created Customer.
     * @throws Exception on validation failure or DB error.
     */
    public Customer register(String name, String email, String password) throws Exception {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        validateEmail(email);
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        String trimmedEmail = email.trim();
        if (customerDAO.emailExists(trimmedEmail)) {
            throw new IllegalArgumentException("Email '" + trimmedEmail + "' is already registered.");
        }
        Customer customer = customerDAO.register(name.trim(), trimmedEmail, password);
        SessionManager.getInstance().login(customer);
        return customer;
    }

    /**
     * Authenticates or registers a user via Google Sign-In.
     * Sets the active session on success.
     *
     * @param name  full name from Google profile.
     * @param email verified email address from Google.
     * @return the logged-in Customer.
     * @throws Exception if validation or database operation fails.
     */
    public Customer loginWithGoogle(String name, String email) throws Exception {
        validateEmail(email);
        String trimmedEmail = email.trim().toLowerCase();
        String displayName = (name != null && !name.isBlank()) ? name.trim() : trimmedEmail.split("@")[0];

        Customer customer = customerDAO.registerOrLoginGoogleUser(displayName, trimmedEmail);
        SessionManager.getInstance().login(customer);
        return customer;
    }

    /** Logs out the current user. */
    public void logout() {
        SessionManager.getInstance().logout();
    }

    // ------------------------------------------------------------------ //
    //  Validation
    // ------------------------------------------------------------------ //

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email address is not valid.");
        }
    }
}
