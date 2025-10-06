package com.comptabilite.service;

import com.comptabilite.dao.UtilisateurDAO;
import com.comptabilite.model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static AuthenticationService instance;
    private final UtilisateurDAO utilisateurDAO;
    private Utilisateur utilisateurConnecte;

    private AuthenticationService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    public Optional<Utilisateur> authenticate(String nomUtilisateur, String motDePasse) {
        logger.debug("Tentative de connexion pour l'utilisateur: {}", nomUtilisateur);

        if (nomUtilisateur == null || nomUtilisateur.trim().isEmpty() ||
            motDePasse == null || motDePasse.isEmpty()) {
            logger.warn("Tentative de connexion avec des identifiants vides");
            return Optional.empty();
        }

        try {
            // Pour le moment, on teste avec le mot de passe en clair
            // TODO: Implémenter un vrai système de hash sécurisé
            Optional<Utilisateur> utilisateur = utilisateurDAO.authenticate(nomUtilisateur.trim(), motDePasse);

            if (utilisateur.isPresent()) {
                Utilisateur user = utilisateur.get();
                user.setDerniereConnexion(LocalDateTime.now());
                utilisateurDAO.update(user);
                logger.info("Connexion réussie pour: {}", nomUtilisateur);
                return utilisateur;
            } else {
                logger.warn("Échec de connexion pour: {}", nomUtilisateur);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("Erreur lors de l'authentification", e);
            return Optional.empty();
        }
    }

    public void logout() {
        if (utilisateurConnecte != null) {
            logger.info("Déconnexion de l'utilisateur: {}", utilisateurConnecte.getNomUtilisateur());
            utilisateurConnecte = null;
        }
    }

    public boolean isUserLoggedIn() {
        return utilisateurConnecte != null;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public void setUtilisateurConnecte(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public boolean hasRole(Utilisateur.RoleUtilisateur role) {
        return utilisateurConnecte != null && utilisateurConnecte.getRole() == role;
    }

    public boolean isAdministrateur() {
        return hasRole(Utilisateur.RoleUtilisateur.ADMINISTRATEUR);
    }

    public boolean isComptable() {
        return hasRole(Utilisateur.RoleUtilisateur.COMPTABLE) || isAdministrateur();
    }

    public boolean canModifyData() {
        return utilisateurConnecte != null &&
               (utilisateurConnecte.getRole() == Utilisateur.RoleUtilisateur.ADMINISTRATEUR ||
                utilisateurConnecte.getRole() == Utilisateur.RoleUtilisateur.COMPTABLE ||
                utilisateurConnecte.getRole() == Utilisateur.RoleUtilisateur.ASSISTANT_COMPTABLE);
    }

    private String hashPassword(String password) {
        return Integer.toString(password.hashCode());
    }

    public String createUser(String nomUtilisateur, String motDePasse, String nom, String prenom,
                           String email, Utilisateur.RoleUtilisateur role) {
        try {
            if (utilisateurDAO.existsByNomUtilisateur(nomUtilisateur)) {
                return "Ce nom d'utilisateur existe déjà";
            }

            if (utilisateurDAO.existsByEmail(email)) {
                return "Cette adresse email est déjà utilisée";
            }

            String hashedPassword = hashPassword(motDePasse);
            Utilisateur nouveauUtilisateur = new Utilisateur(nomUtilisateur, hashedPassword, nom, prenom, email, role);

            if (utilisateurConnecte != null && utilisateurConnecte.getEntreprise() != null) {
                nouveauUtilisateur.setEntreprise(utilisateurConnecte.getEntreprise());
            }

            utilisateurDAO.save(nouveauUtilisateur);
            logger.info("Nouvel utilisateur créé: {}", nomUtilisateur);
            return "Utilisateur créé avec succès";

        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'utilisateur", e);
            return "Erreur lors de la création de l'utilisateur";
        }
    }
}