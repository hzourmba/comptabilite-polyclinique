package com.comptabilite.service;

import com.comptabilite.dao.UtilisateurDAO;
import com.comptabilite.dao.EntrepriseDAO;
import com.comptabilite.dao.ExerciceDAO;
import com.comptabilite.model.Utilisateur;
import com.comptabilite.model.Entreprise;
import com.comptabilite.model.Exercice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class InitializationService {

    private static final Logger logger = LoggerFactory.getLogger(InitializationService.class);
    private final UtilisateurDAO utilisateurDAO;
    private final EntrepriseDAO entrepriseDAO;
    private final ExerciceDAO exerciceDAO;

    public InitializationService() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.entrepriseDAO = new EntrepriseDAO();
        this.exerciceDAO = new ExerciceDAO();
    }

    public void initializeApplication() {
        logger.info("Initialisation de l'application...");

        try {
            // Vérifier s'il existe déjà des utilisateurs
            List<Utilisateur> utilisateurs = utilisateurDAO.findAll();

            if (utilisateurs.isEmpty()) {
                logger.info("Aucun utilisateur trouvé, création de l'utilisateur administrateur par défaut");
                createDefaultAdminUser();
            } else {
                logger.info("Utilisateurs existants trouvés: {}", utilisateurs.size());
            }

            // Vérifier les exercices
            List<Exercice> exercices = exerciceDAO.findAll();
            if (exercices.isEmpty()) {
                logger.info("Création de l'exercice par défaut");
                createDefaultExercice();
            }

        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation", e);
        }
    }

    private void createDefaultAdminUser() {
        try {
            // Récupérer la première entreprise
            List<Entreprise> entreprises = entrepriseDAO.findAll();
            Entreprise entreprise = null;

            if (!entreprises.isEmpty()) {
                entreprise = entreprises.get(0);
            }

            // Créer l'utilisateur administrateur
            Utilisateur admin = new Utilisateur();
            admin.setNomUtilisateur("admin");
            admin.setMotDePasse("admin"); // En clair pour le moment
            admin.setNom("Administrateur");
            admin.setPrenom("Admin");
            admin.setEmail("admin@entreprise.com");
            admin.setRole(Utilisateur.RoleUtilisateur.ADMINISTRATEUR);
            admin.setActif(true);
            admin.setEntreprise(entreprise);

            utilisateurDAO.save(admin);
            logger.info("Utilisateur administrateur créé avec succès");

        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'utilisateur administrateur", e);
        }
    }

    private void createDefaultExercice() {
        try {
            List<Entreprise> entreprises = entrepriseDAO.findAll();
            if (!entreprises.isEmpty()) {
                Entreprise entreprise = entreprises.get(0);

                Exercice exercice = new Exercice();
                exercice.setLibelle("Exercice " + LocalDate.now().getYear());
                exercice.setDateDebut(LocalDate.of(LocalDate.now().getYear(), 1, 1));
                exercice.setDateFin(LocalDate.of(LocalDate.now().getYear(), 12, 31));
                exercice.setEntreprise(entreprise);

                exerciceDAO.save(exercice);
                logger.info("Exercice par défaut créé");
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'exercice", e);
        }
    }
}