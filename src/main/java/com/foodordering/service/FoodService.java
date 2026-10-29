package com.foodordering.service;

import com.foodordering.dao.FoodItemDAO;
import com.foodordering.model.FoodItem;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for food item operations.
 *
 * <p>Algorithms demonstrated:
 * <ul>
 *   <li><b>Linear search</b> — filter by name, description, or cuisine substring.</li>
 *   <li><b>Category & Cuisine filters</b> — multi-attribute filtering.</li>
 *   <li><b>Dietary filter</b> — Veg / Non-Veg classification.</li>
 *   <li><b>Comparator sort</b> — sort by Name (A-Z), Price (low-high, high-low), or Rating (high-low).</li>
 * </ul>
 */
public class FoodService {

    private final FoodItemDAO foodItemDAO = new FoodItemDAO();

    public enum SortOrder { NAME_ASC, PRICE_ASC, PRICE_DESC, RATING_DESC }

    // ------------------------------------------------------------------ //
    //  Read operations
    // ------------------------------------------------------------------ //

    public List<FoodItem> getAllAvailable() throws SQLException {
        return foodItemDAO.findAvailable();
    }

    public List<FoodItem> getAll() throws SQLException {
        return foodItemDAO.findAll();
    }

    public List<String> getCategories() throws SQLException {
        return foodItemDAO.findCategories();
    }

    public List<String> getCuisines() throws SQLException {
        return foodItemDAO.findCuisines();
    }

    // ------------------------------------------------------------------ //
    //  Search & Filter (algorithms)
    // ------------------------------------------------------------------ //

    /**
     * Filters food items by query string across name, description, and cuisine.
     */
    public List<FoodItem> searchByName(List<FoodItem> items, String query) {
        if (query == null || query.isBlank()) return items;
        String lower = query.toLowerCase().trim();
        return items.stream()
                    .filter(f -> f.getName().toLowerCase().contains(lower)
                              || (f.getDescription() != null && f.getDescription().toLowerCase().contains(lower))
                              || (f.getCuisine() != null && f.getCuisine().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
    }

    /**
     * Filters food items by category.
     */
    public List<FoodItem> filterByCategory(List<FoodItem> items, String category) {
        if (category == null || category.isBlank() || "All Categories".equalsIgnoreCase(category) || "All".equalsIgnoreCase(category)) {
            return items;
        }
        return items.stream()
                    .filter(f -> category.equalsIgnoreCase(f.getCategory()))
                    .collect(Collectors.toList());
    }

    /**
     * Filters food items by cuisine.
     */
    public List<FoodItem> filterByCuisine(List<FoodItem> items, String cuisine) {
        if (cuisine == null || cuisine.isBlank() || "All Cuisines".equalsIgnoreCase(cuisine) || "All".equalsIgnoreCase(cuisine)) {
            return items;
        }
        return items.stream()
                    .filter(f -> cuisine.equalsIgnoreCase(f.getCuisine()))
                    .collect(Collectors.toList());
    }

    /**
     * Filters food items by dietary preference (Veg only).
     */
    public List<FoodItem> filterByVegOnly(List<FoodItem> items, boolean vegOnly) {
        if (!vegOnly) return items;
        return items.stream()
                    .filter(FoodItem::isVeg)
                    .collect(Collectors.toList());
    }

    /**
     * Sorts food items using a Comparator.
     */
    public List<FoodItem> sort(List<FoodItem> items, SortOrder sortOrder) {
        Comparator<FoodItem> comparator = switch (sortOrder) {
            case NAME_ASC    -> Comparator.comparing(FoodItem::getName, String.CASE_INSENSITIVE_ORDER);
            case PRICE_ASC   -> Comparator.comparing(FoodItem::getPrice);
            case PRICE_DESC  -> Comparator.comparing(FoodItem::getPrice).reversed();
            case RATING_DESC -> Comparator.comparingDouble(FoodItem::getRating).reversed();
        };
        return items.stream().sorted(comparator).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Admin CRUD
    // ------------------------------------------------------------------ //

    public FoodItem addFoodItem(FoodItem item) throws SQLException {
        validateFoodItem(item);
        return foodItemDAO.insert(item);
    }

    public void updateFoodItem(FoodItem item) throws SQLException {
        validateFoodItem(item);
        foodItemDAO.update(item);
    }

    public void deleteFoodItem(int foodId) throws SQLException {
        foodItemDAO.delete(foodId);
    }

    public void setAvailability(int foodId, boolean available) throws SQLException {
        foodItemDAO.setAvailability(foodId, available);
    }

    private void validateFoodItem(FoodItem item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("Food name is required.");
        }
        if (item.getCategory() == null || item.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
    }
}
