package com.comptabilite.view;

import com.comptabilite.model.Facture;
import com.comptabilite.model.Client;
import com.comptabilite.model.Fournisseur;
import com.comptabilite.service.FactureService;
import com.comptabilite.service.ClientService;
import com.comptabilite.service.FournisseurService;
import com.comptabilite.service.AuthenticationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FacturesController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(FacturesController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TextField rechercheField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> statutCombo;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;

    @FXML private TableView<Facture> facturesTable;
    @FXML private TableColumn<Facture, String> numeroColumn;
    @FXML private TableColumn<Facture, String> dateColumn;
    @FXML private TableColumn<Facture, String> typeColumn;
    @FXML private TableColumn<Facture, String> partenaireColumn;
    @FXML private TableColumn<Facture, String> objetColumn;
    @FXML private TableColumn<Facture, BigDecimal> montantColumn;
    @FXML private TableColumn<Facture, String> statutColumn;
    @FXML private TableColumn<Facture, String> echeanceColumn;

    @FXML private Button nouvelleFactureButton;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;
    @FXML private Button envoyerButton;
    @FXML private Button marquerPayeeButton;
    @FXML private Button voirEcrituresButton;

    private FactureService factureService;
    private ClientService clientService;
    private FournisseurService fournisseurService;
    private ObservableList<Facture> facturesData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        factureService = new FactureService();
        clientService = new ClientService();
        fournisseurService = new FournisseurService();
        facturesData = FXCollections.observableArrayList();

        setupTable();
        setupFilters();
        setupButtons();
        loadFactures();
    }

    private void setupTable() {
        facturesTable.setItems(facturesData);

        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numeroFacture"));

        dateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDateFacture().format(DATE_FORMATTER)));

        typeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(getTypeDisplayName(cellData.getValue().getTypeFacture())));

        partenaireColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getNomPartenaire()));

        objetColumn.setCellValueFactory(new PropertyValueFactory<>("objet"));
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montantTTC"));

        statutColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(getStatutDisplayName(cellData.getValue().getStatut())));

        echeanceColumn.setCellValueFactory(cellData -> {
            LocalDate echeance = cellData.getValue().getDateEcheance();
            return new SimpleStringProperty(echeance != null ? echeance.format(DATE_FORMATTER) : "-");
        });

        // Style conditionnel pour les factures en retard
        facturesTable.setRowFactory(tv -> new TableRow<Facture>() {
            @Override
            protected void updateItem(Facture facture, boolean empty) {
                super.updateItem(facture, empty);
                if (empty || facture == null) {
                    setStyle("");
                } else if (facture.isEnRetard()) {
                    setStyle("-fx-background-color: #ffebee;");
                } else if (facture.getStatut() == Facture.StatutFacture.PAYEE) {
                    setStyle("-fx-background-color: #e8f5e8;");
                } else {
                    setStyle("");
                }
            }
        });

        // Sélection
        facturesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> updateButtonStates());
    }

    private void setupFilters() {
        typeCombo.setItems(FXCollections.observableArrayList(
            "Tous", "Ventes", "Achats", "Avoirs vente", "Avoirs achat"));
        typeCombo.setValue("Tous");

        statutCombo.setItems(FXCollections.observableArrayList(
            "Tous", "Brouillon", "Envoyées", "Payées", "Annulées", "En retard"));
        statutCombo.setValue("Tous");

        // Listeners pour le filtrage automatique
        rechercheField.textProperty().addListener((obs, oldVal, newVal) -> filterFactures());
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterFactures());
        statutCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterFactures());
        dateDebutPicker.valueProperty().addListener((obs, oldVal, newVal) -> filterFactures());
        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> filterFactures());
    }

    private void setupButtons() {
        updateButtonStates();
    }

    private void updateButtonStates() {
        Facture selectedFacture = facturesTable.getSelectionModel().getSelectedItem();
        boolean hasSelection = selectedFacture != null;

        modifierButton.setDisable(!hasSelection);
        supprimerButton.setDisable(!hasSelection ||
            (hasSelection && selectedFacture.getStatut() != Facture.StatutFacture.BROUILLON));
        envoyerButton.setDisable(!hasSelection ||
            (hasSelection && selectedFacture.getStatut() != Facture.StatutFacture.BROUILLON));
        marquerPayeeButton.setDisable(!hasSelection ||
            (hasSelection && selectedFacture.getStatut() != Facture.StatutFacture.ENVOYEE));
        voirEcrituresButton.setDisable(!hasSelection);
    }

    @FXML
    private void nouvelleFacture(ActionEvent event) {
        try {
            // Demander le type de facture
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Vente", "Vente", "Achat");
            dialog.setTitle("Nouvelle Facture");
            dialog.setHeaderText("Type de facture");
            dialog.setContentText("Choisissez le type de facture :");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                Facture.TypeFacture typeFacture = "Vente".equals(result.get()) ?
                    Facture.TypeFacture.VENTE : Facture.TypeFacture.ACHAT;

                ouvrirDialogFacture(null, typeFacture);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'ouverture du dialog de création", e);
            showError("Erreur lors de la création de facture: " + e.getMessage());
        }
    }

    @FXML
    private void modifierFacture(ActionEvent event) {
        Facture selectedFacture = facturesTable.getSelectionModel().getSelectedItem();
        if (selectedFacture != null) {
            ouvrirDialogFacture(selectedFacture, selectedFacture.getTypeFacture());
        }
    }

    @FXML
    private void supprimerFacture(ActionEvent event) {
        Facture selectedFacture = facturesTable.getSelectionModel().getSelectedItem();
        if (selectedFacture != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer la facture");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer la facture " +
                selectedFacture.getNumeroFacture() + " ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    factureService.supprimerFacture(selectedFacture);
                    loadFactures();
                    showInfo("Facture supprimée avec succès.");
                } catch (Exception e) {
                    logger.error("Erreur lors de la suppression", e);
                    showError("Erreur lors de la suppression: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void envoyerFacture(ActionEvent event) {
        Facture selectedFacture = facturesTable.getSelectionModel().getSelectedItem();
        if (selectedFacture != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Envoyer la facture");
            alert.setContentText("Envoyer la facture " + selectedFacture.getNumeroFacture() +
                " ?\n\nCela générera automatiquement l'écriture comptable correspondante.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    factureService.envoyerFacture(selectedFacture);
                    loadFactures();
                    showInfo("Facture envoyée et écriture comptable générée.");
                } catch (Exception e) {
                    logger.error("Erreur lors de l'envoi", e);
                    showError("Erreur lors de l'envoi: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void marquerPayee(ActionEvent event) {
        Facture selectedFacture = facturesTable.getSelectionModel().getSelectedItem();
        if (selectedFacture != null) {
            // Dialog pour saisir la date de paiement
            Dialog<LocalDate> dialog = new Dialog<>();
            dialog.setTitle("Marquer comme payée");
            dialog.setHeaderText("Paiement de la facture " + selectedFacture.getNumeroFacture());

            ButtonType okButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            DatePicker datePicker = new DatePicker(LocalDate.now());
            dialog.getDialogPane().setContent(datePicker);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return datePicker.getValue();
                }
                return null;
            });

            Optional<LocalDate> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    factureService.marquerPayee(selectedFacture, result.get());
                    loadFactures();
                    showInfo("Facture marquée comme payée et écriture de paiement générée.");
                } catch (Exception e) {
                    logger.error("Erreur lors du marquage de paiement", e);
                    showError("Erreur lors du paiement: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void voirEcritures(ActionEvent event) {
        Facture selectedFacture = facturesTable.getSelectionModel().getSelectedItem();
        if (selectedFacture != null) {
            // TODO: Ouvrir la vue des écritures comptables liées à cette facture
            showInfo("Fonctionnalité 'Voir les écritures' en cours de développement.");
        }
    }

    private void ouvrirDialogFacture(Facture facture, Facture.TypeFacture typeFacture) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/facture-dialog.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            FactureDialogController controller = fxmlLoader.getController();
            controller.setFactureService(factureService);
            controller.setClientService(clientService);
            controller.setFournisseurService(fournisseurService);

            Stage stage = new Stage();
            stage.setTitle(facture == null ? "Nouvelle Facture" : "Modifier Facture");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);

            controller.setDialogStage(stage);
            controller.setFacture(facture, typeFacture);

            stage.showAndWait();

            if (controller.isValidated()) {
                loadFactures();
            }

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture du dialog facture", e);
            showError("Erreur lors de l'ouverture du dialog: " + e.getMessage());
        }
    }

    private void loadFactures() {
        try {
            Long entrepriseId = AuthenticationService.getInstance().getUtilisateurConnecte().getEntreprise().getId();
            List<Facture> factures = factureService.getFacturesByEntreprise(entrepriseId);
            facturesData.clear();
            facturesData.addAll(factures);
            filterFactures();
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des factures", e);
            showError("Erreur lors du chargement: " + e.getMessage());
        }
    }

    private void filterFactures() {
        try {
            Long entrepriseId = AuthenticationService.getInstance().getUtilisateurConnecte().getEntreprise().getId();
            List<Facture> factures;

            // Appliquer les filtres
            String searchText = rechercheField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                factures = factureService.rechercherFactures(searchText.trim(), entrepriseId);
            } else {
                factures = factureService.getFacturesByEntreprise(entrepriseId);
            }

            // Filtrer par type
            String typeFilter = typeCombo.getValue();
            if (!"Tous".equals(typeFilter)) {
                factures = factures.stream()
                    .filter(f -> matchesTypeFilter(f, typeFilter))
                    .toList();
            }

            // Filtrer par statut
            String statutFilter = statutCombo.getValue();
            if (!"Tous".equals(statutFilter)) {
                factures = factures.stream()
                    .filter(f -> matchesStatutFilter(f, statutFilter))
                    .toList();
            }

            // Filtrer par période
            LocalDate dateDebut = dateDebutPicker.getValue();
            LocalDate dateFin = dateFinPicker.getValue();
            if (dateDebut != null || dateFin != null) {
                factures = factures.stream()
                    .filter(f -> matchesDateFilter(f, dateDebut, dateFin))
                    .toList();
            }

            facturesData.clear();
            facturesData.addAll(factures);

        } catch (Exception e) {
            logger.error("Erreur lors du filtrage", e);
        }
    }

    private boolean matchesTypeFilter(Facture facture, String typeFilter) {
        return switch (typeFilter) {
            case "Ventes" -> facture.getTypeFacture() == Facture.TypeFacture.VENTE;
            case "Achats" -> facture.getTypeFacture() == Facture.TypeFacture.ACHAT;
            case "Avoirs vente" -> facture.getTypeFacture() == Facture.TypeFacture.AVOIR_VENTE;
            case "Avoirs achat" -> facture.getTypeFacture() == Facture.TypeFacture.AVOIR_ACHAT;
            default -> true;
        };
    }

    private boolean matchesStatutFilter(Facture facture, String statutFilter) {
        return switch (statutFilter) {
            case "Brouillon" -> facture.getStatut() == Facture.StatutFacture.BROUILLON;
            case "Envoyées" -> facture.getStatut() == Facture.StatutFacture.ENVOYEE;
            case "Payées" -> facture.getStatut() == Facture.StatutFacture.PAYEE;
            case "Annulées" -> facture.getStatut() == Facture.StatutFacture.ANNULEE;
            case "En retard" -> facture.isEnRetard();
            default -> true;
        };
    }

    private boolean matchesDateFilter(Facture facture, LocalDate dateDebut, LocalDate dateFin) {
        LocalDate dateFacture = facture.getDateFacture();
        if (dateDebut != null && dateFacture.isBefore(dateDebut)) {
            return false;
        }
        if (dateFin != null && dateFacture.isAfter(dateFin)) {
            return false;
        }
        return true;
    }

    private String getTypeDisplayName(Facture.TypeFacture type) {
        return switch (type) {
            case VENTE -> "Vente";
            case ACHAT -> "Achat";
            case AVOIR_VENTE -> "Avoir vente";
            case AVOIR_ACHAT -> "Avoir achat";
        };
    }

    private String getStatutDisplayName(Facture.StatutFacture statut) {
        return switch (statut) {
            case BROUILLON -> "Brouillon";
            case ENVOYEE -> "Envoyée";
            case PAYEE -> "Payée";
            case ANNULEE -> "Annulée";
            case EN_RETARD -> "En retard";
        };
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}