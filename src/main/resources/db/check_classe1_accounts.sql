-- Vérifier spécifiquement les comptes de classe 1 pour le Cameroun
USE comptabilite_db;

SELECT 'Comptes de classe 1 pour le Cameroun:' as info;
SELECT
    numero,
    libelle,
    accepte_sous_comptes,
    classe_compte
FROM compte
WHERE entreprise_id = 2
AND classe_compte = 'CLASSE_1'
ORDER BY numero;

SELECT 'Tous les comptes qui acceptent des sous-comptes pour le Cameroun:' as info;
SELECT
    numero,
    libelle,
    classe_compte,
    accepte_sous_comptes
FROM compte
WHERE entreprise_id = 2
AND accepte_sous_comptes = TRUE
ORDER BY classe_compte, numero;