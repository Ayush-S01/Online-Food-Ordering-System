package com.foodordering.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton-style JDBC connection utility.
 *
 * <p>Reads database credentials from {@code db.properties} on the classpath.
 * A new Connection is returned for each call to {@link #getConnection()} —
 * use try-with-resources to ensure it is closed promptly.
 *
 * <p>JDBC safety practices used here:
 * <ul>
 *   <li>Credentials stored in a properties file, not hard-coded.</li>
 *   <li>DriverManager used (lightweight; suitable for a desktop mini-project).</li>
 * </ul>
 */
public class DatabaseConnection {

    private static final String PROPS_FILE = "/db.properties";

    private static String url;
    private static String username;
    private static String password;
    private static boolean loaded = false;

    /** Load properties from external db.properties in current dir, or classpath. */
    private static synchronized void loadProperties() {
        if (loaded) return;
        Properties props = new Properties();

        // 1. Try external file in working directory first
        java.io.File externalFile = new java.io.File("db.properties");
        if (externalFile.exists()) {
            try (InputStream in = new java.io.FileInputStream(externalFile)) {
                props.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load external db.properties: " + e.getMessage(), e);
            }
        } else {
            // 2. Fall back to classpath resource
            try (InputStream in = DatabaseConnection.class.getResourceAsStream(PROPS_FILE)) {
                if (in == null) {
                    throw new RuntimeException(
                        "db.properties not found on classpath or in working directory. "
                        + "Copy db.properties.example to db.properties and fill in your credentials.");
                }
                props.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load db.properties: " + e.getMessage(), e);
            }
        }

        url      = props.getProperty("db.url");
        username = props.getProperty("db.username");
        password = props.getProperty("db.password");
        loaded   = true;
    }

    /**
     * Opens and returns a new JDBC Connection.
     *
     * <p>Callers MUST close the connection (best done via try-with-resources).
     *
     * @return a new {@link Connection}.
     * @throws SQLException if the connection cannot be established.
     */
    public static Connection getConnection() throws SQLException {
        loadProperties();
        return DriverManager.getConnection(url, username, password);
    }

    /** Private constructor — utility class, no instances needed. */
    private DatabaseConnection() {}
}
