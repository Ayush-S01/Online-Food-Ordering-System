package com.foodordering.controller.admin;

import com.foodordering.model.Order;
import com.foodordering.model.OrderItem;
import com.foodordering.service.AdminService;
import com.foodordering.service.OrderService;
import com.foodordering.util.AlertUtil;
import com.foodordering.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for Admin Order Management screen.
 *
 * <p>Admins can view all orders and update their status using the state machine.
 */
public class AdminOrderController {

    @FXML private TableView<Order>                   ordersTable;
    @FXML private TableColumn<Order, Integer>        colOrderId;
    @FXML private TableColumn<Order, Integer>        colCustomerId;
    @FXML private TableColumn<Order, LocalDateTime>  colDate;
    @FXML private TableColumn<Order, BigDecimal>     colTotal;
    @FXML private TableColumn<Order, Order.Status>   colStatus;

    @FXML private ComboBox<Order.Status> statusCombo;
    @FXML private Button updateStatusBtn;
    @FXML private Button viewDetailsBtn;
    @FXML private Button backBtn;

    @FXML private Label    selectedOrderIdLabel;
    @FXML private Label    selectedOrderStatusBadge;
    @FXML private Label    customerInfoLabel;
    @FXML private Label    deliveryAddressLabel;
    @FXML private Label    totalAmountLabel;
    @FXML private TextArea orderItemsArea;

    private final AdminService  adminService  = new AdminService();
    private final OrderService  orderService  = new OrderService();
    private final ObservableList<Order> orderList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));

        statusCombo.setItems(FXCollections.observableArrayList(Order.Status.values()));
        ordersTable.setItems(orderList);

        ordersTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldVal, selected) -> displaySelectedOrder(selected));

        loadOrders();
    }

    private void loadOrders() {
        orderList.clear();
        try {
            orderList.addAll(adminService.getAllOrders());
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load orders: " + e.getMessage());
        }
    }

    private void displaySelectedOrder(Order order) {
        if (order == null) {
            if (selectedOrderIdLabel != null) selectedOrderIdLabel.setText("No order selected");
            if (selectedOrderStatusBadge != null) {
                selectedOrderStatusBadge.setText("—");
                selectedOrderStatusBadge.getStyleClass().setAll("order-status-confirmed");
            }
            if (customerInfoLabel != null) customerInfoLabel.setText("Select an order from the table to inspect details");
            if (deliveryAddressLabel != null) deliveryAddressLabel.setText("");
            if (totalAmountLabel != null) totalAmountLabel.setText("");
            if (orderItemsArea != null) orderItemsArea.clear();
            return;
        }

        if (statusCombo != null) {
            statusCombo.setValue(order.getOrderStatus());
        }

        if (selectedOrderIdLabel != null) {
            selectedOrderIdLabel.setText("Order #" + order.getOrderId());
        }

        if (selectedOrderStatusBadge != null) {
            selectedOrderStatusBadge.setText(order.getOrderStatus().name().replace('_', ' '));
            String styleClass = switch (order.getOrderStatus()) {
                case PLACED -> "order-status-confirmed";
                case CONFIRMED -> "order-status-confirmed";
                case PREPARING -> "order-status-preparing";
                case OUT_FOR_DELIVERY -> "order-status-out_for_delivery";
                case DELIVERED -> "order-status-delivered";
                case CANCELLED -> "order-status-cancelled";
            };
            selectedOrderStatusBadge.getStyleClass().setAll(styleClass);
        }

        if (customerInfoLabel != null) {
            customerInfoLabel.setText("Customer ID: #" + order.getCustomerId()
                + (order.getContactPhone() != null && !order.getContactPhone().isBlank()
                    ? "  •  📞 " + order.getContactPhone() : ""));
        }

        if (deliveryAddressLabel != null) {
            deliveryAddressLabel.setText("📍 " + (order.getDeliveryAddress() != null && !order.getDeliveryAddress().isBlank()
                ? order.getDeliveryAddress() : "Pickup / Dine-In Order"));
        }

        if (totalAmountLabel != null) {
            totalAmountLabel.setText("Total: ₹ " + order.getFinalAmount().toPlainString());
        }

        if (orderItemsArea != null) {
            try {
                List<OrderItem> items = orderService.getOrderItems(order.getOrderId());
                StringBuilder sb = new StringBuilder();
                for (OrderItem oi : items) {
                    sb.append(String.format("• %-22s × %d  = ₹ %s\n",
                        oi.getFoodNameSnapshot(),
                        oi.getQuantity(),
                        oi.getLineTotal().toPlainString()));
                }
                if (items.isEmpty()) {
                    sb.append("(No item records found)");
                }
                orderItemsArea.setText(sb.toString());
            } catch (Exception ex) {
                orderItemsArea.setText("Could not load items: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        int selectedId = selected != null ? selected.getOrderId() : -1;
        loadOrders();
        if (selectedId != -1) {
            for (Order o : orderList) {
                if (o.getOrderId() == selectedId) {
                    ordersTable.getSelectionModel().select(o);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleUpdateStatus(ActionEvent event) {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        Order.Status newStatus = statusCombo.getValue();
        if (selected == null || newStatus == null) {
            AlertUtil.showWarning("Selection Required",
                "Please select an order from the table and choose a status.");
            return;
        }
        applyStatusChange(selected.getOrderId(), newStatus);
    }

    @FXML
    private void handleQuickPreparing(ActionEvent event) {
        quickApplyStatus(Order.Status.PREPARING);
    }

    @FXML
    private void handleQuickOutForDelivery(ActionEvent event) {
        quickApplyStatus(Order.Status.OUT_FOR_DELIVERY);
    }

    @FXML
    private void handleQuickDelivered(ActionEvent event) {
        quickApplyStatus(Order.Status.DELIVERED);
    }

    @FXML
    private void handleQuickCancelled(ActionEvent event) {
        quickApplyStatus(Order.Status.CANCELLED);
    }

    private void quickApplyStatus(Order.Status newStatus) {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select an order from the table first.");
            return;
        }
        applyStatusChange(selected.getOrderId(), newStatus);
    }

    private void applyStatusChange(int orderId, Order.Status newStatus) {
        try {
            adminService.updateOrderStatus(orderId, newStatus);
            AlertUtil.showInfo("Status Updated", "Order #" + orderId
                + " status updated to " + newStatus.name().replace('_', ' ') + ".");
            loadOrders();
            for (Order o : orderList) {
                if (o.getOrderId() == orderId) {
                    ordersTable.getSelectionModel().select(o);
                    displaySelectedOrder(o);
                    break;
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleViewDetails(ActionEvent event) {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select an order.");
            return;
        }
        try {
            List<OrderItem> items = orderService.getOrderItems(selected.getOrderId());
            StringBuilder sb = new StringBuilder("Order #" + selected.getOrderId() + "\n\n");
            for (OrderItem oi : items) {
                sb.append("  ").append(oi.getFoodNameSnapshot())
                  .append(" × ").append(oi.getQuantity())
                  .append(" = ₹ ").append(oi.getLineTotal().toPlainString()).append("\n");
            }
            sb.append("\nFinal: ₹ ").append(selected.getFinalAmount().toPlainString());
            AlertUtil.showInfo("Order Details", sb.toString());
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            NavigationUtil.navigateTo(stage, "/fxml/admin/AdminDashboard.fxml");
        } catch (Exception e) {
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }
}
