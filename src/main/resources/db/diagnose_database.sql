-- Diagnostic script to check database state
USE comptabilite_db;

-- Check if tables exist
SHOW TABLES;

-- Check table structure for compte
DESCRIBE compte;

-- Check actual data in compte table
SELECT id, numero, libelle, classe_compte, entreprise_id FROM compte LIMIT 10;

-- Check all unique classe_compte values in the database
SELECT DISTINCT classe_compte FROM compte;

-- Check entreprise table
SELECT id, nom, pays, devise FROM entreprise;