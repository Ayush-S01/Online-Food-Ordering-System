package com.foodordering.controller;

import com.foodordering.model.CartItem;
import com.foodordering.model.FoodItem;
import com.foodordering.service.CartService;
import com.foodordering.service.FoodService;
import com.foodordering.util.AlertUtil;
import com.foodordering.util.FoodCardBuilder;
import com.foodordering.util.NavigationUtil;
import com.foodordering.util.SessionManager;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Controller for the modern Zomato-inspired Home screen.
 *
 * <p>Key UI Behaviors:
 * <ul>
 *   <li>Compact 208px food cards with center-cropped non-stretched photography.</li>
 *   <li>Wider Today's Special cards in the right sidebar ensuring complete text and button visibility.</li>
 *   <li>Distinct Recommendations section located inside the Cart Drawer below ordered items.</li>
 *   <li>Automatic drawer open when adding items, and automatic return to Today's Special when cart is empty.</li>
 * </ul>
 */
public class HomeController {

    // Top navbar
    @FXML private Label       welcomeLabel;
    @FXML private Button      ordersButton;
    @FXML private Button      logoutButton;
    @FXML private TextField   searchField;
    @FXML private CheckBox    vegOnlyCheckBox;

    // Cuisine anchor toolbar
    @FXML private HBox        cuisineChipsBar;
    @FXML private Label       resultsCountLabel;

    // Main left-shifted scrollable content
    @FXML private ScrollPane  mainScrollPane;
    @FXML private VBox        cuisineSectionsContainer;
    @FXML private VBox        homeOffersBox;
    @FXML private HBox        homeOffersContainer;

    // Right sidebar: Today's Special View
    @FXML private VBox        rightSidePanel;
    @FXML private ScrollPane  specialsScrollPane;
    @FXML private VBox        todaysSpecialsContainer;

    // Right sidebar: Cart Drawer View
    @FXML private VBox        cartDrawerView;
    @FXML private Label       drawerItemCountBadge;
    @FXML private Button      closeDrawerBtn;
    @FXML private ScrollPane  drawerCartScrollPane;
    @FXML private VBox        drawerCartItemsContainer;
    @FXML private VBox        drawerRecsBox;
    @FXML private VBox        drawerRecommendationsContainer;
    @FXML private VBox        drawerBillBox;
    @FXML private Label       drawerSubtotalLabel;
    @FXML private Label       drawerTaxLabel;
    @FXML private Label       drawerTotalLabel;
    @FXML private Button      drawerCheckoutBtn;

    // Bottom-right pinned Cart pop-up
    @FXML private HBox        bottomCartPopup;
    @FXML private Label       popupCartItemsLabel;
    @FXML private Label       popupCartTotalLabel;
    @FXML private Button      popupCartToggleBtn;

    private static CartService sharedCartService = new CartService();
    private final FoodService  foodService       = new FoodService();

    private List<FoodItem>    allItems           = new ArrayList<>();
    private String            selectedCuisine    = "All";

    @FXML
    private void initialize() {
        String name = SessionManager.getInstance().getCurrentUser() != null
            ? SessionManager.getInstance().getCurrentUser().getName()
            : "Foodie";
        welcomeLabel.setText("Hello, " + name + " 👋");

        loadMenuData();
        loadActiveOffers();
        updateBottomCartPopup();

        // Ensure initially showing Today's Special when cart is empty
        if (sharedCartService.isCartEmpty()) {
            showTodaysSpecial();
        } else {
            openCartDrawer();
        }

        PauseTransition searchDebounce = new PauseTransition(Duration.millis(150));
        searchDebounce.setOnFinished(e -> renderCuisineSections());
        searchField.textProperty().addListener((obs, old, nw) -> {
            if (!Objects.equals(old, nw)) {
                searchDebounce.playFromStart();
            }
        });

        vegOnlyCheckBox.selectedProperty().addListener((obs, old, nw) -> renderCuisineSections());

        // Ensure searchField is not focused by default
        javafx.application.Platform.runLater(() -> {
            if (mainScrollPane != null) mainScrollPane.requestFocus();
        });
    }

