-- Vérifier la structure de toutes les tables
USE comptabilite_db;

-- Voir toutes les tables
SHOW TABLES;

-- Voir la structure de chaque table importante
DESCRIBE entreprises;
DESCRIBE compte;

-- Voir les données dans entreprises
SELECT * FROM entreprises LIMIT 5;