package com.foodordering.controller;

import com.foodordering.model.CartItem;
import com.foodordering.service.CartService;
import com.foodordering.util.AlertUtil;
import com.foodordering.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for the Shopping Cart screen.
 *
 * <p>Demonstrates ArrayList + HashMap data structures via CartService/Cart.
 */
public class CartController {

    @FXML private VBox   cartItemsContainer;
    @FXML private Label  subtotalLabel;
    @FXML private Label  emptyCartLabel;
    @FXML private Button checkoutButton;
    @FXML private Button menuButton;
    @FXML private Button homeButton;

    private final CartService cartService = HomeController.getSharedCartService();

    @FXML
    private void initialize() {
        renderCart();
    }

    /** Builds the cart item rows. */
    private void renderCart() {
        cartItemsContainer.getChildren().clear();
        List<CartItem> items = cartService.getCart().getItems();

        if (items.isEmpty()) {
            emptyCartLabel.setVisible(true);
            subtotalLabel.setText("Subtotal: ₹ 0.00");
            checkoutButton.setDisable(true);
            return;
        }

        emptyCartLabel.setVisible(false);
        checkoutButton.setDisable(false);

        for (CartItem ci : items) {
            cartItemsContainer.getChildren().add(createCartRow(ci));
        }

        BigDecimal subtotal = cartService.getCart().getSubtotal();
        subtotalLabel.setText("Subtotal: ₹ " + subtotal.toPlainString());
    }

    /** Creates a horizontal row for one cart item. */
    private HBox createCartRow(CartItem ci) {
        HBox row = new HBox(12);
        row.getStyleClass().add("cart-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));

        // Item name
        Label nameLabel = new Label(ci.getFoodItem().getName());
        nameLabel.getStyleClass().add("cart-item-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Unit price
        Label unitPrice = new Label("₹ " + ci.getFoodItem().getPrice().toPlainString());
        unitPrice.getStyleClass().add("cart-unit-price");

        // Quantity controls
        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().add("qty-btn");
        minusBtn.setMinWidth(28);
        minusBtn.setPrefWidth(28);
        minusBtn.setAlignment(Pos.CENTER);
        Label qtyLabel = new Label(String.valueOf(ci.getQuantity()));
        qtyLabel.getStyleClass().add("qty-label");
        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("qty-btn");
        plusBtn.setMinWidth(28);
        plusBtn.setPrefWidth(28);
        plusBtn.setAlignment(Pos.CENTER);

        minusBtn.setOnAction(e -> {
            cartService.decrementQuantity(ci.getFoodItem().getFoodId());
            renderCart();
        });
        plusBtn.setOnAction(e -> {
            cartService.incrementQuantity(ci.getFoodItem().getFoodId());
            renderCart();
        });

        HBox qtyBox = new HBox(6, minusBtn, qtyLabel, plusBtn);
        qtyBox.setAlignment(Pos.CENTER);

        // Line total
        Label lineTotalLabel = new Label("₹ " + ci.getLineTotal().toPlainString());
        lineTotalLabel.getStyleClass().add("cart-line-total");

        // Remove button
        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("remove-btn");
        removeBtn.setOnAction(e -> {
            cartService.removeFromCart(ci.getFoodItem().getFoodId());
            renderCart();
        });

        row.getChildren().addAll(nameLabel, unitPrice, qtyBox, lineTotalLabel, removeBtn);
        return row;
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartService.isCartEmpty()) {
            AlertUtil.showWarning("Empty Cart", "Please add items before checking out.");
            return;
        }
        try {
            Stage stage = (checkoutButton != null && checkoutButton.getScene() != null)
                ? (Stage) checkoutButton.getScene().getWindow() : null;
            NavigationUtil.navigateTo(stage, "/fxml/Checkout.fxml");
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
}
