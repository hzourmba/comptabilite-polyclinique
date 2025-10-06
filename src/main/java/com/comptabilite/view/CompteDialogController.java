package com.comptabilite.view;

import com.comptabilite.dao.CompteDAO;
import com.comptabilite.model.Compte;
import com.comptabilite.service.AuthenticationService;
import com.comptabilite.service.CurrencyService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CompteDialogController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(CompteDialogController.class);

    @FXML private Label titleLabel;
    @FXML private TextField numeroField;
    @FXML private TextField libelleField;
    @FXML private ComboBox<Compte.TypeCompte> typeCombo;
    @FXML private ComboBox<Compte.ClasseCompte> classeCombo;
    @FXML private ComboBox<Compte> compteParentCombo;
    @FXML private CheckBox accepteSousComptesCheck;
    @FXML private TextField soldeDebiteurField;
    @FXML private TextField soldeCrediteurField;
    @FXML private Label currencyHintLabel;
    @FXML private CheckBox actifCheck;
    @FXML private CheckBox lettreageCheck;
    @FXML private CheckBox auxiliaireCheck;
    @FXML private TextArea descriptionArea;
    @FXML private Button annulerButton;
    @FXML private Button validerButton;

    private Stage dialogStage;
    private Compte compte;
    private boolean validated = false;
    private final CompteDAO compteDAO;
    private final AuthenticationService authService;
    private final CurrencyService currencyService;
    private List<Compte> allComptesParents; // Liste complète des comptes parents

    public CompteDialogController() {
        this.compteDAO = new CompteDAO();
        this.authService = AuthenticationService.getInstance();
        this.currencyService = CurrencyService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupValidation();
        setupCurrencyHints();
        setupAutoCompletions();
    }

    private void setupComboBoxes() {
        // Types de compte
        typeCombo.setItems(FXCollections.observableArrayList(Compte.TypeCompte.values()));

        // Classes comptables
        classeCombo.setItems(FXCollections.observableArrayList(Compte.ClasseCompte.values()));

        // Comptes parents (sera rempli dynamiquement)
        setupCompteParentCombo();
        loadComptesParents();

        // Listeners pour mise à jour automatique
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateValidation());
        classeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateClasseBasedFields();
            filterComptesParentsByClasse(); // Filtrer les comptes parents selon la classe
        });
    }

    private void setupCompteParentCombo() {
        // Configurateur d'affichage personnalisé pour le ComboBox
        compteParentCombo.setConverter(new javafx.util.StringConverter<Compte>() {
            @Override
            public String toString(Compte compte) {
                if (compte == null) {
                    return "";
                }
                return compte.getNumero() + " - " + compte.getLibelle();
            }

            @Override
            public Compte fromString(String string) {
                // Non utilisé pour un ComboBox en lecture seule
                return null;
            }
        });

        // Configurateur de cellule pour un meilleur affichage
        compteParentCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<Compte>() {
            @Override
            protected void updateItem(Compte compte, boolean empty) {
                super.updateItem(compte, empty);
                if (empty || compte == null) {
                    setText("");
                } else {
                    setText(compte.getNumero() + " - " + compte.getLibelle());
                }
            }
        });

        logger.info("StringConverter et CellFactory configurés pour le ComboBox");
    }

    private void setupValidation() {
        // Validation en temps réel
        numeroField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateNumeroCompte(newVal);
            updateValidation();
        });

        libelleField.textProperty().addListener((obs, oldVal, newVal) -> updateValidation());

        // Validation des montants
        setupMonetaryField(soldeDebiteurField);
        setupMonetaryField(soldeCrediteurField);
    }

    private void setupMonetaryField(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    currencyService.parseAmount(newVal);
                    field.setStyle(""); // Style normal
                } catch (NumberFormatException e) {
                    field.setStyle("-fx-border-color: red;"); // Erreur
                }
            } else {
                field.setStyle(""); // Style normal
            }
            updateValidation();
        });
    }

    private void setupCurrencyHints() {
        // Adapter les hints selon la monnaie
        String hint = currencyService.getAmountInputFormat();
        currencyHintLabel.setText(hint);

        soldeDebiteurField.setPromptText(currencyService.isOHADAEntreprise() ? "0" : "0,00");
        soldeCrediteurField.setPromptText(currencyService.isOHADAEntreprise() ? "0" : "0,00");
    }

    private void setupAutoCompletions() {
        // Auto-génération du numéro de compte basé sur la classe (seulement pour nouveaux comptes)
        classeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && compte == null) { // Seulement pour nouveaux comptes
                logger.info("=== CLASSE CHANGED: {} -> {} ===", oldVal, newVal);
                generateNumeroCompte();
            }
        });

        // Mise à jour du numéro quand le parent change (seulement pour nouveaux comptes)
        compteParentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            logger.info("=== PARENT CHANGED: {} -> {} ===",
                       oldVal != null ? oldVal.getNumeroEtLibelle() : "null",
                       newVal != null ? newVal.getNumeroEtLibelle() : "null");

            // Régénérer seulement pour les nouveaux comptes
            if (compte == null) {
                generateNumeroCompte();
            }
        });
    }

    private void loadComptesParents() {
        try {
            logger.info("=== DEBUG: Début de loadComptesParents ===");

            if (authService.isUserLoggedIn()) {
                logger.info("Utilisateur connecté: {}", authService.getUtilisateurConnecte().getNomUtilisateur());

                if (authService.getUtilisateurConnecte().getEntreprise() != null) {
                    Long entrepriseId = authService.getUtilisateurConnecte().getEntreprise().getId();
                    logger.info("Entreprise ID: {}", entrepriseId);

                    allComptesParents = compteDAO.getComptesParents(entrepriseId);
                    logger.info("Nombre de comptes parents trouvés: {}", allComptesParents.size());

                    for (Compte compte : allComptesParents) {
                        logger.info("Compte parent: {} - {} (accepte sous-comptes: {})",
                                compte.getNumero(), compte.getLibelle(), compte.isAccepteSousComptes());
                    }

                    // Afficher tous les comptes au départ, le filtrage se fera lors de la sélection de classe
                    compteParentCombo.setItems(FXCollections.observableArrayList(allComptesParents));
                    logger.info("ComboBox mis à jour avec {} éléments", allComptesParents.size());
                } else {
                    logger.error("L'utilisateur connecté n'a pas d'entreprise associée!");
                }
            } else {
                logger.error("Aucun utilisateur connecté!");
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des comptes parents", e);
        }
    }

    private void filterComptesParentsByClasse() {
        logger.info("=== DEBUG FILTRAGE: Début du filtrage ===");

        if (allComptesParents == null || allComptesParents.isEmpty()) {
            logger.error("allComptesParents est null ou vide!");
            return;
        }

        Compte.ClasseCompte classeSelectionnee = classeCombo.getValue();
        logger.info("Classe sélectionnée: {}", classeSelectionnee);

        if (classeSelectionnee == null) {
            // Aucune classe sélectionnée, afficher tous les comptes
            compteParentCombo.setItems(FXCollections.observableArrayList(allComptesParents));
            logger.info("Aucune classe sélectionnée, affichage de tous les {} comptes parents", allComptesParents.size());
            return;
        }

        // Debug: voir toutes les classes disponibles dans allComptesParents
        logger.info("Classes disponibles dans allComptesParents:");
        for (Compte compte : allComptesParents) {
            logger.info("  {} - {} (classe: {})", compte.getNumero(), compte.getLibelle(), compte.getClasseCompte());
        }

        // Filtrer les comptes par classe
        List<Compte> comptesFiltres = allComptesParents.stream()
                .filter(compte -> {
                    boolean match = compte.getClasseCompte() == classeSelectionnee;
                    logger.debug("Comparaison: {} == {} ? {}", compte.getClasseCompte(), classeSelectionnee, match);
                    return match;
                })
                .collect(java.util.stream.Collectors.toList());

        compteParentCombo.setItems(FXCollections.observableArrayList(comptesFiltres));

        logger.info("Classe {} sélectionnée, filtrage: {} comptes trouvés sur {} total",
                   classeSelectionnee, comptesFiltres.size(), allComptesParents.size());

        for (Compte compte : comptesFiltres) {
            logger.info("  - Compte parent filtré: {} - {}", compte.getNumero(), compte.getLibelle());
        }

        if (comptesFiltres.isEmpty()) {
            logger.warn("AUCUN COMPTE TROUVÉ pour la classe {}! Vérifiez la base de données.", classeSelectionnee);
        }
    }

    private void validateNumeroCompte(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            return;
        }

        // Vérifications selon le type d'entreprise
        boolean isOHADA = currencyService.isOHADAEntreprise();

        if (isOHADA) {
            // Numéro OHADA doit commencer par CM
            if (!numero.startsWith("CM")) {
                numeroField.setStyle("-fx-border-color: orange;");
                numeroField.setTooltip(new Tooltip("Les comptes OHADA doivent commencer par 'CM'"));
            } else {
                numeroField.setStyle("");
                numeroField.setTooltip(null);
            }
        } else {
            // Numéro français ne doit pas commencer par CM
            if (numero.startsWith("CM")) {
                numeroField.setStyle("-fx-border-color: orange;");
                numeroField.setTooltip(new Tooltip("Les comptes français ne doivent pas commencer par 'CM'"));
            } else {
                numeroField.setStyle("");
                numeroField.setTooltip(null);
            }
        }

        // Vérifier l'unicité
        if (isNumeroExists(numero)) {
            numeroField.setStyle("-fx-border-color: red;");
            numeroField.setTooltip(new Tooltip("Ce numéro de compte existe déjà"));
        }
    }

    private boolean isNumeroExists(String numero) {
        try {
            if (authService.isUserLoggedIn()) {
                Long entrepriseId = authService.getUtilisateurConnecte().getEntreprise().getId();
                Compte existing = compteDAO.getByNumero(numero, entrepriseId);
                return existing != null && (compte == null || !existing.getId().equals(compte.getId()));
            }
        } catch (Exception e) {
            logger.debug("Erreur lors de la vérification du numéro de compte", e);
        }
        return false;
    }

    private void generateNumeroCompte() {
        Compte.ClasseCompte classe = classeCombo.getValue();
        Compte parent = compteParentCombo.getValue();

        logger.info("=== GENERATE NUMERO START ===");
        logger.info("Classe sélectionnée: {}", classe);
        logger.info("Parent sélectionné: {}", parent != null ? parent.getNumeroEtLibelle() : "AUCUN");
        logger.info("ComboBox parent size: {}", compteParentCombo.getItems().size());
        logger.info("ComboBox parent selection model: {}", compteParentCombo.getSelectionModel().getSelectedItem());

        if (classe == null) {
            logger.warn("Classe null, génération annulée");
            return;
        }

        try {
            Long entrepriseId = authService.getUtilisateurConnecte().getEntreprise().getId();
            boolean isOHADA = currencyService.isOHADAEntreprise();
            logger.info("Entreprise: {}, OHADA: {}", entrepriseId, isOHADA);

            String nextNumber;

            if (parent != null) {
                // Si un parent est sélectionné, utiliser son numéro comme base
                String parentNumero = parent.getNumeroCompte();
                logger.info("=== MODE SOUS-COMPTE ===");
                logger.info("Numéro du parent: {}", parentNumero);

                // Vérifier d'abord tous les comptes existants pour ce parent
                logger.info("=== VERIFICATION AVANT GENERATION ===");
                List<Compte> existingSubAccounts = compteDAO.findSousComptes(parent.getId());
                logger.info("Sous-comptes existants pour parent {}: {}", parent.getId(), existingSubAccounts.size());
                for (Compte subAccount : existingSubAccounts) {
                    logger.info("  - Sous-compte existant: {} - {}", subAccount.getNumeroCompte(), subAccount.getLibelle());
                }

                // Utiliser directement le numéro du parent comme préfixe
                logger.info("=== APPEL getNextNumeroInHierarchy avec parent: {} ===", parentNumero);
                nextNumber = compteDAO.getNextNumeroInHierarchy(parentNumero, entrepriseId);
                logger.info("=== RETOUR getNextNumeroInHierarchy: {} ===", nextNumber);
                numeroField.setText(nextNumber);

                logger.info("Numéro généré pour sous-compte: {}", nextNumber);
            } else {
                // Pas de parent sélectionné, utiliser la classe
                logger.info("=== MODE COMPTE PRINCIPAL ===");
                String prefix = isOHADA ? "CM" + classe.getNumero() : classe.getNumero();
                logger.info("Préfixe de classe: {}", prefix);

                nextNumber = compteDAO.getNextNumeroInClasse(prefix, entrepriseId);
                numeroField.setText(nextNumber);

                logger.info("Numéro généré pour compte principal: {}", nextNumber);
            }

            logger.info("=== GENERATE NUMERO END: {} ===", nextNumber);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du numéro de compte", e);
        }
    }

    private void updateClasseBasedFields() {
        Compte.ClasseCompte classe = classeCombo.getValue();
        if (classe == null) return;

        // Pré-sélectionner le type de compte selon la classe
        switch (classe) {
            case CLASSE_1:
                typeCombo.setValue(Compte.TypeCompte.PASSIF);
                break;
            case CLASSE_2:
                typeCombo.setValue(Compte.TypeCompte.ACTIF);
                break;
            case CLASSE_3:
                typeCombo.setValue(Compte.TypeCompte.ACTIF);
                break;
            case CLASSE_4:
                typeCombo.setValue(Compte.TypeCompte.ACTIF_PASSIF);
                break;
            case CLASSE_5:
                typeCombo.setValue(Compte.TypeCompte.ACTIF);
                break;
            case CLASSE_6:
                typeCombo.setValue(Compte.TypeCompte.CHARGE);
                break;
            case CLASSE_7:
                typeCombo.setValue(Compte.TypeCompte.PRODUIT);
                break;
        }
    }

    private void updateValidation() {
        boolean isValid = isFormValid();
        validerButton.setDisable(!isValid);
    }

    private boolean isFormValid() {
        return !numeroField.getText().trim().isEmpty() &&
               !libelleField.getText().trim().isEmpty() &&
               typeCombo.getValue() != null &&
               classeCombo.getValue() != null &&
               !numeroField.getStyle().contains("red");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;

        if (compte == null) {
            titleLabel.setText("Nouveau Compte");
        } else {
            titleLabel.setText("Modifier le Compte");
            fillForm();
        }
    }

    private void fillForm() {
        if (compte == null) return;

        numeroField.setText(compte.getNumeroCompte());
        libelleField.setText(compte.getLibelle());
        typeCombo.setValue(compte.getTypeCompte());
        classeCombo.setValue(compte.getClasseCompte());

        if (compte.getCompteParent() != null) {
            compteParentCombo.setValue(compte.getCompteParent());
        }

        accepteSousComptesCheck.setSelected(compte.isAccepteSousComptes());

        if (compte.getSoldeDebiteur() != null) {
            soldeDebiteurField.setText(compte.getSoldeDebiteur().toString());
        }
        if (compte.getSoldeCrediteur() != null) {
            soldeCrediteurField.setText(compte.getSoldeCrediteur().toString());
        }

        actifCheck.setSelected(compte.isActif());
        lettreageCheck.setSelected(compte.isLettrable());
        auxiliaireCheck.setSelected(compte.isAuxiliaire());

        if (compte.getDescription() != null) {
            descriptionArea.setText(compte.getDescription());
        }
    }

    public boolean isValidated() {
        return validated;
    }

    @FXML
    private void valider(ActionEvent event) {
        if (!isFormValid()) {
            showError("Veuillez corriger les erreurs dans le formulaire");
            return;
        }

        try {
            if (compte == null) {
                compte = new Compte();
                compte.setEntreprise(authService.getUtilisateurConnecte().getEntreprise());
            }

            // Remplir l'objet compte
            compte.setNumeroCompte(numeroField.getText().trim());
            compte.setLibelle(libelleField.getText().trim());
            compte.setTypeCompte(typeCombo.getValue());
            compte.setClasseCompte(classeCombo.getValue());
            compte.setCompteParent(compteParentCombo.getValue());
            compte.setAccepteSousComptes(accepteSousComptesCheck.isSelected());

            // Soldes
            compte.setSoldeDebiteur(parseAmount(soldeDebiteurField.getText()));
            compte.setSoldeCrediteur(parseAmount(soldeCrediteurField.getText()));

            // Paramètres
            compte.setActif(actifCheck.isSelected());
            compte.setLettrable(lettreageCheck.isSelected());
            compte.setAuxiliaire(auxiliaireCheck.isSelected());
            compte.setDescription(descriptionArea.getText().trim());

            // Sauvegarder
            if (compte.getId() == null) {
                compteDAO.save(compte);
                logger.info("Nouveau compte créé: {}", compte.getNumeroEtLibelle());
            } else {
                compteDAO.update(compte);
                logger.info("Compte modifié: {}", compte.getNumeroEtLibelle());
            }

            validated = true;
            dialogStage.close();

        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde du compte", e);
            showError("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    private BigDecimal parseAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return currencyService.parseAmount(text);
    }

    @FXML
    private void annuler(ActionEvent event) {
        dialogStage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Compte getCompte() {
        return compte;
    }
}