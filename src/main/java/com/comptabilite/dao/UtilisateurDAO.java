package com.comptabilite.dao;

import com.comptabilite.model.Utilisateur;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UtilisateurDAO extends BaseDAO<Utilisateur, Long> {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurDAO.class);

    public UtilisateurDAO() {
        super(Utilisateur.class);
    }

    @Override
    public Optional<Utilisateur> findById(Long id) {
        // Surcharger pour charger l'entreprise avec FETCH JOIN
        String hql = "FROM Utilisateur u LEFT JOIN FETCH u.entreprise WHERE u.id = :id";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(hql, Utilisateur.class);
            query.setParameter("id", id);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par ID pour {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Erreur lors de la recherche", e);
        }
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
        // D'abord trouver l'utilisateur par nom
        String hql = "FROM Utilisateur u LEFT JOIN FETCH u.entreprise WHERE u.nomUtilisateur = :nomUtilisateur AND u.actif = true";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(hql, Utilisateur.class);
            query.setParameter("nomUtilisateur", nomUtilisateur);
            Utilisateur user = query.uniqueResult();

            if (user == null) {
                return Optional.empty();
            }

            // Vérifier le mot de passe (gérer les cas clair et hashé)
            String passwordInDb = user.getMotDePasse();
            String hashedPassword = Integer.toString(motDePasse.hashCode());

            logger.debug("Tentative d'authentification pour: {}", nomUtilisateur);
            logger.debug("Mot de passe saisi: {}", motDePasse);
            logger.debug("Mot de passe saisi hashé: {}", hashedPassword);
            logger.debug("Mot de passe en base: {}", passwordInDb);

            // Vérifier mot de passe clair ou hashé
            if (passwordInDb.equals(motDePasse) || passwordInDb.equals(hashedPassword)) {
                logger.info("Authentification réussie pour: {}", nomUtilisateur);
                return Optional.of(user);
            } else {
                logger.warn("Échec d'authentification pour: {} - mot de passe incorrect", nomUtilisateur);
                return Optional.empty();
            }
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