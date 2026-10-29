package com.foodordering.model;

import java.math.BigDecimal;

/**
 * Represents a single entry in the shopping cart.
 *
 * <p>A CartItem pairs a {@link FoodItem} with a chosen quantity and provides
 * a computed line total (price × quantity).
 *
 * <p>OOP: Encapsulation. Meaningful helper method {@link #getLineTotal()}.
 */
public class CartItem {

    private FoodItem foodItem;
    private int quantity;

    // ------------------------------------------------------------------ //
    //  Constructors
    // ------------------------------------------------------------------ //

    public CartItem(FoodItem foodItem, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        this.foodItem = foodItem;
        this.quantity = quantity;
    }

    // ------------------------------------------------------------------ //
    //  Getters & Setters
    // ------------------------------------------------------------------ //

    public FoodItem getFoodItem()               { return foodItem;          }
    public void setFoodItem(FoodItem foodItem)  { this.foodItem = foodItem; }

    public int getQuantity()                    { return quantity;  }

    /**
     * Updates the quantity.
     *
     * @param quantity new quantity — must be &gt;= 1.
     * @throws IllegalArgumentException if quantity &lt; 1.
     */
    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity cannot be less than 1.");
        }
        this.quantity = quantity;
    }

    // ------------------------------------------------------------------ //
    //  Business logic
    // ------------------------------------------------------------------ //

    /**
     * Computes line total = unit price × quantity.
     * Uses BigDecimal to avoid floating-point rounding issues.
     *
     * @return line total as BigDecimal.
     */
    public BigDecimal getLineTotal() {
        return foodItem.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "CartItem{food='" + foodItem.getName() + "', qty=" + quantity
               + ", lineTotal=" + getLineTotal() + '}';
    }
}
