package com.comptabilite.dao;

import com.comptabilite.model.Exercice;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ExerciceDAO extends BaseDAO<Exercice, Long> {

    public ExerciceDAO() {
        super(Exercice.class);
    }

    public List<Exercice> findByEntreprise(Long entrepriseId) {
        String hql = "FROM Exercice e WHERE e.entreprise.id = :entrepriseId ORDER BY e.dateDebut DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Exercice> query = session.createQuery(hql, Exercice.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        }
    }

    public Optional<Exercice> findExerciceEnCours(Long entrepriseId) {
        LocalDate today = LocalDate.now();
        String hql = "FROM Exercice e WHERE e.entreprise.id = :entrepriseId AND e.statut = 'OUVERT' AND :today BETWEEN e.dateDebut AND e.dateFin";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Exercice> query = session.createQuery(hql, Exercice.class);
            query.setParameter("entrepriseId", entrepriseId);
            query.setParameter("today", today);
            return Optional.ofNullable(query.uniqueResult());
        }
    }

    public List<Exercice> findByStatut(Exercice.StatutExercice statut, Long entrepriseId) {
        String hql = "FROM Exercice e WHERE e.statut = :statut AND e.entreprise.id = :entrepriseId ORDER BY e.dateDebut DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Exercice> query = session.createQuery(hql, Exercice.class);
            query.setParameter("statut", statut);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        }
    }

    public Optional<Exercice> findByAnnee(int annee, Long entrepriseId) {
        String hql = "FROM Exercice e WHERE YEAR(e.dateDebut) = :annee AND e.entreprise.id = :entrepriseId";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Exercice> query = session.createQuery(hql, Exercice.class);
            query.setParameter("annee", annee);
            query.setParameter("entrepriseId", entrepriseId);
            return Optional.ofNullable(query.uniqueResult());
        }
    }
}