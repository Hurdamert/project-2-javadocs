package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.GridPane;
import java.time.LocalDateTime;
import java.time.LocalDate;
import javafx.scene.control.DatePicker;

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
    @FXML private Button usageChart;

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
        usageChart.setOnAction(e -> usageChartShow());
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

    private Label title(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Times New Roman", FontWeight.BOLD, 16));
        return l;
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

            // Check per hour sale
            String sqlStatement3 = """
                                    SELECT EXTRACT(HOUR FROM date_time)::int AS per_hour, COUNT(*) AS orders, ROUND(SUM(sub_total),2) AS gross_sales FROM orders
                                    WHERE date_time >= CURRENT_DATE AND date_time < NOW()
                                    GROUP BY 1
                                    ORDER BY 1
                                                """;
            ResultSet rs3 = stmt.executeQuery(sqlStatement3);

            TableView<ObservableList<String>> tv3 = buildTableFromResultSet(rs3);

            // Check the best sale category
            String sqlStatement4 = """
                                    SELECT c.category_name, SUM(oi.qty) AS qty_sold, ROUND(SUM(oi.qty * COALESCE(oi.item_price, p.product_price)), 2) AS sales FROM orderitems oi
                                    JOIN orders o ON o.order_id = oi.order_id
                                    JOIN products p ON p.product_id = oi.product_id
                                    JOIN categories c ON c.category_id = p.category_id
                                    WHERE o.date_time >= CURRENT_DATE AND o.date_time < NOW()
                                    GROUP BY c.category_name
                                    ORDER BY sales DESC;
                                    """;
            ResultSet rs4 = stmt.executeQuery(sqlStatement4);

            TableView<ObservableList<String>> tv4 = buildTableFromResultSet(rs4);
            
            VBox content = new VBox(10, title("Summary(Today to Now)"), tv, title("Hourly Breakdown(Today to Now)"), tv3, title("Top 10 Items(Today to Now)"), tv2, title("Best Sale Category(Today to Now)"), tv4);
            content.setPrefSize(900, 700);

            VBox.setVgrow(tv,  Priority.ALWAYS);
            VBox.setVgrow(tv2, Priority.ALWAYS);
            VBox.setVgrow(tv3, Priority.ALWAYS);
            VBox.setVgrow(tv4, Priority.ALWAYS);

            // Auto fit the table column width
            tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tv2.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tv3.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tv4.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            
            ScrollPane sp = new ScrollPane(content);
            sp.setFitToWidth(true);
            sp.setFitToHeight(true);

            BorderPane root = new BorderPane(sp);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            String today = LocalDate.now().toString();
            stage.setTitle("X-Report  " + today);
            stage.setScene(new Scene(root, 950, 750));
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

public void zReportShow() {
    // --- Business-day window (keeps your OPEN_HOUR / CLOSE_HOUR behavior) ---
    java.time.LocalDate today = java.time.LocalDate.now();
    java.time.LocalDateTime start = today.atTime(OPEN_HOUR, 0, 0);
    java.time.LocalDateTime end   = today.atTime(CLOSE_HOUR, 0, 0);
    java.time.LocalDate businessDate = start.toLocalDate();

    // Local totals to present/log
    double grossSales = 0.0;
    double taxCollected = 0.0;
    double totalSales = 0.0;

    try (java.sql.Connection conn = java.sql.DriverManager.getConnection(DB_URL, my.user, my.pswd)) {

        // --- Idempotent DDL: durable storage + reset plumbing (safe to run each time) ---
        try (java.sql.Statement ddl = conn.createStatement()) {
            // Z report header: one row per business day
            ddl.executeUpdate("""
                CREATE TABLE IF NOT EXISTS z_reports (
                    id             BIGSERIAL PRIMARY KEY,
                    business_date  DATE NOT NULL UNIQUE,
                    generated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    gross_sales    NUMERIC(12,2) NOT NULL,
                    tax_collected  NUMERIC(12,2) NOT NULL,
                    total_sales    NUMERIC(12,2) NOT NULL
                )
            """);
            ddl.executeUpdate("""
                CREATE UNIQUE INDEX IF NOT EXISTS uq_z_reports_business_date
                ON z_reports (business_date)
            """);

            // Orders get permanently tagged with the Z that closed them (enables “reset to zero”)
            ddl.executeUpdate("""
                ALTER TABLE orders
                ADD COLUMN IF NOT EXISTS z_report_id BIGINT
                REFERENCES z_reports(id) ON DELETE SET NULL
            """);
            ddl.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_orders_zreport ON orders(z_report_id)
            """);
        }

        boolean oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            // --- Guard: only once per business day (serialize with FOR UPDATE) ---
            Long existingId = null;
            java.math.BigDecimal persistedGross = null, persistedTax = null, persistedTotal = null;

            try (java.sql.PreparedStatement ps = conn.prepareStatement("""
                SELECT id, gross_sales, tax_collected, total_sales
                FROM z_reports
                WHERE business_date = ? FOR UPDATE
            """)) {
                ps.setObject(1, businessDate);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingId      = rs.getLong("id");
                        persistedGross  = rs.getBigDecimal("gross_sales");
                        persistedTax    = rs.getBigDecimal("tax_collected");
                        persistedTotal  = rs.getBigDecimal("total_sales");
                    }
                }
            }

            if (existingId != null) {
                // Already finalized today — load persisted totals
                grossSales   = persistedGross.doubleValue();
                taxCollected = persistedTax.doubleValue();
                totalSales   = persistedTotal.doubleValue();

                conn.commit();
                conn.setAutoCommit(oldAuto);

                // === NEW: display totals in a table (like other reports) ===
                try (java.sql.PreparedStatement showPs = conn.prepareStatement("""
                        SELECT
                            ?::numeric(12,2) AS gross_sales,
                            ?::numeric(12,2) AS tax_collected,
                            ?::numeric(12,2) AS total_sales
                    """)) {
                    showPs.setDouble(1, grossSales);
                    showPs.setDouble(2, taxCollected);
                    showPs.setDouble(3, totalSales);
                    try (java.sql.ResultSet showRs = showPs.executeQuery()) {
                        TableView<ObservableList<String>> tv = buildTableFromResultSet(showRs);
                        Stage stage = new Stage();
                        stage.initModality(Modality.WINDOW_MODAL);
                        stage.setTitle("Z-Report (Finalized) — " + businessDate);
                        BorderPane root = new BorderPane(tv);
                        stage.setScene(new Scene(root, 600, 160));
                        stage.show();
                    }
                }

                return;
            }

            // --- Aggregate ONLY orders in the window that haven't been closed by any Z ---
            double aggGross = 0.0;
            double aggTax   = 0.0;
            double aggTotal = 0.0;

            try (java.sql.PreparedStatement ps = conn.prepareStatement("""
                SELECT
                    COALESCE(SUM(sub_total), 0)::numeric(12,2) AS gross_sales
                FROM orders
                WHERE date_time >= ? AND date_time < ?
                  AND z_report_id IS NULL
            """)) {
                ps.setTimestamp(1, java.sql.Timestamp.valueOf(start));
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(end));
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        aggGross = rs.getDouble("gross_sales");
                        aggTax   = aggGross * 0.05;
                        aggTotal = aggGross + aggTax;
                    }
                }
            }

            grossSales   = aggGross;
            taxCollected = aggTax;
            totalSales   = aggTotal;

            // --- Persist the Z header (UNIQUE business_date enforces once/day) ---
            long reportId;
            try (java.sql.PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO z_reports (business_date, gross_sales, tax_collected, total_sales)
                VALUES (?, ?, ?, ?)
                RETURNING id
            """)) {
                ps.setObject(1, businessDate);
                ps.setDouble(2, aggGross);
                ps.setDouble(3, aggTax);
                ps.setDouble(4, aggTotal);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    reportId = rs.getLong(1);
                }
            }

            // --- Permanent side effect: “close” all orders in the window (the reset) ---
            try (java.sql.PreparedStatement ps = conn.prepareStatement("""
                UPDATE orders
                SET z_report_id = ?
                WHERE date_time >= ? AND date_time < ?
                  AND z_report_id IS NULL
            """)) {
                ps.setLong(1, reportId);
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(start));
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(end));
                ps.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(oldAuto);

            // === NEW: display totals in a table (like other reports) ===
            try (java.sql.PreparedStatement showPs = conn.prepareStatement("""
                    SELECT
                        ?::numeric(12,2) AS gross_sales,
                        ?::numeric(12,2) AS tax_collected,
                        ?::numeric(12,2) AS total_sales
                """)) {
                showPs.setDouble(1, grossSales);
                showPs.setDouble(2, taxCollected);
                showPs.setDouble(3, totalSales);
                try (java.sql.ResultSet showRs = showPs.executeQuery()) {
                    TableView<ObservableList<String>> tv = buildTableFromResultSet(showRs);
                    Stage stage = new Stage();
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.setTitle("Z-Report — " + businessDate);
                    BorderPane root = new BorderPane(tv);
                    stage.setScene(new Scene(root, 600, 160));
                    stage.show();
                }
            }

        } catch (java.sql.SQLException e) {
            conn.rollback();
            // If another terminal won the UNIQUE(business_date) race, fetch and display the persisted totals
            String sqlState = e.getSQLState();
            if (sqlState != null && sqlState.startsWith("23")) { // integrity constraint violation
                try (java.sql.PreparedStatement ps = conn.prepareStatement("""
                    SELECT gross_sales, tax_collected, total_sales
                    FROM z_reports WHERE business_date = ?
                """)) {
                    ps.setObject(1, businessDate);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            grossSales   = rs.getBigDecimal("gross_sales").doubleValue();
                            taxCollected = rs.getBigDecimal("tax_collected").doubleValue();
                            totalSales   = rs.getBigDecimal("total_sales").doubleValue();
                        }
                    }
                }
                // Show as table (finalized)
                try (java.sql.PreparedStatement showPs = conn.prepareStatement("""
                        SELECT
                            ?::numeric(12,2) AS gross_sales,
                            ?::numeric(12,2) AS tax_collected,
                            ?::numeric(12,2) AS total_sales
                """)) {
                    showPs.setDouble(1, grossSales);
                    showPs.setDouble(2, taxCollected);
                    showPs.setDouble(3, totalSales);
                    try (java.sql.ResultSet showRs = showPs.executeQuery()) {
                        TableView<ObservableList<String>> tv = buildTableFromResultSet(showRs);
                        Stage stage = new Stage();
                        stage.initModality(Modality.WINDOW_MODAL);
                        stage.setTitle("Z-Report (Finalized) — " + businessDate);
                        BorderPane root = new BorderPane(tv);
                        stage.setScene(new Scene(root, 600, 160));
                        stage.show();
                    }
                }
            } else {
                // No external helpers; just log
                System.err.println("Failed to generate Z Report: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }

    } catch (java.sql.SQLException ex) {
        // No external helpers; just log
        System.err.println("Failed to generate Z Report (connection/setup): " + ex.getMessage());
        ex.printStackTrace(System.err);
    }
}

    private void usageChartShow() {
        Stage stage = new Stage();
        stage.setTitle("Select Date Range");

        DatePicker startPicker = new DatePicker();
        DatePicker endPicker = new DatePicker();
        Button showButton = new Button("Show Chart");

        HBox dateInputs = new HBox(10, new Label("Start:"), startPicker, new Label("End:"), endPicker, showButton);
        dateInputs.setAlignment(Pos.CENTER);
        dateInputs.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(dateInputs);

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.show();

        // When button is clicked, show chart in same window
        showButton.setOnAction(e -> {
            try {
                LocalDate startDate = startPicker.getValue();
                LocalDate endDate = endPicker.getValue();

                if (startDate == null || endDate == null) {
                    showAlert("Missing Dates", "Please select both a start and end date.");
                    return;
                }

                // Database logic same as before:
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

                String sql = """
                    SELECT 
                        i.ingredient_name,
                        i.ingredient_unit,
                        SUM(pi.ingredient_amount * oi.qty) AS total_used
                    FROM Orders o
                    JOIN OrderItems oi ON o.order_id = oi.order_id
                    JOIN Products p ON oi.product_id = p.product_id
                    JOIN ProductIngredients pi ON p.product_id = pi.product_id
                    JOIN Ingredients i ON pi.ingredient_id = i.ingredient_id
                    WHERE o.date_time BETWEEN ? AND ?
                    GROUP BY i.ingredient_name, i.ingredient_unit
                    ORDER BY total_used DESC;
                """;

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setDate(1, java.sql.Date.valueOf(startDate));
                pstmt.setDate(2, java.sql.Date.valueOf(endDate));
                ResultSet rs = pstmt.executeQuery();

                // Create chart
                CategoryAxis xAxis = new CategoryAxis();
                NumberAxis yAxis = new NumberAxis();
                BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
                XYChart.Series<String, Number> series = new XYChart.Series<>();

                while (rs.next()) {
                    String ingredient = rs.getString("ingredient_name");
                    double used = rs.getDouble("total_used");
                    series.getData().add(new XYChart.Data<>(ingredient, used));
                }

                chart.getData().clear();
                chart.getData().add(series);

                // Put chart in center of window
                root.setCenter(chart);

                conn.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Unable to load chart: " + ex.getMessage());
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
