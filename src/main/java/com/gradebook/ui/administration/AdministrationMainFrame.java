package com.gradebook.ui.administration;

import com.gradebook.model.Administration;
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

public class AdministrationMainFrame extends javafx.application.Application {
    private static final int WIDTH = 1100;
    private static final int HEIGHT = 700;
    private static final int MENU_WIDTH = 220;
    private static final int TOP_HEIGHT = 60;

    private static final String WINDOW_TITLE = "GradeBook — Espace Administration";
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

    private static final String MENU_REFERENTIEL = "🏫 Référentiel";
    private static final String MENU_AFFECTATIONS = "👨‍🏫 Affectations";
    private static final String MENU_RELEVES = "📄 Relevés de Notes";
    private static final String MENU_PRESENCES = "📊 Présences";
    private static final String MENU_LOGOUT = "🚪 Se déconnecter";

    private static final String LOGOUT_CONFIRM_TEXT = "Voulez-vous vous déconnecter ?";

    private final Administration administration;

    private Stage stage;
    private StackPane contentPane;
    private Button activeButton;

    private Button referentielButton;
    private Button affectationsButton;
    private Button relevesButton;
    private Button presencesButton;
    private Button logoutButton;

    private ReferentielPanel referentielPanel;
    private AffectationsPanel affectationsPanel;
    private ReleverAdminPanel releverAdminPanel;
    private PresenceAdminPanel presenceAdminPanel;

    public AdministrationMainFrame(Administration administration) {
        this.administration = administration;
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
        setActive(referentielButton);
        showPanel(referentielPanel);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setTitle(WINDOW_TITLE);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private void initPanels() {
        referentielPanel = new ReferentielPanel();
        affectationsPanel = new AffectationsPanel();
        releverAdminPanel = new ReleverAdminPanel(administration);
        presenceAdminPanel = new PresenceAdminPanel();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setPrefHeight(TOP_HEIGHT);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 20, 0, 20));
        bar.setStyle("-fx-background-color: " + TOP_BAR_BG + ";");

        Label title = new Label("GradeBook");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        String prenom = administration != null ? administration.getPrenom() : "";
        String nom = administration != null ? administration.getNom() : "";
        Label greeting = new Label("Administration — " + prenom + " " + nom);
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

        referentielButton = createMenuButton(MENU_REFERENTIEL);
        affectationsButton = createMenuButton(MENU_AFFECTATIONS);
        relevesButton = createMenuButton(MENU_RELEVES);
        presencesButton = createMenuButton(MENU_PRESENCES);
        logoutButton = createMenuButton(MENU_LOGOUT);

        referentielButton.setOnAction(event -> {
            setActive(referentielButton);
            showPanel(referentielPanel);
        });
        affectationsButton.setOnAction(event -> {
            setActive(affectationsButton);
            affectationsPanel.refreshData();
            showPanel(affectationsPanel);
        });
        relevesButton.setOnAction(event -> {
            setActive(relevesButton);
            showPanel(releverAdminPanel);
        });
        presencesButton.setOnAction(event -> {
            setActive(presencesButton);
            showPanel(presenceAdminPanel);
        });
        logoutButton.setOnAction(event -> handleLogout());

        menu.getChildren().addAll(
                referentielButton,
                affectationsButton,
                relevesButton,
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
