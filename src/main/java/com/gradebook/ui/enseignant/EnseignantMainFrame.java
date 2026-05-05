package com.gradebook.ui.enseignant;

import com.gradebook.model.Enseignant;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class EnseignantMainFrame extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private final Enseignant enseignant;

    public EnseignantMainFrame(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setTitle("GradeBook — Enseignant");
        stage.setScene(scene);
        stage.show();
    }
}
