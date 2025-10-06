package com.comptabilite.dao;

import com.comptabilite.model.LigneFacture;
import com.comptabilite.model.Facture;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LigneFactureDAO extends BaseDAO<LigneFacture, Long> {

    private static final Logger logger = LoggerFactory.getLogger(LigneFactureDAO.class);

    public LigneFactureDAO() {
        super(LigneFacture.class);
    }

    // Rechercher les lignes d'une facture
    public List<LigneFacture> findByFacture(Facture facture) {
        try (Session session = com.comptabilite.util.HibernateUtil.getSessionFactory().openSession()) {
            Query<LigneFacture> query = session.createQuery(
                "FROM LigneFacture lf WHERE lf.facture = :facture ORDER BY lf.id ASC", LigneFacture.class);
            query.setParameter("facture", facture);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche des lignes de facture", e);
            return List.of();
        }
    }

    // Rechercher par designation
    public List<LigneFacture> findByDesignation(String designation) {
        try (Session session = com.comptabilite.util.HibernateUtil.getSessionFactory().openSession()) {
            String searchPattern = "%" + designation.toLowerCase() + "%";
            Query<LigneFacture> query = session.createQuery(
                "FROM LigneFacture lf WHERE LOWER(lf.designation) LIKE :search", LigneFacture.class);
            query.setParameter("search", searchPattern);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par désignation", e);
            return List.of();
        }
    }

    // Supprimer toutes les lignes d'une facture
    public void deleteByFacture(Facture facture) {
        try (Session session = com.comptabilite.util.HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Query<?> query = session.createQuery(
                "DELETE FROM LigneFacture lf WHERE lf.facture = :facture");
            query.setParameter("facture", facture);
            query.executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression des lignes de facture", e);
            throw new RuntimeException("Erreur lors de la suppression des lignes", e);
        }
    }
}