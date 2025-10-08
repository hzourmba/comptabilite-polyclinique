# Logiciel de Comptabilité d'Entreprise

Un logiciel de comptabilité complet développé en Java avec JavaFX et MySQL, conçu pour les entreprises souhaitant gérer leur comptabilité de manière professionnelle.

## Fonctionnalités

### Gestion des Comptes
- Plan comptable complet avec classes de comptes français
- Comptes principaux et sous-comptes
- Gestion des soldes débiteurs/créditeurs

### Écritures Comptables
- Saisie d'écritures comptables avec validation
- Numérotation automatique des écritures
- Statuts : Brouillon, Validée, Clôturée
- Journal comptable avec filtres

### Facturation
- Gestion des clients et fournisseurs
- Création et suivi des factures
- Calcul automatique de la TVA
- Différents statuts de factures

### Rapports Comptables
- Grand livre
- Balance générale
- Bilan comptable
- Compte de résultat
- Déclarations TVA

### Gestion Multi-utilisateurs ✨ **NOUVEAU**
- **Interface complète de gestion des utilisateurs** : Création, modification, activation/désactivation
- **Système de rôles avancé** : 4 niveaux de permissions (Administrateur, Comptable, Assistant Comptable, Consultant)
- **Sécurité renforcée** : Authentification hybride, contrôle d'accès basé sur les rôles
- **Profil utilisateur** : Modification des informations personnelles et mot de passe
- **Protection multicouche** : Interface + backend + base de données

### Administration ✨ **AMÉLIORÉ**
- **Initialisation automatique d'entreprise** : Création automatique de l'exercice, plan comptable et utilisateur admin
- **Détection intelligente de devise** : Adaptation automatique selon le contexte utilisateur
- **Gestion des exercices comptables**
- **Paramétrage de l'entreprise**
- **Sauvegarde et restauration automatisées**

### Multi-devises et Multi-normes
- **Support Euro (€) et Franc CFA (FCFA)**
- **Plans comptables français et OHADA/Cameroun**
- **Numérotation automatique adaptée** (avec ou sans préfixe CM)
- **Détection contextuelle** selon l'utilisateur connecté

## Prérequis

- **Java 17 ou supérieur**
- **MySQL 8.0 ou supérieur**
- **Maven 3.6 ou supérieur**

## Installation

### 1. Accéder au projet
```bash
cd C:\Anwendung\Comptabilite\project
```

**Note** : Le projet est déjà créé localement. Si vous souhaitez le versionner avec Git :
```bash
git init
git add .
git commit -m "Initial commit - Logiciel de comptabilité"
```

### 2. Configurer MySQL

Connectez-vous à MySQL et exécutez le script de configuration :

```bash
mysql -u root -p < database_setup.sql
```

Ou manuellement :
```sql
CREATE DATABASE IF NOT EXISTS comptabilite_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'comptabilite_user'@'localhost' IDENTIFIED BY 'comptabilite_pass';
GRANT ALL PRIVILEGES ON comptabilite_db.* TO 'comptabilite_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configurer la base de données

Modifiez le fichier `src/main/resources/hibernate.cfg.xml` si nécessaire pour adapter les paramètres de connexion :

```xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/comptabilite_db?useSSL=false&amp;serverTimezone=UTC</property>
<property name="hibernate.connection.username">comptabilite_user</property>
<property name="hibernate.connection.password">comptabilite_pass</property>
```

### 4. Compiler et lancer l'application

```bash
# Compiler le projet
mvn clean compile

# Lancer l'application
mvn javafx:run
```

## Première utilisation

### Création du premier utilisateur administrateur

Au premier lancement, la base de données sera automatiquement créée par Hibernate. Vous devrez créer manuellement le premier utilisateur administrateur en base :

```sql
USE comptabilite_db;

-- Créer une entreprise
INSERT INTO entreprises (raison_sociale, forme_juridique, date_creation, active)
VALUES ('Ma Première Entreprise', 'SARL', NOW(), true);

