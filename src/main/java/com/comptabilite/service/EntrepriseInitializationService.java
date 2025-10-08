package com.comptabilite.service;

import com.comptabilite.model.*;
import com.comptabilite.dao.*;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service d'initialisation complète d'une nouvelle entreprise
 * Crée automatiquement : exercice, utilisateur admin, comptes de base
 */
public class EntrepriseInitializationService {

    private static final Logger logger = LoggerFactory.getLogger(EntrepriseInitializationService.class);

    private final ExerciceDAO exerciceDAO;
    private final UtilisateurDAO utilisateurDAO;
    private final CompteDAO compteDAO;

    public EntrepriseInitializationService() {
        this.exerciceDAO = new ExerciceDAO();
        this.utilisateurDAO = new UtilisateurDAO();
        this.compteDAO = new CompteDAO();
    }

    /**
     * Initialisation complète d'une entreprise nouvellement créée
     * Utilise une seule transaction pour éviter les problèmes de connexion Hibernate
     */
    public boolean initializeEntreprise(Entreprise entreprise) {
        logger.info("Début de l'initialisation de l'entreprise: {} (ID: {})", entreprise.getRaisonSociale(), entreprise.getId());

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // 1. Créer l'exercice comptable en cours
            logger.info("Étape 1: Création de l'exercice comptable...");
            Exercice exercice = createDefaultExerciceInSession(session, entreprise);
            if (exercice == null) {
                logger.error("Échec création exercice pour entreprise: {} (ID: {})", entreprise.getRaisonSociale(), entreprise.getId());
                transaction.rollback();
                return false;
            }
            logger.info("✅ Exercice créé avec succès: {}", exercice.getLibelle());

            // 2. Créer l'utilisateur administrateur
            logger.info("Étape 2: Création de l'utilisateur administrateur...");
            Utilisateur adminUser = createDefaultAdminUserInSession(session, entreprise);
            if (adminUser == null) {
                logger.error("Échec création utilisateur admin pour entreprise: {} (ID: {})", entreprise.getRaisonSociale(), entreprise.getId());
                transaction.rollback();
                return false;
            }
            logger.info("✅ Utilisateur admin créé avec succès: {}", adminUser.getNomUtilisateur());

            // 3. Créer les comptes de base spécifiques
            logger.info("Étape 3: Création des comptes de base...");
            boolean comptesDeBaseCreated = createComptesDeBaseInSession(session, entreprise);
            if (!comptesDeBaseCreated) {
                logger.error("Échec création comptes de base pour entreprise: {}", entreprise.getId());
                transaction.rollback();
                return false;
            }
            logger.info("✅ Comptes de base créés avec succès");

            // Commit de toute la transaction
            transaction.commit();
            logger.info("Initialisation complète réussie pour entreprise: {}", entreprise.getRaisonSociale());
            return true;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Erreur lors de l'initialisation de l'entreprise: " + entreprise.getRaisonSociale(), e);
            return false;
        }
    }

    /**
     * Crée l'exercice comptable par défaut dans la session fournie
     */
    private Exercice createDefaultExerciceInSession(Session session, Entreprise entreprise) {
        try {
            int currentYear = LocalDate.now().getYear();

            Exercice exercice = new Exercice();
            exercice.setEntreprise(entreprise);
            exercice.setLibelle("Exercice " + currentYear + " - " + entreprise.getRaisonSociale());
            exercice.setDateDebut(LocalDate.of(currentYear, 1, 1));
            exercice.setDateFin(LocalDate.of(currentYear, 12, 31));
            exercice.setStatut(Exercice.StatutExercice.OUVERT);

            session.persist(exercice);
            logger.info("Exercice {} créé pour entreprise: {}", currentYear, entreprise.getRaisonSociale());

            return exercice;
        } catch (Exception e) {
            logger.error("Erreur création exercice pour entreprise: " + entreprise.getRaisonSociale(), e);
            return null;
        }
    }

    /**
     * Crée l'exercice comptable par défaut (année en cours) - version legacy
     */
    private Exercice createDefaultExercice(Entreprise entreprise) {
        try {
            int currentYear = LocalDate.now().getYear();

            Exercice exercice = new Exercice();
            exercice.setEntreprise(entreprise);
            exercice.setLibelle("Exercice " + currentYear + " - " + entreprise.getRaisonSociale());
            exercice.setDateDebut(LocalDate.of(currentYear, 1, 1));
            exercice.setDateFin(LocalDate.of(currentYear, 12, 31));
            exercice.setStatut(Exercice.StatutExercice.OUVERT);

            exerciceDAO.save(exercice);
            logger.info("Exercice {} créé pour entreprise: {}", currentYear, entreprise.getRaisonSociale());

            return exercice;
        } catch (Exception e) {
            logger.error("Erreur création exercice pour entreprise: " + entreprise.getRaisonSociale(), e);
            return null;
        }
    }

    /**
     * Crée l'utilisateur administrateur par défaut dans la session fournie
     */
    private Utilisateur createDefaultAdminUserInSession(Session session, Entreprise entreprise) {
        try {
            // Créer un nom d'utilisateur unique
            String adminUsername = "admin_" + entreprise.getId();

            // Créer un email unique pour éviter les contraintes null
            String adminEmail = entreprise.getEmail() != null && !entreprise.getEmail().trim().isEmpty()
                ? entreprise.getEmail()
                : "admin." + entreprise.getId() + "@" + entreprise.getRaisonSociale().toLowerCase().replaceAll("\\s+", "") + ".local";

            Utilisateur admin = new Utilisateur();
            admin.setEntreprise(entreprise);
            admin.setNomUtilisateur(adminUsername);
            admin.setMotDePasse("admin123"); // Sera hashé par le DAO
            admin.setNom("Administrateur");
            admin.setPrenom("Système");
            admin.setEmail(adminEmail);
            admin.setRole(Utilisateur.RoleUtilisateur.ADMINISTRATEUR);
            admin.setActif(true);

            session.persist(admin);
            logger.info("Utilisateur admin créé pour entreprise: {} avec login: {}", entreprise.getRaisonSociale(), adminUsername);

            return admin;
        } catch (Exception e) {
            logger.error("Erreur création utilisateur admin pour: " + entreprise.getRaisonSociale(), e);
            return null;
        }
    }

    /**
     * Crée l'utilisateur administrateur par défaut - version legacy
     */
    private Utilisateur createDefaultAdminUser(Entreprise entreprise) {
        try {
            Utilisateur admin = new Utilisateur();
            admin.setEntreprise(entreprise);
            admin.setNomUtilisateur("admin");
            admin.setMotDePasse("admin123"); // Sera hashé par le DAO
            admin.setNom("Administrateur");
            admin.setPrenom("Système");
            admin.setEmail(entreprise.getEmail());
            admin.setRole(Utilisateur.RoleUtilisateur.ADMINISTRATEUR);
            admin.setActif(true);

            utilisateurDAO.save(admin);
            logger.info("Utilisateur admin créé pour entreprise: {}", entreprise.getRaisonSociale());

            return admin;
        } catch (Exception e) {
            logger.error("Erreur création utilisateur admin pour: " + entreprise.getRaisonSociale(), e);
            return null;
        }
    }

    /**
     * Crée les comptes de base spécifiques à l'entreprise dans la session fournie
     */
    private boolean createComptesDeBaseInSession(Session session, Entreprise entreprise) {
        try {
            List<Compte> comptesDeBase = new ArrayList<>();

            // Compte banque principal
            Compte banque = new Compte();
            banque.setEntreprise(entreprise);
            banque.setNumeroCompte(isOHADACountry(entreprise.getPays()) ? "CM521000" : "512000");
            banque.setLibelle("Banque " + entreprise.getRaisonSociale());
            banque.setTypeCompte(Compte.TypeCompte.ACTIF);
            banque.setClasseCompte(Compte.ClasseCompte.CLASSE_5);
            banque.setSoldeInitial(BigDecimal.ZERO);
            comptesDeBase.add(banque);

            // Compte capital
            if (entreprise.getCapitalSocial() > 0) {
                Compte capital = new Compte();
                capital.setEntreprise(entreprise);
                capital.setNumeroCompte(isOHADACountry(entreprise.getPays()) ? "CM101000" : "101000");
                capital.setLibelle("Capital social - " + entreprise.getRaisonSociale());
                capital.setTypeCompte(Compte.TypeCompte.PASSIF);
                capital.setClasseCompte(Compte.ClasseCompte.CLASSE_1);
                capital.setSoldeInitial(BigDecimal.valueOf(entreprise.getCapitalSocial()));
                comptesDeBase.add(capital);
            }

            // Compte de caisse
            Compte caisse = new Compte();
            caisse.setEntreprise(entreprise);
            caisse.setNumeroCompte(isOHADACountry(entreprise.getPays()) ? "CM571000" : "530000");
            caisse.setLibelle("Caisse " + entreprise.getRaisonSociale());
            caisse.setTypeCompte(Compte.TypeCompte.ACTIF);
            caisse.setClasseCompte(Compte.ClasseCompte.CLASSE_5);
            caisse.setSoldeInitial(BigDecimal.ZERO);
            comptesDeBase.add(caisse);

            // Sauvegarder tous les comptes dans la session
            for (Compte compte : comptesDeBase) {
                session.persist(compte);
            }

            logger.info("Comptes de base créés ({} comptes) pour entreprise: {}",
                       comptesDeBase.size(), entreprise.getRaisonSociale());
            return true;

        } catch (Exception e) {
            logger.error("Erreur création comptes de base pour: " + entreprise.getRaisonSociale(), e);
            return false;
        }
    }

    /**
     * Crée les comptes de base spécifiques à l'entreprise - version legacy
     */
    private boolean createComptesDeBase(Entreprise entreprise) {
        try {
            List<Compte> comptesDeBase = new ArrayList<>();

            // Compte banque principal
            Compte banque = new Compte();
            banque.setEntreprise(entreprise);
            banque.setNumeroCompte(isOHADACountry(entreprise.getPays()) ? "CM521000" : "512000");
            banque.setLibelle("Banque " + entreprise.getRaisonSociale());
            banque.setTypeCompte(Compte.TypeCompte.ACTIF);
            banque.setClasseCompte(Compte.ClasseCompte.CLASSE_5);
            banque.setSoldeInitial(BigDecimal.ZERO);
            comptesDeBase.add(banque);

            // Compte capital
            if (entreprise.getCapitalSocial() > 0) {
                Compte capital = new Compte();
                capital.setEntreprise(entreprise);
                capital.setNumeroCompte(isOHADACountry(entreprise.getPays()) ? "CM101000" : "101000");
                capital.setLibelle("Capital social - " + entreprise.getRaisonSociale());
                capital.setTypeCompte(Compte.TypeCompte.PASSIF);
                capital.setClasseCompte(Compte.ClasseCompte.CLASSE_1);
                capital.setSoldeInitial(BigDecimal.valueOf(entreprise.getCapitalSocial()));
                comptesDeBase.add(capital);
            }

            // Compte de caisse
            Compte caisse = new Compte();
            caisse.setEntreprise(entreprise);
            caisse.setNumeroCompte(isOHADACountry(entreprise.getPays()) ? "CM571000" : "530000");
            caisse.setLibelle("Caisse " + entreprise.getRaisonSociale());
            caisse.setTypeCompte(Compte.TypeCompte.ACTIF);
            caisse.setClasseCompte(Compte.ClasseCompte.CLASSE_5);
            caisse.setSoldeInitial(BigDecimal.ZERO);
            comptesDeBase.add(caisse);

            // Sauvegarder tous les comptes
            for (Compte compte : comptesDeBase) {
                compteDAO.save(compte);
            }

            logger.info("Comptes de base créés ({} comptes) pour entreprise: {}",
                       comptesDeBase.size(), entreprise.getRaisonSociale());
            return true;

        } catch (Exception e) {
            logger.error("Erreur création comptes de base pour: " + entreprise.getRaisonSociale(), e);
            return false;
        }
    }

    private boolean isOHADACountry(String pays) {
        return pays != null && (
            pays.equals("Cameroun") || pays.equals("Sénégal") || pays.equals("Côte d'Ivoire") ||
            pays.equals("Mali") || pays.equals("Burkina Faso") || pays.equals("Niger") ||
            pays.equals("Tchad") || pays.equals("République Centrafricaine") ||
            pays.equals("Gabon") || pays.equals("Congo")
        );
    }

    /**
     * Résumé des éléments créés pour affichage
     */
    public String getInitializationSummary(Entreprise entreprise) {
        StringBuilder summary = new StringBuilder();
        summary.append("Initialisation complète de l'entreprise '").append(entreprise.getRaisonSociale()).append("' :\n\n");

        summary.append("✅ Exercice comptable ").append(LocalDate.now().getYear()).append(" créé\n");
        summary.append("✅ Utilisateur administrateur créé\n");
        summary.append("   • Login: admin_").append(entreprise.getId()).append("\n");
        summary.append("   • Mot de passe initial: admin123\n\n");

        summary.append("✅ Comptes de base créés:\n");
        summary.append("   • Compte banque\n");
        summary.append("   • Compte caisse\n");

        if (entreprise.getCapitalSocial() > 0) {
            String currency = getCurrencyForCountry(entreprise.getPays());
            summary.append("   • Compte capital social (")
                    .append(String.format("%,.0f", entreprise.getCapitalSocial()))
                    .append(" ").append(currency).append(")\n");
        }

        if (isOHADACountry(entreprise.getPays())) {
            summary.append("\n📋 Plan comptable: Format OHADA (Cameroun)\n");
        } else {
            summary.append("\n📋 Plan comptable: Format français\n");
        }

        summary.append("\n🎯 L'entreprise est maintenant prête à utiliser !");
        summary.append("\n\n⚠️ N'oubliez pas de changer le mot de passe administrateur !");

        return summary.toString();
    }

    private String getCurrencyForCountry(String pays) {
        if (pays == null) return "EUR";
        switch (pays.toLowerCase()) {
            case "cameroun":
            case "sénégal":
            case "côte d'ivoire":
            case "mali":
            case "burkina faso":
            case "niger":
            case "tchad":
            case "république centrafricaire":
            case "gabon":
            case "congo":
                return "FCFA";
            default:
                return "EUR";
        }
    }
}