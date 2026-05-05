package com.gradebook.ui.administration;

import com.gradebook.config.ServiceLocator;
import com.gradebook.model.Classe;
import com.gradebook.model.Matiere;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ReferentielPanel extends VBox {
    private static final String TITLE_STYLE = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2E4057;";
    private static final String PANEL_STYLE = "-fx-background-color: white; -fx-background-radius: 8;";

    private static final String BTN_ADD_STYLE = "-fx-background-color: #2E4057; -fx-text-fill: white;";
    private static final String BTN_EDIT_STYLE = "-fx-background-color: #3a5068; -fx-text-fill: white;";
    private static final String BTN_DELETE_STYLE = "-fx-background-color: #E74C3C; -fx-text-fill: white;";

    private static final String ERR_CHAMPS = "Veuillez remplir tous les champs";
    private static final String ERR_COEFF = "Le coefficient doit être un nombre supérieur à 0";
    private static final String ERR_DELETE_CLASSE = "Impossible de supprimer : des étudiants sont rattachés à cette classe";
    private static final String ERR_DELETE_MATIERE = "Impossible de supprimer : des évaluations sont rattachées à cette matière";

    private final TableView<Classe> classesTable = new TableView<>();
    private final TableView<Matiere> matieresTable = new TableView<>();

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

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        getChildren().addAll(title, tabPane);

        loadClasses();
        loadMatieres();
    }

    private Tab buildClassesTab() {
        Tab tab = new Tab("Classes");

        classesTable.getColumns().setAll(
                createColumn("ID", 60, classe -> String.valueOf(classe.getId())),
                createColumn("Nom", 150, Classe::getNom),
                createColumn("Niveau", 150, Classe::getNiveau),
                createColumn("Année académique", 150, Classe::getAnneeAcademique)
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

        HBox actions = new HBox(10, addButton, editButton, deleteButton);
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
                createColumn("Coefficient", 120, matiere -> formatTwo(matiere.getCoefficient()))
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

    private <T> TableColumn<T, String> createColumn(String title, double width, Function<T, String> mapper) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        return column;
    }

    private void loadClasses() {
        try {
            List<Classe> classes = ServiceLocator.getReferentielService().getAllClasses();
            classesTable.setItems(javafx.collections.FXCollections.observableArrayList(classes));
        } catch (Exception e) {
            showError(e);
        }
    }

    private void loadMatieres() {
        try {
            List<Matiere> matieres = ServiceLocator.getReferentielService().getAllMatieres();
            matieresTable.setItems(javafx.collections.FXCollections.observableArrayList(matieres));
        } catch (Exception e) {
            showError(e);
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
            showErrorMessage(ERR_DELETE_MATIERE);
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

    private void showError(Exception e) {
        showErrorMessage(e.getMessage() != null ? e.getMessage() : "Erreur inconnue");
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
