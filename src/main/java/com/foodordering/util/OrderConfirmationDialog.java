package com.foodordering.util;

import com.foodordering.model.Order;
import com.foodordering.model.Payment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Modern, beautifully styled modal dialog presented upon successful order placement & payment.
 * Replaces generic JavaFX alert boxes with a branded FoodHub order confirmation experience.
 */
public class OrderConfirmationDialog {

    public enum Action {
        TRACK_ORDERS,
        BACK_TO_MENU,
        CLOSE
    }

    private static Action selectedAction = Action.TRACK_ORDERS;

    /**
     * Displays the order confirmation modal dialog.
     *
     * @param owner       the parent stage
     * @param order       the newly placed Order
     * @param method      the payment method selected
     * @param txnRef      the transaction reference identifier
     * @param address     the customer's delivery address
     * @return the Action chosen by the user (TRACK_ORDERS, BACK_TO_MENU, or CLOSE)
     */
    public static Action show(Stage owner, Order order, Payment.Method method, String txnRef, String address) {
        selectedAction = Action.TRACK_ORDERS;

        Stage dialog = new Stage();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.setTitle("Order Confirmed • FoodHub");
        dialog.setResizable(false);

        // Root Container
        VBox root = new VBox(18);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(26, 28, 22, 28));
        root.setPrefWidth(470);
        root.setStyle("-fx-background-color: #FFFFFF; -fx-font-family: 'DM Sans', 'Segoe UI', system-ui, sans-serif;");

        // 1. Success Hero Section (Glowing Emerald Circle Badge + Header)
        StackPane checkCircle = new StackPane();
        checkCircle.setPrefSize(64, 64);
        checkCircle.setMaxSize(64, 64);
        checkCircle.setStyle(
            "-fx-background-color: #ECFDF5; " +
            "-fx-background-radius: 32px; " +
            "-fx-border-color: #10B981; " +
            "-fx-border-width: 2.5px; " +
            "-fx-border-radius: 32px; " +
            "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.35), 14, 0, 0, 3);"
        );

        Label checkMark = new Label("✓");
        checkMark.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #059669;");
        checkCircle.getChildren().add(checkMark);

        VBox heroText = new VBox(4);
        heroText.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Order Placed Successfully!");
        titleLabel.setStyle("-fx-font-size: 21px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");

        Label subtitleLabel = new Label("Payment Approved • Your food is being prepared fresh");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        heroText.getChildren().addAll(titleLabel, subtitleLabel);

        // 2. Structured Order Summary Card
        VBox orderCard = new VBox(12);
        orderCard.setPadding(new Insets(16, 18, 16, 18));
        orderCard.setStyle(
            "-fx-background-color: #F8FAFC; " +
            "-fx-border-color: #E2E8F0; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px;"
        );

        // Card Header: Order ID + Status Pill
        HBox cardHeader = new HBox();
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        Label orderIdLabel = new Label("Order #" + (order != null ? order.getOrderId() : "N/A"));
        orderIdLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusPill = new Label("● CONFIRMED");
        statusPill.setStyle(
            "-fx-background-color: #ECFDF5; " +
            "-fx-text-fill: #059669; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: 800; " +
            "-fx-padding: 4 10; " +
            "-fx-background-radius: 20px; " +
            "-fx-border-color: #A7F3D0; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 20px;"
        );
        cardHeader.getChildren().addAll(orderIdLabel, spacer, statusPill);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-padding: 0; -fx-opacity: 0.6;");

        // Amount & Payment Row (2 Columns)
        HBox infoRow = new HBox(20);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        // Left Col: Amount Paid
        VBox amountBox = new VBox(2);
        Label amountCaption = new Label("AMOUNT PAID");
        amountCaption.setStyle("-fx-font-size: 10.5px; -fx-font-weight: 700; -fx-text-fill: #64748B;");
        BigDecimal finalAmt = (order != null && order.getFinalAmount() != null)
                ? order.getFinalAmount() : BigDecimal.ZERO;
        Label amountValue = new Label(String.format("₹ %.2f", finalAmt.doubleValue()));
        amountValue.setStyle("-fx-font-size: 19px; -fx-font-weight: 800; -fx-text-fill: #FF5E36;");
        amountBox.getChildren().addAll(amountCaption, amountValue);

        Region infoSpacer = new Region();
        HBox.setHgrow(infoSpacer, Priority.ALWAYS);

        // Right Col: Payment Method & Ref
        VBox payBox = new VBox(2);
        payBox.setAlignment(Pos.CENTER_RIGHT);
        Label payCaption = new Label("PAYMENT METHOD");
        payCaption.setStyle("-fx-font-size: 10.5px; -fx-font-weight: 700; -fx-text-fill: #64748B;");
        String methodDisplayName = formatMethodName(method);
        Label payValue = new Label(methodDisplayName);
        payValue.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");
        payBox.getChildren().addAll(payCaption, payValue);