    public static CartService getSharedCartService() {
        return sharedCartService;
    }

    public static void resetCart() {
        sharedCartService = new CartService();
    }

    /**
     * Dynamically populates active promotional offers in the hero banner.
     */
    private void loadActiveOffers() {
        if (homeOffersContainer == null) return;
        homeOffersContainer.getChildren().clear();
        try {
            com.foodordering.service.DiscountService discountService = new com.foodordering.service.DiscountService();
            List<com.foodordering.model.Discount> activeCoupons = discountService.getActiveCoupons();
            if (activeCoupons == null || activeCoupons.isEmpty()) {
                if (homeOffersBox != null) {
                    homeOffersBox.setVisible(false);
                    homeOffersBox.setManaged(false);
                }
                return;
            }
            if (homeOffersBox != null) {
                homeOffersBox.setVisible(true);
                homeOffersBox.setManaged(true);
            }
            int count = 0;
            for (com.foodordering.model.Discount d : activeCoupons) {
                if (count++ >= 3) break;
                Label pill = new Label(d.getDisplayLabel());
                pill.setStyle("-fx-background-color: #FFF0EB; -fx-text-fill: #FF5E36; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8 4 8; -fx-background-radius: 6; -fx-border-color: #FF5E36; -fx-border-radius: 6;");
                homeOffersContainer.getChildren().add(pill);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Loads food items from database and populates views.
     */
    private void loadMenuData() {
        try {
            allItems = foodService.getAllAvailable();
            buildCuisineChips();
            renderCuisineSections();
            populateTodaysSpecial();
        } catch (Exception e) {
            resultsCountLabel.setText("Failed to load dishes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Builds horizontal cuisine quick-filter chips bar.
     */
    private void buildCuisineChips() {
        cuisineChipsBar.getChildren().clear();

        List<String> cuisines = new ArrayList<>();
        cuisines.add("All Cuisines");
        try {
            cuisines.addAll(foodService.getCuisines());
        } catch (Exception ignored) {}

        ToggleGroup chipGroup = new ToggleGroup();

        for (String c : cuisines) {
            String label = c.equals("All Cuisines") ? "🍲 All Cuisines" : FoodCardBuilder.getCuisineEmoji(c) + " " + c;
            ToggleButton chip = new ToggleButton(label);
            chip.getStyleClass().add("cuisine-chip");
            chip.setToggleGroup(chipGroup);

            if ((selectedCuisine.equals("All") && c.equals("All Cuisines")) || selectedCuisine.equalsIgnoreCase(c)) {
                chip.setSelected(true);
            }

            chip.setOnAction(e -> {
                if (chip.isSelected()) {
                    selectedCuisine = c.equals("All Cuisines") ? "All" : c;
                } else {
                    chip.setSelected(true);
                }
                renderCuisineSections();
            });

            cuisineChipsBar.getChildren().add(chip);
        }
    }

    /**
     * Renders cuisine sections with compact, non-stretching food cards.
     */
    public void renderCuisineSections() {
        FoodCardBuilder.clearSlotRegistry();
        cuisineSectionsContainer.getChildren().clear();

        if (allItems == null || allItems.isEmpty()) {
            showEmptyState("No food items available in the menu.");
            resultsCountLabel.setText("0 dishes available");
            return;
        }

        String search = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        boolean vegOnly = vegOnlyCheckBox != null && vegOnlyCheckBox.isSelected();

        Map<String, List<FoodItem>> grouped = new LinkedHashMap<>();
        String[] preferredOrder = {
            "North Indian", "South Indian", "Chinese", "Italian", "Mexican", "Fast Food", "Biryani & Mughlai"
        };
        for (String p : preferredOrder) {
            grouped.put(p, new ArrayList<>());
        }

        int totalMatching = 0;

        for (FoodItem item : allItems) {
            if (!selectedCuisine.equals("All") && !item.getCuisine().equalsIgnoreCase(selectedCuisine)) {
                continue;
            }
            if (vegOnly && !item.isVeg()) {
                continue;
            }
            if (!search.isEmpty()) {
                boolean matches = (item.getName() != null && item.getName().toLowerCase().contains(search))
                    || (item.getDescription() != null && item.getDescription().toLowerCase().contains(search))
                    || (item.getCategory() != null && item.getCategory().toLowerCase().contains(search))
                    || (item.getCuisine() != null && item.getCuisine().toLowerCase().contains(search));
                if (!matches) continue;
            }

            grouped.computeIfAbsent(item.getCuisine(), k -> new ArrayList<>()).add(item);
            totalMatching++;
        }

        resultsCountLabel.setText(totalMatching + " dishes found");

        if (totalMatching == 0) {
            showEmptyState("No dishes match \"" + search + "\"" + (vegOnly ? " (Pure Veg)" : ""));
            return;
        }

        for (Map.Entry<String, List<FoodItem>> entry : grouped.entrySet()) {
            String cuisineName = entry.getKey();
            List<FoodItem> items = entry.getValue();

            if (items.isEmpty()) continue;

            VBox sectionBox = new VBox(10);
            sectionBox.getStyleClass().add("cuisine-section-box");
            sectionBox.setMaxWidth(880);
            sectionBox.setAlignment(Pos.TOP_CENTER);

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(10, 4, 4, 4));
            header.setMaxWidth(880);

            Label emoji = new Label(FoodCardBuilder.getCuisineEmoji(cuisineName));
            emoji.setStyle("-fx-font-size: 22px;");

            Label title = new Label(cuisineName + " Specialities");
            title.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label countBadge = new Label(items.size() + " items");
            countBadge.setStyle("-fx-background-color: #F1F2F6; -fx-text-fill: #7F8C8D; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 12;");

            header.getChildren().addAll(emoji, title, spacer, countBadge);

            Separator sep = new Separator();
            sep.setMaxWidth(880);

            FlowPane grid = new FlowPane();
            grid.setHgap(14);
            grid.setVgap(16);
            grid.setPrefWrapLength(880);
            grid.setMaxWidth(880);
            grid.setAlignment(Pos.CENTER);

            for (FoodItem item : items) {
                VBox card = FoodCardBuilder.buildCard(item, sharedCartService, this::onItemAdded, this::onCartChanged);
                grid.getChildren().add(card);
            }

            sectionBox.getChildren().addAll(header, sep, grid);
            cuisineSectionsContainer.getChildren().add(sectionBox);
        }

        populateTodaysSpecial();
    }

    /**
     * Populates Today's Special in the right sidebar with wide, fully visible cards.
     */
    private void populateTodaysSpecial() {
        if (todaysSpecialsContainer == null || allItems == null || allItems.isEmpty()) return;
        todaysSpecialsContainer.getChildren().clear();

        List<String> specialNames = List.of(
            "Butter Chicken",
            "Hyderabadi Chicken Biryani",
            "Farmhouse Veg Pizza",
            "Gulab Jamun (2 pcs)"
        );

        List<FoodItem> specials = new ArrayList<>();
        for (String target : specialNames) {
            for (FoodItem item : allItems) {
                if (item.getName().equalsIgnoreCase(target) || item.getName().startsWith(target)) {
                    specials.add(item);
                    break;
                }
            }
        }

        for (FoodItem item : specials) {
            HBox miniCard = FoodCardBuilder.buildMiniCard(item, sharedCartService, this::onItemAdded, this::onCartChanged);
            todaysSpecialsContainer.getChildren().add(miniCard);
        }
    }

    /**
     * Updates recommendations displayed inside the Cart Drawer below the ordered items.
     */
    private void updateDrawerRecommendations() {
        if (drawerRecommendationsContainer == null || allItems == null || allItems.isEmpty()) return;
        drawerRecommendationsContainer.getChildren().clear();

        Set<Integer> cartItemIds = new HashSet<>();
        Set<String> cartCuisines = new HashSet<>();
        for (CartItem ci : sharedCartService.getCart().getItems()) {
            cartItemIds.add(ci.getFoodItem().getFoodId());
            cartCuisines.add(ci.getFoodItem().getCuisine());
        }

        List<String> targetNames = new ArrayList<>();
        if (cartCuisines.contains("North Indian") || cartCuisines.contains("Biryani & Mughlai") || cartCuisines.contains("Biryani")) {
            targetNames.addAll(List.of("Garlic Naan", "Butter Naan", "Mango Lassi", "Gulab Jamun (2 pcs)", "Hara Bhara Kabab"));
        }
        if (cartCuisines.contains("Italian")) {
            targetNames.addAll(List.of("Cheesy Garlic Bread", "Classic Tiramisu", "Belgian Chocolate Shake"));
        }
        if (cartCuisines.contains("Mexican")) {
            targetNames.addAll(List.of("Loaded Cheesy Nachos", "Cinnamon Churros", "Belgian Chocolate Shake"));
        }
        if (cartCuisines.contains("South Indian")) {
            targetNames.addAll(List.of("South Indian Filter Coffee", "Idli Vada Combo", "Semiya Payasam"));
        }
        if (cartCuisines.contains("Chinese")) {
            targetNames.addAll(List.of("Crispy Veg Spring Rolls", "Chilli Paneer", "Belgian Chocolate Shake"));
        }
        if (cartCuisines.contains("Fast Food")) {
            targetNames.addAll(List.of("Peri-Peri French Fries", "Belgian Chocolate Shake", "Cold Coffee with Ice Cream"));
        }

        // Universal accompaniments fallback
        targetNames.addAll(List.of(
            "Garlic Naan", "Cheesy Garlic Bread", "Mango Lassi",
            "Peri-Peri French Fries", "Gulab Jamun (2 pcs)", "Paneer Tikka", "Belgian Chocolate Shake"
        ));

        List<FoodItem> recs = new ArrayList<>();
        Set<Integer> addedIds = new HashSet<>();
        for (String target : targetNames) {
            for (FoodItem item : allItems) {
                if ((item.getName().equalsIgnoreCase(target) || item.getName().startsWith(target)) && item.isAvailable()) {
                    if (!cartItemIds.contains(item.getFoodId()) && !addedIds.contains(item.getFoodId())) {
                        recs.add(item);
                        addedIds.add(item.getFoodId());
                        if (recs.size() >= 3) break;
                    }
                }
            }
            if (recs.size() >= 3) break;
        }

        for (FoodItem item : recs) {
            HBox recCard = FoodCardBuilder.buildCartRecommendationCard(item, sharedCartService, this::onItemAdded, this::onCartChanged);
            drawerRecommendationsContainer.getChildren().add(recCard);
        }
    }

    /**
     * Fired when a dish is newly added (+ ADD clicked).
     * Rule: Automatically opens the cart drawer!
     */
    private void onItemAdded() {
        updateBottomCartPopup();
        openCartDrawer();
    }

    /**
     * Fired when item quantity is modified or item removed.
     * Rule: If cart is empty, automatically show Today's Special.
     */
    private void onCartChanged() {
        updateBottomCartPopup();
        if (sharedCartService.isCartEmpty()) {
            showTodaysSpecial();
        } else if (cartDrawerView != null && cartDrawerView.isVisible()) {
            renderCartDrawer();
            updateDrawerRecommendations();
        }
    }

    /**
     * Refreshes the bottom-right floating cart pop-up card.
     */
    private void updateBottomCartPopup() {
        if (popupCartItemsLabel == null || popupCartTotalLabel == null) return;

        int count = sharedCartService.getTotalQuantity();
        BigDecimal subtotal = sharedCartService.getCart().getSubtotal();

        if (count > 0) {
            popupCartItemsLabel.setText(count + (count == 1 ? " item" : " items") + " in cart");
            popupCartTotalLabel.setText("₹ " + subtotal.toPlainString());
            if (bottomCartPopup != null) {
                bottomCartPopup.setOpacity(1.0);
            }
        } else {
            popupCartItemsLabel.setText("0 items in cart");
            popupCartTotalLabel.setText("₹ 0.00");
        }
    }

    /**
     * Toggles between Specials view and Cart Drawer view.
     */
    @FXML
    private void handleToggleCartDrawer() {
        if (cartDrawerView != null && cartDrawerView.isVisible()) {
            showTodaysSpecial();
        } else {
            openCartDrawer();
        }
    }

    private void openCartDrawer() {
        if (specialsScrollPane != null) {
            specialsScrollPane.setVisible(false);
            specialsScrollPane.setManaged(false);
        }
        if (cartDrawerView != null) {
            cartDrawerView.setVisible(true);
            cartDrawerView.setManaged(true);
        }
        if (popupCartToggleBtn != null) {
            popupCartToggleBtn.setText("View Specials 🔥");
        }
        renderCartDrawer();
        updateDrawerRecommendations();
    }

    @FXML
    private void handleCloseCartDrawer() {
        showTodaysSpecial();
    }

    /**
     * Shows Today's Special view in the right sidebar.
     */
    private void showTodaysSpecial() {
        if (cartDrawerView != null) {
            cartDrawerView.setVisible(false);
            cartDrawerView.setManaged(false);
        }
        if (specialsScrollPane != null) {
            specialsScrollPane.setVisible(true);
            specialsScrollPane.setManaged(true);
        }
        if (popupCartToggleBtn != null) {
            popupCartToggleBtn.setText("View Cart ▲");
        }
    }

    /**
     * Renders items and bill breakdown in the right sidebar Cart Drawer.
     */
    private void renderCartDrawer() {
        if (drawerCartItemsContainer == null) return;
        drawerCartItemsContainer.getChildren().clear();

        List<CartItem> items = sharedCartService.getCart().getItems();
        int count = sharedCartService.getTotalQuantity();

        if (drawerItemCountBadge != null) {
            drawerItemCountBadge.setText(count + (count == 1 ? " item" : " items"));
        }

        if (items.isEmpty()) {
            showTodaysSpecial();
            return;
        }

        for (CartItem ci : items) {
            drawerCartItemsContainer.getChildren().add(createDrawerCartRow(ci));
        }

        BigDecimal subtotal = sharedCartService.getCart().getSubtotal();
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);

        if (drawerSubtotalLabel != null) drawerSubtotalLabel.setText("₹ " + subtotal.toPlainString());
        if (drawerTaxLabel != null) drawerTaxLabel.setText("₹ " + tax.toPlainString());
        if (drawerTotalLabel != null) drawerTotalLabel.setText("₹ " + total.toPlainString());
        if (drawerCheckoutBtn != null) {
            drawerCheckoutBtn.setText("Proceed to Checkout (₹ " + total.toPlainString() + ") ➔");
        }
    }

    /**
     * Builds an interactive horizontal row for a cart item in the sidebar drawer.
     */
    private HBox createDrawerCartRow(CartItem ci) {
        HBox row = new HBox(8);
        row.getStyleClass().add("drawer-cart-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Node vegIcon = FoodCardBuilder.createDietaryBadge(ci.getFoodItem().isVeg());

        VBox info = new VBox(2);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(ci.getFoodItem().getName());
        nameLabel.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(160);

        Label priceLabel = new Label("₹ " + ci.getFoodItem().getPrice().toPlainString());
        priceLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7F8C8D;");
        info.getChildren().addAll(nameLabel, priceLabel);

        // Stepper: [-] [qty] [+] with visible white symbols
        HBox stepper = new HBox(3);
        stepper.setAlignment(Pos.CENTER);
        stepper.setStyle("-fx-background-color: #E8F8F5; -fx-border-color: #27AE60; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 2 4 2 4;");

        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().add("qty-btn-small");
        minusBtn.setMinWidth(24);
        minusBtn.setPrefWidth(24);
        minusBtn.setMaxWidth(24);
        minusBtn.setMinHeight(24);
        minusBtn.setPrefHeight(24);
        minusBtn.setMaxHeight(24);
        minusBtn.setPadding(Insets.EMPTY);
        minusBtn.setAlignment(Pos.CENTER);
        minusBtn.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0;");
        minusBtn.setOnAction(e -> {
            sharedCartService.decrementQuantity(ci.getFoodItem().getFoodId());
            FoodCardBuilder.notifySlotChanged(ci.getFoodItem().getFoodId(), sharedCartService);
            onCartChanged();
        });

        Label qtyLabel = new Label(String.valueOf(ci.getQuantity()));
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11.5px; -fx-text-fill: #27AE60; -fx-padding: 0 4 0 4;");
        qtyLabel.setMinWidth(18);
        qtyLabel.setAlignment(Pos.CENTER);

        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("qty-btn-small");
        plusBtn.setMinWidth(24);
        plusBtn.setPrefWidth(24);
        plusBtn.setMaxWidth(24);
        plusBtn.setMinHeight(24);
        plusBtn.setPrefHeight(24);
        plusBtn.setMaxHeight(24);
        plusBtn.setPadding(Insets.EMPTY);
        plusBtn.setAlignment(Pos.CENTER);
        plusBtn.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0;");
        plusBtn.setOnAction(e -> {
            sharedCartService.incrementQuantity(ci.getFoodItem().getFoodId());
            FoodCardBuilder.notifySlotChanged(ci.getFoodItem().getFoodId(), sharedCartService);
            onCartChanged();
        });

        stepper.getChildren().addAll(minusBtn, qtyLabel, plusBtn);

        Label lineTotal = new Label("₹ " + ci.getLineTotal().toPlainString());
        lineTotal.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        lineTotal.setMinWidth(55);
        lineTotal.setAlignment(Pos.CENTER_RIGHT);

        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("remove-btn");
        removeBtn.setOnAction(e -> {
            sharedCartService.removeFromCart(ci.getFoodItem().getFoodId());
            FoodCardBuilder.notifySlotChanged(ci.getFoodItem().getFoodId(), sharedCartService);
            onCartChanged();
        });

        row.getChildren().addAll(vegIcon, info, stepper, lineTotal, removeBtn);
        return row;
    }

    /**
     * Navigates directly from the right sidebar drawer to the Checkout screen.
     */
    @FXML
    private void handleDrawerCheckout(ActionEvent event) {
        if (sharedCartService.isCartEmpty()) {
            AlertUtil.showWarning("Empty Cart", "Please add dishes before checking out.");
            return;
        }
        try {
            Stage stage = getStage(drawerCheckoutBtn);
            NavigationUtil.navigateTo(stage, "/fxml/Checkout.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }

    private void showEmptyState(String message) {
        VBox emptyBox = new VBox(12);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(50));

        Label icon = new Label("🍽️");
        icon.setStyle("-fx-font-size: 42px;");

        Label msg = new Label(message);
        msg.setStyle("-fx-font-size: 16px; -fx-text-fill: #7F8C8D; -fx-font-weight: bold;");

        Button resetBtn = new Button("Clear Search & Filters");
        resetBtn.getStyleClass().add("primary-btn");
        resetBtn.setOnAction(e -> {
            searchField.clear();
            vegOnlyCheckBox.setSelected(false);
            selectedCuisine = "All";
            buildCuisineChips();
            renderCuisineSections();
        });

        emptyBox.getChildren().addAll(icon, msg, resetBtn);
        cuisineSectionsContainer.getChildren().add(emptyBox);
    }

    private Stage getStage(Node fallbackNode) {
        if (fallbackNode != null && fallbackNode.getScene() != null && fallbackNode.getScene().getWindow() instanceof Stage s) {
            return s;
        }
        for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
            if (w instanceof Stage s && s.isShowing()) {
                return s;
            }
        }
        return null;
    }

    @FXML
    private void handleOrders(ActionEvent event) {
        try {
            Stage stage = getStage(ordersButton);
            NavigationUtil.navigateTo(stage, "/fxml/OrderHistory.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().logout();
        HomeController.resetCart();
        try {
            Stage stage = getStage(logoutButton);
            NavigationUtil.navigateTo(stage, "/fxml/Login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }
}
