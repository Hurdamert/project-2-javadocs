package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class AppController {

    @FXML
    private Button myButton;

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db"; //database location


    @FXML
    private void initialize() {
        myButton.setOnAction(e -> runQuery());
    }

    private void runQuery() {
        System.out.println("Query will print here.");

        try {
            // Get database creditials
            dbSetup my = new dbSetup();
 
            // Build the connection
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            // Create statement
            Statement stmt = conn.createStatement();

            // Run sql query
            String sqlStatement = "SELECT product_name FROM products";
            ResultSet rs = stmt.executeQuery(sqlStatement);

            // Output result
            String result = "";
            while (rs.next()) {
                result += rs.getString("product_name") + "\n";
            }

            // Display result
            System.out.println(result);

            // Close connection
            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("Error with database.");
            e.printStackTrace();
            System.exit(0);
        }
    }
}