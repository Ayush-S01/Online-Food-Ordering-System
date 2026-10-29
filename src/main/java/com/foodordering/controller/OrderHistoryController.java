package com.foodordering.controller;

import com.foodordering.model.Order;
import com.foodordering.model.OrderItem;
import com.foodordering.service.OrderService;
import com.foodordering.util.AlertUtil;
import com.foodordering.util.NavigationUtil;
import com.foodordering.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

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

        Label dateLabel = new Label("• " + (order.getOrderDate() != null
            ? order.getOrderDate().toLocalDate().toString() : "N/A"));
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

        Label amtLabel = new Label("Total: ₹ " + order.getFinalAmount().toPlainString());
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
            List<OrderItem> items = orderService.getOrderItems(order.getOrderId());
            StringBuilder sb = new StringBuilder();
            sb.append("🧾 INVOICE / RECEIPT - ORDER #").append(order.getOrderId()).append("\n");
            sb.append("--------------------------------------------------\n");
            sb.append("Date: ").append(order.getOrderDate() != null ? order.getOrderDate() : "N/A").append("\n");
            sb.append("Status: ").append(order.getOrderStatus().name().replace('_', ' ')).append("\n");
            if (order.getDeliveryAddress() != null) {
                sb.append("Address: ").append(order.getDeliveryAddress()).append("\n");
            }
            if (order.getPaymentMethod() != null) {
                sb.append("Payment: ").append(order.getPaymentMethod().replace('_', ' ')).append("\n");
            }
            sb.append("--------------------------------------------------\n");
            sb.append("Ordered Items:\n\n");
            for (OrderItem oi : items) {
                sb.append(String.format("  %-25s x %-2d   ₹ %-8s\n",
                    oi.getFoodNameSnapshot(), oi.getQuantity(), oi.getLineTotal().toPlainString()));
            }
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("  Subtotal:                     ₹ %s\n", order.getSubtotal().toPlainString()));
            sb.append(String.format("  Discount:                   - ₹ %s\n", order.getDiscountAmount().toPlainString()));
            sb.append(String.format("  TOTAL PAID:                   ₹ %s\n", order.getFinalAmount().toPlainString()));
            sb.append("--------------------------------------------------\n");
            sb.append("Thank you for ordering with FoodHub!");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Invoice");
            alert.setHeaderText("Order #" + order.getOrderId() + " Details");
            TextArea ta = new TextArea(sb.toString());
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 13px;");
            ta.setPrefSize(420, 320);
            alert.getDialogPane().setContent(ta);
            alert.showAndWait();
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
