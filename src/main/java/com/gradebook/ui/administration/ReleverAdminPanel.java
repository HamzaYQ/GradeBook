package com.gradebook.ui.administration;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ReleverAdminPanel extends VBox {
    public ReleverAdminPanel() {
        setPadding(new Insets(25));
        getChildren().add(new Label("Panel Relevés — En cours"));
    }
}
