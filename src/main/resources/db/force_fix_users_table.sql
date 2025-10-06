-- Forcer la correction de la table utilisateurs en désactivant les contraintes
USE comptabilite_db;

-- Désactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Supprimer toutes les tables qui pourraient référencer utilisateurs
DROP TABLE IF EXISTS ecritures_comptables;
DROP TABLE IF EXISTS ligne_ecriture;
DROP TABLE IF EXISTS ecriture_comptable;
DROP TABLE IF EXISTS ligne_facture;
DROP TABLE IF EXISTS facture;

-- Maintenant supprimer la table utilisateurs
DROP TABLE IF EXISTS utilisateurs;

-- Créer la table utilisateurs avec la structure correcte
CREATE TABLE utilisateurs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_utilisateur VARCHAR(50) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    role ENUM('ADMINISTRATEUR', 'COMPTABLE', 'ASSISTANT_COMPTABLE', 'CONSULTANT') NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    derniere_connexion TIMESTAMP NULL,
    entreprise_id BIGINT,
    FOREIGN KEY (entreprise_id) REFERENCES entreprise(id) ON DELETE SET NULL
);

-- Recréer les tables supprimées
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

-- Réactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;

-- Insérer les utilisateurs
INSERT INTO utilisateurs (nom_utilisateur, mot_de_passe, nom, prenom, email, role, entreprise_id) VALUES
('admin_france', 'admin', 'Admin', 'France', 'admin.france@comptabilite.com', 'ADMINISTRATEUR', 1),
('admin_cameroun', 'admin', 'Admin', 'Cameroun', 'admin.cameroun@comptabilite.com', 'ADMINISTRATEUR', 2),
('comptable_france', 'comptable', 'Comptable', 'France', 'comptable.france@comptabilite.com', 'COMPTABLE', 1),
('comptable_cameroun', 'comptable', 'Comptable', 'Cameroun', 'comptable.cameroun@comptabilite.com', 'COMPTABLE', 2);

-- Vérifier la création
SELECT id, nom_utilisateur, nom, prenom, role, entreprise_id FROM utilisateurs;

COMMIT;