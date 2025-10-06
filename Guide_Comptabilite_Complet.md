# Guide de Comptabilité - Système Comptable Multi-Devises

## Table des Matières

1. [Vue d'ensemble du système](#vue-densemble-du-système)
2. [Configuration des entreprises et devises](#configuration-des-entreprises-et-devises)
3. [Plan comptable et numérotation](#plan-comptable-et-numérotation)
4. [Gestion du capital et des actionnaires](#gestion-du-capital-et-des-actionnaires)
5. [Écritures comptables](#écritures-comptables)
6. [États financiers](#états-financiers)
7. [Troubleshooting et bonnes pratiques](#troubleshooting-et-bonnes-pratiques)

---

## Vue d'ensemble du système

### Caractéristiques principales

- **Multi-devises** : Support Euro (€) et Franc CFA (FCFA)
- **Multi-normes** : Comptabilité française et OHADA/Cameroun
- **Gestion des actionnaires** : Suivi détaillé des souscriptions et libérations
- **États financiers automatisés** : Balance, Grand Livre, Bilan, Compte de Résultat

### Architecture des données

```
Entreprise
├── Utilisateurs
├── Exercices comptables
├── Plan comptable (Comptes)
│   ├── Comptes principaux
│   └── Sous-comptes (actionnaires, etc.)
├── Journaux
└── Écritures comptables
    └── Lignes d'écriture
```

---

## Configuration des entreprises et devises

### Détection automatique de la devise

Le système détecte automatiquement la devise et les normes comptables :

#### Entreprises OHADA (FCFA)
- **Pays** : Cameroun, Sénégal, Côte d'Ivoire, etc.
- **Devise** : Franc CFA (FCFA)
- **Format numérotation** : CMxxxxxxx
- **Séparateur décimal** : Espace (ex: 1 000 000)

#### Entreprises Européennes (Euro)
- **Pays** : France, Allemagne, Belgique, etc.
- **Devise** : Euro (€)
- **Format numérotation** : xxxxxxx
- **Séparateur décimal** : Virgule (ex: 1 000 000,00)

### Configuration dans l'application

```java
// La détection se fait automatiquement via CurrencyService
boolean isOHADA = currencyService.isOHADAEntreprise();
String format = currencyService.getAmountInputFormat();
```

---

## Plan comptable et numérotation

### Numérotation automatique

#### Format OHADA (Cameroun)
```
CM + Classe + Numéro séquentiel
Exemples :
- CM101000 : Capital social
- CM101001 : Actionnaire Oumarou Gaston
- CM521000 : Banque principale
```

#### Format Français
```
Classe + Numéro séquentiel
Exemples :
- 101000 : Capital social
- 101001 : Actionnaire 1
- 512000 : Banque
```

### Classes comptables

| Classe | Description | Type | Exemples |
|--------|-------------|------|----------|
| 1 | Comptes de capitaux | Passif | Capital, Réserves |
| 2 | Immobilisations | Actif | Matériel, Brevets |
| 3 | Stocks | Actif | Marchandises |
| 4 | Comptes de tiers | Actif/Passif | Clients, Fournisseurs |
| 5 | Comptes financiers | Actif | Banque, Caisse |
| 6 | Charges | Résultat | Achats, Salaires |
| 7 | Produits | Résultat | Ventes, Prestations |

### Création automatique des sous-comptes

```java
// Le système génère automatiquement les numéros
String nextNumber = compteDAO.getNextNumeroInHierarchy(parentPrefix, entrepriseId);
```

---

## Gestion du capital et des actionnaires

### Stratégie recommandée : Écritures comptables

**Nouvelle approche (recommandée) :**
1. Créer les sous-comptes actionnaires à solde zéro
2. Enregistrer chaque libération par écriture comptable

**Avantages :**
- ✅ Traçabilité complète des mouvements
- ✅ Historique des libérations
- ✅ Facilite les audits
- ✅ Respect des normes comptables

### Structure des comptes capital

```
CM101000 - Capital Social (compte principal)
├── CM101001 - Oumarou Gaston
├── CM101002 - Dembe Jean Claude
├── CM101003 - Abba Moussa
└── CM101004 - Adam Teichert Moussa
```

### Écritures type pour libération

```
Date: [Date de virement]
Libellé: Libération capital - [Nom actionnaire]

Débit  : CM521000 (Banque)          [Montant versé]
Crédit : CM101001 (Actionnaire)     [Montant versé]
```

### Gestion des cas particuliers

#### Actionnaire déficitaire (doit encore payer)
```
Solde du sous-compte : Débiteur (+)
→ Apparaît en CRÉANCES au bilan (Actif)
```

#### Actionnaire excédentaire (a trop payé)
```
Solde du sous-compte : Créditeur (-)
→ Apparaît en DETTES au bilan (Passif)
```

---

## Écritures comptables

### Principes de base

#### Équilibre obligatoire
```
Σ Débits = Σ Crédits
```

#### Numérotation automatique
- Format : `[ANNÉE]-[JOURNAL]-[NUMÉRO]`
- Exemple : `2024-VT-001`

### Types d'écritures courantes

#### 1. Libération de capital
```
Débit  : 512000 (Banque)
Crédit : 101001 (Actionnaire)
```

#### 2. Achat d'immobilisation
```
Débit  : 215000 (Matériel médical)
Débit  : 445660 (TVA déductible)
Crédit : 404000 (Fournisseur)
```

#### 3. Vente de prestation
```
Débit  : 411000 (Client)
Crédit : 706000 (Prestations)
Crédit : 445711 (TVA collectée)
```

### Validation des écritures

Le système vérifie automatiquement :
- ✅ Équilibre débit/crédit
- ✅ Comptes existants
- ✅ Montants > 0
- ✅ Date dans l'exercice

---

## États financiers

### 1. Balance

**Colonnes affichées :**
- Numéro de compte
- Libellé
- Total Débit
- Total Crédit
- Solde Débiteur
- Solde Créditeur

**Utilisation :** Vérification de l'équilibre comptable

### 2. Grand Livre

**Informations par compte :**
- Historique de tous les mouvements
- Solde cumule progressive
- Filtrage par période

**Utilisation :** Analyse détaillée d'un compte

### 3. Bilan

#### Structure Actif/Passif

**ACTIF :**
- Immobilisations (Classe 2)
- Actif circulant (Classe 3)
- Créances (Actions non libérées)
- Trésorerie (Banque, Caisse)

**PASSIF :**
- Capitaux propres (Capital + Résultat)
- Dettes (Fournisseurs + Sur-libérations actionnaires)

#### Logique de consolidation

```java
// Comptes avec sous-comptes : utilise la consolidation
BigDecimal soldeConsolide = compte.getSoldeDebiteurConsolide();

// Évite le double comptage parent/enfant
boolean hasSubAccounts = compte.getSousComptes() != null && !compte.getSousComptes().isEmpty();
```

#### Traitement spécifique du capital

1. **Capital principal (CM101000)** → Passif (576 000 000)
2. **Sous-comptes actionnaires :**
   - Solde débiteur → Créances (actions non libérées)
   - Solde créditeur → Dettes (sur-libérations)

### 4. Compte de Résultat

**Structure :**
- Chiffre d'affaires (Classe 7)
- Charges d'exploitation (Classe 6)
- Résultat net = Produits - Charges

**Note :** Vide en phase de constitution (normal)

---

## Troubleshooting et bonnes pratiques

### Problèmes courants et solutions

#### 1. Bilan déséquilibré
**Cause :** Double comptage parent/enfant
**Solution :**
```java
// Exclure les comptes parents qui ont des sous-comptes
boolean hasSubAccounts = compte.getSousComptes() != null && !compte.getSousComptes().isEmpty();
if (!hasSubAccounts) {
    // Traiter seulement les comptes finaux
}
```

#### 2. Compte de résultat qui ne s'exécute pas
**Cause :** Problème d'extraction de classe pour comptes OHADA
**Solution :**
```java
// Gestion des deux formats
if (numeroCompte.startsWith("CM") && numeroCompte.length() > 2) {
    return Integer.parseInt(numeroCompte.substring(2, 3)); // CM6xxxx → 6
} else {
    return Integer.parseInt(numeroCompte.substring(0, 1)); // 6xxxx → 6
}
```

#### 3. Créances mal calculées
**Cause :** Confusion entre logique globale et détaillée
**Solution :** Traiter chaque actionnaire individuellement
```java
if (soldeNet.compareTo(BigDecimal.ZERO) > 0) {
    creances = creances.add(soldeNet); // Doit encore payer
} else if (soldeNet.compareTo(BigDecimal.ZERO) < 0) {
    dettes = dettes.add(soldeNet.abs()); // A trop payé
}
```

### Bonnes pratiques

#### 1. Saisie des écritures
- ✅ Toujours équilibrer débit = crédit
- ✅ Utiliser des libellés explicites
- ✅ Vérifier les comptes utilisés
- ✅ Respecter la chronologie

#### 2. Gestion des actionnaires
- ✅ Créer un sous-compte par actionnaire
- ✅ Enregistrer chaque virement par écriture
- ✅ Éviter les soldes "magiques" sans justification
- ✅ Documenter les sur-libérations

#### 3. Clôture d'exercice
- ✅ Vérifier l'équilibre de la balance
- ✅ Contrôler la cohérence bilan/balance
- ✅ Justifier tous les écarts
- ✅ Sauvegarder les états avant clôture

#### 4. Maintenance du système
- ✅ Sauvegardes régulières de la base
- ✅ Contrôles d'intégrité périodiques
- ✅ Formation des utilisateurs
- ✅ Documentation des procédures spécifiques

---

## Évolution du système

### Phases d'utilisation

1. **Constitution** → Capital, actionnaires, trésorerie
2. **Investissement** → Achats matériel, aménagements (charges)
3. **Exploitation** → Facturation clients (produits)
4. **Maturité** → Gestion complète actif/passif/résultat

### Fonctionnalités avancées

- [ ] Gestion de la TVA
- [ ] Immobilisations et amortissements
- [ ] Paie et charges sociales
- [ ] Analyse financière
- [ ] Budgets et prévisions
- [ ] Consolidation multi-sociétés

---

## Support et contact

Pour toute question ou amélioration du système :
1. Consulter les logs de l'application
2. Vérifier ce guide de référence
3. Tester sur données d'exemple
4. Documenter les cas particuliers

---

*Guide créé le : {{ date }}*
*Version : 1.0*
*Dernière mise à jour : {{ date }}*