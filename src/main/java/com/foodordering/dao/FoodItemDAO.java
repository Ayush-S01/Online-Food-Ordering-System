package com.foodordering.dao;

import com.foodordering.database.DatabaseConnection;
import com.foodordering.model.FoodItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link FoodItem}.
 *
 * <p>Supports retrieval by category and cuisine, dietary filtering (veg/non-veg),
 * ratings, and preparation time.
 */
public class FoodItemDAO {

    private static final String BASE_SELECT =
        "SELECT food_id, name, category, cuisine, description, price, image_path, "
        + "is_veg, rating, prep_time, available FROM food_items ";

    /**
     * Retrieves all food items from the database.
     */
    public List<FoodItem> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY cuisine, category, name";
        List<FoodItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Retrieves only available food items.
     */
    public List<FoodItem> findAvailable() throws SQLException {
        String sql = BASE_SELECT + "WHERE available = 1 ORDER BY cuisine, category, name";
        List<FoodItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Finds a food item by primary key.
     */
    public Optional<FoodItem> findById(int foodId) throws SQLException {
        String sql = BASE_SELECT + "WHERE food_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, foodId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns all distinct categories.
     */
    public List<String> findCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM food_items ORDER BY category";
        List<String> cats = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) cats.add(rs.getString("category"));
        }
        return cats;
    }

    /**
     * Returns all distinct cuisines.
     */
    public List<String> findCuisines() throws SQLException {
        String sql = "SELECT DISTINCT cuisine FROM food_items WHERE cuisine IS NOT NULL AND cuisine != '' ORDER BY cuisine";
        List<String> cuisines = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) cuisines.add(rs.getString("cuisine"));
        }
        return cuisines;
    }

    /**
     * Inserts a new food item.
     */
    public FoodItem insert(FoodItem item) throws SQLException {
        String sql = "INSERT INTO food_items (name, category, cuisine, description, price, "
                   + "image_path, is_veg, rating, prep_time, available) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParams(ps, item);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setFoodId(keys.getInt(1));
            }
        }
        return item;
    }

    /**
     * Updates an existing food item.
     */
    public void update(FoodItem item) throws SQLException {
        String sql = "UPDATE food_items SET name=?, category=?, cuisine=?, description=?, "
                   + "price=?, image_path=?, is_veg=?, rating=?, prep_time=?, available=? WHERE food_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParams(ps, item);
            ps.setInt(11, item.getFoodId());
            ps.executeUpdate();
        }
    }

    /**
     * Toggles the availability of a food item.
     */
    public void setAvailability(int foodId, boolean available) throws SQLException {
        String sql = "UPDATE food_items SET available=? WHERE food_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, available);
            ps.setInt(2, foodId);
            ps.executeUpdate();
        }
    }

    /**
     * Deletes a food item by ID.
     */
    public void delete(int foodId) throws SQLException {
        String sql = "DELETE FROM food_items WHERE food_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, foodId);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void setParams(PreparedStatement ps, FoodItem item) throws SQLException {
        ps.setString(1, item.getName());
        ps.setString(2, item.getCategory());
        ps.setString(3, item.getCuisine() != null ? item.getCuisine() : "North Indian");
        ps.setString(4, item.getDescription());
        ps.setBigDecimal(5, item.getPrice());
        ps.setString(6, item.getImagePath());
        ps.setBoolean(7, item.isVeg());
        ps.setDouble(8, item.getRating() > 0 ? item.getRating() : 4.5);
        ps.setString(9, item.getPrepTime() != null ? item.getPrepTime() : "20-25 mins");
        ps.setBoolean(10, item.isAvailable());
    }

    private FoodItem mapRow(ResultSet rs) throws SQLException {
        String cuisine = "North Indian";
        boolean isVeg = true;
        double rating = 4.5;
        String prepTime = "20-25 mins";

        try {
            String c = rs.getString("cuisine");
            if (c != null && !c.isBlank()) cuisine = c;
            isVeg = rs.getBoolean("is_veg");
            double r = rs.getDouble("rating");
            if (r > 0) rating = r;
            String pt = rs.getString("prep_time");
            if (pt != null && !pt.isBlank()) prepTime = pt;
        } catch (SQLException ignored) {
            // Fallbacks for older schemas
        }

        return new FoodItem(
            rs.getInt("food_id"),
            rs.getString("name"),
            rs.getString("category"),
            cuisine,
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getString("image_path"),
            isVeg,
            rating,
            prepTime,
            rs.getBoolean("available")
        );
    }
}
