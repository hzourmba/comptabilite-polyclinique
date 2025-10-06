package com.comptabilite.dao;

import com.comptabilite.model.Utilisateur;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class UtilisateurDAO extends BaseDAO<Utilisateur, Long> {

    public UtilisateurDAO() {
        super(Utilisateur.class);
    }

    public Optional<Utilisateur> findByNomUtilisateur(String nomUtilisateur) {
        String hql = "FROM Utilisateur u WHERE u.nomUtilisateur = :nomUtilisateur";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(hql, Utilisateur.class);
            query.setParameter("nomUtilisateur", nomUtilisateur);
            return Optional.ofNullable(query.uniqueResult());
        }
    }

    public Optional<Utilisateur> findByEmail(String email) {
        String hql = "FROM Utilisateur u WHERE u.email = :email";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(hql, Utilisateur.class);
            query.setParameter("email", email);
            return Optional.ofNullable(query.uniqueResult());
        }
    }

    public Optional<Utilisateur> authenticate(String nomUtilisateur, String motDePasse) {
        String hql = "FROM Utilisateur u LEFT JOIN FETCH u.entreprise WHERE u.nomUtilisateur = :nomUtilisateur AND u.motDePasse = :motDePasse AND u.actif = true";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(hql, Utilisateur.class);
            query.setParameter("nomUtilisateur", nomUtilisateur);
            query.setParameter("motDePasse", motDePasse);
            return Optional.ofNullable(query.uniqueResult());
        }
    }

    public List<Utilisateur> findByEntreprise(Long entrepriseId) {
        String hql = "FROM Utilisateur u WHERE u.entreprise.id = :entrepriseId";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(hql, Utilisateur.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        }
    }

    public List<Utilisateur> findByRole(Utilisateur.RoleUtilisateur role) {
        String hql = "FROM Utilisateur u WHERE u.role = :role";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(hql, Utilisateur.class);
            query.setParameter("role", role);
            return query.list();
        }
    }

    public List<Utilisateur> findActifs() {
        String hql = "FROM Utilisateur u WHERE u.actif = true";
        return executeQuery(hql);
    }

    public boolean existsByNomUtilisateur(String nomUtilisateur) {
        return findByNomUtilisateur(nomUtilisateur).isPresent();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}