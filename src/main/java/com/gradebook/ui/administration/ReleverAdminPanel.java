package com.gradebook.ui.administration;

import com.gradebook.config.ServiceLocator;
import com.gradebook.model.Administration;
import com.gradebook.model.Classe;
import com.gradebook.model.Etudiant;
import com.gradebook.model.ReleveDeNotes;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReleverAdminPanel extends VBox {
    private static final String PANEL_BG = "#F5F7FA";
    private static final String TITLE_STYLE = "-fx-font-size: 14px; -fx-font-weight: bold;";

    private static final String BTN_PRIMARY = "-fx-background-color: #2E4057; -fx-text-fill: white;";
    private static final String BTN_SECONDARY = "-fx-background-color: #3a5068; -fx-text-fill: white;";
    private static final String BTN_SUCCESS = "-fx-background-color: #27AE60; -fx-text-fill: white;";

    private static final String ALL_CLASSES_LABEL = "Toutes les classes";
    private static final String ALL_SEMESTERS_LABEL = "Tous les semestres";
    private static final String SEMESTRE_1_LABEL = "Semestre 1";
    private static final String SEMESTRE_2_LABEL = "Semestre 2";

    private static final String DEFAULT_ANNEE = "2025-2026";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final double ROUND_TWO = 100.0;

    private final Administration administration;

    private final ComboBox<Classe> cbClasse = new ComboBox<>();
    private final ComboBox<Integer> cbSemestre = new ComboBox<>();
    private final TextField tfAnnee = new TextField(DEFAULT_ANNEE);
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    private final ComboBox<Classe> cbFiltreClasse = new ComboBox<>();
    private final ComboBox<String> cbFiltreSemestre = new ComboBox<>();

    private final TableView<ReleveDeNotes> table = new TableView<>();
    private final List<ReleveDeNotes> allReleves = new ArrayList<>();

    private final BooleanProperty isGenerating = new SimpleBooleanProperty(false);

    private final Button btnGenerer = new Button("⚙️ Générer les relevés");
    private final Button btnFiltrer = new Button("🔍 Filtrer");
    private final Button btnReset = new Button("🔄 Réinitialiser");
    private final Button btnExporter = new Button("📥 Exporter PDF");
    private final Button btnExporterTous = new Button("📥 Exporter tous (PDF)");

    public ReleverAdminPanel(Administration administration) {
        this.administration = administration;

        setPadding(new Insets(25));
        setSpacing(15);
        setStyle("-fx-background-color: " + PANEL_BG + ";");

        VBox topSection = buildTopSection();
        VBox bottomSection = buildBottomSection();

        getChildren().addAll(topSection, new Separator(), bottomSection);

        loadClasses();
        loadAllReleves();
    }

    private VBox buildTopSection() {
        VBox box = new VBox(10);

        Label title = new Label("Générer les relevés");
        title.setStyle(TITLE_STYLE);

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);

        configureClasseComboBox(cbClasse);
        configureSemestreComboBox(cbSemestre);
        cbSemestre.setItems(FXCollections.observableArrayList(1, 2));

        tfAnnee.setMaxWidth(Double.MAX_VALUE);
        cbClasse.setMaxWidth(Double.MAX_VALUE);
        cbSemestre.setMaxWidth(Double.MAX_VALUE);

        form.add(new Label("Classe :"), 0, 0);
        form.add(cbClasse, 1, 0);
        form.add(new Label("Semestre :"), 0, 1);
        form.add(cbSemestre, 1, 1);
        form.add(new Label("Année académique :"), 0, 2);
        form.add(tfAnnee, 1, 2);

        GridPane.setHgrow(cbClasse, Priority.ALWAYS);
        GridPane.setHgrow(cbSemestre, Priority.ALWAYS);
        GridPane.setHgrow(tfAnnee, Priority.ALWAYS);

        progressIndicator.setPrefSize(26, 26);
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);

        btnGenerer.setStyle(BTN_PRIMARY);
        btnGenerer.disableProperty().bind(
            cbClasse.valueProperty().isNull()
                .or(cbSemestre.valueProperty().isNull())
                .or(isGenerating)
        );
        btnGenerer.setOnAction(event -> handleGenererReleves());

        HBox actions = new HBox(10, btnGenerer, progressIndicator);
        actions.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(title, form, actions);
        return box;
    }

    private VBox buildBottomSection() {
        VBox box = new VBox(10);

        Label title = new Label("Relevés disponibles");
        title.setStyle(TITLE_STYLE);

        configureClasseComboBox(cbFiltreClasse);
        cbFiltreSemestre.setItems(FXCollections.observableArrayList(
                ALL_SEMESTERS_LABEL,
                SEMESTRE_1_LABEL,
                SEMESTRE_2_LABEL
        ));
        cbFiltreSemestre.setValue(ALL_SEMESTERS_LABEL);

        btnFiltrer.setStyle(BTN_SECONDARY);
        btnFiltrer.setOnAction(event -> applyFilter());

        btnReset.setOnAction(event -> resetFilters());

        HBox filters = new HBox(10,
                new Label("Classe :"), cbFiltreClasse,
                new Label("Semestre :"), cbFiltreSemestre,
                btnFiltrer, btnReset
        );
        filters.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cbFiltreClasse, Priority.ALWAYS);

        configureTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        btnExporter.setStyle(BTN_SUCCESS);
        btnExporter.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        btnExporter.setOnAction(event -> handleExportSingle());

        btnExporterTous.setStyle(BTN_SECONDARY);
        btnExporterTous.setOnAction(event -> handleExportAll());

        HBox exports = new HBox(10, btnExporter, btnExporterTous);
        exports.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(title, filters, table, exports);
        return box;
    }

    private void configureTable() {
        TableColumn<ReleveDeNotes, String> colEtudiant = new TableColumn<>("Étudiant");
        colEtudiant.setPrefWidth(180);
        colEtudiant.setCellValueFactory(data -> new SimpleStringProperty(formatEtudiantNom(data.getValue())));

        TableColumn<ReleveDeNotes, String> colMatricule = new TableColumn<>("Matricule");
        colMatricule.setPrefWidth(100);
        colMatricule.setCellValueFactory(data -> new SimpleStringProperty(formatMatricule(data.getValue())));

        TableColumn<ReleveDeNotes, String> colClasse = new TableColumn<>("Classe");
        colClasse.setPrefWidth(120);
        colClasse.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getEtudiant().getClasse().getNom()
        ));

        TableColumn<ReleveDeNotes, String> colSemestre = new TableColumn<>("Semestre");
        colSemestre.setPrefWidth(100);
        colSemestre.setCellValueFactory(data -> new SimpleStringProperty(
                "Semestre " + data.getValue().getSemestre()
        ));

        TableColumn<ReleveDeNotes, String> colSession = new TableColumn<>("Session");
        colSession.setPrefWidth(100);
        colSession.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getSession() == 2 ? "Session 2" : "Session 1"
        ));

        TableColumn<ReleveDeNotes, String> colAnnee = new TableColumn<>("Année");
        colAnnee.setPrefWidth(100);
        colAnnee.setCellValueFactory(data -> new SimpleStringProperty(
                safeString(data.getValue().getAnneeAcademique())
        ));

        TableColumn<ReleveDeNotes, Double> colMoyenne = new TableColumn<>("Moyenne /20");
        colMoyenne.setPrefWidth(100);
        colMoyenne.setCellValueFactory(data -> new SimpleObjectProperty<>(
                data.getValue().getMoyenneGenerale()
        ));
        colMoyenne.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                    return;
                }
                setText(formatTwo(value));
                if (value >= 14) {
                    setTextFill(Color.web("#27AE60"));
                } else if (value >= 10) {
                    setTextFill(Color.web("#E67E22"));
                } else {
                    setTextFill(Color.web("#E74C3C"));
                }
            }
        });

        TableColumn<ReleveDeNotes, String> colMention = new TableColumn<>("Mention");
        colMention.setPrefWidth(120);
        colMention.setCellValueFactory(data -> new SimpleStringProperty(
                ServiceLocator.getCalculService().getMentionFromMoyenne(data.getValue().getMoyenneGenerale())
        ));

        TableColumn<ReleveDeNotes, String> colResultat = new TableColumn<>("Résultat");
        colResultat.setPrefWidth(100);
        colResultat.setCellValueFactory(data -> new SimpleStringProperty(
                safeString(data.getValue().getResultat())
        ));
        colResultat.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || value.isBlank()) {
                    setText(null);
                    setTextFill(Color.BLACK);
                    return;
                }
                setText(value);
                if ("Validé".equalsIgnoreCase(value)) {
                    setTextFill(Color.web("#27AE60"));
                } else {
                    setTextFill(Color.web("#E74C3C"));
                }
            }
        });

        TableColumn<ReleveDeNotes, String> colGenere = new TableColumn<>("Généré le");
        colGenere.setPrefWidth(150);
        colGenere.setCellValueFactory(data -> new SimpleStringProperty(
                formatDateTime(data.getValue().getGenereLe())
        ));

        table.getColumns().setAll(
            colEtudiant, colMatricule, colClasse, colSemestre, colSession, colAnnee,
            colMoyenne, colMention, colResultat, colGenere
        );
    }

    private void configureClasseComboBox(ComboBox<Classe> comboBox) {
        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Classe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));
        comboBox.setMaxWidth(Double.MAX_VALUE);
    }

    private void configureSemestreComboBox(ComboBox<Integer> comboBox) {
        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Semestre " + item);
                }
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));
    }

    private void loadClasses() {
        try {
            List<Classe> classes = ServiceLocator.getReferentielService().getAllClasses();
            cbClasse.setItems(FXCollections.observableArrayList(classes));

            Classe all = new Classe();
            all.setId(0);
            all.setNom(ALL_CLASSES_LABEL);
            List<Classe> filtreClasses = new ArrayList<>();
            filtreClasses.add(all);
            filtreClasses.addAll(classes);
            cbFiltreClasse.setItems(FXCollections.observableArrayList(filtreClasses));
            cbFiltreClasse.setValue(all);
        } catch (Exception e) {
            showError(e);
        }
    }

    private void loadAllReleves() {
        try {
            allReleves.clear();
            List<Classe> classes = ServiceLocator.getReferentielService().getAllClasses();
            for (Classe classe : classes) {
                List<Etudiant> etudiants = ServiceLocator.getReferentielService().getEtudiantsByClasse(classe.getId());
                for (Etudiant etudiant : etudiants) {
                    allReleves.addAll(ServiceLocator.getReleveService().getRelevesByEtudiant(etudiant.getId()));
                }
            }
            applyFilter();
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleGenererReleves() {
        Classe classe = cbClasse.getValue();
        Integer semestre = cbSemestre.getValue();
        if (classe == null || semestre == null) {
            return;
        }
        String annee = tfAnnee.getText() != null ? tfAnnee.getText().trim() : "";
        if (annee.isEmpty()) {
            annee = DEFAULT_ANNEE;
            tfAnnee.setText(annee);
        }

        progressIndicator.setVisible(true);
        progressIndicator.setManaged(true);
        isGenerating.set(true);

        int adminId = administration != null ? administration.getId() : 0;

        String anneeFinal = annee;
        int semestreFinal = semestre;
        String classeNom = classe.getNom();

        new Thread(() -> {
            try {
                List<ReleveDeNotes> releves = ServiceLocator.getReleveService()
                        .genererRelevesPourClasse(classe.getId(), semestreFinal, anneeFinal, adminId);
                String sessionLabel = resolveSessionLabel(releves);
                String message = releves.size() + " relevés Session " + sessionLabel +
                        " générés pour " + classeNom + " — Semestre " + semestreFinal;
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    progressIndicator.setManaged(false);
                    isGenerating.set(false);
                    showInfo(message);
                    loadAllReleves();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    progressIndicator.setManaged(false);
                    isGenerating.set(false);
                    showError(e);
                });
            }
        }).start();
    }

    private void applyFilter() {
        Classe classe = cbFiltreClasse.getValue();
        Integer semestre = parseSemestre(cbFiltreSemestre.getValue());

        List<ReleveDeNotes> filtered = new ArrayList<>();
        for (ReleveDeNotes releve : allReleves) {
            boolean matchClasse = classe == null || classe.getId() == 0 ||
                    (releve.getEtudiant() != null && releve.getEtudiant().getClasse() != null &&
                            releve.getEtudiant().getClasse().getId() == classe.getId());
            boolean matchSemestre = semestre == null || releve.getSemestre() == semestre;
            if (matchClasse && matchSemestre) {
                filtered.add(releve);
            }
        }

        table.setItems(FXCollections.observableArrayList(filtered));
    }

    private void resetFilters() {
        if (!cbFiltreClasse.getItems().isEmpty()) {
            cbFiltreClasse.setValue(cbFiltreClasse.getItems().get(0));
        }
        cbFiltreSemestre.setValue(ALL_SEMESTERS_LABEL);
        table.setItems(FXCollections.observableArrayList(allReleves));
    }

    private void handleExportSingle() {
        ReleveDeNotes selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            File generated = ServiceLocator.getReleveService().exporterRelevePDF(selected);

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Enregistrer le relevé PDF");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));
            chooser.setInitialFileName(buildSuggestedFileName(selected));

            File target = chooser.showSaveDialog(getScene().getWindow());
            if (target == null) {
                return;
            }
            Files.copy(generated.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            showInfo("Relevé exporté !");
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleExportAll() {
        List<ReleveDeNotes> current = new ArrayList<>(table.getItems());
        if (current.isEmpty()) {
            return;
        }
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir un dossier d'export");
        File dir = chooser.showDialog(getScene().getWindow());
        if (dir == null) {
            return;
        }

        try {
            int count = 0;
            for (ReleveDeNotes releve : current) {
                File generated = ServiceLocator.getReleveService().exporterRelevePDF(releve);
                File target = new File(dir, buildSuggestedFileName(releve));
                Files.copy(generated.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                count++;
            }
            showInfo(count + " relevés exportés dans " + dir.getAbsolutePath());
        } catch (Exception e) {
            showError(e);
        }
    }

    private Integer parseSemestre(String label) {
        if (label == null || ALL_SEMESTERS_LABEL.equals(label)) {
            return null;
        }
        if (SEMESTRE_1_LABEL.equals(label)) {
            return 1;
        }
        if (SEMESTRE_2_LABEL.equals(label)) {
            return 2;
        }
        return null;
    }

    private String buildSuggestedFileName(ReleveDeNotes releve) {
        String matricule = "inconnu";
        String annee = safeString(releve.getAnneeAcademique());
        Etudiant etudiant = releve.getEtudiant();
        if (etudiant != null && etudiant.getCne() != null) {
            matricule = etudiant.getCne();
        }
        return "releve_" + matricule + "_S" + releve.getSemestre() + "_" + annee + ".pdf";
    }

    private String formatEtudiantNom(ReleveDeNotes releve) {
        Etudiant etudiant = releve.getEtudiant();
        if (etudiant == null) {
            return "";
        }
        return safeString(etudiant.getNom()) + " " + safeString(etudiant.getPrenom());
    }

    private String formatMatricule(ReleveDeNotes releve) {
        Etudiant etudiant = releve.getEtudiant();
        if (etudiant == null || etudiant.getCne() == null) {
            return "";
        }
        return etudiant.getCne();
    }

    private String formatClasseNom(ReleveDeNotes releve) {
        Etudiant etudiant = releve.getEtudiant();
        if (etudiant == null || etudiant.getClasse() == null) {
            return "";
        }
        return safeString(etudiant.getClasse().getNom());
    }

    private String resolveSessionLabel(List<ReleveDeNotes> releves) {
        if (releves == null || releves.isEmpty()) {
            return "1/2";
        }
        boolean allSession1 = true;
        boolean allSession2 = true;
        for (ReleveDeNotes releve : releves) {
            if (releve.getSession() != 1) {
                allSession1 = false;
            }
            if (releve.getSession() != 2) {
                allSession2 = false;
            }
        }
        if (allSession1) {
            return "1";
        }
        if (allSession2) {
            return "2";
        }
        return "1/2";
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(DATE_FORMATTER);
    }

    private String formatTwo(double value) {
        double rounded = Math.round(value * ROUND_TWO) / ROUND_TWO;
        return String.format(Locale.US, "%.2f", rounded);
    }

    private String safeString(String value) {
        return value != null ? value : "";
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Erreur inconnue";
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
