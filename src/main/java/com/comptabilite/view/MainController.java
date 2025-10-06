package com.comptabilite.view;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML private Label userLabel;
    @FXML private Label statusLabel;
    @FXML private Label dateLabel;

    private Stage primaryStage;
    private final AuthenticationService authenticationService;

    public MainController() {
        this.authenticationService = AuthenticationService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateUserInfo();
        updateDateTime();

        // Mettre à jour l'heure toutes les minutes
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.minutes(1), e -> updateDateTime())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateUserInfo() {
        if (authenticationService.isUserLoggedIn()) {
            var utilisateur = authenticationService.getUtilisateurConnecte();
            String entrepriseInfo = "";
            if (utilisateur.getEntreprise() != null) {
                String pays = utilisateur.getEntreprise().getPays();
                String raisonSociale = utilisateur.getEntreprise().getRaisonSociale();
                if ("Cameroun".equals(pays)) {
                    entrepriseInfo = " | 🇨🇲 " + raisonSociale + " (OHADA)";
                } else {
                    entrepriseInfo = " | 🇫🇷 " + raisonSociale;
                }
            }
            userLabel.setText("Utilisateur: " + utilisateur.getNomComplet() + entrepriseInfo);
        }
    }

    private void updateDateTime() {
        dateLabel.setText("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }


    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            authenticationService.logout();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 300);

            LoginController controller = fxmlLoader.getController();
            controller.setPrimaryStage(primaryStage);

            primaryStage.setTitle("Comptabilité Entreprise - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            logger.error("Erreur lors de la déconnexion", e);
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        primaryStage.close();
    }

    @FXML
    private void showPlanComptable(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/plan-comptable.fxml"));

            Alert planComptableDialog = new Alert(Alert.AlertType.NONE);
            planComptableDialog.setTitle("Plan Comptable");
            planComptableDialog.setHeaderText(null);
            planComptableDialog.getDialogPane().setContent(fxmlLoader.load());
            planComptableDialog.getDialogPane().setPrefSize(1000, 600);
            planComptableDialog.getButtonTypes().add(ButtonType.CLOSE);
            planComptableDialog.showAndWait();

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture du plan comptable", e);
            statusLabel.setText("Erreur lors de l'ouverture du plan comptable");
        }
    }

    @FXML
    private void showEcrituresComptables(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ecritures-comptables.fxml"));

            Alert ecrituresDialog = new Alert(Alert.AlertType.NONE);
            ecrituresDialog.setTitle("Écritures Comptables");
            ecrituresDialog.setHeaderText(null);
            ecrituresDialog.getDialogPane().setContent(fxmlLoader.load());
            ecrituresDialog.getDialogPane().setPrefSize(1200, 700);
            ecrituresDialog.getButtonTypes().add(ButtonType.CLOSE);
            ecrituresDialog.showAndWait();

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture des écritures comptables", e);
            statusLabel.setText("Erreur lors de l'ouverture des écritures comptables");
        }
    }

    @FXML
    private void showClients(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/clients.fxml"));

            Alert clientsDialog = new Alert(Alert.AlertType.NONE);
            clientsDialog.setTitle("Gestion des Clients");
            clientsDialog.setHeaderText(null);
            clientsDialog.getDialogPane().setContent(fxmlLoader.load());
            clientsDialog.getDialogPane().setPrefSize(1200, 700);
            clientsDialog.getButtonTypes().add(ButtonType.CLOSE);
            clientsDialog.showAndWait();

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture de la gestion des clients", e);
            statusLabel.setText("Erreur lors de l'ouverture de la gestion des clients");
        }
    }

    @FXML
    private void showRapports(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/rapports.fxml"));

            Alert rapportsDialog = new Alert(Alert.AlertType.NONE);
            rapportsDialog.setTitle("Rapports Comptables");
            rapportsDialog.setHeaderText(null);
            rapportsDialog.getDialogPane().setContent(fxmlLoader.load());
            rapportsDialog.getDialogPane().setPrefSize(1400, 800);
            rapportsDialog.getButtonTypes().add(ButtonType.CLOSE);
            rapportsDialog.showAndWait();

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture des rapports", e);
            statusLabel.setText("Erreur lors de l'ouverture des rapports");
        }
    }

    // Méthodes manquantes pour éviter les erreurs FXML
    @FXML
    private void showFournisseurs(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/fournisseurs.fxml"));

            Alert fournisseursDialog = new Alert(Alert.AlertType.NONE);
            fournisseursDialog.setTitle("Gestion des Fournisseurs");
            fournisseursDialog.setHeaderText(null);
            fournisseursDialog.getDialogPane().setContent(fxmlLoader.load());
            fournisseursDialog.getDialogPane().setPrefSize(1200, 700);
            fournisseursDialog.getButtonTypes().add(ButtonType.CLOSE);
            fournisseursDialog.showAndWait();

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture de la gestion des fournisseurs", e);
            statusLabel.setText("Erreur lors de l'ouverture de la gestion des fournisseurs");
        }
    }

    @FXML
    private void showFactures(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/factures.fxml"));

            Alert facturesDialog = new Alert(Alert.AlertType.NONE);
            facturesDialog.setTitle("Gestion des Factures");
            facturesDialog.setHeaderText(null);
            facturesDialog.getDialogPane().setContent(fxmlLoader.load());
            facturesDialog.getDialogPane().setPrefSize(1400, 800);
            facturesDialog.getButtonTypes().add(ButtonType.CLOSE);
            facturesDialog.showAndWait();

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture de la gestion des factures", e);
            statusLabel.setText("Erreur lors de l'ouverture de la gestion des factures");
        }
    }

    @FXML
    private void showGrandLivre(ActionEvent event) {
        showRapports(event);
    }

    @FXML
    private void showBalance(ActionEvent event) {
        showRapports(event);
    }

    @FXML
    private void showBilan(ActionEvent event) {
        showRapports(event);
    }

    @FXML
    private void showCompteResultat(ActionEvent event) {
        showRapports(event);
    }

    @FXML
    private void showDeclarationTVA(ActionEvent event) {
        showInfo("Déclaration TVA en cours de développement");
    }

    @FXML
    private void showUtilisateurs(ActionEvent event) {
        showInfo("Gestion utilisateurs en cours de développement");
    }

    @FXML
    private void showExercices(ActionEvent event) {
        showInfo("Gestion exercices en cours de développement");
    }

    @FXML
    private void showParametres(ActionEvent event) {
        showInfo("Paramètres en cours de développement");
    }

    @FXML
    private void handleNewCompany(ActionEvent event) {
        try {
            // Charger le dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/entreprise-dialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Créer le dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Nouvelle Entreprise");

            // Configurer la fenêtre
            dialog.initOwner(primaryStage);
            dialog.setResizable(true);

            // Obtenir le contrôleur
            EntrepriseDialogController controller = loader.getController();
            controller.setEntreprise(null); // Nouvelle entreprise

            // Afficher le dialog et traiter le résultat
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    if (controller.validate()) {
                        var entreprise = controller.getEntreprise();
                        if (controller.saveEntreprise()) {
                            showInfo("Entreprise '" + entreprise.getRaisonSociale() + "' créée avec succès!");
                            logger.info("Nouvelle entreprise créée: {}", entreprise.getRaisonSociale());
                        }
                    }
                }
            });

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture du dialog entreprise", e);
            showError("Erreur", "Impossible d'ouvrir l'interface de création d'entreprise.\nErreur: " + e.getMessage());
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void showAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos");
        alert.setHeaderText("Comptabilité Entreprise v1.0");
        alert.setContentText("Logiciel de comptabilité pour entreprises\n\n" +
                           "Développé avec JavaFX et MySQL\n" +
                           "Copyright © 2024");
        alert.showAndWait();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}