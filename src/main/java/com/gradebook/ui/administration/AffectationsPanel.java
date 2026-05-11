package com.gradebook.ui.administration;

import com.gradebook.config.ServiceLocator;
import com.gradebook.model.Classe;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Matiere;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AffectationsPanel extends VBox {
    private static final String PANEL_BG = "#F5F7FA";
    private static final String TITLE_STYLE = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2E4057;";
    private static final String SUBTITLE_STYLE = "-fx-font-size: 15px; -fx-font-weight: bold;";

    private static final String BTN_PRIMARY = "-fx-background-color: #2E4057; -fx-text-fill: white;";
    private static final String BTN_DANGER = "-fx-background-color: #E74C3C; -fx-text-fill: white;";

    private static final String ALL_CLASSES_LABEL = "Toutes les classes";

    private final ComboBox<Enseignant> cbEnseignant = new ComboBox<>();
    private final ComboBox<Classe> cbClasse = new ComboBox<>();
    private final ComboBox<Integer> cbSemestre = new ComboBox<>();
    private final ComboBox<Matiere> cbMatiere = new ComboBox<>();

    private final ComboBox<Classe> cbFiltreClasse = new ComboBox<>();

    private final TableView<LigneAffectation> table = new TableView<>();

    private final List<LigneAffectation> affectations = new ArrayList<>();

    private Button btnRetirer;

    public AffectationsPanel() {
        setPadding(new Insets(25));
        setSpacing(15);
        setStyle("-fx-background-color: " + PANEL_BG + ";");

        Label title = new Label("Affectations Enseignants");
        title.setStyle(TITLE_STYLE);
        VBox.setMargin(title, new Insets(0, 0, 15, 0));

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5);

        VBox left = buildFormPanel();
        VBox right = buildTablePanel();

        splitPane.getItems().addAll(left, right);
        getChildren().addAll(title, splitPane);

        refreshData();
    }

    public void refreshData() {
        loadFormData();
        loadAffectations();
    }

    private VBox buildFormPanel() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(10));

        Label subtitle = new Label("Nouvelle affectation");
        subtitle.setStyle(SUBTITLE_STYLE);

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);

        cbEnseignant.setMaxWidth(Double.MAX_VALUE);
        cbClasse.setMaxWidth(Double.MAX_VALUE);
        cbSemestre.setMaxWidth(Double.MAX_VALUE);
        cbMatiere.setMaxWidth(Double.MAX_VALUE);

        cbEnseignant.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Enseignant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom());
                }
            }
        });
        cbEnseignant.setButtonCell(cbEnseignant.getCellFactory().call(null));

        cbClasse.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
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
        cbClasse.setButtonCell(cbClasse.getCellFactory().call(null));

        cbSemestre.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
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
        cbSemestre.setButtonCell(cbSemestre.getCellFactory().call(null));

        cbMatiere.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
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
        cbMatiere.setButtonCell(cbMatiere.getCellFactory().call(null));

        cbClasse.valueProperty().addListener((obs, oldValue, newValue) ->
            loadMatieresForClasseAndSemestre(newValue, cbSemestre.getValue()));
        cbSemestre.valueProperty().addListener((obs, oldValue, newValue) ->
            loadMatieresForClasseAndSemestre(cbClasse.getValue(), newValue));

        form.add(new Label("Enseignant :"), 0, 0);
        form.add(cbEnseignant, 1, 0);
        form.add(new Label("Classe :"), 0, 1);
        form.add(cbClasse, 1, 1);
        form.add(new Label("Semestre :"), 0, 2);
        form.add(cbSemestre, 1, 2);
        form.add(new Label("Matière :"), 0, 3);
        form.add(cbMatiere, 1, 3);

        GridPane.setHgrow(cbEnseignant, Priority.ALWAYS);
        GridPane.setHgrow(cbClasse, Priority.ALWAYS);
        GridPane.setHgrow(cbSemestre, Priority.ALWAYS);
        GridPane.setHgrow(cbMatiere, Priority.ALWAYS);

        Button btnAffecter = new Button("✅ Affecter");
        btnAffecter.setStyle(BTN_PRIMARY);
        btnAffecter.disableProperty().bind(
                cbEnseignant.valueProperty().isNull()
                        .or(cbClasse.valueProperty().isNull())
                .or(cbSemestre.valueProperty().isNull())
                        .or(cbMatiere.valueProperty().isNull())
        );
        btnAffecter.setOnAction(event -> handleAffecter());

        btnRetirer = new Button("🗑️ Retirer affectation");
        btnRetirer.setStyle(BTN_DANGER);
        btnRetirer.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        btnRetirer.setOnAction(event -> handleRetirer());

        VBox.setVgrow(form, Priority.NEVER);
        box.getChildren().addAll(subtitle, form, btnAffecter, btnRetirer);
        return box;
    }

    private VBox buildTablePanel() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(10));

        Label subtitle = new Label("Affectations actuelles");
        subtitle.setStyle(SUBTITLE_STYLE);

        cbFiltreClasse.setMaxWidth(Double.MAX_VALUE);
        cbFiltreClasse.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
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
        cbFiltreClasse.setButtonCell(cbFiltreClasse.getCellFactory().call(null));
        cbFiltreClasse.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());

        Button btnReset = new Button("🔄 Réinitialiser");
        btnReset.setOnAction(event -> resetFilter());

        HBox filters = new HBox(10, new Label("Classe :"), cbFiltreClasse, btnReset);
        filters.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cbFiltreClasse, Priority.ALWAYS);

        TableColumn<LigneAffectation, String> colEns = new TableColumn<>("Enseignant");
        colEns.setPrefWidth(180);
        colEns.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getEnseignant().getNom() + " " + data.getValue().getEnseignant().getPrenom()
        ));

        TableColumn<LigneAffectation, String> colClasse = new TableColumn<>("Classe");
        colClasse.setPrefWidth(150);
        colClasse.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getClasse().getNom()
        ));

        TableColumn<LigneAffectation, String> colSemestre = new TableColumn<>("Semestre");
        colSemestre.setPrefWidth(80);
        colSemestre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            "S" + data.getValue().getSemestre()
        ));

        TableColumn<LigneAffectation, String> colMatiere = new TableColumn<>("Matière");
        colMatiere.setPrefWidth(180);
        colMatiere.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getMatiere().getIntitule()
        ));

        table.getColumns().setAll(colEns, colClasse, colSemestre, colMatiere);
        VBox.setVgrow(table, Priority.ALWAYS);

        box.getChildren().addAll(subtitle, filters, table);
        return box;
    }

    private void loadFormData() {
        try {
            List<Enseignant> enseignants = ServiceLocator.getReferentielService().getAllEnseignants();
            cbEnseignant.setItems(FXCollections.observableArrayList(enseignants));

            List<Classe> classes = ServiceLocator.getReferentielService().getAllClasses();
            cbClasse.setItems(FXCollections.observableArrayList(classes));

            cbSemestre.setItems(FXCollections.observableArrayList(1, 2));
            cbSemestre.setValue(1);

            cbMatiere.setItems(FXCollections.observableArrayList());

            List<Classe> filtreClasses = new ArrayList<>();
            Classe all = new Classe();
            all.setId(0);
            all.setNom(ALL_CLASSES_LABEL);
            filtreClasses.add(all);
            filtreClasses.addAll(classes);
            cbFiltreClasse.setItems(FXCollections.observableArrayList(filtreClasses));
            cbFiltreClasse.setValue(all);
        } catch (Exception e) {
            showError(e);
        }
    }

    private void loadMatieresForClasseAndSemestre(Classe classe, Integer semestre) {
        if (classe == null || semestre == null) {
            cbMatiere.getItems().clear();
            cbMatiere.setValue(null);
            return;
        }
        try {
            List<Matiere> matieres = ServiceLocator.getReferentielService()
                    .getMatieresByClasseAndSemestre(classe.getId(), semestre);
            cbMatiere.setItems(FXCollections.observableArrayList(matieres));
            cbMatiere.setValue(null);
        } catch (Exception e) {
            showError(e);
        }
    }

    private void loadAffectations() {
        try {
            affectations.clear();
            List<Enseignant> enseignants = ServiceLocator.getReferentielService().getAllEnseignants();
            for (Enseignant enseignant : enseignants) {
                List<Classe> classes = ServiceLocator.getReferentielService().getClassesByEnseignant(enseignant.getId());
                for (Classe classe : classes) {
                    List<Integer> semestres = ServiceLocator.getReferentielService()
                            .getSemestresByEnseignantAndClasse(enseignant.getId(), classe.getId());
                    for (Integer semestre : semestres) {
                        List<Matiere> matieres = ServiceLocator.getReferentielService()
                                .getMatieresByEnseignantAndClasseAndSemestre(
                                        enseignant.getId(),
                                        classe.getId(),
                                        semestre
                                );
                        for (Matiere matiere : matieres) {
                            affectations.add(new LigneAffectation(enseignant, classe, matiere, semestre));
                        }
                    }
                }
            }
            applyFilter();
        } catch (Exception e) {
            showError(e);
        }
    }

    private void applyFilter() {
        Classe selected = cbFiltreClasse.getValue();
        if (selected == null || selected.getId() == 0) {
            table.setItems(FXCollections.observableArrayList(affectations));
            return;
        }
        List<LigneAffectation> filtered = new ArrayList<>();
        for (LigneAffectation ligne : affectations) {
            if (ligne.getClasse().getId() == selected.getId()) {
                filtered.add(ligne);
            }
        }
        table.setItems(FXCollections.observableArrayList(filtered));
    }

    private void resetFilter() {
        if (!cbFiltreClasse.getItems().isEmpty()) {
            cbFiltreClasse.setValue(cbFiltreClasse.getItems().get(0));
        }
        table.setItems(FXCollections.observableArrayList(affectations));
    }

    private void handleAffecter() {
        Enseignant enseignant = cbEnseignant.getValue();
        Classe classe = cbClasse.getValue();
        Integer semestre = cbSemestre.getValue();
        Matiere matiere = cbMatiere.getValue();

        if (enseignant == null || classe == null || matiere == null || semestre == null) {
            showWarning("Veuillez remplir tous les champs");
            return;
        }

        try {
            ServiceLocator.getReferentielService().affecterEnseignant(
                    enseignant.getId(), classe.getId(), matiere.getId(), semestre
            );
            showInfo("Affectation créée avec succès !\n" +
                    enseignant.getNom() + " " + enseignant.getPrenom() + " → " +
                    classe.getNom() + " / " + matiere.getIntitule());
            cbEnseignant.setValue(null);
            cbClasse.setValue(null);
            cbMatiere.getItems().clear();
            cbMatiere.setValue(null);
            loadAffectations();
        } catch (Exception e) {
            showWarning(e.getMessage());
        }
    }

    private void handleRetirer() {
        LigneAffectation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Retirer l'affectation de " +
                        selected.getEnseignant().getNom() + " " + selected.getEnseignant().getPrenom() +
                        " pour " + selected.getMatiere().getIntitule() +
                        " en " + selected.getClasse().getNom() + " ?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            ServiceLocator.getReferentielService().retirerEnseignant(
                    selected.getEnseignant().getId(),
                    selected.getClasse().getId(),
                selected.getMatiere().getId(),
                selected.getSemestre()
            );
            showInfo("Affectation retirée avec succès !");
            loadAffectations();
        } catch (Exception e) {
            showError(e);
        }
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

    public static class LigneAffectation {
        private final Enseignant enseignant;
        private final Classe classe;
        private final Matiere matiere;
        private final int semestre;

        public LigneAffectation(Enseignant enseignant, Classe classe, Matiere matiere, int semestre) {
            this.enseignant = enseignant;
            this.classe = classe;
            this.matiere = matiere;
            this.semestre = semestre;
        }

        public Enseignant getEnseignant() {
            return enseignant;
        }

        public Classe getClasse() {
            return classe;
        }

        public Matiere getMatiere() {
            return matiere;
        }

        public int getSemestre() {
            return semestre;
        }
    }
}
