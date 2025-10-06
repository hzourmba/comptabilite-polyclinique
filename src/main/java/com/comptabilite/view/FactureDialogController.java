package com.comptabilite.view;

import com.comptabilite.model.*;
import com.comptabilite.service.FactureService;
import com.comptabilite.service.ClientService;
import com.comptabilite.service.FournisseurService;
import com.comptabilite.service.AuthenticationService;
import com.comptabilite.service.CurrencyService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class FactureDialogController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(FactureDialogController.class);

    @FXML private TextField numeroField;
    @FXML private DatePicker dateFacturePicker;
    @FXML private DatePicker dateEcheancePicker;
    @FXML private TextField objetField;
    @FXML private Label typeLabel;
    @FXML private ComboBox<Object> partenaireCombo;
    @FXML private ComboBox<String> modePaiementCombo;
    @FXML private TextField tauxTVAField;
    @FXML private TextArea commentairesArea;

    @FXML private TableView<LigneFacture> lignesTable;
    @FXML private TableColumn<LigneFacture, String> designationColumn;
    @FXML private TableColumn<LigneFacture, BigDecimal> quantiteColumn;
    @FXML private TableColumn<LigneFacture, BigDecimal> prixUnitaireColumn;
    @FXML private TableColumn<LigneFacture, BigDecimal> montantHTColumn;

    @FXML private TextField designationLigneField;
    @FXML private TextField quantiteField;
    @FXML private TextField prixUnitaireField;

    @FXML private Label montantHTLabel;
    @FXML private Label montantTVALabel;
    @FXML private Label montantTTCLabel;

    @FXML private Button ajouterLigneButton;
    @FXML private Button supprimerLigneButton;
    @FXML private Button annulerButton;
    @FXML private Button validerButton;

    private FactureService factureService;
    private ClientService clientService;
    private FournisseurService fournisseurService;
    private CurrencyService currencyService;
    private Stage dialogStage;
    private Facture facture;
    private Facture.TypeFacture typeFacture;
    private boolean validated = false;
    private ObservableList<LigneFacture> lignesData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lignesData = FXCollections.observableArrayList();
        setupTable();
        setupValidation();
        setupCalculations();
        setupModePaiement();

        // Valeurs par défaut
        dateFacturePicker.setValue(LocalDate.now());
        dateEcheancePicker.setValue(LocalDate.now().plusDays(30));
        // Initialiser les services
        currencyService = CurrencyService.getInstance();

        // Taux de TVA selon le pays
        tauxTVAField.setText(String.valueOf(currencyService.getDefaultVATRate()));
        quantiteField.setText("1");

        updateTotals();
    }

    private void setupTable() {
        lignesTable.setItems(lignesData);

        designationColumn.setCellValueFactory(new PropertyValueFactory<>("designation"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixUnitaireColumn.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        montantHTColumn.setCellValueFactory(new PropertyValueFactory<>("montantHT"));

        // Sélection
        lignesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                supprimerLigneButton.setDisable(newSelection == null);
                if (newSelection != null) {
                    designationLigneField.setText(newSelection.getDesignation());
                    quantiteField.setText(newSelection.getQuantite().toString());
                    prixUnitaireField.setText(newSelection.getPrixUnitaire().toString());
                }
            });
    }

    private void setupValidation() {
        // Validation en temps réel
        numeroField.textProperty().addListener((obs, oldVal, newVal) -> updateValidateButton());
        objetField.textProperty().addListener((obs, oldVal, newVal) -> updateValidateButton());
        partenaireCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateValidateButton());
        dateFacturePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateValidateButton());

        // Validation des champs numériques
        tauxTVAField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                tauxTVAField.setText(oldVal);
            } else {
                updateTotals();
            }
        });

        quantiteField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                quantiteField.setText(oldVal);
            }
        });

        prixUnitaireField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                prixUnitaireField.setText(oldVal);
            }
        });
    }

    private void setupCalculations() {
        // Recalcul automatique des totaux quand les lignes changent
        lignesData.addListener((javafx.collections.ListChangeListener.Change<? extends LigneFacture> change) -> {
            updateTotals();
        });
    }

    private void setupModePaiement() {
        modePaiementCombo.getItems().addAll(
            "Espèces",
            "Chèque",
            "Carte bancaire",
            "Virement",
            "Prélèvement",
            "Traite",
            "À crédit"
        );
        modePaiementCombo.setValue("Chèque"); // Valeur par défaut
    }

    public void setFactureService(FactureService factureService) {
        this.factureService = factureService;
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }

    public void setFournisseurService(FournisseurService fournisseurService) {
        this.fournisseurService = fournisseurService;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setFacture(Facture facture, Facture.TypeFacture typeFacture) {
        this.facture = facture;
        this.typeFacture = typeFacture;

        // Afficher le type de facture
        if (typeFacture != null) {
            typeLabel.setText(typeFacture.name());
        }

        // Charger la liste des partenaires selon le type
        loadPartenaires();

        if (facture != null) {
            // Mode modification
            populateFields();
        } else {
            // Mode création - générer le numéro
            generateNumeroFacture();
        }

        updateValidateButton();
    }

    private void loadPartenaires() {
        try {
            Long entrepriseId = AuthenticationService.getInstance().getUtilisateurConnecte().getEntreprise().getId();
            partenaireCombo.getItems().clear();

            if (typeFacture == Facture.TypeFacture.VENTE || typeFacture == Facture.TypeFacture.AVOIR_VENTE) {
                // Charger les clients
                List<Client> clients = clientService.getClientsByEntreprise(entrepriseId);
                partenaireCombo.getItems().addAll(clients);
                partenaireCombo.setPromptText("Sélectionnez un client");

                partenaireCombo.setCellFactory(listView -> new ListCell<Object>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else if (item instanceof Client client) {
                            setText(client.getRaisonSociale());
                        }
                    }
                });

                partenaireCombo.setButtonCell(new ListCell<Object>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else if (item instanceof Client client) {
                            setText(client.getRaisonSociale());
                        }
                    }
                });

            } else {
                // Charger les fournisseurs
                List<Fournisseur> fournisseurs = fournisseurService.getFournisseursByEntreprise(entrepriseId);
                partenaireCombo.getItems().addAll(fournisseurs);
                partenaireCombo.setPromptText("Sélectionnez un fournisseur");

                partenaireCombo.setCellFactory(listView -> new ListCell<Object>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else if (item instanceof Fournisseur fournisseur) {
                            setText(fournisseur.getRaisonSociale());
                        }
                    }
                });

                partenaireCombo.setButtonCell(new ListCell<Object>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else if (item instanceof Fournisseur fournisseur) {
                            setText(fournisseur.getRaisonSociale());
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des partenaires", e);
            showError("Erreur lors du chargement des partenaires: " + e.getMessage());
        }
    }

    private void generateNumeroFacture() {
        try {
            if (factureService != null) {
                Long entrepriseId = AuthenticationService.getInstance().getUtilisateurConnecte().getEntreprise().getId();
                String numeroGenere = factureService.getFacturesByEntreprise(entrepriseId).size() > 0 ?
                    (typeFacture == Facture.TypeFacture.VENTE ? "VTE" : "ACH") +
                    LocalDate.now().getYear() +
                    String.format("%04d", factureService.getFacturesByEntreprise(entrepriseId).size() + 1) :
                    (typeFacture == Facture.TypeFacture.VENTE ? "VTE" : "ACH") +
                    LocalDate.now().getYear() + "0001";
                numeroField.setText(numeroGenere);
            }
        } catch (Exception e) {
            logger.warn("Impossible de générer le numéro de facture", e);
            numeroField.setText((typeFacture == Facture.TypeFacture.VENTE ? "VTE" : "ACH") +
                LocalDate.now().getYear() + "0001");
        }
    }

    private void populateFields() {
        numeroField.setText(facture.getNumeroFacture());
        dateFacturePicker.setValue(facture.getDateFacture());
        dateEcheancePicker.setValue(facture.getDateEcheance());
        objetField.setText(facture.getObjet());
        tauxTVAField.setText(facture.getTauxTVA().toString());
        commentairesArea.setText(facture.getCommentaires());

        // Sélectionner le partenaire
        if (facture.getClient() != null) {
            partenaireCombo.setValue(facture.getClient());
        } else if (facture.getFournisseur() != null) {
            partenaireCombo.setValue(facture.getFournisseur());
        }

        // Charger les lignes
        lignesData.clear();
        lignesData.addAll(facture.getLignes());
    }

    @FXML
    private void ajouterLigne(ActionEvent event) {
        if (validateLigneFields()) {
            try {
                String designation = designationLigneField.getText().trim();
                BigDecimal quantite = new BigDecimal(quantiteField.getText());
                BigDecimal prixUnitaire = new BigDecimal(prixUnitaireField.getText());

                LigneFacture ligne = new LigneFacture(designation, quantite, prixUnitaire);
                lignesData.add(ligne);

                // Vider les champs
                designationLigneField.clear();
                quantiteField.setText("1");
                prixUnitaireField.clear();

                designationLigneField.requestFocus();

            } catch (NumberFormatException e) {
                showError("Erreur dans les valeurs numériques.");
            }
        }
    }

    @FXML
    private void supprimerLigne(ActionEvent event) {
        LigneFacture selectedLigne = lignesTable.getSelectionModel().getSelectedItem();
        if (selectedLigne != null) {
            lignesData.remove(selectedLigne);
        }
    }

    @FXML
    private void valider(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            if (facture == null) {
                // Nouvelle facture
                facture = new Facture();
                facture.setTypeFacture(typeFacture);

                // Assigner l'entreprise de l'utilisateur connecté
                Utilisateur utilisateur = AuthenticationService.getInstance().getUtilisateurConnecte();
                if (utilisateur != null && utilisateur.getEntreprise() != null) {
                    facture.setEntreprise(utilisateur.getEntreprise());
                } else {
                    throw new RuntimeException("Aucune entreprise associée à l'utilisateur connecté");
                }
            }

            // Remplir les données
            facture.setNumeroFacture(numeroField.getText().trim());
            facture.setDateFacture(dateFacturePicker.getValue());
            facture.setDateEcheance(dateEcheancePicker.getValue());
            facture.setObjet(objetField.getText().trim());
            facture.setTauxTVA(new BigDecimal(tauxTVAField.getText()));
            facture.setCommentaires(commentairesArea.getText().trim());

            // Assigner le partenaire
            Object selectedPartenaire = partenaireCombo.getValue();
            if (selectedPartenaire instanceof Client client) {
                facture.setClient(client);
                facture.setFournisseur(null);
            } else if (selectedPartenaire instanceof Fournisseur fournisseur) {
                facture.setFournisseur(fournisseur);
                facture.setClient(null);
            }

            // Gérer les lignes
            facture.getLignes().clear();
            for (LigneFacture ligne : lignesData) {
                facture.ajouterLigne(ligne);
            }

            // Calculer les montants
            facture.calculerMontants();

            // Sauvegarder
            if (facture.getId() == null) {
                factureService.creerFacture(facture);
            } else {
                factureService.modifierFacture(facture);
            }

            validated = true;
            dialogStage.close();

        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde de la facture", e);
            showError("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    @FXML
    private void annuler(ActionEvent event) {
        dialogStage.close();
    }

    private void updateValidateButton() {
        boolean valid = numeroField.getText() != null && !numeroField.getText().trim().isEmpty() &&
                       objetField.getText() != null && !objetField.getText().trim().isEmpty() &&
                       partenaireCombo.getValue() != null &&
                       dateFacturePicker.getValue() != null;

        validerButton.setDisable(!valid);
    }

    private boolean validateLigneFields() {
        if (designationLigneField.getText() == null || designationLigneField.getText().trim().isEmpty()) {
            showError("La désignation est obligatoire.");
            return false;
        }

        try {
            BigDecimal quantite = new BigDecimal(quantiteField.getText());
            if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
                showError("La quantité doit être positive.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Quantité invalide.");
            return false;
        }

        try {
            BigDecimal prix = new BigDecimal(prixUnitaireField.getText());
            if (prix.compareTo(BigDecimal.ZERO) < 0) {
                showError("Le prix unitaire ne peut pas être négatif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Prix unitaire invalide.");
            return false;
        }

        return true;
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (numeroField.getText().trim().isEmpty()) {
            errors.append("- Le numéro de facture est obligatoire\n");
        }

        if (objetField.getText().trim().isEmpty()) {
            errors.append("- L'objet est obligatoire\n");
        }

        if (partenaireCombo.getValue() == null) {
            String partenaireType = (typeFacture == Facture.TypeFacture.VENTE ||
                typeFacture == Facture.TypeFacture.AVOIR_VENTE) ? "client" : "fournisseur";
            errors.append("- Le " + partenaireType + " est obligatoire\n");
        }

        if (dateFacturePicker.getValue() == null) {
            errors.append("- La date de facture est obligatoire\n");
        }

        if (lignesData.isEmpty()) {
            errors.append("- Au moins une ligne est requise\n");
        }

        try {
            BigDecimal taux = new BigDecimal(tauxTVAField.getText());
            if (taux.compareTo(BigDecimal.ZERO) < 0 || taux.compareTo(new BigDecimal("100")) > 0) {
                errors.append("- Le taux de TVA doit être entre 0 et 100\n");
            }
        } catch (NumberFormatException e) {
            errors.append("- Le taux de TVA est invalide\n");
        }

        if (errors.length() > 0) {
            showError("Erreurs de validation:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void updateTotals() {
        BigDecimal montantHT = lignesData.stream()
            .map(LigneFacture::getMontantHT)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tauxTVA = BigDecimal.ZERO;
        try {
            tauxTVA = new BigDecimal(tauxTVAField.getText());
        } catch (NumberFormatException e) {
            // Ignorer l'erreur et utiliser 0
        }

        BigDecimal montantTVA = montantHT.multiply(tauxTVA.divide(new BigDecimal("100")));
        BigDecimal montantTTC = montantHT.add(montantTVA);

        montantHTLabel.setText(currencyService.formatAmount(montantHT));
        montantTVALabel.setText(currencyService.formatAmount(montantTVA));
        montantTTCLabel.setText(currencyService.formatAmount(montantTTC));
    }

    public boolean isValidated() {
        return validated;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}