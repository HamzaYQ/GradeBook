package com.gradebook.ui.administration;

import com.gradebook.config.ServiceLocator;
import com.gradebook.model.Classe;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Etudiant;
import com.gradebook.model.Matiere;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ReferentielPanel extends VBox {
    private static final String TITLE_STYLE = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2E4057;";
    private static final String PANEL_STYLE = "-fx-background-color: white; -fx-background-radius: 8;";

    private static final String BTN_ADD_STYLE = "-fx-background-color: #2E4057; -fx-text-fill: white;";
    private static final String BTN_EDIT_STYLE = "-fx-background-color: #3a5068; -fx-text-fill: white;";
    private static final String BTN_DELETE_STYLE = "-fx-background-color: #E74C3C; -fx-text-fill: white;";

    private static final int DIALOG_WIDTH = 600;
    private static final int DIALOG_HEIGHT = 450;

    private static final String ERR_CHAMPS = "Veuillez remplir tous les champs";
    private static final String ERR_COEFF = "Le coefficient doit être un nombre supérieur à 0";
    private static final String ERR_DELETE_CLASSE = "Impossible de supprimer : des étudiants sont rattachés à cette classe";
    private static final String ERR_DELETE_MATIERE = "Impossible de supprimer : des évaluations sont rattachées à cette matière";
    private static final String ERR_EMAIL = "Email invalide";
    private static final String ERR_CLASSE = "Veuillez sélectionner une classe";
    private static final String ERR_DELETE_ETUDIANT =
            "Impossible de supprimer : des notes sont rattachées à cet étudiant";
        private static final String ERR_DELETE_ENSEIGNANT =
            "Impossible de supprimer : cet enseignant possède des affectations ou des notes saisies";

    private static final String ALL_CLASSES_LABEL = "Toutes les classes";
    private static final String MASK_PASSWORD = "********";

    private final TableView<Classe> classesTable = new TableView<>();
    private final TableView<Matiere> matieresTable = new TableView<>();
    private final TableView<Etudiant> etudiantsTable = new TableView<>();
    private final TableView<Enseignant> enseignantsTable = new TableView<>();

    private final ComboBox<Classe> etudiantsFilter = new ComboBox<>();

    private final Map<Integer, Integer> classeMatiereCounts = new HashMap<>();
    private final Map<Integer, Integer> classeEtudiantCounts = new HashMap<>();
    private final Map<Integer, Integer> matiereClasseCounts = new HashMap<>();
    private final Map<Integer, String> enseignantClassesLabels = new HashMap<>();

    public ReferentielPanel() {
        setPadding(new Insets(25));
        setSpacing(15);
        setStyle(PANEL_STYLE);

        Label title = new Label("Référentiel Scolaire");
        title.setStyle(TITLE_STYLE);
        VBox.setMargin(title, new Insets(0, 0, 15, 0));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().add(buildClassesTab());
        tabPane.getTabs().add(buildMatieresTab());
        tabPane.getTabs().add(buildEtudiantsTab());
        tabPane.getTabs().add(buildEnseignantsTab());

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        getChildren().addAll(title, tabPane);

        loadClasses();
        loadMatieres();
        loadEtudiants();
        loadEnseignants();
    }

    private Tab buildClassesTab() {
        Tab tab = new Tab("Classes");

        classesTable.getColumns().setAll(
                createColumn("ID", 60, classe -> String.valueOf(classe.getId())),
                createColumn("Nom", 150, Classe::getNom),
                createColumn("Niveau", 150, Classe::getNiveau),
            createColumn("Année académique", 150, Classe::getAnneeAcademique),
            createColumn("Matières", 120, classe -> formatMatiereCount(classeMatiereCounts.get(classe.getId()))),
            createColumn("Étudiants", 120, classe -> formatEtudiantCount(classeEtudiantCounts.get(classe.getId())))
        );

        Button addButton = new Button("➕ Ajouter classe");
        addButton.setStyle(BTN_ADD_STYLE);
        addButton.setOnAction(event -> handleAddClasse());

        Button editButton = new Button("✏️ Modifier");
        editButton.setStyle(BTN_EDIT_STYLE);
        editButton.disableProperty().bind(classesTable.getSelectionModel().selectedItemProperty().isNull());
        editButton.setOnAction(event -> handleEditClasse());

        Button deleteButton = new Button("🗑️ Supprimer");
        deleteButton.setStyle(BTN_DELETE_STYLE);
        deleteButton.disableProperty().bind(classesTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.setOnAction(event -> handleDeleteClasse());

        Button manageButton = new Button("📚 Gérer les matières");
        manageButton.setStyle(BTN_EDIT_STYLE);
        manageButton.disableProperty().bind(classesTable.getSelectionModel().selectedItemProperty().isNull());
        manageButton.setOnAction(event -> handleManageMatieres());

        Button viewStudentsButton = new Button("👥 Voir les étudiants");
        viewStudentsButton.setStyle(BTN_EDIT_STYLE);
        viewStudentsButton.disableProperty().bind(classesTable.getSelectionModel().selectedItemProperty().isNull());
        viewStudentsButton.setOnAction(event -> handleViewEtudiants());

        Button viewTeachersButton = new Button("👨‍🏫 Voir les enseignants");
        viewTeachersButton.setStyle(BTN_EDIT_STYLE);
        viewTeachersButton.disableProperty().bind(classesTable.getSelectionModel().selectedItemProperty().isNull());
        viewTeachersButton.setOnAction(event -> handleViewEnseignants());

        HBox actions = new HBox(10, addButton, editButton, manageButton, viewStudentsButton,
            viewTeachersButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10, classesTable, actions);
        VBox.setVgrow(classesTable, Priority.ALWAYS);
        content.setPadding(new Insets(10));

        tab.setContent(content);
        return tab;
    }

    private Tab buildMatieresTab() {
        Tab tab = new Tab("Matières");

        matieresTable.getColumns().setAll(
                createColumn("ID", 60, matiere -> String.valueOf(matiere.getId())),
                createColumn("Intitulé", 250, Matiere::getIntitule),
            createColumn("Coefficient", 120, matiere -> formatTwo(matiere.getCoefficient())),
            createColumn("Classes associées", 150, matiere -> formatClasseCount(matiereClasseCounts.get(matiere.getId())))
        );

        Button addButton = new Button("➕ Ajouter matière");
        addButton.setStyle(BTN_ADD_STYLE);
        addButton.setOnAction(event -> handleAddMatiere());

        Button editButton = new Button("✏️ Modifier");
        editButton.setStyle(BTN_EDIT_STYLE);
        editButton.disableProperty().bind(matieresTable.getSelectionModel().selectedItemProperty().isNull());
        editButton.setOnAction(event -> handleEditMatiere());

        Button deleteButton = new Button("🗑️ Supprimer");
        deleteButton.setStyle(BTN_DELETE_STYLE);
        deleteButton.disableProperty().bind(matieresTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.setOnAction(event -> handleDeleteMatiere());

        HBox actions = new HBox(10, addButton, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10, matieresTable, actions);
        VBox.setVgrow(matieresTable, Priority.ALWAYS);
        content.setPadding(new Insets(10));

        tab.setContent(content);
        return tab;
    }

    private Tab buildEtudiantsTab() {
        Tab tab = new Tab("Étudiants");

        etudiantsTable.getColumns().setAll(
            createColumn("CNE", 100, Etudiant::getCne),
                createColumn("Nom", 130, Etudiant::getNom),
                createColumn("Prénom", 130, Etudiant::getPrenom),
                createColumn("Email", 200, Etudiant::getEmail),
                createColumn("Classe", 120, etudiant ->
                        etudiant.getClasse() != null ? etudiant.getClasse().getNom() : "")
        );

        configureClasseComboBox(etudiantsFilter);
        etudiantsFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyEtudiantsFilter(newValue));

        Button resetButton = new Button("🔄 Réinitialiser");
        resetButton.setOnAction(event -> resetEtudiantsFilter());

        HBox filters = new HBox(10, new Label("Filtrer par classe :"), etudiantsFilter, resetButton);
        filters.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(etudiantsFilter, Priority.ALWAYS);

        Button addButton = new Button("➕ Ajouter étudiant");
        addButton.setStyle(BTN_ADD_STYLE);
        addButton.setOnAction(event -> handleAddEtudiant());

        Button editButton = new Button("✏️ Modifier");
        editButton.setStyle(BTN_EDIT_STYLE);
        editButton.disableProperty().bind(etudiantsTable.getSelectionModel().selectedItemProperty().isNull());
        editButton.setOnAction(event -> handleEditEtudiant());

        Button deleteButton = new Button("🗑️ Supprimer");
        deleteButton.setStyle(BTN_DELETE_STYLE);
        deleteButton.disableProperty().bind(etudiantsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.setOnAction(event -> handleDeleteEtudiant());

        HBox actions = new HBox(10, addButton, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10, filters, etudiantsTable, actions);
        VBox.setVgrow(etudiantsTable, Priority.ALWAYS);
        content.setPadding(new Insets(10));

        tab.setContent(content);
        return tab;
    }

    private Tab buildEnseignantsTab() {
        Tab tab = new Tab("Enseignants");

        enseignantsTable.getColumns().setAll(
                createColumn("Nom", 130, Enseignant::getNom),
                createColumn("Prénom", 130, Enseignant::getPrenom),
                createColumn("Email", 200, Enseignant::getEmail),
                createColumn("Classes affectées", 200, enseignant ->
                        enseignantClassesLabels.getOrDefault(enseignant.getId(), ""))
        );

        Button addButton = new Button("➕ Ajouter enseignant");
        addButton.setStyle(BTN_ADD_STYLE);
        addButton.setOnAction(event -> handleAddEnseignant());

        Button editButton = new Button("✏️ Modifier");
        editButton.setStyle(BTN_EDIT_STYLE);
        editButton.disableProperty().bind(enseignantsTable.getSelectionModel().selectedItemProperty().isNull());
        editButton.setOnAction(event -> handleEditEnseignant());

        Button deleteButton = new Button("🗑️ Supprimer");
        deleteButton.setStyle(BTN_DELETE_STYLE);
        deleteButton.disableProperty().bind(enseignantsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.setOnAction(event -> handleDeleteEnseignant());

        HBox actions = new HBox(10, addButton, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10, enseignantsTable, actions);
        VBox.setVgrow(enseignantsTable, Priority.ALWAYS);
        content.setPadding(new Insets(10));

        tab.setContent(content);
        return tab;
    }

    private <T> TableColumn<T, String> createColumn(String title, double width, Function<T, String> mapper) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        return column;
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

    private void loadClasses() {
        try {
            List<Classe> classes = ServiceLocator.getReferentielService().getAllClasses();
            classeMatiereCounts.clear();
            classeEtudiantCounts.clear();
            for (Classe classe : classes) {
                int count = ServiceLocator.getReferentielService().getMatieresByClasse(classe.getId()).size();
                classeMatiereCounts.put(classe.getId(), count);
                int etudiantCount = ServiceLocator.getReferentielService().getEtudiantsByClasse(classe.getId()).size();
                classeEtudiantCounts.put(classe.getId(), etudiantCount);
            }
            classesTable.setItems(javafx.collections.FXCollections.observableArrayList(classes));
        } catch (Exception e) {
            showError(e);
        }
    }

    private void loadMatieres() {
        try {
            List<Matiere> matieres = ServiceLocator.getReferentielService().getAllMatieres();
            matiereClasseCounts.clear();
            for (Matiere matiere : matieres) {
                int count = ServiceLocator.getReferentielService().getClassesByMatiere(matiere.getId()).size();
                matiereClasseCounts.put(matiere.getId(), count);
            }
            matieresTable.setItems(javafx.collections.FXCollections.observableArrayList(matieres));
        } catch (Exception e) {
            showError(e);
        }
    }

    private void loadEtudiants() {
        try {
            List<Classe> classes = ServiceLocator.getReferentielService().getAllClasses();
            List<Classe> filterClasses = new ArrayList<>();
            Classe all = new Classe();
            all.setId(0);
            all.setNom(ALL_CLASSES_LABEL);
            filterClasses.add(all);
            filterClasses.addAll(classes);

            Classe selected = etudiantsFilter.getValue();
            etudiantsFilter.setItems(javafx.collections.FXCollections.observableArrayList(filterClasses));
            if (selected == null) {
                etudiantsFilter.setValue(all);
            } else {
                Classe matched = null;
                for (Classe classe : filterClasses) {
                    if (classe.getId() == selected.getId()) {
                        matched = classe;
                        break;
                    }
                }
                etudiantsFilter.setValue(matched != null ? matched : all);
            }

            applyEtudiantsFilter(etudiantsFilter.getValue());
        } catch (Exception e) {
            showError(e);
        }
    }

    private void loadEnseignants() {
        try {
            List<Enseignant> enseignants = ServiceLocator.getReferentielService().getAllEnseignants();
            enseignantClassesLabels.clear();
            for (Enseignant enseignant : enseignants) {
                List<Classe> classes = ServiceLocator.getReferentielService()
                        .getClassesByEnseignant(enseignant.getId());
                enseignantClassesLabels.put(enseignant.getId(), joinClasseLabels(classes));
            }
            enseignantsTable.setItems(javafx.collections.FXCollections.observableArrayList(enseignants));
        } catch (Exception e) {
            showError(e);
        }
    }

    private void applyEtudiantsFilter(Classe selected) {
        try {
            List<Etudiant> etudiants;
            if (selected == null || selected.getId() == 0) {
                etudiants = ServiceLocator.getReferentielService().getAllEtudiants();
            } else {
                etudiants = ServiceLocator.getReferentielService().getEtudiantsByClasse(selected.getId());
            }
            etudiantsTable.setItems(javafx.collections.FXCollections.observableArrayList(etudiants));
        } catch (Exception e) {
            showError(e);
        }
    }

    private void resetEtudiantsFilter() {
        if (!etudiantsFilter.getItems().isEmpty()) {
            etudiantsFilter.setValue(etudiantsFilter.getItems().get(0));
        }
    }

    private void handleAddClasse() {
        Optional<Classe> result = showClasseDialog(null);
        if (result.isEmpty()) {
            return;
        }
        Classe classe = result.get();
        try {
            ServiceLocator.getReferentielService().creerClasse(classe.getNom(), classe.getNiveau(), classe.getAnneeAcademique());
            loadClasses();
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleEditClasse() {
        Classe selected = classesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Optional<Classe> result = showClasseDialog(selected);
        if (result.isEmpty()) {
            return;
        }
        Classe updated = result.get();
        updated.setId(selected.getId());
        try {
            ServiceLocator.getReferentielService().modifierClasse(updated);
            loadClasses();
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleDeleteClasse() {
        Classe selected = classesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la classe " + selected.getNom() + " ?", ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        try {
            ServiceLocator.getReferentielService().supprimerClasse(selected.getId());
            loadClasses();
        } catch (Exception e) {
            showErrorMessage(ERR_DELETE_CLASSE);
        }
    }

    private void handleManageMatieres() {
        Classe selected = classesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        showGestionMatieresDialog(selected);
    }

    private void handleViewEtudiants() {
        Classe selected = classesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        showEtudiantsClasseDialog(selected);
    }

    private void handleViewEnseignants() {
        Classe selected = classesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        showEnseignantsClasseDialog(selected);
    }

    private void handleAddMatiere() {
        Optional<Matiere> result = showMatiereDialog(null);
        if (result.isEmpty()) {
            return;
        }
        Matiere matiere = result.get();
        try {
            ServiceLocator.getReferentielService().creerMatiere(matiere.getIntitule(), matiere.getCoefficient());
            loadMatieres();
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleEditMatiere() {
        Matiere selected = matieresTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Optional<Matiere> result = showMatiereDialog(selected);
        if (result.isEmpty()) {
            return;
        }
        Matiere updated = result.get();
        updated.setId(selected.getId());
        try {
            ServiceLocator.getReferentielService().modifierMatiere(updated);
            loadMatieres();
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleDeleteMatiere() {
        Matiere selected = matieresTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la matière " + selected.getIntitule() + " ?", ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        try {
            ServiceLocator.getReferentielService().supprimerMatiere(selected.getId());
            loadMatieres();
        } catch (Exception e) {
            showErrorMessage(e.getMessage() != null ? e.getMessage() : ERR_DELETE_MATIERE);
        }
    }

    private void handleAddEtudiant() {
        Optional<EtudiantFormResult> result = showEtudiantDialog(null);
        if (result.isEmpty()) {
            return;
        }

        EtudiantFormResult form = result.get();
        Etudiant etudiant = form.getEtudiant();
        String hashed = ServiceLocator.getAuthService().hasherMotDePasse(form.getMotDePasse());
        etudiant.setMotDePasse(hashed);
        try {
            ServiceLocator.getReferentielService().ajouterEtudiant(etudiant);
            showInfo("Étudiant " + etudiant.getNom() + " " + etudiant.getPrenom() +
                    " ajouté avec succès\n" +
                    "dans la classe " + etudiant.getClasse().getNom());
            loadEtudiants();
            loadClasses();
        } catch (IllegalStateException e) {
            showWarning(e.getMessage());
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleEditEtudiant() {
        Etudiant selected = etudiantsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Optional<EtudiantFormResult> result = showEtudiantDialog(selected);
        if (result.isEmpty()) {
            return;
        }

        EtudiantFormResult form = result.get();
        Etudiant updated = form.getEtudiant();
        updated.setId(selected.getId());

        if (MASK_PASSWORD.equals(form.getMotDePasse())) {
            updated.setMotDePasse(selected.getMotDePasse());
        } else {
            String hashed = ServiceLocator.getAuthService().hasherMotDePasse(form.getMotDePasse());
            updated.setMotDePasse(hashed);
        }

        try {
            ServiceLocator.getReferentielService().modifierEtudiant(updated);
            loadEtudiants();
            loadClasses();
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleDeleteEtudiant() {
        Etudiant selected = etudiantsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'étudiant " + selected.getNom() + " " + selected.getPrenom() +
                " (CNE : " + selected.getCne() + ") ?",
                ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            ServiceLocator.getReferentielService().supprimerEtudiant(selected.getId());
            showInfo("Étudiant supprimé avec succès");
            loadEtudiants();
            loadClasses();
        } catch (Exception e) {
            showErrorMessage(ERR_DELETE_ETUDIANT);
        }
    }

    private void handleAddEnseignant() {
        Optional<EnseignantFormResult> result = showEnseignantDialog(null);
        if (result.isEmpty()) {
            return;
        }

        EnseignantFormResult form = result.get();
        Enseignant enseignant = form.getEnseignant();
        String hashed = ServiceLocator.getAuthService().hasherMotDePasse(form.getMotDePasse());
        enseignant.setMotDePasse(hashed);
        try {
            ServiceLocator.getReferentielService().ajouterEnseignant(enseignant);
            showInfo("Enseignant " + enseignant.getNom() + " " + enseignant.getPrenom() +
                    " ajouté avec succès");
            loadEnseignants();
        } catch (IllegalStateException e) {
            showWarning(e.getMessage());
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleEditEnseignant() {
        Enseignant selected = enseignantsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Optional<EnseignantFormResult> result = showEnseignantDialog(selected);
        if (result.isEmpty()) {
            return;
        }

        EnseignantFormResult form = result.get();
        Enseignant updated = form.getEnseignant();
        updated.setId(selected.getId());

        if (MASK_PASSWORD.equals(form.getMotDePasse())) {
            updated.setMotDePasse(selected.getMotDePasse());
        } else {
            String hashed = ServiceLocator.getAuthService().hasherMotDePasse(form.getMotDePasse());
            updated.setMotDePasse(hashed);
        }

        try {
            ServiceLocator.getReferentielService().modifierEnseignant(updated);
            loadEnseignants();
        } catch (Exception e) {
            showError(e);
        }
    }

    private void handleDeleteEnseignant() {
        Enseignant selected = enseignantsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'enseignant " + selected.getNom() + " " + selected.getPrenom() + " ?",
                ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            ServiceLocator.getReferentielService().supprimerEnseignant(selected.getId());
            showInfo("Enseignant supprimé avec succès");
            loadEnseignants();
        } catch (Exception e) {
            showErrorMessage(ERR_DELETE_ENSEIGNANT);
        }
    }

    private Optional<Classe> showClasseDialog(Classe existing) {
        Dialog<Classe> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvelle Classe" : "Modifier Classe");

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nomField = new TextField(existing != null ? existing.getNom() : "");
        TextField niveauField = new TextField(existing != null ? existing.getNiveau() : "");
        TextField anneeField = new TextField(existing != null ? existing.getAnneeAcademique() : "2025-2026");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Nom :"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Niveau :"), 0, 1);
        grid.add(niveauField, 1, 1);
        grid.add(new Label("Année académique :"), 0, 2);
        grid.add(anneeField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (isBlank(nomField.getText()) || isBlank(niveauField.getText()) || isBlank(anneeField.getText())) {
                showWarning(ERR_CHAMPS);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                Classe classe = new Classe();
                classe.setNom(nomField.getText().trim());
                classe.setNiveau(niveauField.getText().trim());
                classe.setAnneeAcademique(anneeField.getText().trim());
                return classe;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private Optional<Matiere> showMatiereDialog(Matiere existing) {
        Dialog<Matiere> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvelle Matière" : "Modifier Matière");

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField intituleField = new TextField(existing != null ? existing.getIntitule() : "");
        TextField coefField = new TextField(existing != null ? String.valueOf(existing.getCoefficient()) : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Intitulé :"), 0, 0);
        grid.add(intituleField, 1, 0);
        grid.add(new Label("Coefficient :"), 0, 1);
        grid.add(coefField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (isBlank(intituleField.getText()) || isBlank(coefField.getText())) {
                showWarning(ERR_CHAMPS);
                event.consume();
                return;
            }
            try {
                double coef = Double.parseDouble(coefField.getText().trim());
                if (coef <= 0) {
                    showWarning(ERR_COEFF);
                    event.consume();
                }
            } catch (NumberFormatException e) {
                showWarning(ERR_COEFF);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                double coef = Double.parseDouble(coefField.getText().trim());
                Matiere matiere = new Matiere();
                matiere.setIntitule(intituleField.getText().trim());
                matiere.setCoefficient(coef);
                return matiere;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private Optional<EtudiantFormResult> showEtudiantDialog(Etudiant existing) {
        Dialog<EtudiantFormResult> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvel étudiant" : "Modifier étudiant");

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nomField = new TextField(existing != null ? existing.getNom() : "");
        TextField prenomField = new TextField(existing != null ? existing.getPrenom() : "");
        TextField emailField = new TextField(existing != null ? existing.getEmail() : "");
        PasswordField passwordField = new PasswordField();
        passwordField.setText(existing != null ? MASK_PASSWORD : "");
        TextField cneField = new TextField(existing != null ? existing.getCne() : "");

        ComboBox<Classe> classeBox = new ComboBox<>();
        configureClasseComboBox(classeBox);
        try {
            List<Classe> classes = ServiceLocator.getReferentielService().getAllClasses();
            classeBox.setItems(javafx.collections.FXCollections.observableArrayList(classes));
            if (existing != null && existing.getClasse() != null) {
                for (Classe classe : classes) {
                    if (classe.getId() == existing.getClasse().getId()) {
                        classeBox.setValue(classe);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            showError(e);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Nom :"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom :"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email :"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Mot de passe :"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("CNE :"), 0, 4);
        grid.add(cneField, 1, 4);
        grid.add(new Label("Classe :"), 0, 5);
        grid.add(classeBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (isBlank(nomField.getText()) || isBlank(prenomField.getText()) ||
                    isBlank(emailField.getText()) || isBlank(passwordField.getText()) ||
                    isBlank(cneField.getText())) {
                showWarning(ERR_CHAMPS);
                event.consume();
                return;
            }
            if (!emailField.getText().contains("@")) {
                showWarning(ERR_EMAIL);
                event.consume();
                return;
            }
            if (classeBox.getValue() == null) {
                showWarning(ERR_CLASSE);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                Etudiant etudiant = new Etudiant();
                etudiant.setNom(nomField.getText().trim());
                etudiant.setPrenom(prenomField.getText().trim());
                etudiant.setEmail(emailField.getText().trim());
                etudiant.setCne(cneField.getText().trim());
                etudiant.setClasse(classeBox.getValue());
                return new EtudiantFormResult(etudiant, passwordField.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private Optional<EnseignantFormResult> showEnseignantDialog(Enseignant existing) {
        Dialog<EnseignantFormResult> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvel enseignant" : "Modifier enseignant");

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nomField = new TextField(existing != null ? existing.getNom() : "");
        TextField prenomField = new TextField(existing != null ? existing.getPrenom() : "");
        TextField emailField = new TextField(existing != null ? existing.getEmail() : "");
        PasswordField passwordField = new PasswordField();
        passwordField.setText(existing != null ? MASK_PASSWORD : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Nom :"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Prénom :"), 0, 1);
        grid.add(prenomField, 1, 1);
        grid.add(new Label("Email :"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Mot de passe :"), 0, 3);
        grid.add(passwordField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (isBlank(nomField.getText()) || isBlank(prenomField.getText()) ||
                    isBlank(emailField.getText()) || isBlank(passwordField.getText())) {
                showWarning(ERR_CHAMPS);
                event.consume();
                return;
            }
            if (!emailField.getText().contains("@")) {
                showWarning(ERR_EMAIL);
                event.consume();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == saveType) {
                Enseignant enseignant = new Enseignant();
                enseignant.setNom(nomField.getText().trim());
                enseignant.setPrenom(prenomField.getText().trim());
                enseignant.setEmail(emailField.getText().trim());
                return new EnseignantFormResult(enseignant, passwordField.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void showGestionMatieresDialog(Classe classe) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Matières de " + classe.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(DIALOG_WIDTH, DIALOG_HEIGHT);

        TableView<Matiere> associatedTable = new TableView<>();
        associatedTable.getColumns().setAll(
                createColumn("Intitulé", 220, Matiere::getIntitule),
                createColumn("Coefficient", 120, matiere -> formatTwo(matiere.getCoefficient()))
        );

        TableView<Matiere> availableTable = new TableView<>();
        availableTable.getColumns().setAll(
                createColumn("Intitulé", 220, Matiere::getIntitule),
                createColumn("Coefficient", 120, matiere -> formatTwo(matiere.getCoefficient()))
        );

        Button removeButton = new Button("🗑️ Retirer");
        removeButton.setStyle(BTN_DELETE_STYLE);
        removeButton.disableProperty().bind(associatedTable.getSelectionModel().selectedItemProperty().isNull());

        Button addButton = new Button("➕ Ajouter");
        addButton.setStyle(BTN_ADD_STYLE);
        addButton.disableProperty().bind(availableTable.getSelectionModel().selectedItemProperty().isNull());

        removeButton.setOnAction(event -> {
            Matiere selected = associatedTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Retirer la matière " + selected.getIntitule() + " ?", ButtonType.OK, ButtonType.CANCEL);
            confirm.setHeaderText(null);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
            try {
                ServiceLocator.getReferentielService().retirerMatiereDeClasse(classe.getId(), selected.getId());
                refreshGestionTables(classe, associatedTable, availableTable);
                loadClasses();
                loadMatieres();
            } catch (Exception e) {
                showError(e);
            }
        });

        addButton.setOnAction(event -> {
            Matiere selected = availableTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                ServiceLocator.getReferentielService().ajouterMatiereAClasse(classe.getId(), selected.getId());
                refreshGestionTables(classe, associatedTable, availableTable);
                loadClasses();
                loadMatieres();
            } catch (Exception e) {
                showError(e);
            }
        });

        VBox left = new VBox(10, associatedTable, removeButton);
        VBox right = new VBox(10, availableTable, addButton);
        VBox.setVgrow(associatedTable, Priority.ALWAYS);
        VBox.setVgrow(availableTable, Priority.ALWAYS);

        HBox content = new HBox(15, left, right);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        refreshGestionTables(classe, associatedTable, availableTable);
        dialog.showAndWait();
    }

    private void showEtudiantsClasseDialog(Classe classe) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Étudiants de la classe " + classe.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        

        Label title = new Label("Étudiants de " + classe.getNom() +
                " — Année " + classe.getAnneeAcademique());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label totalLabel = new Label("Total : 0 étudiant(s)");
        totalLabel.setStyle("-fx-text-fill: #6C757D;");

        TableView<Etudiant> table = new TableView<>();
        table.getColumns().setAll(
            createColumn("CNE", 100, Etudiant::getCne),
                createColumn("Nom", 130, Etudiant::getNom),
                createColumn("Prénom", 130, Etudiant::getPrenom),
                createColumn("Email", 180, Etudiant::getEmail)
        );
        Label emptyLabel = new Label("Aucun étudiant dans cette classe");
        table.setPlaceholder(emptyLabel);

        try {
            List<Etudiant> etudiants = ServiceLocator.getReferentielService().getEtudiantsByClasse(classe.getId());
            totalLabel.setText("Total : " + etudiants.size() + " étudiant(s)");
            table.setItems(javafx.collections.FXCollections.observableArrayList(etudiants));
        } catch (Exception e) {
            showError(e);
        }

        VBox content = new VBox(10, title, totalLabel, table);
        content.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void showEnseignantsClasseDialog(Classe classe) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Enseignants de la classe " + classe.getNom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(650, DIALOG_HEIGHT);

        Label title = new Label("Enseignants de " + classe.getNom());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label totalLabel = new Label("Total : 0 enseignant(s)");
        totalLabel.setStyle("-fx-text-fill: #6C757D;");

        TableView<Enseignant> table = new TableView<>();
        Map<Integer, String> matieresLabels = new HashMap<>();

        table.getColumns().setAll(
                createColumn("Nom", 130, Enseignant::getNom),
                createColumn("Prénom", 130, Enseignant::getPrenom),
                createColumn("Email", 180, Enseignant::getEmail),
                createColumn("Matière enseignée", 180, enseignant ->
                        matieresLabels.getOrDefault(enseignant.getId(), ""))
        );
        Label emptyLabel = new Label("Aucun enseignant affecté à cette classe");
        table.setPlaceholder(emptyLabel);

        try {
            List<Enseignant> enseignants = ServiceLocator.getReferentielService().getEnseignantsByClasse(classe.getId());
            totalLabel.setText("Total : " + enseignants.size() + " enseignant(s)");
            for (Enseignant enseignant : enseignants) {
                List<Matiere> matieres = ServiceLocator.getReferentielService()
                        .getMatieresByEnseignantAndClasse(enseignant.getId(), classe.getId());
                matieresLabels.put(enseignant.getId(), joinMatiereLabels(matieres));
            }
            table.setItems(javafx.collections.FXCollections.observableArrayList(enseignants));
        } catch (Exception e) {
            showError(e);
        }

        VBox content = new VBox(10, title, totalLabel, table);
        content.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void refreshGestionTables(Classe classe, TableView<Matiere> associatedTable, TableView<Matiere> availableTable) {
        List<Matiere> associated = ServiceLocator.getReferentielService().getMatieresByClasse(classe.getId());
        List<Matiere> all = ServiceLocator.getReferentielService().getAllMatieres();

        Set<Integer> associatedIds = new HashSet<>();
        for (Matiere matiere : associated) {
            associatedIds.add(matiere.getId());
        }

        List<Matiere> available = new ArrayList<>();
        for (Matiere matiere : all) {
            if (!associatedIds.contains(matiere.getId())) {
                available.add(matiere);
            }
        }

        associatedTable.setItems(javafx.collections.FXCollections.observableArrayList(associated));
        availableTable.setItems(javafx.collections.FXCollections.observableArrayList(available));
    }

    private static class EtudiantFormResult {
        private final Etudiant etudiant;
        private final String motDePasse;

        private EtudiantFormResult(Etudiant etudiant, String motDePasse) {
            this.etudiant = etudiant;
            this.motDePasse = motDePasse;
        }

        private Etudiant getEtudiant() {
            return etudiant;
        }

        private String getMotDePasse() {
            return motDePasse;
        }
    }

    private static class EnseignantFormResult {
        private final Enseignant enseignant;
        private final String motDePasse;

        private EnseignantFormResult(Enseignant enseignant, String motDePasse) {
            this.enseignant = enseignant;
            this.motDePasse = motDePasse;
        }

        private Enseignant getEnseignant() {
            return enseignant;
        }

        private String getMotDePasse() {
            return motDePasse;
        }
    }

    private String formatMatiereCount(Integer count) {
        int value = count != null ? count : 0;
        return value + " matières";
    }

    private String formatClasseCount(Integer count) {
        int value = count != null ? count : 0;
        return value + " classes";
    }

    private String formatEtudiantCount(Integer count) {
        int value = count != null ? count : 0;
        return value + " étudiants";
    }

    private String joinClasseLabels(List<Classe> classes) {
        if (classes == null || classes.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Classe classe : classes) {
            if (classe == null || classe.getNom() == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(classe.getNom());
        }
        return builder.toString();
    }

    private String joinMatiereLabels(List<Matiere> matieres) {
        if (matieres == null || matieres.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Matiere matiere : matieres) {
            if (matiere == null || matiere.getIntitule() == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(matiere.getIntitule());
        }
        return builder.toString();
    }

    private String formatTwo(double valeur) {
        double arrondi = Math.round(valeur * 100.0) / 100.0;
        return String.format(java.util.Locale.US, "%.2f", arrondi);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(Exception e) {
        showErrorMessage(e.getMessage() != null ? e.getMessage() : "Erreur inconnue");
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
