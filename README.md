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

### Gestion Multi-utilisateurs
- Système d'authentification sécurisé
- Rôles utilisateurs : Administrateur, Comptable, Assistant, Consultant
- Gestion des droits d'accès

### Administration
- Gestion des exercices comptables
- Paramétrage de l'entreprise
- Sauvegarde et restauration

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
│   ├── UtilisateurDAO.java
│   ├── CompteDAO.java
│   └── EcritureComptableDAO.java
├── service/            # Services métier
│   └── AuthenticationService.java
├── view/               # Contrôleurs JavaFX
│   ├── LoginController.java
│   └── MainController.java
├── util/               # Utilitaires
│   └── HibernateUtil.java
└── ComptabiliteApplication.java

src/main/resources/
├── fxml/               # Fichiers FXML pour l'interface
│   ├── login.fxml
│   └── main.fxml
└── hibernate.cfg.xml   # Configuration Hibernate
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

## Support

Pour toute question ou problème, consultez la documentation ou contactez l'équipe de développement.

---

**Note** : Ce logiciel est conforme aux standards comptables français et peut être adapté pour d'autres réglementations comptables.