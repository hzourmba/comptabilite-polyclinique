-- Vérifier les entreprises existantes
USE comptabilite_db;

-- Voir toutes les entreprises avec leurs IDs
SELECT id, nom, pays, devise FROM entreprises ORDER BY id;

-- Voir la structure de la table entreprises
DESCRIBE entreprises;