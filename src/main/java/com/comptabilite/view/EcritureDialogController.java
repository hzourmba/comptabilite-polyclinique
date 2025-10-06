package com.comptabilite.view;

import com.comptabilite.dao.CompteDAO;
import com.comptabilite.model.Compte;
import com.comptabilite.model.EcritureComptable;
import com.comptabilite.model.LigneEcriture;
import com.comptabilite.service.AuthenticationService;
import com.comptabilite.service.EcritureComptableService;
import com.comptabilite.service.CurrencyService;
import javafx.beans.property.SimpleObjectProperty;
//import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
//import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
//import javafx.scene.control.cell.TextFieldTableCell;
//import javafx.stage.Modality;
import javafx.stage.Stage;
//import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ArrayList;

public class EcritureDialogController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(EcritureDialogController.class);

    @FXML private TextField numeroField;
    @FXML private DatePicker datePicker;
    @FXML private TextField libelleField;
    @FXML private TextField journalField;
    @FXML private TextField referenceField;
    @FXML private TableView<LigneEcritureRow> lignesTable;
    @FXML private TableColumn<LigneEcritureRow, Compte> compteColumn;
    @FXML private TableColumn<LigneEcritureRow, String> compteLibelleColumn;
    @FXML private TableColumn<LigneEcritureRow, String> ligneLibelleColumn;
    @FXML private TableColumn<LigneEcritureRow, BigDecimal> debitColumn;
    @FXML private TableColumn<LigneEcritureRow, BigDecimal> creditColumn;
    @FXML private Button ajouterLigneButton;
    @FXML private Button supprimerLigneButton;
    @FXML private Label equilibreLabel;
    @FXML private Label totalDebitLabel;
    @FXML private Label totalCreditLabel;
    @FXML private Label differenceLabel;
    @FXML private Button annulerButton;
    @FXML private Button sauvegarderBrouillonButton;
    @FXML private Button validerButton;

    private Stage dialogStage;
    private EcritureComptable ecriture;
    private boolean validated = false;
    private final EcritureComptableService ecritureService;
    private final AuthenticationService authService;
    private final CompteDAO compteDAO;
    private final CurrencyService currencyService;
    private ObservableList<LigneEcritureRow> lignesData;
    private List<Compte> comptesDisponibles;

    public EcritureDialogController() {
        this.ecritureService = new EcritureComptableService();
        this.authService = AuthenticationService.getInstance();
        this.compteDAO = new CompteDAO();
        this.currencyService = CurrencyService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadComptes(); // Charger les comptes AVANT de configurer la table
        setupTable();
        setupValidation();

        // Valeurs par défaut
        datePicker.setValue(LocalDate.now());
        lignesData = FXCollections.observableArrayList();
        lignesTable.setItems(lignesData);

        // Ajouter une première ligne vide
        ajouterLigne(null);
        ajouterLigne(null);

        updateTotals();
    }

    private void setupTable() {
        // Configuration des colonnes
        compteColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCompte()));
        compteColumn.setCellFactory(column -> new ComboBoxTableCell<LigneEcritureRow, Compte>(
            FXCollections.observableArrayList(comptesDisponibles != null ? comptesDisponibles : new ArrayList<>())) {

            @Override
            public void startEdit() {
                // Recharger la liste avant chaque édition pour s'assurer qu'elle est à jour
                if (comptesDisponibles != null) {
                    getItems().setAll(comptesDisponibles);
                }
                super.startEdit();
            }
            @Override
            public void updateItem(Compte item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNumeroCompte() + " - " + item.getLibelle());
                }
            }
        });
        compteColumn.setOnEditCommit(event -> {
            LigneEcritureRow row = event.getRowValue();
            Compte selectedCompte = event.getNewValue();

            // =========================
            // DEBUG: Sélection de compte dans ComboBox
            // =========================
            logger.debug("=== COMBOBOX COMPTE SÉLECTIONÉ ===");
            logger.debug("Ancien compte: {}", event.getOldValue() != null ?
                event.getOldValue().getNumeroCompte() + " (ID: " + event.getOldValue().getId() + ")" : "NULL");
            logger.debug("Nouveau compte sélectionné: {}", selectedCompte != null ?
                selectedCompte.getNumeroCompte() + " (ID: " + selectedCompte.getId() + ")" : "NULL");

            if (selectedCompte != null) {
                logger.debug("Détails du compte sélectionné:");
                logger.debug("  - Libellé: {}", selectedCompte.getLibelle());
                logger.debug("  - Classe: {}", selectedCompte.getClass().getSimpleName());
                logger.debug("  - ID: {}", selectedCompte.getId());
                logger.debug("  - Numéro: {}", selectedCompte.getNumeroCompte());
            }

            row.setCompte(selectedCompte);
            updateCompteLibelle(row);
            lignesTable.refresh();

            // Vérification après assignation
            logger.debug("Après assignation - Compte dans row: {}",
                row.getCompte() != null ? row.getCompte().getNumeroCompte() + " (ID: " + row.getCompte().getId() + ")" : "NULL");

            // Force la persistance des données
            if (event.getTableColumn().getTableView() != null) {
                event.getTableColumn().getTableView().edit(-1, null);
            }
        });

        compteLibelleColumn.setCellValueFactory(cellData ->
            cellData.getValue().compteLibelleProperty());

        ligneLibelleColumn.setCellValueFactory(cellData ->
            cellData.getValue().libelleProperty());
        ligneLibelleColumn.setCellFactory(column -> new TableCell<LigneEcritureRow, String>() {
            private TextField textField;

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                    return;
                }
                super.startEdit();

                if (isEditing()) {
                    if (textField == null) {
                        textField = new TextField();
                        textField.setOnAction(evt -> commitEdit(textField.getText()));
                        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                            if (!isNowFocused) {
                                commitEdit(textField.getText());
                            }
                        });
                    }
                    textField.setText(getItem() != null ? getItem() : "");
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                    textField.requestFocus();
                }
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                LigneEcritureRow row = getTableRow().getItem();
                if (row != null) {
                    row.setLibelle(newValue != null ? newValue : "");
                }
                setText(newValue);
                setGraphic(null);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            textField.setText(item != null ? item : "");
                        }
                        setText(null);
                        setGraphic(textField);
                    } else {
                        setText(item != null ? item : "");
                        setGraphic(null);
                    }
                }
            }
        });

        debitColumn.setCellValueFactory(cellData ->
            cellData.getValue().montantDebitProperty());
        debitColumn.setCellFactory(column -> new TableCell<LigneEcritureRow, BigDecimal>() {
            private TextField textField;

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                    return;
                }
                super.startEdit();

                if (isEditing()) {
                    if (textField == null) {
                        textField = new TextField();
                        textField.setOnAction(evt -> {
                            BigDecimal value = parseValue(textField.getText());
                            commitEdit(value);
                        });
                        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                            if (!isNowFocused) {
                                BigDecimal value = parseValue(textField.getText());
                                commitEdit(value);
                            }
                        });
                    }
                    String displayText = getItem() != null && getItem().compareTo(BigDecimal.ZERO) > 0 ?
                        String.format("%.2f", getItem().doubleValue()) : "";
                    textField.setText(displayText);
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                    textField.requestFocus();
                }
            }

            private BigDecimal parseValue(String text) {
                try {
                    return text.isEmpty() ? BigDecimal.ZERO : new BigDecimal(text.replace(",", "."));
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO;
                }
            }

            @Override
            public void commitEdit(BigDecimal newValue) {
                super.commitEdit(newValue);
                LigneEcritureRow row = getTableRow().getItem();
                if (row != null) {
                    if (newValue == null) newValue = BigDecimal.ZERO;
                    row.setMontantDebit(newValue);
                    if (newValue.compareTo(BigDecimal.ZERO) > 0) {
                        row.setMontantCredit(BigDecimal.ZERO);
                    }
                    updateTotals();
                }
                updateDisplay(newValue);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                updateDisplay(getItem());
            }

            private void updateDisplay(BigDecimal value) {
                if (value != null && value.compareTo(BigDecimal.ZERO) > 0) {
                    setText(String.format("%.2f", value.doubleValue()));
                } else {
                    setText("");
                }
                setGraphic(null);
            }

            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            String displayText = item != null && item.compareTo(BigDecimal.ZERO) > 0 ?
                                String.format("%.2f", item.doubleValue()) : "";
                            textField.setText(displayText);
                        }
                        setText(null);
                        setGraphic(textField);
                    } else {
                        updateDisplay(item);
                    }
                }
            }
        });

        creditColumn.setCellValueFactory(cellData ->
            cellData.getValue().montantCreditProperty());
        creditColumn.setCellFactory(column -> new TableCell<LigneEcritureRow, BigDecimal>() {
            private TextField textField;

            @Override
            public void startEdit() {
                if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                    return;
                }
                super.startEdit();

                if (isEditing()) {
                    if (textField == null) {
                        textField = new TextField();
                        textField.setOnAction(evt -> {
                            BigDecimal value = parseValue(textField.getText());
                            commitEdit(value);
                        });
                        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                            if (!isNowFocused) {
                                BigDecimal value = parseValue(textField.getText());
                                commitEdit(value);
                            }
                        });
                    }
                    String displayText = getItem() != null && getItem().compareTo(BigDecimal.ZERO) > 0 ?
                        String.format("%.2f", getItem().doubleValue()) : "";
                    textField.setText(displayText);
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                    textField.requestFocus();
                }
            }

            private BigDecimal parseValue(String text) {
                try {
                    return text.isEmpty() ? BigDecimal.ZERO : new BigDecimal(text.replace(",", "."));
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO;
                }
            }

            @Override
            public void commitEdit(BigDecimal newValue) {
                super.commitEdit(newValue);
                LigneEcritureRow row = getTableRow().getItem();
                if (row != null) {
                    if (newValue == null) newValue = BigDecimal.ZERO;
                    row.setMontantCredit(newValue);
                    if (newValue.compareTo(BigDecimal.ZERO) > 0) {
                        row.setMontantDebit(BigDecimal.ZERO);
                    }
                    updateTotals();
                }
                updateDisplay(newValue);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                updateDisplay(getItem());
            }

            private void updateDisplay(BigDecimal value) {
                if (value != null && value.compareTo(BigDecimal.ZERO) > 0) {
                    setText(String.format("%.2f", value.doubleValue()));
                } else {
                    setText("");
                }
                setGraphic(null);
            }

            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            String displayText = item != null && item.compareTo(BigDecimal.ZERO) > 0 ?
                                String.format("%.2f", item.doubleValue()) : "";
                            textField.setText(displayText);
                        }
                        setText(null);
                        setGraphic(textField);
                    } else {
                        updateDisplay(item);
                    }
                }
            }
        });

        // Rendre la table éditable
        lignesTable.setEditable(true);
    }

    private void setupValidation() {
        // Validation en temps réel
        libelleField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void loadComptes() {
        try {
            Long entrepriseId = authService.getUtilisateurConnecte().getEntreprise().getId(); // Entreprise de l'utilisateur
            comptesDisponibles = compteDAO.findByEntreprise(entrepriseId);
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des comptes", e);
            showError("Erreur lors du chargement des comptes: " + e.getMessage());
            comptesDisponibles = FXCollections.observableArrayList();
        }
    }

    private void updateCompteLibelle(LigneEcritureRow row) {
        if (row.getCompte() != null) {
            row.setCompteLibelle(row.getCompte().getLibelle());
        } else {
            row.setCompteLibelle("");
        }
    }

    @FXML
    private void ajouterLigne(ActionEvent event) {
        LigneEcritureRow nouvelleLigne = new LigneEcritureRow();
        lignesData.add(nouvelleLigne);
        updateTotals();
    }

    @FXML
    private void supprimerLigne(ActionEvent event) {
        LigneEcritureRow selectedLigne = lignesTable.getSelectionModel().getSelectedItem();
        if (selectedLigne != null) {
            lignesData.remove(selectedLigne);
            updateTotals();
        } else {
            showWarning("Veuillez sélectionner une ligne à supprimer");
        }
    }

    private void updateTotals() {
        BigDecimal totalDebit = lignesData.stream()
            .map(LigneEcritureRow::getMontantDebit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = lignesData.stream()
            .map(LigneEcritureRow::getMontantCredit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal difference = totalDebit.subtract(totalCredit);

        totalDebitLabel.setText(currencyService.formatAmount(totalDebit));
        totalCreditLabel.setText(currencyService.formatAmount(totalCredit));
        differenceLabel.setText(currencyService.formatAmount(difference));

        // Mise à jour du statut d'équilibre
        boolean equilibree = difference.compareTo(BigDecimal.ZERO) == 0 &&
                           totalDebit.compareTo(BigDecimal.ZERO) > 0;

        if (equilibree) {
            equilibreLabel.setText("Équilibre: Équilibrée");
            equilibreLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        } else {
            equilibreLabel.setText("Équilibre: Non équilibrée");
            equilibreLabel.setTextFill(javafx.scene.paint.Color.RED);
        }

        // Colorer la différence
        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            differenceLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        } else {
            differenceLabel.setTextFill(javafx.scene.paint.Color.RED);
        }

        validateForm();
    }

    private void validateForm() {
        boolean valid = isFormValid();
        validerButton.setDisable(!valid);
        sauvegarderBrouillonButton.setDisable(!isBasicFormValid());
    }

    private boolean isBasicFormValid() {
        return libelleField.getText() != null && !libelleField.getText().trim().isEmpty() &&
               datePicker.getValue() != null &&
               lignesData.stream().anyMatch(l -> l.getCompte() != null &&
                   (l.getMontantDebit().compareTo(BigDecimal.ZERO) > 0 ||
                    l.getMontantCredit().compareTo(BigDecimal.ZERO) > 0));
    }

    private boolean isFormValid() {
        if (!isBasicFormValid()) return false;

        // Vérifier l'équilibre
        BigDecimal totalDebit = lignesData.stream()
            .map(LigneEcritureRow::getMontantDebit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lignesData.stream()
            .map(LigneEcritureRow::getMontantCredit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalDebit.compareTo(totalCredit) == 0 && totalDebit.compareTo(BigDecimal.ZERO) > 0;
    }

    @FXML
    private void sauvegarderBrouillon(ActionEvent event) {
        sauvegarder(EcritureComptable.StatutEcriture.BROUILLON);
    }

    @FXML
    private void validerEcriture(ActionEvent event) {
        if (!isFormValid()) {
            showError("L'écriture doit être équilibrée pour être validée");
            return;
        }
        sauvegarder(EcritureComptable.StatutEcriture.VALIDEE);
    }

    private void sauvegarder(EcritureComptable.StatutEcriture statut) {
        try {
            if (ecriture == null) {
                ecriture = new EcritureComptable();
                ecriture.setUtilisateur(authService.getUtilisateurConnecte());

                // Pour l'instant, utiliser des valeurs par défaut
                // TODO: Implémenter la sélection d'entreprise et d'exercice
                try {
                    com.comptabilite.dao.EntrepriseDAO entrepriseDAO = new com.comptabilite.dao.EntrepriseDAO();
                    com.comptabilite.dao.ExerciceDAO exerciceDAO = new com.comptabilite.dao.ExerciceDAO();

                    // Utiliser l'entreprise de l'utilisateur connecté
                    Long entrepriseId = authService.getUtilisateurConnecte().getEntreprise().getId();
                    java.util.Optional<com.comptabilite.model.Entreprise> optEntreprise = entrepriseDAO.findById(entrepriseId);
                    if (optEntreprise.isPresent()) {
                        ecriture.setEntreprise(optEntreprise.get());
                    }

                    java.util.List<com.comptabilite.model.Exercice> exercices = exerciceDAO.findByEntreprise(entrepriseId);
                    if (!exercices.isEmpty()) {
                        ecriture.setExercice(exercices.get(0)); // Premier exercice trouvé
                    }
                } catch (Exception e) {
                    logger.warn("Impossible de charger l'entreprise/exercice par défaut", e);
                }
            }

            // Mettre à jour les données de base
            ecriture.setDateEcriture(datePicker.getValue());
            ecriture.setLibelle(libelleField.getText().trim());
            ecriture.setNumeroJournal(journalField.getText());
            ecriture.setReferencePiece(referenceField.getText());
            ecriture.setStatut(statut);

            // =========================
            // DEBUG: Conversion des lignes UI vers modèle
            // =========================
            logger.debug("=== CONVERSION LIGNES UI VERS MODÈLE ===");
            logger.debug("Nombre de lignes dans la table: {}", lignesData.size());

            // Convertir les lignes
            ecriture.getLignes().clear();
            int ligneIndex = 0;
            for (LigneEcritureRow row : lignesData) {
                logger.debug("Traitement ligne UI {}: Compte={}, CompteID={}, Débit={}, Crédit={}",
                    ligneIndex,
                    row.getCompte() != null ? row.getCompte().getNumeroCompte() : "NULL",
                    row.getCompte() != null ? row.getCompte().getId() : "NULL",
                    row.getMontantDebit(),
                    row.getMontantCredit());

                if (row.getCompte() != null &&
                    (row.getMontantDebit().compareTo(BigDecimal.ZERO) > 0 ||
                    row.getMontantCredit().compareTo(BigDecimal.ZERO) > 0)) {

                    logger.debug("Ligne {} valide, tentative rechargement compte...", ligneIndex);

                    // CORRECTION: Recharger le compte depuis la base pour éviter les entités détachées
                    Compte compteFromDB = null;
                    if (row.getCompte().getId() != null) {
                        logger.debug("Rechargement compte ID: {} depuis DAO...", row.getCompte().getId());
                        Optional<Compte> optCompte = compteDAO.findById(row.getCompte().getId());
                        if (optCompte.isPresent()) {
                            compteFromDB = optCompte.get();
                            logger.debug("Compte rechargé avec succès: {} (ID: {})",
                                compteFromDB.getNumeroCompte(), compteFromDB.getId());
                        } else {
                            logger.error("ERREUR: Compte ID {} introuvable dans la base!", row.getCompte().getId());
                        }
                    } else {
                        logger.error("ERREUR: Ligne {} - Compte a un ID NULL!", ligneIndex);
                    }

                    if (compteFromDB != null) {
                        LigneEcriture ligne = new LigneEcriture();
                        ligne.setCompte(compteFromDB);  // Utiliser l'entité rechargée
                        ligne.setLibelle(row.getLibelle());
                        ligne.setMontantDebit(row.getMontantDebit());
                        ligne.setMontantCredit(row.getMontantCredit());
                        ecriture.ajouterLigne(ligne);
                        logger.debug("Ligne {} ajoutée à l'écriture avec compte ID: {}", ligneIndex, compteFromDB.getId());
                    } else {
                        logger.error("ERREUR: Impossible de recharger le compte ID: {}",
                                        (row.getCompte().getId() != null ? row.getCompte().getId() : "NULL"));
                        logger.error("Cette ligne sera ignorée!");
                    }
                } else {
                    logger.debug("Ligne {} ignorée: Compte NULL ou montants à zéro", ligneIndex);
                }
                ligneIndex++;
            }

            logger.debug("Après conversion: {} lignes ajoutées à l'écriture", ecriture.getLignes().size());
            /** 
            // Convertir les lignes
            ecriture.getLignes().clear();
            for (LigneEcritureRow row : lignesData) {
                if (row.getCompte() != null &&
                    (row.getMontantDebit().compareTo(BigDecimal.ZERO) > 0 ||
                     row.getMontantCredit().compareTo(BigDecimal.ZERO) > 0)) {

                    LigneEcriture ligne = new LigneEcriture();
                    ligne.setCompte(row.getCompte());
                    ligne.setLibelle(row.getLibelle());
                    ligne.setMontantDebit(row.getMontantDebit());
                    ligne.setMontantCredit(row.getMontantCredit());
                    ecriture.ajouterLigne(ligne);
                }
            }
*/
            // =========================
            // DEBUG: État final avant envoi au service
            // =========================
            logger.debug("=== ÉTAT FINAL AVANT SERVICE ===");
            logger.debug("Écriture: {} (ID: {})", ecriture.getNumeroEcriture(), ecriture.getId());
            logger.debug("Nombre de lignes dans l'écriture: {}", ecriture.getLignes().size());

            for (int i = 0; i < ecriture.getLignes().size(); i++) {
                LigneEcriture ligne = ecriture.getLignes().get(i);
                logger.debug("Ligne {} finale: Compte={}, CompteID={}, Débit={}, Crédit={}",
                    i,
                    ligne.getCompte() != null ? ligne.getCompte().getNumeroCompte() : "NULL",
                    ligne.getCompte() != null ? ligne.getCompte().getId() : "NULL",
                    ligne.getMontantDebit(),
                    ligne.getMontantCredit());
            }

            // Sauvegarder
            logger.debug("Appel ecritureService.sauvegarderEcriture()...");
            ecritureService.sauvegarderEcriture(ecriture);
            logger.debug("ecritureService.sauvegarderEcriture() terminé avec succès");

            validated = true;
            dialogStage.close();

            String message = statut == EcritureComptable.StatutEcriture.VALIDEE ?
                "Écriture validée avec succès" : "Écriture sauvegardée en brouillon";
            showInfo(message);

        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde", e);
            showError("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    @FXML
    private void annuler(ActionEvent event) {
        dialogStage.close();
    }

    // Classe interne pour représenter une ligne dans la table
    public static class LigneEcritureRow {
        private Compte compte;
        private final javafx.beans.property.SimpleStringProperty compteLibelle = new javafx.beans.property.SimpleStringProperty("");
        private final javafx.beans.property.SimpleStringProperty libelle = new javafx.beans.property.SimpleStringProperty("");
        private final javafx.beans.property.SimpleObjectProperty<BigDecimal> montantDebit = new javafx.beans.property.SimpleObjectProperty<>(BigDecimal.ZERO);
        private final javafx.beans.property.SimpleObjectProperty<BigDecimal> montantCredit = new javafx.beans.property.SimpleObjectProperty<>(BigDecimal.ZERO);

        // Getters et setters
        public Compte getCompte() { return compte; }
        public void setCompte(Compte compte) { this.compte = compte; }

        public String getCompteLibelle() { return compteLibelle.get(); }
        public void setCompteLibelle(String compteLibelle) { this.compteLibelle.set(compteLibelle != null ? compteLibelle : ""); }
        public javafx.beans.property.SimpleStringProperty compteLibelleProperty() { return compteLibelle; }

        public String getLibelle() { return libelle.get(); }
        public void setLibelle(String libelle) { this.libelle.set(libelle != null ? libelle : ""); }
        public javafx.beans.property.SimpleStringProperty libelleProperty() { return libelle; }

        public BigDecimal getMontantDebit() { return montantDebit.get(); }
        public void setMontantDebit(BigDecimal montantDebit) {
            this.montantDebit.set(montantDebit != null ? montantDebit : BigDecimal.ZERO);
        }
        public javafx.beans.property.SimpleObjectProperty<BigDecimal> montantDebitProperty() { return montantDebit; }

        public BigDecimal getMontantCredit() { return montantCredit.get(); }
        public void setMontantCredit(BigDecimal montantCredit) {
            this.montantCredit.set(montantCredit != null ? montantCredit : BigDecimal.ZERO);
        }
        public javafx.beans.property.SimpleObjectProperty<BigDecimal> montantCreditProperty() { return montantCredit; }
    }

    // Méthodes utilitaires
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setEcriture(EcritureComptable ecriture) {
        this.ecriture = ecriture;
        if (ecriture != null) {
            // Pré-remplir le formulaire pour modification
            numeroField.setText(ecriture.getNumeroEcriture());
            datePicker.setValue(ecriture.getDateEcriture());
            libelleField.setText(ecriture.getLibelle());
            journalField.setText(ecriture.getNumeroJournal());
            referenceField.setText(ecriture.getReferencePiece());

            // Charger les lignes existantes
            lignesData.clear();
            for (LigneEcriture ligne : ecriture.getLignes()) {
                LigneEcritureRow row = new LigneEcritureRow();
                row.setCompte(ligne.getCompte());
                row.setCompteLibelle(ligne.getCompte().getLibelle());
                row.setLibelle(ligne.getLibelle());
                row.setMontantDebit(ligne.getMontantDebit());
                row.setMontantCredit(ligne.getMontantCredit());
                lignesData.add(row);
            }
            updateTotals();
        }
    }

    public boolean isValidated() {
        return validated;
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