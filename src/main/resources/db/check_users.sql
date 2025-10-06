-- Vérifier les utilisateurs existants dans la base de données
USE comptabilite_db;

-- Voir la structure de la table utilisateur
DESCRIBE utilisateur;

-- Voir tous les utilisateurs existants
SELECT * FROM utilisateur;

-- Vérifier s'il y a une colonne nom_utilisateur
SHOW COLUMNS FROM utilisateur LIKE '%utilisateur%';
SHOW COLUMNS FROM utilisateur LIKE '%nom%';
SHOW COLUMNS FROM utilisateur LIKE '%login%';