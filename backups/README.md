# Système de Backup Intelligent - Base de Données Comptable

## 🎯 **Objectif**
Créer des backups optimisés pour Git/GitHub tout en conservant des backups complets locaux.

## 📁 **Types de Backups**

### ✅ **Compatible GitHub (< 10 MB)**
- `schema_structure_*.sql` - Structure DDL complète (tables, index, contraintes)
- `reference_data_*.sql` - Données de référence (comptes, entreprises, utilisateurs)
- `github_ready_*.sql` - Backup optimisé = structure + référence

### 🗂️ **Usage Local Uniquement**
- `transaction_data_*.sql` - Écritures comptables, factures (peut être gros)
- `full_backup_*.sql` - Backup complet avec toutes les données

## 🔧 **Utilisation**

### Créer un Backup
```bash
backup-database.bat
```
**Résultat :**
- Crée tous les types de backup avec timestamp
- Vérifie les tailles automatiquement
- Donne des recommandations pour Git

### Restaurer la Base
```bash
restore-database.bat
```
**Options :**
1. **Rapide** - Structure + données de référence (idéal pour développement)
2. **Complète** - Inclut les données transactionnelles
3. **Depuis backup complet** - Restauration totale
4. **Personnalisée** - Choisir un fichier spécifique

## 📊 **Stratégie Git**

### ✅ **À Committer**
```bash
git add backups/schema_structure_*.sql
git add backups/reference_data_*.sql
git add backups/github_ready_*.sql
```

### ❌ **Exclu de Git (automatique)**
- `backups/full_backup_*.sql`
- `backups/transaction_data_*.sql`

## 🚀 **Avantages**

1. **Pas de limite GitHub** - Les gros fichiers restent locaux
2. **Historique propre** - Structure et référence versionnées
3. **Déploiement facile** - Backup GitHub-ready pour nouvelle installation
4. **Backup complet** - Sécurité locale préservée
5. **Automatique** - Vérification des tailles et recommandations

## 📋 **Configuration .gitignore**
```gitignore
# Database backups - Configuration intelligente
backups/full_backup_*.sql         # Trop gros pour Git
backups/transaction_data_*.sql    # Potentiellement gros
!backups/schema_structure_*.sql   # Structure (OK pour Git)
!backups/reference_data_*.sql     # Référence (OK pour Git)
!backups/github_ready_*.sql       # Optimisé GitHub
```

## 🔄 **Workflow Recommandé**

### Développement Quotidien
```bash
# Backup rapide
backup-database.bat
git add backups/github_ready_[date].sql
git commit -m "Update database schema and reference data"
```

### Backup Sécurité Complet
```bash
# Backup complet (reste local)
backup-database.bat
# Seuls les fichiers compatibles GitHub seront committés
```

### Déploiement Nouvelle Installation
```bash
# Utiliser github_ready_*.sql ou
# Restaurer structure + référence séparément
restore-database.bat  # Option 1
```

## 📈 **Monitoring**
Le script affiche automatiquement :
- Taille de chaque backup
- Compatibilité GitHub (< 100 MB)
- Recommandations de commit