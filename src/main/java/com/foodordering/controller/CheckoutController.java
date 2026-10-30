package com.foodordering.controller;

import com.foodordering.model.Discount;
import com.foodordering.model.Order;
import com.foodordering.model.Payment;
import com.foodordering.service.CartService;
import com.foodordering.service.DiscountService;
import com.foodordering.service.OrderService;
import com.foodordering.util.AlertUtil;
import com.foodordering.util.NavigationUtil;
import com.foodordering.util.OrderConfirmationDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Controller for the modern Checkout & Payment screen.
 *
 * <p>Supports:
 * <ul>
 *   <li>Delivery address & contact phone inputs.</li>
 *   <li>Single-click coupon pills & custom coupon input.</li>
 *   <li>Multi-channel payment strategies: UPI, Credit/Debit Card, Net Banking, and COD.</li>
 *   <li>Simulated bank authorization & transaction receipt generation.</li>
 *   <li>ACID multi-table order placement.</li>
 * </ul>
 */
public class CheckoutController {

    // Delivery fields
    @FXML private TextArea        deliveryAddressField;
    @FXML private TextField       phoneField;
    @FXML private TextField       instructionsField;

    // Coupon fields
    @FXML private TextField       couponField;
    @FXML private Button          applyCouponBtn;
    @FXML private Label           couponStatusLabel;
    @FXML private VBox            availableOffersBox;
    @FXML private Label           availableOffersLabel;
    @FXML private FlowPane        couponChipsPane;

    // Payment Selection
    @FXML private ToggleGroup     paymentToggle;
    @FXML private RadioButton     upiRadio;
    @FXML private RadioButton     cardRadio;
    @FXML private RadioButton     netBankingRadio;
    @FXML private RadioButton     codRadio;

    // Dynamic payment sub-panels
    @FXML private VBox            upiDetailsBox;
    @FXML private TextField       upiIdField;

    @FXML private VBox            cardDetailsBox;
    @FXML private TextField       cardNumberField;
    @FXML private TextField       cardExpiryField;
    @FXML private TextField       cardCvvField;
    @FXML private TextField       cardHolderField;

    @FXML private VBox            netBankingDetailsBox;
    @FXML private ComboBox<String> bankSelectCombo;

    @FXML private VBox            codDetailsBox;

    // Summary Labels
    @FXML private Label           subtotalLabel;
    @FXML private Label           discountLabel;
    @FXML private Label           deliveryFeeLabel;
    @FXML private Label           taxLabel;
    @FXML private Label           finalAmountLabel;

    @FXML private Button          placeOrderBtn;
    @FXML private Button          backToCartBtn;
    @FXML private ProgressIndicator payProgressIndicator;

    private final CartService     cartService     = HomeController.getSharedCartService();
    private final DiscountService discountService = new DiscountService();
    private final OrderService    orderService    = new OrderService();

    private BigDecimal subtotal       = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount      = BigDecimal.ZERO;
    private BigDecimal finalAmount    = BigDecimal.ZERO;
    private Discount   appliedDiscount = null;

