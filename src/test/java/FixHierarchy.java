import com.comptabilite.dao.CompteDAO;
import com.comptabilite.model.Compte;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class FixHierarchy {
    public static void main(String[] args) {
        try {
            System.out.println("=== CORRECTION DE LA HIÉRARCHIE ===");

            Long entrepriseId = 1L;

            // 1. Afficher la structure actuelle
            System.out.println("\n1. STRUCTURE ACTUELLE:");
            afficherStructure(entrepriseId);

            // 2. Corriger la hiérarchie
            System.out.println("\n2. CORRECTION DE LA HIÉRARCHIE:");
            corrigerHierarchie(entrepriseId);

            // 3. Afficher la structure corrigée
            System.out.println("\n3. STRUCTURE APRÈS CORRECTION:");
            afficherStructure(entrepriseId);

            // 4. Tester la consolidation
            System.out.println("\n4. TEST DE CONSOLIDATION:");
            testerConsolidation(entrepriseId);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.getSessionFactory().close();
        }
    }

    private static void afficherStructure(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Compte c WHERE c.entreprise.id = :entrepriseId " +
                        "AND c.numeroCompte IN ('CM101000', 'CM101310', 'CM101320') " +
                        "ORDER BY c.numeroCompte";

            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("entrepriseId", entrepriseId);
            List<Compte> comptes = query.list();

            for (Compte compte : comptes) {
                System.out.printf("%-12s %-30s Parent: %s%n",
                    compte.getNumeroCompte(),
                    compte.getLibelle(),
                    compte.getCompteParent() != null ? compte.getCompteParent().getNumeroCompte() : "null"
                );
            }
        }
    }

    private static void corrigerHierarchie(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            // Trouver le compte Capital
            String hqlCapital = "FROM Compte c WHERE c.numeroCompte = 'CM101000' AND c.entreprise.id = :entrepriseId";
            Query<Compte> queryCapital = session.createQuery(hqlCapital, Compte.class);
            queryCapital.setParameter("entrepriseId", entrepriseId);
            Compte capital = queryCapital.uniqueResult();

            if (capital == null) {
                System.out.println("ERREUR: Compte Capital CM101000 non trouvé!");
                return;
            }

            // Trouver les comptes actionnaires
            String hqlActionnaires = "FROM Compte c WHERE c.numeroCompte IN ('CM101310', 'CM101320') AND c.entreprise.id = :entrepriseId";
            Query<Compte> queryActionnaires = session.createQuery(hqlActionnaires, Compte.class);
            queryActionnaires.setParameter("entrepriseId", entrepriseId);
            List<Compte> actionnaires = queryActionnaires.list();

            // Assigner Capital comme parent
            for (Compte actionnaire : actionnaires) {
                actionnaire.setCompteParent(capital);
                session.merge(actionnaire);
                System.out.println("Assigné " + actionnaire.getNumeroCompte() + " comme enfant de " + capital.getNumeroCompte());
            }

            transaction.commit();
            System.out.println("Hiérarchie corrigée avec succès!");
        }
    }

    private static void testerConsolidation(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Compte c LEFT JOIN FETCH c.sousComptes " +
                        "WHERE c.numeroCompte = 'CM101000' AND c.entreprise.id = :entrepriseId";

            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("entrepriseId", entrepriseId);
            Compte capital = query.uniqueResult();

            if (capital != null) {
                System.out.printf("Capital CM101000:%n");
                System.out.printf("  Enfants: %d%n", capital.getSousComptes() != null ? capital.getSousComptes().size() : 0);
                System.out.printf("  Débit consolidé: %s%n", capital.getSoldeDebiteurConsolide());
                System.out.printf("  Crédit consolidé: %s%n", capital.getSoldeCrediteurConsolide());

                if (capital.getSousComptes() != null) {
                    for (Compte enfant : capital.getSousComptes()) {
                        System.out.printf("    - %s: %s/%s%n",
                            enfant.getNumeroCompte(),
                            enfant.getSoldeDebiteur(),
                            enfant.getSoldeCrediteur());
                    }
                }
            }
        }
    }
}