import java.sql.*;

public class DebugComptes {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/comptabilite_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull";
        String username = "root";
        String password = "password";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            System.out.println("=== STRUCTURE HIERARCHIQUE DES COMPTES ===");

            String sql = "SELECT c.id, c.numero_compte, c.libelle, c.accepte_sous_comptes, " +
                        "c.solde_debiteur, c.solde_crediteur, c.parent_id, " +
                        "p.numero_compte as parent_numero, p.libelle as parent_libelle " +
                        "FROM compte c " +
                        "LEFT JOIN compte p ON c.parent_id = p.id " +
                        "WHERE c.entreprise_id = 1 " +
                        "AND (c.numero_compte LIKE 'CM101%' OR c.numero_compte = 'CM1' OR c.numero_compte LIKE 'CM10%') " +
                        "ORDER BY c.numero_compte";

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    System.out.printf("ID: %d | Numéro: %-12s | Libellé: %-30s | Accepte SS: %-5s | Débit: %10.2f | Crédit: %10.2f | Parent: %s (%s)%n",
                        rs.getLong("id"),
                        rs.getString("numero_compte"),
                        rs.getString("libelle"),
                        rs.getBoolean("accepte_sous_comptes") ? "OUI" : "NON",
                        rs.getBigDecimal("solde_debiteur").doubleValue(),
                        rs.getBigDecimal("solde_crediteur").doubleValue(),
                        rs.getString("parent_numero") != null ? rs.getString("parent_numero") : "NULL",
                        rs.getString("parent_libelle") != null ? rs.getString("parent_libelle") : "NULL"
                    );
                }
            }

            System.out.println("\n=== COMPTES AVEC SOLDES NON ZERO ===");
            String sql2 = "SELECT numero_compte, libelle, solde_debiteur, solde_crediteur " +
                         "FROM compte WHERE entreprise_id = 1 " +
                         "AND (solde_debiteur > 0 OR solde_crediteur > 0) " +
                         "ORDER BY numero_compte";

            try (PreparedStatement stmt = conn.prepareStatement(sql2);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    System.out.printf("%-12s | %-30s | Débit: %10.2f | Crédit: %10.2f%n",
                        rs.getString("numero_compte"),
                        rs.getString("libelle"),
                        rs.getBigDecimal("solde_debiteur").doubleValue(),
                        rs.getBigDecimal("solde_crediteur").doubleValue()
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}