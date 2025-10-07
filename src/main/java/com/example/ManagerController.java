package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.gluonhq.charm.glisten.control.BottomNavigationButton;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ManagerController {

    // --- FXML refs (from ManagerPage.fxml)
    @FXML
    private TableView<InventoryItem> inventoryTable;
    @FXML
    private TableColumn<InventoryItem, Integer> colID;
    @FXML
    private TableColumn<InventoryItem, String> colItem;
    @FXML
    private TableColumn<InventoryItem, Integer> colStock;

    @FXML
    private TextField nameField;
    @FXML
    private TextField qtyField;

    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;

    // @FXML
    // private Label statusLabel;

    @FXML
    private BarChart<String, Number> supplyChart;

    // Bottom nav
    @FXML private BottomNavigationButton displayMenu;


    @FXML
    private BottomNavigationButton employeeData;
    @FXML
    private BottomNavigationButton saleHistory; 

    // Ensure we only have one employee page
    private Stage employeeStage;

    private Stage productStage;

    // --- DB config
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db";
    private final dbSetup my = new dbSetup();

    private final ObservableList<InventoryItem> inventory = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Table column bindings
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
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

        addButton.setOnAction(e -> onAddInventory());
        updateButton.setOnAction(e -> onUpdateInventory());

        employeeData.setOnAction(e -> showEmployeesData());
        displayMenu.setOnAction(e -> displayMenu());
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

    // --- UI actions

    @FXML
    private void onAddInventory() {
        String name = nameField.getText().trim();
        String qtyStr = qtyField.getText().trim();
        if (name.isBlank() || qtyStr.isBlank()) {
            //status("Enter name and quantity.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            //status("Quantity must be an integer.");
            System.out.println();
            return;
        }

        String sql = "INSERT INTO ingredients (ingredient_name, quantity) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, qty);
            System.out.println(ps);
            ps.executeUpdate();

            //status("Added: " + name + " (" + qty + ")");
            clearForm();
            refreshInventory();
            refreshChart();
        } catch (Exception ex) {
            ex.printStackTrace();
            //status("Add failed: " + ex.getMessage());
        }
    }

    @FXML
    private void onUpdateInventory() {
        InventoryItem sel = inventoryTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            //status("Select a row to update.");
            return;
        }
        String name = nameField.getText().trim();
        System.out.println(name);
        String qtyStr = qtyField.getText().trim();
        if (name.isBlank() || qtyStr.isBlank()) {
            //status("Enter name and quantity.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            //status("Quantity must be an integer.");
            return;
        }

        // Prefer updating by a stable ID column if you have one (inventory_id). Example
        // shown:
        String sql = "UPDATE ingredients SET ingredient_name = ?, quantity = ? WHERE ingredient_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd)) {
            Integer id = sel.getId();

            if (id == null) {
                try (PreparedStatement ps = conn
                        .prepareStatement("UPDATE ingredients SET quantity = ? WHERE ingredient_name = ?")) {
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

            //status("Updated: " + name + " -> " + qty);
            clearForm();
            refreshInventory();
            refreshChart();
        } catch (Exception ex) {
            ex.printStackTrace();
            //status("Update failed: " + ex.getMessage());
        }
    }

    // --- Data loaders

    private void refreshInventory() {
        inventory.clear();

        // Adjust columns to match your schema
        String sql = "SELECT ingredient_id, ingredient_name, quantity FROM ingredients ORDER BY ingredient_id";

        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("ingredient_id");
                String name = rs.getString("ingredient_name");
                int qty = rs.getInt("quantity");
                inventory.add(new InventoryItem(id, name, qty));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //status("Load failed: " + ex.getMessage());
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

    // private void status(String msg) {
    //     statusLabel.setText(msg);
    // }

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

    private void displayMenu() {
        try {
            if(productStage == null){
                Stage owner = (Stage) displayMenu.getScene().getWindow();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/MenuData.fxml"));
                javafx.scene.Parent root = loader.load();

                productStage = new Stage();
                productStage.setTitle("Menu");
                productStage.initOwner(owner);
                productStage.initModality(Modality.WINDOW_MODAL);
                productStage.setResizable(true);
                productStage.setScene(new Scene(root, 900, 530));
                productStage.show();
            }
            productStage.show();
            productStage.toFront();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
