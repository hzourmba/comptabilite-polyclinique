-- Ajouter les comptes de classe 1 manquants et les marquer comme parents
USE comptabilite_db;

-- Mettre à jour les comptes de classe 1 existants pour qu'ils acceptent des sous-comptes
UPDATE compte
SET accepte_sous_comptes = TRUE
WHERE classe_compte = 'CLASSE_1'
AND numero IN (
    'CM101000', 'CM101100', 'CM101200', 'CM101300', 'CM101310', 'CM101320', 'CM101330', 'CM101340', 'CM104000'
);

-- Vérifier les résultats
SELECT 'Comptes de classe 1 qui acceptent maintenant des sous-comptes (Cameroun):' as info;
SELECT
    numero,
    libelle,
    accepte_sous_comptes,
    classe_compte
FROM compte
WHERE entreprise_id = 2
AND classe_compte = 'CLASSE_1'
AND accepte_sous_comptes = TRUE
ORDER BY numero;

COMMIT;