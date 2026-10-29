package com.foodordering.dao;

import com.foodordering.database.DatabaseConnection;
import com.foodordering.model.Customer;
import com.foodordering.util.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data Access Object for {@link Customer}.
 *
 * <p>JDBC safety:
 * <ul>
 *   <li>All queries use PreparedStatement to prevent SQL injection.</li>
 *   <li>Resources are closed with try-with-resources.</li>
 *   <li>Passwords are compared via hash, never stored/returned in plain text.</li>
 * </ul>
 */
public class CustomerDAO {

    /**
     * Finds a customer by email and verifies the password.
     *
     * @param email     the customer's email.
     * @param plainPassword the plain-text password entered at login.
     * @return an Optional containing the Customer if credentials are valid.
     * @throws SQLException on database errors.
     */
    public Optional<Customer> authenticate(String email, String plainPassword)
            throws SQLException {
        String sql = "SELECT customer_id, name, email, password_hash, role, created_at "
                   + "FROM customers WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verify(plainPassword, storedHash)) {
                        return Optional.of(mapRow(rs));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Registers a new customer.
     *
     * @param name     customer name.
     * @param email    customer email (must be unique).
     * @param password plain-text password (will be hashed before storage).
     * @return the newly created Customer.
     * @throws SQLException if the email is already registered or a DB error occurs.
     */
    public Customer register(String name, String email, String password)
            throws SQLException {
        String sql = "INSERT INTO customers (name, email, password_hash, role) "
                   + "VALUES (?, ?, ?, 'CUSTOMER')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hash(password));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return findById(id)
                        .orElseThrow(() -> new SQLException(
                            "Customer created but could not be retrieved."));
                }
            }
        }
        throw new SQLException("Registration failed: no ID returned.");
    }

    /**
     * Finds a customer by primary key.
     */
    public Optional<Customer> findById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, name, email, password_hash, role, created_at "
                   + "FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a customer by email address.
     */
    public Optional<Customer> findByEmail(String email) throws SQLException {
        String sql = "SELECT customer_id, name, email, password_hash, role, created_at "
                   + "FROM customers WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Authenticates or registers a customer via Google OAuth.
     * If the account already exists, returns the existing record.
     * If it is a new Google account, automatically creates a new customer row.
     */
    public Customer registerOrLoginGoogleUser(String name, String email) throws SQLException {
        Optional<Customer> existing = findByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }

        String sql = "INSERT INTO customers (name, email, password_hash, role) "
                   + "VALUES (?, ?, ?, 'CUSTOMER')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name != null && !name.isBlank() ? name : "Google User");
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hash("GOOGLE_OAUTH_" + email));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return findById(id).orElseThrow(() -> new SQLException("Google user created but not found."));
                }
            }
        }
        throw new SQLException("Google authentication registration failed.");
    }

    /**
     * Checks whether an email is already registered.
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM customers WHERE email = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Mapping helper
    // ------------------------------------------------------------------ //

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPasswordHash(rs.getString("password_hash"));
        c.setRole(rs.getString("role"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}
