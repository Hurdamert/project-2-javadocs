package com.example;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class ManagerController {

    // --- FXML refs (from ManagerPage.fxml)
    @FXML
    private TableView<InventoryItem> inventoryTable;
    @FXML
    private TableColumn<InventoryItem, String> colItem;
    @FXML
    private TableColumn<InventoryItem, Integer> colStock;

    @FXML
    private TextField nameField;
    @FXML
    private TextField qtyField;
    @FXML
    private Label statusLabel;

    @FXML
    private BarChart<String, Number> supplyChart;

    // --- DB config (re-use your existing dbSetup)
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db";
    private final dbSetup my = new dbSetup();

    private final ObservableList<InventoryItem> inventory = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Table column bindings
        colItem.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("qty"));

        inventoryTable.setItems(inventory);

        // When selecting a row, populate the form (for update)
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                nameField.setText(sel.getName());
                qtyField.setText(String.valueOf(sel.getQty()));
            }
        });

        // Load data + chart
        refreshInventory();
        refreshChart();
    }

    // --- UI actions

    @FXML
    private void onAddInventory() {
        String name = nameField.getText().trim();
        String qtyStr = qtyField.getText().trim();
        if (name.isBlank() || qtyStr.isBlank()) {
            status("Enter name and quantity.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            status("Quantity must be an integer.");
            return;
        }

        String sql = "INSERT INTO inventory (item_name, quantity) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, qty);
            ps.executeUpdate();
            status("Added: " + name + " (" + qty + ")");
            clearForm();
            refreshInventory();
            refreshChart();
        } catch (Exception ex) {
            ex.printStackTrace();
            status("Add failed: " + ex.getMessage());
        }
    }

    @FXML
    private void onUpdateInventory() {
        InventoryItem sel = inventoryTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            status("Select a row to update.");
            return;
        }
        String name = nameField.getText().trim();
        String qtyStr = qtyField.getText().trim();
        if (name.isBlank() || qtyStr.isBlank()) {
            status("Enter name and quantity.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            status("Quantity must be an integer.");
            return;
        }

        // Prefer updating by a stable ID column if you have one (inventory_id). Example
        // shown:
        String sql = "UPDATE inventory SET item_name = ?, quantity = ? WHERE inventory_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd)) {
            Integer id = sel.getId();

            if (id == null) {
                try (PreparedStatement ps = conn
                        .prepareStatement("UPDATE inventory SET quantity = ? WHERE item_name = ?")) {
                    ps.setInt(1, qty);
                    ps.setString(2, sel.getName());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, name);
                    ps.setInt(2, qty);
                    ps.setInt(3, id);
                    ps.executeUpdate();
                }
            }

            status("Updated: " + name + " -> " + qty);
            clearForm();
            refreshInventory();
            refreshChart();
        } catch (Exception ex) {
            ex.printStackTrace();
            status("Update failed: " + ex.getMessage());
        }
    }

    // --- Data loaders

    private void refreshInventory() {
        inventory.clear();

        // Adjust columns to match your schema
        String sql = "SELECT inventory_id, item_name, quantity FROM inventory ORDER BY item_name";

        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Integer id = (Integer) rs.getObject("inventory_id"); // nullable if no id column
                String name = rs.getString("item_name");
                int qty = rs.getInt("quantity");
                inventory.add(new InventoryItem(id, name, qty));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status("Load failed: " + ex.getMessage());
        }
    }

    private void refreshChart() {
        supplyChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (InventoryItem it : inventory) {
            s.getData().add(new XYChart.Data<>(it.getName(), it.getQty()));
        }
        supplyChart.getData().add(s);
    }

    private void status(String msg) {
        statusLabel.setText(msg);
    }

    private void clearForm() {
        nameField.clear();
        qtyField.clear();
        inventoryTable.getSelectionModel().clearSelection();
    }

    // --- model for the TableView
    public static class InventoryItem {
        private final Integer id; // can be null if table lacks id column
        private final StringProperty name = new SimpleStringProperty();
        private final IntegerProperty qty = new SimpleIntegerProperty();

        public InventoryItem(Integer id, String name, int qty) {
            this.id = id;
            this.name.set(name);
            this.qty.set(qty);
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String v) {
            name.set(v);
        }

        public int getQty() {
            return qty.get();
        }

        public void setQty(int v) {
            qty.set(v);
        }
    }
}
