-- Script to completely clean and recreate the database
-- This will fix the enum value issues by starting fresh

-- Use the database
USE comptabilite_db;

-- Disable foreign key checks to allow dropping tables
SET FOREIGN_KEY_CHECKS = 0;

-- Drop all tables if they exist
DROP TABLE IF EXISTS ligne_facture;
DROP TABLE IF EXISTS ligne_ecriture;
DROP TABLE IF EXISTS facture;
DROP TABLE IF EXISTS ecriture_comptable;
DROP TABLE IF EXISTS compte;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS fournisseur;
DROP TABLE IF EXISTS exercice;
DROP TABLE IF EXISTS entreprise;
DROP TABLE IF EXISTS utilisateur;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Create utilisateur table
CREATE TABLE utilisateur (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'COMPTABLE', 'UTILISATEUR') NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create entreprise table
CREATE TABLE entreprise (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse TEXT,
    telephone VARCHAR(50),
    email VARCHAR(255),
    siret VARCHAR(50),
    numero_tva VARCHAR(50),
    pays VARCHAR(100) NOT NULL DEFAULT 'France',
    devise VARCHAR(10) NOT NULL DEFAULT 'EUR',
    logo LONGBLOB,
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create exercice table
CREATE TABLE exercice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entreprise_id BIGINT NOT NULL,
    annee INT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    cloture BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE CASCADE,
    UNIQUE KEY unique_exercice_entreprise (entreprise_id, annee)
);

-- Create compte table with ONLY the new simplified enum values
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
    FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES compte(id) ON DELETE SET NULL,
    UNIQUE KEY unique_compte_numero (entreprise_id, numero),
    INDEX idx_compte_numero (numero),
    INDEX idx_compte_classe (classe_compte),
    INDEX idx_compte_parent (parent_id)
);

-- Create client table
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
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE CASCADE,
    UNIQUE KEY unique_client_numero (entreprise_id, numero_client)
);

-- Create fournisseur table
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
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE CASCADE,
    UNIQUE KEY unique_fournisseur_numero (entreprise_id, numero_fournisseur)
);

-- Create ecriture_comptable table
CREATE TABLE ecriture_comptable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entreprise_id BIGINT NOT NULL,
    exercice_id BIGINT NOT NULL,
    numero VARCHAR(50) NOT NULL,
    date_ecriture DATE NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    piece_justificative VARCHAR(255),
    montant_total DECIMAL(15,2) NOT NULL,
    equilibree BOOLEAN DEFAULT FALSE,
    validee BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE CASCADE,
    FOREIGN KEY (exercice_id) REFERENCES exercice(id) ON DELETE CASCADE,
    UNIQUE KEY unique_ecriture_numero (entreprise_id, exercice_id, numero),
    INDEX idx_ecriture_date (date_ecriture),
    INDEX idx_ecriture_numero (numero)
);

-- Create ligne_ecriture table
CREATE TABLE ligne_ecriture (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ecriture_id BIGINT NOT NULL,
    compte_id BIGINT NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    montant_debit DECIMAL(15,2) DEFAULT 0.00,
    montant_credit DECIMAL(15,2) DEFAULT 0.00,
    lettrage VARCHAR(20),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ecriture_id) REFERENCES ecriture_comptable(id) ON DELETE CASCADE,
    FOREIGN KEY (compte_id) REFERENCES compte(id) ON DELETE RESTRICT,
    INDEX idx_ligne_ecriture_compte (compte_id),
    INDEX idx_ligne_ecriture_lettrage (lettrage)
);

-- Create facture table
CREATE TABLE facture (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entreprise_id BIGINT NOT NULL,
    exercice_id BIGINT NOT NULL,
    client_id BIGINT,
    fournisseur_id BIGINT,
    numero VARCHAR(50) NOT NULL,
    date_facture DATE NOT NULL,
    date_echeance DATE,
    type_facture ENUM('VENTE', 'ACHAT') NOT NULL,
    montant_ht DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    montant_tva DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    montant_ttc DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    statut ENUM('BROUILLON', 'VALIDEE', 'PAYEE', 'ANNULEE') DEFAULT 'BROUILLON',
    notes TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE CASCADE,
    FOREIGN KEY (exercice_id) REFERENCES exercice(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE SET NULL,
    FOREIGN KEY (fournisseur_id) REFERENCES fournisseur(id) ON DELETE SET NULL,
    UNIQUE KEY unique_facture_numero (entreprise_id, exercice_id, numero),
    INDEX idx_facture_date (date_facture),
    INDEX idx_facture_client (client_id),
    INDEX idx_facture_fournisseur (fournisseur_id)
);

-- Create ligne_facture table
CREATE TABLE ligne_facture (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    facture_id BIGINT NOT NULL,
    compte_id BIGINT,
    designation VARCHAR(255) NOT NULL,
    quantite DECIMAL(10,3) DEFAULT 1.000,
    prix_unitaire DECIMAL(15,2) NOT NULL,
    taux_tva DECIMAL(5,4) DEFAULT 0.0000,
    montant_ht DECIMAL(15,2) NOT NULL,
    montant_tva DECIMAL(15,2) NOT NULL,
    montant_ttc DECIMAL(15,2) NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (facture_id) REFERENCES facture(id) ON DELETE CASCADE,
    FOREIGN KEY (compte_id) REFERENCES compte(id) ON DELETE SET NULL,
    INDEX idx_ligne_facture_compte (compte_id)
);

-- Insert default data for France
INSERT INTO entreprise (nom, pays, devise, adresse, telephone, email) VALUES
('Polyclinique Exemple France', 'France', 'EUR', '123 Rue de la Santé, 75001 Paris', '01.23.45.67.89', 'contact@polyclinique-france.fr');

-- Insert default data for Cameroun
INSERT INTO entreprise (nom, pays, devise, adresse, telephone, email) VALUES
('Polyclinique Exemple Cameroun', 'Cameroun', 'FCFA', 'Avenue Charles de Gaulle, Douala', '+237 6 12 34 56 78', 'contact@polyclinique-cameroun.cm');

-- Insert basic chart of accounts for France (PCG) with NEW ENUM VALUES
INSERT INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes) VALUES
-- Classe 1 - Comptes de capitaux
(1, '101000', 'Capital', 'PASSIF', 'CLASSE_1', FALSE),
(1, '110000', 'Report à nouveau', 'PASSIF', 'CLASSE_1', FALSE),
(1, '120000', 'Résultat de l''exercice', 'PASSIF', 'CLASSE_1', FALSE),

