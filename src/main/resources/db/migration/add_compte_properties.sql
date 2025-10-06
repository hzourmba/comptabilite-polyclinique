-- Migration pour ajouter les nouvelles propriétés aux comptes
-- Date: 2024-09-28
-- Description: Ajoute les colonnes pour accepte_sous_comptes, lettrable, auxiliaire et description

-- Ajouter les nouvelles colonnes si elles n'existent pas déjà
ALTER TABLE comptes
ADD COLUMN IF NOT EXISTS accepte_sous_comptes BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS lettrable BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS auxiliaire BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS description TEXT NULL;

-- Mettre à jour les comptes principaux pour accepter des sous-comptes
UPDATE comptes
SET accepte_sous_comptes = TRUE
WHERE numeroCompte IN (
    -- Comptes de classe 1 à 5 principaux pouvant avoir des sous-comptes
    '101', '102', '103', '104', '105', '106', '107', '108', '109',
    '201', '202', '203', '204', '205', '206', '207', '208', '209',
    '301', '302', '303', '304', '305', '306', '307', '308', '309',
    '401', '402', '403', '404', '405', '406', '407', '408', '409',
    '501', '502', '503', '504', '505', '506', '507', '508', '509',
    -- Comptes OHADA équivalents
    'CM101', 'CM102', 'CM103', 'CM104', 'CM105', 'CM106', 'CM107', 'CM108', 'CM109',
    'CM201', 'CM202', 'CM203', 'CM204', 'CM205', 'CM206', 'CM207', 'CM208', 'CM209',
    'CM301', 'CM302', 'CM303', 'CM304', 'CM305', 'CM306', 'CM307', 'CM308', 'CM309',
    'CM401', 'CM402', 'CM403', 'CM404', 'CM405', 'CM406', 'CM407', 'CM408', 'CM409',
    'CM501', 'CM502', 'CM503', 'CM504', 'CM505', 'CM506', 'CM507', 'CM508', 'CM509'
);

-- Mettre à jour certains comptes pour être lettrables (comptes de tiers principalement)
UPDATE comptes
SET lettrable = TRUE
WHERE numeroCompte LIKE '4%' OR numeroCompte LIKE 'CM4%';

-- Mettre à jour certains comptes comme auxiliaires (sous-comptes de tiers)
UPDATE comptes
SET auxiliaire = TRUE
WHERE LENGTH(numeroCompte) > 3 AND (numeroCompte LIKE '4%' OR numeroCompte LIKE 'CM4%');

-- Ajouter des descriptions par défaut pour les classes principales
UPDATE comptes SET description = 'Comptes de capitaux, réserves et résultats' WHERE numeroCompte LIKE '1%' OR numeroCompte LIKE 'CM1%';
UPDATE comptes SET description = 'Comptes d''immobilisations corporelles et incorporelles' WHERE numeroCompte LIKE '2%' OR numeroCompte LIKE 'CM2%';
UPDATE comptes SET description = 'Comptes de stocks et en-cours de production' WHERE numeroCompte LIKE '3%' OR numeroCompte LIKE 'CM3%';
UPDATE comptes SET description = 'Comptes de tiers (clients, fournisseurs, etc.)' WHERE numeroCompte LIKE '4%' OR numeroCompte LIKE 'CM4%';
UPDATE comptes SET description = 'Comptes financiers (banques, caisse, etc.)' WHERE numeroCompte LIKE '5%' OR numeroCompte LIKE 'CM5%';
UPDATE comptes SET description = 'Comptes de charges d''exploitation' WHERE numeroCompte LIKE '6%' OR numeroCompte LIKE 'CM6%';
UPDATE comptes SET description = 'Comptes de produits d''exploitation' WHERE numeroCompte LIKE '7%' OR numeroCompte LIKE 'CM7%';

-- Vérification finale
SELECT
    COUNT(*) as total_comptes,
    SUM(CASE WHEN accepte_sous_comptes THEN 1 ELSE 0 END) as comptes_avec_sous_comptes,
    SUM(CASE WHEN lettrable THEN 1 ELSE 0 END) as comptes_lettrables,
    SUM(CASE WHEN auxiliaire THEN 1 ELSE 0 END) as comptes_auxiliaires,
    SUM(CASE WHEN description IS NOT NULL THEN 1 ELSE 0 END) as comptes_avec_description
FROM comptes;