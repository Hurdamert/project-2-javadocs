package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.gluonhq.charm.glisten.control.BottomNavigationButton;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AppController {

    // @FXML private Button myButton;
    @FXML private Button mainMenu;
    @FXML private Button manager;
    @FXML private Button newOrder;

    @FXML private BottomNavigationButton checkOut;
    @FXML private BottomNavigationButton transactions;
    @FXML private BottomNavigationButton clockInOut;
    @FXML private BottomNavigationButton more;


    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db"; //database location


    @FXML
    private void initialize() {
        // myButton.setOnAction(e -> System.out.println("Button clicked!"));
        // Listen all button events
        mainMenu.setOnAction(e -> goMenu());
        manager.setOnAction(e -> managerPage());
        newOrder.setOnAction(e -> createNeworder());
        
        checkOut.setOnAction(e -> checkout());
        transactions.setOnAction(e -> openTransaction());
        clockInOut.setOnAction(e -> clockIn_Out());
        more.setOnAction(e -> showMore());
    }

    private void goMenu() {
        System.out.println("Opening menu...");
    }

    // This just a template window
    private void managerPage() {
        Stage owner = (Stage) manager.getScene().getWindow(); // get the parent window

        Button close = new Button("Close");
        close.setOnAction(ev -> ((Stage) close.getScene().getWindow()).close());

        VBox root = new VBox(12, new Label("Opening manager page..."), close);
        root.setStyle("-fx-padding:16;"); // we can also use CSS to link this I think
        root.setAlignment(Pos.CENTER);
        root.setSpacing(12);

        Stage dialog = new Stage();
        dialog.setTitle("Manager");
        dialog.initModality(Modality.NONE); // The sub-window can work with parent window, we can use Modality.WINDOW_MODAL field to control the parent window that is the sub-window must go first, and the parent window would be freezed at this time.
        owner.setOnCloseRequest(e -> dialog.close()); // The sub-window will close once the parent window close
        dialog.setResizable(false);
        dialog.setScene(new Scene(root, 360, 220));
        dialog.showAndWait(); // Block until close
    }

    private void createNeworder() {
        System.out.println("Creating a new order...");
    }

    private void checkout() {
        System.out.println("Processing checkout...");
    }

    private void openTransaction() {
        System.out.println("Opening transaction history...");
    }

    private void clockIn_Out() {
        System.out.println("Clock in...");
    }

    private void showMore() {
        System.out.println("More information...");
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