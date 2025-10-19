package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.util.Objects;

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.*;

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
    @FXML private Button makeSalesReport;

    @FXML private TextField beginHourField;
    @FXML private TextField endHourField;

    private final ObservableList<Row> data = FXCollections.observableArrayList();
    private final ObservableList<SalesRow> rows = FXCollections.observableArrayList();

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
        makeSalesReport.setOnAction(e -> salesReportShow());

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

private void salesReportShow() {
    // 1) Validate hours
    Integer beginHour = parseHour(beginHourField.getText());
    Integer endHour   = parseHour(endHourField.getText());

    if (beginHour == null || endHour == null) {
        alert("Invalid hour", "Hours must be integers between 0 and 23 (e.g., 11 and 22).");
        return;
    }
    if (endHour <= beginHour) {
        alert("Invalid range", "End hour must be greater than begin hour (e.g., 11 → 22).");
        return;
    }

    // 2) Build [start, end) window for TODAY
    LocalDate today = LocalDate.now();
    Timestamp startTs = Timestamp.valueOf(today.atTime(beginHour, 0));
    Timestamp endTs   = Timestamp.valueOf(today.atTime(endHour, 0));

    // 3) Query "sales by item" from order history
    final String sql = """
        SELECT
            p.product_id              AS id,
            p.product_name            AS name,
            COALESCE(SUM(oi.qty), 0)  AS qty_sold
        FROM orders o
        JOIN orderItems oi ON oi.order_id  = o.order_id
        JOIN products   p  ON p.product_id = oi.product_id
        WHERE o.date_time >= ? AND o.date_time < ?
        GROUP BY p.product_id, p.product_name
        ORDER BY qty_sold DESC, p.product_name
    """;

    data.clear();

    // hide irrelevant columns
    if (colMinQty != null)  colMinQty.setVisible(false);
    if (colFullQty != null) colFullQty.setVisible(false);
    if (colUnit != null)    colUnit.setVisible(false);
    if (colOrderToFull != null) colOrderToFull.setVisible(false);
    if (colQty != null)     colQty.setText("Qty Sold");

    try (Connection conn = open();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setTimestamp(1, startTs);
        ps.setTimestamp(2, endTs);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int qty = rs.getInt("qty_sold");
                // reuse Row model
                data.add(new Row(id, name, qty, 0, 0, "", 0));
            }
        }

        if (data.isEmpty()) {
            info("No results", "No items sold in the selected window.");
        }

        table.setItems(data);

    } catch (Exception ex) {
        ex.printStackTrace();
        alert("Error loading report", ex.getMessage());
    }
}


    private Integer parseHour(String s) {
        try {
            int h = Integer.parseInt(s.trim());
            return (h >= 0 && h <= 23) ? h : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void alert(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Sales Report");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void info(String header, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sales Report");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    // ====== Row model for the table ======
    public static class SalesRow {
        private final IntegerProperty id   = new SimpleIntegerProperty();
        private final StringProperty  name = new SimpleStringProperty();
        private final IntegerProperty qty  = new SimpleIntegerProperty();

        public SalesRow(int id, String name, int qty) {
            this.id.set(id);
            this.name.set(name);
            this.qty.set(qty);
        }

        public int getId() { return id.get(); }
        public String getName() { return name.get(); }
        public int getQty() { return qty.get(); }

        public IntegerProperty idProperty() { return id; }
        public StringProperty  nameProperty() { return name; }
        public IntegerProperty qtyProperty() { return qty; }
    }
}
