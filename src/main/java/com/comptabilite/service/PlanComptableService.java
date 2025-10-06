package com.comptabilite.service;

import com.comptabilite.dao.CompteDAO;
import com.comptabilite.dao.EntrepriseDAO;
import com.comptabilite.model.Compte;
import com.comptabilite.model.Entreprise;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PlanComptableService {

    private static final Logger logger = LoggerFactory.getLogger(PlanComptableService.class);
    private final CompteDAO compteDAO;
    private final EntrepriseDAO entrepriseDAO;

    public PlanComptableService() {
        this.compteDAO = new CompteDAO();
        this.entrepriseDAO = new EntrepriseDAO();
    }

    public void initialiserPlanComptableFrancais() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Utiliser des requêtes SQL natives pour éviter les problèmes de session
            String sql = """
                INSERT IGNORE INTO comptes (numeroCompte, libelle, typeCompte, classeCompte, soldeDebiteur, soldeCrediteur, actif, dateCreation, entreprise_id) VALUES
                ('101', 'Capital', 'PASSIF', 'CLASSE_1_COMPTES_CAPITAUX', 0.00, 0.00, true, NOW(), 1),
                ('106', 'Réserves', 'PASSIF', 'CLASSE_1_COMPTES_CAPITAUX', 0.00, 0.00, true, NOW(), 1),
                ('120', 'Résultat de l\\'exercice', 'PASSIF', 'CLASSE_1_COMPTES_CAPITAUX', 0.00, 0.00, true, NOW(), 1),
                ('164', 'Emprunts auprès des établissements de crédit', 'PASSIF', 'CLASSE_1_COMPTES_CAPITAUX', 0.00, 0.00, true, NOW(), 1),
                ('205', 'Concessions et droits similaires', 'ACTIF', 'CLASSE_2_COMPTES_IMMOBILISATIONS', 0.00, 0.00, true, NOW(), 1),
                ('213', 'Constructions', 'ACTIF', 'CLASSE_2_COMPTES_IMMOBILISATIONS', 0.00, 0.00, true, NOW(), 1),
                ('215', 'Installations techniques', 'ACTIF', 'CLASSE_2_COMPTES_IMMOBILISATIONS', 0.00, 0.00, true, NOW(), 1),
                ('218', 'Autres immobilisations corporelles', 'ACTIF', 'CLASSE_2_COMPTES_IMMOBILISATIONS', 0.00, 0.00, true, NOW(), 1),
                ('401', 'Fournisseurs', 'PASSIF', 'CLASSE_4_COMPTES_TIERS', 0.00, 0.00, true, NOW(), 1),
                ('411', 'Clients', 'ACTIF', 'CLASSE_4_COMPTES_TIERS', 0.00, 0.00, true, NOW(), 1),
                ('421', 'Personnel - Rémunérations dues', 'PASSIF', 'CLASSE_4_COMPTES_TIERS', 0.00, 0.00, true, NOW(), 1),
                ('445', 'État - TVA', 'PASSIF', 'CLASSE_4_COMPTES_TIERS', 0.00, 0.00, true, NOW(), 1),
                ('512', 'Banques', 'ACTIF', 'CLASSE_5_COMPTES_FINANCIERS', 0.00, 0.00, true, NOW(), 1),
                ('530', 'Caisse', 'ACTIF', 'CLASSE_5_COMPTES_FINANCIERS', 0.00, 0.00, true, NOW(), 1),
                ('607', 'Achats de marchandises', 'CHARGE', 'CLASSE_6_COMPTES_CHARGES', 0.00, 0.00, true, NOW(), 1),
                ('613', 'Locations', 'CHARGE', 'CLASSE_6_COMPTES_CHARGES', 0.00, 0.00, true, NOW(), 1),
                ('641', 'Rémunérations du personnel', 'CHARGE', 'CLASSE_6_COMPTES_CHARGES', 0.00, 0.00, true, NOW(), 1),
                ('701', 'Ventes de produits finis', 'PRODUIT', 'CLASSE_7_COMPTES_PRODUITS', 0.00, 0.00, true, NOW(), 1),
                ('707', 'Ventes de marchandises', 'PRODUIT', 'CLASSE_7_COMPTES_PRODUITS', 0.00, 0.00, true, NOW(), 1)
                """;

            int result = session.createNativeQuery(sql).executeUpdate();
            transaction.commit();

            logger.info("Plan comptable français initialisé: {} comptes ajoutés", result);

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Erreur lors de l'initialisation du plan comptable français", e);
            throw new RuntimeException("Erreur lors de l'initialisation du plan comptable: " + e.getMessage(), e);
        }
    }

    public List<Compte> getComptesByEntreprise(Long entrepriseId) {
        return compteDAO.findByEntreprise(entrepriseId);
    }

    public void supprimerCompte(Compte compte) {
        compteDAO.delete(compte);
    }
}