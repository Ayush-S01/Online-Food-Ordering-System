package com.foodordering;

import com.foodordering.model.Cart;
import com.foodordering.model.CartItem;
import com.foodordering.model.FoodItem;
import com.foodordering.service.CartService;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Cart and CartService business logic.
 *
 * <p>Tests cover: add, remove, update quantity, subtotal calculation,
 * unavailable item rejection, invalid quantity rejection.
 */
@DisplayName("Cart Service Tests")
class CartServiceTest {

    private CartService cartService;
    private FoodItem pizza;
    private FoodItem burger;
    private FoodItem unavailableItem;

    @BeforeEach
    void setUp() {
        cartService = new CartService();

        pizza = new FoodItem(1, "Paneer Pizza", "Starters",
            "Delicious paneer pizza", new BigDecimal("250.00"), null, true);
        burger = new FoodItem(2, "Veg Burger", "Starters",
            "Crispy veg burger", new BigDecimal("120.00"), null, true);
        unavailableItem = new FoodItem(3, "Sold Out Item", "Main Course",
            "Not available", new BigDecimal("100.00"), null, false);
    }

    // ------------------------------------------------------------------ //
    //  Add to Cart
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Should add an available item to cart")
    void testAddAvailableItem() {
        cartService.addToCart(pizza);
        assertFalse(cartService.isCartEmpty());
        assertEquals(1, cartService.getItemCount());
    }

    @Test
    @DisplayName("Should reject unavailable food item")
    void testAddUnavailableItemThrows() {
        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> cartService.addToCart(unavailableItem));
        assertTrue(ex.getMessage().contains("unavailable"));
    }

    @Test
    @DisplayName("Adding same item twice should increase quantity, not add a new line")
    void testAddSameItemIncreasesQuantity() {
        cartService.addToCart(pizza);
        cartService.addToCart(pizza);
        assertEquals(1, cartService.getItemCount());       // still 1 line
        assertEquals(2, cartService.getTotalQuantity());   // quantity = 2
    }

    @Test
    @DisplayName("Should add multiple distinct items")
    void testAddMultipleItems() {
        cartService.addToCart(pizza);
        cartService.addToCart(burger);
        assertEquals(2, cartService.getItemCount());
    }

    // ------------------------------------------------------------------ //
    //  Remove from Cart
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Should remove an item from cart")
    void testRemoveItem() {
        cartService.addToCart(pizza);
        boolean removed = cartService.removeFromCart(pizza.getFoodId());
        assertTrue(removed);
        assertTrue(cartService.isCartEmpty());
    }

    @Test
    @DisplayName("Removing non-existent item returns false")
    void testRemoveNonExistentItem() {
        boolean removed = cartService.removeFromCart(999);
        assertFalse(removed);
    }

    // ------------------------------------------------------------------ //
    //  Quantity updates
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Should update item quantity")
    void testUpdateQuantity() {
        cartService.addToCart(pizza);
        cartService.updateQuantity(pizza.getFoodId(), 5);
        assertEquals(5, cartService.getCart().getItem(pizza.getFoodId()).getQuantity());
    }

    @Test
    @DisplayName("Setting quantity to 0 removes the item")
    void testUpdateQuantityToZeroRemovesItem() {
        cartService.addToCart(pizza);
        cartService.updateQuantity(pizza.getFoodId(), 0);
        assertTrue(cartService.isCartEmpty());
    }

    @Test
    @DisplayName("Negative quantity throws IllegalArgumentException")
    void testNegativeQuantityThrows() {
        cartService.addToCart(pizza);
        assertThrows(IllegalArgumentException.class,
            () -> cartService.updateQuantity(pizza.getFoodId(), -1));
    }

    @Test
    @DisplayName("Increment quantity increases by 1")
    void testIncrementQuantity() {
        cartService.addToCart(pizza);
        cartService.incrementQuantity(pizza.getFoodId());
        assertEquals(2, cartService.getCart().getItem(pizza.getFoodId()).getQuantity());
    }

    @Test
    @DisplayName("Decrement to 0 removes item")
    void testDecrementToZeroRemovesItem() {
        cartService.addToCart(pizza);
        cartService.decrementQuantity(pizza.getFoodId());  // qty goes to 0 → removed
        assertTrue(cartService.isCartEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Subtotal calculation
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Subtotal is correctly calculated with single item")
    void testSubtotalSingleItem() {
        cartService.addToCart(pizza, 3);
        // 250.00 × 3 = 750.00
        assertEquals(new BigDecimal("750.00"), cartService.getCart().getSubtotal());
    }

    @Test
    @DisplayName("Subtotal is correctly calculated with multiple items")
    void testSubtotalMultipleItems() {
        cartService.addToCart(pizza, 2);   // 250 × 2 = 500
        cartService.addToCart(burger, 3);  // 120 × 3 = 360
        // Total = 860.00
        assertEquals(new BigDecimal("860.00"), cartService.getCart().getSubtotal());
    }

    @Test
    @DisplayName("Empty cart has zero subtotal")
    void testEmptyCartSubtotal() {
        assertEquals(BigDecimal.ZERO, cartService.getCart().getSubtotal());
    }

    // ------------------------------------------------------------------ //
    //  Clear
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Clear empties the cart")
    void testClearCart() {
        cartService.addToCart(pizza);
        cartService.addToCart(burger);
        cartService.clearCart();
        assertTrue(cartService.isCartEmpty());
        assertEquals(0, cartService.getTotalQuantity());
    }

    // ------------------------------------------------------------------ //
    //  CartItem line total
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("CartItem line total equals price × quantity")
    void testCartItemLineTotal() {
        CartItem item = new CartItem(pizza, 4);
        // 250.00 × 4 = 1000.00
        assertEquals(new BigDecimal("1000.00"), item.getLineTotal());
    }

    @Test
    @DisplayName("CartItem with quantity < 1 throws IllegalArgumentException")
    void testCartItemZeroQuantityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new CartItem(pizza, 0));
    }
}
