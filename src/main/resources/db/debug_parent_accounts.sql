-- Diagnostic pour comprendre pourquoi le ComboBox est vide
USE comptabilite_db;

-- Vérifier tous les comptes qui acceptent des sous-comptes
SELECT 'Comptes qui acceptent des sous-comptes:' as info;
SELECT
    c.id,
    c.numero,
    c.libelle,
    c.classe_compte,
    c.accepte_sous_comptes,
    e.raisonSociale as entreprise,
    e.id as entreprise_id
FROM compte c
JOIN entreprises e ON c.entreprise_id = e.id
WHERE c.accepte_sous_comptes = TRUE
ORDER BY e.id, c.numero;

-- Vérifier spécifiquement les comptes de classe 1
SELECT 'Comptes de classe 1:' as info;
SELECT
    c.id,
    c.numero,
    c.libelle,
    c.accepte_sous_comptes,
    e.raisonSociale as entreprise
FROM compte c
JOIN entreprises e ON c.entreprise_id = e.id
WHERE c.classe_compte = 'CLASSE_1'
ORDER BY e.id, c.numero;

-- Vérifier la méthode getComptesParents dans CompteDAO
-- Elle devrait chercher: accepte_sous_comptes = TRUE ET entreprise_id = ?
SELECT 'Simulation de la requête getComptesParents pour entreprise Cameroun (ID=2):' as info;
SELECT
    c.id,
    c.numero,
    c.libelle,
    c.type_compte,
    c.classe_compte
FROM compte c
WHERE c.accepte_sous_comptes = TRUE
AND c.entreprise_id = 2
ORDER BY c.numero;

-- Vérifier les utilisateurs et leurs entreprises
SELECT 'Utilisateurs et leurs entreprises:' as info;
SELECT
    u.nomUtilisateur,
    u.entreprise_id,
    e.raisonSociale,
    e.pays
FROM utilisateurs u
LEFT JOIN entreprises e ON u.entreprise_id = e.id;