package com.comptabilite.view;

import com.comptabilite.dao.EcritureComptableDAO;
import com.comptabilite.model.EcritureComptable;
import com.comptabilite.service.AuthenticationService;
import com.comptabilite.service.EcritureComptableService;
import com.comptabilite.service.CurrencyService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EcrituresComptablesController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(EcrituresComptablesController.class);

    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField rechercheField;
    @FXML private ComboBox<String> filtreStatutCombo;
    @FXML private TableView<EcritureComptable> ecrituresTable;
    @FXML private TableColumn<EcritureComptable, LocalDate> dateColumn;
    @FXML private TableColumn<EcritureComptable, String> numeroColumn;
    @FXML private TableColumn<EcritureComptable, String> libelleColumn;
    @FXML private TableColumn<EcritureComptable, String> referenceColumn;
    @FXML private TableColumn<EcritureComptable, BigDecimal> montantColumn;
    @FXML private TableColumn<EcritureComptable, String> statutColumn;
    @FXML private TableColumn<EcritureComptable, String> utilisateurColumn;
    @FXML private Button ajouterButton;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;
    @FXML private Label totalEcrituresLabel;
    @FXML private Label totalMontantLabel;

    private final EcritureComptableDAO ecritureDAO;
    private final AuthenticationService authService;
    private final EcritureComptableService ecritureService;
    private final CurrencyService currencyService;
    private ObservableList<EcritureComptable> ecritures;

    public EcrituresComptablesController() {
        this.ecritureDAO = new EcritureComptableDAO();
        this.authService = AuthenticationService.getInstance();
        this.ecritureService = new EcritureComptableService();
        this.currencyService = CurrencyService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupComboBoxes();
        setupDatePickers();
        loadEcritures();

        // Activer/désactiver les boutons selon les droits
        boolean canModify = authService.canModifyData();
        ajouterButton.setDisable(!canModify);
        modifierButton.setDisable(!canModify);
        supprimerButton.setDisable(!canModify);
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateEcriture"));
        dateColumn.setCellFactory(col -> new TableCell<EcritureComptable, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numeroEcriture"));
        libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        referenceColumn.setCellValueFactory(new PropertyValueFactory<>("reference"));

        montantColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getMontantTotal()));
        montantColumn.setCellFactory(col -> new TableCell<EcritureComptable, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyService.formatAmountForTable(item));
                }
            }
        });

        statutColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatut().toString()));

        utilisateurColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getUtilisateur().getNomComplet()));

        // Activer la sélection multiple
        ecrituresTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Double-clic pour modifier
        ecrituresTable.setRowFactory(tv -> {
            TableRow<EcritureComptable> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    modifierEcriture(null);
                }
            });
            return row;
        });
    }

    private void setupComboBoxes() {
        filtreStatutCombo.getItems().addAll(
            "Tous les statuts",
            "BROUILLON",
            "VALIDEE",
            "CLOTUREE"
        );
        filtreStatutCombo.getSelectionModel().selectFirst();
    }

    private void setupDatePickers() {
        // Période par défaut: mois courant
        LocalDate now = LocalDate.now();
        dateDebutPicker.setValue(now.withDayOfMonth(1));
        dateFinPicker.setValue(now.withDayOfMonth(now.lengthOfMonth()));
    }

    private void loadEcritures() {
        try {
            if (authService.isUserLoggedIn()) {
                // Utiliser l'entreprise de l'utilisateur connecté
                Long entrepriseId = authService.getUtilisateurConnecte().getEntreprise().getId();
                List<EcritureComptable> ecrituresList = ecritureService.getEcrituresByEntreprise(entrepriseId);
                ecritures = FXCollections.observableArrayList(ecrituresList);
                ecrituresTable.setItems(ecritures);

                updateStatistics();
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des écritures", e);
            showError("Erreur lors du chargement des écritures: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        if (ecritures == null) return;

        int count = ecrituresTable.getItems().size();
        BigDecimal total = ecrituresTable.getItems().stream()
            .map(EcritureComptable::getMontantTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalEcrituresLabel.setText("Total: " + count + " écritures");
        totalMontantLabel.setText("Montant total: " + currencyService.formatAmount(total));
    }

    @FXML
    private void rechercher(KeyEvent event) {
        filtrerEcritures();
    }

    @FXML
    private void rechercher(ActionEvent event) {
        filtrerEcritures();
    }

    private void filtrerEcritures() {
        String terme = rechercheField.getText().toLowerCase();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        if (ecritures == null) return;

        ObservableList<EcritureComptable> filtered = ecritures.filtered(ecriture -> {
            // Filtre par terme de recherche
            boolean matchTerme = terme.isEmpty() ||
                ecriture.getLibelle().toLowerCase().contains(terme) ||
                ecriture.getReference().toLowerCase().contains(terme) ||
                ecriture.getNumeroEcriture().toLowerCase().contains(terme);

            // Filtre par période
            boolean matchPeriode = true;
            if (dateDebut != null) {
                matchPeriode = !ecriture.getDateEcriture().isBefore(dateDebut);
            }
            if (dateFin != null && matchPeriode) {
                matchPeriode = !ecriture.getDateEcriture().isAfter(dateFin);
            }

            return matchTerme && matchPeriode;
        });

        ecrituresTable.setItems(filtered);
        updateStatistics();
    }

    @FXML
    private void filtrerParStatut(ActionEvent event) {
        String statutSelectionne = filtreStatutCombo.getSelectionModel().getSelectedItem();
        if (statutSelectionne == null || statutSelectionne.equals("Tous les statuts")) {
            ecrituresTable.setItems(ecritures);
        } else {
            ObservableList<EcritureComptable> filtered = ecritures.filtered(ecriture ->
                ecriture.getStatut().toString().equals(statutSelectionne));
            ecrituresTable.setItems(filtered);
        }
        updateStatistics();
    }

    @FXML
    private void ajouterEcriture(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ecriture-dialog.fxml"));
            Scene scene = new Scene(loader.load());

            Stage dialog = new Stage();
            dialog.setTitle("Nouvelle Écriture Comptable");
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setScene(scene);
            dialog.setResizable(true);
            dialog.setMinWidth(900);
            dialog.setMinHeight(600);

            EcritureDialogController controller = loader.getController();
            controller.setDialogStage(dialog);

            dialog.showAndWait();

            // Rafraîchir la liste si une écriture a été ajoutée
            if (controller.isValidated()) {
                loadEcritures();
            }

        } catch (Exception e) {
            logger.error("Erreur lors de l'ouverture du dialogue d'écriture", e);
            showError("Erreur lors de l'ouverture du dialogue: " + e.getMessage());
        }
    }

    @FXML
    private void modifierEcriture(ActionEvent event) {
        EcritureComptable selectedEcriture = ecrituresTable.getSelectionModel().getSelectedItem();
        if (selectedEcriture == null) {
            showWarning("Veuillez sélectionner une écriture à modifier");
            return;
        }

        if (selectedEcriture.getStatut() == EcritureComptable.StatutEcriture.CLOTUREE) {
            showWarning("Les écritures clôturées ne peuvent pas être modifiées");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ecriture-dialog.fxml"));
            Scene scene = new Scene(loader.load());

            Stage dialog = new Stage();
            dialog.setTitle("Modifier Écriture - " + selectedEcriture.getNumeroEcriture());
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setScene(scene);
            dialog.setResizable(true);
            dialog.setMinWidth(900);
            dialog.setMinHeight(600);

            EcritureDialogController controller = loader.getController();
            controller.setDialogStage(dialog);

            // Charger l'écriture avec toutes ses lignes
            EcritureComptable ecritureWithLignes = ecritureService.getEcritureWithLignes(selectedEcriture.getId());
            controller.setEcriture(ecritureWithLignes); // Pré-remplir avec les données existantes

            dialog.showAndWait();

            // Rafraîchir la liste si l'écriture a été modifiée
            if (controller.isValidated()) {
                loadEcritures();
            }

        } catch (Exception e) {
            logger.error("Erreur lors de l'ouverture du dialogue d'écriture", e);
            showError("Erreur lors de l'ouverture du dialogue: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerEcriture(ActionEvent event) {
        EcritureComptable selectedEcriture = ecrituresTable.getSelectionModel().getSelectedItem();
        if (selectedEcriture == null) {
            showWarning("Veuillez sélectionner une écriture à supprimer");
            return;
        }

        if (selectedEcriture.getStatut() != EcritureComptable.StatutEcriture.BROUILLON) {
            showWarning("Seules les écritures en brouillon peuvent être supprimées");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'écriture");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer l'écriture " +
                                   selectedEcriture.getNumeroEcriture() + " ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                ecritureService.supprimerEcriture(selectedEcriture);
                loadEcritures();
                showInfo("Écriture supprimée avec succès");
            } catch (Exception e) {
                logger.error("Erreur lors de la suppression", e);
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void exporterEcritures(ActionEvent event) {
        showInfo("Export PDF en cours de développement");
    }

    @FXML
    private void validerEcritures(ActionEvent event) {
        // Récupérer les écritures sélectionnées
        ObservableList<EcritureComptable> ecrituresSelectionnees = ecrituresTable.getSelectionModel().getSelectedItems();

        if (ecrituresSelectionnees.isEmpty()) {
            showWarning("Veuillez sélectionner au moins une écriture à valider.");
            return;
        }

        // Filtrer les écritures en brouillon
        List<EcritureComptable> ecrituresAValider = ecrituresSelectionnees.stream()
            .filter(e -> e.getStatut() == EcritureComptable.StatutEcriture.BROUILLON)
            .toList();

        if (ecrituresAValider.isEmpty()) {
            showWarning("Aucune écriture en statut BROUILLON sélectionnée. Seules les écritures en brouillon peuvent être validées.");
            return;
        }

        // Confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Validation des écritures");
        confirmation.setHeaderText("Confirmer la validation");
        confirmation.setContentText(String.format("Êtes-vous sûr de vouloir valider %d écriture(s) ?", ecrituresAValider.size()));

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // Valider les écritures
        int validees = 0;
        int erreurs = 0;
        StringBuilder messageErreurs = new StringBuilder();

        for (EcritureComptable ecriture : ecrituresAValider) {
            try {
                ecritureService.validerEcriture(ecriture);
                validees++;
                logger.info("Écriture {} validée avec succès", ecriture.getNumeroEcriture());
            } catch (Exception e) {
                erreurs++;
                logger.error("Erreur lors de la validation de l'écriture {}: {}", ecriture.getNumeroEcriture(), e.getMessage());
                messageErreurs.append(String.format("- Écriture %s : %s\n", ecriture.getNumeroEcriture(), e.getMessage()));
            }
        }

        // Afficher le résultat
        if (erreurs == 0) {
            showInfo(String.format("%d écriture(s) validée(s) avec succès.", validees));
        } else {
            showWarning(String.format("Validation terminée : %d succès, %d erreur(s).\n\nErreurs :\n%s",
                        validees, erreurs, messageErreurs.toString()));
        }

        // Rafraîchir la liste
        loadEcritures();
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