package com.gradebook.ui;

import com.gradebook.config.ServiceLocator;
import com.gradebook.model.Administration;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Etudiant;
import com.gradebook.ui.administration.AdministrationMainFrame;
import com.gradebook.ui.enseignant.EnseignantMainFrame;
import com.gradebook.ui.etudiant.EtudiantMainFrame;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginController {
    private static final String ERR_CHAMPS_VIDE = "Veuillez remplir tous les champs";
    private static final String ERR_IDENTIFIANTS = "Email ou mot de passe incorrect";

    private static final String BUTTON_STYLE_NORMAL =
            "-fx-background-color: #2E4057; -fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-background-radius: 6;";
    private static final String BUTTON_STYLE_HOVER =
            "-fx-background-color: #1a2535; -fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-background-radius: 6;";

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label labelErreur;

    @FXML
    private void initialize() {
        if (loginButton != null) {
            loginButton.setStyle(BUTTON_STYLE_NORMAL);
            loginButton.setOnMouseEntered(event -> loginButton.setStyle(BUTTON_STYLE_HOVER));
            loginButton.setOnMouseExited(event -> loginButton.setStyle(BUTTON_STYLE_NORMAL));
        }
    }

    @FXML
    private void handleLogin() {
        loginButton.setDisable(true);
        hideError();

        Stage currentStage = (Stage) loginButton.getScene().getWindow();

        try {
            String email = emailField.getText() != null ? emailField.getText().trim() : "";
            String motDePasse = passwordField.getText() != null ? passwordField.getText() : "";

            if (isBlank(email) || isBlank(motDePasse)) {
                showError(ERR_CHAMPS_VIDE);
                return;
            }

            Optional<Etudiant> etudiant = ServiceLocator.getAuthService().connecterEtudiant(email, motDePasse);
            if (etudiant.isPresent()) {
                currentStage.close();
                openEtudiantWindow(etudiant.get());
                return;
            }

            Optional<Enseignant> enseignant = ServiceLocator.getAuthService().connecterEnseignant(email, motDePasse);
            if (enseignant.isPresent()) {
                currentStage.close();
                openEnseignantWindow(enseignant.get());
                return;
            }

            Optional<Administration> admin = ServiceLocator.getAuthService().connecterAdministration(email, motDePasse);
            if (admin.isPresent()) {
                currentStage.close();
                openAdministrationWindow(admin.get());
                return;
            }

            showError(ERR_IDENTIFIANTS);
        } finally {
            if (currentStage.isShowing()) {
                loginButton.setDisable(false);
            }
        }
    }

    private void openEtudiantWindow(Etudiant etudiant) {
        try {
            EtudiantMainFrame frame = new EtudiantMainFrame(etudiant);
            frame.start(new Stage());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void openEnseignantWindow(Enseignant enseignant) {
        try {
            EnseignantMainFrame frame = new EnseignantMainFrame(enseignant);
            frame.start(new Stage());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void openAdministrationWindow(Administration admin) {
        try {
            AdministrationMainFrame frame = new AdministrationMainFrame(admin);
            frame.start(new Stage());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void showError(String message) {
        labelErreur.setText(message);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
    }

    private void hideError() {
        labelErreur.setVisible(false);
        labelErreur.setManaged(false);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
