-- Mettre à jour les utilisateurs existants
USE comptabilite_db;

-- Vider la table utilisateurs existante
DELETE FROM utilisateurs;

-- Insérer les utilisateurs avec la bonne structure
INSERT INTO utilisateurs (nom_utilisateur, mot_de_passe, nom, prenom, email, role, entreprise_id) VALUES
('admin_france', 'admin', 'Admin', 'France', 'admin.france@comptabilite.com', 'ADMINISTRATEUR', 1),
('admin_cameroun', 'admin', 'Admin', 'Cameroun', 'admin.cameroun@comptabilite.com', 'ADMINISTRATEUR', 2),
('comptable_france', 'comptable', 'Comptable', 'France', 'comptable.france@comptabilite.com', 'COMPTABLE', 1),
('comptable_cameroun', 'comptable', 'Comptable', 'Cameroun', 'comptable.cameroun@comptabilite.com', 'COMPTABLE', 2);

-- Vérifier les utilisateurs créés
SELECT id, nom_utilisateur, nom, prenom, role, entreprise_id FROM utilisateurs;

COMMIT;