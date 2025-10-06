-- Corriger les utilisateurs en s'assurant que les entreprises existent
USE comptabilite_db;

-- Désactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Supprimer et recréer la table utilisateurs
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

-- S'assurer que les entreprises existent
INSERT IGNORE INTO entreprises (id, nom, pays, devise, adresse, telephone, email) VALUES
(1, 'Polyclinique Exemple France', 'France', 'EUR', '123 Rue de la Santé, 75001 Paris', '01.23.45.67.89', 'contact@polyclinique-france.fr'),
(2, 'Polyclinique Exemple Cameroun', 'Cameroun', 'FCFA', 'Avenue Charles de Gaulle, Douala', '+237 6 12 34 56 78', 'contact@polyclinique-cameroun.cm');

-- Voir les entreprises existantes
SELECT id, nom, pays FROM entreprises;

-- Insérer les utilisateurs (d'abord sans entreprise_id)
INSERT INTO utilisateurs (nomUtilisateur, motDePasse, nom, prenom, email, role) VALUES
('admin_france', 'admin', 'Admin', 'France', 'admin.france@comptabilite.com', 'ADMINISTRATEUR'),
('admin_cameroun', 'admin', 'Admin', 'Cameroun', 'admin.cameroun@comptabilite.com', 'ADMINISTRATEUR'),
('comptable_france', 'comptable', 'Comptable', 'France', 'comptable.france@comptabilite.com', 'COMPTABLE'),
('comptable_cameroun', 'comptable', 'Comptable', 'Cameroun', 'comptable.cameroun@comptabilite.com', 'COMPTABLE');

-- Mettre à jour avec les entreprise_id après insertion
UPDATE utilisateurs SET entreprise_id = (SELECT id FROM entreprises WHERE pays = 'France' LIMIT 1)
WHERE nomUtilisateur IN ('admin_france', 'comptable_france');

UPDATE utilisateurs SET entreprise_id = (SELECT id FROM entreprises WHERE pays = 'Cameroun' LIMIT 1)
WHERE nomUtilisateur IN ('admin_cameroun', 'comptable_cameroun');

-- Vérifier le résultat final
SELECT u.id, u.nomUtilisateur, u.nom, u.prenom, u.role, u.entreprise_id, e.nom as entreprise_nom, e.pays
FROM utilisateurs u
LEFT JOIN entreprises e ON u.entreprise_id = e.id;

COMMIT;