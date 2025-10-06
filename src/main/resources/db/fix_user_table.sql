-- Corriger le problème de la table utilisateur
USE comptabilite_db;

-- Supprimer l'ancienne table utilisateur
DROP TABLE IF EXISTS utilisateur;

-- Créer la table utilisateurs (avec 's') pour correspondre au modèle
CREATE TABLE utilisateurs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_utilisateur VARCHAR(50) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    role ENUM('ADMINISTRATEUR', 'COMPTABLE', 'ASSISTANT_COMPTABLE', 'CONSULTANT') NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    derniere_connexion TIMESTAMP NULL,
    entreprise_id BIGINT,
    FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE SET NULL
);

-- Insérer les utilisateurs avec la bonne structure et les bons noms de rôles
INSERT INTO utilisateurs (nom_utilisateur, mot_de_passe, nom, prenom, email, role, entreprise_id) VALUES
('admin_france', 'admin', 'Admin', 'France', 'admin.france@comptabilite.com', 'ADMINISTRATEUR', 1),
('admin_cameroun', 'admin', 'Admin', 'Cameroun', 'admin.cameroun@comptabilite.com', 'ADMINISTRATEUR', 2),
('comptable_france', 'comptable', 'Comptable', 'France', 'comptable.france@comptabilite.com', 'COMPTABLE', 1),
('comptable_cameroun', 'comptable', 'Comptable', 'Cameroun', 'comptable.cameroun@comptabilite.com', 'COMPTABLE', 2);

-- Vérifier les utilisateurs créés
SELECT id, nom_utilisateur, nom, prenom, role, entreprise_id FROM utilisateurs;

COMMIT;