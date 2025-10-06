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
-- Dumping data for table `compte`
--

LOCK TABLES `compte` WRITE;
/*!40000 ALTER TABLE `compte` DISABLE KEYS */;
INSERT INTO `compte` (`id`, `entreprise_id`, `numero`, `libelle`, `type_compte`, `classe_compte`, `parent_id`, `solde_initial`, `solde_debit`, `solde_credit`, `accepte_sous_comptes`, `lettrable`, `auxiliaire`, `description`, `actif`, `date_creation`, `date_modification`) VALUES (1,1,'101000','Capital','PASSIF','CLASSE_1',NULL,0.00,0.00,0.00,0,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(2,1,'110000','Report ├á nouveau','PASSIF','CLASSE_1',NULL,0.00,0.00,0.00,0,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(3,1,'400000','Fournisseurs','PASSIF','CLASSE_4',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(4,1,'410000','Clients','ACTIF','CLASSE_4',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(5,1,'512000','Banque','ACTIF','CLASSE_5',NULL,0.00,0.00,0.00,0,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(6,1,'530000','Caisse','ACTIF','CLASSE_5',NULL,0.00,0.00,0.00,0,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(7,1,'600000','Achats','CHARGE','CLASSE_6',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(8,1,'700000','Ventes','PRODUIT','CLASSE_7',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(9,2,'CM101000','Capital','PASSIF','CLASSE_1',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 16:13:39'),(10,2,'CM110000','Report ├á nouveau','PASSIF','CLASSE_1',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 16:13:39'),(11,2,'CM400000','Fournisseurs','PASSIF','CLASSE_4',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(12,2,'CM410000','Clients','ACTIF','CLASSE_4',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(13,2,'CM520000','Banque','ACTIF','CLASSE_5',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 16:13:39'),(14,2,'CM570000','Caisse','ACTIF','CLASSE_5',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 16:13:39'),(15,2,'CM600000','Achats','CHARGE','CLASSE_6',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(16,2,'CM700000','Ventes','PRODUIT','CLASSE_7',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:48:36.000000','2025-09-28 10:48:36'),(17,1,'411000','Clients - Comptes ordinaires','ACTIF','CLASSE_4',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(18,1,'401000','Fournisseurs - Comptes ordinaires','PASSIF','CLASSE_4',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(19,1,'610000','Services ext├®rieurs','CHARGE','CLASSE_6',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(20,1,'620000','Autres services ext├®rieurs','CHARGE','CLASSE_6',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(21,1,'640000','Charges de personnel','CHARGE','CLASSE_6',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(22,1,'701000','Ventes de produits finis','PRODUIT','CLASSE_7',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(23,1,'706000','Prestations de services','PRODUIT','CLASSE_7',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(24,2,'CM411000','Clients - Comptes ordinaires','ACTIF','CLASSE_4',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(25,2,'CM401000','Fournisseurs - Comptes ordinaires','PASSIF','CLASSE_4',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(26,2,'CM610000','Services ext├®rieurs','CHARGE','CLASSE_6',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(27,2,'CM620000','Autres services ext├®rieurs','CHARGE','CLASSE_6',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(28,2,'CM640000','Charges de personnel','CHARGE','CLASSE_6',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(29,2,'CM701000','Ventes de produits finis','PRODUIT','CLASSE_7',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(30,2,'CM706000','Prestations de services','PRODUIT','CLASSE_7',NULL,0.00,0.00,0.00,1,0,0,NULL,1,'2025-09-28 12:54:25.000000','2025-09-28 10:54:25'),(31,2,'CM101003','Capital vers├®','PASSIF','CLASSE_1',9,0.00,0.00,0.00,1,0,0,'Capital effectivement vers├®',1,'2025-09-28 18:17:44.000000','2025-09-30 06:21:38'),(32,2,'CM101001','Actionnaires - Personnes physiques','PASSIF','CLASSE_1',9,0.00,335000000.00,264000000.00,1,0,0,'Actions d├®tenues par des personnes physiques',1,'2025-09-28 18:17:44.000000','2025-09-30 06:20:51'),(33,2,'CM101002','Actionnaires - Personnes morales','PASSIF','CLASSE_1',9,0.00,42500000.00,42500000.00,1,0,0,'Actions d├®tenues par des soci├®t├®s',1,'2025-09-28 18:17:44.000000','2025-09-30 06:21:12'),(34,2,'CM101004','Actionnaires - Fondateurs','PASSIF','CLASSE_1',9,0.00,0.00,0.00,1,0,0,'Actions d├®tenues par les fondateurs',1,'2025-09-28 18:17:44.000000','2025-09-30 06:22:34'),(35,2,'CM104000','Primes et r├®serves','PASSIF','CLASSE_1',NULL,0.00,0.00,0.00,1,0,0,'Primes d\'├®mission et r├®serves',1,'2025-09-28 18:17:44.000000','2025-09-28 16:17:44'),(36,2,'CM120000','R├®sultat de l\'exercice','PASSIF','CLASSE_1',NULL,0.00,0.00,0.00,0,0,0,'R├®sultat de l\'exercice en cours',1,'2025-09-28 18:17:44.000000','2025-09-28 16:17:44'),(37,2,'CM210000','Immobilisations corporelles','ACTIF','CLASSE_2',NULL,0.00,0.00,0.00,1,0,0,'Terrains, b├ótiments, mat├®riel',1,'2025-09-28 18:17:44.000000','2025-09-28 16:17:44'),(38,2,'CM215000','Mat├®riel m├®dical','ACTIF','CLASSE_2',NULL,0.00,0.00,0.00,1,0,0,'├ëquipements m├®dicaux',1,'2025-09-28 18:17:44.000000','2025-09-28 16:17:44'),(39,2,'CM218000','Mat├®riel informatique','ACTIF','CLASSE_2',NULL,0.00,0.00,0.00,1,0,0,'Ordinateurs, serveurs',1,'2025-09-28 18:17:44.000000','2025-09-28 16:17:44'),(40,2,'CM310000','Stocks de m├®dicaments','ACTIF','CLASSE_3',NULL,0.00,0.00,0.00,1,0,0,'M├®dicaments en stock',1,'2025-09-28 18:17:44.000000','2025-09-28 16:17:44'),(41,2,'CM320000','Stocks de consommables m├®dicaux','ACTIF','CLASSE_3',NULL,0.00,0.00,0.00,1,0,0,'Mat├®riel m├®dical consommable',1,'2025-09-28 18:17:44.000000','2025-09-28 16:17:44'),(42,2,'CM521000','Banque principale','ACTIF','CLASSE_5',13,0.00,451182382.00,0.00,1,1,1,'Compte bancaire principal',1,'2025-09-28 18:17:44.000000','2025-10-03 13:04:12'),(43,2,'CM571000','Caisse principale','ACTIF','CLASSE_5',NULL,0.00,0.00,0.00,0,0,0,'Caisse bureau principal',1,'2025-09-28 18:17:44.000000','2025-09-28 16:17:44'),(44,2,'CM572000','Caisse pharmacie','ACTIF','CLASSE_5',NULL,0.00,0.00,0.00,0,0,0,'Caisse pharmacie',1,'2025-09-28 18:17:44.000000','2025-09-28 16:17:44'),(45,2,'CM801000','Engagements hors bilan','ACTIF_PASSIF','CLASSE_8',NULL,0.00,0.00,0.00,1,0,0,'Engagements et garanties',1,'2025-09-28 18:17:44.000000','2025-09-28 16:17:44'),(49,2,'CM10131996','Hamadou Zourmba','PASSIF','CLASSE_1',34,0.00,20000000.00,17000000.00,0,1,1,'Compte de l\'actionnaire Hamadou Zourmba',1,'2025-09-28 17:15:54.430800','2025-10-05 16:51:10'),(50,2,'CM1013199995','Abba Moussa','PASSIF','CLASSE_1',34,0.00,20000000.00,21919322.00,0,1,1,'Compte de l\'actionnaire Abba Moussa',1,'2025-09-28 20:38:02.002034','2025-10-05 16:53:08'),(51,2,'CM10131998','Hamaoua Paul','PASSIF','CLASSE_1',34,0.00,20000000.00,20000000.00,0,1,1,'Compte de l\'actionnaire Hamaoua Paul',1,'2025-09-28 20:38:44.442268','2025-10-05 16:51:51'),(52,2,'CM10131997','Iliyassou Dandjouma','PASSIF','CLASSE_1',34,0.00,30000000.00,30000000.00,0,1,1,'Compte de l\'actionnaire Iliyassou Dandjouma',1,'2025-09-28 20:39:29.034146','2025-10-05 16:51:32'),(53,2,'CM101319990','Danki Franklin','PASSIF','CLASSE_1',34,0.00,10000000.00,10000000.00,0,1,1,'',1,'2025-09-28 20:49:07.593843','2025-10-05 16:52:06'),(54,2,'CM1013199996','Djakari Yaouba','PASSIF','CLASSE_1',34,0.00,20000000.00,13000000.00,0,1,1,'',1,'2025-09-28 20:49:31.608998','2025-10-05 16:53:22'),(55,2,'CM1013199997','Hayatou El Hadji Souley','PASSIF','CLASSE_1',34,0.00,20000000.00,6000000.00,0,1,1,'',1,'2025-09-28 20:50:03.321162','2025-10-05 16:53:34'),(56,2,'CM101319999992','Dembe Jean Claude','PASSIF','CLASSE_1',34,0.00,20000000.00,20003959.00,0,1,1,'',1,'2025-09-28 20:50:29.193441','2025-10-05 16:54:39'),(57,2,'CM1013199998','Oumarou Gaston','PASSIF','CLASSE_1',34,0.00,30000000.00,30254409.00,0,1,1,'',1,'2025-09-28 20:50:52.209121','2025-10-05 16:53:44'),(58,2,'CM1013199999','Adam Teichert Moussa','PASSIF','CLASSE_1',34,0.00,20000000.00,20001366.00,0,1,1,'',1,'2025-09-28 20:51:20.689230','2025-10-05 16:53:56'),(59,2,'CM101319994','Didi Aissatou Marie-Paule','PASSIF','CLASSE_1',34,0.00,20000000.00,20000000.00,0,1,1,'',1,'2025-09-28 20:51:52.850197','2025-10-05 16:52:18'),(60,2,'CM10131999990','Saliou Zourmba','PASSIF','CLASSE_1',34,0.00,10000000.00,6000000.00,0,1,1,'',1,'2025-09-28 20:52:28.137158','2025-10-05 16:54:08'),(61,2,'CM101323','Dynamique SARL','PASSIF','CLASSE_1',34,0.00,20000000.00,20000000.00,0,1,1,'Représenté par Hayatou Bassoro',1,'2025-09-28 20:53:18.801043','2025-10-05 16:54:51'),(62,2,'CM1013199993','Abba Alim','PASSIF','CLASSE_1',34,0.00,20000000.00,5000000.00,0,1,1,'',1,'2025-09-28 20:54:06.537001','2025-10-05 16:52:44'),(63,2,'CM10131999991','Ismaila Bello','PASSIF','CLASSE_1',34,0.00,20000000.00,6000000.00,0,1,1,'',1,'2025-09-28 20:54:31.897133','2025-10-05 16:54:17'),(64,2,'CM1013199994','Saly Rayhana','PASSIF','CLASSE_1',34,0.00,20000000.00,5000000.00,0,1,1,'',1,'2025-09-28 20:55:03.353138','2025-10-05 16:52:58'),(65,2,'CM101319995','Iliassa Ibrahima','PASSIF','CLASSE_1',32,0.00,20000000.00,20000000.00,0,1,1,'',1,'2025-09-28 20:55:28.913041','2025-10-03 12:29:56'),(66,2,'CM101319996','Mohamadou Abbo','PASSIF','CLASSE_1',32,0.00,10000000.00,10000000.00,0,1,1,'Mohamadou Abbo',1,'2025-09-28 20:55:55.400917','2025-10-03 12:31:19'),(67,2,'CM101324','Sarkifada, Socièté','PASSIF','CLASSE_1',33,0.00,22500000.00,22500000.00,0,1,1,'Représenté par Mohamadou Dewa',1,'2025-09-28 20:56:53.592923','2025-10-03 13:04:12'),(68,2,'CM101319997','Abdourahaman','PASSIF','CLASSE_1',32,0.00,10000000.00,10000000.00,0,1,1,'',1,'2025-09-28 20:57:16.185349','2025-10-03 12:32:49'),(69,2,'CM10131999992','Abdoullahi Aboubakar','PASSIF','CLASSE_1',32,0.00,20000000.00,19973326.00,0,1,1,'',1,'2025-09-28 20:57:46.816923','2025-10-03 12:18:47'),(70,2,'CM101319998','Oumoul Oumarou','PASSIF','CLASSE_1',32,0.00,10000000.00,10000000.00,0,1,1,'Oumoul Oumarou epse Bassoro',1,'2025-09-28 20:58:27.872936','2025-10-03 12:34:40'),(71,2,'CM1013199991','Nankam Wouanko Christelle','PASSIF','CLASSE_1',32,0.00,5000000.00,5000000.00,0,1,1,'Nankam Wouanko Christelle (Mme Dembé)',1,'2025-09-28 20:59:25.673070','2025-10-03 12:11:57'),(72,2,'CM101319999','Astadicko Iyawa','PASSIF','CLASSE_1',32,0.00,10000000.00,10000000.00,0,1,1,'Astadicko Iyawa epse Yaya Sani',1,'2025-09-28 21:00:09.560521','2025-10-03 12:36:03'),(73,2,'CM1013199990','Saly Oumarou','PASSIF','CLASSE_1',32,0.00,20000000.00,2000000.00,0,1,1,'',1,'2025-09-28 21:00:50.240561','2025-10-03 12:37:27'),(74,2,'CM10131999993','Mohamadou Issa','PASSIF','CLASSE_1',32,0.00,20000000.00,12000000.00,0,1,1,'',1,'2025-09-28 21:01:18.760452','2025-10-03 12:49:40'),(75,2,'CM10131999994','Abbo Aboubakar','PASSIF','CLASSE_1',32,0.00,21000000.00,11250000.00,0,1,1,'',1,'2025-09-28 21:01:38.672532','2025-10-03 12:51:14'),(76,2,'CM10131999995','Fodoue Boukar','PASSIF','CLASSE_1',32,0.00,10500000.00,10500000.00,0,1,1,'',1,'2025-09-30 07:47:16.055335','2025-10-03 12:52:39'),(77,2,'CM10131999996','Lekeufack Jean Bruno','PASSIF','CLASSE_1',32,0.00,10500000.00,10500000.00,0,1,1,'',1,'2025-09-30 07:48:20.575174','2025-10-03 12:54:24'),(78,2,'CM10131999997','Fadimatou Lailla Ngomna','PASSIF','CLASSE_1',32,0.00,15000000.00,10000000.00,0,1,1,'',1,'2025-09-30 07:49:29.237697','2025-10-03 12:55:43'),(79,2,'CM10131999998','Djapo Yogwa Serges','PASSIF','CLASSE_1',32,0.00,10500000.00,8300000.00,0,1,1,'',1,'2025-09-30 07:50:28.397067','2025-10-03 12:57:22'),(80,2,'CM10131999999','Nyabata Assalé Sanda Miguel','PASSIF','CLASSE_1',32,0.00,10500000.00,10500000.00,0,1,1,'',1,'2025-09-30 07:51:17.944697','2025-10-03 12:59:11'),(81,2,'CM101319999990','Aissatou Issa','PASSIF','CLASSE_1',32,0.00,10500000.00,9480000.00,0,1,1,'',1,'2025-09-30 07:52:18.334008','2025-10-03 13:00:35'),(82,2,'CM101319999991','Mengu Ma Tooh','PASSIF','CLASSE_1',32,0.00,20000000.00,9000000.00,0,1,1,'',1,'2025-09-30 07:53:00.862670','2025-10-03 13:01:54');
/*!40000 ALTER TABLE `compte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `entreprises`
--

LOCK TABLES `entreprises` WRITE;
/*!40000 ALTER TABLE `entreprises` DISABLE KEYS */;
INSERT INTO `entreprises` (`id`, `raisonSociale`, `siret`, `siren`, `numeroTVA`, `adresse`, `codePostal`, `ville`, `pays`, `telephone`, `email`, `siteWeb`, `formeJuridique`, `capitalSocial`, `date_creation`, `active`) VALUES (1,'Polyclinique Exemple France',NULL,NULL,NULL,'123 Rue de la Sant├®, 75001 Paris',NULL,NULL,'France','01.23.45.67.89','contact@polyclinique-france.fr',NULL,'SARL',0,'2025-09-28 12:48:36.000000',1),(2,'Polyclinique Exemple Cameroun',NULL,NULL,NULL,'Avenue Charles de Gaulle, Douala',NULL,NULL,'Cameroun','+237 6 12 34 56 78','contact@polyclinique-cameroun.cm',NULL,'SARL',0,'2025-09-28 12:48:36.000000',1);
/*!40000 ALTER TABLE `entreprises` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `utilisateurs`
--

LOCK TABLES `utilisateurs` WRITE;
/*!40000 ALTER TABLE `utilisateurs` DISABLE KEYS */;
INSERT INTO `utilisateurs` (`id`, `nomUtilisateur`, `motDePasse`, `nom`, `prenom`, `email`, `role`, `actif`, `dateCreation`, `derniereConnexion`, `entreprise_id`) VALUES (1,'admin_france','admin','Admin','France','admin.france@comptabilite.com','ADMINISTRATEUR',1,'2025-09-28 12:48:36.000000','2025-10-03 18:29:08.195197',1),(2,'admin_cameroun','admin','Admin','Cameroun','admin.cameroun@comptabilite.com','ADMINISTRATEUR',1,'2025-09-28 12:48:36.000000','2025-10-06 10:08:53.577975',2),(3,'comptable_france','comptable','Comptable','France','comptable.france@comptabilite.com','COMPTABLE',1,'2025-09-28 12:48:36.000000',NULL,1),(4,'comptable_cameroun','comptable','Comptable','Cameroun','comptable.cameroun@comptabilite.com','COMPTABLE',1,'2025-09-28 12:48:36.000000',NULL,2);
/*!40000 ALTER TABLE `utilisateurs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `exercices`
--

LOCK TABLES `exercices` WRITE;
/*!40000 ALTER TABLE `exercices` DISABLE KEYS */;
INSERT INTO `exercices` (`id`, `date_cloture`, `date_creation`, `date_debut`, `date_fin`, `libelle`, `statut`, `entreprise_id`) VALUES (1,NULL,'2025-09-28 10:49:00.188148','2025-01-01','2025-12-31','Exercice 2025','OUVERT',1),(2,NULL,'2025-10-02 00:13:06.000000','2025-01-01','2025-12-31','Exercice 2025','OUVERT',2);
/*!40000 ALTER TABLE `exercices` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-06 12:53:35
