import com.comptabilite.dao.CompteDAO;
import com.comptabilite.model.Compte;
import com.comptabilite.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;

public class DebugAndResetBalances {
    public static void main(String[] args) {
        try {
            System.out.println("=== DEBUG ET RESET DES BALANCES ===");

            CompteDAO compteDAO = new CompteDAO();
            Long entrepriseId = 1L;

            // 1. Afficher la structure actuelle
            System.out.println("\n1. STRUCTURE ACTUELLE DES COMPTES CM101:");
            afficherComptesStructure(entrepriseId);

            // 2. Reset des comptes parents à zéro
            System.out.println("\n2. RESET DES COMPTES PARENTS:");
            resetComptesParents(entrepriseId);

            // 3. Afficher la structure après reset
            System.out.println("\n3. STRUCTURE APRÈS RESET:");
            afficherComptesStructure(entrepriseId);

            // 4. Tester la consolidation dynamique
            System.out.println("\n4. TEST CONSOLIDATION DYNAMIQUE:");
            testerConsolidationDynamique(entrepriseId);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.getSessionFactory().close();
        }
    }

    private static void afficherComptesStructure(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Compte c LEFT JOIN FETCH c.sousComptes " +
                        "WHERE c.entreprise.id = :entrepriseId " +
                        "AND c.numeroCompte LIKE 'CM101%' " +
                        "ORDER BY c.numeroCompte";

            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("entrepriseId", entrepriseId);
            List<Compte> comptes = query.list();

            for (Compte compte : comptes) {
                System.out.printf("%-12s %-30s Débit: %12s Crédit: %12s Parent: %s Enfants: %d%n",
                    compte.getNumeroCompte(),
                    compte.getLibelle(),
                    compte.getSoldeDebiteur(),
                    compte.getSoldeCrediteur(),
                    compte.getCompteParent() != null ? compte.getCompteParent().getNumeroCompte() : "null",
                    compte.getSousComptes() != null ? compte.getSousComptes().size() : 0
                );
            }
        }
    }

    private static void resetComptesParents(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            // Reset Capital (CM101000)
            String hql1 = "UPDATE Compte SET soldeDebiteur = :zero, soldeCrediteur = :zero " +
                         "WHERE numeroCompte = 'CM101000' AND entreprise.id = :entrepriseId";
            Query query1 = session.createQuery(hql1);
            query1.setParameter("zero", BigDecimal.ZERO);
            query1.setParameter("entrepriseId", entrepriseId);
            int updated1 = query1.executeUpdate();
            System.out.println("Reset CM101000 Capital: " + updated1 + " compte(s)");

            // Reset comptes qui ont des enfants (parents logiques)
            String hql2 = "UPDATE Compte SET soldeDebiteur = :zero, soldeCrediteur = :zero " +
                         "WHERE entreprise.id = :entrepriseId " +
                         "AND numeroCompte IN ('CM101310', 'CM101320') " +
                         "AND EXISTS (SELECT 1 FROM Compte c2 WHERE c2.compteParent.id = id)";
            Query query2 = session.createQuery(hql2);
            query2.setParameter("zero", BigDecimal.ZERO);
            query2.setParameter("entrepriseId", entrepriseId);
            int updated2 = query2.executeUpdate();
            System.out.println("Reset comptes parents logiques: " + updated2 + " compte(s)");

            transaction.commit();
        }
    }

    private static void testerConsolidationDynamique(Long entrepriseId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Compte c LEFT JOIN FETCH c.sousComptes " +
                        "WHERE c.entreprise.id = :entrepriseId " +
                        "AND c.numeroCompte IN ('CM101000', 'CM101310', 'CM101320') " +
                        "ORDER BY c.numeroCompte";

            Query<Compte> query = session.createQuery(hql, Compte.class);
            query.setParameter("entrepriseId", entrepriseId);
            List<Compte> comptes = query.list();

            for (Compte compte : comptes) {
                BigDecimal debitConsolide = compte.getSoldeDebiteurConsolide();
                BigDecimal creditConsolide = compte.getSoldeCrediteurConsolide();

                System.out.printf("%-12s: Stocké: %s/%s, Consolidé: %s/%s%n",
                    compte.getNumeroCompte(),
                    compte.getSoldeDebiteur(),
                    compte.getSoldeCrediteur(),
                    debitConsolide,
                    creditConsolide
                );
            }
        }
    }
}