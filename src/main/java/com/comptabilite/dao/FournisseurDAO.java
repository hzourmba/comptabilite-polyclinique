package com.comptabilite.dao;

import com.comptabilite.model.Fournisseur;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class FournisseurDAO extends BaseDAO<Fournisseur, Long> {

    public FournisseurDAO() {
        super(Fournisseur.class);
    }

    public List<Fournisseur> findByEntreprise(Long entrepriseId) {
        String hql = "FROM Fournisseur f ORDER BY f.nom";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Fournisseur> query = session.createQuery(hql, Fournisseur.class);
            return query.list();
        }
    }

    public Fournisseur findByCodeFournisseur(String codeFournisseur) {
        String hql = "FROM Fournisseur f WHERE f.codeFournisseur = :codeFournisseur";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Fournisseur> query = session.createQuery(hql, Fournisseur.class);
            query.setParameter("codeFournisseur", codeFournisseur);
            return query.uniqueResult();
        }
    }

    public boolean existsByCodeFournisseur(String codeFournisseur) {
        return findByCodeFournisseur(codeFournisseur) != null;
    }

    public List<Fournisseur> findByStatut(Fournisseur.StatutFournisseur statut, Long entrepriseId) {
        String hql = "FROM Fournisseur f WHERE f.statutFournisseur = :statut ORDER BY f.nom";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Fournisseur> query = session.createQuery(hql, Fournisseur.class);
            query.setParameter("statut", statut);
            return query.list();
        }
    }

    public List<Fournisseur> searchByNomOrEmail(String terme) {
        String hql = "FROM Fournisseur f WHERE LOWER(f.nom) LIKE LOWER(:terme) OR LOWER(f.prenom) LIKE LOWER(:terme) OR LOWER(f.email) LIKE LOWER(:terme) OR LOWER(f.raisonSociale) LIKE LOWER(:terme) ORDER BY f.nom";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Fournisseur> query = session.createQuery(hql, Fournisseur.class);
            query.setParameter("terme", "%" + terme + "%");
            return query.list();
        }
    }

    public long countFournisseurs() {
        String hql = "SELECT COUNT(f) FROM Fournisseur f";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(hql, Long.class);
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
        }
    }

    public long countFacturesByFournisseur(Long fournisseurId) {
        String hql = "SELECT COUNT(f) FROM Facture f WHERE f.fournisseur.id = :fournisseurId";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("fournisseurId", fournisseurId);
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
        }
    }

    public List<Fournisseur> findFournisseursWithSoldePositif(Long entrepriseId) {
        String hql = "FROM Fournisseur f WHERE f.soldeFournisseur > 0 ORDER BY f.soldeFournisseur DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Fournisseur> query = session.createQuery(hql, Fournisseur.class);
            return query.list();
        }
    }

    public List<Fournisseur> findFournisseursWithSoldeNegatif(Long entrepriseId) {
        String hql = "FROM Fournisseur f WHERE f.soldeFournisseur < 0 ORDER BY f.soldeFournisseur ASC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Fournisseur> query = session.createQuery(hql, Fournisseur.class);
            return query.list();
        }
    }

    public List<Fournisseur> findByType(Fournisseur.TypeFournisseur type, Long entrepriseId) {
        String hql = "FROM Fournisseur f WHERE f.typeFournisseur = :type ORDER BY f.nom";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Fournisseur> query = session.createQuery(hql, Fournisseur.class);
            query.setParameter("type", type);
            return query.list();
        }
    }

    public String generateNextCodeFournisseur() {
        String hql = "SELECT MAX(f.codeFournisseur) FROM Fournisseur f WHERE f.codeFournisseur LIKE 'F%'";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<String> query = session.createQuery(hql, String.class);
            String maxCode = query.uniqueResult();

            if (maxCode == null) {
                return "F0001";
            }

            try {
                int numero = Integer.parseInt(maxCode.substring(1));
                return String.format("F%04d", numero + 1);
            } catch (NumberFormatException e) {
                return "F0001";
            }
        }
    }
}