-- Créer l'utilisateur administrateur (mot de passe: "admin" hashé)
INSERT INTO utilisateurs (nom_utilisateur, mot_de_passe, nom, prenom, email, role, actif, date_creation, entreprise_id)
VALUES ('admin', '92668751', 'Administrateur', 'Système', 'admin@entreprise.com', 'ADMINISTRATEUR', true, NOW(), 1);
```

### Connexion
- **Nom d'utilisateur** : `admin`
- **Mot de passe** : `admin`

## Structure du Projet

```
src/main/java/com/comptabilite/
├── model/              # Entités JPA/Hibernate
│   ├── Utilisateur.java
│   ├── Entreprise.java
│   ├── Compte.java
│   ├── EcritureComptable.java
│   ├── LigneEcriture.java
│   ├── Exercice.java
│   ├── Client.java
│   ├── Fournisseur.java
│   └── Facture.java
├── dao/                # Data Access Objects
│   ├── BaseDAO.java
│   ├── UtilisateurDAO.java           ✨ Sécurité renforcée
│   ├── CompteDAO.java
│   └── EcritureComptableDAO.java
├── service/            # Services métier
│   ├── AuthenticationService.java
│   ├── EntrepriseInitializationService.java   ✨ NOUVEAU
│   └── CurrencyService.java
├── view/               # Contrôleurs JavaFX
│   ├── LoginController.java
│   ├── MainController.java            ✨ Contrôle d'accès
│   ├── UserManagementController.java  ✨ NOUVEAU
│   ├── UserEditDialogController.java  ✨ NOUVEAU
│   ├── UserProfileDialogController.java ✨ NOUVEAU
│   └── EntrepriseDialogController.java ✨ Amélioré
├── util/               # Utilitaires
│   └── HibernateUtil.java
└── ComptabiliteApplication.java

src/main/resources/
├── fxml/               # Fichiers FXML pour l'interface
│   ├── login.fxml
│   ├── main_simple.fxml        ✨ Contrôle permissions
│   ├── user-management.fxml    ✨ NOUVEAU
│   ├── user-edit-dialog.fxml   ✨ NOUVEAU
│   ├── user-profile-dialog.fxml ✨ NOUVEAU
│   └── entreprise-dialog.fxml  ✨ Amélioré
├── hibernate.cfg.xml   # Configuration Hibernate
└── css/                # Styles CSS

Documentation/
├── Guide_Comptabilite_Complet.md    ✨ Mis à jour
├── Guide_Comptabilite_Complet.html  ✨ Mis à jour
└── backups/            # Sauvegardes base de données
```

## Technologies Utilisées

- **Java 17** - Langage de programmation
- **JavaFX 21** - Framework d'interface graphique
- **Hibernate 6.2** - ORM pour la persistance
- **MySQL 8.0** - Base de données
- **Maven** - Gestionnaire de dépendances
- **SLF4J + Logback** - Logging

## Développement

### Ajouter de nouvelles fonctionnalités

1. **Modèles** : Créer les entités dans le package `model`
2. **DAO** : Implémenter les couches d'accès aux données
3. **Services** : Développer la logique métier
4. **Contrôleurs** : Créer les contrôleurs JavaFX
5. **FXML** : Concevoir les interfaces utilisateur

### Tests

```bash
mvn test
```

### Packaging

```bash
# Créer un JAR exécutable
mvn package

# Le fichier sera généré dans target/
java -jar target/comptabilite-entreprise-1.0.0.jar
```

## Licence

Ce projet est développé à des fins éducatives et professionnelles.

## Nouvelles Fonctionnalités (Octobre 2025)

### 🎯 Gestion Complète des Utilisateurs
- **Interface de gestion** : `Menu → Administration → Gestion des utilisateurs`
- **Création d'utilisateurs** : Formulaire complet avec validation
- **Activation/Désactivation** : Contrôle des accès utilisateur
- **Double-clic** : Édition rapide des utilisateurs

### 🔐 Sécurité Renforcée
- **4 niveaux de rôles** : Administrateur, Comptable, Assistant Comptable, Consultant
- **Contrôle d'accès dynamique** : Menus adaptés selon les permissions
- **Protection multicouche** : Interface + backend + base de données
- **Authentification hybride** : Support mots de passe legacy et hashés

### 👤 Profil Utilisateur
- **Accès** : `Menu → Administration → Mon profil`
- **Modification** : Informations personnelles
- **Sécurité** : Changement de mot de passe sécurisé
- **Informations** : Dernière connexion, entreprise

### 🏢 Initialisation Automatique d'Entreprise
- **Détection intelligente** : Devise et plan comptable selon l'utilisateur
- **Création automatique** : Exercice, comptes de base, utilisateur admin
- **Support multi-normes** : Français (EUR) et OHADA/Cameroun (FCFA)
- **Comptes essentiels** : Capital, banque, clients, fournisseurs, etc.

### 📚 Documentation Mise à Jour
- **Guide complet** : `Guide_Comptabilite_Complet.md` et `.html`
- **Section sécurité** : Documentation complète des permissions
- **Exemples pratiques** : Code et configurations
- **Interface moderne** : Guide HTML avec nouveau design

## Support

Pour toute question ou problème, consultez la documentation mise à jour ou contactez l'équipe de développement.

## Dernière Mise à Jour

**Version** : Octobre 2025 - Gestion des utilisateurs et sécurité
**Commit** : Implement complete user management system with role-based security
**GitHub** : Synchronisé avec le repository

---

**Note** : Ce logiciel est conforme aux standards comptables français et OHADA/Cameroun, avec support multi-devises (EUR/FCFA).