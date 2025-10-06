package com.comptabilite.dao;

import com.comptabilite.model.Facture;
import com.comptabilite.model.Client;
import com.comptabilite.model.Fournisseur;
import org.hibernate.Session;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FactureDAO extends BaseDAO<Facture, Long> {

    private static final Logger logger = LoggerFactory.getLogger(FactureDAO.class);

    public FactureDAO() {
        super(Facture.class);
    }

    // Rechercher par numéro de facture
    public Optional<Facture> findByNumero(String numeroFacture) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Facture> query = session.createQuery(
                "FROM Facture f WHERE f.numeroFacture = :numero", Facture.class);
            query.setParameter("numero", numeroFacture);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de facture par numéro: " + numeroFacture, e);
            return Optional.empty();
        }
    }

    // Rechercher par ID avec chargement des lignes et partenaires
    public Optional<Facture> findByIdWithLignes(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Facture> query = session.createQuery(
                "FROM Facture f " +
                "LEFT JOIN FETCH f.client " +
                "LEFT JOIN FETCH f.fournisseur " +
                "LEFT JOIN FETCH f.lignes " +
                "WHERE f.id = :id", Facture.class);
            query.setParameter("id", id);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de facture par ID avec lignes: " + id, e);
            return Optional.empty();
        }
    }

    // Rechercher les factures par entreprise
    public List<Facture> findByEntreprise(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Facture> query = session.createQuery(
                "FROM Facture f WHERE f.entreprise.id = :entrepriseId ORDER BY f.dateFacture DESC", Facture.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de factures par entreprise: " + entrepriseId, e);
            return List.of();
        }
    }

    // Rechercher les factures par entreprise avec chargement des partenaires et lignes
    public List<Facture> findByEntrepriseWithPartenaires(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Facture> query = session.createQuery(
                "FROM Facture f " +
                "LEFT JOIN FETCH f.client " +
                "LEFT JOIN FETCH f.fournisseur " +
                "LEFT JOIN FETCH f.lignes " +
                "WHERE f.entreprise.id = :entrepriseId " +
                "ORDER BY f.dateFacture DESC", Facture.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de factures par entreprise avec partenaires: " + entrepriseId, e);
            return List.of();
        }
    }

    // Rechercher les factures de vente (clients)
    public List<Facture> findFacturesVente(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Facture> query = session.createQuery(
                "FROM Facture f WHERE f.entreprise.id = :entrepriseId " +
                "AND (f.typeFacture = 'VENTE' OR f.typeFacture = 'AVOIR_VENTE') " +
                "ORDER BY f.dateFacture DESC", Facture.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de factures de vente", e);
            return List.of();
        }
    }

    // Rechercher les factures d'achat (fournisseurs)
    public List<Facture> findFacturesAchat(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Facture> query = session.createQuery(
                "FROM Facture f WHERE f.entreprise.id = :entrepriseId " +
                "AND (f.typeFacture = 'ACHAT' OR f.typeFacture = 'AVOIR_ACHAT') " +
                "ORDER BY f.dateFacture DESC", Facture.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de factures d'achat", e);
            return List.of();
        }
    }

    // Rechercher par client
    public List<Facture> findByClient(Client client) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Facture> query = session.createQuery(
                "FROM Facture f WHERE f.client = :client ORDER BY f.dateFacture DESC", Facture.class);
            query.setParameter("client", client);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de factures par client", e);
            return List.of();
        }
    }

    // Rechercher par fournisseur
    public List<Facture> findByFournisseur(Fournisseur fournisseur) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Facture> query = session.createQuery(
                "FROM Facture f WHERE f.fournisseur = :fournisseur ORDER BY f.dateFacture DESC", Facture.class);
            query.setParameter("fournisseur", fournisseur);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de factures par fournisseur", e);
            return List.of();
        }
    }

    // Rechercher par statut
    public List<Facture> findByStatut(Facture.StatutFacture statut, Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Facture> query = session.createQuery(
                "FROM Facture f WHERE f.statut = :statut AND f.entreprise.id = :entrepriseId " +
                "ORDER BY f.dateFacture DESC", Facture.class);
            query.setParameter("statut", statut);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de factures par statut", e);
            return List.of();
        }
    }

    // Rechercher les factures en retard
    public List<Facture> findFacturesEnRetard(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Facture> query = session.createQuery(
                "FROM Facture f WHERE f.entreprise.id = :entrepriseId " +
                "AND f.dateEcheance < :today " +
                "AND f.statut NOT IN ('PAYEE', 'ANNULEE') " +
                "ORDER BY f.dateEcheance ASC", Facture.class);
            query.setParameter("entrepriseId", entrepriseId);
            query.setParameter("today", LocalDate.now());
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de factures en retard", e);
            return List.of();
        }
    }

    // Rechercher par période
    public List<Facture> findByPeriode(LocalDate dateDebut, LocalDate dateFin, Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Facture> query = session.createQuery(
                "FROM Facture f WHERE f.entreprise.id = :entrepriseId " +
                "AND f.dateFacture BETWEEN :dateDebut AND :dateFin " +
                "ORDER BY f.dateFacture DESC", Facture.class);
            query.setParameter("entrepriseId", entrepriseId);
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de factures par période", e);
            return List.of();
        }
    }

    // Recherche textuelle (numéro, objet, commentaires)
    public List<Facture> searchByText(String searchTerm, Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Query<Facture> query = session.createQuery(
                "FROM Facture f WHERE f.entreprise.id = :entrepriseId " +
                "AND (LOWER(f.numeroFacture) LIKE :search " +
                "OR LOWER(f.objet) LIKE :search " +
                "OR LOWER(f.commentaires) LIKE :search) " +
                "ORDER BY f.dateFacture DESC", Facture.class);
            query.setParameter("entrepriseId", entrepriseId);
            query.setParameter("search", searchPattern);
            return query.list();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche textuelle de factures", e);
            return List.of();
        }
    }

    // Calculer le CA (Chiffre d'Affaires) sur une période
    public BigDecimal calculateCAByPeriode(LocalDate dateDebut, LocalDate dateFin, Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BigDecimal> query = session.createQuery(
                "SELECT COALESCE(SUM(f.montantTTC), 0) FROM Facture f " +
                "WHERE f.entreprise.id = :entrepriseId " +
                "AND f.typeFacture = 'VENTE' " +
                "AND f.statut = 'PAYEE' " +
                "AND f.datePaiement BETWEEN :dateDebut AND :dateFin", BigDecimal.class);
            query.setParameter("entrepriseId", entrepriseId);
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Erreur lors du calcul du CA", e);
            return BigDecimal.ZERO;
        }
    }

    // Calculer les achats sur une période
    public BigDecimal calculateAchatsByPeriode(LocalDate dateDebut, LocalDate dateFin, Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BigDecimal> query = session.createQuery(
                "SELECT COALESCE(SUM(f.montantTTC), 0) FROM Facture f " +
                "WHERE f.entreprise.id = :entrepriseId " +
                "AND f.typeFacture = 'ACHAT' " +
                "AND f.statut = 'PAYEE' " +
                "AND f.datePaiement BETWEEN :dateDebut AND :dateFin", BigDecimal.class);
            query.setParameter("entrepriseId", entrepriseId);
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des achats", e);
            return BigDecimal.ZERO;
        }
    }

    // Générer le prochain numéro de facture
    public String generateNextNumeroFacture(Facture.TypeFacture typeFacture, Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String prefix = typeFacture == Facture.TypeFacture.VENTE ? "VTE" : "ACH";
            String year = String.valueOf(LocalDate.now().getYear());

            Query<String> query = session.createQuery(
                "SELECT f.numeroFacture FROM Facture f " +
                "WHERE f.entreprise.id = :entrepriseId " +
                "AND f.numeroFacture LIKE :pattern " +
                "ORDER BY f.numeroFacture DESC", String.class);
            query.setParameter("entrepriseId", entrepriseId);
            query.setParameter("pattern", prefix + year + "%");
            query.setMaxResults(1);

            Optional<String> lastNumber = query.uniqueResultOptional();

            if (lastNumber.isPresent()) {
                String last = lastNumber.get();
                String numberPart = last.substring(prefix.length() + 4); // Enlever prefix + année
                int nextNumber = Integer.parseInt(numberPart) + 1;
                return String.format("%s%s%04d", prefix, year, nextNumber);
            } else {
                return String.format("%s%s0001", prefix, year);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du numéro de facture", e);
            String prefix = typeFacture == Facture.TypeFacture.VENTE ? "VTE" : "ACH";
            String year = String.valueOf(LocalDate.now().getYear());
            return String.format("%s%s0001", prefix, year);
        }
    }

    // Vérifier l'unicité du numéro de facture
    public boolean existsByNumero(String numeroFacture) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(f) FROM Facture f WHERE f.numeroFacture = :numero", Long.class);
            query.setParameter("numero", numeroFacture);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification d'unicité du numéro", e);
            return false;
        }
    }

    // Compter les factures par entreprise
    public long countByEntreprise(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(f) FROM Facture f WHERE f.entreprise.id = :entrepriseId", Long.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des factures", e);
            return 0;
        }
    }
}