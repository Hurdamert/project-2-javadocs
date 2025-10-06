package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.gluonhq.charm.glisten.control.BottomNavigationButton;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AppController {

    // Left Side
    @FXML
    private Button mainMenu;
    @FXML
    private Button manager;
    @FXML
    private Button newOrder;

    // Center
    @FXML
    private ScrollPane centerScrollPane;
    @FXML
    private AnchorPane centerAnchorPane;

    // Bottom nav
    @FXML private BottomNavigationButton checkOut;
    @FXML private BottomNavigationButton transactions;
    @FXML private BottomNavigationButton clockInOut;
    @FXML private BottomNavigationButton more;

    @FXML private ListView<Products> orderList;
    @FXML private Text totalLabel;
    @FXML private Text taxLabel;

    // Right Side
    @FXML private Button chargeButton;

    // Get database location and credentials
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db";
    private dbSetup my = new dbSetup();

    private double currentSubTotal = 0;



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
        chargeButton.setOnAction(e -> checkOutOrder());
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
            ResultSet rs1 = stmt1
                    .executeQuery("SELECT product_name, product_price FROM products WHERE product_id = " + product_id);

            if (rs1.next()) {
                String product_name = rs1.getString("product_name");
                double product_price = rs1.getDouble("product_price");

                VBox productInfo = new VBox(5); // spacing 5px

                Text productText = new Text(product_name);
                productText.setStyle("-fx-font-weight: bold; -fx-font-size: 30px;");
                Text productPrice = new Text("Item Price: $" + product_price);
                productPrice.setStyle("-fx-font-size: 20px;");

                IntegerProperty quantity = new SimpleIntegerProperty(1);
                HBox quantitySelector = createQuantitySelector(quantity);
                int product_quantity = quantity.get();

                Button orderButton = new Button("Add to order");
                orderButton.setOnAction(e -> addItemToOrder(product_price));
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
        try {
            Stage owner = (Stage) manager.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/ManagerPage.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Manager");
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(true);
            dialog.setScene(new Scene(root, 900, 530));
            dialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    private void checkOutOrder() {
        System.out.println("Checked out yo!");
        currentSubTotal = 0;
        updateTotalAndTax();

        // go back to the main menu
        getCategories();
    }

    private void addItemToOrder(double priceToAddToTotal){
        System.out.println("Item added!");
        currentSubTotal += priceToAddToTotal;
        updateTotalAndTax();
    }

    private void updateTotalAndTax(){
        totalLabel.setText(String.format("Total: $%.2f", currentSubTotal));
        taxLabel.setText(String.format("Tax: $%.2f", currentSubTotal * 0.05));
    }

    // Creates a reusable category card
    private VBox createCategoryCard(String category_name, int category_id) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-border-color: black; -fx-border-radius: 5; -fx-background-color: #b9b9b9ff; -fx-background-radius: 5;");
        card.setPadding(new javafx.geometry.Insets(10));

        Text text = new Text(category_name);
        text.setStyle("-fx-font-weight: bold;");
        //javafx.scene.control.Button button = new javafx.scene.control.Button("Select");
        card.setOnMouseClicked(e -> showProducts(category_id));

        card.getChildren().addAll(text);
        return card;
    }

    // Creates a reusable product card
    private VBox createProductCard(String product_name, int product_id, int category_id) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-border-color: black; -fx-border-radius: 5; -fx-background-color: #b9b9b9ff; -fx-background-radius: 5;");
        card.setPadding(new javafx.geometry.Insets(10));

        Text text = new Text(product_name);
        text.setStyle("-fx-font-weight: bold;");
        //javafx.scene.control.Button button = new javafx.scene.control.Button("Select");
        card.setOnMouseClicked(e -> getProduct(product_id, category_id));

        card.getChildren().addAll(text);
        return card;
    }

    // Creates a reusable addon card
    private VBox createAddonCard(String addon_name, double addon_price, int addon_id) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-border-color: black; -fx-border-radius: 5; -fx-background-color: #b9b9b9ff; -fx-background-radius: 5;");
        card.setPadding(new javafx.geometry.Insets(10));

        Text name = new Text(addon_name);
        name.setStyle("-fx-font-weight: bold;");
        Text price = new Text("Add-on Price: $" + addon_price);
        javafx.scene.control.Button button = new javafx.scene.control.Button("Add");
        button.setOnMouseClicked(
            //e -> getProduct(product_id, category_id)
            e -> System.out.println("Addon item clicked")
            );

        card.getChildren().addAll(name, price, button);
        return card;
    }

    public static HBox createQuantitySelector(IntegerProperty quantity) {
        Button minusButton = new Button("-");
        Button plusButton = new Button("+");
        Label quantityLabel = new Label();
        quantityLabel.textProperty().bind(quantity.asString());

        // Set button actions
        minusButton.setOnAction(e -> {
            if (quantity.get() > 1) {
                quantity.set(quantity.get() - 1);
            }
        });

        plusButton.setOnAction(e -> {
            quantity.set(quantity.get() + 1);
        });

        HBox box = new HBox(5); // spacing between buttons and label
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(minusButton, quantityLabel, plusButton);

        return box;
    }
}