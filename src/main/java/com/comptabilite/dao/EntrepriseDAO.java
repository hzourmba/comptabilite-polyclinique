package com.comptabilite.dao;

import com.comptabilite.model.Entreprise;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class EntrepriseDAO extends BaseDAO<Entreprise, Long> {

    public EntrepriseDAO() {
        super(Entreprise.class);
    }

    public List<Entreprise> findActives() {
        String hql = "FROM Entreprise e WHERE e.active = true";
        return executeQuery(hql);
    }

    public Optional<Entreprise> findBySiret(String siret) {
        String hql = "FROM Entreprise e WHERE e.siret = :siret";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Entreprise> query = session.createQuery(hql, Entreprise.class);
            query.setParameter("siret", siret);
            return Optional.ofNullable(query.uniqueResult());
        }
    }

    public List<Entreprise> searchByRaisonSociale(String raisonSociale) {
        String hql = "FROM Entreprise e WHERE LOWER(e.raisonSociale) LIKE LOWER(:raisonSociale)";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Entreprise> query = session.createQuery(hql, Entreprise.class);
            query.setParameter("raisonSociale", "%" + raisonSociale + "%");
            return query.list();
        }
    }
}