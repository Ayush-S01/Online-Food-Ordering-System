package com.foodordering.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password hashing.
 *
 * <p>Uses SHA-256 (the same algorithm used in the database seed script).
 * For production, prefer BCrypt — SHA-256 without salting is shown here
 * to match the MySQL {@code SHA2()} seed approach used in this mini-project.
 *
 * <p>JDBC safety: passwords are never stored or transmitted in plain text.
 */
public final class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Returns the lowercase hex SHA-256 hash of the given plain-text password.
     *
     * @param plainText the raw password.
     * @return 64-character hex string.
     * @throws RuntimeException if SHA-256 is not available (extremely unlikely).
     */
    public static String hash(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored hash.
     *
     * @param plainText   raw password entered by the user.
     * @param storedHash  hash retrieved from the database.
     * @return {@code true} if they match.
     */
    public static boolean verify(String plainText, String storedHash) {
        return hash(plainText).equalsIgnoreCase(storedHash);
    }
}
