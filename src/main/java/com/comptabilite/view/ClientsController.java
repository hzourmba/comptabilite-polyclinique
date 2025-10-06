package com.comptabilite.view;

import com.comptabilite.dao.ClientDAO;
import com.comptabilite.model.Client;
import com.comptabilite.service.AuthenticationService;
import com.comptabilite.service.ClientService;
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

public class ClientsController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ClientsController.class);

    @FXML private TextField rechercheField;
    @FXML private ComboBox<String> filtreStatutCombo;
    @FXML private TableView<Client> clientsTable;
    @FXML private TableColumn<Client, String> codeColumn;
    @FXML private TableColumn<Client, String> nomColumn;
    @FXML private TableColumn<Client, String> emailColumn;
    @FXML private TableColumn<Client, String> telephoneColumn;
    @FXML private TableColumn<Client, String> villeColumn;
    @FXML private TableColumn<Client, BigDecimal> soldeColumn;
    @FXML private TableColumn<Client, String> statutColumn;
    @FXML private Button ajouterButton;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;
    @FXML private Label totalClientsLabel;
    @FXML private Label totalSoldeLabel;

    private final ClientDAO clientDAO;
    private final AuthenticationService authService;
    private final ClientService clientService;
    private final CurrencyService currencyService;
    private ObservableList<Client> clients;

    public ClientsController() {
        this.clientDAO = new ClientDAO();
        this.authService = AuthenticationService.getInstance();
        this.clientService = new ClientService();
        this.currencyService = CurrencyService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupComboBoxes();
        loadClients();

        // Activer/désactiver les boutons selon les droits
        boolean canModify = authService.canModifyData();
        ajouterButton.setDisable(!canModify);
        modifierButton.setDisable(!canModify);
        supprimerButton.setDisable(!canModify);
    }

    private void setupTable() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("codeClient"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));

        soldeColumn.setCellValueFactory(new PropertyValueFactory<>("soldeClient"));
        soldeColumn.setCellFactory(col -> new TableCell<Client, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyService.formatAmountForTable(item));
                    // Colorer en rouge si négatif
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
                cellData.getValue().getStatutClient().toString()));

        // Double-clic pour modifier
        clientsTable.setRowFactory(tv -> {
            TableRow<Client> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    modifierClient(null);
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
    }

    private void loadClients() {
        try {
            if (authService.isUserLoggedIn()) {
                // Utiliser l'entreprise de l'utilisateur connecté
                Long entrepriseId = authService.getUtilisateurConnecte().getEntreprise().getId();
                List<Client> clientsList = clientService.getClientsByEntreprise(entrepriseId);
                clients = FXCollections.observableArrayList(clientsList);
                clientsTable.setItems(clients);

                updateStatistics();
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des clients", e);
            showError("Erreur lors du chargement des clients: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        if (clients == null) return;

        int count = clientsTable.getItems().size();
        BigDecimal totalSolde = clientsTable.getItems().stream()
            .map(Client::getSoldeClient)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalClientsLabel.setText("Total: " + count + " clients");
        totalSoldeLabel.setText("Solde total: " + currencyService.formatAmount(totalSolde));
    }

    @FXML
    private void rechercher(KeyEvent event) {
        filtrerClients();
    }

    private void filtrerClients() {
        String terme = rechercheField.getText().toLowerCase();

        if (clients == null) return;

        ObservableList<Client> filtered = clients.filtered(client -> {
            if (terme.isEmpty()) return true;

            return client.getNomComplet().toLowerCase().contains(terme) ||
                   (client.getEmail() != null && client.getEmail().toLowerCase().contains(terme)) ||
                   (client.getTelephone() != null && client.getTelephone().contains(terme)) ||
                   client.getCodeClient().toLowerCase().contains(terme);
        });

        clientsTable.setItems(filtered);
        updateStatistics();
    }

    @FXML
    private void filtrerParStatut(ActionEvent event) {
        String statutSelectionne = filtreStatutCombo.getSelectionModel().getSelectedItem();
        if (statutSelectionne == null || statutSelectionne.equals("Tous les statuts")) {
            clientsTable.setItems(clients);
        } else {
            ObservableList<Client> filtered = clients.filtered(client ->
                client.getStatutClient().toString().equals(statutSelectionne));
            clientsTable.setItems(filtered);
        }
        updateStatistics();
    }

    @FXML
    private void rafraichir(ActionEvent event) {
        loadClients();
    }

    @FXML
    private void ajouterClient(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/client-dialog.fxml"));
            Scene scene = new Scene(loader.load());

            Stage dialog = new Stage();
            dialog.setTitle("Nouveau Client");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(scene);
            dialog.setResizable(false);

            ClientDialogController controller = loader.getController();
            controller.setClientService(clientService);
            controller.setDialogStage(dialog);

            dialog.showAndWait();

            // Rafraîchir la liste si un client a été ajouté
            if (controller.isValidated()) {
                loadClients();
                showInfo("Client ajouté avec succès");
            }

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture du dialogue client", e);
            showError("Erreur lors de l'ouverture du dialogue: " + e.getMessage());
        }
    }

    @FXML
    private void modifierClient(ActionEvent event) {
        Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showWarning("Veuillez sélectionner un client à modifier");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/client-dialog.fxml"));
            Scene scene = new Scene(loader.load());

            Stage dialog = new Stage();
            dialog.setTitle("Modifier Client - " + selectedClient.getNomComplet());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(scene);
            dialog.setResizable(false);

            ClientDialogController controller = loader.getController();
            controller.setClientService(clientService);
            controller.setDialogStage(dialog);
            controller.setClient(selectedClient); // Pré-remplir avec les données existantes

            dialog.showAndWait();

            // Rafraîchir la liste si le client a été modifié
            if (controller.isValidated()) {
                loadClients();
                showInfo("Client modifié avec succès");
            }

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture du dialogue client", e);
            showError("Erreur lors de l'ouverture du dialogue: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerClient(ActionEvent event) {
        Client selectedClient = clientsTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showWarning("Veuillez sélectionner un client à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le client");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer le client " +
                                   selectedClient.getNomComplet() + " ?\n\n" +
                                   "Cette action supprimera également toutes les factures associées.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                clientService.supprimerClient(selectedClient);
                loadClients();
                showInfo("Client supprimé avec succès");
            } catch (Exception e) {
                logger.error("Erreur lors de la suppression", e);
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void exporterClients(ActionEvent event) {
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