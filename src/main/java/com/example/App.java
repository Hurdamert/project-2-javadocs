package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The App class launches the JavaFX application for the POS system.
 * It loads the main FXML layout and sets up the primary stage window.
 * 
 * @author Sahil Kasturi
 */
public class App extends Application {

    /**
     * This method starts the JavaFX application.
     * It loads the main view (MainView.fxml) and displays it on screen.
     * 
     * @param stage the main window of the application
     * @throws Exception if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        stage.setTitle("JavaFX FXML Example");
        fxmlLoader.setController(new AppController());
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    /**
     * The main method that launches the JavaFX application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}
