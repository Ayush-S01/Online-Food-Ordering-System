package com.foodordering.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The shopping cart.
 *
 * <p>Data Structures used:
 * <ul>
 *   <li>{@code ArrayList<CartItem>} — ordered list of items for display.</li>
 *   <li>{@code HashMap<Integer, CartItem>} — O(1) lookup by {@code foodId}
 *       to detect duplicates and perform fast updates.</li>
 * </ul>
 *
 * <p>Cart logic is kept completely separate from the JavaFX UI layer.
 */
public class Cart {

    /** Ordered list of cart items for display / iteration. */
    private final List<CartItem> items = new ArrayList<>();

    /**
     * HashMap keyed by food_id for O(1) existence checks.
     * Algorithm: if food already in cart → increment quantity; else → add new CartItem.
     */
    private final Map<Integer, CartItem> itemMap = new HashMap<>();

    // ------------------------------------------------------------------ //
    //  Cart operations
    // ------------------------------------------------------------------ //

    /**
     * Adds a food item to the cart.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Check HashMap — O(1).</li>
     *   <li>If present, increment quantity on existing CartItem.</li>
     *   <li>If absent, create a new CartItem and add to both ArrayList and HashMap.</li>
     * </ol>
     *
     * @param foodItem the food item to add.
     * @param quantity how many to add (must be &ge; 1).
     * @throws IllegalArgumentException if food item is unavailable or quantity &lt; 1.
     */
    public void addItem(FoodItem foodItem, int quantity) {
        if (!foodItem.isAvailable()) {
            throw new IllegalArgumentException(
                    "'" + foodItem.getName() + "' is currently unavailable.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        CartItem existing = itemMap.get(foodItem.getFoodId()); // O(1) lookup
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem(foodItem, quantity);
            items.add(newItem);
            itemMap.put(foodItem.getFoodId(), newItem);
        }
    }

    /**
     * Removes a food item from the cart completely.
     *
     * @param foodId the ID of the food item to remove.
     * @return {@code true} if the item was present and removed.
     */
    public boolean removeItem(int foodId) {
        CartItem item = itemMap.remove(foodId);
        if (item != null) {
            items.remove(item);
            return true;
        }
        return false;
    }

    /**
     * Updates the quantity of an existing cart item.
     *
     * <p>If {@code quantity} is zero the item is removed from the cart.
     *
     * @param foodId   ID of the food item.
     * @param quantity the new quantity (&ge; 0; 0 removes the item).
     * @throws IllegalArgumentException if {@code quantity} &lt; 0.
     * @throws IllegalStateException    if the food item is not in the cart.
     */
    public void updateQuantity(int foodId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        CartItem item = itemMap.get(foodId);
        if (item == null) {
            throw new IllegalStateException("Food item not found in cart.");
        }
        if (quantity == 0) {
            removeItem(foodId);
        } else {
            item.setQuantity(quantity);
        }
    }

    /**
     * Clears all items from the cart.
     */
    public void clear() {
        items.clear();
        itemMap.clear();
    }

    // ------------------------------------------------------------------ //
    //  Calculations — BigDecimal for accurate monetary arithmetic
    // ------------------------------------------------------------------ //

    /**
     * Computes the subtotal by summing all line totals.
     * Algorithm: iterate ArrayList, accumulate BigDecimal.
     *
     * @return subtotal as BigDecimal.
     */
    public BigDecimal getSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : items) {
            subtotal = subtotal.add(item.getLineTotal());
        }
        return subtotal;
    }

    // ------------------------------------------------------------------ //
    //  Queries
    // ------------------------------------------------------------------ //

    /** Returns {@code true} if the cart has no items. */
    public boolean isEmpty() { return items.isEmpty(); }

    /** Returns the total number of distinct item lines in the cart. */
    public int getItemCount() { return items.size(); }

    /**
     * Returns the total quantity across all cart items
     * (used for the cart icon badge).
     */
    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    /**
     * Returns an unmodifiable view of the cart item list
     * (prevents direct mutation from outside this class).
     */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Returns {@code true} if a food item with the given ID is in the cart. */
    public boolean contains(int foodId) {
        return itemMap.containsKey(foodId); // O(1)
    }

    /** Returns the CartItem for the given foodId, or {@code null}. */
    public CartItem getItem(int foodId) {
        return itemMap.get(foodId); // O(1)
    }

    @Override
    public String toString() {
        return "Cart{items=" + items.size() + ", subtotal=" + getSubtotal() + '}';
    }
}
