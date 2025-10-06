-- Créer un plan comptable complet pour toutes les classes avec des comptes parents
USE comptabilite_db;

-- Supprimer les anciens comptes pour repartir proprement (sauf classe 4, 6, 7 qui fonctionnent)
DELETE FROM compte WHERE entreprise_id = 2 AND classe_compte IN ('CLASSE_1', 'CLASSE_2', 'CLASSE_3', 'CLASSE_5', 'CLASSE_8');

-- === CLASSE 1 - COMPTES DE CAPITAUX ===
INSERT INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES
-- Comptes principaux (parents)
(2, 'CM101000', 'Capital social', 'PASSIF', 'CLASSE_1', TRUE, 'Capital social de la société'),
(2, 'CM104000', 'Primes et réserves', 'PASSIF', 'CLASSE_1', TRUE, 'Primes d''émission et réserves'),
(2, 'CM110000', 'Report à nouveau', 'PASSIF', 'CLASSE_1', FALSE, 'Bénéfices non distribués'),
(2, 'CM120000', 'Résultat de l''exercice', 'PASSIF', 'CLASSE_1', FALSE, 'Résultat de l''exercice en cours'),

-- Sous-comptes détaillés pour le capital (parents pour actionnaires individuels)
(2, 'CM101100', 'Capital souscrit', 'PASSIF', 'CLASSE_1', TRUE, 'Capital souscrit par les actionnaires'),
(2, 'CM101200', 'Capital appelé', 'PASSIF', 'CLASSE_1', TRUE, 'Capital appelé par la société'),
(2, 'CM101300', 'Capital versé', 'PASSIF', 'CLASSE_1', TRUE, 'Capital effectivement versé'),

