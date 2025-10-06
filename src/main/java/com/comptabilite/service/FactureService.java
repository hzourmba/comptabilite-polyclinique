package com.comptabilite.service;

import com.comptabilite.dao.FactureDAO;
import com.comptabilite.dao.LigneFactureDAO;
import com.comptabilite.dao.EcritureComptableDAO;
import com.comptabilite.dao.CompteDAO;
import com.comptabilite.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FactureService {

    private static final Logger logger = LoggerFactory.getLogger(FactureService.class);

    private final FactureDAO factureDAO;
    private final LigneFactureDAO ligneFactureDAO;
    private final EcritureComptableDAO ecritureDAO;
    private final CompteDAO compteDAO;

    public FactureService() {
        this.factureDAO = new FactureDAO();
        this.ligneFactureDAO = new LigneFactureDAO();
        this.ecritureDAO = new EcritureComptableDAO();
        this.compteDAO = new CompteDAO();
    }

    // === GESTION DES FACTURES ===

    public Facture creerFacture(Facture facture) {
        try {
            // Générer le numéro de facture si nécessaire
            if (facture.getNumeroFacture() == null || facture.getNumeroFacture().isEmpty()) {
                String numeroGenere = factureDAO.generateNextNumeroFacture(
                    facture.getTypeFacture(), facture.getEntreprise().getId());
                facture.setNumeroFacture(numeroGenere);
            }

            // Vérifier l'unicité du numéro
            if (factureDAO.existsByNumero(facture.getNumeroFacture())) {
                throw new RuntimeException("Une facture avec ce numéro existe déjà: " + facture.getNumeroFacture());
            }

            // Valider la cohérence client/fournisseur selon le type
            validerCoherencePartenaire(facture);

            // Calculer automatiquement la date d'échéance si non définie (30 jours par défaut)
            if (facture.getDateEcheance() == null) {
                facture.setDateEcheance(facture.getDateFacture().plusDays(30));
            }

            // Sauvegarder la facture
            Facture savedFacture = factureDAO.save(facture);
            logger.info("Facture créée avec succès: {}", savedFacture.getNumeroFacture());
            return savedFacture;

        } catch (Exception e) {
            logger.error("Erreur lors de la création de la facture", e);
            throw new RuntimeException("Erreur lors de la création de la facture: " + e.getMessage(), e);
        }
    }

    public Facture modifierFacture(Facture facture) {
        try {
            // Vérifier que la facture existe
            Optional<Facture> existanteOpt = factureDAO.findById(facture.getId());
            if (existanteOpt.isEmpty()) {
                throw new RuntimeException("Facture introuvable avec l'ID: " + facture.getId());
            }

            Facture existante = existanteOpt.get();

            // Empêcher la modification si la facture est payée
            if (existante.getStatut() == Facture.StatutFacture.PAYEE) {
                throw new RuntimeException("Impossible de modifier une facture payée");
            }

            // Vérifier l'unicité du numéro si modifié
            if (!existante.getNumeroFacture().equals(facture.getNumeroFacture())) {
                if (factureDAO.existsByNumero(facture.getNumeroFacture())) {
                    throw new RuntimeException("Une facture avec ce numéro existe déjà: " + facture.getNumeroFacture());
                }
            }

            // Valider la cohérence
            validerCoherencePartenaire(facture);

            // Recalculer les montants
            facture.calculerMontants();

            Facture updatedFacture = factureDAO.update(facture);
            logger.info("Facture modifiée avec succès: {}", updatedFacture.getNumeroFacture());
            return updatedFacture;

        } catch (Exception e) {
            logger.error("Erreur lors de la modification de la facture", e);
            throw new RuntimeException("Erreur lors de la modification de la facture: " + e.getMessage(), e);
        }
    }

    public void supprimerFacture(Facture facture) {
        try {
            // Empêcher la suppression si la facture est payée ou envoyée
            if (facture.getStatut() == Facture.StatutFacture.PAYEE ||
                facture.getStatut() == Facture.StatutFacture.ENVOYEE) {
                throw new RuntimeException("Impossible de supprimer une facture payée ou envoyée");
            }

            factureDAO.delete(facture);
            logger.info("Facture supprimée: {}", facture.getNumeroFacture());

        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de la facture", e);
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage(), e);
        }
    }

    // === WORKFLOW DES FACTURES ===

    public void envoyerFacture(Facture facture) {
        try {
            // Recharger la facture avec ses lignes pour éviter LazyInitializationException
            Optional<Facture> factureComplete = factureDAO.findByIdWithLignes(facture.getId());
            if (factureComplete.isEmpty()) {
                throw new RuntimeException("Facture introuvable");
            }

            facture = factureComplete.get();

            if (facture.getStatut() != Facture.StatutFacture.BROUILLON) {
                throw new RuntimeException("Seules les factures en brouillon peuvent être envoyées");
            }

            if (facture.getLignes().isEmpty()) {
                throw new RuntimeException("Impossible d'envoyer une facture sans lignes");
            }

            facture.envoyer();
            factureDAO.update(facture);

            // Générer l'écriture comptable pour les factures d'achat
            if (facture.isFactureAchat()) {
                genererEcritureAchat(facture);
            } else {
                genererEcritureVente(facture);
            }

            logger.info("Facture envoyée et écriture générée: {}", facture.getNumeroFacture());

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la facture", e);
            throw new RuntimeException("Erreur lors de l'envoi: " + e.getMessage(), e);
        }
    }

    public void marquerPayee(Facture facture, LocalDate datePaiement) {
        try {
            if (facture.getStatut() != Facture.StatutFacture.ENVOYEE) {
                throw new RuntimeException("Seules les factures envoyées peuvent être marquées comme payées");
            }

            facture.setStatut(Facture.StatutFacture.PAYEE);
            facture.setDatePaiement(datePaiement);
            factureDAO.update(facture);

            // Générer l'écriture de paiement
            genererEcriturePaiement(facture);

            logger.info("Facture marquée comme payée: {}", facture.getNumeroFacture());

        } catch (Exception e) {
            logger.error("Erreur lors du marquage de paiement", e);
            throw new RuntimeException("Erreur lors du paiement: " + e.getMessage(), e);
        }
    }

    // === INTÉGRATION COMPTABLE ===

    private void genererEcritureVente(Facture facture) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            org.hibernate.Session session = com.comptabilite.util.HibernateUtil.getSessionFactory().openSession();
            org.hibernate.Transaction transaction = session.beginTransaction();

            try {
                Long entrepriseId = facture.getEntreprise().getId();
                logger.info("Génération écriture de vente pour facture {} (tentative {})", facture.getNumeroFacture(), attempt);

            // Récupérer l'exercice comptable ouvert
            com.comptabilite.model.Exercice exercice = session.createQuery("FROM Exercice e WHERE e.entreprise.id = :entrepriseId AND e.statut = 'OUVERT' ORDER BY e.dateFin DESC", com.comptabilite.model.Exercice.class)
                .setParameter("entrepriseId", entrepriseId)
                .setMaxResults(1)
                .uniqueResult();

            if (exercice == null) {
                throw new RuntimeException("Aucun exercice comptable ouvert trouvé pour l'entreprise");
            }

            // Générer le numéro d'écriture
            String numeroEcriture = genererNumeroEcriture(session, "VT", entrepriseId);

            // Créer l'écriture comptable
            EcritureComptable ecriture = new EcritureComptable();
            ecriture.setDateEcriture(facture.getDateFacture());
            ecriture.setNumeroJournal("VT"); // Journal des ventes
            ecriture.setNumeroEcriture(numeroEcriture);
            ecriture.setLibelle("Facture de vente " + facture.getNumeroFacture());
            ecriture.setReferencePiece(facture.getNumeroFacture());
            ecriture.setEntreprise(facture.getEntreprise());
            ecriture.setExercice(exercice);
            ecriture.setUtilisateur(AuthenticationService.getInstance().getUtilisateurConnecte());
            ecriture.setStatut(EcritureComptable.StatutEcriture.VALIDEE);
            ecriture.setDateValidation(java.time.LocalDateTime.now());

            // Rechercher les comptes nécessaires
            Compte compteClient = session.createQuery("FROM Compte c WHERE c.numeroCompte = '411000' AND c.entreprise.id = :entrepriseId", Compte.class)
                .setParameter("entrepriseId", entrepriseId)
                .uniqueResult();
            if (compteClient == null) {
                throw new RuntimeException("Compte client 411000 introuvable");
            }

            Compte compteVente = session.createQuery("FROM Compte c WHERE c.numeroCompte = '707000' AND c.entreprise.id = :entrepriseId", Compte.class)
                .setParameter("entrepriseId", entrepriseId)
                .uniqueResult();
            if (compteVente == null) {
                throw new RuntimeException("Compte ventes 707000 introuvable");
            }

            // Ligne 1 : Débit Client (411)
            LigneEcriture ligneClient = new LigneEcriture();
            ligneClient.setCompte(compteClient);
            ligneClient.setLibelle("Client " + facture.getNomPartenaire());
            ligneClient.setMontantDebit(facture.getMontantTTC());
            ligneClient.setMontantCredit(BigDecimal.ZERO);
            ligneClient.setEcritureComptable(ecriture);
            ecriture.getLignes().add(ligneClient);

            // Ligne 2 : Crédit Ventes (707)
            LigneEcriture ligneVente = new LigneEcriture();
            ligneVente.setCompte(compteVente);
            ligneVente.setLibelle("Vente de prestations - " + facture.getObjet());
            ligneVente.setMontantDebit(BigDecimal.ZERO);
            ligneVente.setMontantCredit(facture.getMontantHT());
            ligneVente.setEcritureComptable(ecriture);
            ecriture.getLignes().add(ligneVente);

            // Ligne 3 : Crédit TVA collectée (44571) si TVA > 0
            if (facture.getMontantTVA().compareTo(BigDecimal.ZERO) > 0) {
                Compte compteTVA = session.createQuery("FROM Compte c WHERE c.numeroCompte = '445710' AND c.entreprise.id = :entrepriseId", Compte.class)
                    .setParameter("entrepriseId", entrepriseId)
                    .uniqueResult();
                if (compteTVA == null) {
                    throw new RuntimeException("Compte TVA collectée 445710 introuvable");
                }

                LigneEcriture ligneTVA = new LigneEcriture();
                ligneTVA.setCompte(compteTVA);
                ligneTVA.setLibelle("TVA collectée " + facture.getTauxTVA() + "%");
                ligneTVA.setMontantDebit(BigDecimal.ZERO);
                ligneTVA.setMontantCredit(facture.getMontantTVA());
                ligneTVA.setEcritureComptable(ecriture);
                ecriture.getLignes().add(ligneTVA);
            }

            // Sauvegarder dans la même session
            session.persist(ecriture);

                transaction.commit();
                logger.info("Écriture de vente générée avec succès pour facture: {}", facture.getNumeroFacture());
                return; // Succès, sortir de la boucle

            } catch (Exception e) {
                transaction.rollback();
                logger.error("Erreur lors de la génération de l'écriture de vente (tentative {})", attempt, e);

                // Si c'est une erreur de contrainte unique et qu'il reste des tentatives, réessayer
                if (attempt < maxRetries && e.getMessage().contains("Duplicate entry")) {
                    logger.warn("Conflit de numéro d'écriture détecté, tentative {} suivante...", attempt + 1);
                    try {
                        Thread.sleep(100); // Petite pause avant retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                // Dernière tentative ou erreur différente
                throw new RuntimeException("Erreur génération écriture vente: " + e.getMessage(), e);
            } finally {
                session.close();
            }
        }

        throw new RuntimeException("Échec de génération d'écriture après " + maxRetries + " tentatives");
    }

    private void genererEcritureAchat(Facture facture) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            org.hibernate.Session session = com.comptabilite.util.HibernateUtil.getSessionFactory().openSession();
            org.hibernate.Transaction transaction = session.beginTransaction();

            try {
                Long entrepriseId = facture.getEntreprise().getId();
                logger.info("Génération écriture d'achat pour facture {} et entreprise {} (tentative {})", facture.getNumeroFacture(), entrepriseId, attempt);

            // Rechercher tous les comptes nécessaires en une fois
            Compte compteAchat = session.createQuery("FROM Compte c WHERE c.numeroCompte = '607000' AND c.entreprise.id = :entrepriseId", Compte.class)
                .setParameter("entrepriseId", entrepriseId)
                .uniqueResult();

            if (compteAchat == null) {
                throw new RuntimeException("Compte achats 607000 introuvable");
            }

            Compte compteFournisseur = session.createQuery("FROM Compte c WHERE c.numeroCompte = '401000' AND c.entreprise.id = :entrepriseId", Compte.class)
                .setParameter("entrepriseId", entrepriseId)
                .uniqueResult();

            if (compteFournisseur == null) {
                throw new RuntimeException("Compte fournisseur 401000 introuvable");
            }

            Compte compteTVA = null;
            if (facture.getMontantTVA().compareTo(BigDecimal.ZERO) > 0) {
                compteTVA = session.createQuery("FROM Compte c WHERE c.numeroCompte = '445660' AND c.entreprise.id = :entrepriseId", Compte.class)
                    .setParameter("entrepriseId", entrepriseId)
                    .uniqueResult();

                if (compteTVA == null) {
                    throw new RuntimeException("Compte TVA déductible 445660 introuvable");
                }
            }

            // Récupérer l'exercice comptable ouvert
            com.comptabilite.model.Exercice exercice = session.createQuery("FROM Exercice e WHERE e.entreprise.id = :entrepriseId AND e.statut = 'OUVERT' ORDER BY e.dateFin DESC", com.comptabilite.model.Exercice.class)
                .setParameter("entrepriseId", entrepriseId)
                .setMaxResults(1)
                .uniqueResult();

            if (exercice == null) {
                throw new RuntimeException("Aucun exercice comptable ouvert trouvé pour l'entreprise");
            }

            // Générer le numéro d'écriture
            String numeroEcriture = genererNumeroEcriture(session, "AC", entrepriseId);

            // Créer l'écriture comptable
            EcritureComptable ecriture = new EcritureComptable();
            ecriture.setDateEcriture(facture.getDateFacture());
            ecriture.setNumeroJournal("AC");
            ecriture.setNumeroEcriture(numeroEcriture);
            ecriture.setLibelle("Facture d'achat " + facture.getNumeroFacture());
            ecriture.setReferencePiece(facture.getNumeroFacture());
            ecriture.setEntreprise(facture.getEntreprise());
            ecriture.setExercice(exercice);
            ecriture.setUtilisateur(AuthenticationService.getInstance().getUtilisateurConnecte());
            ecriture.setStatut(EcritureComptable.StatutEcriture.VALIDEE);
            ecriture.setDateValidation(java.time.LocalDateTime.now());

            // Ligne 1 : Débit Achats (607)
            LigneEcriture ligneAchat = new LigneEcriture();
            ligneAchat.setCompte(compteAchat);
            ligneAchat.setLibelle("Achat - " + facture.getObjet());
            ligneAchat.setMontantDebit(facture.getMontantHT());
            ligneAchat.setMontantCredit(BigDecimal.ZERO);
            ligneAchat.setEcritureComptable(ecriture);
            ecriture.getLignes().add(ligneAchat);

            // Ligne 2 : Débit TVA déductible si TVA > 0
            if (compteTVA != null) {
                LigneEcriture ligneTVA = new LigneEcriture();
                ligneTVA.setCompte(compteTVA);
                ligneTVA.setLibelle("TVA déductible " + facture.getTauxTVA() + "%");
                ligneTVA.setMontantDebit(facture.getMontantTVA());
                ligneTVA.setMontantCredit(BigDecimal.ZERO);
                ligneTVA.setEcritureComptable(ecriture);
                ecriture.getLignes().add(ligneTVA);
            }

            // Ligne 3 : Crédit Fournisseur (401)
            LigneEcriture ligneFournisseur = new LigneEcriture();
            ligneFournisseur.setCompte(compteFournisseur);
            ligneFournisseur.setLibelle("Fournisseur " + facture.getNomPartenaire());
            ligneFournisseur.setMontantDebit(BigDecimal.ZERO);
            ligneFournisseur.setMontantCredit(facture.getMontantTTC());
            ligneFournisseur.setEcritureComptable(ecriture);
            ecriture.getLignes().add(ligneFournisseur);

            // Sauvegarder dans la même session
            session.persist(ecriture);

                transaction.commit();
                logger.info("Écriture d'achat générée avec succès pour facture: {}", facture.getNumeroFacture());
                return; // Succès, sortir de la boucle

            } catch (Exception e) {
                transaction.rollback();
                logger.error("Erreur lors de la génération de l'écriture d'achat (tentative {})", attempt, e);

                // Si c'est une erreur de contrainte unique et qu'il reste des tentatives, réessayer
                if (attempt < maxRetries && e.getMessage().contains("Duplicate entry")) {
                    logger.warn("Conflit de numéro d'écriture détecté, tentative {} suivante...", attempt + 1);
                    try {
                        Thread.sleep(100); // Petite pause avant retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                // Dernière tentative ou erreur différente
                throw new RuntimeException("Erreur génération écriture achat: " + e.getMessage(), e);
            } finally {
                session.close();
            }
        }

        throw new RuntimeException("Échec de génération d'écriture après " + maxRetries + " tentatives");
    }

    private void genererEcriturePaiement(Facture facture) {
        try {
            // Créer l'écriture de paiement
            EcritureComptable ecriture = new EcritureComptable();
            ecriture.setDateEcriture(facture.getDatePaiement().atStartOfDay().toLocalDate());
            ecriture.setNumeroJournal("BQ"); // Journal de banque
            ecriture.setLibelle("Paiement facture " + facture.getNumeroFacture());
            ecriture.setReference("PAY-" + facture.getNumeroFacture());
            ecriture.setEntreprise(facture.getEntreprise());
            ecriture.setUtilisateur(AuthenticationService.getInstance().getUtilisateurConnecte());

            if (facture.isFactureVente()) {
                // Paiement client : Débit Banque, Crédit Client
                LigneEcriture ligneBanque = new LigneEcriture();
                Optional<Compte> compteBanque = compteDAO.findByNumeroCompte("512000");
                if (compteBanque.isEmpty()) {
                    throw new RuntimeException("Compte banque 512000 introuvable");
                }
                ligneBanque.setCompte(compteBanque.get());
                ligneBanque.setLibelle("Encaissement client");
                ligneBanque.setMontantDebit(facture.getMontantTTC());
                ligneBanque.setMontantCredit(BigDecimal.ZERO);
                ligneBanque.setEcritureComptable(ecriture);
                ecriture.getLignes().add(ligneBanque);

                LigneEcriture ligneClient = new LigneEcriture();
                Optional<Compte> compteClient = compteDAO.findByNumeroCompte("411000");
                if (compteClient.isEmpty()) {
                    throw new RuntimeException("Compte client 411000 introuvable");
                }
                ligneClient.setCompte(compteClient.get());
                ligneClient.setLibelle("Client " + facture.getNomPartenaire());
                ligneClient.setMontantDebit(BigDecimal.ZERO);
                ligneClient.setMontantCredit(facture.getMontantTTC());
                ligneClient.setEcritureComptable(ecriture);
                ecriture.getLignes().add(ligneClient);

            } else {
                // Paiement fournisseur : Débit Fournisseur, Crédit Banque
                LigneEcriture ligneFournisseur = new LigneEcriture();
                Optional<Compte> compteFournisseur = compteDAO.findByNumeroCompte("401000");
                if (compteFournisseur.isEmpty()) {
                    throw new RuntimeException("Compte fournisseur 401000 introuvable");
                }
                ligneFournisseur.setCompte(compteFournisseur.get());
                ligneFournisseur.setLibelle("Fournisseur " + facture.getNomPartenaire());
                ligneFournisseur.setMontantDebit(facture.getMontantTTC());
                ligneFournisseur.setMontantCredit(BigDecimal.ZERO);
                ligneFournisseur.setEcritureComptable(ecriture);
                ecriture.getLignes().add(ligneFournisseur);

                LigneEcriture ligneBanque = new LigneEcriture();
                Optional<Compte> compteBanque = compteDAO.findByNumeroCompte("512000");
                if (compteBanque.isEmpty()) {
                    throw new RuntimeException("Compte banque 512000 introuvable");
                }
                ligneBanque.setCompte(compteBanque.get());
                ligneBanque.setLibelle("Paiement fournisseur");
                ligneBanque.setMontantDebit(BigDecimal.ZERO);
                ligneBanque.setMontantCredit(facture.getMontantTTC());
                ligneBanque.setEcritureComptable(ecriture);
                ecriture.getLignes().add(ligneBanque);
            }

            ecritureDAO.save(ecriture);
            logger.info("Écriture de paiement générée pour facture: {}", facture.getNumeroFacture());

        } catch (Exception e) {
            logger.error("Erreur lors de la génération de l'écriture de paiement", e);
            throw new RuntimeException("Erreur génération écriture paiement: " + e.getMessage(), e);
        }
    }

    // === MÉTHODES DE RECHERCHE ===

    public List<Facture> getFacturesByEntreprise(Long entrepriseId) {
        return factureDAO.findByEntrepriseWithPartenaires(entrepriseId);
    }

    public List<Facture> getFacturesVente(Long entrepriseId) {
        return factureDAO.findFacturesVente(entrepriseId);
    }

    public List<Facture> getFacturesAchat(Long entrepriseId) {
        return factureDAO.findFacturesAchat(entrepriseId);
    }

    public List<Facture> getFacturesEnRetard(Long entrepriseId) {
        return factureDAO.findFacturesEnRetard(entrepriseId);
    }

    public List<Facture> rechercherFactures(String terme, Long entrepriseId) {
        return factureDAO.searchByText(terme, entrepriseId);
    }

    public Optional<Facture> findByNumero(String numeroFacture) {
        return factureDAO.findByNumero(numeroFacture);
    }

    // === STATISTIQUES ===

    public BigDecimal getChiffreAffaires(LocalDate debut, LocalDate fin, Long entrepriseId) {
        return factureDAO.calculateCAByPeriode(debut, fin, entrepriseId);
    }

    public BigDecimal getTotalAchats(LocalDate debut, LocalDate fin, Long entrepriseId) {
        return factureDAO.calculateAchatsByPeriode(debut, fin, entrepriseId);
    }

    // === VALIDATION PRIVÉE ===

    private void validerCoherencePartenaire(Facture facture) {
        if (facture.isFactureVente()) {
            if (facture.getClient() == null) {
                throw new RuntimeException("Un client est requis pour une facture de vente");
            }
            if (facture.getFournisseur() != null) {
                throw new RuntimeException("Un fournisseur ne peut pas être associé à une facture de vente");
            }
        } else {
            if (facture.getFournisseur() == null) {
                throw new RuntimeException("Un fournisseur est requis pour une facture d'achat");
            }
            if (facture.getClient() != null) {
                throw new RuntimeException("Un client ne peut pas être associé à une facture d'achat");
            }
        }
    }

    private String genererNumeroEcriture(org.hibernate.Session session, String journal, Long entrepriseId) {
        // Utiliser un verrou pour éviter les conditions de course
        String tableLock = "SELECT 1 FROM ecritures_comptables WHERE entreprise_id = :entrepriseId FOR UPDATE";
        session.createNativeQuery(tableLock)
            .setParameter("entrepriseId", entrepriseId)
            .list();

        // Trouver le dernier numéro d'écriture pour ce journal et cette entreprise
        String pattern = journal + "%";
        Long maxNumero = session.createQuery(
            "SELECT MAX(CAST(SUBSTRING(e.numeroEcriture, " + (journal.length() + 1) + ") AS long)) " +
            "FROM EcritureComptable e " +
            "WHERE e.numeroJournal = :journal AND e.entreprise.id = :entrepriseId " +
            "AND e.numeroEcriture LIKE :pattern", Long.class)
            .setParameter("journal", journal)
            .setParameter("entrepriseId", entrepriseId)
            .setParameter("pattern", pattern)
            .uniqueResult();

        Long prochainNumero = (maxNumero != null) ? maxNumero + 1 : 1;
        String numeroGenere = journal + String.format("%06d", prochainNumero);

        logger.info("Numéro d'écriture généré: {} pour journal: {} entreprise: {}", numeroGenere, journal, entrepriseId);
        return numeroGenere;
    }
}