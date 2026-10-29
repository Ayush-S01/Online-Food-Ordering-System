package com.foodordering.controller;

import com.foodordering.model.Customer;
import com.foodordering.service.AuthService;
import com.foodordering.util.AlertUtil;
import com.foodordering.util.GoogleAuthDialog;
import com.foodordering.util.NavigationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Controller for the modern Bistro Bliss Landing & Authentication Screen.
 * Implements:
 * 1. Hero landing page matching the Bistro Bliss design.
 * 2. Top-right "Signin as admin" action.
 * 3. Hero "Sign In" and "Sign Up" actions with modal overlay dialogs.
 * 4. Google Sign-In and Sign-Up integration.
 */
public class LoginController {

    // Background & Overlay
    @FXML private ImageView bgImageView;
    @FXML private StackPane modalOverlay;
    @FXML private VBox      signInCard;
    @FXML private VBox      signUpCard;
    @FXML private VBox      adminCard;

    // Customer Sign In Fields
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Button        googleLoginButton;
    @FXML private Label         errorLabel;

    // Customer Sign Up Fields
    @FXML private TextField     regNameField;
    @FXML private TextField     regEmailField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private Button        registerSubmitButton;
    @FXML private Button        googleSignupButton;
    @FXML private Label         regErrorLabel;

    // Admin Sign In Fields
    @FXML private TextField     adminEmailField;
    @FXML private PasswordField adminPasswordField;
    @FXML private Button        adminLoginButton;
    @FXML private Label         adminErrorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        // Pre-fill demo credentials
        emailField.setText("ayush@example.com");
        passwordField.setText("customer123");

        adminEmailField.setText("admin@food.com");
        adminPasswordField.setText("Admin@123");

        // Allow pressing Enter in password fields to submit
        passwordField.setOnAction(this::handleLogin);
        regConfirmPasswordField.setOnAction(this::handleRegisterSubmit);
        adminPasswordField.setOnAction(this::handleAdminLogin);

        // Click outside modal card to close
        modalOverlay.setOnMouseClicked(event -> {
            if (event.getTarget() == modalOverlay) {
                handleCloseModal(null);
            }
        });

