-- Corriger les problèmes de dates nulles et zéro
USE comptabilite_db;

-- Vérifier les dates problématiques
SELECT 'Comptes avec dates problématiques:' as info;
SELECT id, numero, libelle, date_creation, date_modification
FROM compte
WHERE date_creation IS NULL OR date_creation = '0000-00-00 00:00:00'
   OR date_modification IS NULL OR date_modification = '0000-00-00 00:00:00';

-- Corriger les dates nulles ou zéro dans la table compte
UPDATE compte
SET date_creation = CURRENT_TIMESTAMP
WHERE date_creation IS NULL OR date_creation = '0000-00-00 00:00:00';

UPDATE compte
SET date_modification = CURRENT_TIMESTAMP
WHERE date_modification IS NULL OR date_modification = '0000-00-00 00:00:00';

-- Vérifier les autres tables
SELECT 'Utilisateurs avec dates problématiques:' as info;
SELECT id, nomUtilisateur, dateCreation, derniereConnexion
FROM utilisateurs
WHERE dateCreation IS NULL OR dateCreation = '0000-00-00 00:00:00'
   OR derniereConnexion = '0000-00-00 00:00:00';

-- Corriger les dates dans utilisateurs
UPDATE utilisateurs
SET dateCreation = CURRENT_TIMESTAMP
WHERE dateCreation IS NULL OR dateCreation = '0000-00-00 00:00:00';

UPDATE utilisateurs
SET derniereConnexion = NULL
WHERE derniereConnexion = '0000-00-00 00:00:00';

-- Vérifier les entreprises
SELECT 'Entreprises avec dates problématiques:' as info;
SELECT id, raisonSociale, date_creation
FROM entreprises
WHERE date_creation IS NULL OR date_creation = '0000-00-00 00:00:00';

-- Corriger les dates dans entreprises
UPDATE entreprises
SET date_creation = CURRENT_TIMESTAMP
WHERE date_creation IS NULL OR date_creation = '0000-00-00 00:00:00';

-- Afficher un résumé après correction
SELECT 'Vérification après correction - Comptes:' as info;
SELECT COUNT(*) as nb_comptes_ok FROM compte WHERE date_creation IS NOT NULL AND date_creation != '0000-00-00 00:00:00';

SELECT 'Vérification après correction - Utilisateurs:' as info;
SELECT COUNT(*) as nb_utilisateurs_ok FROM utilisateurs WHERE dateCreation IS NOT NULL AND dateCreation != '0000-00-00 00:00:00';

COMMIT;