-- Classe 4 - Comptes de tiers
(1, '400000', 'Fournisseurs', 'PASSIF', 'CLASSE_4', TRUE),
(1, '410000', 'Clients', 'ACTIF', 'CLASSE_4', TRUE),
(1, '445660', 'TVA déductible', 'ACTIF', 'CLASSE_4', FALSE),
(1, '445710', 'TVA collectée', 'PASSIF', 'CLASSE_4', FALSE),

-- Classe 5 - Comptes financiers
(1, '512000', 'Banque', 'ACTIF', 'CLASSE_5', FALSE),
(1, '530000', 'Caisse', 'ACTIF', 'CLASSE_5', FALSE),

-- Classe 6 - Comptes de charges
(1, '600000', 'Achats', 'CHARGE', 'CLASSE_6', TRUE),
(1, '610000', 'Services extérieurs', 'CHARGE', 'CLASSE_6', TRUE),
(1, '640000', 'Charges de personnel', 'CHARGE', 'CLASSE_6', TRUE),

-- Classe 7 - Comptes de produits
(1, '700000', 'Ventes', 'PRODUIT', 'CLASSE_7', TRUE),
(1, '760000', 'Produits financiers', 'PRODUIT', 'CLASSE_7', TRUE);

-- Insert basic chart of accounts for Cameroun (OHADA) with NEW ENUM VALUES
INSERT INTO compte (entreprise_id, numero, libelle, type_compte, classe_compte, accepte_sous_comptes) VALUES
-- Classe 1 - Comptes de capitaux
(2, 'CM101000', 'Capital', 'PASSIF', 'CLASSE_1', FALSE),
(2, 'CM110000', 'Report à nouveau', 'PASSIF', 'CLASSE_1', FALSE),
(2, 'CM120000', 'Résultat de l''exercice', 'PASSIF', 'CLASSE_1', FALSE),

-- Classe 4 - Comptes de tiers
(2, 'CM400000', 'Fournisseurs', 'PASSIF', 'CLASSE_4', TRUE),
(2, 'CM410000', 'Clients', 'ACTIF', 'CLASSE_4', TRUE),
(2, 'CM445000', 'État - TVA', 'ACTIF_PASSIF', 'CLASSE_4', TRUE),

-- Classe 5 - Comptes financiers
(2, 'CM520000', 'Banque', 'ACTIF', 'CLASSE_5', FALSE),
(2, 'CM570000', 'Caisse', 'ACTIF', 'CLASSE_5', FALSE),

-- Classe 6 - Comptes de charges
(2, 'CM600000', 'Achats', 'CHARGE', 'CLASSE_6', TRUE),
(2, 'CM610000', 'Services extérieurs', 'CHARGE', 'CLASSE_6', TRUE),
(2, 'CM640000', 'Charges de personnel', 'CHARGE', 'CLASSE_6', TRUE),

-- Classe 7 - Comptes de produits
(2, 'CM700000', 'Ventes', 'PRODUIT', 'CLASSE_7', TRUE),
(2, 'CM760000', 'Produits financiers', 'PRODUIT', 'CLASSE_7', TRUE);

-- Insert default admin user
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role) VALUES
('Admin', 'Système', 'admin@comptabilite.com', 'admin123', 'ADMIN');

-- Insert default exercises
INSERT INTO exercice (entreprise_id, annee, date_debut, date_fin) VALUES
(1, 2024, '2024-01-01', '2024-12-31'),
(2, 2024, '2024-01-01', '2024-12-31');

COMMIT;

-- Show confirmation
SELECT 'Database recreated successfully with correct enum values' AS status;