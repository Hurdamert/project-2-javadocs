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


public class MenuController {  
            // --- DB config
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db";
    private final dbSetup my = new dbSetup();

    @FXML TableView<ProductRow> table;
    @FXML TableColumn<ProductRow, Integer> rsId;
    @FXML TableColumn<ProductRow, String> rsName;
    @FXML TableColumn<ProductRow, Float> rsPrice;

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField categoryField;

    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button modifyButton;

    // Observe and fecth the data in real time
    private final ObservableList<ProductRow> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        rsId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        rsName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        rsPrice.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        table.setItems(data);

        table.getSelectionModel().selectedItemProperty().addListener((observ, old, newcell) -> {
            if(newcell != null){
                nameField.setText(newcell.getProductName());
                priceField.setText(String.valueOf(newcell.getProductPrice()));
            }
        });
        loadData();

        addButton.setOnAction(e -> addData());
        deleteButton.setOnAction(e -> deleteData());
        modifyButton.setOnAction(e -> modify());


    }

    // Use this to finsih the Async function
    private void runAsync(Runnable ioWork, Runnable uiAfter) {
    // Disable button, avoid to click the button for multiple times(will cause program crashed)
    addButton.setDisable(true);
    deleteButton.setDisable(true);
    modifyButton.setDisable(true);
    table.setDisable(true);
    Thread t = new Thread(() -> {
        try {
            ioWork.run();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            Platform.runLater(() -> {
                try { 
                    if (uiAfter != null) uiAfter.run(); 
                }
                finally {    
                    addButton.setDisable(false);
                    deleteButton.setDisable(false);
                    modifyButton.setDisable(false);
                    table.setDisable(false); 
                }
            });
        }
    });
    t.setDaemon(true);
    t.start();
}

    private ObservableList<ProductRow> tempHub = FXCollections.observableArrayList(); // store the data we fetch from database by using other thread, this should transfer itself to data_Collection

    private void loadData(){
         // Build the connection
        runAsync(() -> {
        try {
            ObservableList<ProductRow> temp = FXCollections.observableArrayList(); // store the data we fetch from database by using other thread, this should transfer itself to data_Collection
            temp.clear();
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Create statement
            Statement stmt = conn.createStatement();
            
            // Run sql query
            String sqlStatement = "SELECT * FROM products ORDER BY product_id";
            ResultSet rs = stmt.executeQuery(sqlStatement);

            
            while (rs.next()) {
                temp.add(new ProductRow(rs.getInt("product_id"), rs.getString("product_name"), rs.getFloat("product_price")));
            }
            conn.close();
            this.tempHub = temp;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            e.getMessage();
        }
        }, () -> {
            data.setAll(tempHub);
        });

    }

    private void addData() {
        String name = nameField.getText().trim();
        Float price = Float.parseFloat(priceField.getText().trim());

        runAsync(() -> {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            // add to category
            
            // Run sql query
            String sqlStatement = "INSERT INTO PRODUCTS (product_name, product_price, category_id) VALUES(?, ?, ?)";
            // Create statement
            PreparedStatement stmt = conn.prepareStatement(sqlStatement);
            stmt.setString(1, name);
            stmt.setFloat(2, price);
            stmt.setInt(3, Integer.parseInt(categoryField.getText().trim()));

            stmt.executeUpdate(); // add the data

            conn.close();
        }
        catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            e.getMessage();
        }
        }, () -> {

            nameField.clear();
            priceField.clear();
            categoryField.clear();
            table.getSelectionModel().clearSelection();
            
            loadData(); // query
        });
        
    }

    private void deleteData() {
        ProductRow dele = table.getSelectionModel().getSelectedItem();
        if(dele == null){
            System.out.println("You should select a row");
        }

        runAsync(() -> {
            try {
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

                // Delete product ingredients
                String sqlStatement2 = "DELETE FROM productingredients WHERE product_id=?";
                PreparedStatement stmt2 = conn.prepareStatement(sqlStatement2);
                stmt2.setInt(1, dele.getProductId());

                // Run sql query
                String sqlStatement = "DELETE FROM products WHERE product_id=?";
                // Create statement
                PreparedStatement stmt = conn.prepareStatement(sqlStatement);
                stmt.setInt(1, dele.getProductId());

                stmt2.executeUpdate(); // delete the product ingredients
                stmt.executeUpdate(); // delete the data

                conn.close();

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                e.getMessage();
            }
        }, () -> {
            nameField.clear();
            priceField.clear();
            table.getSelectionModel().clearSelection();

            loadData(); // query
        });

    }

    private void modify() {
        String name = nameField.getText().trim();
        Float price = Float.parseFloat(priceField.getText().trim());
        ProductRow updateRow = table.getSelectionModel().getSelectedItem();

        runAsync(() -> {
            try {
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                
                // Run sql query
                String sqlStatement = "UPDATE products SET product_name = ?, product_price = ? WHERE product_id = ?";
                // Create statement
                PreparedStatement stmt = conn.prepareStatement(sqlStatement);
                stmt.setString(1, name);
                stmt.setFloat(2, price);
                stmt.setInt(3, updateRow.getProductId());

                stmt.executeUpdate(); // add the data

                conn.close();
    
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                e.getMessage();
            }
        }, () -> {
            nameField.clear();
            priceField.clear();
            table.getSelectionModel().clearSelection();

            loadData(); // query
        });
        
    }

}
