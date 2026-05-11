package com.gradebook.ui.etudiant;

import com.gradebook.config.ServiceLocator;
import com.gradebook.model.Classe;
import com.gradebook.model.Etudiant;
import com.gradebook.model.Evaluation;
import com.gradebook.model.LigneReleve;
import com.gradebook.model.Matiere;
import com.gradebook.model.Note;
import com.gradebook.model.ReleveDeNotes;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class EtudiantMainFrame extends Application {
    private static final int WIDTH = 950;
    private static final int HEIGHT = 650;
    private static final int MENU_WIDTH = 200;
    private static final int TOP_HEIGHT = 60;

    private static final String APP_TITLE = "GradeBook";
    private static final String WINDOW_TITLE = "GradeBook — Espace Étudiant";
    private static final String BACKGROUND_MAIN = "#F5F7FA";
    private static final String TOP_BAR_BG = "#2E4057";
    private static final String MENU_BG = "#3a5068";

    private static final String MENU_BTN_NORMAL =
            "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13px;" +
            "-fx-alignment: center-left; -fx-padding: 15px;";
    private static final String MENU_BTN_HOVER =
            "-fx-background-color: #2E4057; -fx-text-fill: white; -fx-font-size: 13px;" +
            "-fx-alignment: center-left; -fx-padding: 15px;";
    private static final String MENU_BTN_ACTIVE =
            "-fx-background-color: #1a2535; -fx-text-fill: white; -fx-font-size: 13px;" +
            "-fx-alignment: center-left; -fx-padding: 15px;";

    private static final String PANEL_STYLE =
            "-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);";

    private static final String COLOR_SUCCESS = "#27AE60";
    private static final String COLOR_WARNING = "#E67E22";
    private static final String COLOR_DANGER = "#E74C3C";

    private static final String NOTES_EMPTY_TEXT = "Aucune note disponible pour cette période";
    private static final String RELEVES_EMPTY_TEXT = "Aucun relevé disponible";
    private static final String LOGOUT_CONFIRM_TEXT = "Voulez-vous vraiment vous déconnecter ?";

    private static final String ROLE_NOTES = "📋 Mes Notes";
    private static final String ROLE_RELEVES = "📄 Mes Relevés";
    private static final String ROLE_LOGOUT = "🚪 Se déconnecter";

    private static final String PERIOD_SEM1 = "SEMESTRE_1";
    private static final String PERIOD_SEM2 = "SEMESTRE_2";
    private static final String PERIOD_YEAR = "ANNUEL";

    private static final double ROUND_TWO = 100.0;
    private static final double NOTE_MAX = 20.0;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Etudiant etudiant;

    private Stage stage;
    private StackPane contentPane;
    private Button activeButton;

    private TableView<Note> notesTable;
    private Label notesEmptyLabel;
    private GridPane moyennesGrid;
    private Label moyenneGeneraleLabel;
    private ComboBox<String> periodeCombo;

    private TableView<ReleveDeNotes> relevesTable;
    private Button exportButton;

    private VBox notesPanel;
    private VBox relevesPanel;

    public EtudiantMainFrame(Etudiant etudiant) {
        this.etudiant = etudiant;
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        BorderPane root = new BorderPane();
        root.setStyle("-fx-font-family: System; -fx-font-size: 13px;");
        root.setTop(buildTopBar());
        root.setLeft(buildMenu());

        contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: " + BACKGROUND_MAIN + "; -fx-padding: 20;");
        root.setCenter(contentPane);

        showNotesPanel();

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setTitle(WINDOW_TITLE);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setPrefHeight(TOP_HEIGHT);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 20, 0, 20));
        bar.setStyle("-fx-background-color: " + TOP_BAR_BG + ";");

        Label title = new Label(APP_TITLE);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        String prenom = etudiant != null ? etudiant.getPrenom() : "";
        String nom = etudiant != null ? etudiant.getNom() : "";
        Label greeting = new Label("Bonjour, " + prenom + " " + nom);
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

        Button notesButton = createMenuButton(ROLE_NOTES);
        Button relevesButton = createMenuButton(ROLE_RELEVES);
        Button logoutButton = createMenuButton(ROLE_LOGOUT);

        notesButton.setOnAction(event -> {
            setActive(notesButton);
            showNotesPanel();
        });

        relevesButton.setOnAction(event -> {
            setActive(relevesButton);
            showRelevesPanel();
        });

        logoutButton.setOnAction(event -> handleLogout());

        menu.getChildren().addAll(notesButton, relevesButton, logoutButton);
        setActive(notesButton);
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

    private void showNotesPanel() {
        if (notesPanel == null) {
            notesPanel = buildNotesPanel();
        }
        contentPane.getChildren().setAll(notesPanel);
        loadNotes();
    }

    private void showRelevesPanel() {
        if (relevesPanel == null) {
            relevesPanel = buildRelevesPanel();
        }
        contentPane.getChildren().setAll(relevesPanel);
        loadReleves();
    }

    private VBox buildNotesPanel() {
        VBox panel = new VBox(15);
        panel.setStyle(PANEL_STYLE);

        Label title = new Label("Mes Notes");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2E4057;");

        periodeCombo = new ComboBox<>();
        periodeCombo.setItems(FXCollections.observableArrayList(PERIOD_SEM1, PERIOD_SEM2, PERIOD_YEAR));
        periodeCombo.setValue(PERIOD_YEAR);
        periodeCombo.setOnAction(event -> loadNotes());

        Button refreshButton = new Button("Actualiser");
        refreshButton.setOnAction(event -> loadNotes());

        HBox header = new HBox(10, title, periodeCombo, refreshButton);
        header.setAlignment(Pos.CENTER_LEFT);

        notesTable = new TableView<>();
        notesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Note, String> matiereCol = new TableColumn<>("Matière");
        matiereCol.setCellValueFactory(data -> new SimpleStringProperty(getMatiereIntitule(data.getValue())));

        TableColumn<Note, String> evalCol = new TableColumn<>("Évaluation");
        evalCol.setCellValueFactory(data -> new SimpleStringProperty(getEvaluationLibelle(data.getValue())));

        TableColumn<Note, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(getTypeLibelle(data.getValue())));

        TableColumn<Note, String> sessionCol = new TableColumn<>("Session");
        sessionCol.setCellValueFactory(data -> new SimpleStringProperty(getSessionLibelle(data.getValue())));

        TableColumn<Note, String> coefCol = new TableColumn<>("Coefficient");
        coefCol.setCellValueFactory(data -> new SimpleStringProperty(getEvaluationCoefficient(data.getValue())));

        TableColumn<Note, String> noteCol = new TableColumn<>("Note /20");
        noteCol.setCellValueFactory(data -> new SimpleStringProperty(formatTwo(data.getValue().getValeur())));

        notesTable.getColumns().addAll(matiereCol, evalCol, typeCol, sessionCol, coefCol, noteCol);

        notesEmptyLabel = new Label(NOTES_EMPTY_TEXT);
        notesEmptyLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
        notesEmptyLabel.setVisible(false);
        notesEmptyLabel.setManaged(false);
        StackPane.setAlignment(notesEmptyLabel, Pos.CENTER);

        StackPane tableWrapper = new StackPane(notesTable, notesEmptyLabel);
        VBox.setVgrow(tableWrapper, Priority.ALWAYS);

        moyennesGrid = new GridPane();
        moyennesGrid.setHgap(12);
        moyennesGrid.setVgap(6);

        moyenneGeneraleLabel = new Label();
        moyenneGeneraleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox averagesBox = new VBox(8, moyennesGrid, moyenneGeneraleLabel);

        panel.getChildren().addAll(header, tableWrapper, averagesBox);
        return panel;
    }

    private VBox buildRelevesPanel() {
        VBox panel = new VBox(15);
        panel.setStyle(PANEL_STYLE);

        Label title = new Label("Mes Relevés de Notes");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2E4057;");

        relevesTable = new TableView<>();
        relevesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        relevesTable.setPlaceholder(new Label(RELEVES_EMPTY_TEXT));

        TableColumn<ReleveDeNotes, String> periodeCol = new TableColumn<>("Semestre");
        periodeCol.setCellValueFactory(data -> new SimpleStringProperty(getPeriodeLibelle(data.getValue())));

        TableColumn<ReleveDeNotes, String> anneeCol = new TableColumn<>("Année académique");
        anneeCol.setCellValueFactory(data -> new SimpleStringProperty(getAnneeAcademique(data.getValue())));

        TableColumn<ReleveDeNotes, String> moyenneCol = new TableColumn<>("Moyenne générale");
        moyenneCol.setCellValueFactory(data -> new SimpleStringProperty(formatTwo(data.getValue().getMoyenneGenerale())));

        TableColumn<ReleveDeNotes, String> genereCol = new TableColumn<>("Généré le");
        genereCol.setCellValueFactory(data -> new SimpleStringProperty(getGenereLe(data.getValue())));

        relevesTable.getColumns().addAll(periodeCol, anneeCol, moyenneCol, genereCol);

        exportButton = new Button("📥 Télécharger PDF");
        exportButton.disableProperty().bind(relevesTable.getSelectionModel().selectedItemProperty().isNull());
        exportButton.setOnAction(event -> exportSelectedReleve());

        VBox.setVgrow(relevesTable, Priority.ALWAYS);
        panel.getChildren().addAll(title, relevesTable, exportButton);
        return panel;
    }

    private void loadNotes() {
        try {
            if (etudiant == null || etudiant.getClasse() == null) {
                showErrorAlert(new IllegalStateException("Classe introuvable pour l'étudiant"));
                return;
            }

            int semestre = getSelectedSemestre();
            List<Note> notes;
            if (semestre == 0) {
                notes = ServiceLocator.getNoteService().getNotesByEtudiant(etudiant.getId());
            } else {
                notes = ServiceLocator.getNoteService().getNotesByEtudiantAndSemestre(etudiant.getId(), semestre);
            }
            enrichNotes(notes);

            notesTable.setItems(FXCollections.observableArrayList(notes));
            boolean empty = notes.isEmpty();
            notesEmptyLabel.setVisible(empty);
            notesEmptyLabel.setManaged(empty);

            List<Matiere> matieres = ServiceLocator.getMatiereDao().findByClasse(etudiant.getClasse().getId());
            updateMoyennes(matieres, semestre);
        } catch (Exception e) {
            showErrorAlert(e);
        }
    }

    private void updateMoyennes(List<Matiere> matieres, int semestre) {
        moyennesGrid.getChildren().clear();
        if (matieres == null) {
            matieres = new ArrayList<>();
        }

        int row = 0;
        for (Matiere matiere : matieres) {
            double moyenne;
            if (semestre == 0) {
            double moyenneS1 = ServiceLocator.getCalculService()
                .calculerMoyenneParMatiere(etudiant.getId(), matiere.getId(), 1);
            double moyenneS2 = ServiceLocator.getCalculService()
                .calculerMoyenneParMatiere(etudiant.getId(), matiere.getId(), 2);
            moyenne = (moyenneS1 + moyenneS2) / 2.0;
            } else {
            moyenne = ServiceLocator.getCalculService()
                .calculerMoyenneParMatiere(etudiant.getId(), matiere.getId(), semestre);
            }
            String text = "Moyenne " + matiere.getIntitule() + " : " + formatTwo(moyenne) + "/20";
            Label label = new Label(text);
            moyennesGrid.add(label, 0, row++);
        }

        double moyenneGenerale;
        if (semestre == 0) {
            moyenneGenerale = ServiceLocator.getCalculService()
                .calculerMoyenneAnnuelle(etudiant.getId(), etudiant.getClasse().getId());
        } else {
            moyenneGenerale = ServiceLocator.getCalculService()
                .calculerMoyenneGenerale(etudiant.getId(), etudiant.getClasse().getId(), semestre);
        }
        moyenneGeneraleLabel.setText("Moyenne Générale : " + formatTwo(moyenneGenerale) + "/20");
        moyenneGeneraleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " +
                getMoyenneColor(moyenneGenerale) + ";");
    }

    private void enrichNotes(List<Note> notes) {
        Map<Integer, Evaluation> cache = new HashMap<>();
        for (Note note : notes) {
            Evaluation evaluation = note.getEvaluation();
            if (evaluation == null || evaluation.getId() == 0) {
                continue;
            }
            if (evaluation.getMatiere() == null) {
                Evaluation full = cache.get(evaluation.getId());
                if (full == null) {
                    Optional<Evaluation> loaded = ServiceLocator.getEvaluationDao().findById(evaluation.getId());
                    if (loaded.isPresent()) {
                        full = loaded.get();
                        cache.put(evaluation.getId(), full);
                    }
                }
                if (full != null) {
                    note.setEvaluation(full);
                }
            }
        }
    }

    private void loadReleves() {
        try {
            List<ReleveDeNotes> releves = ServiceLocator.getReleveService().getRelevesByEtudiant(etudiant.getId());
            relevesTable.setItems(FXCollections.observableArrayList(releves));
        } catch (Exception e) {
            showErrorAlert(e);
        }
    }

    private void exportSelectedReleve() {
        ReleveDeNotes selected = relevesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName(buildReleveFileName(selected));
        File destination = chooser.showSaveDialog(stage);
        if (destination == null) {
            return;
        }

        try {
            File generated = ServiceLocator.getReleveService().exporterRelevePDF(selected);
            Files.copy(generated.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            showInfoAlert("Relevé exporté avec succès !");
        } catch (Exception e) {
            showErrorAlert(e);
        }
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

    private String getMatiereIntitule(Note note) {
        Evaluation evaluation = note.getEvaluation();
        if (evaluation == null || evaluation.getMatiere() == null) {
            return "-";
        }
        return evaluation.getMatiere().getIntitule();
    }

    private String getEvaluationLibelle(Note note) {
        Evaluation evaluation = note.getEvaluation();
        return evaluation != null ? evaluation.getLibelle() : "-";
    }

    private String getTypeLibelle(Note note) {
        Evaluation evaluation = note.getEvaluation();
        if (evaluation == null || evaluation.getType() == null) {
            return "-";
        }
        return evaluation.getType().getLibelle();
    }

    private String getSessionLibelle(Note note) {
        Evaluation evaluation = note.getEvaluation();
        if (evaluation == null || evaluation.getSession() == null) {
            return "-";
        }
        return evaluation.getSession().getLibelle();
    }

    private String getEvaluationCoefficient(Note note) {
        Evaluation evaluation = note.getEvaluation();
        if (evaluation == null) {
            return "-";
        }
        return formatTwo(evaluation.getCoefficient());
    }

    private String getPeriodeLibelle(ReleveDeNotes releve) {
        int semestre = releve.getSemestre();
        if (semestre != 1 && semestre != 2) {
            return "-";
        }
        return "Semestre " + semestre;
    }

    private String getAnneeAcademique(ReleveDeNotes releve) {
        return releve.getAnneeAcademique() != null ? releve.getAnneeAcademique() : "-";
    }

    private String getGenereLe(ReleveDeNotes releve) {
        LocalDateTime date = releve.getGenereLe();
        if (date == null) {
            return "-";
        }
        return date.format(DATE_TIME_FORMATTER);
    }

    private String buildReleveFileName(ReleveDeNotes releve) {
        String cne = releve.getEtudiant() != null ? releve.getEtudiant().getCne() : "etudiant";
        String periode = "SEMESTRE_" + releve.getSemestre();
        String annee = releve.getAnneeAcademique() != null ? releve.getAnneeAcademique() : "NA";
        return "releve_" + cne + "_" + periode + "_" + annee + ".pdf";
    }

    private String getMoyenneColor(double moyenne) {
        if (moyenne >= 14.0) {
            return COLOR_SUCCESS;
        }
        if (moyenne >= 10.0) {
            return COLOR_WARNING;
        }
        return COLOR_DANGER;
    }

    private int getSelectedSemestre() {
        String periode = periodeCombo != null ? periodeCombo.getValue() : null;
        if (PERIOD_SEM1.equals(periode)) {
            return 1;
        }
        if (PERIOD_SEM2.equals(periode)) {
            return 2;
        }
        return 0;
    }

    private String formatTwo(double valeur) {
        double arrondi = Math.round(valeur * ROUND_TWO) / ROUND_TWO;
        return String.format(Locale.US, "%.2f", arrondi);
    }

    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showErrorAlert(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Erreur inconnue";
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
