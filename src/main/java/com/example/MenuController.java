package com.example;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;


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
    @FXML private ComboBox<Category> categoryComboBox;

    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button modifyButton;

    @FXML private ComboBox<Ingredient> ingredientComboBox;
    @FXML private TextField ingredientAmountField;
    @FXML private ListView<String> productIngredientList;
    private ObservableList<String> observableproductIngredients = FXCollections.observableArrayList();
    @FXML private Button addIngredientButton;
    @FXML private Button clearIngredientButton;


    // Observe and fecth the data in real time
    private final ObservableList<ProductRow> data = FXCollections.observableArrayList();

    private ArrayList<ProductIngredient> productIngredients = new ArrayList<>();

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
        addIngredientButton.setOnAction(e -> addProductIngredient());
        clearIngredientButton.setOnAction(e -> clearProductIngredients());

        productIngredientList.setItems(observableproductIngredients);
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

    private ObservableList<ProductRow> tempProducts = FXCollections.observableArrayList(); // store the data we fetch from database by using other thread, this should transfer itself to data_Collection
    private ObservableList<Ingredient> tempIngredients = FXCollections.observableArrayList();
    private ObservableList<Category> tempCategories = FXCollections.observableArrayList();

    private void loadData(){
         // Build the connection
        runAsync(() -> {
            ObservableList<ProductRow> fetchedProducts = FXCollections.observableArrayList();
            ObservableList<Ingredient> fetchedIngredients = FXCollections.observableArrayList();
            ObservableList<Category> fetchedCategories = FXCollections.observableArrayList();

            try {

                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                
                // Create statement
                Statement stmt = conn.createStatement();
                
                // Run sql query
                String sqlStatement = "SELECT * FROM products ORDER BY product_id";
                ResultSet rs = stmt.executeQuery(sqlStatement);

                
                while (rs.next()) {
                    fetchedProducts.add(new ProductRow(rs.getInt("product_id"), rs.getString("product_name"), rs.getFloat("product_price")));
                }

                // get categories from the database
                Statement categoryStatement = conn.createStatement();
                String categorySQL = "SELECT * FROM categories ORDER BY category_id";
                ResultSet categoryRS = categoryStatement.executeQuery(categorySQL);

                while (categoryRS.next()) {
                    fetchedCategories.add(new Category(categoryRS.getInt("category_id"), categoryRS.getString("category_name")));
                }

                // get ingredients from the database
                Statement ingredientStatement = conn.createStatement();
                String ingredientSQL = "SELECT ingredient_id, ingredient_name, ingredient_unit FROM ingredients ORDER BY ingredient_id";
                ResultSet ingredientRS = ingredientStatement.executeQuery(ingredientSQL);

                while (ingredientRS.next()) {
                    fetchedIngredients.add(new Ingredient(ingredientRS.getInt("ingredient_id"), ingredientRS.getString("ingredient_name"), ingredientRS.getString("ingredient_unit")));
                }

                conn.close();

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                e.getMessage();
            }

            Platform.runLater(() -> {
                tempProducts.setAll(fetchedProducts);
                data.setAll(tempProducts);

                tempCategories.setAll(fetchedCategories);
                categoryComboBox.setItems(tempCategories);
                categoryComboBox.setPromptText("Select Category");

                tempIngredients.setAll(fetchedIngredients);
                ingredientComboBox.setItems(tempIngredients);
                ingredientComboBox.setPromptText("Select an ingredient");
            });

        }, null);
    }

    private void addData() {
        if (nameField.getText().isBlank() || priceField.getText().isBlank() || productIngredients.isEmpty()) {
            System.out.println("Not all fields have been completed.");
            return;
        }
        String name = nameField.getText().trim();
        BigDecimal price = new BigDecimal(priceField.getText().trim());
        Category category = categoryComboBox.getSelectionModel().getSelectedItem();

        runAsync(() -> {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);

            // add to products
            String sqlStatement = "INSERT INTO PRODUCTS (product_name, product_price, category_id) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, name);
            stmt.setBigDecimal(2, price);
            stmt.setInt(3, category.getId());
            stmt.executeUpdate();

            // Get the generated product_id
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                // successfully created product
                int product_id = generatedKeys.getInt(1); // first column is the generated key

                // now add each productingredient
                for (ProductIngredient ingredient : productIngredients) {
                    try {
                        String piSQL = "INSERT INTO productingredients (ingredient_id, product_id, ingredient_amount) VALUES (?, ?, ?)";
                        PreparedStatement piStatement = conn.prepareStatement(piSQL);
                        piStatement.setInt(1, ingredient.getIngredientId());
                        piStatement.setInt(2, product_id);
                        piStatement.setFloat(3, ingredient.getAmount());
                        piStatement.executeUpdate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

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
            clearProductIngredients();
            categoryComboBox.getSelectionModel().clearSelection();
            ingredientComboBox.getSelectionModel().clearSelection();
            table.getSelectionModel().clearSelection();
            ingredientAmountField.clear();
            
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
            clearProductIngredients();
            categoryComboBox.getSelectionModel().clearSelection();
            ingredientComboBox.getSelectionModel().clearSelection();
            table.getSelectionModel().clearSelection();
            ingredientAmountField.clear();

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
            clearProductIngredients();
            categoryComboBox.getSelectionModel().clearSelection();
            ingredientComboBox.getSelectionModel().clearSelection();
            table.getSelectionModel().clearSelection();
            ingredientAmountField.clear();

            loadData(); // query
        });
        
    }

    private void addProductIngredient() {
        Ingredient selected = ingredientComboBox.getSelectionModel().getSelectedItem();
        if (selected != null && !ingredientAmountField.getText().isBlank()) {
            // add to listview and to the productingredients array
            ProductIngredient pi = new ProductIngredient(selected.getId(), selected.getName(), Integer.parseInt(ingredientAmountField.getText().trim()));
            productIngredients.add(pi);
            String piString = "Name: " + pi.getIngredientName() + " | Amount: " + pi.getAmount();
            observableproductIngredients.add(piString);
        }
    }

    private void clearProductIngredients() {
        productIngredients.clear();
        observableproductIngredients.clear();
    }

}