-- Comptes individuels d'actionnaires
(2, 'CM101310', 'Actionnaires - Personnes physiques', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par des personnes physiques'),
(2, 'CM101320', 'Actionnaires - Personnes morales', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par des sociétés'),
(2, 'CM101330', 'Actionnaires - Fondateurs', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par les fondateurs'),
(2, 'CM101340', 'Actionnaires - État', 'PASSIF', 'CLASSE_1', TRUE, 'Participation de l''État'),

-- Primes et réserves détaillées
(2, 'CM104100', 'Primes d''émission', 'PASSIF', 'CLASSE_1', TRUE, 'Primes d''émission d''actions'),
(2, 'CM104200', 'Réserve légale', 'PASSIF', 'CLASSE_1', FALSE, 'Réserve légale OHADA'),
(2, 'CM104300', 'Réserves statutaires', 'PASSIF', 'CLASSE_1', TRUE, 'Réserves prévues par les statuts'),
(2, 'CM104400', 'Réserves facultatives', 'PASSIF', 'CLASSE_1', TRUE, 'Réserves constituées librement'),

-- === CLASSE 2 - COMPTES D'IMMOBILISATIONS ===
INSERT INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES
-- Immobilisations incorporelles
(2, 'CM201000', 'Immobilisations incorporelles', 'ACTIF', 'CLASSE_2', TRUE, 'Logiciels, brevets, etc.'),
(2, 'CM201100', 'Logiciels', 'ACTIF', 'CLASSE_2', TRUE, 'Logiciels informatiques'),
(2, 'CM201200', 'Brevets et licences', 'ACTIF', 'CLASSE_2', TRUE, 'Brevets et licences'),

-- Immobilisations corporelles
(2, 'CM210000', 'Immobilisations corporelles', 'ACTIF', 'CLASSE_2', TRUE, 'Terrains, bâtiments, matériel'),
(2, 'CM211000', 'Terrains', 'ACTIF', 'CLASSE_2', TRUE, 'Terrains de l''entreprise'),
(2, 'CM213000', 'Bâtiments', 'ACTIF', 'CLASSE_2', TRUE, 'Bâtiments et constructions'),
(2, 'CM215000', 'Matériel médical', 'ACTIF', 'CLASSE_2', TRUE, 'Équipements médicaux'),
(2, 'CM218000', 'Matériel informatique', 'ACTIF', 'CLASSE_2', TRUE, 'Ordinateurs, serveurs'),
(2, 'CM218100', 'Mobilier et matériel de bureau', 'ACTIF', 'CLASSE_2', TRUE, 'Mobilier de bureau'),

-- Immobilisations financières
(2, 'CM260000', 'Immobilisations financières', 'ACTIF', 'CLASSE_2', TRUE, 'Participations, prêts'),
(2, 'CM261000', 'Participations', 'ACTIF', 'CLASSE_2', TRUE, 'Participations dans d''autres sociétés'),

-- === CLASSE 3 - COMPTES DE STOCKS ===
INSERT INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES
(2, 'CM310000', 'Stocks de médicaments', 'ACTIF', 'CLASSE_3', TRUE, 'Médicaments en stock'),
(2, 'CM311000', 'Médicaments génériques', 'ACTIF', 'CLASSE_3', TRUE, 'Médicaments génériques'),
(2, 'CM312000', 'Médicaments spécialisés', 'ACTIF', 'CLASSE_3', TRUE, 'Médicaments spécialisés'),
(2, 'CM320000', 'Stocks de consommables médicaux', 'ACTIF', 'CLASSE_3', TRUE, 'Matériel médical consommable'),
(2, 'CM321000', 'Seringues et aiguilles', 'ACTIF', 'CLASSE_3', TRUE, 'Matériel d''injection'),
(2, 'CM322000', 'Pansements et compresses', 'ACTIF', 'CLASSE_3', TRUE, 'Matériel de soin'),
(2, 'CM330000', 'Stocks de fournitures de bureau', 'ACTIF', 'CLASSE_3', TRUE, 'Fournitures administratives'),

-- === CLASSE 5 - COMPTES FINANCIERS ===
INSERT INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES
(2, 'CM520000', 'Banques', 'ACTIF', 'CLASSE_5', TRUE, 'Comptes bancaires'),
(2, 'CM521000', 'Banque Centrale - Compte principal', 'ACTIF', 'CLASSE_5', TRUE, 'Compte principal'),
(2, 'CM522000', 'Banque Secondaire', 'ACTIF', 'CLASSE_5', TRUE, 'Compte secondaire'),
(2, 'CM530000', 'Établissements financiers', 'ACTIF', 'CLASSE_5', TRUE, 'Autres établissements financiers'),
(2, 'CM540000', 'Instruments de trésorerie', 'ACTIF', 'CLASSE_5', TRUE, 'Placements à court terme'),
(2, 'CM570000', 'Caisse', 'ACTIF', 'CLASSE_5', TRUE, 'Espèces en caisse'),
(2, 'CM571000', 'Caisse principale', 'ACTIF', 'CLASSE_5', FALSE, 'Caisse bureau principal'),
(2, 'CM572000', 'Caisse pharmacie', 'ACTIF', 'CLASSE_5', FALSE, 'Caisse pharmacie'),

-- === CLASSE 8 - COMPTES SPÉCIAUX ===
INSERT INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES
(2, 'CM801000', 'Engagements hors bilan', 'ACTIF_PASSIF', 'CLASSE_8', TRUE, 'Engagements et garanties'),
(2, 'CM810000', 'Engagements donnés', 'ACTIF_PASSIF', 'CLASSE_8', TRUE, 'Garanties données'),
(2, 'CM820000', 'Engagements reçus', 'ACTIF_PASSIF', 'CLASSE_8', TRUE, 'Garanties reçues'),
(2, 'CM890000', 'Comptes de contrôle', 'ACTIF_PASSIF', 'CLASSE_8', TRUE, 'Comptes de vérification');

-- Vérifier les résultats
SELECT 'RÉSUMÉ PAR CLASSE - Comptes parents disponibles:' as info;
SELECT
    classe_compte as classe,
    COUNT(*) as nb_comptes_parents
FROM compte
WHERE entreprise_id = 2 AND accepte_sous_comptes = TRUE
GROUP BY classe_compte
ORDER BY classe_compte;

SELECT 'DÉTAIL - Tous les comptes parents par classe:' as info;
SELECT
    classe_compte,
    numero,
    libelle,
    accepte_sous_comptes
FROM compte
WHERE entreprise_id = 2 AND accepte_sous_comptes = TRUE
ORDER BY classe_compte, numero;

COMMIT;