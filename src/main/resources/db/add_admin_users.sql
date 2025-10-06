-- Script pour ajouter les utilisateurs administrateurs spécifiques
USE comptabilite_db;

-- Supprimer l'utilisateur générique s'il existe
DELETE FROM utilisateur WHERE email = 'admin@comptabilite.com';

-- Ajouter les utilisateurs administrateurs spécifiques
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role) VALUES
('Admin', 'France', 'admin_france', 'admin', 'ADMIN'),
('Admin', 'Cameroun', 'admin_cameroun', 'admin', 'ADMIN'),
('Comptable', 'France', 'comptable_france', 'comptable', 'COMPTABLE'),
('Comptable', 'Cameroun', 'comptable_cameroun', 'comptable', 'COMPTABLE');

-- Vérifier que les utilisateurs ont été créés
SELECT id, nom, prenom, email, role FROM utilisateur;

COMMIT;