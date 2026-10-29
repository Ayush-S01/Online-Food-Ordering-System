package com.foodordering.service;

import com.foodordering.model.Cart;
import com.foodordering.model.CartItem;
import com.foodordering.model.FoodItem;

/**
 * Service for shopping cart business logic.
 *
 * <p>Delegates to {@link Cart} (which contains the data structure logic).
 * This class enforces higher-level business rules such as availability checks.
 *
 * <p>The Cart object is maintained as a singleton-like instance within the
 * application session (one cart per user session).
 */
public class CartService {

    private final Cart cart;

    public CartService() {
        this.cart = new Cart();
    }

    // ------------------------------------------------------------------ //
    //  Cart operations
    // ------------------------------------------------------------------ //

    /**
     * Adds one unit of the given food item to the cart.
     *
     * @throws IllegalArgumentException if the item is unavailable.
     */
    public void addToCart(FoodItem foodItem) {
        if (!foodItem.isAvailable()) {
            throw new IllegalArgumentException(
                "'" + foodItem.getName() + "' is currently unavailable and cannot be added.");
        }
        cart.addItem(foodItem, 1);
    }

    /**
     * Adds the specified quantity of a food item to the cart.
     */
    public void addToCart(FoodItem foodItem, int quantity) {
        cart.addItem(foodItem, quantity);
    }

    /**
     * Removes a food item from the cart.
     *
     * @param foodId the ID of the food item.
     * @return {@code true} if removed.
     */
    public boolean removeFromCart(int foodId) {
        return cart.removeItem(foodId);
    }

    /**
     * Updates the quantity of a cart item.
     * Setting quantity to 0 removes the item.
     *
     * @param foodId   the food item's ID.
     * @param quantity the new quantity (0 removes the item).
     */
    public void updateQuantity(int foodId, int quantity) {
        cart.updateQuantity(foodId, quantity);
    }

    /** Increments the quantity of a cart item by 1. */
    public void incrementQuantity(int foodId) {
        CartItem item = cart.getItem(foodId);
        if (item != null) {
            cart.updateQuantity(foodId, item.getQuantity() + 1);
        }
    }

    /** Decrements the quantity of a cart item by 1. Removes if it reaches 0. */
    public void decrementQuantity(int foodId) {
        CartItem item = cart.getItem(foodId);
        if (item != null) {
            int newQty = item.getQuantity() - 1;
            cart.updateQuantity(foodId, newQty); // 0 removes automatically
        }
    }

    /** Clears all items from the cart. */
    public void clearCart() {
        cart.clear();
    }

    // ------------------------------------------------------------------ //
    //  Accessors
    // ------------------------------------------------------------------ //

    /** Returns the underlying Cart model object. */
    public Cart getCart() {
        return cart;
    }

    /** Returns {@code true} if the cart has no items. */
    public boolean isCartEmpty() {
        return cart.isEmpty();
    }

    /** Returns the current item count (distinct lines). */
    public int getItemCount() {
        return cart.getItemCount();
    }

    /** Returns the total quantity badge number. */
    public int getTotalQuantity() {
        return cart.getTotalQuantity();
    }
}
