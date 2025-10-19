package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import java.time.LocalDateTime;
import java.time.LocalDate;






public class ReportController {

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db";
    private final dbSetup my = new dbSetup();

    @FXML private Button weeklySaleHistory;
    @FXML private Button realisticSaleHistory;
    @FXML private Button peakSaleHistory;
    @FXML private Button menuItemUse;
    @FXML private Button BofW;
    @FXML private Button top10Product;
    @FXML private Button recentOrders;
    @FXML private Button daily;
    @FXML private Button xReport;
    @FXML private Button zReport;

    private static final int OPEN_HOUR  = 11;
    private static final int CLOSE_HOUR = 22;
    
    @FXML
    private void initialize() {
        weeklySaleHistory.setOnAction(e -> weeklySaleHistoryShow());
        realisticSaleHistory.setOnAction(e -> realisticSaleHistoryShow());
        peakSaleHistory.setOnAction(e -> peakSaleHistoryShow());
        menuItemUse.setOnAction(e -> menuItemUseShow());
        BofW.setOnAction(e -> BestofWorstShow());
        top10Product.setOnAction(e -> top10ProductShow());
        recentOrders.setOnAction(e -> recentOrdersShow());
        daily.setOnAction(e -> dailyOrderNRevenuesShow());
        xReport.setOnAction(e -> checkXReport());
        zReport.setOnAction(e -> zReportShow());

    }

    // help function -- detect the column for each query dynamically
    private TableView<ObservableList<String>> buildTableFromResultSet(ResultSet rs){
        TableView<ObservableList<String>> table = new TableView<>();
        try{
            ResultSetMetaData md = rs.getMetaData();
            int colCount = md.getColumnCount();
        
            // build the table header dynamically
            for (int i = 1; i <= colCount; i++) {
                final int colIndex = i - 1;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(md.getColumnLabel(i));
                col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(colIndex)));
                table.getColumns().add(col);
            }
        
