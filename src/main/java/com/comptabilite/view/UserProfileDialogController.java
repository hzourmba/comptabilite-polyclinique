package com.comptabilite.view;

import com.comptabilite.model.Utilisateur;
import com.comptabilite.dao.UtilisateurDAO;
import com.comptabilite.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class UserProfileDialogController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileDialogController.class);

    @FXML private TextField usernameField;
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private Label roleLabel;

    @FXML private CheckBox changePasswordCheckBox;
    @FXML private GridPane passwordGrid;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private Utilisateur currentUser;
    private final UtilisateurDAO utilisateurDAO;
    private final AuthenticationService authService;

    public UserProfileDialogController() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.authService = AuthenticationService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupPasswordChangeToggle();
        setupValidation();
    }

    private void setupPasswordChangeToggle() {
        changePasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            passwordGrid.setDisable(!newVal);
            if (!newVal) {
                // Nettoyer les champs de mot de passe si décoché
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            }
        });
    }

    private void setupValidation() {
        // Validation en temps réel
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        populateFields();
    }

    private void populateFields() {
        if (currentUser != null) {
            usernameField.setText(currentUser.getNomUtilisateur());
            prenomField.setText(currentUser.getPrenom());
            nomField.setText(currentUser.getNom());
            emailField.setText(currentUser.getEmail());
            roleLabel.setText(currentUser.getRole().toString());
        }
    }

    public boolean saveChanges() {
        if (!validateFields()) {
            return false;
        }

        try {
            // Recharger l'utilisateur depuis la base pour éviter les problèmes de session Hibernate
            Utilisateur userToUpdate = utilisateurDAO.findById(currentUser.getId()).orElse(null);
            if (userToUpdate == null) {
                showError("Erreur: utilisateur non trouvé en base");
                return false;
            }

            // Gérer le changement de mot de passe si demandé (AVANT de modifier les autres champs)
            if (changePasswordCheckBox.isSelected()) {
                logger.info("Changement de mot de passe demandé pour: {}", userToUpdate.getNomUtilisateur());

                if (!validatePasswordChangeWithUser(userToUpdate)) {
                    logger.warn("Validation du changement de mot de passe échouée");
                    return false;
                }

                // Hashage simple du mot de passe (même méthode que lors de la création)
                String newPassword = newPasswordField.getText();
                String hashedPassword = Integer.toString(newPassword.hashCode());

                logger.info("Ancien mot de passe hashé: {}", userToUpdate.getMotDePasse());
                logger.info("Nouveau mot de passe hashé: {}", hashedPassword);

                userToUpdate.setMotDePasse(hashedPassword);
                logger.info("Mot de passe défini sur l'objet utilisateur rechargé");
            }

            // Mettre à jour les informations de base
            userToUpdate.setNomUtilisateur(usernameField.getText().trim());
            userToUpdate.setPrenom(prenomField.getText().trim());
            userToUpdate.setNom(nomField.getText().trim());
            userToUpdate.setEmail(emailField.getText().trim());

            // Sauvegarder en base
            logger.info("Tentative de sauvegarde de l'utilisateur: {} avec mot de passe: {}",
                       userToUpdate.getNomUtilisateur(), userToUpdate.getMotDePasse());

            Utilisateur updatedUser = utilisateurDAO.update(userToUpdate);
            logger.info("Utilisateur sauvegardé avec ID: {} et mot de passe: {}",
                       updatedUser.getId(), updatedUser.getMotDePasse());

            // Mettre à jour l'utilisateur dans le service d'authentification si c'est l'utilisateur connecté
            if (authService.isUserLoggedIn() &&
                authService.getUtilisateurConnecte().getId().equals(updatedUser.getId())) {

                // Recharger complètement l'utilisateur avec ses dépendances pour éviter les LazyInitializationException
                Utilisateur freshUser = utilisateurDAO.findById(updatedUser.getId()).orElse(null);
                if (freshUser != null) {
                    authService.setUtilisateurConnecte(freshUser);
                    logger.info("Utilisateur connecté rechargé et mis à jour dans AuthenticationService");
                } else {
                    logger.warn("Impossible de recharger l'utilisateur mis à jour");
                }
            }

            showSuccess("Profil mis à jour avec succès !");
            logger.info("Profil utilisateur mis à jour: {}", updatedUser.getNomUtilisateur());

            return true;

        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du profil", e);
            showError("Erreur lors de la sauvegarde: " + e.getMessage());
            return false;
        }
    }

    private boolean validateFields() {
        // Vérifier les champs obligatoires
        if (isFieldEmpty(usernameField)) {
            showError("Le nom d'utilisateur est obligatoire");
            return false;
        }

        if (isFieldEmpty(prenomField)) {
            showError("Le prénom est obligatoire");
            return false;
        }

        if (isFieldEmpty(nomField)) {
            showError("Le nom est obligatoire");
            return false;
        }

        if (isFieldEmpty(emailField)) {
            showError("L'email est obligatoire");
            return false;
        }

        // Validation email basique
        String email = emailField.getText().trim();
        if (!email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            showError("Format d'email invalide");
            return false;
        }

        // Vérifier unicité du nom d'utilisateur (sauf pour l'utilisateur actuel)
        String newUsername = usernameField.getText().trim();
        if (!newUsername.equals(currentUser.getNomUtilisateur()) &&
            utilisateurDAO.existsByNomUtilisateur(newUsername)) {
            showError("Ce nom d'utilisateur existe déjà");
            return false;
        }

        // Vérifier unicité de l'email (sauf pour l'utilisateur actuel)
        if (!email.equals(currentUser.getEmail()) &&
            utilisateurDAO.existsByEmail(email)) {
            showError("Cette adresse email est déjà utilisée");
            return false;
        }

        return true;
    }

    private boolean validatePasswordChangeWithUser(Utilisateur userFromDb) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Vérifier que le mot de passe actuel est correct
        String hashedCurrentPassword = Integer.toString(currentPassword.hashCode());
        String passwordInDb = userFromDb.getMotDePasse();

        logger.info("Mot de passe saisi: {}", currentPassword);
        logger.info("Mot de passe saisi hashé: {}", hashedCurrentPassword);
        logger.info("Mot de passe en base: {}", passwordInDb);

        // Gérer les cas où le mot de passe est stocké en clair ou hashé
        boolean passwordMatches = false;
        if (passwordInDb.equals(currentPassword)) {
            // Mot de passe stocké en clair (cas legacy)
            logger.info("Mot de passe trouvé en clair dans la base");
            passwordMatches = true;
        } else if (passwordInDb.equals(hashedCurrentPassword)) {
            // Mot de passe hashé
            logger.info("Mot de passe trouvé hashé dans la base");
            passwordMatches = true;
        }

        if (!passwordMatches) {
            showError("Mot de passe actuel incorrect");
            return false;
        }

        // Vérifier la longueur du nouveau mot de passe
        if (newPassword.length() < 6) {
            showError("Le nouveau mot de passe doit contenir au moins 6 caractères");
            return false;
        }

        // Vérifier la confirmation
        if (!newPassword.equals(confirmPassword)) {
            showError("La confirmation du mot de passe ne correspond pas");
            return false;
        }

        return true;
    }

    private boolean validatePasswordChange() {
        // Méthode legacy - utilise currentUser
        return validatePasswordChangeWithUser(currentUser);
    }

    private boolean isFieldEmpty(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }

    private void clearMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }
}