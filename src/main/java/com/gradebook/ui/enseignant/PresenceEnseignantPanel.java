package com.gradebook.ui.enseignant;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PresenceEnseignantPanel extends VBox {
    public PresenceEnseignantPanel() {
        setPadding(new Insets(25));
        getChildren().add(new Label("Panel Presences - En cours"));
    }
}
