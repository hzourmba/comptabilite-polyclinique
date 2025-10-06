-- Corriger seulement la table utilisateurs
USE comptabilite_db;

-- Désactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Supprimer et recréer la table utilisateurs avec les EXACTS noms de colonnes du modèle Java
DROP TABLE IF EXISTS utilisateurs;

CREATE TABLE utilisateurs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nomUtilisateur VARCHAR(50) UNIQUE NOT NULL,
    motDePasse VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    role ENUM('ADMINISTRATEUR', 'COMPTABLE', 'ASSISTANT_COMPTABLE', 'CONSULTANT') NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    dateCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    derniereConnexion TIMESTAMP NULL,
    entreprise_id BIGINT,
    FOREIGN KEY (entreprise_id) REFERENCES entreprises(id) ON DELETE SET NULL
);

-- Réactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;

-- Insérer les utilisateurs
INSERT INTO utilisateurs (nomUtilisateur, motDePasse, nom, prenom, email, role, entreprise_id) VALUES
('admin_france', 'admin', 'Admin', 'France', 'admin.france@comptabilite.com', 'ADMINISTRATEUR', 1),
('admin_cameroun', 'admin', 'Admin', 'Cameroun', 'admin.cameroun@comptabilite.com', 'ADMINISTRATEUR', 2),
('comptable_france', 'comptable', 'Comptable', 'France', 'comptable.france@comptabilite.com', 'COMPTABLE', 1),
('comptable_cameroun', 'comptable', 'Comptable', 'Cameroun', 'comptable.cameroun@comptabilite.com', 'COMPTABLE', 2);

-- Vérifier la structure finale
DESCRIBE utilisateurs;
SELECT id, nomUtilisateur, nom, prenom, role, entreprise_id FROM utilisateurs;

COMMIT;