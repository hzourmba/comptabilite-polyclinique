package com.comptabilite.dao;

import com.comptabilite.model.LigneEcriture;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

public class LigneEcritureDAO extends BaseDAO<LigneEcriture, Long> {

    public LigneEcritureDAO() {
        super(LigneEcriture.class);
    }

    public List<LigneEcriture> findByEcriture(Long ecritureId) {
        String hql = "FROM LigneEcriture l WHERE l.ecritureComptable.id = :ecritureId ORDER BY l.id";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<LigneEcriture> query = session.createQuery(hql, LigneEcriture.class);
            query.setParameter("ecritureId", ecritureId);
            return query.list();
        }
    }

    public List<LigneEcriture> findByCompte(Long compteId) {
        String hql = "FROM LigneEcriture l WHERE l.compte.id = :compteId ORDER BY l.ecritureComptable.dateEcriture DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<LigneEcriture> query = session.createQuery(hql, LigneEcriture.class);
            query.setParameter("compteId", compteId);
            return query.list();
        }
    }

    public List<LigneEcriture> findByCompteAndDateRange(Long compteId, LocalDate dateDebut, LocalDate dateFin) {
        System.out.println("DEBUG: Recherche lignes pour compte ID: " + compteId + ", du " + dateDebut + " au " + dateFin);

        // Essayons d'abord une requête plus simple
        String hql = "SELECT l FROM LigneEcriture l " +
                    "JOIN FETCH l.ecritureComptable ec " +
                    "JOIN FETCH l.compte c " +
                    "WHERE c.id = :compteId " +
                    "AND ec.dateEcriture BETWEEN :dateDebut AND :dateFin " +
                    "ORDER BY ec.dateEcriture ASC";

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<LigneEcriture> query = session.createQuery(hql, LigneEcriture.class);
            query.setParameter("compteId", compteId);
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);

            List<LigneEcriture> results = query.list();
            System.out.println("DEBUG: Nombre de résultats trouvés: " + results.size());

            return results;
        } catch (Exception e) {
            System.out.println("DEBUG: Erreur dans la requête HQL: " + e.getMessage());
            e.printStackTrace();

            // Fallback: requête SQL native
            return findByCompteAndDateRangeNative(compteId, dateDebut, dateFin);
        }
    }

    // Méthode fallback avec requête SQL native
    private List<LigneEcriture> findByCompteAndDateRangeNative(Long compteId, LocalDate dateDebut, LocalDate dateFin) {
        System.out.println("DEBUG: Utilisation de la requête SQL native");

        String sql = "SELECT le.* FROM lignes_ecriture le " +
                    "INNER JOIN ecritures_comptables ec ON le.ecritureComptable_id = ec.id " +
                    "WHERE le.compte_id = :compteId " +
                    "AND ec.dateEcriture BETWEEN :dateDebut AND :dateFin " +
                    "ORDER BY ec.dateEcriture ASC";

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            @SuppressWarnings("unchecked")
            Query<LigneEcriture> query = session.createNativeQuery(sql, LigneEcriture.class);
            query.setParameter("compteId", compteId);
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);

            List<LigneEcriture> results = query.list();
            System.out.println("DEBUG: Résultats avec SQL native: " + results.size());

            return results;
        }
    }
}