package com.comptabilite.dao;

import com.comptabilite.model.Client;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ClientDAO extends BaseDAO<Client, Long> {

    public ClientDAO() {
        super(Client.class);
    }

    public List<Client> findByEntreprise(Long entrepriseId) {
        // Temporairement, retourner tous les clients
        String hql = "FROM Client c ORDER BY c.nom, c.prenom";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Client> query = session.createQuery(hql, Client.class);
            return query.list();
        }
    }

    public Client findByCodeClient(String codeClient) {
        String hql = "FROM Client c WHERE c.codeClient = :codeClient";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Client> query = session.createQuery(hql, Client.class);
            query.setParameter("codeClient", codeClient);
            return query.uniqueResult();
        }
    }

    public boolean existsByCodeClient(String codeClient) {
        return findByCodeClient(codeClient) != null;
    }

    public List<Client> findByStatut(Client.StatutClient statut, Long entrepriseId) {
        // Temporairement, ignorer l'entreprise
        String hql = "FROM Client c WHERE c.statutClient = :statut ORDER BY c.nom, c.prenom";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Client> query = session.createQuery(hql, Client.class);
            query.setParameter("statut", statut);
            return query.list();
        }
    }

    public List<Client> searchByNomOrEmail(String terme) {
        String hql = "FROM Client c WHERE LOWER(c.nom) LIKE LOWER(:terme) OR LOWER(c.prenom) LIKE LOWER(:terme) OR LOWER(c.email) LIKE LOWER(:terme) ORDER BY c.nom, c.prenom";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Client> query = session.createQuery(hql, Client.class);
            query.setParameter("terme", "%" + terme + "%");
            return query.list();
        }
    }

    public long countClients() {
        String hql = "SELECT COUNT(c) FROM Client c";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(hql, Long.class);
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
        }
    }

    public long countFacturesByClient(Long clientId) {
        String hql = "SELECT COUNT(f) FROM Facture f WHERE f.client.id = :clientId";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("clientId", clientId);
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
        }
    }

    public List<Client> findClientsWithSoldePositif(Long entrepriseId) {
        String hql = "FROM Client c WHERE c.soldeClient > 0 AND c.entreprise.id = :entrepriseId ORDER BY c.soldeClient DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Client> query = session.createQuery(hql, Client.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        }
    }

    public List<Client> findClientsWithSoldeNegatif(Long entrepriseId) {
        String hql = "FROM Client c WHERE c.soldeClient < 0 ORDER BY c.soldeClient ASC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Client> query = session.createQuery(hql, Client.class);
            return query.list();
        }
    }

}