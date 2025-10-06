-- Corriger immédiatement accepte_sous_comptes pour tous les comptes concernés
USE comptabilite_db;

-- Mettre à jour TOUS les comptes qui devraient être des parents
UPDATE compte SET accepte_sous_comptes = TRUE
WHERE entreprise_id = 2
AND numero IN (
    -- Classe 1 existants
    'CM101000', 'CM110000',

    -- Classe 5 existants
    'CM520000', 'CM570000',

    -- Classe 4 (déjà corrects mais pour être sûr)
    'CM400000', 'CM410000', 'CM411000', 'CM401000',

    -- Classe 6 (déjà corrects)
    'CM600000', 'CM610000', 'CM620000', 'CM640000',

    -- Classe 7 (déjà corrects)
    'CM700000', 'CM701000', 'CM706000'
);

-- Ajouter les comptes manquants pour avoir une structure complète

-- === CLASSE 1 - Comptes manquants pour actionnaires ===
INSERT IGNORE INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES
(2, 'CM101300', 'Capital versé', 'PASSIF', 'CLASSE_1', TRUE, 'Capital effectivement versé'),
(2, 'CM101310', 'Actionnaires - Personnes physiques', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par des personnes physiques'),
(2, 'CM101320', 'Actionnaires - Personnes morales', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par des sociétés'),
(2, 'CM101330', 'Actionnaires - Fondateurs', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par les fondateurs'),
(2, 'CM104000', 'Primes et réserves', 'PASSIF', 'CLASSE_1', TRUE, 'Primes d''émission et réserves'),
(2, 'CM120000', 'Résultat de l''exercice', 'PASSIF', 'CLASSE_1', FALSE, 'Résultat de l''exercice en cours');

-- === CLASSE 2 - Immobilisations ===
INSERT IGNORE INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES
(2, 'CM210000', 'Immobilisations corporelles', 'ACTIF', 'CLASSE_2', TRUE, 'Terrains, bâtiments, matériel'),
(2, 'CM215000', 'Matériel médical', 'ACTIF', 'CLASSE_2', TRUE, 'Équipements médicaux'),
(2, 'CM218000', 'Matériel informatique', 'ACTIF', 'CLASSE_2', TRUE, 'Ordinateurs, serveurs');

-- === CLASSE 3 - Stocks ===
INSERT IGNORE INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES
(2, 'CM310000', 'Stocks de médicaments', 'ACTIF', 'CLASSE_3', TRUE, 'Médicaments en stock'),
(2, 'CM320000', 'Stocks de consommables médicaux', 'ACTIF', 'CLASSE_3', TRUE, 'Matériel médical consommable');

-- === CLASSE 5 - Comptes financiers (ajouter des sous-comptes) ===
INSERT IGNORE INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES
(2, 'CM521000', 'Banque principale', 'ACTIF', 'CLASSE_5', TRUE, 'Compte bancaire principal'),
(2, 'CM571000', 'Caisse principale', 'ACTIF', 'CLASSE_5', FALSE, 'Caisse bureau principal'),
(2, 'CM572000', 'Caisse pharmacie', 'ACTIF', 'CLASSE_5', FALSE, 'Caisse pharmacie');

-- === CLASSE 8 - Comptes spéciaux ===
INSERT IGNORE INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES
(2, 'CM801000', 'Engagements hors bilan', 'ACTIF_PASSIF', 'CLASSE_8', TRUE, 'Engagements et garanties');

-- Vérifier le résultat
SELECT 'APRÈS CORRECTION - Comptage par classe:' as info;
SELECT
    classe_compte,
    COUNT(*) as total_comptes,
    SUM(CASE WHEN accepte_sous_comptes = TRUE THEN 1 ELSE 0 END) as comptes_parents
FROM compte
WHERE entreprise_id = 2
GROUP BY classe_compte
ORDER BY classe_compte;

SELECT 'CLASSE_1 avec comptes parents:' as info;
SELECT numero, libelle, accepte_sous_comptes
FROM compte
WHERE entreprise_id = 2 AND classe_compte = 'CLASSE_1' AND accepte_sous_comptes = TRUE
ORDER BY numero;

COMMIT;