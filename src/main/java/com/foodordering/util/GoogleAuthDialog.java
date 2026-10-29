package com.foodordering.util;

import com.foodordering.model.Customer;
import com.foodordering.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;
import java.util.Optional;

/**
 * Modern Google Sign-In & Sign-Up Account Chooser dialog.
 * Presents authentic Google Material Design styling for seamless 1-click Google authentication.
 */
public class GoogleAuthDialog {

    private final AuthService authService = new AuthService();
    private Customer authenticatedCustomer = null;

    /**
     * Shows the Google Account Chooser dialog modal.
     *
     * @param owner the parent Stage.
     * @return Optional containing the authenticated Customer if signed in, or empty if cancelled.
     */
    public Optional<Customer> show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.setTitle("Sign in with Google - FoodHub");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(28, 32, 24, 32));
        root.setPrefWidth(440);
        root.setStyle("-fx-background-color: #FFFFFF; -fx-font-family: 'Segoe UI', system-ui, sans-serif;");

        // 1. Google "G" Logo
        ImageView logoView = null;
        try (InputStream in = getClass().getResourceAsStream("/images/google_logo.png")) {
            if (in != null) {
                logoView = new ImageView(new Image(in));
                logoView.setFitWidth(36);
                logoView.setFitHeight(36);
                logoView.setPreserveRatio(true);
            }
        } catch (Exception ignored) {}

        if (logoView != null) {
            root.getChildren().add(logoView);
        }

        // 2. Headers
        VBox headerBox = new VBox(4);
        headerBox.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Choose an account");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #202124;");
        Label subLabel = new Label("to continue to FoodHub");
        subLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5F6368;");
        headerBox.getChildren().addAll(titleLabel, subLabel);
        root.getChildren().add(headerBox);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-padding: 4 0;");
        root.getChildren().add(sep1);

        // Status Label for error / progress
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #D93025;");
        statusLabel.setWrapText(true);
        statusLabel.setVisible(false);

        // 3. Accounts List Container
        VBox accountsBox = new VBox(8);
        accountsBox.setAlignment(Pos.CENTER);
        accountsBox.setMaxWidth(Double.MAX_VALUE);

        // --- Account 1: Ayush Satyam ---
        HBox ayushCard = createAccountRow(
                "A", "#7B1FA2",
                "Ayush Satyam",
                "ayushsatyam012006@gmail.com",
                () -> authenticateGoogleUser(dialog, statusLabel, "Ayush Satyam", "ayushsatyam012006@gmail.com")
        );

        // --- Account 2: Other Google Account Form Container ---
        VBox customContainer = new VBox(10);
        customContainer.setAlignment(Pos.CENTER);
        customContainer.setVisible(false);
        customContainer.setManaged(false);
        customContainer.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 14; -fx-background-radius: 8; -fx-border-color: #DADCE0; -fx-border-radius: 8;");

        TextField nameField = new TextField();
        nameField.setPromptText("Your Full Name (e.g. Priya Sharma)");
        nameField.setStyle("-fx-background-color: white; -fx-border-color: #DADCE0; -fx-border-radius: 6; -fx-padding: 8 12;");

        TextField emailField = new TextField();
        emailField.setPromptText("Google Email (e.g. user@gmail.com)");
        emailField.setStyle("-fx-background-color: white; -fx-border-color: #DADCE0; -fx-border-radius: 6; -fx-padding: 8 12;");

        Button submitCustomBtn = new Button("Sign In with Google Account →");
        submitCustomBtn.setMaxWidth(Double.MAX_VALUE);
        submitCustomBtn.setStyle("-fx-background-color: #1A73E8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 9 16; -fx-background-radius: 6; -fx-cursor: hand;");
        submitCustomBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            if (email.isEmpty() || !email.contains("@")) {
                statusLabel.setText("Please enter a valid Google email address.");
                statusLabel.setVisible(true);
                return;
            }
            authenticateGoogleUser(dialog, statusLabel, name.isEmpty() ? "Google User" : name, email);
        });

        customContainer.getChildren().addAll(new Label("Enter your Google Account details:"), nameField, emailField, submitCustomBtn);

        // --- Account 3: Use Another Account Row ---
        HBox anotherCard = createAccountRow(
                "+", "#1A73E8",
                "Use another account",
                "Sign in with a different Google email",
                () -> {
                    customContainer.setVisible(true);
                    customContainer.setManaged(true);
                    nameField.requestFocus();
                }
        );

        accountsBox.getChildren().addAll(ayushCard, anotherCard, customContainer);
        root.getChildren().addAll(statusLabel, accountsBox);

        // 4. Footer Note & Disclaimer
        Separator sep2 = new Separator();
        sep2.setStyle("-fx-padding: 8 0 4 0;");

        Label disclaimer = new Label("To continue, Google will share your name and email address with FoodHub. Before using this app, review FoodHub's privacy policy and terms of service.");
        disclaimer.setStyle("-fx-font-size: 11px; -fx-text-fill: #5F6368; -fx-line-spacing: 1.2;");
        disclaimer.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1A73E8; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;");
        cancelBtn.setOnAction(e -> dialog.close());

        HBox footerBox = new HBox(cancelBtn);
        footerBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(sep2, disclaimer, footerBox);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();

        return Optional.ofNullable(authenticatedCustomer);
    }

    private HBox createAccountRow(String badge, String colorHex, String name, String email, Runnable onClick) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle("-fx-background-color: white; -fx-border-color: #DADCE0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");

        // Hover animation
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #4285F4; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-border-color: #DADCE0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"));

        // Avatar circle
        StackPane avatar = new StackPane();
        Circle bg = new Circle(18, Color.web(colorHex));
        Label letter = new Label(badge);
        letter.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 15px;");
        avatar.getChildren().addAll(bg, letter);

        VBox info = new VBox(2);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #202124;");
        Label emailLbl = new Label(email);
        emailLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #5F6368;");
        info.getChildren().addAll(nameLbl, emailLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("→");
        arrow.setStyle("-fx-font-size: 16px; -fx-text-fill: #9AA0A6;");

        row.getChildren().addAll(avatar, info, spacer, arrow);
        row.setOnMouseClicked(e -> onClick.run());
        return row;
    }

    private void authenticateGoogleUser(Stage dialog, Label statusLabel, String name, String email) {
        statusLabel.setVisible(true);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1A73E8;");
        statusLabel.setText("Connecting to FoodHub backend...");

        try {
            Customer customer = authService.loginWithGoogle(name, email);
            this.authenticatedCustomer = customer;
            dialog.close();
        } catch (Exception ex) {
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #D93025;");
            String msg = ex.getMessage();
            if (ex instanceof java.sql.SQLException || (msg != null && msg.contains("Communications link"))) {
                statusLabel.setText("Backend Error: MySQL connection failed. Please check database.");
            } else {
                statusLabel.setText(msg != null ? msg : "Google authentication failed.");
            }
        }
    }
}
