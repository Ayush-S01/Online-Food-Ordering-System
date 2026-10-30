package com.foodordering.controller;

import com.foodordering.model.Order;
import com.foodordering.model.OrderItem;
import com.foodordering.service.OrderService;
import com.foodordering.util.AlertUtil;
import com.foodordering.util.NavigationUtil;
import com.foodordering.util.OrderInvoiceDialog;
import com.foodordering.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the customer's Order History & Live Tracking screen.
 */
public class OrderHistoryController {

    @FXML private VBox   ordersContainer;
    @FXML private Label  emptyLabel;
    @FXML private Button homeButton;
    @FXML private Button menuButton;

    private final OrderService orderService = new OrderService();

    @FXML
    private void initialize() {
        loadOrders();
    }

    private void loadOrders() {
        ordersContainer.getChildren().clear();
        if (!SessionManager.getInstance().isLoggedIn()) {
            emptyLabel.setVisible(true);
            return;
        }
        try {
            List<Order> orders = orderService.getMyOrders();
            if (orders.isEmpty()) {
                emptyLabel.setVisible(true);
                return;
            }
            emptyLabel.setVisible(false);
            for (Order order : orders) {
                ordersContainer.getChildren().add(createOrderCard(order));
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", "Could not load orders: " + e.getMessage());
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.getStyleClass().add("order-card");
        card.setPadding(new Insets(16));

        // Header: Order ID + Date + Status Badge
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Order #" + order.getOrderId());
        idLabel.getStyleClass().add("order-id");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        Label dateLabel = new Label("• " + (order.getOrderDate() != null
            ? order.getOrderDate().format(dtf) : "N/A"));
        dateLabel.setStyle("-fx-text-fill: #777; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String statusStr = order.getOrderStatus().name().replace('_', ' ');
        Label statusBadge = new Label(getStatusEmoji(order.getOrderStatus()) + " " + statusStr);
        statusBadge.getStyleClass().add("order-status-" + order.getOrderStatus().name().toLowerCase());

        header.getChildren().addAll(idLabel, dateLabel, spacer, statusBadge);

        // Delivery address & payment method info
        VBox metaBox = new VBox(3);
        String address = (order.getDeliveryAddress() != null && !order.getDeliveryAddress().isBlank())
            ? order.getDeliveryAddress() : "Standard Address";
        Label addrLabel = new Label("📍 Deliver to: " + address);
        addrLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        String pay = (order.getPaymentMethod() != null) ? order.getPaymentMethod().replace('_', ' ') : "Prepaid";
        Label payLabel = new Label("💳 Paid via: " + pay);
        payLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        metaBox.getChildren().addAll(addrLabel, payLabel);

        // Footer: Total Amount + Details Button
        HBox footer = new HBox(14);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label amtLabel = new Label(String.format("Total: ₹ %.2f",
            order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0));
        amtLabel.getStyleClass().add("order-amount");

        Region spacerFooter = new Region();
        HBox.setHgrow(spacerFooter, Priority.ALWAYS);

        Button detailBtn = new Button("View Receipt & Items");
        detailBtn.getStyleClass().add("detail-btn");
        detailBtn.setOnAction(e -> showOrderDetails(order));

        footer.getChildren().addAll(amtLabel, spacerFooter, detailBtn);

        card.getChildren().addAll(header, metaBox, new Separator(), footer);
        return card;
    }

    private String getStatusEmoji(Order.Status status) {
        return switch (status) {
            case PLACED          -> "📝";
            case CONFIRMED       -> "👨‍🍳";
            case PREPARING       -> "🍳";
            case OUT_FOR_DELIVERY-> "🛵";
            case DELIVERED       -> "✅";
            case CANCELLED       -> "❌";
        };
    }

    private void showOrderDetails(Order order) {
        try {
            Stage stage = (ordersContainer != null && ordersContainer.getScene() != null)
                ? (Stage) ordersContainer.getScene().getWindow() : null;
            List<OrderItem> items = orderService.getOrderItems(order.getOrderId());
            OrderInvoiceDialog.show(stage, order, items);
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleHome(ActionEvent event) {
        try {
            Stage stage = (homeButton != null && homeButton.getScene() != null)
                ? (Stage) homeButton.getScene().getWindow() : null;
            NavigationUtil.navigateTo(stage, "/fxml/Home.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }

    @FXML
    private void handleMenu(ActionEvent event) {
        try {
            Stage stage = (menuButton != null && menuButton.getScene() != null)
                ? (Stage) menuButton.getScene().getWindow() : null;
            NavigationUtil.navigateTo(stage, "/fxml/Home.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }
}
