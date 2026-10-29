package com.foodordering.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Automatically initializes database schema and sample data on application startup
 * if the required tables do not already exist.
 *
 * <p>This provides a seamless experience for college project demonstrations:
 * as long as MySQL is running, the application can auto-seed the schema and Indian menu items.
 */
public class DatabaseInitializer {

    /**
     * Checks if the 'food_items' table exists. If not, runs schema.sql and sample-data.sql.
     * If the table already exists, ensures image_path fields are populated.
     */
    public static synchronized void initializeIfNeeded() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (!tablesExist(conn)) {
                System.out.println("[DatabaseInitializer] Initializing schema and sample data...");
                executeSqlScript(conn, "/schema.sql");
                executeSqlScript(conn, "/sample-data.sql");
                System.out.println("[DatabaseInitializer] Database initialized successfully.");
            } else {
                syncMissingImagePaths(conn);
            }
        } catch (Exception e) {
            // Log warning, allow normal operation to proceed so detailed error dialogs show up in UI
            System.err.println("[DatabaseInitializer] Auto-initialization check notice: " + e.getMessage());
        }
    }

    private static void syncMissingImagePaths(Connection conn) {
        String query = "SELECT food_id, name FROM food_items WHERE image_path IS NULL OR image_path = ''";
        String update = "UPDATE food_items SET image_path = ? WHERE food_id = ?";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             PreparedStatement ps = conn.prepareStatement(update)) {
            boolean hasUpdates = false;
            while (rs.next()) {
                int id = rs.getInt("food_id");
                String name = rs.getString("name");
                String slug = com.foodordering.util.FoodCardBuilder.toImageSlug(name) + ".png";
                ps.setString(1, slug);
                ps.setInt(2, id);
                ps.addBatch();
                hasUpdates = true;
            }
            if (hasUpdates) {
                ps.executeBatch();
                System.out.println("[DatabaseInitializer] Auto-synced dish image filenames in database.");
            }
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Image sync notice: " + e.getMessage());
        }
    }

    private static boolean tablesExist(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'food_items'")) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private static void executeSqlScript(Connection conn, String resourcePath) {
        try (InputStream in = DatabaseInitializer.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                System.err.println("[DatabaseInitializer] Resource not found: " + resourcePath);
                return;
            }
            String content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Split on semicolons outside of string literals (basic SQL delimiter split)
            String[] statements = content.split(";\\s*(\\r?\\n|$)");
            try (Statement stmt = conn.createStatement()) {
                for (String raw : statements) {
                    String trimmed = raw.trim();
                    // Skip empty lines, comments, and USE/CREATE DATABASE (already connected to DB)
                    if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("/*")) {
                        continue;
                    }
                    if (trimmed.toUpperCase().startsWith("CREATE DATABASE")
                            || trimmed.toUpperCase().startsWith("USE ")) {
                        continue;
                    }
                    try {
                        stmt.execute(trimmed);
                    } catch (SQLException ex) {
                        System.err.println("[DatabaseInitializer] Skipped statement: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Error executing " + resourcePath + ": " + e.getMessage());
        }
    }
}
