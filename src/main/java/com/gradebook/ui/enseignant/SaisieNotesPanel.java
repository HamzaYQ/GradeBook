package com.gradebook.ui.enseignant;

import com.gradebook.config.ServiceLocator;
import com.gradebook.model.Classe;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Etudiant;
import com.gradebook.model.Evaluation;
import com.gradebook.model.Matiere;
import com.gradebook.model.Note;
import com.gradebook.model.Session;
import com.gradebook.model.TypeEvaluation;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SaisieNotesPanel extends VBox {
    private static final String PANEL_BG = "#F5F7FA";
    private static final String TITLE_STYLE = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2E4057;";
    private static final String SUBTITLE_STYLE = "-fx-font-size: 14px; -fx-font-weight: bold;";
    private static final String INFO_STYLE = "-fx-text-fill: #666666; -fx-font-style: italic;";

    private static final String BTN_PRIMARY = "-fx-background-color: #2E4057; -fx-text-fill: white;";
    private static final String BTN_SECONDARY = "-fx-background-color: #3a5068; -fx-text-fill: white;";
    private static final String BTN_SAVE = "-fx-background-color: #27AE60; -fx-text-fill: white;";

    private static final String ERR_CHAMPS = "Veuillez remplir tous les champs";
    private static final String ERR_COEFF = "Le coefficient doit être supérieur à 0";

    private final Enseignant enseignant;

    private final ComboBox<Classe> cbClasse = new ComboBox<>();
    private final ComboBox<Integer> cbSemestre = new ComboBox<>();
    private final ComboBox<Matiere> cbMatiere = new ComboBox<>();
    private final ComboBox<Evaluation> cbEvaluation = new ComboBox<>();

    private final TableView<NoteRow> table = new TableView<>();
    private final Label infoLabel = new Label();
    private final Label summaryLabel = new Label("Notes saisies : 0 / 0 étudiants");
    private final VBox tableSection = new VBox(10);

    private final Map<Integer, Double> notesModifiees = new HashMap<>();
    private final Map<Integer, Boolean> notesExistantes = new HashMap<>();

    public SaisieNotesPanel(Enseignant enseignant) {
        this.enseignant = enseignant;

        setPadding(new Insets(25));
        setSpacing(15);
        setStyle("-fx-background-color: " + PANEL_BG + ";");

        Label title = new Label("Saisie des Notes");
        title.setStyle(TITLE_STYLE);
        VBox.setMargin(title, new Insets(0, 0, 15, 0));

        VBox selectionBox = buildSelectionSection();
        buildTableSection();

        getChildren().addAll(title, selectionBox, tableSection);

        tableSection.setVisible(false);
        tableSection.setManaged(false);

        loadClasses();
    }

    private VBox buildSelectionSection() {
        VBox box = new VBox(12);

        Label subtitle = new Label("Sélectionner une évaluation");
        subtitle.setStyle(SUBTITLE_STYLE);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);

        configureClasseComboBox(cbClasse);
        configureSemestreComboBox(cbSemestre);
        configureMatiereComboBox(cbMatiere);
        configureEvaluationComboBox(cbEvaluation);

        cbClasse.valueProperty().addListener((obs, oldValue, newValue) -> {
            loadSemestresForClasse(newValue);
            clearTableSection();
        });
        cbSemestre.valueProperty().addListener((obs, oldValue, newValue) -> {
            loadMatieresForClasseAndSemestre(cbClasse.getValue(), newValue);
            clearTableSection();
        });
        cbMatiere.valueProperty().addListener((obs, oldValue, newValue) -> {
            loadEvaluationsForMatiere(cbClasse.getValue(), newValue, cbSemestre.getValue());
            clearTableSection();
        });
        cbEvaluation.valueProperty().addListener((obs, oldValue, newValue) -> clearTableSection());

        grid.add(new Label("Classe :"), 0, 0);
        grid.add(cbClasse, 1, 0);
        grid.add(new Label("Semestre :"), 0, 1);
        grid.add(cbSemestre, 1, 1);
        grid.add(new Label("Matière :"), 0, 2);
        grid.add(cbMatiere, 1, 2);
        grid.add(new Label("Évaluation :"), 0, 3);
        grid.add(cbEvaluation, 1, 3);

        GridPane.setHgrow(cbClasse, Priority.ALWAYS);
        GridPane.setHgrow(cbSemestre, Priority.ALWAYS);
        GridPane.setHgrow(cbMatiere, Priority.ALWAYS);
        GridPane.setHgrow(cbEvaluation, Priority.ALWAYS);

        Button btnCharger = new Button("📋 Charger les étudiants");
        btnCharger.setStyle(BTN_PRIMARY);
        btnCharger.disableProperty().bind(
                cbClasse.valueProperty().isNull()
                .or(cbSemestre.valueProperty().isNull())
                        .or(cbMatiere.valueProperty().isNull())
                        .or(cbEvaluation.valueProperty().isNull())
        );
        btnCharger.setOnAction(event -> handleChargerEtudiants());

        Button btnCreerEvaluation = new Button("➕ Créer une évaluation");
        btnCreerEvaluation.setStyle(BTN_SECONDARY);
        btnCreerEvaluation.disableProperty().bind(
            cbClasse.valueProperty().isNull()
                .or(cbSemestre.valueProperty().isNull())
                .or(cbMatiere.valueProperty().isNull())
        );
        btnCreerEvaluation.setOnAction(event -> handleCreerEvaluation());

        HBox actions = new HBox(10, btnCharger, btnCreerEvaluation);
        actions.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(subtitle, grid, actions);
        return box;
    }

    private void buildTableSection() {
        infoLabel.setStyle(INFO_STYLE);

        TableColumn<NoteRow, String> colCne = new TableColumn<>("CNE");
        colCne.setPrefWidth(100);
        colCne.setCellValueFactory(data -> data.getValue().cneProperty());

        TableColumn<NoteRow, String> colNom = new TableColumn<>("Nom");
        colNom.setPrefWidth(130);
        colNom.setCellValueFactory(data -> data.getValue().nomProperty());

        TableColumn<NoteRow, String> colPrenom = new TableColumn<>("Prénom");
        colPrenom.setPrefWidth(130);
        colPrenom.setCellValueFactory(data -> data.getValue().prenomProperty());

        TableColumn<NoteRow, String> colNote = new TableColumn<>("Note /20");
        colNote.setPrefWidth(120);
        colNote.setEditable(true);
        colNote.setCellValueFactory(data -> data.getValue().noteTextProperty());
        colNote.setCellFactory(column -> new NoteEditingCell());

        TableColumn<NoteRow, String> colStatut = new TableColumn<>("Statut");
        colStatut.setPrefWidth(100);
        colStatut.setCellValueFactory(data -> data.getValue().statutProperty());
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                if (item.startsWith("✓")) {
                    setStyle("-fx-text-fill: #27AE60;");
                } else {
                    setStyle("-fx-text-fill: #7F8C8D;");
                }
            }
        });

        table.getColumns().setAll(colCne, colNom, colPrenom, colNote, colStatut);
        table.setEditable(true);
        VBox.setVgrow(table, Priority.ALWAYS);

        Button btnSave = new Button("💾 Enregistrer toutes les notes");
        btnSave.setStyle(BTN_SAVE);
        btnSave.setOnAction(event -> handleEnregistrerNotes());

        Button btnReset = new Button("🔄 Réinitialiser");
        btnReset.setOnAction(event -> handleResetNotes());

        HBox actions = new HBox(10, btnSave, btnReset);
        actions.setAlignment(Pos.CENTER_LEFT);

        tableSection.getChildren().addAll(infoLabel, table, actions, summaryLabel);
    }

    private void loadClasses() {
        try {
            List<Classe> classes = ServiceLocator.getReferentielService()
                    .getClassesByEnseignant(enseignant.getId());
            cbClasse.setItems(FXCollections.observableArrayList(classes));
            cbSemestre.getItems().clear();
            cbSemestre.setValue(null);
            cbMatiere.getItems().clear();
            cbEvaluation.getItems().clear();
        } catch (Exception e) {
            showError(e);
        }
    }

    private void loadSemestresForClasse(Classe classe) {
        cbSemestre.getItems().clear();
        cbSemestre.setValue(null);
        cbMatiere.getItems().clear();
        cbEvaluation.getItems().clear();
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
        cbEvaluation.getItems().clear();
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

    private void loadEvaluationsForMatiere(Classe classe, Matiere matiere, Integer semestre) {
        cbEvaluation.getItems().clear();
        if (classe == null || matiere == null || semestre == null) {
            return;
        }
        try {
            List<Evaluation> evaluations = ServiceLocator.getEvaluationDao()
                    .findByClasseAndMatiereBySemestre(classe.getId(), matiere.getId(), semestre);
            cbEvaluation.setItems(FXCollections.observableArrayList(evaluations));
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleChargerEtudiants() {
        Classe classe = cbClasse.getValue();
        Matiere matiere = cbMatiere.getValue();
        Evaluation evaluation = cbEvaluation.getValue();
        if (classe == null || matiere == null || evaluation == null) {
            showWarning(ERR_CHAMPS);
            return;
        }

        try {
            notesModifiees.clear();
            notesExistantes.clear();

            List<Etudiant> etudiants = ServiceLocator.getReferentielService().getEtudiantsByClasse(classe.getId());
            List<NoteRow> rows = new ArrayList<>();

            for (Etudiant etudiant : etudiants) {
                boolean exists = ServiceLocator.getNoteService().noteExiste(etudiant.getId(), evaluation.getId());
                notesExistantes.put(etudiant.getId(), exists);

                String noteText = "";
                if (exists) {
                    List<Note> notes = ServiceLocator.getNoteService().getNotesByEtudiant(etudiant.getId());
                    Double valeur = findNoteValeur(notes, evaluation.getId());
                    if (valeur != null) {
                        noteText = formatTwo(valeur);
                    }
                }

                NoteRow row = new NoteRow(etudiant, noteText, true);
                row.updateStatut(exists, false);
                rows.add(row);
            }

            table.setItems(FXCollections.observableArrayList(rows));

            infoLabel.setText("Évaluation : " + evaluation.getLibelle() + " — " +
                    "Classe : " + classe.getNom() + " — " +
                    "Matière : " + matiere.getIntitule() + " — " +
                    "Coefficient : " + formatTwo(evaluation.getCoefficient()));

            updateSummary();
            table.refresh();

            tableSection.setVisible(true);
            tableSection.setManaged(true);
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleCreerEvaluation() {
        Classe classe = cbClasse.getValue();
        Matiere matiere = cbMatiere.getValue();
        if (classe == null || matiere == null) {
            showWarning(ERR_CHAMPS);
            return;
        }

        Optional<Evaluation> result = showEvaluationDialog(classe, matiere, cbSemestre.getValue());
        if (result.isEmpty()) {
            return;
        }

        try {
            ServiceLocator.getEvaluationDao().create(result.get());
            showInfo("Évaluation créée avec succès !");
            loadEvaluationsForMatiere(classe, matiere, cbSemestre.getValue());
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleEnregistrerNotes() {
        Evaluation evaluation = cbEvaluation.getValue();
        if (evaluation == null) {
            showWarning(ERR_CHAMPS);
            return;
        }

        int successCount = 0;
        for (Map.Entry<Integer, Double> entry : notesModifiees.entrySet()) {
            int idEtudiant = entry.getKey();
            double valeur = entry.getValue();
            try {
                if (notesExistantes.getOrDefault(idEtudiant, false)) {
                    ServiceLocator.getNoteService().modifierNote(
                            idEtudiant,
                            evaluation.getId(),
                            enseignant.getId(),
                            valeur
                    );
                } else {
                    ServiceLocator.getNoteService().saisirNote(
                            idEtudiant,
                            evaluation.getId(),
                            enseignant.getId(),
                            valeur
                    );
                }
                successCount++;
            } catch (Exception e) {
                showError(e);
                return;
            }
        }

        showInfo(successCount + " notes enregistrées avec succès !");
        handleChargerEtudiants();
        notesModifiees.clear();
    }

    private void handleResetNotes() {
        notesModifiees.clear();
        handleChargerEtudiants();
    }

    private Optional<Evaluation> showEvaluationDialog(Classe classe, Matiere matiere, Integer selectedSemestre) {
        Dialog<Evaluation> dialog = new Dialog<>();
        dialog.setTitle("Créer une évaluation");

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField libelleField = new TextField();
        ComboBox<TypeEvaluation> typeBox = new ComboBox<>();
        typeBox.setItems(FXCollections.observableArrayList(TypeEvaluation.values()));
        configureTypeComboBox(typeBox);

        ComboBox<Session> sessionBox = new ComboBox<>();
        sessionBox.setItems(FXCollections.observableArrayList(Session.values()));
        configureSessionComboBox(sessionBox);

        int semestre = selectedSemestre != null ? selectedSemestre : 1;
        Label semestreInfo = new Label("Semestre : Semestre " + semestre);
        semestreInfo.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");

        TextField coeffField = new TextField();
        DatePicker datePicker = new DatePicker();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Libellé :"), 0, 0);
        grid.add(libelleField, 1, 0);
        grid.add(new Label("Type :"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Session :"), 0, 2);
        grid.add(sessionBox, 1, 2);
        grid.add(semestreInfo, 1, 3);
        grid.add(new Label("Coefficient :"), 0, 4);
        grid.add(coeffField, 1, 4);
        grid.add(new Label("Date :"), 0, 5);
        grid.add(datePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (isBlank(libelleField.getText()) || typeBox.getValue() == null || sessionBox.getValue() == null ||
                    isBlank(coeffField.getText()) || datePicker.getValue() == null) {
                showWarning(ERR_CHAMPS);
                event.consume();
                return;
            }
            Double coefficient = parseDouble(coeffField.getText());
            if (coefficient == null || coefficient <= 0) {
                showWarning(ERR_COEFF);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                int selected = cbSemestre.getValue() != null ? cbSemestre.getValue() : semestre;
                Evaluation evaluation = new Evaluation();
                evaluation.setLibelle(libelleField.getText().trim());
                evaluation.setType(typeBox.getValue());
                evaluation.setSession(sessionBox.getValue());
                evaluation.setSemestre(selected);
                evaluation.setCoefficient(Double.parseDouble(coeffField.getText().trim()));
                evaluation.setDateSession(datePicker.getValue());
                evaluation.setMatiere(matiere);
                evaluation.setClasse(classe);
                evaluation.setEnseignant(enseignant);
                return evaluation;
            }
            return null;
        });

        return dialog.showAndWait();
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
        comboBox.setMaxWidth(Double.MAX_VALUE);
    }

    private void configureMatiereComboBox(ComboBox<Matiere> comboBox) {
        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Matiere item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getIntitule());
                }
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));
        comboBox.setMaxWidth(Double.MAX_VALUE);
    }

    private void configureEvaluationComboBox(ComboBox<Evaluation> comboBox) {
        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Evaluation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatEvaluation(item));
                }
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));
        comboBox.setMaxWidth(Double.MAX_VALUE);
    }

    private void configureTypeComboBox(ComboBox<TypeEvaluation> comboBox) {
        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(TypeEvaluation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLibelle());
                }
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));
    }

    private void configureSessionComboBox(ComboBox<Session> comboBox) {
        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Session item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getLibelle());
                }
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));
    }

    private void clearTableSection() {
        notesModifiees.clear();
        notesExistantes.clear();
        table.getItems().clear();
        tableSection.setVisible(false);
        tableSection.setManaged(false);
        summaryLabel.setText("Notes saisies : 0 / 0 étudiants");
    }

    private void updateSummary() {
        int total = table.getItems() != null ? table.getItems().size() : 0;
        Set<Integer> saisies = new HashSet<>();
        for (NoteRow row : table.getItems()) {
            int idEtudiant = row.getEtudiant().getId();
            if (notesExistantes.getOrDefault(idEtudiant, false) || notesModifiees.containsKey(idEtudiant)) {
                saisies.add(idEtudiant);
            }
        }
        summaryLabel.setText("Notes saisies : " + saisies.size() + " / " + total + " étudiants");
    }

    private void applyNoteChange(NoteRow row, String newValue) {
        if (row == null) {
            return;
        }
        String value = newValue != null ? newValue.trim() : "";
        if (value.isEmpty()) {
            row.setValid(true);
            row.setNoteText("");
            notesModifiees.remove(row.getEtudiant().getId());
        } else if (isValidNoteValue(value)) {
            double val = Double.parseDouble(value);
            row.setValid(true);
            row.setNoteText(formatTwo(val));
            notesModifiees.put(row.getEtudiant().getId(), val);
        } else {
            row.setValid(false);
            row.setNoteText(value);
            notesModifiees.remove(row.getEtudiant().getId());
        }
        row.updateStatut(notesExistantes.getOrDefault(row.getEtudiant().getId(), false),
                notesModifiees.containsKey(row.getEtudiant().getId()));
        updateSummary();
        table.refresh();
    }

    private boolean isValidNoteValue(String value) {
        Double val = parseDouble(value);
        return val != null && val >= 0.0 && val <= 20.0;
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double findNoteValeur(List<Note> notes, int idEvaluation) {
        if (notes == null) {
            return null;
        }
        for (Note note : notes) {
            if (note.getEvaluation() != null && note.getEvaluation().getId() == idEvaluation) {
                return note.getValeur();
            }
        }
        return null;
    }

    private String formatEvaluation(Evaluation evaluation) {
        String libelle = evaluation.getLibelle() != null ? evaluation.getLibelle() : "";
        String type = evaluation.getType() != null ? evaluation.getType().getLibelle() : "";
        String session = evaluation.getSession() != null ? evaluation.getSession().getLibelle() : "";
        return libelle + " (" + type + " — " + session + ")";
    }

    private String formatTwo(double value) {
        double rounded = Math.round(value * 100.0) / 100.0;
        return String.format(Locale.US, "%.2f", rounded);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Erreur inconnue";
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private class NoteEditingCell extends TableCell<NoteRow, String> {
        private TextField textField;

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                if (textField == null) {
                    createTextField();
                }
                textField.setText(getItem());
                setText(null);
                setGraphic(textField);
                textField.selectAll();
                updateTextFieldStyle(textField.getText());
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
            updateCellStyle();
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
                setStyle("");
                return;
            }
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(item);
                    updateTextFieldStyle(item);
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(item);
                setGraphic(null);
                updateCellStyle();
            }
        }

        @Override
        public void commitEdit(String newValue) {
            if (!isEditing()) {
                return;
            }
            super.commitEdit(newValue);
            NoteRow row = getTableRow() != null ? getTableRow().getItem() : null;
            applyNoteChange(row, newValue);
            setGraphic(null);
        }

        private void createTextField() {
            textField = new TextField(getItem());
            textField.textProperty().addListener((obs, oldValue, newValue) -> updateTextFieldStyle(newValue));
            textField.setOnAction(event -> commitEdit(textField.getText()));
            textField.focusedProperty().addListener((obs, oldValue, focused) -> {
                if (!focused) {
                    commitEdit(textField.getText());
                }
            });
        }

        private void updateTextFieldStyle(String value) {
            if (value == null || value.trim().isEmpty()) {
                textField.setStyle("-fx-background-color: white;");
                return;
            }
            if (isValidNoteValue(value)) {
                textField.setStyle("-fx-background-color: white;");
            } else {
                textField.setStyle("-fx-background-color: #FFCCCC;");
            }
        }

        private void updateCellStyle() {
            NoteRow row = getTableRow() != null ? getTableRow().getItem() : null;
            if (row == null) {
                setStyle("");
                return;
            }
            if (!row.isValid() && !isBlank(row.getNoteText())) {
                setStyle("-fx-background-color: #FFCCCC;");
            } else {
                setStyle("");
            }
        }
    }

    private static class NoteRow {
        private final Etudiant etudiant;
        private final javafx.beans.property.SimpleStringProperty cne;
        private final javafx.beans.property.SimpleStringProperty nom;
        private final javafx.beans.property.SimpleStringProperty prenom;
        private final javafx.beans.property.SimpleStringProperty noteText;
        private final javafx.beans.property.SimpleStringProperty statut;
        private boolean valid;

        private NoteRow(Etudiant etudiant, String noteText, boolean valid) {
            this.etudiant = etudiant;
            this.cne = new javafx.beans.property.SimpleStringProperty(
                    etudiant != null ? etudiant.getCne() : "");
            this.nom = new javafx.beans.property.SimpleStringProperty(
                    etudiant != null ? etudiant.getNom() : "");
            this.prenom = new javafx.beans.property.SimpleStringProperty(
                    etudiant != null ? etudiant.getPrenom() : "");
            this.noteText = new javafx.beans.property.SimpleStringProperty(noteText);
            this.statut = new javafx.beans.property.SimpleStringProperty();
            this.valid = valid;
            updateStatut(false, false);
        }

        public Etudiant getEtudiant() {
            return etudiant;
        }

        public javafx.beans.property.SimpleStringProperty cneProperty() {
            return cne;
        }

        public javafx.beans.property.SimpleStringProperty nomProperty() {
            return nom;
        }

        public javafx.beans.property.SimpleStringProperty prenomProperty() {
            return prenom;
        }

        public javafx.beans.property.SimpleStringProperty noteTextProperty() {
            return noteText;
        }

        public javafx.beans.property.SimpleStringProperty statutProperty() {
            return statut;
        }

        public String getNoteText() {
            return noteText.get();
        }

        public void setNoteText(String value) {
            noteText.set(value);
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public void updateStatut(boolean noteExiste, boolean noteModifiee) {
            if (noteExiste || noteModifiee) {
                statut.set("✓ Saisie");
            } else {
                statut.set("— Non saisie");
            }
        }
    }
}
