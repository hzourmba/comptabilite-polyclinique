package com.comptabilite.view;

import com.comptabilite.dao.CompteDAO;
import com.comptabilite.model.Compte;
import com.comptabilite.service.AuthenticationService;
import com.comptabilite.service.PlanComptableService;
import com.comptabilite.service.CurrencyService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PlanComptableController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(PlanComptableController.class);

    @FXML private TextField rechercheField;
    @FXML private ComboBox<String> filtreClasseCombo;
    @FXML private TableView<Compte> comptesTable;
    @FXML private TableColumn<Compte, String> numeroColumn;
    @FXML private TableColumn<Compte, String> libelleColumn;
    @FXML private TableColumn<Compte, String> typeColumn;
    @FXML private TableColumn<Compte, String> classeColumn;
    @FXML private TableColumn<Compte, BigDecimal> soldeDebiteurColumn;
    @FXML private TableColumn<Compte, BigDecimal> soldeCrediteurColumn;
    @FXML private TableColumn<Compte, Boolean> actifColumn;
    @FXML private Button ajouterButton;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;
    @FXML private Button initialiserPlanButton;
    @FXML private Label totalComptesLabel;

    private final CompteDAO compteDAO;
    private final AuthenticationService authService;
    private final PlanComptableService planComptableService;
    private final CurrencyService currencyService;
    private ObservableList<Compte> comptes;

    public PlanComptableController() {
        this.compteDAO = new CompteDAO();
        this.authService = AuthenticationService.getInstance();
        this.planComptableService = new PlanComptableService();
        this.currencyService = CurrencyService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupComboBox();
        loadComptes();

        // Sélection par défaut du filtre
        filtreClasseCombo.getSelectionModel().selectFirst();

        // Activer/désactiver les boutons selon les droits
        boolean canModify = authService.canModifyData();
        ajouterButton.setDisable(!canModify);
        modifierButton.setDisable(!canModify);
        supprimerButton.setDisable(!canModify);

        // Adapter le bouton d'initialisation selon l'entreprise
        setupInitializationButton();
    }

    private void setupComboBox() {
        filtreClasseCombo.getItems().addAll(
            "Toutes les classes",
            "Classe 1 - Comptes de capitaux",
            "Classe 2 - Comptes d'immobilisations",
            "Classe 3 - Comptes de stocks",
            "Classe 4 - Comptes de tiers",
            "Classe 5 - Comptes financiers",
            "Classe 6 - Comptes de charges",
            "Classe 7 - Comptes de produits"
        );
    }

    private void setupTable() {
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numeroCompte"));
        libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        typeColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTypeCompte().toString()));
        classeColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getClasseCompte().toString()));
        // Utiliser les soldes consolidés pour les comptes parents
        soldeDebiteurColumn.setCellValueFactory(cellData -> {
            Compte compte = cellData.getValue();
            BigDecimal solde = compte.getAccepteSousComptes() ?
                compte.getSoldeDebiteurConsolide() : compte.getSoldeDebiteur();
            return new javafx.beans.property.SimpleObjectProperty<>(solde);
        });

        soldeCrediteurColumn.setCellValueFactory(cellData -> {
            Compte compte = cellData.getValue();
            BigDecimal solde = compte.getAccepteSousComptes() ?
                compte.getSoldeCrediteurConsolide() : compte.getSoldeCrediteur();
            return new javafx.beans.property.SimpleObjectProperty<>(solde);
        });

        actifColumn.setCellValueFactory(new PropertyValueFactory<>("actif"));

        // Formatage des colonnes numériques avec indication consolidée
        soldeDebiteurColumn.setCellFactory(col -> new TableCell<Compte, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Compte compte = getTableView().getItems().get(getIndex());
                    String formattedAmount = currencyService.formatAmountForTable(item);

                    if (compte.getAccepteSousComptes() && !compte.getSousComptes().isEmpty()) {
                        setText(formattedAmount + " (C)"); // (C) = Consolidé
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #2E8B57;");
                    } else {
                        setText(formattedAmount);
                        setStyle("");
                    }
                }
            }
        });

        soldeCrediteurColumn.setCellFactory(col -> new TableCell<Compte, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Compte compte = getTableView().getItems().get(getIndex());
                    String formattedAmount = currencyService.formatAmountForTable(item);

                    if (compte.getAccepteSousComptes() && !compte.getSousComptes().isEmpty()) {
                        setText(formattedAmount + " (C)"); // (C) = Consolidé
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #2E8B57;");
                    } else {
                        setText(formattedAmount);
                        setStyle("");
                    }
                }
            }
        });

        // Double-clic pour modifier
        comptesTable.setRowFactory(tv -> {
            TableRow<Compte> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    modifierCompte(null);
                }
            });
            return row;
        });
    }

    private void loadComptes() {
        try {
            if (authService.isUserLoggedIn()) {
                // Utiliser l'entreprise de l'utilisateur connecté
                Long entrepriseId = authService.getUtilisateurConnecte().getEntreprise().getId();
                logger.info("Chargement des comptes pour l'entreprise ID: {}", entrepriseId);

                // Recalculer les soldes consolidés avant le chargement
                logger.info("Recalcul des soldes consolidés...");
                compteDAO.recalculerTousLesSoldesConsolides(entrepriseId);

                List<Compte> comptesList = planComptableService.getComptesByEntreprise(entrepriseId);
                comptes = FXCollections.observableArrayList(comptesList);
                comptesTable.setItems(comptes);

                totalComptesLabel.setText("Total: " + comptes.size() + " comptes");
                logger.info("Comptes chargés avec soldes consolidés actualisés");
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des comptes", e);
            showError("Erreur lors du chargement des comptes: " + e.getMessage());
        }
    }

    /**
     * Rafraîchit l'affichage pour mettre à jour les soldes consolidés
     */
    public void rafraichirSoldes() {
        Platform.runLater(() -> {
            comptesTable.refresh();
            logger.info("Affichage des soldes consolidés rafraîchi");
        });
    }

    @FXML
    private void rechercher(KeyEvent event) {
        String terme = rechercheField.getText().toLowerCase();
        if (terme.isEmpty()) {
            comptesTable.setItems(comptes);
        } else {
            ObservableList<Compte> filtered = comptes.filtered(compte ->
                compte.getNumeroCompte().toLowerCase().contains(terme) ||
                compte.getLibelle().toLowerCase().contains(terme));
            comptesTable.setItems(filtered);
        }
        totalComptesLabel.setText("Affichés: " + comptesTable.getItems().size() + " / " + comptes.size() + " comptes");
    }

    @FXML
    private void filtrerParClasse(ActionEvent event) {
        String classeSelectionnee = filtreClasseCombo.getSelectionModel().getSelectedItem();
        if (classeSelectionnee == null || classeSelectionnee.equals("Toutes les classes")) {
            comptesTable.setItems(comptes);
        } else {
            // Extraire le numéro de classe
            String numeroClasse = classeSelectionnee.substring(7, 8); // "Classe X"

            // Déterminer si on travaille avec des comptes CM (Cameroun) ou français
            boolean isOHADA = authService.isUserLoggedIn() &&
                             authService.getUtilisateurConnecte().getEntreprise() != null &&
                             "Cameroun".equals(authService.getUtilisateurConnecte().getEntreprise().getPays());

            ObservableList<Compte> filtered = comptes.filtered(compte -> {
                String numCompte = compte.getNumeroCompte();
                if (isOHADA) {
                    // Pour les comptes OHADA avec préfixe CM
                    return numCompte.startsWith("CM" + numeroClasse);
                } else {
                    // Pour les comptes français traditionnels
                    return numCompte.startsWith(numeroClasse);
                }
            });
            comptesTable.setItems(filtered);
        }
        totalComptesLabel.setText("Affichés: " + comptesTable.getItems().size() + " / " + comptes.size() + " comptes");
    }

    @FXML
    private void ajouterCompte(ActionEvent event) {
        openCompteDialog(null);
    }

    @FXML
    private void modifierCompte(ActionEvent event) {
        Compte selectedCompte = comptesTable.getSelectionModel().getSelectedItem();
        if (selectedCompte == null) {
            showWarning("Veuillez sélectionner un compte à modifier");
            return;
        }
        openCompteDialog(selectedCompte);
    }

    @FXML
    private void supprimerCompte(ActionEvent event) {
        Compte selectedCompte = comptesTable.getSelectionModel().getSelectedItem();
        if (selectedCompte == null) {
            showWarning("Veuillez sélectionner un compte à supprimer");
            return;
        }

        // Vérifier si le compte peut être supprimé
        if (!compteDAO.canDelete(selectedCompte.getId())) {
            showWarning("Ce compte ne peut pas être supprimé car il contient des écritures comptables.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le compte");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer le compte " +
                                   selectedCompte.getNumeroEtLibelle() + " ?\n\n" +
                                   "Cette action est irréversible.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                planComptableService.supprimerCompte(selectedCompte);
                loadComptes();
                showInfo("Compte supprimé avec succès");
            } catch (Exception e) {
                logger.error("Erreur lors de la suppression", e);
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void exporterPlan(ActionEvent event) {
        showInfo("Export CSV en cours de développement");
    }

    private void setupInitializationButton() {
        if (authService.isUserLoggedIn() && authService.getUtilisateurConnecte().getEntreprise() != null) {
            String pays = authService.getUtilisateurConnecte().getEntreprise().getPays();
            if ("Cameroun".equals(pays)) {
                initialiserPlanButton.setText("Initialiser Plan OHADA");
            } else {
                initialiserPlanButton.setText("Initialiser Plan Français");
            }
        }
    }

    @FXML
    private void initialiserPlanFrancais(ActionEvent event) {
        if (authService.isUserLoggedIn() && authService.getUtilisateurConnecte().getEntreprise() != null) {
            String pays = authService.getUtilisateurConnecte().getEntreprise().getPays();

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Initialisation du Plan Comptable");
            info.setHeaderText("Initialisation manuelle requise");

            if ("Cameroun".equals(pays)) {
                info.setContentText("Pour initialiser le plan comptable OHADA, exécutez cette commande dans votre terminal :\n\n" +
                                   "cd C:\\Anwendung\\Comptabilite\\project\n" +
                                   "mysql -u comptabilite_user -p < plan_comptable_ohada_final.sql\n\n" +
                                   "Puis fermez et rouvrez ce module pour voir les comptes OHADA avec préfixe CM créés.");
            } else {
                info.setContentText("Pour initialiser le plan comptable français, exécutez cette commande dans votre terminal :\n\n" +
                                   "cd C:\\Anwendung\\Comptabilite\\project\n" +
                                   "mysql -u comptabilite_user -p < plan_comptable_francais.sql\n\n" +
                                   "Puis fermez et rouvrez ce module pour voir les comptes créés.");
            }

            info.getDialogPane().setPrefWidth(600);
            info.showAndWait();
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Ouvre le dialog de création/modification de compte
     */
    private void openCompteDialog(Compte compte) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/compte-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle(compte == null ? "Nouveau Compte" : "Modifier le Compte");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(ajouterButton.getScene().getWindow());
            dialogStage.setScene(new Scene(loader.load()));

            CompteDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCompte(compte);

            dialogStage.showAndWait();

            // Si validé, recharger la liste
            if (controller.isValidated()) {
                loadComptes();

                // Sélectionner le compte créé/modifié
                Compte savedCompte = controller.getCompte();
                if (savedCompte != null) {
                    comptesTable.getSelectionModel().select(savedCompte);
                    comptesTable.scrollTo(savedCompte);
                }

                showInfo(compte == null ?
                    "Compte créé avec succès !" :
                    "Compte modifié avec succès !");
            }

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture du dialog de compte", e);
            showError("Erreur lors de l'ouverture du formulaire: " + e.getMessage());
        }
    }
}