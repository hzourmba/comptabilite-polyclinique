package com.comptabilite.view;

import com.comptabilite.model.Utilisateur;
import com.comptabilite.dao.UtilisateurDAO;
import com.comptabilite.service.AuthenticationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    @FXML private Label enterpriseLabel;
    @FXML private Label statusLabel;
    @FXML private Label countLabel;

    @FXML private Button addUserBtn;
    @FXML private Button editUserBtn;
    @FXML private Button toggleStatusBtn;
    @FXML private Button resetPasswordBtn;

    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, String> activeColumn;
    @FXML private TableColumn<Utilisateur, String> usernameColumn;
    @FXML private TableColumn<Utilisateur, String> firstNameColumn;
    @FXML private TableColumn<Utilisateur, String> lastNameColumn;
    @FXML private TableColumn<Utilisateur, String> emailColumn;
    @FXML private TableColumn<Utilisateur, String> roleColumn;
    @FXML private TableColumn<Utilisateur, String> lastLoginColumn;

    private final UtilisateurDAO utilisateurDAO;
    private final AuthenticationService authService;
    private final ObservableList<Utilisateur> usersList;

    public UserManagementController() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.authService = AuthenticationService.getInstance();
        this.usersList = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupTableSelection();
        loadEnterpriseInfo();
        loadUsers();

        // Vérifier les permissions
        checkPermissions();
    }

    private void setupTableColumns() {
        // Colonne statut avec indicateurs visuels
        activeColumn.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().getActif();
            return new SimpleStringProperty(isActive ? "✅" : "❌");
        });

        // Colonnes standard
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("nomUtilisateur"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Colonne rôle avec traduction
        roleColumn.setCellValueFactory(cellData -> {
            Utilisateur.RoleUtilisateur role = cellData.getValue().getRole();
            String roleText = translateRole(role);
            return new SimpleStringProperty(roleText);
        });

        // Colonne dernière connexion avec formatage
        lastLoginColumn.setCellValueFactory(cellData -> {
            var lastLogin = cellData.getValue().getDerniereConnexion();
            if (lastLogin != null) {
                String formattedDate = lastLogin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                return new SimpleStringProperty(formattedDate);
            } else {
                return new SimpleStringProperty("Jamais");
            }
        });

        // Lier la liste observable à la table
        usersTable.setItems(usersList);
    }

    private void setupTableSelection() {
        // Activer/désactiver les boutons selon la sélection
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            boolean canModifySelected = hasSelection && canModifyUser(newSelection);
            boolean isCurrentUser = hasSelection && isCurrentUser(newSelection);

            editUserBtn.setDisable(!hasSelection);
            resetPasswordBtn.setDisable(!canModifySelected);

            // Impossible de se désactiver soi-même
            toggleStatusBtn.setDisable(!canModifySelected || isCurrentUser);

            // Mettre à jour le texte du bouton selon le statut
            if (hasSelection) {
                boolean isActive = newSelection.getActif();
                toggleStatusBtn.setText(isActive ? "❌ Désactiver" : "✅ Activer");
            } else {
                toggleStatusBtn.setText("🔄 Activer/Désactiver");
            }
        });

        // Double-clic pour modifier
        usersTable.setRowFactory(tv -> {
            TableRow<Utilisateur> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditUser();
                }
            });
            return row;
        });
    }

    private void loadEnterpriseInfo() {
        if (authService.isUserLoggedIn()) {
            var currentUser = authService.getUtilisateurConnecte();
            if (currentUser.getEntreprise() != null) {
                enterpriseLabel.setText("Entreprise: " + currentUser.getEntreprise().getRaisonSociale());
            }
        }
    }

    private void loadUsers() {
        try {
            statusLabel.setText("Chargement des utilisateurs...");

            if (!authService.isUserLoggedIn()) {
                statusLabel.setText("Erreur: utilisateur non connecté");
                return;
            }

            var currentUser = authService.getUtilisateurConnecte();
            if (currentUser.getEntreprise() == null) {
                statusLabel.setText("Erreur: aucune entreprise associée");
                return;
            }

            // Charger les utilisateurs de l'entreprise courante
            List<Utilisateur> users = utilisateurDAO.findByEntreprise(currentUser.getEntreprise().getId());
            usersList.clear();
            usersList.addAll(users);

            // Mettre à jour les labels de statut
            statusLabel.setText("Utilisateurs chargés");
            countLabel.setText(users.size() + " utilisateur(s)");

            logger.info("Chargement de {} utilisateurs pour l'entreprise {}",
                       users.size(), currentUser.getEntreprise().getRaisonSociale());

        } catch (Exception e) {
            logger.error("Erreur lors du chargement des utilisateurs", e);
            statusLabel.setText("Erreur lors du chargement");
            showError("Erreur", "Impossible de charger les utilisateurs: " + e.getMessage());
        }
    }

    private void checkPermissions() {
        // Seuls les administrateurs peuvent gérer les utilisateurs
        if (!authService.isAdministrateur()) {
            addUserBtn.setDisable(true);
            editUserBtn.setDisable(true);
            toggleStatusBtn.setDisable(true);
            resetPasswordBtn.setDisable(true);

            statusLabel.setText("Accès limité - Seuls les administrateurs peuvent gérer les utilisateurs");
        }
    }

    @FXML
    private void handleAddUser() {
        logger.info("Ajout d'un nouvel utilisateur");
        showUserEditDialog(null);
    }

    @FXML
    private void handleEditUser() {
        Utilisateur selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            logger.info("Modification de l'utilisateur: {}", selectedUser.getNomUtilisateur());
            showUserEditDialog(selectedUser);
        }
    }

    @FXML
    private void handleToggleStatus() {
        Utilisateur selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;

        // Vérifications de sécurité
        if (isCurrentUser(selectedUser)) {
            showWarning("Action interdite", "Vous ne pouvez pas désactiver votre propre compte.");
            return;
        }

        if (!canModifyUser(selectedUser)) {
            showWarning("Permission refusée", "Vous n'avez pas l'autorisation de modifier cet utilisateur.");
            return;
        }

        try {
            boolean newStatus = !selectedUser.getActif();
            String action = newStatus ? "activer" : "désactiver";

            // Confirmation
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Changer le statut de l'utilisateur");
            confirm.setContentText("Voulez-vous vraiment " + action + " l'utilisateur " +
                                  selectedUser.getNomComplet() + " ?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                selectedUser.setActif(newStatus);
                utilisateurDAO.update(selectedUser);

                logger.info("Utilisateur {} {}", selectedUser.getNomUtilisateur(),
                           newStatus ? "activé" : "désactivé");

                loadUsers(); // Recharger la liste
                showInfo("Statut modifié", "L'utilisateur a été " + action + " avec succès.");
            }

        } catch (Exception e) {
            logger.error("Erreur lors du changement de statut", e);
            showError("Erreur", "Impossible de modifier le statut: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetPassword() {
        Utilisateur selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) return;

        if (!canModifyUser(selectedUser)) {
            showWarning("Permission refusée", "Vous n'avez pas l'autorisation de modifier cet utilisateur.");
            return;
        }

        // TODO: Implémenter la réinitialisation de mot de passe
        showInfo("Fonctionnalité à venir", "La réinitialisation de mot de passe sera implémentée prochainement.");
    }

    @FXML
    private void handleRefresh() {
        logger.info("Actualisation de la liste des utilisateurs");
        loadUsers();
    }

    private void showUserEditDialog(Utilisateur userToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-edit-dialog.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(userToEdit == null ? "Nouvel Utilisateur" : "Modifier Utilisateur");

            // Récupérer le contrôleur et configurer
            UserEditDialogController controller = loader.getController();
            if (userToEdit != null) {
                controller.setUserToEdit(userToEdit);
            }
            controller.setEntreprise(authService.getUtilisateurConnecte().getEntreprise());

            // Afficher et traiter le résultat
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (controller.saveUser()) {
                    loadUsers(); // Recharger la liste
                    String message = userToEdit == null ? "Utilisateur créé" : "Utilisateur modifié";
                    showInfo(message, message + " avec succès.");
                }
            }

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture du dialogue utilisateur", e);
            showError("Erreur", "Impossible d'ouvrir le dialogue: " + e.getMessage());
        }
    }

    // Méthodes utilitaires
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

    private boolean canModifyUser(Utilisateur user) {
        // Seuls les administrateurs peuvent modifier
        return authService.isAdministrateur();
    }

    private boolean isCurrentUser(Utilisateur user) {
        if (!authService.isUserLoggedIn()) return false;
        var currentUser = authService.getUtilisateurConnecte();
        return currentUser.getId().equals(user.getId());
    }

    // Méthodes d'affichage de messages
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}