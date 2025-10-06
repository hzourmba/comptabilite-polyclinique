-- Vérifier tous les comptes par classe pour le Cameroun
USE comptabilite_db;

SELECT 'CLASSE_1 - Comptes de capitaux:' as info;
SELECT numero, libelle, accepte_sous_comptes FROM compte WHERE entreprise_id = 2 AND classe_compte = 'CLASSE_1' ORDER BY numero;

SELECT 'CLASSE_2 - Comptes d''immobilisations:' as info;
SELECT numero, libelle, accepte_sous_comptes FROM compte WHERE entreprise_id = 2 AND classe_compte = 'CLASSE_2' ORDER BY numero;

SELECT 'CLASSE_3 - Comptes de stocks:' as info;
SELECT numero, libelle, accepte_sous_comptes FROM compte WHERE entreprise_id = 2 AND classe_compte = 'CLASSE_3' ORDER BY numero;

SELECT 'CLASSE_4 - Comptes de tiers:' as info;
SELECT numero, libelle, accepte_sous_comptes FROM compte WHERE entreprise_id = 2 AND classe_compte = 'CLASSE_4' ORDER BY numero;

SELECT 'CLASSE_5 - Comptes financiers:' as info;
SELECT numero, libelle, accepte_sous_comptes FROM compte WHERE entreprise_id = 2 AND classe_compte = 'CLASSE_5' ORDER BY numero;

SELECT 'CLASSE_6 - Comptes de charges:' as info;
SELECT numero, libelle, accepte_sous_comptes FROM compte WHERE entreprise_id = 2 AND classe_compte = 'CLASSE_6' ORDER BY numero;

SELECT 'CLASSE_7 - Comptes de produits:' as info;
SELECT numero, libelle, accepte_sous_comptes FROM compte WHERE entreprise_id = 2 AND classe_compte = 'CLASSE_7' ORDER BY numero;

SELECT 'CLASSE_8 - Comptes spéciaux:' as info;
SELECT numero, libelle, accepte_sous_comptes FROM compte WHERE entreprise_id = 2 AND classe_compte = 'CLASSE_8' ORDER BY numero;