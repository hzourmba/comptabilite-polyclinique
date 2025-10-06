-- Ajouter une structure complète pour les comptes de capitaux (Classe 1)
USE comptabilite_db;

-- Supprimer les anciens comptes de classe 1 basiques
DELETE FROM compte WHERE classe_compte = 'CLASSE_1';

-- Ajouter une structure complète pour les comptes de capitaux

-- === FRANCE ===
INSERT INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES

-- Comptes de capital principal (parents)
(1, '101000', 'Capital social', 'PASSIF', 'CLASSE_1', TRUE, 'Capital social de la société'),
(1, '104000', 'Primes liées au capital social', 'PASSIF', 'CLASSE_1', TRUE, 'Primes d''émission, de fusion, d''apport'),
(1, '106000', 'Réserves', 'PASSIF', 'CLASSE_1', TRUE, 'Réserves légales, statutaires et autres'),
(1, '110000', 'Report à nouveau', 'PASSIF', 'CLASSE_1', FALSE, 'Bénéfices non distribués'),
(1, '120000', 'Résultat de l''exercice', 'PASSIF', 'CLASSE_1', FALSE, 'Résultat de l''exercice en cours'),

-- Sous-comptes détaillés pour le capital social
(1, '101100', 'Capital souscrit - appelé', 'PASSIF', 'CLASSE_1', TRUE, 'Capital souscrit et appelé'),
(1, '101200', 'Capital souscrit - non appelé', 'PASSIF', 'CLASSE_1', TRUE, 'Capital souscrit mais non encore appelé'),
(1, '101300', 'Capital souscrit - versé', 'PASSIF', 'CLASSE_1', TRUE, 'Capital effectivement versé par les actionnaires'),

-- Comptes individuels d'actionnaires (sous 101300)
(1, '101310', 'Actionnaires - Personnes physiques', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par des personnes physiques'),
(1, '101320', 'Actionnaires - Personnes morales', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par des sociétés'),
(1, '101330', 'Actionnaires - Dirigeants', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par les dirigeants'),

-- === CAMEROUN (OHADA) ===
INSERT INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes, description) VALUES

-- Comptes de capital principal (parents)
(2, 'CM101000', 'Capital social', 'PASSIF', 'CLASSE_1', TRUE, 'Capital social de la société'),
(2, 'CM104000', 'Primes et réserves', 'PASSIF', 'CLASSE_1', TRUE, 'Primes d''émission et réserves'),
(2, 'CM110000', 'Report à nouveau', 'PASSIF', 'CLASSE_1', FALSE, 'Bénéfices non distribués'),
(2, 'CM120000', 'Résultat de l''exercice', 'PASSIF', 'CLASSE_1', FALSE, 'Résultat de l''exercice en cours'),

-- Sous-comptes détaillés pour le capital social
(2, 'CM101100', 'Capital souscrit', 'PASSIF', 'CLASSE_1', TRUE, 'Capital souscrit par les actionnaires'),
(2, 'CM101200', 'Capital appelé', 'PASSIF', 'CLASSE_1', TRUE, 'Capital appelé par la société'),
(2, 'CM101300', 'Capital versé', 'PASSIF', 'CLASSE_1', TRUE, 'Capital effectivement versé'),

-- Comptes individuels d'actionnaires (sous 101300)
(2, 'CM101310', 'Actionnaires - Personnes physiques', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par des personnes physiques'),
(2, 'CM101320', 'Actionnaires - Personnes morales', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par des sociétés'),
(2, 'CM101330', 'Actionnaires - Fondateurs', 'PASSIF', 'CLASSE_1', TRUE, 'Actions détenues par les fondateurs'),
(2, 'CM101340', 'Actionnaires - État', 'PASSIF', 'CLASSE_1', TRUE, 'Participation de l''État');

-- Afficher la structure créée
SELECT 'Comptes de capitaux disponibles pour sous-comptes d''actionnaires:' as info;
SELECT numero, libelle,
       CASE WHEN accepte_sous_comptes THEN 'OUI' ELSE 'NON' END as accept_sous_comptes,
       (SELECT raisonSociale FROM entreprises WHERE id = compte.entreprise_id) as entreprise
FROM compte
WHERE classe_compte = 'CLASSE_1' AND accepte_sous_comptes = TRUE
ORDER BY entreprise_id, numero;

COMMIT;