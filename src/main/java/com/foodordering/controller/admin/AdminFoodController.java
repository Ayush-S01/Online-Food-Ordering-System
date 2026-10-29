package com.foodordering.controller.admin;

import com.foodordering.model.FoodItem;
import com.foodordering.service.AdminService;
import com.foodordering.util.AlertUtil;
import com.foodordering.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;

/**
 * Controller for Admin Food Management screen.
 */
public class AdminFoodController {

    @FXML private TableView<FoodItem>            foodTable;
    @FXML private TableColumn<FoodItem, Integer> colId;
    @FXML private TableColumn<FoodItem, String>  colName;
    @FXML private TableColumn<FoodItem, String>  colCuisine;
    @FXML private TableColumn<FoodItem, String>  colCategory;
    @FXML private TableColumn<FoodItem, BigDecimal> colPrice;
    @FXML private TableColumn<FoodItem, Boolean> colAvailable;

    @FXML private TextField  nameField;
    @FXML private TextField  categoryField;
    @FXML private TextField  cuisineField;
    @FXML private TextArea   descField;
    @FXML private TextField  priceField;
    @FXML private CheckBox   availableCheck;
    @FXML private CheckBox   isVegCheck;
    @FXML private TextField  imagePathField;

    @FXML private Button     addBtn;
    @FXML private Button     updateBtn;
    @FXML private Button     deleteBtn;
    @FXML private Button     toggleAvailBtn;
    @FXML private Button     backBtn;

    private final AdminService adminService = new AdminService();
    private final ObservableList<FoodItem> foodList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("foodId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colCuisine != null) colCuisine.setCellValueFactory(new PropertyValueFactory<>("cuisine"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

        foodTable.setItems(foodList);
        foodTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, sel) -> populateForm(sel));

        loadFood();
    }

    private void loadFood() {
        foodList.clear();
        try {
            foodList.addAll(adminService.getAllFood());
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load food items: " + e.getMessage());
        }
    }

    private void populateForm(FoodItem item) {
        if (item == null) return;
        nameField.setText(item.getName() != null ? item.getName() : "");
        categoryField.setText(item.getCategory() != null ? item.getCategory() : "");
        if (cuisineField != null) cuisineField.setText(item.getCuisine() != null ? item.getCuisine() : "");
        descField.setText(item.getDescription() != null ? item.getDescription() : "");
        priceField.setText(item.getPrice() != null ? item.getPrice().toPlainString() : "0.00");
        availableCheck.setSelected(item.isAvailable());
        if (isVegCheck != null) isVegCheck.setSelected(item.isVeg());
        if (imagePathField != null) imagePathField.setText(item.getImagePath() != null ? item.getImagePath() : "");
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            FoodItem item = buildItemFromForm();
            adminService.addFood(item);
            AlertUtil.showInfo("Success", "Food item added.");
            loadFood();
            clearForm();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        FoodItem selected = foodTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a food item from the table to update.");
            return;
        }
        try {
            FoodItem item = buildItemFromForm();
            item.setFoodId(selected.getFoodId());
            if ((item.getImagePath() == null || item.getImagePath().isBlank()) && selected.getImagePath() != null) {
                item.setImagePath(selected.getImagePath());
            }
            adminService.updateFood(item);
            AlertUtil.showInfo("Success", "Food item '" + item.getName() + "' updated successfully.");
            int selectedId = item.getFoodId();
            loadFood();
            for (FoodItem fi : foodList) {
                if (fi.getFoodId() == selectedId) {
                    foodTable.getSelectionModel().select(fi);
                    break;
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        foodTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        FoodItem selected = foodTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a food item to delete.");
            return;
        }
        if (!AlertUtil.showConfirm("Confirm Delete",
                "Delete '" + selected.getName() + "'? This cannot be undone.")) return;
        try {
            adminService.deleteFood(selected.getFoodId());
            AlertUtil.showInfo("Deleted", "Food item deleted.");
            loadFood();
            clearForm();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleToggleAvail(ActionEvent event) {
        FoodItem selected = foodTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a food item.");
            return;
        }
        try {
            adminService.setFoodAvailability(selected.getFoodId(), !selected.isAvailable());
            int selectedId = selected.getFoodId();
            loadFood();
            for (FoodItem fi : foodList) {
                if (fi.getFoodId() == selectedId) {
                    foodTable.getSelectionModel().select(fi);
                    break;
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            NavigationUtil.navigateTo(stage, "/fxml/admin/AdminDashboard.fxml");
        } catch (Exception e) {
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }

    private FoodItem buildItemFromForm() {
        String name  = nameField.getText() != null ? nameField.getText().trim() : "";
        String cat   = categoryField.getText() != null ? categoryField.getText().trim() : "";
        String cui   = (cuisineField != null && cuisineField.getText() != null && !cuisineField.getText().isBlank()) ? cuisineField.getText().trim() : "North Indian";
        String desc  = descField.getText() != null ? descField.getText().trim() : "";
        String priceStr = priceField.getText() != null ? priceField.getText().trim() : "";
        boolean avail = availableCheck.isSelected();
        boolean veg = (isVegCheck != null) ? isVegCheck.isSelected() : true;
        String imgPath = (imagePathField != null && imagePathField.getText() != null) ? imagePathField.getText().trim() : "";

        if (name.isEmpty() || cat.isEmpty() || priceStr.isEmpty()) {
            throw new IllegalArgumentException("Name, Category and Price are required.");
        }
        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Price must be a valid number.");
        }
        return new FoodItem(0, name, cat, cui, desc, price, imgPath.isEmpty() ? null : imgPath, veg, 4.5, "20-25 mins", avail);
    }

    private void clearForm() {
        nameField.clear(); categoryField.clear(); descField.clear();
        if (cuisineField != null) cuisineField.clear();
        priceField.clear();
        if (imagePathField != null) imagePathField.clear();
        availableCheck.setSelected(true);
        if (isVegCheck != null) isVegCheck.setSelected(true);
    }
}
