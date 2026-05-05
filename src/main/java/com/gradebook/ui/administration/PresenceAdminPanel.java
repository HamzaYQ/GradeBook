package com.gradebook.ui.administration;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PresenceAdminPanel extends VBox {
    public PresenceAdminPanel() {
        setPadding(new Insets(25));
        getChildren().add(new Label("Panel Présences — En cours"));
    }
}
