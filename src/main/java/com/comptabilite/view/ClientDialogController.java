package com.comptabilite.view;

import com.comptabilite.model.Client;
import com.comptabilite.service.AuthenticationService;
import com.comptabilite.service.ClientService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientDialogController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ClientDialogController.class);

    @FXML private TextField codeField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextArea adresseArea;
    @FXML private TextField codePostalField;
    @FXML private TextField villeField;
    @FXML private TextField paysField;
    @FXML private TextField siretField;
    @FXML private TextField numeroTVAField;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextArea notesArea;
    @FXML private Button annulerButton;
    @FXML private Button validerButton;

    private ClientService clientService;
    private AuthenticationService authService;
    private Stage dialogStage;
    private Client client;
    private boolean validated = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.authService = AuthenticationService.getInstance();
        setupComboBoxes();
        setupValidation();
    }

    private void setupComboBoxes() {
        typeCombo.getItems().addAll("PARTICULIER", "ENTREPRISE");
        typeCombo.setValue("PARTICULIER");

        statutCombo.getItems().addAll("ACTIF", "INACTIF", "SUSPENDU");
        statutCombo.setValue("ACTIF");

        // Adapter l'interface selon le type
        typeCombo.setOnAction(e -> {
            boolean isEntreprise = "ENTREPRISE".equals(typeCombo.getValue());
            siretField.setDisable(!isEntreprise);
            numeroTVAField.setDisable(!isEntreprise);
            prenomField.setDisable(isEntreprise);
        });
    }

    private void setupValidation() {
        // Validation en temps réel
        nomField.textProperty().addListener((obs, oldVal, newVal) -> updateValidateButton());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> updateValidateButton());
    }

    private void updateValidateButton() {
        boolean valid = !nomField.getText().trim().isEmpty();
        validerButton.setDisable(!valid);
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setClient(Client client) {
        this.client = client;
        if (client != null) {
            populateFields();
        } else {
            // Nouveau client - générer le code automatiquement
            if (clientService != null) {
                try {
                    String codeGenere = clientService.genererCodeClient();
                    codeField.setText(codeGenere);
                } catch (Exception e) {
                    logger.warn("Impossible de générer le code client", e);
                }
            }
        }
    }

    private void populateFields() {
        codeField.setText(client.getCodeClient());
        typeCombo.setValue(client.getTypeClient().toString());
        nomField.setText(client.getNom());
        prenomField.setText(client.getPrenom());
        emailField.setText(client.getEmail());
        telephoneField.setText(client.getTelephone());
        adresseArea.setText(client.getAdresse());
        codePostalField.setText(client.getCodePostal());
        villeField.setText(client.getVille());
        paysField.setText(client.getPays());
        siretField.setText(client.getSiret());
        numeroTVAField.setText(client.getNumeroTVA());
        statutCombo.setValue(client.getStatutClient().toString());
        notesArea.setText(client.getNotes());
    }

    @FXML
    private void valider(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            if (client == null) {
                // Nouveau client
                client = new Client();
                // Assigner l'entreprise par défaut (ID = 1)
                com.comptabilite.dao.EntrepriseDAO entrepriseDAO = new com.comptabilite.dao.EntrepriseDAO();
                java.util.Optional<com.comptabilite.model.Entreprise> entrepriseOpt = java.util.Optional.ofNullable(authService.getUtilisateurConnecte().getEntreprise());
                if (entrepriseOpt.isPresent()) {
                    client.setEntreprise(entrepriseOpt.get());
                } else {
                    throw new RuntimeException("Entreprise par défaut introuvable");
                }
            }

            // Remplir les données
            client.setCodeClient(codeField.getText().trim());
            client.setTypeClient(Client.TypeClient.valueOf(typeCombo.getValue()));
            client.setNom(nomField.getText().trim());
            client.setPrenom(prenomField.getText().trim());

            // Définir la raison sociale basée sur le type
            String raisonSociale;
            if (client.getTypeClient() == Client.TypeClient.PARTICULIER) {
                raisonSociale = client.getNomComplet();
                if (raisonSociale == null || raisonSociale.trim().isEmpty()) {
                    raisonSociale = nomField.getText().trim();
                }
            } else {
                raisonSociale = nomField.getText().trim();
            }

            // S'assurer qu'on a toujours une valeur
            if (raisonSociale == null || raisonSociale.trim().isEmpty()) {
                raisonSociale = "Client sans nom";
            }

            client.setRaisonSociale(raisonSociale);

            // S'assurer que tous les champs obligatoires sont définis
            if (client.getDateCreation() == null) {
                client.setDateCreation(java.time.LocalDateTime.now());
            }
            if (client.getActif() == null) {
                client.setActif(true);
            }

            System.out.println("DEBUG Dialog: raisonSociale définie = '" + raisonSociale + "'");
            System.out.println("DEBUG Dialog: dateCreation = '" + client.getDateCreation() + "'");
            System.out.println("DEBUG Dialog: actif = '" + client.getActif() + "'");
            client.setEmail(emailField.getText().trim());
            client.setTelephone(telephoneField.getText().trim());
            client.setAdresse(adresseArea.getText().trim());
            client.setCodePostal(codePostalField.getText().trim());
            client.setVille(villeField.getText().trim());
            client.setPays(paysField.getText().trim());
            client.setSiret(siretField.getText().trim());
            client.setNumeroTVA(numeroTVAField.getText().trim());
            client.setStatutClient(Client.StatutClient.valueOf(statutCombo.getValue()));
            client.setNotes(notesArea.getText().trim());

            // Sauvegarder
            if (client.getId() == null) {
                clientService.creerClient(client);
            } else {
                clientService.modifierClient(client);
            }

            validated = true;
            dialogStage.close();

        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde du client", e);
            showError("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    @FXML
    private void annuler(ActionEvent event) {
        dialogStage.close();
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (nomField.getText().trim().isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        }

        if (!emailField.getText().trim().isEmpty() && !isValidEmail(emailField.getText())) {
            errors.append("- L'email n'est pas valide\n");
        }

        if ("ENTREPRISE".equals(typeCombo.getValue())) {
            if (!siretField.getText().trim().isEmpty() && !isValidSiret(siretField.getText())) {
                errors.append("- Le SIRET n'est pas valide (14 chiffres)\n");
            }
        }

        if (errors.length() > 0) {
            showError("Erreurs de validation:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidSiret(String siret) {
        return siret.replaceAll("\\s", "").matches("\\d{14}");
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