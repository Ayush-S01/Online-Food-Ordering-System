package com.foodordering.controller;

import com.foodordering.model.CartItem;
import com.foodordering.model.FoodItem;
import com.foodordering.service.CartService;
import com.foodordering.service.FoodService;
import com.foodordering.util.AlertUtil;
import com.foodordering.util.FoodCardBuilder;
import com.foodordering.util.NavigationUtil;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Modern Multi-Cuisine Food Menu screen.
 *
 * <p>Supports:
 * <ul>
 *   <li>Cuisine chip filtering (North Indian, South Indian, Chinese, Italian, Mexican, Fast Food, Biryani).</li>
 *   <li>Category dropdown filter (Starters, Main Course, Breads, Desserts, etc.).</li>
 *   <li>Pure Veg dietary filter toggle.</li>
 *   <li>Multi-criteria sorting (Rating, Price, Name).</li>
 *   <li>Live search by dish name, description, or cuisine.</li>
 *   <li>Interactive food cards with ratings, prep time, veg indicator, and live cart controls.</li>
 * </ul>
 */
public class MenuController {

    @FXML private TextField       searchField;
    @FXML private HBox            cuisineChipsBar;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> sortCombo;
    @FXML private CheckBox        vegOnlyCheckBox;
    @FXML private FlowPane        foodGrid;
    @FXML private Label           cartBadgeLabel;
    @FXML private Button          viewCartButton;
    @FXML private Button          homeButton;
    @FXML private Label           statusLabel;
    @FXML private Label           resultsCountLabel;

    private final FoodService  foodService  = new FoodService();
    private final CartService  cartService  = HomeController.getSharedCartService();
    private List<FoodItem>     allItems;
    private String             selectedCuisine = "All";
    private static String      initialCuisine = "All";

    public static void setInitialCategory(String cuisineOrCat) {
        initialCuisine = (cuisineOrCat != null && !cuisineOrCat.isBlank()) ? cuisineOrCat : "All";
    }

    @FXML
    private void initialize() {
        sortCombo.setItems(FXCollections.observableArrayList(
            "Rating: High to Low ⭐", "Price: Low to High", "Price: High to Low", "Name A-Z"
        ));
        sortCombo.setValue("Rating: High to Low ⭐");

        loadFoodItems();

        PauseTransition searchDebounce = new PauseTransition(Duration.millis(150));
        searchDebounce.setOnFinished(e -> applyFilters());
        searchField.textProperty().addListener((obs, old, nw) -> searchDebounce.playFromStart());

        categoryFilter.valueProperty().addListener((obs, old, nw) -> applyFilters());
        sortCombo.valueProperty().addListener((obs, old, nw) -> applyFilters());
        vegOnlyCheckBox.selectedProperty().addListener((obs, old, nw) -> applyFilters());

        refreshCartBadge();
    }

    private void loadFoodItems() {
        try {
            allItems = foodService.getAllAvailable();

            // Populate category filter
            List<String> categories = new ArrayList<>();
            categories.add("All Categories");
            categories.addAll(foodService.getCategories());
            categoryFilter.setItems(FXCollections.observableArrayList(categories));
            categoryFilter.setValue("All Categories");

            // Build cuisine filter pills
            buildCuisineChips();

            applyFilters();
        } catch (Exception e) {
            statusLabel.setText("Failed to load menu: " + e.getMessage());
        }
    }

    private void buildCuisineChips() {
        cuisineChipsBar.getChildren().clear();

        List<String> cuisines = new ArrayList<>();
        cuisines.add("All Cuisines");
        try {
            cuisines.addAll(foodService.getCuisines());
        } catch (Exception ignored) {}

        ToggleGroup chipGroup = new ToggleGroup();

        for (String c : cuisines) {
            ToggleButton chip = new ToggleButton(getCuisineEmoji(c) + " " + c);
            chip.getStyleClass().add("cuisine-chip");
            chip.setToggleGroup(chipGroup);

            if (c.equalsIgnoreCase(initialCuisine) || (initialCuisine.equals("All") && c.equals("All Cuisines"))) {
                chip.setSelected(true);
                selectedCuisine = c.equals("All Cuisines") ? "All" : c;
            }

            chip.setOnAction(e -> {
                if (chip.isSelected()) {
                    selectedCuisine = c.equals("All Cuisines") ? "All" : c;
                } else {
                    chip.setSelected(true); // Keep at least one selected
                }
                applyFilters();
            });

            cuisineChipsBar.getChildren().add(chip);
        }

        // Reset initial
        initialCuisine = "All";
    }

    private String getCuisineEmoji(String cuisine) {
        return switch (cuisine) {
            case "North Indian" -> "🍛";
            case "South Indian" -> "🥞";
            case "Chinese"      -> "🥢";
            case "Italian"      -> "🍕";
            case "Mexican"      -> "🌮";
            case "Fast Food"    -> "🍔";
            case "Biryani & Mughlai", "Biryani" -> "🍚";
            case "All Cuisines" -> "🍽️";
            default             -> "🥘";
        };
    }

    private void applyFilters() {
        if (allItems == null) return;
        List<FoodItem> result = allItems;

        // 1. Cuisine filter
        result = foodService.filterByCuisine(result, selectedCuisine);

        // 2. Category filter
        String cat = categoryFilter.getValue();
        result = foodService.filterByCategory(result, cat);

        // 3. Veg Only filter
        result = foodService.filterByVegOnly(result, vegOnlyCheckBox.isSelected());

        // 4. Search query
        result = foodService.searchByName(result, searchField.getText());

        // 5. Sort
        FoodService.SortOrder order = switch (sortCombo.getValue()) {
            case "Price: Low to High"   -> FoodService.SortOrder.PRICE_ASC;
            case "Price: High to Low"   -> FoodService.SortOrder.PRICE_DESC;
            case "Name A-Z"             -> FoodService.SortOrder.NAME_ASC;
            default                     -> FoodService.SortOrder.RATING_DESC;
        };
        result = foodService.sort(result, order);

        resultsCountLabel.setText(result.size() + " dishes found");
        renderFoodCards(result);
    }

    private void renderFoodCards(List<FoodItem> items) {
        foodGrid.getChildren().clear();

        if (items.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(40));
            Label empty = new Label("🍽️ No dishes match your selected filters.");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #7F8C8D; -fx-font-weight: bold;");
            Button resetBtn = new Button("Reset All Filters");
            resetBtn.getStyleClass().add("primary-btn");
            resetBtn.setOnAction(e -> {
                selectedCuisine = "All";
                categoryFilter.setValue("All Categories");
                searchField.clear();
                vegOnlyCheckBox.setSelected(false);
                buildCuisineChips();
                applyFilters();
            });
            emptyBox.getChildren().addAll(empty, resetBtn);
            foodGrid.getChildren().add(emptyBox);
            return;
        }

        for (FoodItem item : items) {
            foodGrid.getChildren().add(FoodCardBuilder.buildCard(item, cartService, () -> {
                refreshCartBadge();
            }));
        }
    }

    private void refreshCartBadge() {
        int count = cartService.getTotalQuantity();
        cartBadgeLabel.setText(count > 0 ? "(" + count + ")" : "");
    }

    @FXML
    private void handleViewCart(ActionEvent event) {
        try {
            Stage stage = (viewCartButton != null && viewCartButton.getScene() != null)
                ? (Stage) viewCartButton.getScene().getWindow() : null;
            NavigationUtil.navigateTo(stage, "/fxml/Cart.fxml");
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
