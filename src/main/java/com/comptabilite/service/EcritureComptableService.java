package com.comptabilite.service;

import com.comptabilite.dao.EcritureComptableDAO;
import com.comptabilite.dao.LigneEcritureDAO;
import com.comptabilite.model.Compte;
import com.comptabilite.model.EcritureComptable;
import com.comptabilite.model.LigneEcriture;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EcritureComptableService {

    private static final Logger logger = LoggerFactory.getLogger(EcritureComptableService.class);
    private final EcritureComptableDAO ecritureDAO;
    private final LigneEcritureDAO ligneEcritureDAO;

    public EcritureComptableService() {
        this.ecritureDAO = new EcritureComptableDAO();
        this.ligneEcritureDAO = new LigneEcritureDAO();
    }

    public List<EcritureComptable> getEcrituresByEntreprise(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Utiliser JOIN FETCH pour charger les lignes en une seule requête
            String hql = "SELECT DISTINCT e FROM EcritureComptable e " +
                        "LEFT JOIN FETCH e.lignes l " +
                        "LEFT JOIN FETCH l.compte " +
                        "LEFT JOIN FETCH e.utilisateur " +
                        "WHERE e.entreprise.id = :entrepriseId " +
                        "ORDER BY e.dateEcriture DESC, e.numeroEcriture";

            return session.createQuery(hql, EcritureComptable.class)
                         .setParameter("entrepriseId", entrepriseId)
                         .getResultList();
        }
    }

    public EcritureComptable getEcritureWithLignes(Long ecritureId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT e FROM EcritureComptable e " +
                        "LEFT JOIN FETCH e.lignes l " +
                        "LEFT JOIN FETCH l.compte " +
                        "LEFT JOIN FETCH e.utilisateur " +
                        "WHERE e.id = :ecritureId";

            return session.createQuery(hql, EcritureComptable.class)
                         .setParameter("ecritureId", ecritureId)
                         .uniqueResult();
        }
    }

    public EcritureComptable sauvegarderEcriture(EcritureComptable ecriture) {
        return ecriture.getId() == null ? creerEcriture(ecriture) : modifierEcriture(ecriture);
    }

    public EcritureComptable creerEcriture(EcritureComptable ecriture) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // =========================
            // DEBUG: État initial de l'écriture
            // =========================
            logger.info("=== DÉBUT DEBUG CRÉATION ÉCRITURE ===");
            logger.debug("Nombre de lignes reçues: {}", ecriture.getLignes() != null ? ecriture.getLignes().size() : "NULL");

            // Debug console direct pour visibilité immédiate
            System.out.println("=== DEBUG CONSOLE CRÉATION ÉCRITURE ===");
            System.out.println("Nombre de lignes reçues: " + (ecriture.getLignes() != null ? ecriture.getLignes().size() : "NULL"));

            if (ecriture.getLignes() != null) {
                for (int i = 0; i < ecriture.getLignes().size(); i++) {
                    LigneEcriture ligne = ecriture.getLignes().get(i);
                    logger.debug("LIGNE {}: Compte={}, CompteID={}, Débit={}, Crédit={}",
                        i,
                        ligne.getCompte() != null ? ligne.getCompte().getNumeroCompte() : "NULL",
                        ligne.getCompte() != null ? ligne.getCompte().getId() : "NULL",
                        ligne.getMontantDebit(),
                        ligne.getMontantCredit());

                    // Debug console direct
                    if (ligne.getCompte() == null) {
                        System.out.println("ERREUR CRITIQUE: Ligne " + i + " a un compte NULL!");
                    } else if (ligne.getCompte().getId() == null) {
                        System.out.println("ERREUR CRITIQUE: Ligne " + i + " - Compte '" + ligne.getCompte().getNumeroCompte() + "' a un ID NULL!");
                    } else {
                        System.out.println("Ligne " + i + ": Compte ID=" + ligne.getCompte().getId() +
                                         ", Numéro=" + ligne.getCompte().getNumeroCompte() +
                                         ", Débit=" + ligne.getMontantDebit() +
                                         ", Crédit=" + ligne.getMontantCredit());
                    }

                    if (ligne.getCompte() != null) {
                        logger.debug("  -> Détails Compte: Libellé='{}', Classe={}",
                            ligne.getCompte().getLibelle(),
                            ligne.getCompte().getClass().getSimpleName());
                    }
                }
            }

            // Générer le numéro d'écriture automatiquement
            if (ecriture.getNumeroEcriture() == null || ecriture.getNumeroEcriture().isEmpty()) {
                String numeroGenere = genererNumeroEcriture(ecriture.getEntreprise().getId());
                ecriture.setNumeroEcriture(numeroGenere);
                logger.debug("Numéro d'écriture généré: {}", numeroGenere);
            }

            // Vérifier l'équilibre débit/crédit
            if (!verifierEquilibre(ecriture)) {
                throw new RuntimeException("L'écriture n'est pas équilibrée (Débit ≠ Crédit)");
            }

            // =========================
            // DEBUG: État avant sauvegarde de l'écriture principale
            // =========================
            logger.debug("=== AVANT session.save(ecriture) ===");
            logger.debug("Tentative de sauvegarde de l'écriture: {}", ecriture.getNumeroEcriture());

            // Vérification finale des comptes avant la sauvegarde
            for (int i = 0; i < ecriture.getLignes().size(); i++) {
                LigneEcriture ligne = ecriture.getLignes().get(i);
                if (ligne.getCompte() == null) {
                    logger.error("ERREUR CRITIQUE: Ligne {} a un compte NULL!", i);
                } else if (ligne.getCompte().getId() == null) {
                    logger.error("ERREUR CRITIQUE: Ligne {} - Compte '{}' a un ID NULL!",
                        i, ligne.getCompte().getNumeroCompte());
                } else {
                    logger.debug("OK: Ligne {} - Compte '{}' avec ID: {}",
                        i, ligne.getCompte().getNumeroCompte(), ligne.getCompte().getId());
                }
            }
            // CORRECTION CRITIQUE: Sauvegarder l'écriture SANS cascade
            logger.debug("Sauvegarde de l'écriture principale sans cascade...");
            System.out.println("=== SAUVEGARDE ÉCRITURE SANS CASCADE ===");
            // Temporairement détacher les lignes pour éviter le cascade save
            List<LigneEcriture> lignesTemp = new ArrayList<>(ecriture.getLignes());
            ecriture.getLignes().clear();
            System.out.println("Lignes temporairement détachées: " + lignesTemp.size());
            // Sauvegarder l'écriture principale (sans lignes)
            session.save(ecriture);
            session.flush(); // Force l'exécution pour générer l'ID
            logger.debug("Écriture principale sauvegardée, ID généré: {}", ecriture.getId());
            System.out.println("ID écriture généré: " + ecriture.getId());
            // Rétablir les lignes dans l'écriture pour le traitement manuel
            ecriture.getLignes().addAll(lignesTemp);
            System.out.println("Lignes ré-attachées à l'écriture");
            /* 
             // CORRECTION CRITIQUE: Attacher tous les comptes à la session AVANT la sauvegarde
            logger.debug("Attachement des comptes à la session courante...");
            System.out.println("=== ATTACHEMENT DES COMPTES À LA SESSION ===");
            for (LigneEcriture ligne : ecriture.getLignes()) {
                if (ligne.getCompte() != null && ligne.getCompte().getId() != null) {
                    Compte compteAttache = session.get(Compte.class, ligne.getCompte().getId());
                    if (compteAttache != null) {
                        ligne.setCompte(compteAttache);
                        System.out.println("Compte ID " + compteAttache.getId() + " (" + compteAttache.getNumeroCompte() + ") attaché avec succès");
                    } else {
                        throw new RuntimeException("Compte introuvable avec ID: " + ligne.getCompte().getId());
                    }
                }
            }

            // Sauvegarder d'abord l'écriture pour obtenir l'ID
            logger.debug("Exécution de session.save(ecriture)...");
            System.out.println("=== EXÉCUTION session.save(ecriture) ===");
            session.save(ecriture);
            logger.debug("session.save(ecriture) réussi, ID généré: {}", ecriture.getId());
            session.flush(); // Force l'exécution immédiate pour générer l'ID
            logger.debug("session.flush() terminé");
            */
            // =========================
            // DEBUG: Sauvegarde des lignes
            // =========================
            logger.debug("=== SAUVEGARDE DES LIGNES ===");

            // Puis sauvegarder les lignes avec la référence correcte
            for (int i = 0; i < ecriture.getLignes().size(); i++) {
                LigneEcriture ligne = ecriture.getLignes().get(i);
                logger.debug("Traitement ligne {}: Compte={}, CompteID={}",
                    i,
                    ligne.getCompte() != null ? ligne.getCompte().getNumeroCompte() : "NULL",
                    ligne.getCompte() != null ? ligne.getCompte().getId() : "NULL");

                ligne.setEcritureComptable(ecriture);

                // CORRECTION: Recharger le compte pour éviter les entités détachées
                if (ligne.getCompte() != null && ligne.getCompte().getId() != null) {
                    logger.debug("Rechargement du compte ID: {} depuis la base...", ligne.getCompte().getId());
                    Compte compteAttache = session.get(Compte.class, ligne.getCompte().getId());
                    if (compteAttache != null) {
                        logger.debug("Compte rechargé avec succès: {} (ID: {})",
                            compteAttache.getNumeroCompte(), compteAttache.getId());
                        ligne.setCompte(compteAttache);
                    } else {
                        logger.error("ERREUR: Compte introuvable avec ID: {}", ligne.getCompte().getId());
                        throw new RuntimeException("Compte introuvable avec ID: " + ligne.getCompte().getId());
                    }
                } else {
                    logger.error("ERREUR: Ligne {} - Compte ou ID du compte est NULL!", i);
                    logger.error("  -> Compte: {}", ligne.getCompte());
                    logger.error("  -> Compte ID: {}", ligne.getCompte() != null ? ligne.getCompte().getId() : "COMPTE_NULL");
                    throw new RuntimeException("Ligne " + i + ": Compte ou ID du compte est NULL");
                }

                logger.debug("Sauvegarde de la ligne {} avec compte ID: {}", i, ligne.getCompte().getId());
                session.save(ligne);
                logger.debug("Ligne {} sauvegardée avec succès", i);
            }

            logger.debug("Validation finale de la transaction...");
            transaction.commit();
            logger.info("Écriture créée avec succès: {}", ecriture.getNumeroEcriture());

            // Si l'écriture est VALIDEE, mettre à jour les soldes des comptes
            if (ecriture.getStatut() == EcritureComptable.StatutEcriture.VALIDEE) {
                logger.debug("Mise à jour des soldes pour écriture VALIDEE...");
                mettreAJourSoldesComptes(ecriture);
            }

            logger.debug("=== FIN DEBUG CRÉATION ÉCRITURE ===");
            return ecriture;

        } catch (Exception e) {
            logger.error("=== ERREUR PENDANT LA CRÉATION ===");
            logger.error("Type d'exception: {}", e.getClass().getSimpleName());
            logger.error("Message: {}", e.getMessage());

            // Log supplémentaire de l'état des lignes en cas d'erreur
            if (ecriture != null && ecriture.getLignes() != null) {
                logger.error("État des lignes au moment de l'erreur:");
                for (int i = 0; i < ecriture.getLignes().size(); i++) {
                    LigneEcriture ligne = ecriture.getLignes().get(i);
                    logger.error("  Ligne {}: Compte={}, CompteID={}, Débit={}, Crédit={}",
                        i,
                        ligne.getCompte() != null ? ligne.getCompte().getNumeroCompte() : "NULL",
                        ligne.getCompte() != null ? ligne.getCompte().getId() : "NULL",
                        ligne.getMontantDebit(),
                        ligne.getMontantCredit());
                }
            }

            if (transaction != null) {
                transaction.rollback();
                logger.debug("Transaction rollback effectué");
            }
            logger.error("Erreur lors de la création de l'écriture", e);
            throw new RuntimeException("Erreur lors de la création de l'écriture: " + e.getMessage(), e);
        }
    }

    public EcritureComptable modifierEcriture(EcritureComptable ecriture) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Vérifier l'équilibre débit/crédit
            if (!verifierEquilibre(ecriture)) {
                throw new RuntimeException("L'écriture n'est pas équilibrée (Débit ≠ Crédit)");
            }

            // Charger l'écriture existante et la mettre à jour
            EcritureComptable existingEcriture = session.get(EcritureComptable.class, ecriture.getId());

            // Mettre à jour les propriétés de base
            existingEcriture.setDateEcriture(ecriture.getDateEcriture());
            existingEcriture.setLibelle(ecriture.getLibelle());
            existingEcriture.setNumeroJournal(ecriture.getNumeroJournal());
            existingEcriture.setReferencePiece(ecriture.getReferencePiece());
            existingEcriture.setStatut(ecriture.getStatut());

            // Vider les anciennes lignes
            existingEcriture.getLignes().clear();

            // Ajouter les nouvelles lignes
            for (LigneEcriture ligne : ecriture.getLignes()) {
                ligne.setEcritureComptable(existingEcriture);
                existingEcriture.getLignes().add(ligne);
            }

            session.update(existingEcriture);

            transaction.commit();
            logger.info("Écriture modifiée avec succès: {}", ecriture.getNumeroEcriture());
            return ecriture;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Erreur lors de la modification de l'écriture", e);
            throw new RuntimeException("Erreur lors de la modification de l'écriture: " + e.getMessage(), e);
        }
    }

    public void supprimerEcriture(EcritureComptable ecriture) {
        if (ecriture.getStatut() != EcritureComptable.StatutEcriture.BROUILLON) {
            throw new RuntimeException("Seules les écritures en brouillon peuvent être supprimées");
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Supprimer d'abord les lignes d'écriture
            for (LigneEcriture ligne : ecriture.getLignes()) {
                session.delete(ligne);
            }

            // Puis supprimer l'écriture
            session.delete(ecriture);

            transaction.commit();
            logger.info("Écriture supprimée: {}", ecriture.getNumeroEcriture());

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Erreur lors de la suppression de l'écriture", e);
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage(), e);
        }
    }

    public void validerEcriture(EcritureComptable ecriture) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Recharger l'écriture dans la session courante avec toutes ses lignes
            EcritureComptable ecritureReloadee = session.createQuery(
                "SELECT e FROM EcritureComptable e " +
                "LEFT JOIN FETCH e.lignes l " +
                "LEFT JOIN FETCH l.compte " +
                "WHERE e.id = :id", EcritureComptable.class)
                .setParameter("id", ecriture.getId())
                .uniqueResult();

            if (ecritureReloadee == null) {
                throw new RuntimeException("Écriture introuvable avec ID: " + ecriture.getId());
            }

            // Vérifier à nouveau le statut avec l'entité rechargée
            if (ecritureReloadee.getStatut() != EcritureComptable.StatutEcriture.BROUILLON) {
                throw new RuntimeException("Seules les écritures en brouillon peuvent être validées");
            }

            if (!verifierEquilibre(ecritureReloadee)) {
                throw new RuntimeException("L'écriture n'est pas équilibrée (Débit ≠ Crédit)");
            }

            ecritureReloadee.setStatut(EcritureComptable.StatutEcriture.VALIDEE);
            ecritureReloadee.setDateValidation(java.time.LocalDateTime.now());
            session.saveOrUpdate(ecritureReloadee);

            transaction.commit();
            logger.info("Écriture validée: {}", ecriture.getNumeroEcriture());

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Erreur lors de la validation de l'écriture", e);
            throw new RuntimeException("Erreur lors de la validation: " + e.getMessage(), e);
        }

        // Mettre à jour les soldes des comptes dans une transaction séparée
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction2 = session.beginTransaction();

            // Recharger l'écriture validée avec ses lignes
            EcritureComptable ecritureValidee = session.createQuery(
                "SELECT e FROM EcritureComptable e " +
                "LEFT JOIN FETCH e.lignes l " +
                "LEFT JOIN FETCH l.compte " +
                "WHERE e.id = :id", EcritureComptable.class)
                .setParameter("id", ecriture.getId())
                .uniqueResult();

            if (ecritureValidee != null && ecritureValidee.getStatut() == EcritureComptable.StatutEcriture.VALIDEE) {
                for (LigneEcriture ligne : ecritureValidee.getLignes()) {
                    Compte compte = ligne.getCompte();

                    if (ligne.getMontantDebit().compareTo(BigDecimal.ZERO) > 0) {
                        compte.debiter(ligne.getMontantDebit());
                    } else if (ligne.getMontantCredit().compareTo(BigDecimal.ZERO) > 0) {
                        compte.crediter(ligne.getMontantCredit());
                    }
                    session.saveOrUpdate(compte);

                    // Mettre à jour les comptes parents dans la hiérarchie
                    Compte parent = compte.getCompteParent();
                    while (parent != null) {
                        // Recharger le parent avec ses sous-comptes
                        parent = session.createQuery(
                            "SELECT c FROM Compte c LEFT JOIN FETCH c.sousComptes WHERE c.id = :id",
                            Compte.class)
                            .setParameter("id", parent.getId())
                            .uniqueResult();

                        logger.info("Mise à jour du compte parent: {} après modification de {}",
                            parent.getNumeroCompte(), compte.getNumeroCompte());

                        // Les soldes consolidés sont calculés dynamiquement
                        // Pas besoin de modifier les soldes stockés
                        parent = parent.getCompteParent(); // Remonter vers le grand-parent
                    }
                }
            }

            transaction2.commit();

        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des soldes", e);
            // Ne pas lancer d'exception car l'écriture a déjà été validée
        }
    }

    private boolean verifierEquilibre(EcritureComptable ecriture) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (LigneEcriture ligne : ecriture.getLignes()) {
            totalDebit = totalDebit.add(ligne.getMontantDebit());
            totalCredit = totalCredit.add(ligne.getMontantCredit());
        }

        return totalDebit.compareTo(totalCredit) == 0;
    }

    private String genererNumeroEcriture(Long entrepriseId) {
        // Format: EC-YYYY-NNNN (ex: EC-2024-0001)
        java.time.LocalDate now = java.time.LocalDate.now();
        int annee = now.getYear();

        // Compter les écritures de l'année pour l'entreprise
        long count = ecritureDAO.countByEntrepriseAndYear(entrepriseId, annee);

        return String.format("EC-%d-%04d", annee, count + 1);
    }
    
    public void fixEntryNumberingConstraint() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            logger.info("=== FIXING ENTRY NUMBERING CONSTRAINT ===");
            System.out.println("=== FIXING ENTRY NUMBERING CONSTRAINT ===");

            // Step 1: Check table structure first
            try {
                String describeSQL = "DESCRIBE ecritures_comptables";
                var result = session.createNativeQuery(describeSQL).getResultList();
                System.out.println("Table structure for ecritures_comptables:");
                for (Object row : result) {
                    if (row instanceof Object[]) {
                        Object[] columns = (Object[]) row;
                        System.out.println("Column: " + columns[0] + " | Type: " + columns[1] + " | Null: " + columns[2] + " | Key: " + columns[3]);
                    } else {
                        System.out.println("Column: " + row);
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not describe table: " + e.getMessage());
            }

            // Step 2: Check existing indexes on all columns
            try {
                String checkIndexesSQL = "SHOW INDEX FROM ecritures_comptables";
                var result = session.createNativeQuery(checkIndexesSQL).getResultList();
                System.out.println("All indexes on ecritures_comptables: " + result.size());
                for (Object row : result) {
                    System.out.println("Index info: " + row);
                }
            } catch (Exception e) {
                System.out.println("Could not check indexes: " + e.getMessage());
            }

            // Step 2: Try to drop known constraint names
            String[] possibleConstraintNames = {
                "UK_ld1iedp121muy2bxvkyhmunlg",
                "numero_ecriture",
                "uk_numero_ecriture",
                "unique_numero_ecriture"
            };

            boolean constraintDropped = false;
            for (String constraintName : possibleConstraintNames) {
                try {
                    String dropSQL = "ALTER TABLE ecritures_comptables DROP INDEX " + constraintName;
                    session.createNativeQuery(dropSQL).executeUpdate();
                    logger.info("Successfully dropped constraint: {}", constraintName);
                    System.out.println("✓ Successfully dropped constraint: " + constraintName);
                    constraintDropped = true;
                    break;
                } catch (Exception e) {
                    System.out.println("⚠ Could not drop constraint " + constraintName + ": " + e.getMessage());
                }
            }

            if (!constraintDropped) {
                System.out.println("⚠ No known unique constraints found to drop. This might be OK if already fixed.");
            }

            // Step 3: Add the composite unique constraint
            try {
                String addConstraintSQL = "ALTER TABLE ecritures_comptables " +
                                        "ADD CONSTRAINT uk_numero_ecriture_entreprise " +
                                        "UNIQUE (numero_ecriture, entreprise_id)";
                session.createNativeQuery(addConstraintSQL).executeUpdate();
                logger.info("Added composite unique constraint on (numero_ecriture, entreprise_id)");
                System.out.println("✓ Added composite unique constraint on (numero_ecriture, entreprise_id)");
            } catch (Exception e) {
                if (e.getMessage().contains("Duplicate key name")) {
                    System.out.println("⚠ Composite constraint already exists: " + e.getMessage());
                } else {
                    logger.error("Failed to add composite constraint: {}", e.getMessage());
                    System.out.println("✗ Failed to add composite constraint: " + e.getMessage());
                    throw e;
                }
            }

            transaction.commit();
            logger.info("Database constraint migration completed");
            System.out.println("✓ Database constraint migration completed successfully");

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Failed to fix entry numbering constraint", e);
            System.out.println("✗ Failed to fix entry numbering constraint: " + e.getMessage());
            throw new RuntimeException("Failed to fix entry numbering constraint: " + e.getMessage(), e);
        }
    }

    private void mettreAJourSoldesComptes(EcritureComptable ecriture) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        Transaction transaction = session.beginTransaction();

        logger.debug("=== DÉBUT MISE À JOUR SOLDES ===");
        System.out.println("=== DÉBUT MISE À JOUR SOLDES ===");

        // Recharger l'écriture avec ses lignes
        EcritureComptable ecritureComplete = session.createQuery(
            "SELECT e FROM EcritureComptable e " +
            "LEFT JOIN FETCH e.lignes l " +
            "LEFT JOIN FETCH l.compte " +
            "WHERE e.id = :id", EcritureComptable.class)
            .setParameter("id", ecriture.getId())
            .uniqueResult();

        if (ecritureComplete != null && ecritureComplete.getStatut() == EcritureComptable.StatutEcriture.VALIDEE) {
            logger.debug("Mise à jour des soldes pour {} lignes", ecritureComplete.getLignes().size());
            System.out.println("Nombre de lignes à traiter: " + ecritureComplete.getLignes().size());

            for (LigneEcriture ligne : ecritureComplete.getLignes()) {
                Compte compte = ligne.getCompte();

                System.out.println("Traitement compte: " + compte.getNumeroCompte() + " (ID: " + compte.getId() + ")");
                System.out.println("  Avant - Solde débit: " + compte.getSoldeDebiteur() + ", Solde crédit: " + compte.getSoldeCrediteur());

                if (ligne.getMontantDebit().compareTo(BigDecimal.ZERO) > 0) {
                    logger.debug("Débit de {} sur compte {}", ligne.getMontantDebit(), compte.getNumeroCompte());
                    System.out.println("  Débit de: " + ligne.getMontantDebit());
                    compte.debiter(ligne.getMontantDebit());
                } else if (ligne.getMontantCredit().compareTo(BigDecimal.ZERO) > 0) {
                    logger.debug("Crédit de {} sur compte {}", ligne.getMontantCredit(), compte.getNumeroCompte());
                    System.out.println("  Crédit de: " + ligne.getMontantCredit());
                    compte.crediter(ligne.getMontantCredit());
                }

                session.saveOrUpdate(compte);
                System.out.println("  Après - Solde débit: " + compte.getSoldeDebiteur() + ", Solde crédit: " + compte.getSoldeCrediteur());

                logger.debug("Soldes mis à jour pour compte {}: Débit={}, Crédit={}",
                    compte.getNumeroCompte(), compte.getSoldeDebiteur(), compte.getSoldeCrediteur());
            }
        }

        transaction.commit();
        logger.debug("Mise à jour des soldes terminée avec succès");
        System.out.println("=== FIN MISE À JOUR SOLDES ===");

    } catch (Exception e) {
        logger.error("Erreur lors de la mise à jour des soldes", e);
        System.out.println("ERREUR lors de la mise à jour des soldes: " + e.getMessage());
        // Ne pas lancer d'exception car l'écriture a déjà été créée
    }
}

}