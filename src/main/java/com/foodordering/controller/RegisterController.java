package com.foodordering.controller;

import com.foodordering.model.Customer;
import com.foodordering.service.AuthService;
import com.foodordering.util.AlertUtil;
import com.foodordering.util.GoogleAuthDialog;
import com.foodordering.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Controller for the customer registration screen.
 */
public class RegisterController {

    @FXML private TextField     nameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button        registerButton;
    @FXML private Button        googleSignupButton;
    @FXML private Hyperlink     loginLink;
    @FXML private Label         errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleGoogleSignup(ActionEvent event) {
        errorLabel.setVisible(false);
        Stage currentStage = (Stage) registerButton.getScene().getWindow();
        GoogleAuthDialog dialog = new GoogleAuthDialog();
        Optional<Customer> result = dialog.show(currentStage);
        result.ifPresent(customer -> {
            try {
                NavigationUtil.navigateTo(currentStage, "/fxml/Home.fxml");
            } catch (Exception ex) {
                AlertUtil.showError("Navigation Error", "Could not load Home screen: " + ex.getMessage());
            }
        });
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        errorLabel.setVisible(false);
        String name    = nameField.getText() != null ? nameField.getText().trim() : "";
        String email   = emailField.getText() != null ? emailField.getText().trim() : "";
        String pass    = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (name.isEmpty()) {
            errorLabel.setText("Please enter your full name.");
            errorLabel.setVisible(true);
            nameField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            errorLabel.setText("Please enter your email address.");
            errorLabel.setVisible(true);
            emailField.requestFocus();
            return;
        }

        if (pass == null || pass.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters.");
            errorLabel.setVisible(true);
            passwordField.requestFocus();
            return;
        }

        if (!pass.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            errorLabel.setVisible(true);
            confirmPasswordField.requestFocus();
            return;
        }

        try {
            Customer customer = authService.register(name, email, pass);
            AlertUtil.showInfo("Registration Successful",
                "Welcome to FoodHub, " + customer.getName() + "! Your account has been created.");
            Stage stage = (Stage) registerButton.getScene().getWindow();
            NavigationUtil.navigateTo(stage, "/fxml/Home.fxml");
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e instanceof java.sql.SQLException || (msg != null && msg.contains("Communications link"))) {
                errorLabel.setText("Database error: Ensure MySQL is running & check db.properties.");
            } else {
                errorLabel.setText(msg != null ? msg : "An unexpected error occurred.");
            }
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleLoginLink(ActionEvent event) {
        try {
            Stage stage = (Stage) loginLink.getScene().getWindow();
            NavigationUtil.navigateTo(stage, "/fxml/Login.fxml");
        } catch (Exception e) {
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }
}
