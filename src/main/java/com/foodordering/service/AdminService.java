package com.foodordering.service;

import com.foodordering.model.Customer;
import com.foodordering.util.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Service exposing administrator-only operations.
 *
 * <p>Role-based authorization is enforced in this service layer.
 * Every public method calls {@link #assertAdmin()} before proceeding.
 * UI hiding alone is NOT relied upon for security.
 */
public class AdminService {

    private final FoodService   foodService   = new FoodService();
    private final DiscountService discountService = new DiscountService();
    private final OrderService  orderService  = new OrderService();

    // ------------------------------------------------------------------ //
    //  Dashboard
    // ------------------------------------------------------------------ //

    public int getTotalOrders()       throws SQLException { assertAdmin(); return orderService.getTotalOrderCount(); }
    public BigDecimal getRevenue()    throws SQLException { assertAdmin(); return orderService.getTotalRevenue();    }
    public int getTodayOrders()       throws SQLException { assertAdmin(); return orderService.getTodayOrderCount(); }
    public int getTotalFoodCount()    throws SQLException { assertAdmin(); return foodService.getAll().size();        }
    public int getActiveCouponsCount()throws SQLException {
        assertAdmin();
        return (int) discountService.getAllCoupons().stream().filter(com.foodordering.model.Discount::isEnabled).count();
    }

    // ------------------------------------------------------------------ //
    //  Food management (delegates to FoodService, which delegates to DAO)
    // ------------------------------------------------------------------ //

    public List<com.foodordering.model.FoodItem> getAllFood()
            throws SQLException { assertAdmin(); return foodService.getAll(); }

    public com.foodordering.model.FoodItem addFood(com.foodordering.model.FoodItem item)
            throws SQLException { assertAdmin(); return foodService.addFoodItem(item); }

    public void updateFood(com.foodordering.model.FoodItem item)
            throws SQLException { assertAdmin(); foodService.updateFoodItem(item); }

    public void deleteFood(int foodId)
            throws SQLException { assertAdmin(); foodService.deleteFoodItem(foodId); }

    public void setFoodAvailability(int foodId, boolean available)
            throws SQLException { assertAdmin(); foodService.setAvailability(foodId, available); }

    // ------------------------------------------------------------------ //
    //  Order management
    // ------------------------------------------------------------------ //

    public List<com.foodordering.model.Order> getAllOrders()
            throws SQLException { assertAdmin(); return orderService.getAllOrders(); }

    public void updateOrderStatus(int orderId, com.foodordering.model.Order.Status status)
            throws SQLException, IllegalAccessException {
        assertAdmin();
        orderService.updateOrderStatus(orderId, status);
    }

    // ------------------------------------------------------------------ //
    //  Coupon management
    // ------------------------------------------------------------------ //

    public List<com.foodordering.model.Discount> getAllCoupons()
            throws SQLException { assertAdmin(); return discountService.getAllCoupons(); }

    public com.foodordering.model.Discount addCoupon(com.foodordering.model.Discount d)
            throws SQLException { assertAdmin(); return discountService.addCoupon(d); }

    public void updateCoupon(com.foodordering.model.Discount d)
            throws SQLException { assertAdmin(); discountService.updateCoupon(d); }

    public void setCouponEnabled(int discountId, boolean enabled)
            throws SQLException { assertAdmin(); discountService.setCouponEnabled(discountId, enabled); }

    // ------------------------------------------------------------------ //
    //  Role enforcement
    // ------------------------------------------------------------------ //

    private void assertAdmin() {
        Customer user = SessionManager.getInstance().getCurrentUser();
        if (user == null || !user.isAdmin()) {
            throw new SecurityException(
                "Access denied. Administrator privileges required.");
        }
    }
}