        infoRow.getChildren().addAll(amountBox, infoSpacer, payBox);

        // Transaction Ref Pill
        if (txnRef != null && !txnRef.isBlank()) {
            HBox txnRow = new HBox(6);
            txnRow.setAlignment(Pos.CENTER_LEFT);
            Label txnCaption = new Label("Txn Ref:");
            txnCaption.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #64748B;");
            Label txnBadge = new Label(txnRef);
            txnBadge.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: #EDE9FE; " +
                "-fx-text-fill: #6D28D9; " +
                "-fx-padding: 2 7; " +
                "-fx-background-radius: 4px;"
            );
            txnRow.getChildren().addAll(txnCaption, txnBadge);
            orderCard.getChildren().addAll(cardHeader, sep1, infoRow, txnRow);
        } else {
            orderCard.getChildren().addAll(cardHeader, sep1, infoRow);
        }

        // Delivery Address Block
        if (address != null && !address.isBlank()) {
            Separator sep2 = new Separator();
            sep2.setStyle("-fx-padding: 0; -fx-opacity: 0.6;");

            VBox addrBox = new VBox(3);
            Label addrHeader = new Label("📍 Delivery Address");
            addrHeader.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 700; -fx-text-fill: #334155;");
            Label addrText = new Label(address);
            addrText.setWrapText(true);
            addrText.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #475569;");
            addrBox.getChildren().addAll(addrHeader, addrText);

            orderCard.getChildren().addAll(sep2, addrBox);
        }

        // 3. Estimated Delivery Time Pill Banner (Safe glyphs, avoids font tofu boxes)
        HBox etaBanner = new HBox(12);
        etaBanner.setAlignment(Pos.CENTER_LEFT);
        etaBanner.setPadding(new Insets(11, 14, 11, 14));
        etaBanner.setStyle(
            "-fx-background-color: #FFF7ED; " +
            "-fx-border-color: #FED7AA; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 10px; " +
            "-fx-background-radius: 10px;"
        );

        Label etaIcon = new Label("⚡");
        etaIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #EA580C;");

        VBox etaText = new VBox(2);
        Label etaTitle = new Label("Estimated Delivery: 25 - 30 minutes");
        etaTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #C2410C;");
        Label etaDesc = new Label("Freshly prepared and dispatched directly to your doorstep!");
        etaDesc.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #9A3412;");
        etaText.getChildren().addAll(etaTitle, etaDesc);

        etaBanner.getChildren().addAll(etaIcon, etaText);

        // 4. Action Buttons
        VBox buttonBox = new VBox(8);
        buttonBox.setAlignment(Pos.CENTER);

        Button trackBtn = new Button("Track Order History →");
        trackBtn.setMaxWidth(Double.MAX_VALUE);
        trackBtn.setPrefHeight(42);
        trackBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #FF5E36, #FF844B); " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 800; " +
            "-fx-background-radius: 10px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(255, 94, 54, 0.35), 8, 0, 0, 2);"
        );
        trackBtn.setOnAction(e -> {
            selectedAction = Action.TRACK_ORDERS;
            dialog.close();
        });

        Button menuBtn = new Button("Back to Main Menu");
        menuBtn.setMaxWidth(Double.MAX_VALUE);
        menuBtn.setPrefHeight(38);
        menuBtn.setStyle(
            "-fx-background-color: #F1F5F9; " +
            "-fx-border-color: #CBD5E1; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-text-fill: #334155; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        );
        menuBtn.setOnAction(e -> {
            selectedAction = Action.BACK_TO_MENU;
            dialog.close();
        });

        buttonBox.getChildren().addAll(trackBtn, menuBtn);

        root.getChildren().addAll(checkCircle, heroText, orderCard, etaBanner, buttonBox);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(
                Objects.requireNonNull(OrderConfirmationDialog.class.getResource("/css/style.css")).toExternalForm()
            );
        } catch (Exception ignored) {}

        // Keyboard shortcuts: Enter -> Track, Esc -> Close
        scene.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.ENTER) {
                selectedAction = Action.TRACK_ORDERS;
                dialog.close();
            } else if (ke.getCode() == KeyCode.ESCAPE) {
                selectedAction = Action.CLOSE;
                dialog.close();
            }
        });

        dialog.setScene(scene);
        dialog.showAndWait();

        return selectedAction;
    }

    private static String formatMethodName(Payment.Method method) {
        if (method == null) return "Prepaid";
        return switch (method) {
            case UPI                 -> "⚡ UPI Payment";
            case CASH_ON_DELIVERY    -> "💵 Cash on Delivery";
            case CREDIT_DEBIT_CARD   -> "💳 Credit / Debit Card";
            case NET_BANKING         -> "🏦 Net Banking";
            case DEMO_ONLINE         -> "💳 Online Payment";
        };
    }
}
