package com.geekbrains.mycloud.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/sampleAuth.fxml"));
        primaryStage.setTitle("MyCloud Registration");
        primaryStage.setScene(new Scene(root, 700, 700));
        primaryStage.show();
    }
}
