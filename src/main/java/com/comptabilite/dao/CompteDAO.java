package com.comptabilite.dao;

import com.comptabilite.model.Compte;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CompteDAO extends BaseDAO<Compte, Long> {

    private static final Logger logger = LoggerFactory.getLogger(CompteDAO.class);

    public CompteDAO() {
        super(Compte.class);
    }

    public Optional<Compte> findByNumeroCompte(String numeroCompte) {
        String hql = "FROM Compte c WHERE c.numeroCompte = :numeroCompte";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("numeroCompte", numeroCompte);
            return Optional.ofNullable(query.uniqueResult());
        }
    }

    public Optional<Compte> findByNumeroCompteWithRefresh(String numeroCompte) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createNativeQuery("SELECT 1").uniqueResult();
            Query<Compte> query = session.createQuery("FROM Compte c WHERE c.numeroCompte = :numeroCompte", Compte.class);
            query.setParameter("numeroCompte", numeroCompte);
            Optional<Compte> result = Optional.ofNullable(query.uniqueResult());
            session.getTransaction().commit();
            return result;
        }
    }

    public Optional<Compte> findByNumeroCompteAndEntreprise(String numeroCompte, Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Compte> query = session.createQuery("FROM Compte c WHERE c.numeroCompte = :numeroCompte AND c.entreprise.id = :entrepriseId", Compte.class);
            query.setParameter("numeroCompte", numeroCompte);
            query.setParameter("entrepriseId", entrepriseId);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de compte {} pour entreprise {}", numeroCompte, entrepriseId, e);
            return Optional.empty();
        }
    }

    public List<Compte> findByEntreprise(Long entrepriseId) {
        String hql = "FROM Compte c LEFT JOIN FETCH c.sousComptes WHERE c.entreprise.id = :entrepriseId ORDER BY c.numeroCompte";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        }
    }

    public List<Compte> findByTypeCompte(Compte.TypeCompte typeCompte, Long entrepriseId) {
        String hql = "FROM Compte c WHERE c.typeCompte = :typeCompte AND c.entreprise.id = :entrepriseId ORDER BY c.numeroCompte";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("typeCompte", typeCompte);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        }
    }

    public List<Compte> findByClasseCompte(Compte.ClasseCompte classeCompte, Long entrepriseId) {
        String hql = "FROM Compte c WHERE c.classeCompte = :classeCompte AND c.entreprise.id = :entrepriseId ORDER BY c.numeroCompte";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("classeCompte", classeCompte);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        }
    }

    public List<Compte> findComptesActifs(Long entrepriseId) {
        String hql = "FROM Compte c WHERE c.actif = true AND c.entreprise.id = :entrepriseId ORDER BY c.numeroCompte";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        }
    }

    public List<Compte> findComptesPrincipaux(Long entrepriseId) {
        String hql = "FROM Compte c WHERE c.compteParent IS NULL AND c.entreprise.id = :entrepriseId ORDER BY c.numeroCompte";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        }
    }

    public List<Compte> findSousComptes(Long compteParentId) {
        String hql = "FROM Compte c WHERE c.compteParent.id = :compteParentId ORDER BY c.numeroCompte";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("compteParentId", compteParentId);
            return query.list();
        }
    }

    public List<Compte> searchByLibelle(String libelle, Long entrepriseId) {
        String hql = "FROM Compte c WHERE LOWER(c.libelle) LIKE LOWER(:libelle) AND c.entreprise.id = :entrepriseId ORDER BY c.numeroCompte";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("libelle", "%" + libelle + "%");
            query.setParameter("entrepriseId", entrepriseId);
            return query.list();
        }
    }

    public boolean existsByNumeroCompte(String numeroCompte) {
        return findByNumeroCompte(numeroCompte).isPresent();
    }

    /**
     * Trouve un compte par son numéro dans une entreprise spécifique
     */
    public Compte getByNumero(String numeroCompte, Long entrepriseId) {
        return findByNumeroCompteAndEntreprise(numeroCompte, entrepriseId).orElse(null);
    }

    /**
     * Récupère les comptes parents (acceptant des sous-comptes) pour une entreprise
     */
    public List<Compte> getComptesParents(Long entrepriseId) {
        String hql = "FROM Compte c WHERE c.accepteSousComptes = true AND c.entreprise.id = :entrepriseId ORDER BY c.numeroCompte";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            logger.info("=== DEBUG DAO: Recherche comptes parents pour entreprise ID: {} ===", entrepriseId);
            logger.info("HQL: {}", hql);

            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("entrepriseId", entrepriseId);
            List<Compte> results = query.list();

            logger.info("Résultats trouvés: {} comptes", results.size());
            for (Compte compte : results) {
                logger.info("  - {} : {} (accepte: {})", compte.getNumero(), compte.getLibelle(), compte.isAccepteSousComptes());
            }

            return results;
        } catch (Exception e) {
            logger.error("Erreur dans getComptesParents pour entreprise {}", entrepriseId, e);
            return List.of();
        }
    }

    /**
     * Génère le prochain numéro de compte dans une classe donnée
     */
    public String getNextNumeroInClasse(String prefix, Long entrepriseId) {
        String hql = "SELECT c.numeroCompte FROM Compte c WHERE c.numeroCompte LIKE :prefix AND c.entreprise.id = :entrepriseId ORDER BY c.numeroCompte DESC";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<String> query = session.createQuery(hql, String.class);
            query.setParameter("prefix", prefix + "%");
            query.setParameter("entrepriseId", entrepriseId);
            query.setMaxResults(1);

            List<String> results = query.list();
            if (results.isEmpty()) {
                // Premier compte de cette classe
                return prefix + "000";
            }

            String lastNumber = results.get(0);
            // Extraire la partie numérique après le préfixe
            String numericPart = lastNumber.substring(prefix.length());
            try {
                int nextNum = Integer.parseInt(numericPart) + 1;
                return prefix + String.format("%03d", nextNum);
            } catch (NumberFormatException e) {
                // Si on ne peut pas parser, retourner un numéro par défaut
                return prefix + "000";
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du numéro de compte", e);
            return prefix + "000";
        }
    }

    /**
     * Génère le prochain numéro de compte dans une hiérarchie donnée (sous-compte d'un parent)
     */
    public String getNextNumeroInHierarchy(String parentPrefix, Long entrepriseId) {
        logger.info("=== HIERARCHY DEBUG START ===");
        logger.info("Recherche pour parent prefix: '{}', entreprise: {}", parentPrefix, entrepriseId);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            // Nouvelle approche: chercher directement les sous-comptes par parent_id
            String sqlByParentId = "SELECT MAX(numero) FROM compte WHERE parent_id = (SELECT id FROM compte WHERE numero = ? AND entreprise_id = ?)";

            @SuppressWarnings("unchecked")
            String maxNumber = (String) session.createNativeQuery(sqlByParentId)
                    .setParameter(1, parentPrefix)
                    .setParameter(2, entrepriseId)
                    .uniqueResult();

            session.getTransaction().commit();

            logger.info("Parent prefix: '{}'", parentPrefix);
            logger.info("Numéro maximum trouvé: {}", maxNumber);

            if (maxNumber == null) {
                // Aucun sous-compte direct trouvé, créer le premier
                String firstChild = incrementLastDigit(parentPrefix);
                logger.info("Aucun sous-compte direct trouvé, création du premier: {}", firstChild);
                return firstChild;
            } else {
                // Incrémenter le dernier numéro trouvé
                String nextChild = incrementLastDigit(maxNumber);
                logger.info("Dernier sous-compte trouvé: {}, suivant: {}", maxNumber, nextChild);
                return nextChild;
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du numéro hiérarchique", e);
            return incrementLastDigit(parentPrefix);
        }
    }

    /**
     * Incrémente le dernier chiffre d'un numéro de compte
     */
    private String incrementLastDigit(String numero) {
        if (numero.isEmpty()) {
            return "1";
        }

        // Extraire le dernier chiffre et l'incrémenter
        char lastChar = numero.charAt(numero.length() - 1);
        if (Character.isDigit(lastChar)) {
            int lastDigit = Character.getNumericValue(lastChar);
            if (lastDigit < 9) {
                return numero.substring(0, numero.length() - 1) + (lastDigit + 1);
            } else {
                // Si le dernier chiffre est 9, il faut gérer le report
                // Pour simplifier, on ajoute un chiffre
                return numero + "0";
            }
        } else {
            // Si le dernier caractère n'est pas un chiffre, ajouter 1
            return numero + "1";
        }
    }


    /**
     * Sauvegarde ou met à jour un compte avec gestion des soldes consolidés
     */
    @Override
    public Compte save(Compte compte) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Compte savedCompte;
                if (compte.getId() == null) {
                    session.persist(compte);
                    savedCompte = compte;
                    logger.info("Nouveau compte créé: {}", compte.getNumeroCompte());
                } else {
                    savedCompte = (Compte) session.merge(compte);
                    logger.info("Compte mis à jour: {}", compte.getNumeroCompte());
                }

                // Si c'est un sous-compte avec des soldes, notifier le parent
                if (savedCompte.getCompteParent() != null &&
                    (savedCompte.getSoldeDebiteur().compareTo(BigDecimal.ZERO) != 0 ||
                     savedCompte.getSoldeCrediteur().compareTo(BigDecimal.ZERO) != 0)) {
                    // Mettre à jour et sauvegarder toute la hiérarchie des parents
                    mettreAJourEtSauvegarderParents(session, savedCompte.getCompteParent());
                    logger.info("Mise à jour et sauvegarde des soldes parents pour: {}", savedCompte.getNumeroCompte());
                }

                transaction.commit();
                return savedCompte;
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde du compte {}", compte.getNumeroCompte(), e);
            throw new RuntimeException("Erreur de sauvegarde", e);
        }
    }

    /**
     * Met à jour récursivement les soldes consolidés des comptes parents et les sauvegarde
     */
    private void mettreAJourEtSauvegarderParents(Session session, Compte parent) {
        if (parent != null) {
            // Calculer et sauvegarder les nouveaux soldes consolidés
            // Les soldes consolidés sont calculés dynamiquement - pas besoin de les sauvegarder
            // parent.calculerEtSauvegarderSoldesConsolides();
            // session.merge(parent);
            logger.info("Compte parent trouvé: {} (soldes consolidés calculés dynamiquement)",
                parent.getNumeroCompte());

            // Remonter récursivement vers le parent supérieur
            if (parent.getCompteParent() != null) {
                mettreAJourEtSauvegarderParents(session, parent.getCompteParent());
            }
        }
    }

    /**
     * Recalcule tous les soldes consolidés pour une entreprise donnée
     * Cette méthode doit être appelée après des modifications importantes
     */
    public void recalculerTousLesSoldesConsolides(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                // Récupérer tous les comptes qui ont des sous-comptes (indépendamment du flag accepteSousComptes)
                String hql = "FROM Compte c LEFT JOIN FETCH c.sousComptes " +
                           "WHERE c.entreprise.id = :entrepriseId " +
                           "AND EXISTS (SELECT 1 FROM Compte sc WHERE sc.compteParent = c) " +
                           "ORDER BY LENGTH(c.numeroCompte) ASC, c.numeroCompte ASC";

                Query<Compte> query = session.createQuery(hql, Compte.class);
                query.setParameter("entrepriseId", entrepriseId);
                List<Compte> comptesParents = query.list();

                logger.info("Recalcul des soldes consolidés pour {} comptes parents", comptesParents.size());

                // Recalculer les soldes de bas en haut (du plus profond vers la racine)
                // D'abord traiter les comptes les plus profonds (plus long numéro)
                comptesParents.sort((c1, c2) -> Integer.compare(c2.getNumeroCompte().length(), c1.getNumeroCompte().length()));

                for (Compte compte : comptesParents) {
                    // Consolidation désactivée - calcul dynamique utilisé
                    // compte.calculerEtSauvegarderSoldesConsolides();
                    // session.merge(compte);
                    logger.info("Compte parent: {} (consolidation dynamique)",
                        compte.getNumeroCompte());
                }

                transaction.commit();
                logger.info("Recalcul des soldes consolidés terminé");
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            logger.error("Erreur lors du recalcul des soldes consolidés pour l'entreprise {}", entrepriseId, e);
            throw new RuntimeException("Erreur de recalcul", e);
        }
    }

    /**
     * Met à jour uniquement les soldes d'un compte
     */
    public void updateSoldes(Compte compte, BigDecimal nouveauSoldeDebiteur, BigDecimal nouveauSoldeCrediteur) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Compte compteToUpdate = session.get(Compte.class, compte.getId());
                if (compteToUpdate != null) {
                    compteToUpdate.setSoldeDebiteur(nouveauSoldeDebiteur);
                    compteToUpdate.setSoldeCrediteur(nouveauSoldeCrediteur);
                    session.merge(compteToUpdate);

                    // Notifier le parent que les soldes ont changé
                    compteToUpdate.mettreAJourSoldesParent();

                    logger.info("Soldes mis à jour pour {}: Débit={}, Crédit={}",
                        compteToUpdate.getNumeroCompte(), nouveauSoldeDebiteur, nouveauSoldeCrediteur);
                }
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des soldes pour {}", compte.getNumeroCompte(), e);
            throw new RuntimeException("Erreur de mise à jour des soldes", e);
        }
    }

    /**
     * Vérifie si un compte peut être supprimé (pas d'écritures associées)
     */
    public boolean canDelete(Long compteId) {
        String hql = "SELECT COUNT(le) FROM LigneEcriture le WHERE le.compte.id = :compteId";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("compteId", compteId);
            Long count = query.uniqueResult();
            return count == 0;
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de suppression du compte", e);
            return false;
        }
    }
}