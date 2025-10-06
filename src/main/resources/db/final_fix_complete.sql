-- Correction finale et complète de toutes les tables
USE comptabilite_db;

-- Désactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Supprimer toutes les tables et les recréer avec la structure exacte du modèle Java
DROP TABLE IF EXISTS ligne_facture;
DROP TABLE IF EXISTS ligne_ecriture;
DROP TABLE IF EXISTS facture;
DROP TABLE IF EXISTS ecriture_comptable;
DROP TABLE IF EXISTS compte;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS fournisseur;
DROP TABLE IF EXISTS exercice;
DROP TABLE IF EXISTS utilisateurs;
DROP TABLE IF EXISTS entreprises;

-- Créer la table entreprises avec la structure EXACTE du modèle Java
CREATE TABLE entreprises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    raisonSociale VARCHAR(200) NOT NULL,
    siret VARCHAR(20) UNIQUE,
    siren VARCHAR(20),
    numeroTVA VARCHAR(30),
    adresse VARCHAR(200),
    codePostal VARCHAR(10),
    ville VARCHAR(100),
    pays VARCHAR(100) DEFAULT 'France',
    telephone VARCHAR(20),
    email VARCHAR(150),
    siteWeb VARCHAR(100),
    formeJuridique ENUM('SARL', 'SAS', 'SA', 'EURL', 'SNC', 'EIRL', 'MICRO_ENTREPRISE', 'ASSOCIATION', 'AUTRE') NOT NULL,
    capitalSocial DOUBLE DEFAULT 0.0,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- Créer la table utilisateurs avec la structure EXACTE du modèle Java
CREATE TABLE utilisateurs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nomUtilisateur VARCHAR(50) UNIQUE NOT NULL,
    motDePasse VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    role ENUM('ADMINISTRATEUR', 'COMPTABLE', 'ASSISTANT_COMPTABLE', 'CONSULTANT') NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    dateCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    derniereConnexion TIMESTAMP NULL,
    entreprise_id BIGINT,
    FOREIGN KEY (entreprise_id) REFERENCES entreprises(id) ON DELETE SET NULL
);

-- Créer la table exercice
CREATE TABLE exercice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entreprise_id BIGINT NOT NULL,
    annee INT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    cloture BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (entreprise_id) REFERENCES entreprises(id) ON DELETE CASCADE,
    UNIQUE KEY unique_exercice_entreprise (entreprise_id, annee)
);

-- Créer la table compte
CREATE TABLE compte (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entreprise_id BIGINT NOT NULL,
    numero VARCHAR(20) NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    type_compte ENUM('ACTIF', 'PASSIF', 'CHARGE', 'PRODUIT', 'ACTIF_PASSIF') NOT NULL,
    classe_compte ENUM('CLASSE_1', 'CLASSE_2', 'CLASSE_3', 'CLASSE_4', 'CLASSE_5', 'CLASSE_6', 'CLASSE_7', 'CLASSE_8') NOT NULL,
    parent_id BIGINT,
    solde_initial DECIMAL(15,2) DEFAULT 0.00,
    solde_debit DECIMAL(15,2) DEFAULT 0.00,
    solde_credit DECIMAL(15,2) DEFAULT 0.00,
    accepte_sous_comptes BOOLEAN DEFAULT FALSE,
    lettrable BOOLEAN DEFAULT FALSE,
    auxiliaire BOOLEAN DEFAULT FALSE,
    description TEXT,
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (entreprise_id) REFERENCES entreprises(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES compte(id) ON DELETE SET NULL,
    UNIQUE KEY unique_compte_numero (entreprise_id, numero)
);

-- Créer les autres tables nécessaires
CREATE TABLE client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entreprise_id BIGINT NOT NULL,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255),
    adresse TEXT,
    telephone VARCHAR(50),
    email VARCHAR(255),
    numero_client VARCHAR(50),
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (entreprise_id) REFERENCES entreprises(id) ON DELETE CASCADE
);