            ObservableList<ObservableList<String>> items = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= colCount; i++) {
                    Object v = rs.getObject(i);
                    row.add(v == null ? "" : v.toString());
                }
                items.add(row);
            }
            table.setItems(items);
        } catch(Exception e){
            e.getStackTrace();
            e.getMessage();
        }
        return table;
    }


    private void weeklySaleHistoryShow() {
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Create statement
            Statement stmt = conn.createStatement();
            
            // Run sql query
            String sqlStatement = """
                                    SELECT date_part('isoyear', date_time)::int AS Years, date_part('week', date_time)::int AS Weeks, COUNT(*) AS orders FROM orders
                                    GROUP BY Years, Weeks
                                    ORDER BY Years, Weeks
                                                        """;
            ResultSet rs = stmt.executeQuery(sqlStatement);

            TableView<ObservableList<String>> tv = buildTableFromResultSet(rs);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Weekly Sale History");
            BorderPane root = new BorderPane(tv);
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

            conn.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            e.getMessage();
        }
    }

    private void checkXReport() {
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Create statement
            Statement stmt = conn.createStatement();

            // ** The query result might be null if there is 0 record for today in the database **
            
            // Check the sub_total statred at open time and until now.
            String sqlStatement1 = """
                                    SELECT COUNT(*) AS orders, COALESCE(SUM(sub_total), 0)::NUMERIC(12,2) AS gross_sales,
                                    (CASE WHEN COUNT(*) = 0 THEN 0 
                                        ELSE ROUND(SUM(sub_total)/COUNT(*), 2)
                                        END) AS avg_ticket FROM orders
                                    WHERE date_time >= CURRENT_DATE AND date_time <  NOW();
                                                                                            """;
            ResultSet rs1 = stmt.executeQuery(sqlStatement1);

            TableView<ObservableList<String>> tv = buildTableFromResultSet(rs1);
            
            // Check today's top 10 product
            String sqlStatement2 = """
                                    SELECT p.product_id, p.product_name, SUM(oi.qty) AS qty_sold FROM orderitems oi JOIN orders o ON o.order_id = oi.order_id JOIN products p ON p.product_id = oi.product_id
                                    WHERE o.date_time >= CURRENT_DATE AND o.date_time < NOW()
                                    GROUP BY p.product_id, p.product_name
                                    ORDER BY qty_sold DESC, p.product_name
                                                                            """;
            ResultSet rs2 = stmt.executeQuery(sqlStatement2);

            TableView<ObservableList<String>> tv2 = buildTableFromResultSet(rs2);

            // Check today's top 10 product
            String sqlStatement3 = """
                                    SELECT EXTRACT(HOUR FROM date_time)::int AS per_hour, COUNT(*) AS orders, ROUND(SUM(sub_total),2) AS gross_sales FROM orders
                                    WHERE date_time >= CURRENT_DATE AND date_time < NOW()
                                    GROUP BY 1
                                    ORDER BY 1
                                                """;
            ResultSet rs3 = stmt.executeQuery(sqlStatement3);

            TableView<ObservableList<String>> tv3 = buildTableFromResultSet(rs3);
            
            VBox content = new VBox(10, new Label("Summary (Today to Now)"), tv, new Label("Hourly Breakdown"), tv3, new Label("Top 10 Items"), tv2);
            content.setPrefSize(900, 700);


            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("X-Report");
            stage.setScene(new Scene(new BorderPane(new ScrollPane(content)), 950, 750));
            stage.show();

            conn.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            e.getMessage();
        }

    }

    private void realisticSaleHistoryShow() {
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Create statement
            Statement stmt = conn.createStatement();
            
            // Run sql query
            String sqlStatement = """
                                    SELECT EXTRACT(HOUR FROM date_time) AS hour_of_day, COUNT(order_id) AS total_orders, SUM(sub_total) AS total_sales FROM Orders
                                    GROUP BY hour_of_day
                                    ORDER BY hour_of_day;
                                                        """;
            ResultSet rs = stmt.executeQuery(sqlStatement);

            TableView<ObservableList<String>> tv = buildTableFromResultSet(rs);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Realistic Sale History");
            BorderPane root = new BorderPane(tv);
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

            conn.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            e.getMessage();
        }
    }

    private void peakSaleHistoryShow() {
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Create statement
            Statement stmt = conn.createStatement();
            
            // Run sql query
            String sqlStatement = """
                                    SELECT DATE(date_time) AS order_day, SUM(sub_total) AS total_sales FROM Orders
                                    GROUP BY order_day
                                    ORDER BY total_sales DESC
                                    LIMIT 10;
                                                        """;
            ResultSet rs = stmt.executeQuery(sqlStatement);

            TableView<ObservableList<String>> tv = buildTableFromResultSet(rs);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Peak Sale History");
            BorderPane root = new BorderPane(tv);
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

            conn.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            e.getMessage();
        }
    }

    private void menuItemUseShow() {
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Create statement
            Statement stmt = conn.createStatement();
            
            // Run sql query
            String sqlStatement = """
                                    SELECT p.product_name, COUNT(pi.ingredient_id) AS ingredient_count FROM products p
                                    JOIN productingredients pi ON pi.product_id = p.product_id
                                    GROUP BY p.product_name
                                    ORDER BY ingredient_count DESC, p.product_name;
                                                        """;
            ResultSet rs = stmt.executeQuery(sqlStatement);

            TableView<ObservableList<String>> tv = buildTableFromResultSet(rs);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Ingredients that Menu Item Use");
            BorderPane root = new BorderPane(tv);
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

            conn.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            e.getMessage();
        }
    }

    private void BestofWorstShow() {
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Create statement
            Statement stmt = conn.createStatement();
            
            // Run sql query
            String sqlStatement = """
                                    SELECT worst_day.order_day, p.product_name, worst_day.total_sales, SUM(oi.qty) AS total_qty FROM OrderItems oi JOIN Orders o ON oi.order_id = o.order_id JOIN Products p ON oi.product_id = p.product_id
                                    JOIN (
                                        SELECT DATE(date_time) AS order_day, SUM(sub_total) AS total_sales
                                        FROM Orders
                                        GROUP BY DATE(date_time)
                                        ORDER BY total_sales ASC
                                        LIMIT 1
                                    ) worst_day
                                    ON DATE(o.date_time) = worst_day.order_day
                                    GROUP BY worst_day.order_day, p.product_name, worst_day.total_sales
                                    ORDER BY total_qty DESC
                                    LIMIT 1;
                                                        """;
            ResultSet rs = stmt.executeQuery(sqlStatement);

            TableView<ObservableList<String>> tv = buildTableFromResultSet(rs);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Best of Worst day");
            BorderPane root = new BorderPane(tv);
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

            conn.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            e.getMessage();
        }
    }

    private void top10ProductShow() {
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Create statement
            Statement stmt = conn.createStatement();
            
            // Run sql query
            String sqlStatement = """
                                    SELECT DATE(date_time) AS order_day, SUM(sub_total) AS total_sales FROM Orders
                                    GROUP BY order_day
                                    ORDER BY total_sales DESC
                                    LIMIT 10;
                                                        """;
            ResultSet rs = stmt.executeQuery(sqlStatement);

            TableView<ObservableList<String>> tv = buildTableFromResultSet(rs);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Top10 Product");
            BorderPane root = new BorderPane(tv);
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

            conn.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            e.getMessage();
        }
    }

    private void recentOrdersShow() {
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Create statement
            Statement stmt = conn.createStatement();
            
            // Run sql query
            String sqlStatement = """
                                    SELECT order_id, sub_total, date_time FROM orders
                                    ORDER BY date_time DESC
                                    LIMIT 20;
                                                        """;
            ResultSet rs = stmt.executeQuery(sqlStatement);

            TableView<ObservableList<String>> tv = buildTableFromResultSet(rs);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Recent Orders In 30Days");
            BorderPane root = new BorderPane(tv);
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

            conn.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            e.getMessage();
        }
    }

    private void dailyOrderNRevenuesShow() {
        try{
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Create statement
            Statement stmt = conn.createStatement();
            
            // Run sql query
            String sqlStatement = """
                                    SELECT DATE(date_time) AS day, COUNT(*) AS orders, ROUND(SUM(sub_total),2) AS revenue FROM orders
                                    WHERE date_time >= NOW() - INTERVAL '31 days'
                                    GROUP BY day
                                    ORDER BY day;
                                                        """;
            ResultSet rs = stmt.executeQuery(sqlStatement);

            TableView<ObservableList<String>> tv = buildTableFromResultSet(rs);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("DailyOrders&Revenues");
            BorderPane root = new BorderPane(tv);
            stage.setScene(new Scene(root, 900, 600));
            stage.show();

            conn.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            e.getMessage();
        }
    }

    // Signature class for employee signatures
    public static class SignatureRow {
    private final IntegerProperty employeeId = new SimpleIntegerProperty();
    private final StringProperty  employeeName = new SimpleStringProperty();
    private final StringProperty  signature = new SimpleStringProperty("");
    private final BooleanProperty signed = new SimpleBooleanProperty(false);

    public SignatureRow(int id, String name) {
        employeeId.set(id);
        employeeName.set(name);
    }
    public int getEmployeeId() { 
        return employeeId.get(); 
        }
    public String getEmployeeName() { 
        return employeeName.get(); 
        }
    public StringProperty signatureProperty() { 
        return signature; 
        }
    public BooleanProperty signedProperty() {
            return signed; 
            }
    public String getSignature() {
            return signature.get(); 
            }
    public boolean isSigned() { 
        return signed.get();
            }
    }

    //help convert double to String
    private static String currency(double x) {
    if (x == 0.0){
        return "$0.00";
    } 
    return java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US).format(x);
    }

    private void zReportShow() {
        //Compute the business day window
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atTime(OPEN_HOUR, 0, 0);
        LocalDateTime end   = today.atTime(CLOSE_HOUR, 0, 0);

        Stage stage = new Stage();

        //Query: sales + tax for the window
        double grossSales   = 0.0;
        double taxCollected = 0.0; 
        double totalSales   = 0.0;

        //Employees for signatures
        ObservableList<SignatureRow> sigRows = FXCollections.observableArrayList();

        // Connect to database
        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd)) {

            // Sales & tax
            String aggSql = """
                SELECT
                    COALESCE(SUM(sub_total), 0) AS gross_sales
                FROM orders
                WHERE date_time >= ? AND date_time < ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(aggSql)) {
                ps.setTimestamp(1, Timestamp.valueOf(start));
                ps.setTimestamp(2, Timestamp.valueOf(end));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        grossSales   = rs.getDouble("gross_sales");
                        taxCollected = grossSales * 0.05;
                        totalSales   = grossSales + taxCollected;
                    }
                }
            }

            // Employees (adjust filter to match your schema; this uses "active = true")
            String empSql = """
                SELECT employee_id,
                    COALESCE(employee_name,'') AS name
                FROM employees
                WHERE COALESCE(status, 'Active') = 'Active'
                ORDER BY name
            """;
            try (PreparedStatement ps = conn.prepareStatement(empSql);
                ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sigRows.add(new SignatureRow(
                        rs.getInt("employee_id"),
                        rs.getString("name").trim()
                    ));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load Z-Report data.").showAndWait();
            return;
        }

        //Build the UI
        Label title = new Label("Z-Report");
        title.getStyleClass().add("h2");

        Label range = new Label(String.format(
            "Business day: %s %02d:00 — %02d:00",
            today, OPEN_HOUR, CLOSE_HOUR
        ));
        range.getStyleClass().add("subtle");

        // Sales & Tax box
        GridPane salesGrid = new GridPane();
        salesGrid.setHgap(16);
        salesGrid.setVgap(8);

        Label grossLbl = new Label("Gross Sales (pre-tax):");
        Label taxLbl = new Label("Tax Collected:");
        Label totalLbl = new Label("Total Sales (with tax):");

        Label grossVal = new Label(currency(grossSales));
        Label taxVal = new Label(currency(taxCollected));
        Label totalVal = new Label(currency(totalSales));

        salesGrid.addRow(0, grossLbl, grossVal);
        salesGrid.addRow(1, taxLbl,   taxVal);
        salesGrid.addRow(2, totalLbl, totalVal);

        TitledPane salesPane = new TitledPane("Sales & Tax Information", salesGrid);
        salesPane.setExpanded(true);

        // Employee Signatures table
        TableView<SignatureRow> sigTable = new TableView<>(sigRows);
        sigTable.setEditable(true);

        TableColumn<SignatureRow, String> empCol = new TableColumn<>("Employee");
        empCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEmployeeName()));
        empCol.setPrefWidth(260);

        TableColumn<SignatureRow, String> sigCol = new TableColumn<>("Signature");
        sigCol.setCellValueFactory(cd -> cd.getValue().signatureProperty());
        sigCol.setCellFactory(TextFieldTableCell.forTableColumn());
        sigCol.setPrefWidth(220);

        TableColumn<SignatureRow, Boolean> signedCol = new TableColumn<>("Signed");
        signedCol.setCellValueFactory(cd -> cd.getValue().signedProperty());
        signedCol.setCellFactory(tc -> {
            CheckBoxTableCell<SignatureRow, Boolean> cell = new CheckBoxTableCell<>();
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        signedCol.setPrefWidth(90);

        sigTable.getColumns().setAll(empCol, sigCol, signedCol);

        TitledPane sigPane = new TitledPane("Employee Signatures", sigTable);
        sigPane.setExpanded(true);

        VBox rootBox = new VBox(12, title, range, salesPane, sigPane);
        rootBox.setPadding(new Insets(18));

        stage = new Stage();
        stage.setTitle("Z-Report");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(true);
        
        stage.setScene(new Scene(new BorderPane(rootBox), 720, 560));
        stage.show();
    }
}
