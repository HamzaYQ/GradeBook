package com.gradebook.ui.enseignant;

import com.gradebook.config.ServiceLocator;
import com.gradebook.model.Classe;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Etudiant;
import com.gradebook.model.Evaluation;
import com.gradebook.model.Matiere;
import com.gradebook.model.Note;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatistiquesPanel extends VBox {
    private static final int SUMMARY_ID = -1;

    private static final String PANEL_BG = "#F5F7FA";
    private static final String PRIMARY_COLOR = "#2E4057";
    private static final String TEXT_GRAY = "#666666";
    private static final String MISSING_NOTE_TEXT = "—";
    private static final String INCOMPLETE_TEXT = "Incomplète";
    private static final String WARNING_COLOR = "#E67E22";

    private static final String EMPTY_EVALUATIONS_TEXT =
            "Aucune évaluation trouvée pour\ncette sélection";
    private static final String EMPTY_STUDENTS_TEXT =
            "Aucun étudiant dans cette classe";

    private final Enseignant enseignant;

    private final ComboBox<Classe> cbClasse = new ComboBox<>();
    private final ComboBox<Integer> cbSemestre = new ComboBox<>();
    private final ComboBox<Matiere> cbMatiere = new ComboBox<>();
    private final Button btnAfficher = new Button("📊 Afficher");

    private final TableView<Etudiant> table = new TableView<>();
    private final Label studentsLabel = new Label();
    private final Label completeLabel = new Label();
    private final Label missingLabel = new Label();
    private final Label successLabel = new Label();
    private final VBox summaryBox = new VBox(6);

    public StatistiquesPanel(Enseignant enseignant) {
        this.enseignant = enseignant;

        setPadding(new Insets(25));
        setSpacing(15);
        setStyle("-fx-background-color: " + PANEL_BG + ";");

        Label title = new Label("Statistiques");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + PRIMARY_COLOR + ";");

        HBox filters = buildFilters();
        buildTable();
        buildSummary();

        getChildren().addAll(title, filters, table, summaryBox);
        VBox.setVgrow(table, Priority.ALWAYS);

        loadClasses();
    }

    private HBox buildFilters() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);

        configureClasseComboBox(cbClasse);
        configureSemestreComboBox(cbSemestre);
        configureMatiereComboBox(cbMatiere);

        cbClasse.valueProperty().addListener((obs, oldValue, newValue) -> {
            loadSemestresForClasse(newValue);
            clearResults();
        });
        cbSemestre.valueProperty().addListener((obs, oldValue, newValue) -> {
            loadMatieresForClasseAndSemestre(cbClasse.getValue(), newValue);
            clearResults();
        });
        cbMatiere.valueProperty().addListener((obs, oldValue, newValue) -> clearResults());

        btnAfficher.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;");
        btnAfficher.disableProperty().bind(
                cbClasse.valueProperty().isNull()
                        .or(cbSemestre.valueProperty().isNull())
                        .or(cbMatiere.valueProperty().isNull())
        );
        btnAfficher.setOnAction(event -> afficherTableau());

        box.getChildren().addAll(
                buildFilterBox("Classe :", cbClasse),
                buildFilterBox("Semestre :", cbSemestre),
                buildFilterBox("Matière :", cbMatiere),
                buildButtonBox(btnAfficher)
        );

        return box;
    }

    private void buildTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(buildPlaceholderLabel(EMPTY_EVALUATIONS_TEXT));
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Etudiant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    return;
                }
                if (isSummaryRow(item)) {
                    setStyle("-fx-background-color: #EEF2FF; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void buildSummary() {
        HBox infoBox = new HBox(15, studentsLabel, completeLabel, missingLabel);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        successLabel.setStyle("-fx-text-fill: " + TEXT_GRAY + ";");
        summaryBox.getChildren().addAll(infoBox, successLabel);
        summaryBox.setVisible(false);
        summaryBox.setManaged(false);
    }

    private void loadClasses() {
        try {
            List<Classe> classes = ServiceLocator.getReferentielService()
                    .getClassesByEnseignant(enseignant.getId());
            cbClasse.setItems(FXCollections.observableArrayList(classes));
            cbSemestre.getItems().clear();
            cbMatiere.getItems().clear();
        } catch (Exception e) {
            showError(e);
        }
    }

    private void loadSemestresForClasse(Classe classe) {
        cbSemestre.getItems().clear();
        cbSemestre.setValue(null);
        cbMatiere.getItems().clear();
        cbMatiere.setValue(null);
        if (classe == null) {
            return;
        }
        try {
            List<Integer> semestres = ServiceLocator.getReferentielService()
                    .getSemestresByEnseignantAndClasse(enseignant.getId(), classe.getId());
            cbSemestre.setItems(FXCollections.observableArrayList(semestres));
            if (!semestres.isEmpty()) {
                cbSemestre.setValue(semestres.get(0));
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    private void loadMatieresForClasseAndSemestre(Classe classe, Integer semestre) {
        cbMatiere.getItems().clear();
        cbMatiere.setValue(null);
        if (classe == null || semestre == null) {
            return;
        }
        try {
            List<Matiere> matieres = ServiceLocator.getReferentielService()
                    .getMatieresByEnseignantAndClasseAndSemestre(enseignant.getId(), classe.getId(), semestre);
            cbMatiere.setItems(FXCollections.observableArrayList(matieres));
        } catch (Exception e) {
            showError(e);
        }
    }

    private void afficherTableau() {
        Classe classe = cbClasse.getValue();
        Integer semestre = cbSemestre.getValue();
        Matiere matiere = cbMatiere.getValue();
        if (classe == null || semestre == null || matiere == null) {
            return;
        }

        try {
            List<Etudiant> etudiants = ServiceLocator.getReferentielService()
                    .getEtudiantsByClasse(classe.getId());
            if (etudiants.isEmpty()) {
                setEmptyState(EMPTY_STUDENTS_TEXT);
                return;
            }

            List<Evaluation> evaluations = ServiceLocator.getEvaluationDao()
                    .findByClasseAndMatiereBySemestre(classe.getId(), matiere.getId(), semestre);
            if (evaluations.isEmpty()) {
                setEmptyState(EMPTY_EVALUATIONS_TEXT);
                return;
            }

            Map<Integer, Map<Integer, Note>> notesByStudent = new HashMap<>();
            for (Etudiant etudiant : etudiants) {
                List<Note> notes = ServiceLocator.getNoteService()
                        .getNotesByEtudiantAndMatiereAndSemestre(etudiant.getId(), matiere.getId(), semestre);
                Map<Integer, Note> noteMap = new HashMap<>();
                for (Note note : notes) {
                    Evaluation evaluation = note.getEvaluation();
                    if (evaluation != null) {
                        noteMap.put(evaluation.getId(), note);
                    }
                }
                notesByStudent.put(etudiant.getId(), noteMap);
            }

            Map<Integer, Double> evaluationMoyennes = new HashMap<>();
            for (Evaluation evaluation : evaluations) {
                double somme = 0.0;
                int count = 0;
                for (Etudiant etudiant : etudiants) {
                    Note note = getNoteForStudent(notesByStudent, etudiant, evaluation.getId());
                    if (note != null) {
                        somme += note.getValeur();
                        count++;
                    }
                }
                evaluationMoyennes.put(evaluation.getId(), count > 0 ? somme / count : Double.NaN);
            }

            Map<Integer, Double> moyennesEtudiants = new HashMap<>();
            int evalCount = evaluations.size();
            int completeCount = 0;
            int successCount = 0;
            double sommeMoyennes = 0.0;
            int moyenneCount = 0;

            for (Etudiant etudiant : etudiants) {
                double somme = 0.0;
                double sommeCoeff = 0.0;
                int found = 0;

                for (Evaluation evaluation : evaluations) {
                    Note note = getNoteForStudent(notesByStudent, etudiant, evaluation.getId());
                    if (note != null) {
                        somme += note.getValeur() * evaluation.getCoefficient();
                        sommeCoeff += evaluation.getCoefficient();
                        found++;
                    }
                }

                if (found == evalCount && sommeCoeff > 0.0) {
                    double moyenne = somme / sommeCoeff;
                    moyennesEtudiants.put(etudiant.getId(), moyenne);
                    completeCount++;
                    sommeMoyennes += moyenne;
                    moyenneCount++;
                    if (moyenne >= 10.0) {
                        successCount++;
                    }
                }
            }

            int missingCount = etudiants.size() - completeCount;
            double moyenneClasse = moyenneCount > 0 ? sommeMoyennes / moyenneCount : Double.NaN;

            buildColumns(evaluations, notesByStudent, evaluationMoyennes, moyennesEtudiants, moyenneClasse);

            List<Etudiant> rows = new ArrayList<>(etudiants);
            rows.add(buildSummaryRow());
            table.setItems(FXCollections.observableArrayList(rows));

            updateSummary(etudiants.size(), completeCount, missingCount, successCount);
        } catch (Exception e) {
            showError(e);
        }
    }

    private void buildColumns(List<Evaluation> evaluations,
                              Map<Integer, Map<Integer, Note>> notesByStudent,
                              Map<Integer, Double> evaluationMoyennes,
                              Map<Integer, Double> moyennesEtudiants,
                              double moyenneClasse) {
        table.getColumns().clear();

        TableColumn<Etudiant, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(data -> {
            Etudiant etudiant = data.getValue();
            if (isSummaryRow(etudiant)) {
                return new SimpleStringProperty("Moyenne classe");
            }
            String nom = etudiant.getNom() != null ? etudiant.getNom() : "";
            return new SimpleStringProperty(nom);
        });

        TableColumn<Etudiant, String> colPrenom = new TableColumn<>("Prénom");
        colPrenom.setCellValueFactory(data -> {
            Etudiant etudiant = data.getValue();
            if (isSummaryRow(etudiant)) {
                return new SimpleStringProperty("");
            }
            String prenom = etudiant.getPrenom() != null ? etudiant.getPrenom() : "";
            return new SimpleStringProperty(prenom);
        });

        table.getColumns().add(colNom);
        table.getColumns().add(colPrenom);

        for (Evaluation evaluation : evaluations) {
            String header = evaluation.getLibelle() + "\n(coef " +
                    formatTwo(evaluation.getCoefficient()) + ")";
            TableColumn<Etudiant, String> col = new TableColumn<>(header);
            col.setCellValueFactory(data -> {
                Etudiant etudiant = data.getValue();
                if (isSummaryRow(etudiant)) {
                    Double moyenne = evaluationMoyennes.get(evaluation.getId());
                    return new SimpleStringProperty(formatNullable(moyenne));
                }
                Note note = getNoteForStudent(notesByStudent, etudiant, evaluation.getId());
                return new SimpleStringProperty(note != null ? formatTwo(note.getValeur()) : MISSING_NOTE_TEXT);
            });
            col.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                        return;
                    }
                    setText(item);
                    if (MISSING_NOTE_TEXT.equals(item)) {
                        setStyle("-fx-text-fill: #AAAAAA; -fx-font-style: italic;");
                    } else {
                        setStyle("");
                    }
                }
            });
            table.getColumns().add(col);
        }

        TableColumn<Etudiant, String> colMoyenne = new TableColumn<>("Moyenne");
        colMoyenne.setPrefWidth(120);
        colMoyenne.setCellValueFactory(data -> {
            Etudiant etudiant = data.getValue();
            if (isSummaryRow(etudiant)) {
                return new SimpleStringProperty(formatNullable(moyenneClasse));
            }
            Double moyenne = moyennesEtudiants.get(etudiant.getId());
            return new SimpleStringProperty(moyenne != null ? formatTwo(moyenne) : INCOMPLETE_TEXT);
        });
        colMoyenne.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                if (INCOMPLETE_TEXT.equals(item)) {
                    setStyle("-fx-text-fill: " + WARNING_COLOR + "; -fx-font-style: italic;");
                } else {
                    setStyle("");
                }
            }
        });
        table.getColumns().add(colMoyenne);

        equalizeColumnWidths();
    }

    private void equalizeColumnWidths() {
        int columnCount = table.getColumns().size();
        if (columnCount == 0) {
            return;
        }
        for (TableColumn<Etudiant, ?> column : table.getColumns()) {
            column.prefWidthProperty().unbind();
            column.minWidthProperty().unbind();
            column.maxWidthProperty().unbind();
            column.setMinWidth(80);
            column.setMaxWidth(Double.MAX_VALUE);
            column.prefWidthProperty().bind(
                    table.widthProperty().subtract(20).divide(columnCount)
            );
        }
    }

    private void updateSummary(int total, int completes, int missing, int successCount) {
        studentsLabel.setText("👥 Étudiants : " + total);
        completeLabel.setText("✓ Moyennes complètes : " + completes);
        missingLabel.setText("⚠️ Notes manquantes : " + missing + " étudiant(s)");

        String rate = completes > 0 ? formatTwo((successCount * 100.0) / completes) : "0.00";
        successLabel.setText("Taux de réussite : " + rate + "% (" +
                successCount + " étudiants >= 10 / " + completes + " étudiants avec moyenne complète)");

        summaryBox.setVisible(true);
        summaryBox.setManaged(true);
    }

    private void clearResults() {
        table.getItems().clear();
        table.getColumns().clear();
        summaryBox.setVisible(false);
        summaryBox.setManaged(false);
    }

    private void setEmptyState(String message) {
        clearResults();
        table.setPlaceholder(buildPlaceholderLabel(message));
    }

    private Note getNoteForStudent(Map<Integer, Map<Integer, Note>> notesByStudent,
                                   Etudiant etudiant,
                                   int evaluationId) {
        Map<Integer, Note> noteMap = notesByStudent.get(etudiant.getId());
        if (noteMap == null) {
            return null;
        }
        return noteMap.get(evaluationId);
    }

    private Etudiant buildSummaryRow() {
        Etudiant summary = new Etudiant();
        summary.setId(SUMMARY_ID);
        summary.setNom("Moyenne classe");
        summary.setPrenom("");
        summary.setCne("");
        return summary;
    }

    private boolean isSummaryRow(Etudiant etudiant) {
        return etudiant != null && etudiant.getId() == SUMMARY_ID;
    }

    private VBox buildFilterBox(String labelText, ComboBox<?> comboBox) {
        Label label = new Label(labelText);
        VBox box = new VBox(6, label, comboBox);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(comboBox, Priority.NEVER);
        return box;
    }

    private VBox buildButtonBox(Button button) {
        Label spacer = new Label(" ");
        spacer.setStyle("-fx-opacity: 0;");
        VBox box = new VBox(6, spacer, button);
        return box;
    }

    private Label buildPlaceholderLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TEXT_GRAY + "; -fx-font-style: italic;");
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private void configureClasseComboBox(ComboBox<Classe> comboBox) {
        comboBox.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Classe item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));
    }

    private void configureSemestreComboBox(ComboBox<Integer> comboBox) {
        comboBox.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "Semestre " + item);
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));
    }

    private void configureMatiereComboBox(ComboBox<Matiere> comboBox) {
        comboBox.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Matiere item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getIntitule());
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));
    }

    private String formatTwo(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatNullable(Double value) {
        if (value == null || value.isNaN()) {
            return MISSING_NOTE_TEXT;
        }
        return formatTwo(value);
    }

    private void showError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Erreur inconnue";
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
