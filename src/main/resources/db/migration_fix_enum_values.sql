-- Migration script to fix ClasseCompte enum values
-- This script updates the old enum values to the new simplified format

UPDATE compte SET classe_compte = 'CLASSE_1' WHERE classe_compte = 'CLASSE_1_COMPTES_CAPITAUX';
UPDATE compte SET classe_compte = 'CLASSE_2' WHERE classe_compte = 'CLASSE_2_COMPTES_IMMOBILISATIONS';
UPDATE compte SET classe_compte = 'CLASSE_3' WHERE classe_compte = 'CLASSE_3_COMPTES_STOCKS';
UPDATE compte SET classe_compte = 'CLASSE_4' WHERE classe_compte = 'CLASSE_4_COMPTES_TIERS';
UPDATE compte SET classe_compte = 'CLASSE_5' WHERE classe_compte = 'CLASSE_5_COMPTES_FINANCIERS';
UPDATE compte SET classe_compte = 'CLASSE_6' WHERE classe_compte = 'CLASSE_6_COMPTES_CHARGES';
UPDATE compte SET classe_compte = 'CLASSE_7' WHERE classe_compte = 'CLASSE_7_COMPTES_PRODUITS';
UPDATE compte SET classe_compte = 'CLASSE_8' WHERE classe_compte = 'CLASSE_8_COMPTES_SPECIAUX';

-- Add the new columns if they don't exist
ALTER TABLE compte
ADD COLUMN IF NOT EXISTS accepte_sous_comptes BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS lettrable BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS auxiliaire BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS description TEXT;

-- Update some default values for common accounts
UPDATE compte SET accepte_sous_comptes = TRUE WHERE numero LIKE '4%' AND LENGTH(numero) <= 3;
UPDATE compte SET lettrable = TRUE WHERE numero LIKE '411%' OR numero LIKE '401%';
UPDATE compte SET auxiliaire = TRUE WHERE numero LIKE '411%' OR numero LIKE '401%';