    @FXML
    private void initialize() {
        subtotal = cartService.getCart().getSubtotal();
        discountAmount = BigDecimal.ZERO;

        bankSelectCombo.setItems(FXCollections.observableArrayList(
            "HDFC Bank", "State Bank of India (SBI)", "ICICI Bank", "Axis Bank", "Kotak Mahindra Bank", "Punjab National Bank"
        ));
        bankSelectCombo.setValue("HDFC Bank");

        if (paymentToggle == null) {
            paymentToggle = new ToggleGroup();
            upiRadio.setToggleGroup(paymentToggle);
            cardRadio.setToggleGroup(paymentToggle);
            netBankingRadio.setToggleGroup(paymentToggle);
            codRadio.setToggleGroup(paymentToggle);
        }

        // Payment toggle listeners to show corresponding interactive input form
        paymentToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            updatePaymentForms();
        });
        updatePaymentForms();

        // Preset user phone if possible
        phoneField.setText("9876543210");
        deliveryAddressField.setText("Flat 402, Greenview Residency, Park Avenue");

        updateSummaryLabels();
        loadAvailableCoupons();
        couponStatusLabel.setVisible(false);
        if (payProgressIndicator != null) payProgressIndicator.setVisible(false);
    }

    private void loadAvailableCoupons() {
        if (couponChipsPane == null) return;
        couponChipsPane.getChildren().clear();

        List<Discount> activeCoupons = discountService.getActiveCoupons();
        if (activeCoupons == null || activeCoupons.isEmpty()) {
            if (availableOffersBox != null) {
                availableOffersBox.setVisible(false);
                availableOffersBox.setManaged(false);
            }
            return;
        }

        if (availableOffersBox != null) {
            availableOffersBox.setVisible(true);
            availableOffersBox.setManaged(true);
        }

        for (Discount d : activeCoupons) {
            Button chip = new Button(d.getDisplayLabel());
            chip.getStyleClass().add("coupon-chip");

            StringBuilder tip = new StringBuilder("Tap to apply '").append(d.getCouponCode()).append("'");
            if (d.getMinimumOrderAmount() != null && d.getMinimumOrderAmount().compareTo(BigDecimal.ZERO) > 0) {
                tip.append(" (Min order ₹").append(d.getMinimumOrderAmount().stripTrailingZeros().toPlainString()).append(")");
            }
            chip.setTooltip(new Tooltip(tip.toString()));

            chip.setOnAction(e -> applyCouponCode(d.getCouponCode()));
            couponChipsPane.getChildren().add(chip);
        }
    }

    private void updatePaymentForms() {
        upiDetailsBox.setVisible(upiRadio.isSelected());
        upiDetailsBox.setManaged(upiRadio.isSelected());

        cardDetailsBox.setVisible(cardRadio.isSelected());
        cardDetailsBox.setManaged(cardRadio.isSelected());

        netBankingDetailsBox.setVisible(netBankingRadio.isSelected());
        netBankingDetailsBox.setManaged(netBankingRadio.isSelected());

        codDetailsBox.setVisible(codRadio.isSelected());
        codDetailsBox.setManaged(codRadio.isSelected());
    }

    @FXML
    private void handleApplyCoupon(ActionEvent event) {
        applyCouponCode(couponField.getText().trim());
    }

    private void applyCouponCode(String code) {
        couponField.setText(code);
        couponStatusLabel.setVisible(false);
        try {
            Discount d = discountService.validateCoupon(code, subtotal);
            discountAmount  = discountService.calculateDiscountAmount(d, subtotal);
            appliedDiscount = d;
            couponStatusLabel.setText("✓ Coupon '" + code + "' applied! You save ₹" + discountAmount.toPlainString());
            couponStatusLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
            couponStatusLabel.setVisible(true);
            updateSummaryLabels();
        } catch (Exception ex) {
            discountAmount  = BigDecimal.ZERO;
            appliedDiscount = null;
            couponStatusLabel.setText("✗ " + ex.getMessage());
            couponStatusLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
            couponStatusLabel.setVisible(true);
            updateSummaryLabels();
        }
    }

    private void updateSummaryLabels() {
        BigDecimal afterDiscount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);
        // 5% GST tax
        taxAmount = afterDiscount.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        finalAmount = afterDiscount.add(taxAmount);

        subtotalLabel.setText("₹ " + subtotal.toPlainString());
        discountLabel.setText("- ₹ " + discountAmount.toPlainString());
        deliveryFeeLabel.setText("FREE");
        taxLabel.setText("₹ " + taxAmount.toPlainString());
        finalAmountLabel.setText("₹ " + finalAmount.toPlainString());
    }

    @FXML
    private void handlePlaceOrder(ActionEvent event) {
        String address = deliveryAddressField.getText().trim();
        String phone   = phoneField.getText().trim();

        if (address.isBlank()) {
            AlertUtil.showWarning("Missing Address", "Please enter your delivery address.");
            deliveryAddressField.requestFocus();
            return;
        }
        if (phone.isBlank()) {
            AlertUtil.showWarning("Missing Phone", "Please enter your contact phone number.");
            phoneField.requestFocus();
            return;
        }

        // Determine payment method and validate inputs
        Payment.Method method;
        String transactionRef;

        if (upiRadio.isSelected()) {
            method = Payment.Method.UPI;
            String upiId = upiIdField.getText().trim();
            if (upiId.isEmpty()) {
                AlertUtil.showWarning("UPI Required", "Please enter your UPI ID (e.g. yourname@oksbi).");
                upiIdField.requestFocus();
                return;
            }
            transactionRef = "UPI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else if (cardRadio.isSelected()) {
            method = Payment.Method.CREDIT_DEBIT_CARD;
            String cardNum = cardNumberField.getText().trim();
            if (cardNum.length() < 12) {
                AlertUtil.showWarning("Card Number", "Please enter a valid card number.");
                cardNumberField.requestFocus();
                return;
            }
            transactionRef = "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else if (netBankingRadio.isSelected()) {
            method = Payment.Method.NET_BANKING;
            transactionRef = "NB-" + bankSelectCombo.getValue().replaceAll("\\s+", "").toUpperCase() + "-" + System.currentTimeMillis() % 100000;
        } else {
            method = Payment.Method.CASH_ON_DELIVERY;
            transactionRef = "COD-VERIFIED";
        }

        // Disable button during payment simulation
        placeOrderBtn.setDisable(true);
        if (payProgressIndicator != null) payProgressIndicator.setVisible(true);

        Task<Order> paymentTask = new Task<>() {
            @Override
            protected Order call() throws Exception {
                // Simulate payment gateway processing delay
                if (method != Payment.Method.CASH_ON_DELIVERY) {
                    Thread.sleep(1200);
                }
                return orderService.placeOrder(
                    cartService.getCart(),
                    discountAmount,
                    method,
                    address,
                    phone,
                    transactionRef,
                    cartService
                );
            }
        };

        paymentTask.setOnSucceeded(e -> {
            placeOrderBtn.setDisable(false);
            if (payProgressIndicator != null) payProgressIndicator.setVisible(false);

            Order order = paymentTask.getValue();
            Stage stage = (placeOrderBtn != null && placeOrderBtn.getScene() != null)
                ? (Stage) placeOrderBtn.getScene().getWindow() : null;

            OrderConfirmationDialog.Action action = OrderConfirmationDialog.show(
                stage, order, method, transactionRef, address
            );

            try {
                if (action == OrderConfirmationDialog.Action.BACK_TO_MENU) {
                    NavigationUtil.navigateTo(stage, "/fxml/Home.fxml");
                } else {
                    NavigationUtil.navigateTo(stage, "/fxml/OrderHistory.fxml");
                }
            } catch (Exception ex) {
                AlertUtil.showError("Navigation Error", ex.getMessage());
            }
        });

        paymentTask.setOnFailed(e -> {
            placeOrderBtn.setDisable(false);
            if (payProgressIndicator != null) payProgressIndicator.setVisible(false);
            Throwable ex = paymentTask.getException();
            AlertUtil.showError("Payment / Order Failed", ex != null ? ex.getMessage() : "Unknown error");
        });

        new Thread(paymentTask).start();
    }

    @FXML
    private void handleBackToCart(ActionEvent event) {
        try {
            Stage stage = (backToCartBtn != null && backToCartBtn.getScene() != null)
                ? (Stage) backToCartBtn.getScene().getWindow() : null;
            NavigationUtil.navigateTo(stage, "/fxml/Home.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }
}
