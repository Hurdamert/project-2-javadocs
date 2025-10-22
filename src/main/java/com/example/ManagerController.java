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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
  * The ManagerController class allows managers to manage the inventory
  * and acts as the homepage for the other manager features
  * @author Jake Hewett
*/
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
    private TextField restockQtyField;
    @FXML
    private TextField fullQtyField;

    @FXML
    private ToggleGroup unitToggleGroup;
    @FXML
    private RadioButton gSelector;
    @FXML
    private RadioButton mlSelector;

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
    private BottomNavigationButton report; 
    @FXML
    private BottomNavigationButton salesAndRestock;

    // Ensure we only have one employee page
    private Stage employeeStage;
    private Stage reportStage;
    private Stage salesAndRestockStage;

    private Stage productStage;

    // --- DB config
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db";
    private final dbSetup my = new dbSetup();

    private final ObservableList<InventoryItem> inventory = FXCollections.observableArrayList();

    /**
    * The initialize function sets actions to each of the buttons on the manager page,
    * as well as initializes the inventory table and chart
    */
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
        report.setOnAction(e -> showReport());
        salesAndRestock.setOnAction(e -> showSalesAndRestock());
        displayMenu.setOnAction(e -> displayMenu());

        unitToggleGroup = new ToggleGroup();
        gSelector.setToggleGroup(unitToggleGroup);
        mlSelector.setToggleGroup(unitToggleGroup);
    }

    /**
     * Displays the employee data window when the corresponding button is clicked.
     */
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

    /**
     * Displays the reports window when the corresponding button is clicked.
     */
    private void showReport() {
        try {
            if(reportStage == null){
                Stage owner = (Stage) report.getScene().getWindow();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/Report.fxml"));
                javafx.scene.Parent root = loader.load();

                reportStage = new Stage();
                reportStage.setTitle("Reports");
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

    /**
     * Displays the sales and restock window when the corresponding button is clicked.
     */
    private void showSalesAndRestock() {
        try {
            if(salesAndRestockStage == null){
                Stage owner = (Stage) salesAndRestock.getScene().getWindow();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/SalesAndRestock.fxml"));
                javafx.scene.Parent root = loader.load();

                salesAndRestockStage = new Stage();
                salesAndRestockStage.setTitle("Sales And Restock");
                salesAndRestockStage.initOwner(owner);
                salesAndRestockStage.initModality(Modality.WINDOW_MODAL);
                salesAndRestockStage.setResizable(true);
                salesAndRestockStage.setScene(new Scene(root, 900, 530));
                salesAndRestockStage.show();
            }
            salesAndRestockStage.show();
            salesAndRestockStage.toFront();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // --- UI actions

    /**
     * Handles the addition of a new inventory item when the add button is clicked.
     */
    @FXML
    private void onAddInventory() {
        String name = nameField.getText().trim();
        String qtyStr = qtyField.getText().trim();
        String restockQtyStr = restockQtyField.getText().trim();
        String fullQtyStr = fullQtyField.getText().trim();
        String unit = getSelectedUnit().trim();
        if (name.isBlank() || qtyStr.isBlank() || restockQtyStr.isBlank() || fullQtyStr.isBlank() || unit.isBlank()) {
            //status("Enter name and quantity.");
            return;
        }
        int qty;
        int restockQty;
        int fullQty;
        try {
            qty = Integer.parseInt(qtyStr);
            restockQty = Integer.parseInt(restockQtyStr);
            fullQty = Integer.parseInt(fullQtyStr);
        } catch (NumberFormatException e) {
            //status("Quantity must be an integer.");
            return;
        }

        String sql = "INSERT INTO ingredients (ingredient_name, quantity, minimum_quantity, full_quantity, ingredient_unit) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, qty);
            ps.setInt(3, restockQty);
            ps.setInt(4, fullQty);
            ps.setString(5, unit);
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

    /**
     * Handles the update of an existing inventory item when the update button is clicked.
     */
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

    /**
     * Refreshes the inventory list from the database.
     */
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

    /**
     * Refreshes the supply chart to reflect current inventory levels.
     */
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

    /**
     * Clears the input form fields.
     */
    private void clearForm() {
        nameField.clear();
        qtyField.clear();
        restockQtyField.clear();
        fullQtyField.clear();
        inventoryTable.getSelectionModel().clearSelection();
    }

    /**
     * Model class for inventory items displayed in the TableView.
     */
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

    /**
     * Displays the menu window when the corresponding button is clicked.
     */
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
    
    /**
     * Gets the selected unit from the radio buttons.
     * @return null
     */
    public String getSelectedUnit() {
        Toggle selectedToggle = unitToggleGroup.getSelectedToggle();
        if (selectedToggle != null) {
            RadioButton selectedButton = (RadioButton) selectedToggle;
            return selectedButton.getText(); // "g" or "ml"
        }
        return null;
    }
}
