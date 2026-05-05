package com.gradebook;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource("/com/gradebook/ui/login.fxml"));
            Scene scene = new Scene(root, 450, 550);
            stage.setTitle("GradeBook — Connexion");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger login.fxml", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
