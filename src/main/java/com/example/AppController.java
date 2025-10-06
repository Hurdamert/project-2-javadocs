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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class AppController {

    // Left Side
    @FXML private Button mainMenu;
    @FXML private Button manager;
    @FXML private Button newOrder;

    // Center
    @FXML private ScrollPane centerScrollPane;
    @FXML private AnchorPane centerAnchorPane;

    // Bottom nav
    @FXML private BottomNavigationButton checkOut;
    @FXML private BottomNavigationButton transactions;
    @FXML private BottomNavigationButton clockInOut;
    @FXML private BottomNavigationButton more;

    @FXML private ListView<Products> orderList;
    @FXML private Label totalLabel;

    // Get database location and credentials
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db";
    private dbSetup my = new dbSetup();


    @FXML
    private void initialize() {
        // Listen all button events
        mainMenu.setOnAction(e -> getCategories());
        manager.setOnAction(e -> managerPage());
        newOrder.setOnAction(e -> createNeworder());
        
        checkOut.setOnAction(e -> checkout());
        transactions.setOnAction(e -> openTransaction());
        clockInOut.setOnAction(e -> clockIn_Out());
        more.setOnAction(e -> showMore());
    }

    private void getCategories() {
        System.out.println("Opening menu...");

        centerAnchorPane.getChildren().clear();

        TilePane categoryPane = new TilePane();
        categoryPane.setHgap(10);
        categoryPane.setVgap(10);
        categoryPane.setPrefColumns(3);
        categoryPane.setPrefWidth(centerScrollPane.getPrefWidth());

        try {
            // Build the connection
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            // Create statement
            Statement stmt = conn.createStatement();

            // Run sql query
            String sqlStatement = "SELECT * FROM categories";
            ResultSet rs = stmt.executeQuery(sqlStatement);

            // output result
            while (rs.next()) {
                int category_id = rs.getInt("category_id");
                String category_name = rs.getString("category_name");

                VBox card = createCategoryCard(category_name, category_id);
                categoryPane.getChildren().add(card);
            }

            // Close connection
            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("Error with database.");
            e.printStackTrace();
            System.exit(0);
        }

        centerAnchorPane.getChildren().add(categoryPane);
    }

    private void showProducts(int category_id) {
        centerAnchorPane.getChildren().clear();

        // Create Back button
        Button backButton = new Button("Back to Categories");
        backButton.setOnAction(e -> getCategories());

        TilePane productPane = new TilePane();
        productPane.setHgap(10);
        productPane.setVgap(10);
        productPane.setPrefColumns(3);
        productPane.setPrefWidth(centerScrollPane.getPrefWidth());

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM products WHERE category_id = " + category_id);

            while (rs.next()) {
                String product_name = rs.getString("product_name");
                int product_id = rs.getInt("product_id");
                VBox card = createProductCard(product_name, product_id, category_id);
                productPane.getChildren().add(card);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Combine back button and products into a VBox
        VBox contentBox = new VBox(10);
        contentBox.getChildren().addAll(backButton, productPane);

        centerAnchorPane.getChildren().add(contentBox);
    }

    private void getProduct(int product_id, int category_id) {
        centerAnchorPane.getChildren().clear();

        // Create Back button
        Button backButton = new Button("Back to Products");
        backButton.setOnAction(e -> showProducts(category_id));

        TilePane productPane = new TilePane();
        productPane.setHgap(10);
        productPane.setVgap(10);
        productPane.setPrefColumns(3);
        productPane.setPrefWidth(centerScrollPane.getPrefWidth());

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery("SELECT product_name, product_price FROM products WHERE product_id = " + product_id);

            if (rs1.next()) {
                String product_name = rs1.getString("product_name");
                double product_price = rs1.getDouble("product_price");

                VBox productInfo = new VBox(5); // spacing 5px

                Text productText = new Text(product_name);
                productText.setStyle("-fx-font-weight: bold; -fx-font-size: 30px;");
                Text productPrice = new Text("Current Price: $" + product_price);
                productPrice.setStyle("-fx-font-size: 20px;");
                Button orderButton = new Button("Add to order");
                productInfo.getChildren().addAll(productText, productPrice, orderButton);
                productPane.getChildren().add(productInfo);
                productPane.setStyle("-fx-padding: 20px 10 0 0;");
            }

            Statement stmt2 = conn.createStatement();
            ResultSet rs2 = stmt2.executeQuery("SELECT * FROM addons");
            while (rs2.next()) {
                String addon_name = rs2.getString("addon_name");
                double addon_price = rs2.getDouble("addon_price");
                int addon_id = rs2.getInt("addon_id");
                VBox card = createAddonCard(addon_name, addon_price, addon_id);
                productPane.getChildren().add(card);
            }

            rs1.close();
            stmt1.close();
            rs2.close();
            stmt2.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Combine back button and products into a VBox
        VBox contentBox = new VBox();
        contentBox.getChildren().addAll(backButton, productPane);

        centerAnchorPane.getChildren().add(contentBox);
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

        getCategories();
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

    // Creates a reusable category card
    private VBox createCategoryCard(String category_name, int category_id) {
        VBox card = new VBox(5);
        card.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-background-color: #b9b9b9ff; -fx-background-radius: 5;");
        card.setPadding(new javafx.geometry.Insets(10));

        Text text = new Text(category_name);
        text.setStyle("-fx-font-weight: bold;");
        javafx.scene.control.Button button = new javafx.scene.control.Button("Select");
        button.setOnAction(e -> showProducts(category_id));

        card.getChildren().addAll(text, button);
        return card;
    }

    // Creates a reusable product card
    private VBox createProductCard(String product_name, int product_id, int category_id) {
        VBox card = new VBox(5);
        card.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-background-color: #b9b9b9ff; -fx-background-radius: 5;");
        card.setPadding(new javafx.geometry.Insets(10));

        Text text = new Text(product_name);
        text.setStyle("-fx-font-weight: bold;");
        javafx.scene.control.Button button = new javafx.scene.control.Button("Select");
        button.setOnAction(e -> getProduct(product_id, category_id));

        card.getChildren().addAll(text, button);
        return card;
    }

    // Creates a reusable addon card
    private VBox createAddonCard(String addon_name, double addon_price, int addon_id) {
        VBox card = new VBox(5);
        card.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-background-color: #b9b9b9ff; -fx-background-radius: 5;");
        card.setPadding(new javafx.geometry.Insets(10));

        Text name = new Text(addon_name);
        name.setStyle("-fx-font-weight: bold;");
        Text price = new Text("Add-on Price: $" + addon_price);
        javafx.scene.control.Button button = new javafx.scene.control.Button("Add");
        //button.setOnAction(e -> getProduct(product_id, category_id));

        card.getChildren().addAll(name, price, button);
        return card;
    }
}