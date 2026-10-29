package com.foodordering;

import com.foodordering.controller.HomeController;
import com.foodordering.model.Customer;
import com.foodordering.model.FoodItem;
import com.foodordering.service.CartService;
import com.foodordering.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Home View & Sidebar Integration Tests")
class HomeViewTest {

    private static boolean jfxStarted = false;

    @BeforeAll
    static void initJFX() throws Exception {
        if (!jfxStarted) {
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(() -> {
                    jfxStarted = true;
                    latch.countDown();
                });
            } catch (IllegalStateException e) {
                jfxStarted = true;
                latch.countDown();
            }
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        }
    }

    @BeforeEach
    void setUp() {
        Customer testCust = new Customer(1, "Ayush", "ayush@example.com", "hash", "CUSTOMER", LocalDateTime.now());
        SessionManager.getInstance().login(testCust);
        HomeController.resetCart();
    }

    @Test
    @DisplayName("Home.fxml should load successfully and show Today's Special when cart is empty")
    void testHomeFxmlLoadingAndSpecials() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
                Parent root = loader.load();
                assertNotNull(root, "Root node of Home.fxml should not be null");

                Scene scene = new Scene(root);
                root.applyCss();

                HomeController controller = loader.getController();
                assertNotNull(controller, "HomeController should be instantiated");

                // Verify Today's Special container has 4 signature dishes
                VBox specials = getPrivateField(controller, "todaysSpecialsContainer");
                assertNotNull(specials, "todaysSpecialsContainer should be injected");
                assertEquals(4, specials.getChildren().size(), "todaysSpecialsContainer should have 4 specials");

                // When cart is empty, specials should be visible and cart drawer hidden
                ScrollPane specialsScroll = getPrivateField(controller, "specialsScrollPane");
                assertTrue(specialsScroll.isVisible(), "specialsScrollPane should be visible when cart is empty");

                VBox cartDrawer = getPrivateField(controller, "cartDrawerView");
                assertFalse(cartDrawer.isVisible(), "cartDrawerView should be hidden when cart is empty");

                // Bottom pop-up initial state
                Label popupItems = getPrivateField(controller, "popupCartItemsLabel");
                assertEquals("0 items in cart", popupItems.getText());

                Label popupTotal = getPrivateField(controller, "popupCartTotalLabel");
                assertEquals("₹ 0.00", popupTotal.getText());

            } catch (Throwable t) {
                error[0] = t;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (error[0] != null) {
            fail("Exception during Home.fxml test: " + error[0].getMessage(), error[0]);
        }
    }

    @Test
    @DisplayName("Adding an item should automatically open the cart drawer with in-cart recommendations")
    void testCartAdditionAutoOpensDrawer() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                root.applyCss();

                HomeController controller = loader.getController();
                CartService cs = HomeController.getSharedCartService();

                FoodItem biryani = new FoodItem(84, "Hyderabadi Chicken Biryani", "Main Course",
                        "Biryani", new BigDecimal("290.00"), null, true);
                biryani.setCuisine("Biryani & Mughlai");

                // Simulate adding item to cart
                cs.addToCart(biryani);

                // Invoke onItemAdded callback via reflection
                Method onItemAddedMethod = controller.getClass().getDeclaredMethod("onItemAdded");
                onItemAddedMethod.setAccessible(true);
                onItemAddedMethod.invoke(controller);

                // Verify cart drawer is now automatically open
                VBox cartDrawer = getPrivateField(controller, "cartDrawerView");
                assertTrue(cartDrawer.isVisible(), "cartDrawerView should automatically open on adding an item");

                ScrollPane specialsScroll = getPrivateField(controller, "specialsScrollPane");
                assertFalse(specialsScroll.isVisible(), "specialsScrollPane should hide when cart drawer is open");

                // Verify in-cart recommendations are populated
                VBox inCartRecs = getPrivateField(controller, "drawerRecommendationsContainer");
                assertNotNull(inCartRecs, "drawerRecommendationsContainer should exist");
                assertFalse(inCartRecs.getChildren().isEmpty(), "drawerRecommendationsContainer should contain recommended add-ons");

                // Verify pop-up reflects 1 item
                Label popupItems = getPrivateField(controller, "popupCartItemsLabel");
                assertEquals("1 item in cart", popupItems.getText());

                // Now simulate removing the item from cart
                cs.removeFromCart(84);
                Method onCartChangedMethod = controller.getClass().getDeclaredMethod("onCartChanged");
                onCartChangedMethod.setAccessible(true);
                onCartChangedMethod.invoke(controller);

                // Cart is now empty -> should automatically switch back to Today's Special!
                assertTrue(specialsScroll.isVisible(), "specialsScrollPane should automatically show when cart is empty");
                assertFalse(cartDrawer.isVisible(), "cartDrawerView should automatically hide when cart is empty");

            } catch (Throwable t) {
                error[0] = t;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (error[0] != null) {
            fail("Exception during cart auto-open test: " + error[0].getMessage(), error[0]);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }
}