        // Bind background image to scene dimensions once loaded
        Platform.runLater(() -> {
            Scene scene = modalOverlay.getScene();
            if (scene != null) {
                bgImageView.fitWidthProperty().bind(scene.widthProperty());
                bgImageView.fitHeightProperty().bind(scene.heightProperty());

                // Close active modal on Escape key
                scene.setOnKeyPressed(keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.ESCAPE && modalOverlay.isVisible()) {
                        handleCloseModal(null);
                    }
                });
            }
        });
    }

    // =========================================================================
    //  MODAL VISIBILITY CONTROLS
    // =========================================================================

    @FXML
    public void handleOpenSignInModal(ActionEvent event) {
        hideAllModals();
        errorLabel.setVisible(false);
        signInCard.setVisible(true);
        signInCard.setManaged(true);
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        passwordField.requestFocus();
    }

    @FXML
    public void handleOpenSignUpModal(ActionEvent event) {
        hideAllModals();
        regErrorLabel.setVisible(false);
        signUpCard.setVisible(true);
        signUpCard.setManaged(true);
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        regNameField.requestFocus();
    }

    @FXML
    public void handleOpenAdminModal(ActionEvent event) {
        hideAllModals();
        adminErrorLabel.setVisible(false);
        adminCard.setVisible(true);
        adminCard.setManaged(true);
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        adminPasswordField.requestFocus();
    }

    @FXML
    public void handleCloseModal(ActionEvent event) {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        hideAllModals();
    }

    private void hideAllModals() {
        signInCard.setVisible(false);
        signInCard.setManaged(false);
        signUpCard.setVisible(false);
        signUpCard.setManaged(false);
        adminCard.setVisible(false);
        adminCard.setManaged(false);
    }

    // =========================================================================
    //  AUTHENTICATION ACTIONS
    // =========================================================================

    @FXML
    private void handleLogin(ActionEvent event) {
        errorLabel.setVisible(false);
        String email    = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        if (email.isEmpty()) {
            errorLabel.setText("Please enter your email address.");
            errorLabel.setVisible(true);
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            errorLabel.setText("Please enter your password.");
            errorLabel.setVisible(true);
            passwordField.requestFocus();
            return;
        }

        try {
            Customer customer = authService.login(email, password);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            if (customer.isAdmin()) {
                NavigationUtil.navigateTo(stage, "/fxml/admin/AdminDashboard.fxml");
            } else {
                NavigationUtil.navigateTo(stage, "/fxml/Home.fxml");
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e instanceof java.sql.SQLException || (msg != null && msg.contains("Communications link"))) {
                errorLabel.setText("Database error: Ensure MySQL is running and check db.properties.");
            } else {
                errorLabel.setText(msg != null ? msg : "Invalid email or password.");
            }
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        errorLabel.setVisible(false);
        Stage currentStage = (Stage) loginButton.getScene().getWindow();
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
    private void handleRegisterSubmit(ActionEvent event) {
        regErrorLabel.setVisible(false);
        String name    = regNameField.getText() != null ? regNameField.getText().trim() : "";
        String email   = regEmailField.getText() != null ? regEmailField.getText().trim() : "";
        String pass    = regPasswordField.getText();
        String confirm = regConfirmPasswordField.getText();

        if (name.isEmpty()) {
            regErrorLabel.setText("Please enter your full name.");
            regErrorLabel.setVisible(true);
            regNameField.requestFocus();
            return;
        }

        if (email.isEmpty() || !email.contains("@")) {
            regErrorLabel.setText("Please enter a valid email address.");
            regErrorLabel.setVisible(true);
            regEmailField.requestFocus();
            return;
        }

        if (pass == null || pass.length() < 6) {
            regErrorLabel.setText("Password must be at least 6 characters.");
            regErrorLabel.setVisible(true);
            regPasswordField.requestFocus();
            return;
        }

        if (!pass.equals(confirm)) {
            regErrorLabel.setText("Passwords do not match.");
            regErrorLabel.setVisible(true);
            regConfirmPasswordField.requestFocus();
            return;
        }

        try {
            Customer customer = authService.register(name, email, pass);
            AlertUtil.showInfo("Registration Successful",
                "Welcome to FoodHub, " + customer.getName() + "! Your account has been created.");
            Stage stage = (Stage) registerSubmitButton.getScene().getWindow();
            NavigationUtil.navigateTo(stage, "/fxml/Home.fxml");
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e instanceof java.sql.SQLException || (msg != null && msg.contains("Communications link"))) {
                regErrorLabel.setText("Database error: Ensure MySQL is running & check db.properties.");
            } else {
                regErrorLabel.setText(msg != null ? msg : "An unexpected error occurred.");
            }
            regErrorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleGoogleSignup(ActionEvent event) {
        regErrorLabel.setVisible(false);
        Stage currentStage = (Stage) registerSubmitButton.getScene().getWindow();
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
    private void handleAdminLogin(ActionEvent event) {
        adminErrorLabel.setVisible(false);
        String email    = adminEmailField.getText() != null ? adminEmailField.getText().trim() : "";
        String password = adminPasswordField.getText() != null ? adminPasswordField.getText() : "";

        if (email.isEmpty() || password.isEmpty()) {
            adminErrorLabel.setText("Please enter admin credentials.");
            adminErrorLabel.setVisible(true);
            return;
        }

        try {
            Customer customer = authService.login(email, password);
            if (!customer.isAdmin()) {
                adminErrorLabel.setText("Access Denied: This account does not have Admin privileges.");
                adminErrorLabel.setVisible(true);
                return;
            }
            Stage stage = (Stage) adminLoginButton.getScene().getWindow();
            NavigationUtil.navigateTo(stage, "/fxml/admin/AdminDashboard.fxml");
        } catch (Exception e) {
            adminErrorLabel.setText(e.getMessage() != null ? e.getMessage() : "Invalid admin credentials.");
            adminErrorLabel.setVisible(true);
        }
    }
}
