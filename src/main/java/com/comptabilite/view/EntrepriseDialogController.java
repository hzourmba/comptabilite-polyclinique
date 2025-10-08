package com.comptabilite.view;

import com.comptabilite.model.Entreprise;
import com.comptabilite.model.Utilisateur;
import com.comptabilite.service.CurrencyService;
import com.comptabilite.service.EntrepriseInitializationService;
import com.comptabilite.service.AuthenticationService;
import com.comptabilite.dao.EntrepriseDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class EntrepriseDialogController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(EntrepriseDialogController.class);

    @FXML private TextField raisonSocialeField;
    @FXML private ComboBox<Entreprise.FormeJuridique> formeJuridiqueCombo;
    @FXML private TextField capitalSocialField;
    @FXML private ComboBox<String> paysCombo;
    @FXML private Label deviseLabel;

    @FXML private TextField siretField;
    @FXML private TextField sirenField;
    @FXML private TextField numeroTVAField;

    @FXML private TextField adresseField;
    @FXML private TextField codePostalField;
    @FXML private TextField villeField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextField siteWebField;

    @FXML private CheckBox activeCheckBox;
    @FXML private Label helpLabel;

    private final EntrepriseDAO entrepriseDAO;
    private final CurrencyService currencyService;
    private final EntrepriseInitializationService initService;
    private final AuthenticationService authService;
    private Entreprise entreprise;
    private boolean newEntreprise = true;

    public EntrepriseDialogController() {
        this.entrepriseDAO = new EntrepriseDAO();
        this.currencyService = CurrencyService.getInstance();
        this.initService = new EntrepriseInitializationService();
        this.authService = AuthenticationService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupValidation();
        setupCurrencyDetection();
    }

    private void setupComboBoxes() {
        // Formes juridiques
        formeJuridiqueCombo.setItems(FXCollections.observableList(Arrays.asList(Entreprise.FormeJuridique.values())));
        formeJuridiqueCombo.setValue(Entreprise.FormeJuridique.SARL);

        // Pays (focus sur Afrique + France)
        paysCombo.setItems(FXCollections.observableArrayList(
            "Cameroun", "France", "Sénégal", "Côte d'Ivoire", "Mali", "Burkina Faso",
            "Niger", "Tchad", "République Centrafricaine", "Gabon", "Congo",
            "République Démocratique du Congo", "Belgique", "Canada"
        ));

        // Détection intelligente du pays par défaut selon l'utilisateur connecté
        String defaultCountry = getDefaultCountryForCurrentUser();
        paysCombo.setValue(defaultCountry);

        // Mise à jour immédiate de la devise selon le pays par défaut
        updateCurrencyDisplay(defaultCountry);
    }

    private void setupValidation() {
        // Validation en temps réel
        raisonSocialeField.textProperty().addListener((obs, oldVal, newVal) -> validateField(raisonSocialeField, newVal != null && !newVal.trim().isEmpty()));

        siretField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                siretField.setText(oldVal);
            } else if (newVal != null && newVal.length() > 14) {
                siretField.setText(newVal.substring(0, 14));
            }
        });

        sirenField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                sirenField.setText(oldVal);
            } else if (newVal != null && newVal.length() > 9) {
                sirenField.setText(newVal.substring(0, 9));
            }
        });

        capitalSocialField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*\\.?\\d*")) {
                capitalSocialField.setText(oldVal);
            }
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean valid = newVal == null || newVal.isEmpty() || newVal.matches("^[^@]+@[^@]+\\.[^@]+$");
            validateField(emailField, valid);
        });
    }

    private void setupCurrencyDetection() {
        paysCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateCurrencyDisplay(newVal);
            }
        });
    }

    /**
     * Met à jour l'affichage de la devise et les éléments liés selon le pays
     */
    private void updateCurrencyDisplay(String pays) {
        if (pays != null) {
            String currency = getCurrencyForCountry(pays);
            deviseLabel.setText(currency);

            // Mise à jour du placeholder selon le pays
            if ("FCFA".equals(currency)) {
                capitalSocialField.setPromptText("Ex: 1000000");
                helpLabel.setText("* Champs obligatoires. Devise: FCFA (Franc CFA). Forme juridique adaptée au droit OHADA.");
            } else {
                capitalSocialField.setPromptText("Ex: 10000");
                helpLabel.setText("* Champs obligatoires. Devise: " + currency + ". Règles fiscales selon le pays sélectionné.");
            }
        }
    }

    private void validateField(TextField field, boolean valid) {
        if (valid) {
            field.setStyle("");
        } else {
            field.setStyle("-fx-border-color: red;");
        }
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
        this.newEntreprise = (entreprise == null);

        if (entreprise != null) {
            fillFields(entreprise);
        }
    }

    private void fillFields(Entreprise entreprise) {
        raisonSocialeField.setText(entreprise.getRaisonSociale());
        formeJuridiqueCombo.setValue(entreprise.getFormeJuridique());
        capitalSocialField.setText(entreprise.getCapitalSocial().toString());
        paysCombo.setValue(entreprise.getPays());

        siretField.setText(entreprise.getSiret());
        sirenField.setText(entreprise.getSiren());
        numeroTVAField.setText(entreprise.getNumeroTVA());

        adresseField.setText(entreprise.getAdresse());
        codePostalField.setText(entreprise.getCodePostal());
        villeField.setText(entreprise.getVille());
        telephoneField.setText(entreprise.getTelephone());
        emailField.setText(entreprise.getEmail());
        siteWebField.setText(entreprise.getSiteWeb());

        activeCheckBox.setSelected(entreprise.getActive());
    }

    public boolean validate() {
        StringBuilder errors = new StringBuilder();

        // Validation des champs obligatoires
        if (raisonSocialeField.getText() == null || raisonSocialeField.getText().trim().isEmpty()) {
            errors.append("- La raison sociale est obligatoire\n");
        }

        if (formeJuridiqueCombo.getValue() == null) {
            errors.append("- La forme juridique est obligatoire\n");
        }

        if (paysCombo.getValue() == null || paysCombo.getValue().trim().isEmpty()) {
            errors.append("- Le pays est obligatoire\n");
        }

        // Validation email
        String email = emailField.getText();
        if (email != null && !email.isEmpty() && !email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            errors.append("- L'email n'est pas valide\n");
        }

        // Validation SIRET/SIREN
        String siret = siretField.getText();
        if (siret != null && !siret.isEmpty() && siret.length() != 14) {
            errors.append("- Le SIRET doit contenir exactement 14 chiffres\n");
        }

        String siren = sirenField.getText();
        if (siren != null && !siren.isEmpty() && siren.length() != 9) {
            errors.append("- Le SIREN doit contenir exactement 9 chiffres\n");
        }

        // Validation capital social
        try {
            String capital = capitalSocialField.getText();
            if (capital != null && !capital.isEmpty()) {
                double value = Double.parseDouble(capital);
                if (value < 0) {
                    errors.append("- Le capital social ne peut pas être négatif\n");
                }
            }
        } catch (NumberFormatException e) {
            errors.append("- Le capital social doit être un nombre valide\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreurs de validation");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    public Entreprise getEntreprise() {
        if (entreprise == null) {
            entreprise = new Entreprise();
        }

        // Remplir l'objet avec les données du formulaire
        entreprise.setRaisonSociale(raisonSocialeField.getText().trim());
        entreprise.setFormeJuridique(formeJuridiqueCombo.getValue());
        entreprise.setPays(paysCombo.getValue());

        // Capital social
        try {
            String capital = capitalSocialField.getText();
            if (capital != null && !capital.isEmpty()) {
                entreprise.setCapitalSocial(Double.parseDouble(capital));
            } else {
                entreprise.setCapitalSocial(0.0);
            }
        } catch (NumberFormatException e) {
            entreprise.setCapitalSocial(0.0);
        }

        // Identifiants légaux
        entreprise.setSiret(getTextOrNull(siretField));
        entreprise.setSiren(getTextOrNull(sirenField));
        entreprise.setNumeroTVA(getTextOrNull(numeroTVAField));

        // Coordonnées
        entreprise.setAdresse(getTextOrNull(adresseField));
        entreprise.setCodePostal(getTextOrNull(codePostalField));
        entreprise.setVille(getTextOrNull(villeField));
        entreprise.setTelephone(getTextOrNull(telephoneField));
        entreprise.setEmail(getTextOrNull(emailField));
        entreprise.setSiteWeb(getTextOrNull(siteWebField));

        // Statut
        entreprise.setActive(activeCheckBox.isSelected());

        return entreprise;
    }

    private String getTextOrNull(TextField field) {
        String text = field.getText();
        return (text == null || text.trim().isEmpty()) ? null : text.trim();
    }

    public boolean saveEntreprise() {
        try {
            if (newEntreprise) {
                // 1. Sauvegarder l'entreprise
                entrepriseDAO.save(entreprise);
                logger.info("Nouvelle entreprise créée: {}", entreprise.getRaisonSociale());

                // 2. Initialisation complète (exercice, plan comptable, utilisateur admin)
                logger.info("Initialisation automatique de l'entreprise...");
                boolean initSuccess = initService.initializeEntreprise(entreprise);

                if (initSuccess) {
                    // 3. Afficher le résumé de l'initialisation
                    String summary = initService.getInitializationSummary(entreprise);

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Entreprise créée avec succès");
                    successAlert.setHeaderText("Initialisation complète terminée");
                    successAlert.setContentText(summary);
                    successAlert.setResizable(true);
                    successAlert.showAndWait();
                } else {
                    // Avertissement si l'initialisation a échoué
                    Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                    warningAlert.setTitle("Entreprise créée");
                    warningAlert.setHeaderText("Initialisation partielle");
                    warningAlert.setContentText("L'entreprise a été créée mais l'initialisation automatique " +
                                               "(exercice, plan comptable, utilisateur) a échoué.\n\n" +
                                               "Vous devrez configurer ces éléments manuellement.");
                    warningAlert.showAndWait();
                }
            } else {
                // Mise à jour d'une entreprise existante
                entrepriseDAO.update(entreprise);
                logger.info("Entreprise mise à jour: {}", entreprise.getRaisonSociale());
            }
            return true;

        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde de l'entreprise", e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de sauvegarde");
            alert.setHeaderText("Impossible de sauvegarder l'entreprise");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();

            return false;
        }
    }

    /**
     * Détecte le pays par défaut selon l'utilisateur connecté
     */
    private String getDefaultCountryForCurrentUser() {
        try {
            if (authService.isUserLoggedIn()) {
                Utilisateur currentUser = authService.getUtilisateurConnecte();
                if (currentUser != null) {
                    String username = currentUser.getNomUtilisateur();
                    if (username != null) {
                        String usernameLower = username.toLowerCase();

                        // Détection basée sur le nom d'utilisateur
                        if (usernameLower.contains("france") || usernameLower.contains("francais")) {
                            return "France";
                        } else if (usernameLower.contains("cameroun") || usernameLower.contains("cm")) {
                            return "Cameroun";
                        } else if (usernameLower.contains("senegal") || usernameLower.contains("sn")) {
                            return "Sénégal";
                        } else if (usernameLower.contains("belgique") || usernameLower.contains("be")) {
                            return "Belgique";
                        } else if (usernameLower.contains("canada") || usernameLower.contains("ca")) {
                            return "Canada";
                        }
                    }

                    // Tentative de récupération du contexte de l'entreprise de l'utilisateur
                    Entreprise userEntreprise = currentUser.getEntreprise();
                    if (userEntreprise != null && userEntreprise.getPays() != null) {
                        return userEntreprise.getPays();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Impossible de déterminer le contexte utilisateur: {}", e.getMessage());
        }

        // Par défaut: Cameroun (système principal)
        return "Cameroun";
    }

    /**
     * Détermine la devise selon le pays
     */
    private String getCurrencyForCountry(String pays) {
        if (pays == null) return "EUR";

        // Pays utilisant le Franc CFA
        switch (pays.toLowerCase()) {
            case "cameroun":
            case "sénégal":
            case "côte d'ivoire":
            case "mali":
            case "burkina faso":
            case "niger":
            case "tchad":
            case "république centrafricaine":
            case "gabon":
            case "congo":
                return "FCFA";
            case "république démocratique du congo":
                return "CDF";
            case "belgique":
            case "france":
                return "EUR";
            case "canada":
                return "CAD";
            default:
                return "EUR"; // Par défaut
        }
    }
}