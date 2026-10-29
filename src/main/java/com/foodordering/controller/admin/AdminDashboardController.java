package com.foodordering.controller.admin;

import com.foodordering.service.AdminService;
import com.foodordering.util.AlertUtil;
import com.foodordering.util.NavigationUtil;
import com.foodordering.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller for the Admin Dashboard.
 *
 * <p>Displays total orders, today's orders, and revenue summary.
 * Role-based: only accessible after admin login (enforced in AuthService + AdminService).
 */
public class AdminDashboardController {

    @FXML private Label  welcomeLabel;
    @FXML private Label  totalOrdersLabel;
    @FXML private Label  todayOrdersLabel;
    @FXML private Label  revenueLabel;
    @FXML private Label  foodCountLabel;
    @FXML private Label  couponsCountLabel;

    @FXML private Button manageFoodBtn;
    @FXML private Button manageOrdersBtn;
    @FXML private Button manageCouponsBtn;
    @FXML private Button logoutBtn;

    private final AdminService adminService = new AdminService();

    @FXML
    private void initialize() {
        String name = SessionManager.getInstance().getCurrentUser() != null
            ? SessionManager.getInstance().getCurrentUser().getName() : "Administrator";
        welcomeLabel.setText("Welcome back, " + name + " 👋");

        loadStats();
    }

    private void loadStats() {
        try {
            if (totalOrdersLabel != null) totalOrdersLabel.setText(String.valueOf(adminService.getTotalOrders()));
            if (todayOrdersLabel != null) todayOrdersLabel.setText(String.valueOf(adminService.getTodayOrders()));
            if (revenueLabel != null)     revenueLabel.setText("₹ " + adminService.getRevenue().toPlainString());
            if (foodCountLabel != null)   foodCountLabel.setText(String.valueOf(adminService.getTotalFoodCount()));
            if (couponsCountLabel != null)couponsCountLabel.setText(String.valueOf(adminService.getActiveCouponsCount()));
        } catch (Exception e) {
            AlertUtil.showError("Dashboard Error", "Could not load metrics: " + e.getMessage());
        }
    }

    @FXML private void handleManageFood(ActionEvent e)    { navigate("/fxml/admin/AdminFood.fxml"); }
    @FXML private void handleManageOrders(ActionEvent e)  { navigate("/fxml/admin/AdminOrders.fxml"); }
    @FXML private void handleManageCoupons(ActionEvent e) { navigate("/fxml/admin/AdminCoupons.fxml"); }

    @FXML
    private void handleGoToStorefront(ActionEvent event) {
        navigate("/fxml/Home.fxml");
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadStats();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        navigate("/fxml/Login.fxml");
    }

    private void navigate(String fxmlPath) {
        try {
            Stage stage = (Stage) manageFoodBtn.getScene().getWindow();
            NavigationUtil.navigateTo(stage, fxmlPath);
        } catch (Exception ex) {
            AlertUtil.showError("Navigation Error", ex.getMessage());
        }
    }
}
