package com.foodordering.controller.admin;

import com.foodordering.model.Discount;
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
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for Admin Coupon Management screen.
 */
public class AdminCouponController {

    @FXML private TableView<Discount>                   couponsTable;
    @FXML private TableColumn<Discount, Integer>        colId;
    @FXML private TableColumn<Discount, String>         colCode;
    @FXML private TableColumn<Discount, Discount.Type>  colType;
    @FXML private TableColumn<Discount, BigDecimal>     colValue;
    @FXML private TableColumn<Discount, BigDecimal>     colMinOrder;
    @FXML private TableColumn<Discount, LocalDate>      colExpiry;
    @FXML private TableColumn<Discount, Boolean>        colEnabled;

    @FXML private TextField  codeField;
    @FXML private ComboBox<Discount.Type> typeCombo;
    @FXML private TextField  valueField;
    @FXML private TextField  minOrderField;
    @FXML private DatePicker expiryPicker;
    @FXML private CheckBox   enabledCheck;

    @FXML private Button addBtn;
    @FXML private Button updateBtn;
    @FXML private Button toggleBtn;
    @FXML private Button deleteBtn;
    @FXML private Button clearBtn;
    @FXML private Button backBtn;

    private final AdminService adminService = new AdminService();
    private final ObservableList<Discount> couponList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("discountId"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("couponCode"));
        colType.setCellValueFactory(new PropertyValueFactory<>("discountType"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("discountValue"));
        colMinOrder.setCellValueFactory(new PropertyValueFactory<>("minimumOrderAmount"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colEnabled.setCellValueFactory(new PropertyValueFactory<>("enabled"));

        typeCombo.setItems(FXCollections.observableArrayList(Discount.Type.values()));
        typeCombo.setValue(Discount.Type.PERCENTAGE);

        couponsTable.setItems(couponList);
        couponsTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, old, sel) -> populateForm(sel));

        loadCoupons();
    }

    private void loadCoupons() {
        couponList.clear();
        try {
            couponList.addAll(adminService.getAllCoupons());
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    private void populateForm(Discount d) {
        if (d == null) return;
        codeField.setText(d.getCouponCode() != null ? d.getCouponCode() : "");
        if (d.getDiscountType() != null) typeCombo.setValue(d.getDiscountType());
        valueField.setText(d.getDiscountValue() != null ? d.getDiscountValue().toPlainString() : "");
        minOrderField.setText(d.getMinimumOrderAmount() != null ? d.getMinimumOrderAmount().toPlainString() : "0.00");
        expiryPicker.setValue(d.getExpiryDate() != null ? d.getExpiryDate() : LocalDate.now().plusMonths(1));
        enabledCheck.setSelected(d.isEnabled());
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            Discount d = buildFromForm();
            adminService.addCoupon(d);
            AlertUtil.showInfo("Added", "Coupon '" + d.getCouponCode() + "' added successfully.");
            loadCoupons();
            clearForm();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        Discount sel = couponsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtil.showWarning("Select", "Please select a coupon from the table to update.");
            return;
        }
        try {
            Discount d = buildFromForm();
            d.setDiscountId(sel.getDiscountId());
            adminService.updateCoupon(d);
            AlertUtil.showInfo("Updated", "Coupon '" + d.getCouponCode() + "' updated successfully.");
            int selectedId = d.getDiscountId();
            loadCoupons();
            for (Discount c : couponList) {
                if (c.getDiscountId() == selectedId) {
                    couponsTable.getSelectionModel().select(c);
                    break;
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleToggleEnabled(ActionEvent event) {
        Discount sel = couponsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtil.showWarning("Select", "Please select a coupon from the table.");
            return;
        }
        try {
            adminService.setCouponEnabled(sel.getDiscountId(), !sel.isEnabled());
            int selectedId = sel.getDiscountId();
            loadCoupons();
            for (Discount c : couponList) {
                if (c.getDiscountId() == selectedId) {
                    couponsTable.getSelectionModel().select(c);
                    break;
                }
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Discount sel = couponsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtil.showWarning("Selection Required", "Please select a coupon from the table to delete.");
            return;
        }
        if (!AlertUtil.showConfirm("Confirm Delete",
                "Delete promo coupon '" + sel.getCouponCode() + "'? This action cannot be undone.")) {
            return;
        }
        try {
            adminService.deleteCoupon(sel.getDiscountId());
            AlertUtil.showInfo("Deleted", "Coupon '" + sel.getCouponCode() + "' has been deleted.");
            loadCoupons();
            clearForm();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        couponsTable.getSelectionModel().clearSelection();
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

    private Discount buildFromForm() {
        String code = codeField.getText() != null ? codeField.getText().trim().toUpperCase() : "";
        if (code.isEmpty()) throw new IllegalArgumentException("Coupon code is required.");
        Discount.Type type = typeCombo.getValue() != null ? typeCombo.getValue() : Discount.Type.PERCENTAGE;
        String valStr = valueField.getText() != null ? valueField.getText().trim().replace("%", "").replace("₹", "").trim() : "";
        if (valStr.isEmpty()) throw new IllegalArgumentException("Discount value is required.");
        BigDecimal val;
        try {
            val = new BigDecimal(valStr);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Discount value must be a valid number.");
        }
        String minStr = minOrderField.getText() != null ? minOrderField.getText().trim().replace("₹", "").trim() : "";
        BigDecimal min = minStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(minStr);
        LocalDate expiry = expiryPicker.getValue();
        if (expiry == null) throw new IllegalArgumentException("Expiry date is required.");
        return new Discount(0, code, type, val, min, expiry, enabledCheck.isSelected());
    }

    private void clearForm() {
        codeField.clear();
        valueField.clear();
        minOrderField.clear();
        expiryPicker.setValue(LocalDate.now().plusMonths(1));
        enabledCheck.setSelected(true);
    }
}
