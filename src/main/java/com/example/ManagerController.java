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

public class ManagerController {

        @FXML private Button mainMenu;
        @FXML private Button manager;
        @FXML private Button newOrder;

        
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
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL); // The sub-window can work with parent window, we can use Modality.WINDOW_MODAL field to control the parent window that is the sub-window must go first, and the parent window would be freezed at this time.
        dialog.setResizable(false);
        dialog.setScene(new Scene(root, 360, 220));
        dialog.showAndWait(); // Block until close
    }
}
