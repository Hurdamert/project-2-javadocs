package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class AppController {

    @FXML
    private Button myButton;

    @FXML
    private void initialize() {
        myButton.setOnAction(e -> System.out.println("Button clicked!"));
    }
}