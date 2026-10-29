package com.foodordering;

/**
 * Entry point wrapper for JavaFX application execution.
 *
 * <p>In JavaFX 11+, launching an uber-jar directly from a class that extends
 * {@link javafx.application.Application} triggers the JVM launcher check
 * ("Error: JavaFX runtime components are missing").
 *
 * <p>Starting from this standalone launcher class bypasses this check and
 * allows the shaded JavaFX libraries to initialize cleanly.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
