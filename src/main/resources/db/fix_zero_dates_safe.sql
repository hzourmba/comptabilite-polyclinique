-- Corriger les dates zéro de manière sécurisée
USE comptabilite_db;

-- Temporairement désactiver le mode strict
SET sql_mode = '';

-- Corriger les dates nulles ou zéro dans la table compte
UPDATE compte
SET date_creation = CURRENT_TIMESTAMP
WHERE date_creation = '0000-00-00 00:00:00' OR date_creation IS NULL;

UPDATE compte
SET date_modification = CURRENT_TIMESTAMP
WHERE date_modification = '0000-00-00 00:00:00' OR date_modification IS NULL;

-- Corriger la table utilisateurs
UPDATE utilisateurs
SET dateCreation = CURRENT_TIMESTAMP
WHERE dateCreation = '0000-00-00 00:00:00' OR dateCreation IS NULL;

-- Pour derniereConnexion, mettre NULL au lieu d'une date zéro
UPDATE utilisateurs
SET derniereConnexion = NULL
WHERE derniereConnexion = '0000-00-00 00:00:00';

-- Corriger la table entreprises
UPDATE entreprises
SET date_creation = CURRENT_TIMESTAMP
WHERE date_creation = '0000-00-00 00:00:00' OR date_creation IS NULL;

-- Remettre le mode strict si nécessaire (optionnel)
-- SET sql_mode = 'TRADITIONAL';

-- Vérifier que les corrections ont fonctionné
SELECT 'Nombre de comptes avec dates valides:' as info;
SELECT COUNT(*) as nb_comptes FROM compte;

SELECT 'Nombre d''utilisateurs avec dates valides:' as info;
SELECT COUNT(*) as nb_utilisateurs FROM utilisateurs;

SELECT 'Exemple de comptes après correction:' as info;
SELECT numero, libelle, date_creation FROM compte LIMIT 3;

COMMIT;