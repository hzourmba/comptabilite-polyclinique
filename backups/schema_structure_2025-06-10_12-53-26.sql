-- MySQL dump 10.13  Distrib 9.4.0, for Win64 (x86_64)
--
-- Host: localhost    Database: comptabilite_db
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `clients`
--

DROP TABLE IF EXISTS `clients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clients` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `actif` bit(1) NOT NULL,
  `adresse` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `codeClient` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `codePostal` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_creation` datetime(6) NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nom` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `notes` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numeroTVA` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pays` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `personneContact` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `prenom` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `raisonSociale` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `siret` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `soldeClient` decimal(15,2) DEFAULT NULL,
  `statutClient` enum('ACTIF','INACTIF','SUSPENDU') COLLATE utf8mb4_unicode_ci NOT NULL,
  `telephone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `typeClient` enum('ADMINISTRATION','ASSOCIATION','ENTREPRISE','PARTICULIER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `ville` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `entreprise_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7ti034xvcmrkit6apauy5c5ds` (`codeClient`),
  KEY `FKtao5i8j0vrhq73sa09nud6xk7` (`entreprise_id`),
  CONSTRAINT `FKtao5i8j0vrhq73sa09nud6xk7` FOREIGN KEY (`entreprise_id`) REFERENCES `entreprises` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `compte`
--

DROP TABLE IF EXISTS `compte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `compte` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `entreprise_id` bigint NOT NULL,
  `numero` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `libelle` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type_compte` enum('ACTIF','ACTIF_PASSIF','CHARGE','PASSIF','PRODUIT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `classe_compte` enum('CLASSE_1','CLASSE_2','CLASSE_3','CLASSE_4','CLASSE_5','CLASSE_6','CLASSE_7','CLASSE_8') COLLATE utf8mb4_unicode_ci NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `solde_initial` decimal(15,2) DEFAULT '0.00',
  `solde_debit` decimal(15,2) DEFAULT '0.00',
  `solde_credit` decimal(15,2) DEFAULT '0.00',
  `accepte_sous_comptes` tinyint(1) DEFAULT '0',
  `lettrable` tinyint(1) DEFAULT '0',
  `auxiliaire` tinyint(1) DEFAULT '0',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `actif` tinyint(1) DEFAULT '1',
  `date_creation` datetime(6) NOT NULL,
  `date_modification` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_compte_numero` (`entreprise_id`,`numero`),
  KEY `parent_id` (`parent_id`),
  CONSTRAINT `compte_ibfk_1` FOREIGN KEY (`entreprise_id`) REFERENCES `entreprises` (`id`) ON DELETE CASCADE,
  CONSTRAINT `compte_ibfk_2` FOREIGN KEY (`parent_id`) REFERENCES `compte` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ecritures_comptables`
--

DROP TABLE IF EXISTS `ecritures_comptables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ecritures_comptables` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `dateEcriture` date NOT NULL,
  `date_validation` datetime(6) DEFAULT NULL,
  `libelle` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numeroEcriture` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `numeroJournal` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `referencePiece` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `statut` enum('BROUILLON','CLOTUREE','VALIDEE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `entreprise_id` bigint NOT NULL,
  `exercice_id` bigint NOT NULL,
  `utilisateur_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKeiderr3j43hrb5h2wadl7b6na` (`entreprise_id`),
  KEY `FKo6l37jkvj1yrdbx5g78eusu2d` (`exercice_id`),
  KEY `FKcbrpv168g3k3pe0jxkei1smkc` (`utilisateur_id`),
  CONSTRAINT `FKcbrpv168g3k3pe0jxkei1smkc` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`),
  CONSTRAINT `FKeiderr3j43hrb5h2wadl7b6na` FOREIGN KEY (`entreprise_id`) REFERENCES `entreprises` (`id`),
  CONSTRAINT `FKo6l37jkvj1yrdbx5g78eusu2d` FOREIGN KEY (`exercice_id`) REFERENCES `exercices` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entreprises`
--

DROP TABLE IF EXISTS `entreprises`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `entreprises` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `raisonSociale` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `siret` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `siren` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numeroTVA` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `adresse` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `codePostal` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ville` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pays` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'France',
  `telephone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `siteWeb` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `formeJuridique` enum('ASSOCIATION','AUTRE','EIRL','EURL','MICRO_ENTREPRISE','SA','SARL','SAS','SNC') COLLATE utf8mb4_unicode_ci NOT NULL,
  `capitalSocial` double NOT NULL,
  `date_creation` datetime(6) NOT NULL,
  `active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `siret` (`siret`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exercices`
--

DROP TABLE IF EXISTS `exercices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exercices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date_cloture` datetime(6) DEFAULT NULL,
  `date_creation` datetime(6) NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `libelle` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `statut` enum('ARCHIVE','CLOTURE','OUVERT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `entreprise_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKaajgyiykclygpwjwnp1kgp7px` (`entreprise_id`),
  CONSTRAINT `FKaajgyiykclygpwjwnp1kgp7px` FOREIGN KEY (`entreprise_id`) REFERENCES `entreprises` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `factures`
--

DROP TABLE IF EXISTS `factures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factures` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `commentaires` text COLLATE utf8mb4_unicode_ci,
  `date_creation` datetime(6) NOT NULL,
  `dateEcheance` date DEFAULT NULL,
  `date_envoi` datetime(6) DEFAULT NULL,
  `dateFacture` date NOT NULL,
  `date_paiement` date DEFAULT NULL,
  `montantHT` decimal(15,2) NOT NULL,
  `montantTTC` decimal(15,2) NOT NULL,
  `montantTVA` decimal(15,2) NOT NULL,
  `numeroFacture` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `objet` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `statut` enum('ANNULEE','BROUILLON','ENVOYEE','EN_RETARD','PAYEE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `tauxTVA` decimal(5,2) NOT NULL,
  `typeFacture` enum('ACHAT','AVOIR_ACHAT','AVOIR_VENTE','VENTE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `client_id` bigint DEFAULT NULL,
  `entreprise_id` bigint NOT NULL,
  `fournisseur_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_s9u2oycvbsai6ab22jf28bptx` (`numeroFacture`),
  KEY `FKakoqi8cpfxab6j16clvdjuldi` (`client_id`),
  KEY `FKpakjr3ushoktpoi6smgatwod2` (`entreprise_id`),
  KEY `FKcstw4letgt1qpjs3t7agx7saf` (`fournisseur_id`),
  CONSTRAINT `FKakoqi8cpfxab6j16clvdjuldi` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`),
  CONSTRAINT `FKcstw4letgt1qpjs3t7agx7saf` FOREIGN KEY (`fournisseur_id`) REFERENCES `fournisseurs` (`id`),
  CONSTRAINT `FKpakjr3ushoktpoi6smgatwod2` FOREIGN KEY (`entreprise_id`) REFERENCES `entreprises` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fournisseurs`
--

DROP TABLE IF EXISTS `fournisseurs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fournisseurs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `actif` bit(1) NOT NULL,
  `adresse` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `codeFournisseur` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `codePostal` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_creation` datetime(6) NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nom` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `notes` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numeroCompteFournisseur` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numeroTVA` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pays` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `personneContact` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `prenom` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `raisonSociale` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `siret` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `soldeFournisseur` decimal(15,2) DEFAULT NULL,
  `statutFournisseur` enum('ACTIF','INACTIF','SUSPENDU') COLLATE utf8mb4_unicode_ci NOT NULL,
  `telephone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `typeFournisseur` enum('ADMINISTRATION','ASSOCIATION','ENTREPRISE','FREELANCE','PARTICULIER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `ville` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `entreprise_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_8od8mhs37dxmxjch4d6f42e60` (`codeFournisseur`),
  KEY `FKl3r6jrigstvlobxsky3sr377c` (`entreprise_id`),
  CONSTRAINT `FKl3r6jrigstvlobxsky3sr377c` FOREIGN KEY (`entreprise_id`) REFERENCES `entreprises` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lignes_ecriture`
--

DROP TABLE IF EXISTS `lignes_ecriture`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lignes_ecriture` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `libelle` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `montantCredit` decimal(15,2) NOT NULL,
  `montantDebit` decimal(15,2) NOT NULL,
  `compte_id` bigint NOT NULL,
  `ecritureComptable_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg8k6k2js2dng8xxh23acckn95` (`ecritureComptable_id`),
  KEY `FKmym4sprldavqci3idhh90vcmk` (`compte_id`),
  CONSTRAINT `FK6s4y7s1w6kdv1krs6ha7we70n` FOREIGN KEY (`compte_id`) REFERENCES `compte` (`id`),
  CONSTRAINT `FKg8k6k2js2dng8xxh23acckn95` FOREIGN KEY (`ecritureComptable_id`) REFERENCES `ecritures_comptables` (`id`),
  CONSTRAINT `FKmym4sprldavqci3idhh90vcmk` FOREIGN KEY (`compte_id`) REFERENCES `compte` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lignes_facture`
--

DROP TABLE IF EXISTS `lignes_facture`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lignes_facture` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `designation` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `montantHT` decimal(15,2) NOT NULL,
  `prixUnitaire` decimal(15,2) NOT NULL,
  `quantite` decimal(10,2) NOT NULL,
  `facture_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp2ih5jcnrycex0g56t1wkw8o6` (`facture_id`),
  CONSTRAINT `FKp2ih5jcnrycex0g56t1wkw8o6` FOREIGN KEY (`facture_id`) REFERENCES `factures` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `utilisateurs`
--

DROP TABLE IF EXISTS `utilisateurs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilisateurs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nomUtilisateur` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `motDePasse` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nom` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `prenom` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ADMINISTRATEUR','ASSISTANT_COMPTABLE','COMPTABLE','CONSULTANT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `actif` tinyint(1) DEFAULT '1',
  `dateCreation` datetime(6) NOT NULL,
  `derniereConnexion` datetime(6) DEFAULT NULL,
  `entreprise_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nomUtilisateur` (`nomUtilisateur`),
  UNIQUE KEY `email` (`email`),
  KEY `entreprise_id` (`entreprise_id`),
  CONSTRAINT `utilisateurs_ibfk_1` FOREIGN KEY (`entreprise_id`) REFERENCES `entreprises` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'comptabilite_db'
--

--
-- Dumping routines for database 'comptabilite_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-06 12:53:31
