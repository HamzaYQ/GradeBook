package com.gradebook.ui.enseignant;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StatistiquesPanel extends VBox {
    public StatistiquesPanel() {
        setPadding(new Insets(25));
        getChildren().add(new Label("Panel Statistiques - En cours"));
    }
}
