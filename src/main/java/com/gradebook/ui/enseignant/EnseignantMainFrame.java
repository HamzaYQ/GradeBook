package com.gradebook.ui.enseignant;

import com.gradebook.model.Enseignant;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class EnseignantMainFrame extends Application {
    private static final int WIDTH = 1100;
    private static final int HEIGHT = 700;
    private static final int MENU_WIDTH = 220;
    private static final int TOP_HEIGHT = 60;

    private static final String WINDOW_TITLE = "GradeBook — Espace Enseignant";
    private static final String TOP_BAR_BG = "#2E4057";
    private static final String MENU_BG = "#3a5068";
    private static final String CONTENT_BG = "#F5F7FA";

    private static final String MENU_BTN_NORMAL =
            "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13px;" +
            "-fx-alignment: center-left; -fx-padding: 15px 20px;";
    private static final String MENU_BTN_HOVER =
            "-fx-background-color: #2E4057; -fx-text-fill: white; -fx-font-size: 13px;" +
            "-fx-alignment: center-left; -fx-padding: 15px 20px;";
    private static final String MENU_BTN_ACTIVE =
            "-fx-background-color: #1a2535; -fx-text-fill: white; -fx-font-size: 13px;" +
            "-fx-alignment: center-left; -fx-padding: 15px 20px;";

    private static final String MENU_NOTES = "📝 Saisie des notes";
    private static final String MENU_STATS = "📊 Statistiques";
    private static final String MENU_PRESENCES = "👁️ Présences";
    private static final String MENU_LOGOUT = "🚪 Se déconnecter";

    private static final String LOGOUT_CONFIRM_TEXT = "Voulez-vous vous déconnecter ?";

    private final Enseignant enseignant;

    private Stage stage;
    private StackPane contentPane;
    private Button activeButton;

    private Button notesButton;
    private Button statsButton;
    private Button presencesButton;
    private Button logoutButton;

    private SaisieNotesPanel saisieNotesPanel;
    private StatistiquesPanel statistiquesPanel;
    private PresenceEnseignantPanel presenceEnseignantPanel;

    public EnseignantMainFrame(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setLeft(buildMenu());

        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        root.setCenter(contentPane);

        initPanels();
        setActive(notesButton);
        showPanel(saisieNotesPanel);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setTitle(WINDOW_TITLE);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private void initPanels() {
        saisieNotesPanel = new SaisieNotesPanel(enseignant);
        statistiquesPanel = new StatistiquesPanel(enseignant);
        presenceEnseignantPanel = new PresenceEnseignantPanel();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setPrefHeight(TOP_HEIGHT);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 20, 0, 20));
        bar.setStyle("-fx-background-color: " + TOP_BAR_BG + ";");

        Label title = new Label("GradeBook");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        String prenom = enseignant != null ? enseignant.getPrenom() : "";
        String nom = enseignant != null ? enseignant.getNom() : "";
        Label greeting = new Label("Enseignant — " + prenom + " " + nom);
        greeting.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(title, spacer, greeting);
        return bar;
    }

    private VBox buildMenu() {
        VBox menu = new VBox();
        menu.setPrefWidth(MENU_WIDTH);
        menu.setStyle("-fx-background-color: " + MENU_BG + ";");
        menu.setPadding(new Insets(10, 0, 10, 0));

        notesButton = createMenuButton(MENU_NOTES);
        statsButton = createMenuButton(MENU_STATS);
        presencesButton = createMenuButton(MENU_PRESENCES);
        logoutButton = createMenuButton(MENU_LOGOUT);

        notesButton.setOnAction(event -> {
            setActive(notesButton);
            showPanel(saisieNotesPanel);
        });
        statsButton.setOnAction(event -> {
            setActive(statsButton);
            showPanel(statistiquesPanel);
        });
        presencesButton.setOnAction(event -> {
            setActive(presencesButton);
            showPanel(presenceEnseignantPanel);
        });
        logoutButton.setOnAction(event -> handleLogout());

        menu.getChildren().addAll(
                notesButton,
                statsButton,
                presencesButton,
                logoutButton
        );
        return menu;
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle(MENU_BTN_NORMAL);
        button.setOnMouseEntered(event -> {
            if (button != activeButton) {
                button.setStyle(MENU_BTN_HOVER);
            }
        });
        button.setOnMouseExited(event -> {
            if (button != activeButton) {
                button.setStyle(MENU_BTN_NORMAL);
            }
        });
        return button;
    }

    private void setActive(Button button) {
        if (activeButton != null) {
            activeButton.setStyle(MENU_BTN_NORMAL);
        }
        activeButton = button;
        activeButton.setStyle(MENU_BTN_ACTIVE);
    }

    private void showPanel(VBox panel) {
        contentPane.getChildren().setAll(panel);
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, LOGOUT_CONFIRM_TEXT, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Déconnexion");
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stage.close();
            openLogin();
        }
    }

    private void openLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/gradebook/ui/login.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("GradeBook — Connexion");
            loginStage.setResizable(false);
            loginStage.setScene(new Scene(root, 450, 550));
            loginStage.centerOnScreen();
            loginStage.show();
        } catch (IOException e) {
            showErrorAlert(e);
        }
    }

    private void showErrorAlert(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Erreur inconnue";
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
