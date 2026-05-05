package com.gradebook.ui.administration;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AffectationsPanel extends VBox {
    public AffectationsPanel() {
        setPadding(new Insets(25));
        getChildren().add(new Label("Panel Affectations — En cours"));
    }
}
