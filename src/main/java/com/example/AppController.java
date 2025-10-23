package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.gluonhq.charm.glisten.control.BottomNavigationButton;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

/**
 * The AppController class manages the main interface of the POS system.
 * It connects the JavaFX UI elements to the PostgreSQL database
 * and handles interactions like viewing categories, products, and placing
 * orders.
 * 
 * @author Sahil Kasturi
 */
public class AppController {

    // Left Side Buttons
    @FXML
    private Button mainMenu;
    @FXML
    private Button manager;
    @FXML
    private Button newOrder;

    // Center Section
    @FXML
    private ScrollPane centerScrollPane;
    @FXML
    private AnchorPane centerAnchorPane;

    // Bottom Navigation Buttons
    @FXML
    private BottomNavigationButton checkOut;
    @FXML
    private BottomNavigationButton transactions;
    @FXML
    private BottomNavigationButton clockInOut;
    @FXML
    private BottomNavigationButton more;

    @FXML
    private ListView<String> orderList;
    private ObservableList<String> observableOrderItems = FXCollections.observableArrayList();
    @FXML
    private Text totalLabel;
    @FXML
    private Text taxLabel;
    @FXML
    private Text subTotalLabel;
    @FXML
    private Button chargeButton;

    // Database config
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db";
    private dbSetup my = new dbSetup();

    private double currentSubTotal = 0;
    private ArrayList<OrderItem> orderItems = new ArrayList<>();

    /**
     * Initializes button actions and sets up listeners for UI elements.
     */
    @FXML
    private void initialize() {
        mainMenu.setOnAction(e -> getCategories());
        manager.setOnAction(e -> managerPage());
        newOrder.setOnAction(e -> createNeworder());
        checkOut.setOnAction(e -> checkout());
        transactions.setOnAction(e -> openTransaction());
        clockInOut.setOnAction(e -> clockIn_Out());
        more.setOnAction(e -> showMore());
        chargeButton.setOnAction(e -> checkOutOrder());
        orderList.setItems(observableOrderItems);
    }

    /**
     * Loads all product categories from the database and displays them in the main
     * view.
     */
    private void getCategories() {
        System.out.println("Opening menu...");
        centerAnchorPane.getChildren().clear();

        TilePane categoryPane = new TilePane();
        categoryPane.setHgap(10);
        categoryPane.setVgap(10);
        categoryPane.setPrefColumns(3);
        categoryPane.setPrefWidth(centerScrollPane.getPrefWidth());

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM categories");

            while (rs.next()) {
                int category_id = rs.getInt("category_id");
                String category_name = rs.getString("category_name");
                VBox card = createCategoryCard(category_name, category_id);
                categoryPane.getChildren().add(card);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error with database.");
            e.printStackTrace();
        }

        centerAnchorPane.getChildren().add(categoryPane);
    }

    /**
     * Displays all products for a selected category.
     * 
     * @param category_id the ID of the selected category
     */
    private void showProducts(int category_id) {
        centerAnchorPane.getChildren().clear();
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

        VBox contentBox = new VBox(10);
        contentBox.getChildren().addAll(backButton, productPane);
        centerAnchorPane.getChildren().add(contentBox);
    }

    /**
     * Displays a product’s details, price, and available add-ons.
     * 
     * @param product_id        the product’s ID
     * @param category_id       the category the product belongs to
     * @param addon_extra_price any additional cost from add-ons
     * @param addon_string      a string of selected add-ons
     */
    private void getProduct(int product_id, int category_id, double addon_extra_price, String addon_string) {
        centerAnchorPane.getChildren().clear();
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
                double product_price = rs1.getDouble("product_price") + addon_extra_price;

                VBox productInfo = new VBox(5);
                Text productText = new Text(product_name);
                productText.setStyle("-fx-font-weight: bold; -fx-font-size: 30px;");
                Text productPrice = new Text("Current Price: $" + product_price);
                productPrice.setStyle("-fx-font-size: 20px;");

                IntegerProperty quantity = new SimpleIntegerProperty(1);
                HBox quantitySelector = createQuantitySelector(quantity);

                Button orderButton = new Button("Add to order");
                orderButton.setOnAction(e -> {
                    int product_quantity = quantity.get();
                    addItemToOrder(product_id, product_name, product_price, product_quantity, addon_string,
                            category_id);
                });

                productInfo.getChildren().addAll(productText, productPrice, quantitySelector, orderButton);
                productPane.getChildren().add(productInfo);
            }

