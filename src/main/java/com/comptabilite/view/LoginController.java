package com.comptabilite.view;

import com.comptabilite.dao.UtilisateurDAO;
import com.comptabilite.model.Utilisateur;
import com.comptabilite.service.AuthenticationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private Stage primaryStage;
    private final AuthenticationService authenticationService;

    public LoginController() {
        this.authenticationService = AuthenticationService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginButton.setDefaultButton(true);
        usernameField.requestFocus();

        usernameField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                passwordField.requestFocus();
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLogin(null);
            }
        });
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez saisir votre nom d'utilisateur et votre mot de passe.");
            return;
        }

        try {
            Optional<Utilisateur> utilisateur = authenticationService.authenticate(username, password);

            if (utilisateur.isPresent()) {
                logger.info("Connexion réussie pour l'utilisateur: {}", username);
                authenticationService.setUtilisateurConnecte(utilisateur.get());
                openMainWindow();
            } else {
                showError("Nom d'utilisateur ou mot de passe incorrect.");
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la connexion", e);
            showError("Erreur de connexion. Veuillez réessayer.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        primaryStage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void openMainWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main_simple.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

            MainController controller = fxmlLoader.getController();
            controller.setPrimaryStage(primaryStage);

            primaryStage.setTitle("Comptabilité Entreprise - " + authenticationService.getUtilisateurConnecte().getNomComplet());
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture de la fenêtre principale", e);
            showError("Erreur lors de l'ouverture de l'application.");
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}