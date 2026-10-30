package com.foodordering.util;

import com.foodordering.model.Order;
import com.foodordering.model.OrderItem;
import com.foodordering.service.OrderService;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Modern, authentic restaurant digital tax invoice & receipt modal.
 * Replaces plain ASCII-formatted text areas with a clean, itemized FoodHub invoice card.
 */
public class OrderInvoiceDialog {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    /**
     * Shows the order invoice dialog, automatically querying items from the database.
     */
    public static void show(Stage owner, Order order) {
        show(owner, order, null);
    }

    /**
     * Shows the order invoice dialog for the given order and items list.
     *
     * @param owner  the parent stage
     * @param order  the order to display
     * @param items  the itemized list of dishes (if null, loaded via OrderService)
     */
    public static void show(Stage owner, Order order, List<OrderItem> items) {
        if (order == null) return;

        if (items == null || items.isEmpty()) {
            try {
                items = new OrderService().getOrderItems(order.getOrderId());
            } catch (Exception e) {
                items = Collections.emptyList();
            }
        }

        final List<OrderItem> finalItems = items;

        Stage dialog = new Stage();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.setTitle("Tax Invoice & Receipt • Order #" + order.getOrderId() + " - FoodHub");
        dialog.setResizable(false);

        // Root container with soft background
        VBox root = new VBox(14);
        root.setPadding(new Insets(18, 22, 18, 22));
        root.setPrefWidth(530);
        root.setMaxHeight(700);
        root.setStyle("-fx-background-color: #F8FAFC; -fx-font-family: 'DM Sans', 'Segoe UI', system-ui, sans-serif;");

        // White Receipt Card Paper
        VBox card = new VBox(12);
        card.setPadding(new Insets(20, 22, 18, 22));
        card.setStyle(
            "-fx-background-color: #FFFFFF; " +
            "-fx-background-radius: 14px; " +
            "-fx-border-color: #E2E8F0; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 14px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.08), 12, 0, 0, 3);"
        );

        // 1. Header Row (Brand Logo + Tax Invoice Tag & Formatted Date)
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox brandBox = new VBox(2);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        HBox logoBrandLine = new HBox(8);
        logoBrandLine.setAlignment(Pos.CENTER_LEFT);

        ImageView logoView = null;
        try (InputStream in = OrderInvoiceDialog.class.getResourceAsStream("/images/foodhub_logo.png")) {
            if (in != null) {
                logoView = new ImageView(new Image(in));
                logoView.setFitHeight(28);
                logoView.setPreserveRatio(true);
            }
        } catch (Exception ignored) {}

        if (logoView != null) {
            logoBrandLine.getChildren().add(logoView);
        } else {
            Label brandName = new Label("🍲 FoodHub");
            brandName.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #FF5E36;");
            logoBrandLine.getChildren().add(brandName);
        }

        Label tagline = new Label("Multi-Cuisine Dining & Express Delivery");
        tagline.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        brandBox.getChildren().addAll(logoBrandLine, tagline);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        VBox invoiceMetaBox = new VBox(3);
        invoiceMetaBox.setAlignment(Pos.CENTER_RIGHT);

        Label invoicePill = new Label("TAX INVOICE & RECEIPT");
        invoicePill.setStyle(
            "-fx-background-color: #F1F5F9; " +
            "-fx-text-fill: #334155; " +
            "-fx-font-size: 10px; " +
            "-fx-font-weight: 800; " +
            "-fx-padding: 3 8; " +
            "-fx-background-radius: 4px; " +
            "-fx-border-color: #E2E8F0; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 4px;"
        );

        String dateStr = (order.getOrderDate() != null)
                ? order.getOrderDate().format(DATE_FORMATTER) : "N/A";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748B;");
        invoiceMetaBox.getChildren().addAll(invoicePill, dateLabel);

        headerRow.getChildren().addAll(brandBox, headerSpacer, invoiceMetaBox);

        Separator sepHeader = new Separator();
        sepHeader.setStyle("-fx-padding: 2 0; -fx-opacity: 0.7;");

        // 2. Order Metadata Card
        VBox metaCard = new VBox(8);
        metaCard.setPadding(new Insets(10, 14, 10, 14));
        metaCard.setStyle(
            "-fx-background-color: #F8FAFC; " +
            "-fx-border-color: #E2E8F0; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;"
        );

        HBox metaRow1 = new HBox(12);
        metaRow1.setAlignment(Pos.CENTER_LEFT);

        Label orderIdLbl = new Label("Order #" + order.getOrderId());
        orderIdLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");

        Region metaSpacer = new Region();
        HBox.setHgrow(metaSpacer, Priority.ALWAYS);

        // Status pill with custom style
        Label statusPill = createStatusPill(order.getOrderStatus());

        metaRow1.getChildren().addAll(orderIdLbl, metaSpacer, statusPill);

        // Payment and Delivery Info
        VBox metaRow2 = new VBox(3);
        String paymentStr = (order.getPaymentMethod() != null)
                ? order.getPaymentMethod().replace('_', ' ') : "Prepaid";
        Label payLabel = new Label("💳 Paid via: " + paymentStr);
        payLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #475569;");

        if (order.getDeliveryAddress() != null && !order.getDeliveryAddress().isBlank()) {
            Label addrLabel = new Label("📍 Deliver To: " + order.getDeliveryAddress());
            addrLabel.setWrapText(true);
            addrLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
            metaRow2.getChildren().addAll(payLabel, addrLabel);
        } else {
            metaRow2.getChildren().add(payLabel);
        }

        metaCard.getChildren().addAll(metaRow1, metaRow2);

        // 3. Itemized Ordered Dishes Table
        VBox itemsSection = new VBox(6);

        Label sectionHeader = new Label("ORDERED DISHES");
        sectionHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: #64748B; -fx-letter-spacing: 0.5px;");

        // Table Header
        HBox tableHeader = new HBox(8);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        tableHeader.setPadding(new Insets(6, 12, 6, 12));
        tableHeader.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 6px;");

        Label thItem = new Label("ITEM");
        thItem.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #475569;");
        HBox.setHgrow(thItem, Priority.ALWAYS);

        Label thQty = new Label("QTY");
        thQty.setPrefWidth(50);
        thQty.setAlignment(Pos.CENTER);
        thQty.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #475569;");

        Label thPrice = new Label("PRICE");
        thPrice.setPrefWidth(75);
        thPrice.setAlignment(Pos.CENTER_RIGHT);
        thPrice.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #475569;");

        Label thTotal = new Label("TOTAL");
        thTotal.setPrefWidth(85);
        thTotal.setAlignment(Pos.CENTER_RIGHT);
        thTotal.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #475569;");

        tableHeader.getChildren().addAll(thItem, thQty, thPrice, thTotal);

        // Items Container inside ScrollPane
        VBox itemsBox = new VBox(0);
        for (int i = 0; i < finalItems.size(); i++) {
            OrderItem item = finalItems.get(i);
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            if (i < finalItems.size() - 1) {
                row.setStyle("-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");
            }

            Label nameLbl = new Label(item.getFoodNameSnapshot());
            nameLbl.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #1E293B;");
            HBox.setHgrow(nameLbl, Priority.ALWAYS);

            Label qtyLbl = new Label("× " + item.getQuantity());
            qtyLbl.setPrefWidth(50);
            qtyLbl.setAlignment(Pos.CENTER);
            qtyLbl.setStyle(
                "-fx-font-size: 11.5px; -fx-font-weight: 600; -fx-text-fill: #475569; " +
                "-fx-background-color: #F1F5F9; -fx-background-radius: 4px; -fx-padding: 2 4;"
            );

            BigDecimal uPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            Label priceLbl = new Label(String.format("₹ %.2f", uPrice.doubleValue()));
            priceLbl.setPrefWidth(75);
            priceLbl.setAlignment(Pos.CENTER_RIGHT);
            priceLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

            BigDecimal lTotal = item.getLineTotal() != null ? item.getLineTotal() : BigDecimal.ZERO;
            Label totalLbl = new Label(String.format("₹ %.2f", lTotal.doubleValue()));
            totalLbl.setPrefWidth(85);
            totalLbl.setAlignment(Pos.CENTER_RIGHT);
            totalLbl.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

            row.getChildren().addAll(nameLbl, qtyLbl, priceLbl, totalLbl);
            itemsBox.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(itemsBox);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(160);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        itemsSection.getChildren().addAll(sectionHeader, tableHeader, scroll);

        // 4. Financial Summary (Bill Breakdown)
        VBox billBox = new VBox(6);
        billBox.setPadding(new Insets(12, 14, 12, 14));
        billBox.setStyle(
            "-fx-background-color: #F8FAFC; " +
            "-fx-border-color: #E2E8F0; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 10px; " +
            "-fx-background-radius: 10px;"
        );

        BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;
        billBox.getChildren().add(createBillRow("Item Subtotal", String.format("₹ %.2f", subtotal.doubleValue()), false, "#334155"));

        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            billBox.getChildren().add(
                createBillRow("Promotional Coupon Savings", String.format("- ₹ %.2f", discount.doubleValue()), true, "#16A34A")
            );
        }

        billBox.getChildren().add(createBillRow("Delivery Partner Fee", "FREE", true, "#16A34A"));
        billBox.getChildren().add(createBillRow("Taxes & Restaurant GST (5%)", "Included", false, "#64748B"));

        Separator sepBill = new Separator();
        sepBill.setStyle("-fx-padding: 2 0; -fx-opacity: 0.7;");
        billBox.getChildren().add(sepBill);

        // Grand Total Paid
        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label totalCaption = new Label("TOTAL AMOUNT PAID");
        totalCaption.setStyle("-fx-font-size: 13.5px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");

        Region totalSpacer = new Region();
        HBox.setHgrow(totalSpacer, Priority.ALWAYS);

        BigDecimal finalAmt = order.getFinalAmount() != null ? order.getFinalAmount() : BigDecimal.ZERO;
        Label totalValue = new Label(String.format("₹ %.2f", finalAmt.doubleValue()));
        totalValue.setStyle("-fx-font-size: 19px; -fx-font-weight: 800; -fx-text-fill: #FF5E36;");

        totalRow.getChildren().addAll(totalCaption, totalSpacer, totalValue);
        billBox.getChildren().add(totalRow);

        // 5. Card Footer note
        Label footerNote = new Label("Thank you for ordering with FoodHub! Hope you enjoy your meal 🍽️");
        footerNote.setMaxWidth(Double.MAX_VALUE);
        footerNote.setAlignment(Pos.CENTER);
        footerNote.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748B; -fx-font-style: italic; -fx-padding: 4 0 0 0;");

        card.getChildren().addAll(headerRow, sepHeader, metaCard, itemsSection, billBox, footerNote);

        // 6. Action Buttons Bar
        HBox actionsBar = new HBox(12);
        actionsBar.setAlignment(Pos.CENTER_RIGHT);

        Button copyBtn = new Button("📋 Copy Summary");
        copyBtn.setPrefHeight(36);
        copyBtn.setStyle(
            "-fx-background-color: #FFFFFF; " +
            "-fx-border-color: #CBD5E1; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-text-fill: #334155; " +
            "-fx-font-size: 12.5px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        );
        copyBtn.setOnAction(e -> {
            copyInvoiceToClipboard(order, finalItems);
            copyBtn.setText("✓ Copied!");
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> copyBtn.setText("📋 Copy Summary"));
            pause.play();
        });

        Button closeBtn = new Button("Close Receipt");
        closeBtn.setPrefHeight(36);
        closeBtn.setPrefWidth(120);
        closeBtn.setStyle(
            "-fx-background-color: #1E293B; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> dialog.close());

        actionsBar.getChildren().addAll(copyBtn, closeBtn);

        root.getChildren().addAll(card, actionsBar);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(
                Objects.requireNonNull(OrderInvoiceDialog.class.getResource("/css/style.css")).toExternalForm()
            );
        } catch (Exception ignored) {}

        scene.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.ESCAPE || ke.getCode() == KeyCode.ENTER) {
                dialog.close();
            }
        });

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static HBox createBillRow(String label, String value, boolean isBold, String valueColor) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label v = new Label(value);
        v.setStyle(String.format(
            "-fx-font-size: 12px; -fx-font-weight: %s; -fx-text-fill: %s;",
            isBold ? "bold" : "normal", valueColor
        ));

        row.getChildren().addAll(l, spacer, v);
        return row;
    }

    private static Label createStatusPill(Order.Status status) {
        if (status == null) status = Order.Status.PLACED;
        String text = status.name().replace('_', ' ');

        Label pill = new Label(text);
        pill.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 3 9; -fx-background-radius: 12px; " +
            switch (status) {
                case PLACED -> "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;";
                case CONFIRMED -> "-fx-background-color: #E0F2FE; -fx-text-fill: #0284C7;";
                case PREPARING -> "-fx-background-color: #F3E8FF; -fx-text-fill: #9333EA;";
                case OUT_FOR_DELIVERY -> "-fx-background-color: #CCFBF1; -fx-text-fill: #0D9488;";
                case DELIVERED -> "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A;";
                case CANCELLED -> "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;";
            }
        );
        return pill;
    }

    private static void copyInvoiceToClipboard(Order order, List<OrderItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("FOODHUB TAX INVOICE & RECEIPT\n");
        sb.append("Order #").append(order.getOrderId()).append("\n");
        sb.append("Date: ").append(order.getOrderDate() != null ? order.getOrderDate().format(DATE_FORMATTER) : "N/A").append("\n");
        sb.append("Status: ").append(order.getOrderStatus()).append("\n");
        if (order.getDeliveryAddress() != null) {
            sb.append("Address: ").append(order.getDeliveryAddress()).append("\n");
        }
        if (order.getPaymentMethod() != null) {
            sb.append("Payment: ").append(order.getPaymentMethod()).append("\n");
        }
        sb.append("\nITEMS:\n");
        for (OrderItem it : items) {
            sb.append(String.format("  - %s x %d = ₹ %.2f\n",
                it.getFoodNameSnapshot(), it.getQuantity(),
                it.getLineTotal() != null ? it.getLineTotal().doubleValue() : 0.0));
        }
        sb.append("\nItem Subtotal: ₹ ").append(String.format("%.2f", order.getSubtotal() != null ? order.getSubtotal().doubleValue() : 0.0)).append("\n");
        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("Discount: - ₹ ").append(String.format("%.2f", order.getDiscountAmount().doubleValue())).append("\n");
        }
        sb.append("TOTAL PAID: ₹ ").append(String.format("%.2f", order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0)).append("\n");
        sb.append("Thank you for ordering with FoodHub!");

        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }
}
