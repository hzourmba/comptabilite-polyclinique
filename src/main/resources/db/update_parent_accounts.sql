-- Mettre à jour certains comptes pour qu'ils acceptent des sous-comptes
USE comptabilite_db;

-- Mettre à jour les comptes qui peuvent avoir des sous-comptes
-- Classes 4 (Tiers), 6 (Charges) et 7 (Produits) sont généralement des comptes parents

UPDATE compte
SET accepte_sous_comptes = TRUE
WHERE numero IN (
    '400000',  -- Fournisseurs France
    '410000',  -- Clients France
    '600000',  -- Achats France
    '700000',  -- Ventes France
    'CM400000', -- Fournisseurs Cameroun
    'CM410000', -- Clients Cameroun
    'CM600000', -- Achats Cameroun
    'CM700000'  -- Ventes Cameroun
);

-- Ajouter quelques comptes parents supplémentaires pour plus d'options
INSERT INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes) VALUES
-- France - Comptes parents supplémentaires
(1, '411000', 'Clients - Comptes ordinaires', 'ACTIF', 'CLASSE_4', TRUE),
(1, '401000', 'Fournisseurs - Comptes ordinaires', 'PASSIF', 'CLASSE_4', TRUE),
(1, '610000', 'Services extérieurs', 'CHARGE', 'CLASSE_6', TRUE),
(1, '620000', 'Autres services extérieurs', 'CHARGE', 'CLASSE_6', TRUE),
(1, '640000', 'Charges de personnel', 'CHARGE', 'CLASSE_6', TRUE),
(1, '701000', 'Ventes de produits finis', 'PRODUIT', 'CLASSE_7', TRUE),
(1, '706000', 'Prestations de services', 'PRODUIT', 'CLASSE_7', TRUE),

-- Cameroun - Comptes parents supplémentaires
(2, 'CM411000', 'Clients - Comptes ordinaires', 'ACTIF', 'CLASSE_4', TRUE),
(2, 'CM401000', 'Fournisseurs - Comptes ordinaires', 'PASSIF', 'CLASSE_4', TRUE),
(2, 'CM610000', 'Services extérieurs', 'CHARGE', 'CLASSE_6', TRUE),
(2, 'CM620000', 'Autres services extérieurs', 'CHARGE', 'CLASSE_6', TRUE),
(2, 'CM640000', 'Charges de personnel', 'CHARGE', 'CLASSE_6', TRUE),
(2, 'CM701000', 'Ventes de produits finis', 'PRODUIT', 'CLASSE_7', TRUE),
(2, 'CM706000', 'Prestations de services', 'PRODUIT', 'CLASSE_7', TRUE);

-- Vérifier quels comptes acceptent maintenant des sous-comptes
SELECT
    numero,
    libelle,
    type_compte,
    classe_compte,
    accepte_sous_comptes,
    (SELECT raisonSociale FROM entreprises WHERE id = compte.entreprise_id) as entreprise
FROM compte
WHERE accepte_sous_comptes = TRUE
ORDER BY entreprise_id, numero;

COMMIT;