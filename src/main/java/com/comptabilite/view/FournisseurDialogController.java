package com.comptabilite.view;

import com.comptabilite.model.Fournisseur;
import com.comptabilite.service.AuthenticationService;
import com.comptabilite.service.FournisseurService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class FournisseurDialogController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(FournisseurDialogController.class);

    @FXML private TextField codeField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField raisonSocialeField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextArea adresseArea;
    @FXML private TextField codePostalField;
    @FXML private TextField villeField;
    @FXML private TextField paysField;
    @FXML private TextField siretField;
    @FXML private TextField numeroTVAField;
    @FXML private TextField personneContactField;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextArea notesArea;
    @FXML private Button annulerButton;
    @FXML private Button validerButton;

    private FournisseurService fournisseurService;
    private AuthenticationService authService;
    private Stage dialogStage;
    private Fournisseur fournisseur;
    private boolean validated = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.authService = AuthenticationService.getInstance();
        setupComboBoxes();
        setupValidation();

        // Validation initiale
        updateValidateButton();
    }

    private void setupComboBoxes() {
        typeCombo.getItems().addAll("PARTICULIER", "ENTREPRISE", "ASSOCIATION", "ADMINISTRATION", "FREELANCE");
        typeCombo.setValue("PARTICULIER");

        statutCombo.getItems().addAll("ACTIF", "INACTIF", "SUSPENDU");
        statutCombo.setValue("ACTIF");

        // Adapter l'interface selon le type
        typeCombo.setOnAction(e -> {
            boolean isEntreprise = "ENTREPRISE".equals(typeCombo.getValue()) ||
                                   "ASSOCIATION".equals(typeCombo.getValue()) ||
                                   "ADMINISTRATION".equals(typeCombo.getValue());
            siretField.setDisable(!isEntreprise);
            numeroTVAField.setDisable(!isEntreprise);
            prenomField.setDisable(isEntreprise);

            // Pour les entreprises, la raison sociale est obligatoire
            if (isEntreprise) {
                raisonSocialeField.setPromptText("Raison sociale obligatoire");
            } else {
                raisonSocialeField.setPromptText("Raison sociale (optionnel)");
            }

            // Déclencher la validation après changement de type
            updateValidateButton();
        });
    }

    private void setupValidation() {
        // Validation en temps réel
        nomField.textProperty().addListener((obs, oldVal, newVal) -> updateValidateButton());
        raisonSocialeField.textProperty().addListener((obs, oldVal, newVal) -> updateValidateButton());
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateValidateButton());
    }

    private void updateValidateButton() {
        // Validation basique : Le nom est toujours obligatoire
        boolean valid = nomField.getText() != null && !nomField.getText().trim().isEmpty();

        // Pour les entreprises, associations et administrations, vérifier aussi la raison sociale
        String typeValue = typeCombo.getValue();
        if (typeValue != null && ("ENTREPRISE".equals(typeValue) ||
            "ASSOCIATION".equals(typeValue) ||
            "ADMINISTRATION".equals(typeValue))) {

            String raisonSociale = raisonSocialeField.getText();
            valid = valid && (raisonSociale != null && !raisonSociale.trim().isEmpty());
        }

        validerButton.setDisable(!valid);
    }

    public void setFournisseurService(FournisseurService fournisseurService) {
        this.fournisseurService = fournisseurService;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
        if (fournisseur != null) {
            populateFields();
        } else {
            // Nouveau fournisseur - générer le code automatiquement
            if (fournisseurService != null) {
                try {
                    String codeGenere = fournisseurService.getFournisseursByEntreprise(1L).size() > 0 ?
                        String.format("F%04d", fournisseurService.getNombreFournisseurs() + 1) : "F0001";
                    codeField.setText(codeGenere);
                } catch (Exception e) {
                    logger.warn("Impossible de générer le code fournisseur", e);
                    codeField.setText("F0001");
                }
            }
        }

        // Déclencher la validation après l'initialisation
        updateValidateButton();
    }

    private void populateFields() {
        codeField.setText(fournisseur.getCodeFournisseur());
        typeCombo.setValue(fournisseur.getTypeFournisseur().toString());
        nomField.setText(fournisseur.getNom());
        prenomField.setText(fournisseur.getPrenom());
        raisonSocialeField.setText(fournisseur.getRaisonSociale());
        emailField.setText(fournisseur.getEmail());
        telephoneField.setText(fournisseur.getTelephone());
        adresseArea.setText(fournisseur.getAdresse());
        codePostalField.setText(fournisseur.getCodePostal());
        villeField.setText(fournisseur.getVille());
        paysField.setText(fournisseur.getPays());
        siretField.setText(fournisseur.getSiret());
        numeroTVAField.setText(fournisseur.getNumeroTVA());
        personneContactField.setText(fournisseur.getPersonneContact());
        statutCombo.setValue(fournisseur.getStatutFournisseur().toString());
        notesArea.setText(fournisseur.getNotes());
    }

    @FXML
    private void valider(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            if (fournisseur == null) {
                // Nouveau fournisseur
                fournisseur = new Fournisseur();
                // Assigner l'entreprise par défaut (ID = 1)
                com.comptabilite.dao.EntrepriseDAO entrepriseDAO = new com.comptabilite.dao.EntrepriseDAO();
                java.util.Optional<com.comptabilite.model.Entreprise> entrepriseOpt = java.util.Optional.ofNullable(authService.getUtilisateurConnecte().getEntreprise());
                if (entrepriseOpt.isPresent()) {
                    fournisseur.setEntreprise(entrepriseOpt.get());
                } else {
                    throw new RuntimeException("Entreprise par défaut introuvable");
                }
            }

            // Remplir les données
            fournisseur.setCodeFournisseur(codeField.getText().trim());
            fournisseur.setTypeFournisseur(Fournisseur.TypeFournisseur.valueOf(typeCombo.getValue()));
            fournisseur.setNom(nomField.getText().trim());
            fournisseur.setPrenom(prenomField.getText().trim());

            // Gérer la raison sociale
            String raisonSociale = raisonSocialeField.getText().trim();
            if (raisonSociale.isEmpty()) {
                // Si pas de raison sociale, utiliser le nom complet
                if (fournisseur.getTypeFournisseur() == Fournisseur.TypeFournisseur.PARTICULIER) {
                    StringBuilder nomComplet = new StringBuilder();
                    if (!prenomField.getText().trim().isEmpty()) {
                        nomComplet.append(prenomField.getText().trim()).append(" ");
                    }
                    nomComplet.append(nomField.getText().trim());
                    raisonSociale = nomComplet.toString();
                } else {
                    raisonSociale = nomField.getText().trim();
                }
            }

            // S'assurer qu'on a toujours une raison sociale (sécurité)
            if (raisonSociale == null || raisonSociale.trim().isEmpty()) {
                raisonSociale = nomField.getText().trim();
            }
            fournisseur.setRaisonSociale(raisonSociale);

            fournisseur.setEmail(emailField.getText().trim());
            fournisseur.setTelephone(telephoneField.getText().trim());
            fournisseur.setAdresse(adresseArea.getText().trim());
            fournisseur.setCodePostal(codePostalField.getText().trim());
            fournisseur.setVille(villeField.getText().trim());
            fournisseur.setPays(paysField.getText().trim());
            fournisseur.setSiret(siretField.getText().trim());
            fournisseur.setNumeroTVA(numeroTVAField.getText().trim());
            fournisseur.setPersonneContact(personneContactField.getText().trim());
            fournisseur.setStatutFournisseur(Fournisseur.StatutFournisseur.valueOf(statutCombo.getValue()));
            fournisseur.setNotes(notesArea.getText().trim());

            // S'assurer que les champs obligatoires sont définis
            if (fournisseur.getDateCreation() == null) {
                fournisseur.setDateCreation(java.time.LocalDateTime.now());
            }
            if (fournisseur.getActif() == null) {
                fournisseur.setActif(true);
            }

            // Sauvegarder
            if (fournisseur.getId() == null) {
                fournisseurService.creerFournisseur(fournisseur);
            } else {
                fournisseurService.modifierFournisseur(fournisseur);
            }

            validated = true;
            dialogStage.close();

        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde du fournisseur", e);
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

        // Pour les entreprises, la raison sociale est obligatoire
        boolean isEntreprise = "ENTREPRISE".equals(typeCombo.getValue()) ||
                               "ASSOCIATION".equals(typeCombo.getValue()) ||
                               "ADMINISTRATION".equals(typeCombo.getValue());

        if (isEntreprise && raisonSocialeField.getText().trim().isEmpty()) {
            errors.append("- La raison sociale est obligatoire pour les entreprises\n");
        }

        if (!emailField.getText().trim().isEmpty() && !isValidEmail(emailField.getText())) {
            errors.append("- L'email n'est pas valide\n");
        }

        if (isEntreprise && !siretField.getText().trim().isEmpty() && !isValidSiret(siretField.getText())) {
            errors.append("- Le SIRET n'est pas valide (14 chiffres)\n");
        }

        // Vérifier l'unicité du code
        if (fournisseurService != null && !codeField.getText().trim().isEmpty()) {
            boolean codeExists = false;
            if (fournisseur == null) {
                // Nouveau fournisseur
                codeExists = fournisseurService.codeExiste(codeField.getText().trim());
            } else {
                // Modification - vérifier seulement si le code a changé
                if (!fournisseur.getCodeFournisseur().equals(codeField.getText().trim())) {
                    codeExists = fournisseurService.codeExiste(codeField.getText().trim());
                }
            }

            if (codeExists) {
                errors.append("- Ce code fournisseur existe déjà\n");
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