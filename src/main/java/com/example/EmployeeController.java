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

public class EmployeeController {

        // --- DB config
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/gang_00_db";
    private final dbSetup my = new dbSetup();

    @FXML TableView<EmployeeRow> table;
    @FXML TableColumn<EmployeeRow, Integer> rsId;
    @FXML TableColumn<EmployeeRow, String> rsName;
    @FXML TableColumn<EmployeeRow, String> rsRole;
    @FXML TableColumn<EmployeeRow, String> rsStatus;

    @FXML private TextField nameField;
    @FXML private TextField roleField;
    @FXML private TextField statusField;

    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button modifyButton;

    // Observe and fecth the data in real time
    private final ObservableList<EmployeeRow> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        rsId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        rsName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        rsRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        rsStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        table.setItems(data);

        table.getSelectionModel().selectedItemProperty().addListener((observ, old, newcell) -> {
            if(newcell != null){
                nameField.setText(newcell.getEmployeeName());
                roleField.setText(newcell.getRole());
                statusField.setText(newcell.getStatus());
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

    private ObservableList<EmployeeRow> tempHub = FXCollections.observableArrayList(); // store the data we fetch from database by using other thread, this should transfer itself to data_Collection

    private void loadData(){
         // Build the connection
        runAsync(() -> {
        try {
            ObservableList<EmployeeRow> temp = FXCollections.observableArrayList(); // store the data we fetch from database by using other thread, this should transfer itself to data_Collection
            temp.clear();
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Create statement
            Statement stmt = conn.createStatement();
            
            // Run sql query
            String sqlStatement = "SELECT * FROM employees ORDER BY employee_id";
            ResultSet rs = stmt.executeQuery(sqlStatement);

            
            while (rs.next()) {
                temp.add(new EmployeeRow(rs.getInt("employee_id"), rs.getString("employee_name"), rs.getString("role"), rs.getString("status")));
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
        String role = roleField.getText().trim();
        String status = statusField.getText().trim();

        runAsync(() -> {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            
            // Run sql query
            String sqlStatement = "INSERT INTO EMPLOYEES (employee_name, role, status) VALUES(?, ?, ?)";
            // Create statement
            PreparedStatement stmt = conn.prepareStatement(sqlStatement);
            stmt.setString(1, name);
            stmt.setString(2, role);
            stmt.setString(3, status);

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
            roleField.clear();
            statusField.clear();
            table.getSelectionModel().clearSelection();
            
            loadData(); // query
        });
        
    }

    private void deleteData() {
        EmployeeRow dele = table.getSelectionModel().getSelectedItem();
        if(dele == null){
            System.out.println("You should select a row");
        }

        runAsync(() -> {
            try {
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                
                // Run sql query
                String sqlStatement = "DELETE FROM EMPLOYEES WHERE employee_id=?";
                // Create statement
                PreparedStatement stmt = conn.prepareStatement(sqlStatement);
                stmt.setInt(1, dele.getEmployeeId());

                stmt.executeUpdate(); // delete the data

                conn.close();

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                e.getMessage();
            }
        }, () -> {
            nameField.clear();
            roleField.clear();
            statusField.clear();
            table.getSelectionModel().clearSelection();

            loadData(); // query
        });

    }

    private void modify() {
        String name = nameField.getText().trim();
        String role = roleField.getText().trim();
        String status = statusField.getText().trim();
        EmployeeRow updateRow = table.getSelectionModel().getSelectedItem();

        runAsync(() -> {
            try {
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                
                // Run sql query
                String sqlStatement = "UPDATE EMPLOYEES SET employee_name = ?, \"role\" = ?, status = ? WHERE employee_id = ?";
                // Create statement
                PreparedStatement stmt = conn.prepareStatement(sqlStatement);
                stmt.setString(1, name);
                stmt.setString(2, role);
                stmt.setString(3, status);
                stmt.setInt(4,updateRow.getEmployeeId());
    
                stmt.executeUpdate(); // add the data

                conn.close();
    
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                e.getMessage();
            }
        }, () -> {
            nameField.clear();
            roleField.clear();
            statusField.clear();
            table.getSelectionModel().clearSelection();

            loadData(); // query
        });
        
    }

}
