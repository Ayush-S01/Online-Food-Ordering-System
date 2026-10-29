package com.foodordering;

import com.foodordering.model.Order;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Order status state machine.
 *
 * <p>Validates that only permitted status transitions are allowed.
 */
@DisplayName("Order Status Transition Tests")
class OrderServiceTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
    }

    // ------------------------------------------------------------------ //
    //  Valid transitions
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("PLACED can transition to CONFIRMED")
    void testPlacedToConfirmed() {
        order.setOrderStatus(Order.Status.PLACED);
        assertTrue(order.canTransitionTo(Order.Status.CONFIRMED));
    }

    @Test
    @DisplayName("PLACED can transition to CANCELLED")
    void testPlacedToCancelled() {
        order.setOrderStatus(Order.Status.PLACED);
        assertTrue(order.canTransitionTo(Order.Status.CANCELLED));
    }

    @Test
    @DisplayName("CONFIRMED can transition to PREPARING")
    void testConfirmedToPreparing() {
        order.setOrderStatus(Order.Status.CONFIRMED);
        assertTrue(order.canTransitionTo(Order.Status.PREPARING));
    }

    @Test
    @DisplayName("PREPARING can transition to OUT_FOR_DELIVERY")
    void testPreparingToOutForDelivery() {
        order.setOrderStatus(Order.Status.PREPARING);
        assertTrue(order.canTransitionTo(Order.Status.OUT_FOR_DELIVERY));
    }

    @Test
    @DisplayName("OUT_FOR_DELIVERY can transition to DELIVERED")
    void testOutForDeliveryToDelivered() {
        order.setOrderStatus(Order.Status.OUT_FOR_DELIVERY);
        assertTrue(order.canTransitionTo(Order.Status.DELIVERED));
    }

    // ------------------------------------------------------------------ //
    //  Invalid transitions
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("PLACED cannot go directly to DELIVERED")
    void testPlacedToDeliveredInvalid() {
        order.setOrderStatus(Order.Status.PLACED);
        assertFalse(order.canTransitionTo(Order.Status.DELIVERED));
    }

    @Test
    @DisplayName("DELIVERED is terminal — no further transitions allowed")
    void testDeliveredIsTerminal() {
        order.setOrderStatus(Order.Status.DELIVERED);
        for (Order.Status s : Order.Status.values()) {
            assertFalse(order.canTransitionTo(s),
                "DELIVERED should not transition to " + s);
        }
    }

    @Test
    @DisplayName("CANCELLED is terminal — no further transitions allowed")
    void testCancelledIsTerminal() {
        order.setOrderStatus(Order.Status.CANCELLED);
        for (Order.Status s : Order.Status.values()) {
            assertFalse(order.canTransitionTo(s),
                "CANCELLED should not transition to " + s);
        }
    }

    @Test
    @DisplayName("CONFIRMED cannot go directly to DELIVERED")
    void testConfirmedToDeliveredInvalid() {
        order.setOrderStatus(Order.Status.CONFIRMED);
        assertFalse(order.canTransitionTo(Order.Status.DELIVERED));
    }
}