            rs1.close();
            stmt1.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        VBox contentBox = new VBox();
        contentBox.getChildren().addAll(backButton, productPane);
        centerAnchorPane.getChildren().add(contentBox);
    }

    /**
     * Opens the manager window.
     */
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

    /**
     * Creates a new order and resets the interface.
     */
    private void createNeworder() {
        System.out.println("Creating a new order...");
        orderItems.clear();
        currentSubTotal = 0.00;
        updateTotalAndTax();
        observableOrderItems.clear();
        getCategories();
    }

    /** Handles checkout button click. */
    private void checkout() {
        System.out.println("Processing checkout...");
    }

    /** Opens the transaction history window. */
    private void openTransaction() {
        System.out.println("Opening transaction history...");
    }

    /** Logs an employee clock-in or clock-out. */
    private void clockIn_Out() {
        System.out.println("Clock in...");
    }

    /** Displays additional menu options. */
    private void showMore() {
        System.out.println("More information...");
    }

    /**
     * Finalizes the current order, updates the database, and resets the cart.
     */
    private void checkOutOrder() {
        if (!orderItems.isEmpty()) {
            int employee_id = 6;
            try {
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                conn.setAutoCommit(false);

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("INSERT INTO orders (employee_id, sub_total) VALUES (" + employee_id
                        + ", " + currentSubTotal + ") RETURNING order_id;");

                if (rs.next()) {
                    int order_id = rs.getInt("order_id");
                    for (OrderItem item : orderItems) {
                        Statement stmt4 = conn.createStatement();
                        stmt4.executeUpdate("INSERT INTO orderitems (order_id, product_id, qty, item_price) VALUES ("
                                + order_id + ", " + item.getProduct_id() + ", " + item.getItem_count() + ", "
                                + item.getItem_price() + ");");
                        stmt4.close();
                    }
                }

                conn.commit();
                rs.close();
                stmt.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        orderItems.clear();
        currentSubTotal = 0.00;
        updateTotalAndTax();
        observableOrderItems.clear();
        getCategories();
    }

    /**
     * Adds an item to the current order.
     */
    private void addItemToOrder(int product_id, String product_name, double price, int item_count, String addon_string,
            int category_id) {
        currentSubTotal += price * item_count;
        OrderItem item = new OrderItem(product_id, product_name, price, item_count);
        orderItems.add(item);
        if (addon_string.length() < 3) {
            addon_string += "  None";
        }
        String itemString = item.getProduct_name()
                + "\n    quantity: " + item_count
                + "\n    add-ons: " + addon_string.substring(2)
                + "\n    price: " + item.getItem_price();
        observableOrderItems.add(itemString);
        updateTotalAndTax();
        getProduct(product_id, category_id, 0, "");
    }

    /** Updates subtotal, tax, and total on the screen. */
    private void updateTotalAndTax() {
        totalLabel.setText(String.format("Total: $%.2f", currentSubTotal + (currentSubTotal * 0.05)));
        taxLabel.setText(String.format("Tax: $%.2f", currentSubTotal * 0.05));
        subTotalLabel.setText(String.format("Sub Total: $%.2f", currentSubTotal));
    }

    /** Creates a clickable card for a product category. */
    private VBox createCategoryCard(String category_name, int category_id) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-border-color: black; -fx-border-radius: 5; -fx-background-color: #b9b9b9ff; -fx-background-radius: 5;");
        card.setPadding(new javafx.geometry.Insets(10));
        Text text = new Text(category_name);
        text.setStyle("-fx-font-weight: bold;");
        card.setOnMouseClicked(e -> showProducts(category_id));
        card.getChildren().addAll(text);
        return card;
    }

    /** Creates a clickable card for a product. */
    private VBox createProductCard(String product_name, int product_id, int category_id) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-border-color: black; -fx-border-radius: 5; -fx-background-color: #b9b9b9ff; -fx-background-radius: 5;");
        card.setPadding(new javafx.geometry.Insets(10));
        Text text = new Text(product_name);
        text.setStyle("-fx-font-weight: bold;");
        card.setOnMouseClicked(e -> getProduct(product_id, category_id, 0, ""));
        card.getChildren().addAll(text);
        return card;
    }

    /** Creates a card for an add-on option. */
    private VBox createAddonCard(String addon_name, double addon_price, int addon_id, int product_id, int category_id,
            double current_addon_extra_price, String current_addon_string) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-border-color: black; -fx-border-radius: 5; -fx-background-color: #b9b9b9ff; -fx-background-radius: 5;");
        card.setPadding(new javafx.geometry.Insets(10));
        Text name = new Text(addon_name);
        name.setStyle("-fx-font-weight: bold;");
        Text price = new Text("Add-on Price: $" + addon_price);
        Button button = new Button("Add");
        button.setOnMouseClicked(e -> getProduct(product_id, category_id, current_addon_extra_price + addon_price,
                current_addon_string + ", " + addon_name));
        card.getChildren().addAll(name, price, button);
        return card;
    }

    /** Creates a quantity selector with plus and minus buttons. */
    private HBox createQuantitySelector(IntegerProperty quantity) {
        Button plus = new Button("+");
        Button minus = new Button("-");
        Label quantityLabel = new Label();
        quantityLabel.textProperty().bind(quantity.asString());

        plus.setOnAction(e -> quantity.set(quantity.get() + 1));
        minus.setOnAction(e -> {
            if (quantity.get() > 1)
                quantity.set(quantity.get() - 1);
        });

        HBox box = new HBox(5, minus, quantityLabel, plus);
        box.setAlignment(Pos.CENTER);
        return box;
    }
}
