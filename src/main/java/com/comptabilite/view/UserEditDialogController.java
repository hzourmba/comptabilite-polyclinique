package com.comptabilite.view;

import com.comptabilite.model.Entreprise;
import com.comptabilite.model.Utilisateur;
import com.comptabilite.dao.UtilisateurDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class UserEditDialogController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(UserEditDialogController.class);

    @FXML private Label titleLabel;
    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<Utilisateur.RoleUtilisateur> roleComboBox;
    @FXML private CheckBox activeCheckBox;

    @FXML private VBox passwordSection;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final UtilisateurDAO utilisateurDAO;
    private Utilisateur userToEdit;
    private Entreprise entreprise;
    private boolean isEditMode = false;

    public UserEditDialogController() {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRoleComboBox();
        setupValidation();
    }

    private void setupRoleComboBox() {
        // Charger les rôles disponibles
        roleComboBox.setItems(FXCollections.observableList(Arrays.asList(Utilisateur.RoleUtilisateur.values())));

        // Personnaliser l'affichage des rôles
        roleComboBox.setCellFactory(listView -> new ListCell<Utilisateur.RoleUtilisateur>() {
            @Override
            protected void updateItem(Utilisateur.RoleUtilisateur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(translateRole(item));
                }
            }
        });

        roleComboBox.setButtonCell(new ListCell<Utilisateur.RoleUtilisateur>() {
            @Override
            protected void updateItem(Utilisateur.RoleUtilisateur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(translateRole(item));
                }
            }
        });

        // Sélectionner un rôle par défaut
        roleComboBox.setValue(Utilisateur.RoleUtilisateur.ASSISTANT_COMPTABLE);
    }

    private void setupValidation() {
        // Validation en temps réel
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> clearMessages());
    }

    public void setUserToEdit(Utilisateur user) {
        this.userToEdit = user;
        this.isEditMode = true;

        // Masquer la section mot de passe en mode édition
        passwordSection.setVisible(false);
        passwordSection.setManaged(false);

        // Changer le titre
        titleLabel.setText("Modifier Utilisateur");

        // Pré-remplir les champs
        populateFields();
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    private void populateFields() {
        if (userToEdit != null) {
            usernameField.setText(userToEdit.getNomUtilisateur());
            firstNameField.setText(userToEdit.getPrenom());
            lastNameField.setText(userToEdit.getNom());
            emailField.setText(userToEdit.getEmail());
            roleComboBox.setValue(userToEdit.getRole());
            activeCheckBox.setSelected(userToEdit.getActif());
        }
    }

    public boolean saveUser() {
        if (!validateFields()) {
            return false;
        }

        try {
            if (isEditMode) {
                return updateExistingUser();
            } else {
                return createNewUser();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde de l'utilisateur", e);
            showError("Erreur lors de la sauvegarde: " + e.getMessage());
            return false;
        }
    }

    private boolean createNewUser() {
        // Créer un nouvel utilisateur
        Utilisateur newUser = new Utilisateur();
        newUser.setEntreprise(entreprise);
        newUser.setNomUtilisateur(usernameField.getText().trim());
        newUser.setPrenom(firstNameField.getText().trim());
        newUser.setNom(lastNameField.getText().trim());
        newUser.setEmail(emailField.getText().trim());
        newUser.setRole(roleComboBox.getValue());
        newUser.setActif(activeCheckBox.isSelected());

        // Hashage du mot de passe
        String hashedPassword = Integer.toString(passwordField.getText().hashCode());
        newUser.setMotDePasse(hashedPassword);

        // Sauvegarder
        utilisateurDAO.save(newUser);
        logger.info("Nouvel utilisateur créé: {}", newUser.getNomUtilisateur());

        showSuccess("Utilisateur créé avec succès !");
        return true;
    }

    private boolean updateExistingUser() {
        // Recharger l'utilisateur depuis la base pour éviter les problèmes de session
        Utilisateur userToUpdate = utilisateurDAO.findById(userToEdit.getId()).orElse(null);
        if (userToUpdate == null) {
            showError("Erreur: utilisateur non trouvé en base");
            return false;
        }

        // Mettre à jour les champs
        userToUpdate.setNomUtilisateur(usernameField.getText().trim());
        userToUpdate.setPrenom(firstNameField.getText().trim());
        userToUpdate.setNom(lastNameField.getText().trim());
        userToUpdate.setEmail(emailField.getText().trim());
        userToUpdate.setRole(roleComboBox.getValue());
        userToUpdate.setActif(activeCheckBox.isSelected());

        // Sauvegarder
        utilisateurDAO.update(userToUpdate);
        logger.info("Utilisateur modifié: {}", userToUpdate.getNomUtilisateur());

        showSuccess("Utilisateur modifié avec succès !");
        return true;
    }

    private boolean validateFields() {
        // Vérifier les champs obligatoires
        if (isFieldEmpty(usernameField)) {
            showError("Le nom d'utilisateur est obligatoire");
            return false;
        }

        if (isFieldEmpty(firstNameField)) {
            showError("Le prénom est obligatoire");
            return false;
        }

        if (isFieldEmpty(lastNameField)) {
            showError("Le nom est obligatoire");
            return false;
        }

        if (isFieldEmpty(emailField)) {
            showError("L'email est obligatoire");
            return false;
        }

        if (roleComboBox.getValue() == null) {
            showError("Le rôle est obligatoire");
            return false;
        }

        // Validation email
        String email = emailField.getText().trim();
        if (!email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            showError("Format d'email invalide");
            return false;
        }

        // Vérifier l'unicité du nom d'utilisateur
        String username = usernameField.getText().trim();
        if (!isEditMode || !username.equals(userToEdit.getNomUtilisateur())) {
            if (utilisateurDAO.existsByNomUtilisateur(username)) {
                showError("Ce nom d'utilisateur existe déjà");
                return false;
            }
        }

        // Vérifier l'unicité de l'email
        if (!isEditMode || !email.equals(userToEdit.getEmail())) {
            if (utilisateurDAO.existsByEmail(email)) {
                showError("Cette adresse email est déjà utilisée");
                return false;
            }
        }

        // Validation mot de passe pour nouveau utilisateur
        if (!isEditMode) {
            return validatePassword();
        }

        return true;
    }

    private boolean validatePassword() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password == null || password.isEmpty()) {
            showError("Le mot de passe est obligatoire");
            return false;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("La confirmation du mot de passe ne correspond pas");
            return false;
        }

        return true;
    }

    private boolean isFieldEmpty(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private String translateRole(Utilisateur.RoleUtilisateur role) {
        if (role == null) return "Non défini";

        switch (role) {
            case ADMINISTRATEUR: return "Administrateur";
            case COMPTABLE: return "Comptable";
            case ASSISTANT_COMPTABLE: return "Assistant Comptable";
            case CONSULTANT: return "Consultant";
            default: return role.toString();
        }
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