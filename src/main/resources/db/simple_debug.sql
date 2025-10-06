-- Diagnostic simple pour voir ce qui se passe
USE comptabilite_db;

-- Compter tous les comptes par classe pour l'entreprise Cameroun (ID=2)
SELECT 'COMPTAGE PAR CLASSE:' as info;
SELECT
    classe_compte,
    COUNT(*) as total_comptes,
    SUM(CASE WHEN accepte_sous_comptes = TRUE THEN 1 ELSE 0 END) as comptes_parents
FROM compte
WHERE entreprise_id = 2
GROUP BY classe_compte
ORDER BY classe_compte;

-- Voir exactement quels comptes existent pour CLASSE_1
SELECT 'COMPTES CLASSE_1:' as info;
SELECT numero, libelle, accepte_sous_comptes
FROM compte
WHERE entreprise_id = 2 AND classe_compte = 'CLASSE_1'
ORDER BY numero;

-- Test de la requête exacte du DAO
SELECT 'TEST REQUÊTE DAO pour CLASSE_1:' as info;
SELECT numero, libelle, classe_compte, accepte_sous_comptes
FROM compte
WHERE accepte_sous_comptes = TRUE
AND entreprise_id = 2
AND classe_compte = 'CLASSE_1'
ORDER BY numero;