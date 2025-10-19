package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.util.Objects;

public class SalesAndRestockController {

    // DB wiring 
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db";
    private final dbSetup my = new dbSetup();

    // Table + columns 
    @FXML private TableView<Row> table;
    @FXML private TableColumn<Row, Number> colId;
    @FXML private TableColumn<Row, String> colName;
    @FXML private TableColumn<Row, Number> colQty;
    @FXML private TableColumn<Row, Number> colMinQty;
    @FXML private TableColumn<Row, Number> colFullQty;
    @FXML private TableColumn<Row, String> colUnit;
    @FXML private TableColumn<Row, Number> colOrderToFull;

    // Buttons 
    @FXML private Button btnRefresh;
    @FXML private Button btnRestockOne;
    @FXML private Button btnRestockAll;

    private final ObservableList<Row> data = FXCollections.observableArrayList();

    // Simple row model for the table
    public static class Row {
        public final int id;
        public final String name;
        public final int qty;
        public final int minQty;
        public final int fullQty;
        public final String unit;
        public final int orderToFull;
        public Row(int id, String name, int qty, int minQty, int fullQty, String unit, int orderToFull) {
            this.id = id; this.name = name; this.qty = qty; this.minQty = minQty; this.fullQty = fullQty;
            this.unit = unit; this.orderToFull = orderToFull;
        }
    }

    @FXML
    private void initialize() {
        // Table column bindings
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().id));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().name));
        colQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().qty));
        colMinQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().minQty));
        colFullQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().fullQty));
        colUnit.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().unit));
        colOrderToFull.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().orderToFull));

        table.setItems(data);

        // Button actions
        btnRefresh.setOnAction(e -> refresh());
        btnRestockOne.setOnAction(e -> restockSelected());
        btnRestockAll.setOnAction(e -> restockAll());

        // Initial load
        refresh();
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(DB_URL, my.user, my.pswd);
    }

    // Load items needing restock
    private void refresh() {
        data.clear();
        final String sql = """
            SELECT ingredient_id, ingredient_name, quantity, minimum_quantity, full_quantity, ingredient_unit,
                   (full_quantity - quantity) AS order_to_full
            FROM ingredients
            WHERE quantity < minimum_quantity
            ORDER BY (quantity - minimum_quantity) ASC
        """;
        try (Connection conn = open();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                data.add(new Row(
                        rs.getInt("ingredient_id"),
                        rs.getString("ingredient_name"),
                        rs.getInt("quantity"),
                        rs.getInt("minimum_quantity"),
                        rs.getInt("full_quantity"),
                        rs.getString("ingredient_unit"),
                        rs.getInt("order_to_full")
                ));
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    // Restock selected item to FULL
    private void restockSelected() {
        Row r = table.getSelectionModel().getSelectedItem();
        if (r == null) return;

        final String sql = "UPDATE ingredients SET quantity = full_quantity WHERE ingredient_id = ?";
        try (Connection conn = open();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.id);
            ps.executeUpdate();
            refresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    // Restock ALL items below minimum to FULL
    private void restockAll() {
        final String sql = "UPDATE ingredients SET quantity = full_quantity WHERE quantity < minimum_quantity";
        try (Connection conn = open();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            refresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, Objects.toString(ex.getMessage(), "Error"), ButtonType.CLOSE);
        alert.setHeaderText("Database Error");
        alert.showAndWait();
        ex.printStackTrace();
    }
}
