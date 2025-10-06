package com.comptabilite.view;

import com.comptabilite.dao.FournisseurDAO;
import com.comptabilite.model.Fournisseur;
import com.comptabilite.service.AuthenticationService;
import com.comptabilite.service.FournisseurService;
import com.comptabilite.service.CurrencyService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FournisseursController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(FournisseursController.class);

    @FXML private TextField rechercheField;
    @FXML private ComboBox<String> filtreStatutCombo;
    @FXML private ComboBox<String> filtreTypeCombo;
    @FXML private TableView<Fournisseur> fournisseursTable;
    @FXML private TableColumn<Fournisseur, String> codeColumn;
    @FXML private TableColumn<Fournisseur, String> nomColumn;
    @FXML private TableColumn<Fournisseur, String> emailColumn;
    @FXML private TableColumn<Fournisseur, String> telephoneColumn;
    @FXML private TableColumn<Fournisseur, String> villeColumn;
    @FXML private TableColumn<Fournisseur, BigDecimal> soldeColumn;
    @FXML private TableColumn<Fournisseur, String> statutColumn;
    @FXML private TableColumn<Fournisseur, String> typeColumn;
    @FXML private Button ajouterButton;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;
    @FXML private Label totalFournisseursLabel;
    @FXML private Label totalSoldeLabel;

    private final FournisseurDAO fournisseurDAO;
    private final AuthenticationService authService;
    private final FournisseurService fournisseurService;
    private final CurrencyService currencyService;
    private ObservableList<Fournisseur> fournisseurs;

    public FournisseursController() {
        this.fournisseurDAO = new FournisseurDAO();
        this.authService = AuthenticationService.getInstance();
        this.fournisseurService = new FournisseurService();
        this.currencyService = CurrencyService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupComboBoxes();
        loadFournisseurs();

        // Activer/désactiver les boutons selon les droits
        boolean canModify = authService.canModifyData();
        ajouterButton.setDisable(!canModify);
        modifierButton.setDisable(!canModify);
        supprimerButton.setDisable(!canModify);
    }

    private void setupTable() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("codeFournisseur"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));

        soldeColumn.setCellValueFactory(new PropertyValueFactory<>("soldeFournisseur"));
        soldeColumn.setCellFactory(col -> new TableCell<Fournisseur, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyService.formatAmountForTable(item));
                    // Colorer en rouge si négatif (dette envers le fournisseur)
                    if (item.compareTo(BigDecimal.ZERO) < 0) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        statutColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatutFournisseur().toString()));

        typeColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTypeFournisseur().toString()));

        // Double-clic pour modifier
        fournisseursTable.setRowFactory(tv -> {
            TableRow<Fournisseur> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    modifierFournisseur(null);
                }
            });
            return row;
        });
    }

    private void setupComboBoxes() {
        filtreStatutCombo.getItems().addAll(
            "Tous les statuts",
            "ACTIF",
            "INACTIF",
            "SUSPENDU"
        );
        filtreStatutCombo.getSelectionModel().selectFirst();

        filtreTypeCombo.getItems().addAll(
            "Tous les types",
            "PARTICULIER",
            "ENTREPRISE",
            "ASSOCIATION",
            "ADMINISTRATION",
            "FREELANCE"
        );
        filtreTypeCombo.getSelectionModel().selectFirst();
    }

    private void loadFournisseurs() {
        try {
            if (authService.isUserLoggedIn()) {
                // Utiliser l'entreprise de l'utilisateur connecté
                Long entrepriseId = authService.getUtilisateurConnecte().getEntreprise().getId();
                List<Fournisseur> fournisseursList = fournisseurService.getFournisseursByEntreprise(entrepriseId);
                fournisseurs = FXCollections.observableArrayList(fournisseursList);
                fournisseursTable.setItems(fournisseurs);

                updateStatistics();
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des fournisseurs", e);
            showError("Erreur lors du chargement des fournisseurs: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        if (fournisseurs == null) return;

        int count = fournisseursTable.getItems().size();
        BigDecimal totalSolde = fournisseursTable.getItems().stream()
            .map(Fournisseur::getSoldeFournisseur)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalFournisseursLabel.setText("Total: " + count + " fournisseurs");
        totalSoldeLabel.setText("Solde total: " + currencyService.formatAmount(totalSolde));
    }

    @FXML
    private void rechercher(KeyEvent event) {
        filtrerFournisseurs();
    }

    private void filtrerFournisseurs() {
        String terme = rechercheField.getText().toLowerCase();

        if (fournisseurs == null) return;

        ObservableList<Fournisseur> filtered = fournisseurs.filtered(fournisseur -> {
            if (terme.isEmpty()) return true;

            return fournisseur.getNomComplet().toLowerCase().contains(terme) ||
                   (fournisseur.getEmail() != null && fournisseur.getEmail().toLowerCase().contains(terme)) ||
                   (fournisseur.getTelephone() != null && fournisseur.getTelephone().contains(terme)) ||
                   fournisseur.getCodeFournisseur().toLowerCase().contains(terme) ||
                   (fournisseur.getRaisonSociale() != null && fournisseur.getRaisonSociale().toLowerCase().contains(terme));
        });

        fournisseursTable.setItems(filtered);
        updateStatistics();
    }

    @FXML
    private void filtrerParStatut(ActionEvent event) {
        String statutSelectionne = filtreStatutCombo.getSelectionModel().getSelectedItem();
        applyFilters(statutSelectionne, filtreTypeCombo.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void filtrerParType(ActionEvent event) {
        String typeSelectionne = filtreTypeCombo.getSelectionModel().getSelectedItem();
        applyFilters(filtreStatutCombo.getSelectionModel().getSelectedItem(), typeSelectionne);
    }

    private void applyFilters(String statutSelectionne, String typeSelectionne) {
        if (fournisseurs == null) return;

        ObservableList<Fournisseur> filtered = fournisseurs.filtered(fournisseur -> {
            boolean statutMatch = statutSelectionne == null ||
                                  statutSelectionne.equals("Tous les statuts") ||
                                  fournisseur.getStatutFournisseur().toString().equals(statutSelectionne);

            boolean typeMatch = typeSelectionne == null ||
                                typeSelectionne.equals("Tous les types") ||
                                fournisseur.getTypeFournisseur().toString().equals(typeSelectionne);

            return statutMatch && typeMatch;
        });

        fournisseursTable.setItems(filtered);
        updateStatistics();
    }

    @FXML
    private void rafraichir(ActionEvent event) {
        loadFournisseurs();
    }

    @FXML
    private void ajouterFournisseur(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fournisseur-dialog.fxml"));
            Scene scene = new Scene(loader.load());

            Stage dialog = new Stage();
            dialog.setTitle("Nouveau Fournisseur");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(scene);
            dialog.setResizable(false);

            FournisseurDialogController controller = loader.getController();
            controller.setFournisseurService(fournisseurService);
            controller.setDialogStage(dialog);

            dialog.showAndWait();

            // Rafraîchir la liste si un fournisseur a été ajouté
            if (controller.isValidated()) {
                loadFournisseurs();
                showInfo("Fournisseur ajouté avec succès");
            }

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture du dialogue fournisseur", e);
            showError("Erreur lors de l'ouverture du dialogue: " + e.getMessage());
        }
    }

    @FXML
    private void modifierFournisseur(ActionEvent event) {
        Fournisseur selectedFournisseur = fournisseursTable.getSelectionModel().getSelectedItem();
        if (selectedFournisseur == null) {
            showWarning("Veuillez sélectionner un fournisseur à modifier");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fournisseur-dialog.fxml"));
            Scene scene = new Scene(loader.load());

            Stage dialog = new Stage();
            dialog.setTitle("Modifier Fournisseur - " + selectedFournisseur.getNomComplet());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(scene);
            dialog.setResizable(false);

            FournisseurDialogController controller = loader.getController();
            controller.setFournisseurService(fournisseurService);
            controller.setDialogStage(dialog);
            controller.setFournisseur(selectedFournisseur); // Pré-remplir avec les données existantes

            dialog.showAndWait();

            // Rafraîchir la liste si le fournisseur a été modifié
            if (controller.isValidated()) {
                loadFournisseurs();
                showInfo("Fournisseur modifié avec succès");
            }

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture du dialogue fournisseur", e);
            showError("Erreur lors de l'ouverture du dialogue: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerFournisseur(ActionEvent event) {
        Fournisseur selectedFournisseur = fournisseursTable.getSelectionModel().getSelectedItem();
        if (selectedFournisseur == null) {
            showWarning("Veuillez sélectionner un fournisseur à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le fournisseur");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer le fournisseur " +
                                   selectedFournisseur.getNomComplet() + " ?\n\n" +
                                   "Cette action supprimera également toutes les factures associées.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                fournisseurService.supprimerFournisseur(selectedFournisseur);
                loadFournisseurs();
                showInfo("Fournisseur supprimé avec succès");
            } catch (Exception e) {
                logger.error("Erreur lors de la suppression", e);
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void exporterFournisseurs(ActionEvent event) {
        showInfo("Export CSV en cours de développement");
    }

    @FXML
    private void imprimerFiches(ActionEvent event) {
        showInfo("Impression des fiches en cours de développement");
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
}