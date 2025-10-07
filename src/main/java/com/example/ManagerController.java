package com.example;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;

import com.gluonhq.charm.glisten.control.BottomNavigationButton;

public class ManagerController {

    private static final String TBL_INVENTORY = "ingredients";
    private static final String COL_ID = "ingredient_id";
    private static final String COL_NAME = "ingredient_name";
    private static final String COL_QTY = "quantity";
    // -------------------------------------------------------------------------

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

    @FXML
    private BottomNavigationButton employeeData;
    @FXML
    private BottomNavigationButton report; 

    // Ensure we only have one employee page
    private Stage employeeStage;
    private Stage reportStage;

    // --- DB config
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
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel != null) {
                nameField.setText(sel.getName());
                qtyField.setText(String.valueOf(sel.getQty()));
            }
        });

        refreshInventory();
        refreshChart();

        employeeData.setOnAction(e -> showEmployeesData());
        report.setOnAction(e -> showReport());
    }

    @FXML
    private void showEmployeesData() {
        try {
            if(employeeStage == null){
                Stage owner = (Stage) employeeData.getScene().getWindow();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/Employee_data.fxml"));
                javafx.scene.Parent root = loader.load();

                employeeStage = new Stage();
                employeeStage.setTitle("Employee");
                employeeStage.initOwner(owner);
                employeeStage.initModality(Modality.WINDOW_MODAL);
                employeeStage.setResizable(true);
                employeeStage.setScene(new Scene(root, 900, 530));
                employeeStage.show();
            }
            employeeStage.show();
            employeeStage.toFront();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showReport() {
        try {
            if(reportStage == null){
                Stage owner = (Stage) report.getScene().getWindow();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/Report.fxml"));
                javafx.scene.Parent root = loader.load();

                reportStage = new Stage();
                reportStage.setTitle("Report");
                reportStage.initOwner(owner);
                reportStage.initModality(Modality.WINDOW_MODAL);
                reportStage.setResizable(true);
                reportStage.setScene(new Scene(root, 900, 530));
                reportStage.show();
            }
            reportStage.show();
            reportStage.toFront();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

        String sqlAdd = String.format(
                "INSERT INTO %s (%s, %s) VALUES (?, ?)",
                TBL_INVENTORY, COL_NAME, COL_QTY);

        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                PreparedStatement ps = conn.prepareStatement(sqlAdd)) {
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

        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd)) {

            if (COL_ID != null && sel.getId() != null) {
                // Update by ID (preferred)
                String sqlUpdateById = String.format(
                        "UPDATE %s SET %s = ?, %s = ? WHERE %s = ?",
                        TBL_INVENTORY, COL_NAME, COL_QTY, COL_ID);
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateById)) {
                    ps.setString(1, name);
                    ps.setInt(2, qty);
                    ps.setInt(3, sel.getId());
                    ps.executeUpdate();
                }
            } else {
                // Fallback: update by name
                String sqlUpdateByName = String.format(
                        "UPDATE %s SET %s = ? WHERE %s = ?",
                        TBL_INVENTORY, COL_QTY, COL_NAME);
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateByName)) {
                    ps.setInt(1, qty);
                    ps.setString(2, sel.getName());
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

        // Build SELECT with or without ID
        final String selectCols = (COL_ID != null)
                ? String.format("%s, %s, %s", COL_ID, COL_NAME, COL_QTY)
                : String.format("%s, %s", COL_NAME, COL_QTY);

        String sqlLoad = String.format(
                "SELECT %s FROM %s ORDER BY %s",
                selectCols, TBL_INVENTORY, COL_NAME);

        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sqlLoad)) {

            while (rs.next()) {
                Integer id = null;
                int colShift = 0;
                if (COL_ID != null) {
                    id = (Integer) rs.getObject(1);
                    colShift = 1;
                }
                String name = rs.getString(1 + colShift);
                int qty = rs.getInt(2 + colShift);

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
