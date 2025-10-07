package com.example;

import javafx.application.Platform;
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




















}
