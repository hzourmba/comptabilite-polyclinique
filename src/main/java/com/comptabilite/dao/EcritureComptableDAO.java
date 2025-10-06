package com.comptabilite.dao;

import com.comptabilite.model.EcritureComptable;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EcritureComptableDAO extends BaseDAO<EcritureComptable, Long> {

    public EcritureComptableDAO() {
        super(EcritureComptable.class);
    }

    public Optional<EcritureComptable> findByNumeroEcriture(String numeroEcriture) {
        String hql = "FROM EcritureComptable e WHERE e.numeroEcriture = :numeroEcriture";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EcritureComptable> query = session.createQuery(hql, EcritureComptable.class);
            query.setParameter("numeroEcriture", numeroEcriture);
            return Optional.ofNullable(query.uniqueResult());
        }
    }

    public List<EcritureComptable> findByExercice(Long exerciceId) {
        String hql = "FROM EcritureComptable e WHERE e.exercice.id = :exerciceId ORDER BY e.dateEcriture DESC, e.numeroEcriture";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EcritureComptable> query = session.createQuery(hql, EcritureComptable.class);
            query.setParameter("exerciceId", exerciceId);
            return query.list();
        }
    }

    public List<EcritureComptable> findByDateRange(LocalDate dateDebut, LocalDate dateFin, Long exerciceId) {
        String hql = "FROM EcritureComptable e WHERE e.dateEcriture BETWEEN :dateDebut AND :dateFin AND e.exercice.id = :exerciceId ORDER BY e.dateEcriture DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EcritureComptable> query = session.createQuery(hql, EcritureComptable.class);
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);
            query.setParameter("exerciceId", exerciceId);
            return query.list();
        }
    }

    public List<EcritureComptable> findByStatut(EcritureComptable.StatutEcriture statut, Long exerciceId) {
        String hql = "FROM EcritureComptable e WHERE e.statut = :statut AND e.exercice.id = :exerciceId ORDER BY e.dateEcriture DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EcritureComptable> query = session.createQuery(hql, EcritureComptable.class);
            query.setParameter("statut", statut);
            query.setParameter("exerciceId", exerciceId);
            return query.list();
        }
    }

    public List<EcritureComptable> findByUtilisateur(Long utilisateurId, Long exerciceId) {
        String hql = "FROM EcritureComptable e WHERE e.utilisateur.id = :utilisateurId AND e.exercice.id = :exerciceId ORDER BY e.dateEcriture DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EcritureComptable> query = session.createQuery(hql, EcritureComptable.class);
            query.setParameter("utilisateurId", utilisateurId);
            query.setParameter("exerciceId", exerciceId);
            return query.list();
        }
    }

    public List<EcritureComptable> findByJournal(String numeroJournal, Long exerciceId) {
        String hql = "FROM EcritureComptable e WHERE e.numeroJournal = :numeroJournal AND e.exercice.id = :exerciceId ORDER BY e.dateEcriture DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EcritureComptable> query = session.createQuery(hql, EcritureComptable.class);
            query.setParameter("numeroJournal", numeroJournal);
            query.setParameter("exerciceId", exerciceId);
            return query.list();
        }
    }

    public List<EcritureComptable> searchByLibelle(String libelle, Long exerciceId) {
        String hql = "FROM EcritureComptable e WHERE LOWER(e.libelle) LIKE LOWER(:libelle) AND e.exercice.id = :exerciceId ORDER BY e.dateEcriture DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EcritureComptable> query = session.createQuery(hql, EcritureComptable.class);
            query.setParameter("libelle", "%" + libelle + "%");
            query.setParameter("exerciceId", exerciceId);
            return query.list();
        }
    }

    public List<EcritureComptable> findBrouillons(Long exerciceId) {
        return findByStatut(EcritureComptable.StatutEcriture.BROUILLON, exerciceId);
    }

    public List<EcritureComptable> findValidees(Long exerciceId) {
        return findByStatut(EcritureComptable.StatutEcriture.VALIDEE, exerciceId);
    }

    public String generateNextNumeroEcriture(Long exerciceId) {
        String hql = "SELECT MAX(e.numeroEcriture) FROM EcritureComptable e WHERE e.exercice.id = :exerciceId";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<String> query = session.createQuery(hql, String.class);
            query.setParameter("exerciceId", exerciceId);
            String lastNumero = query.uniqueResult();

            if (lastNumero == null) {
                return "ECR-" + exerciceId + "-001";
            }

            String[] parts = lastNumero.split("-");
            if (parts.length == 3) {
                int nextNumber = Integer.parseInt(parts[2]) + 1;
                return "ECR-" + exerciceId + "-" + String.format("%03d", nextNumber);
            }

            return "ECR-" + exerciceId + "-001";
        }
    }

    public boolean existsByNumeroEcriture(String numeroEcriture) {
        return findByNumeroEcriture(numeroEcriture).isPresent();
    }

    public List<EcritureComptable> findByEntreprise(Long entrepriseId) {
        String hql = "FROM EcritureComptable e WHERE e.entreprise.id = :entrepriseId ORDER BY e.dateEcriture DESC, e.numeroEcriture";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EcritureComptable> query = session.createQuery(hql, EcritureComptable.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        }
    }

    public long countByEntrepriseAndYear(Long entrepriseId, int year) {
        String hql = "SELECT COUNT(e) FROM EcritureComptable e WHERE e.entreprise.id = :entrepriseId AND YEAR(e.dateEcriture) = :year";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("entrepriseId", entrepriseId);
            query.setParameter("year", year);
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
        }
    }
}