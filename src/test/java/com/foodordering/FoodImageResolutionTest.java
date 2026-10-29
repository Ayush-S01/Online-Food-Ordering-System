package com.foodordering;

import com.foodordering.model.FoodItem;
import com.foodordering.util.FoodCardBuilder;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Food Image Resolution Tests")
public class FoodImageResolutionTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized
        }
    }

    @Test
    @DisplayName("Verify that food items resolve images by name slug even when image_path is null")
    void testFoodImageResolutionWithNullImagePath() {
        List<String> dishNames = List.of(
            "Paneer Tikka",
            "Hara Bhara Kabab",
            "Dal Makhani",
            "Butter Chicken",
            "Paneer Butter Masala",
            "Chole Bhature",
            "Butter Naan",
            "Garlic Naan",
            "Gulab Jamun (2 pcs)",
            "Mango Lassi",
            "Masala Dosa",
            "Idli Vada Combo",
            "Mysore Masala Dosa",
            "Chicken Chettinad",
            "Chicken 65",
            "South Indian Filter Coffee",
            "Semiya Payasam",
            "Veg Manchurian Dry",
            "Chilli Paneer",
            "Veg Hakka Noodles",
            "Schezwan Chicken Rice",
            "Crispy Veg Spring Rolls",
            "Steamed Chicken Momos",
            "Margherita Pizza (10\")",
            "Farmhouse Veg Pizza",
            "Chicken Pepperoni Pizza",
            "Creamy Penne Alfredo",
            "Spicy Penne Arrabiata",
            "Cheesy Garlic Bread",
            "Classic Tiramisu",
            "Loaded Cheesy Nachos",
            "Paneer Fiesta Burrito",
            "Crispy Chicken Tacos (3)",
            "Grilled Veg Quesadilla",
            "Cinnamon Churros",
            "Crispy Veg Burger",
            "Double Cheese Chicken Burger",
            "Peri-Peri French Fries",
            "BBQ Chicken Wings (6 pcs)",
            "Belgian Chocolate Shake",
            "Cold Coffee with Ice Cream",
            "Hyderabadi Chicken Biryani",
            "Lucknowi Veg Dum Biryani",
            "Mutton Galouti Kabab",
            "Shahi Tukda"
        );

        for (String dishName : dishNames) {
            FoodItem item = new FoodItem(
                1, dishName, "TestCat", "North Indian", "Delicious",
                new BigDecimal("100.00"), null, true, 4.8, "15 mins", true
            );

            Image img = FoodCardBuilder.resolveFoodImage(item);
            assertNotNull(img, "Image should be resolved for dish: " + dishName);
            assertFalse(img.isError(), "Image load should not have error for dish: " + dishName);
            assertTrue(img.getWidth() > 0, "Image width should be greater than 0 for dish: " + dishName);
        }
    }

    @Test
    @DisplayName("Verify explicit filename resolution with and without prefix")
    void testExplicitFilenameResolution() {
        FoodItem item1 = new FoodItem(
            2, "Custom Dish", "TestCat", "Chinese", "Tasty",
            new BigDecimal("200.00"), "chilli_paneer.png", true, 4.7, "20 mins", true
        );
        Image img1 = FoodCardBuilder.resolveFoodImage(item1);
        assertNotNull(img1, "Explicit chilli_paneer.png should resolve");

        FoodItem item2 = new FoodItem(
            3, "Custom Dish 2", "TestCat", "Chinese", "Tasty",
            new BigDecimal("200.00"), "/images/chilli_paneer.png", true, 4.7, "20 mins", true
        );
        Image img2 = FoodCardBuilder.resolveFoodImage(item2);
        assertNotNull(img2, "Explicit /images/chilli_paneer.png should resolve");
    }
}