CREATE TABLE fournisseur (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entreprise_id BIGINT NOT NULL,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT,
    telephone VARCHAR(50),
    email VARCHAR(255),
    numero_fournisseur VARCHAR(50),
    numero_siret VARCHAR(50),
    numero_tva VARCHAR(50),
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (entreprise_id) REFERENCES entreprises(id) ON DELETE CASCADE
);

-- Réactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;

-- Insérer les entreprises
INSERT INTO entreprises (raisonSociale, pays, formeJuridique, adresse, telephone, email) VALUES
('Polyclinique Exemple France', 'France', 'SARL', '123 Rue de la Santé, 75001 Paris', '01.23.45.67.89', 'contact@polyclinique-france.fr'),
('Polyclinique Exemple Cameroun', 'Cameroun', 'SARL', 'Avenue Charles de Gaulle, Douala', '+237 6 12 34 56 78', 'contact@polyclinique-cameroun.cm');

-- Insérer les utilisateurs
INSERT INTO utilisateurs (nomUtilisateur, motDePasse, nom, prenom, email, role, entreprise_id) VALUES
('admin_france', 'admin', 'Admin', 'France', 'admin.france@comptabilite.com', 'ADMINISTRATEUR', 1),
('admin_cameroun', 'admin', 'Admin', 'Cameroun', 'admin.cameroun@comptabilite.com', 'ADMINISTRATEUR', 2),
('comptable_france', 'comptable', 'Comptable', 'France', 'comptable.france@comptabilite.com', 'COMPTABLE', 1),
('comptable_cameroun', 'comptable', 'Comptable', 'Cameroun', 'comptable.cameroun@comptabilite.com', 'COMPTABLE', 2);

-- Insérer les exercices
INSERT INTO exercice (entreprise_id, annee, date_debut, date_fin) VALUES
(1, 2024, '2024-01-01', '2024-12-31'),
(2, 2024, '2024-01-01', '2024-12-31');

-- Insérer les comptes de base
INSERT INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes) VALUES
-- France
(1, '101000', 'Capital', 'PASSIF', 'CLASSE_1', FALSE),
(1, '110000', 'Report à nouveau', 'PASSIF', 'CLASSE_1', FALSE),
(1, '400000', 'Fournisseurs', 'PASSIF', 'CLASSE_4', TRUE),
(1, '410000', 'Clients', 'ACTIF', 'CLASSE_4', TRUE),
(1, '512000', 'Banque', 'ACTIF', 'CLASSE_5', FALSE),
(1, '530000', 'Caisse', 'ACTIF', 'CLASSE_5', FALSE),
(1, '600000', 'Achats', 'CHARGE', 'CLASSE_6', TRUE),
(1, '700000', 'Ventes', 'PRODUIT', 'CLASSE_7', TRUE),

-- Cameroun
(2, 'CM101000', 'Capital', 'PASSIF', 'CLASSE_1', FALSE),
(2, 'CM110000', 'Report à nouveau', 'PASSIF', 'CLASSE_1', FALSE),
(2, 'CM400000', 'Fournisseurs', 'PASSIF', 'CLASSE_4', TRUE),
(2, 'CM410000', 'Clients', 'ACTIF', 'CLASSE_4', TRUE),
(2, 'CM520000', 'Banque', 'ACTIF', 'CLASSE_5', FALSE),
(2, 'CM570000', 'Caisse', 'ACTIF', 'CLASSE_5', FALSE),
(2, 'CM600000', 'Achats', 'CHARGE', 'CLASSE_6', TRUE),
(2, 'CM700000', 'Ventes', 'PRODUIT', 'CLASSE_7', TRUE);

-- Vérifier le résultat final
SELECT 'Entreprises créées:' as info;
SELECT id, raisonSociale, pays FROM entreprises;

SELECT 'Utilisateurs créés:' as info;
SELECT u.id, u.nomUtilisateur, u.nom, u.prenom, u.role, e.raisonSociale, e.pays
FROM utilisateurs u
LEFT JOIN entreprises e ON u.entreprise_id = e.id;

SELECT 'Comptes créés:' as info;
SELECT COUNT(*) as total_comptes FROM compte;

COMMIT;