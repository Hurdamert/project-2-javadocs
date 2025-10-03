package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Hello world!
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Button btn = new Button("Click me!");
        btn.setOnAction(e -> System.out.println("Button clicked!"));

        StackPane root = new StackPane(btn);
        Scene scene = new Scene(root, 400, 300);

        stage.setTitle("JavaFX Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(); // this starts the JavaFX application
    }
}
