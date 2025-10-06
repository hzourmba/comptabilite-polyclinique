-- Corriger les valeurs de accepte_sous_comptes pour les comptes parents
USE comptabilite_db;

-- D'abord, voir l'état actuel
SELECT 'État actuel des comptes accepte_sous_comptes:' as info;
SELECT numero, libelle, accepte_sous_comptes, entreprise_id
FROM compte
WHERE classe_compte = 'CLASSE_1'
ORDER BY entreprise_id, numero;

-- Mettre à jour tous les comptes qui devraient accepter des sous-comptes
UPDATE compte SET accepte_sous_comptes = TRUE
WHERE numero IN (
    -- France
    '101000', '104000', '106000', '101100', '101200', '101300', '101310', '101320', '101330',
    '400000', '410000', '411000', '401000', '600000', '610000', '620000', '640000', '700000', '701000', '706000',

    -- Cameroun
    'CM101000', 'CM104000', 'CM101100', 'CM101200', 'CM101300', 'CM101310', 'CM101320', 'CM101330', 'CM101340',
    'CM400000', 'CM410000', 'CM411000', 'CM401000', 'CM600000', 'CM610000', 'CM620000', 'CM640000', 'CM700000', 'CM701000', 'CM706000'
);

-- Vérifier le résultat
SELECT 'Comptes qui acceptent maintenant des sous-comptes:' as info;
SELECT
    c.numero,
    c.libelle,
    c.accepte_sous_comptes,
    e.raisonSociale as entreprise
FROM compte c
JOIN entreprises e ON c.entreprise_id = e.id
WHERE c.accepte_sous_comptes = TRUE
ORDER BY e.id, c.numero;

-- Test spécifique pour la requête du DAO (entreprise Cameroun = ID 2)
SELECT 'Test requête getComptesParents pour Cameroun (ID=2):' as info;
SELECT numero, libelle, accepte_sous_comptes
FROM compte
WHERE accepte_sous_comptes = TRUE AND entreprise_id = 2
ORDER BY numero;

COMMIT;