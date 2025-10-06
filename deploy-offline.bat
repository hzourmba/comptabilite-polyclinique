@echo off
echo ====================================================
echo       DEPLOYMENT PACKAGE - COMPTABILITE OFFLINE
echo       Version: 1.4 - Multi-Currency Consolidation
echo       Date: %DATE%
echo ====================================================
echo.

REM Create deployment directory
if not exist "deployment-offline" mkdir deployment-offline
cd deployment-offline

echo [1/10] Cleaning previous deployment...
if exist "*" del /Q *
if exist "lib" rmdir /S /Q lib
if exist "src" rmdir /S /Q src
if exist "target" rmdir /S /Q target
if exist "dependencies" rmdir /S /Q dependencies
echo      ✓ Cleanup completed

echo.
echo [2/10] Copying main JAR file...
copy "..\target\comptabilite-1.0-SNAPSHOT.jar" "comptabilite.jar" >nul
if exist "comptabilite.jar" (
    echo      ✓ JAR file copied successfully
) else (
    echo      ✗ JAR file not found! Please run 'mvn clean package' first
    pause
    exit /b 1
)

echo.
echo [3/10] Copying dependency libraries...
mkdir lib
copy "..\target\lib\*" "lib\" >nul
if exist "lib\*.jar" (
    echo      ✓ Dependencies copied
) else (
    echo      ✗ Dependencies not found!
    pause
    exit /b 1
)

echo.
echo [4/10] Creating standalone dependencies package...
mkdir dependencies

REM Copy all required JARs to dependencies folder
echo Packaging JavaFX dependencies...
copy "lib\javafx-*.jar" "dependencies\" >nul 2>nul
echo Packaging Hibernate dependencies...
copy "lib\hibernate-*.jar" "dependencies\" >nul 2>nul
copy "lib\jakarta-*.jar" "dependencies\" >nul 2>nul
echo Packaging MySQL connector...
copy "lib\mysql-connector-*.jar" "dependencies\" >nul 2>nul
echo Packaging logging dependencies...
copy "lib\slf4j-*.jar" "dependencies\" >nul 2>nul
copy "lib\logback-*.jar" "dependencies\" >nul 2>nul
echo Packaging other core dependencies...
copy "lib\*.jar" "dependencies\" >nul 2>nul

echo      ✓ Standalone dependencies created

echo.
echo [5/10] Copying configuration files...
copy "..\src\main\resources\hibernate.cfg.xml" "hibernate.cfg.xml" >nul
copy "..\src\main\resources\application.properties" "application.properties" >nul 2>nul
echo      ✓ Configuration files copied

echo.
echo [6/10] Copying database files...
copy "..\src\main\resources\schema.sql" "schema.sql" >nul 2>nul
echo      ✓ Database schema copied

echo.
echo [7/10] Creating portable Java runtime (if available)...
REM Check if jlink is available for creating minimal JRE
where jlink >nul 2>nul
if %errorlevel% == 0 (
    echo Creating minimal Java runtime...
    jlink --add-modules java.base,java.desktop,java.sql,java.xml,java.logging,java.naming,java.management,java.security.jgss,java.instrument,java.compiler,jdk.unsupported --output jre --strip-debug --compress=2 --no-header-files --no-man-pages 2>nul
    if exist "jre" (
        echo      ✓ Portable Java runtime created
    ) else (
        echo      ⚠ Could not create portable runtime (will use system Java)
    )
) else (
    echo      ⚠ jlink not available (will use system Java)
)

echo.
echo [8/10] Creating startup scripts...

REM Create Windows startup script with offline capabilities
echo @echo off > start-comptabilite-offline.bat
echo echo ================================================= >> start-comptabilite-offline.bat
echo echo    COMPTABILITE - Offline Multi-Currency System >> start-comptabilite-offline.bat
echo echo    Version 1.4 - Enhanced Consolidation >> start-comptabilite-offline.bat
echo echo ================================================= >> start-comptabilite-offline.bat
echo echo. >> start-comptabilite-offline.bat
echo echo Checking for portable Java runtime... >> start-comptabilite-offline.bat
echo if exist "jre\bin\java.exe" ( >> start-comptabilite-offline.bat
echo     echo Using portable Java runtime >> start-comptabilite-offline.bat
echo     set JAVA_CMD=jre\bin\java.exe >> start-comptabilite-offline.bat
echo ) else ( >> start-comptabilite-offline.bat
echo     echo Using system Java >> start-comptabilite-offline.bat
echo     set JAVA_CMD=java >> start-comptabilite-offline.bat
echo     java -version >> start-comptabilite-offline.bat
echo     if errorlevel 1 ( >> start-comptabilite-offline.bat
echo         echo ERREUR: Java n'est pas installé ou non trouvé >> start-comptabilite-offline.bat
echo         echo Veuillez installer Java 11 ou supérieur >> start-comptabilite-offline.bat
echo         pause >> start-comptabilite-offline.bat
echo         exit /b 1 >> start-comptabilite-offline.bat
echo     ) >> start-comptabilite-offline.bat
echo ) >> start-comptabilite-offline.bat
echo echo. >> start-comptabilite-offline.bat
echo echo Setting up classpath with all dependencies... >> start-comptabilite-offline.bat
echo set CLASSPATH=comptabilite.jar >> start-comptabilite-offline.bat
echo for %%%%f in (dependencies\*.jar) do set CLASSPATH=%%CLASSPATH%%;%%%%f >> start-comptabilite-offline.bat
echo echo. >> start-comptabilite-offline.bat
echo echo Starting Comptabilite application... >> start-comptabilite-offline.bat
echo echo Classpath: %%CLASSPATH%% >> start-comptabilite-offline.bat
echo echo. >> start-comptabilite-offline.bat
echo %%JAVA_CMD%% -Xmx1024m -cp "%%CLASSPATH%%" com.comptabilite.Main >> start-comptabilite-offline.bat
echo if errorlevel 1 ( >> start-comptabilite-offline.bat
echo     echo. >> start-comptabilite-offline.bat
echo     echo ERREUR: Échec du démarrage de l'application >> start-comptabilite-offline.bat
echo     echo Vérifiez les logs ci-dessus pour plus de détails >> start-comptabilite-offline.bat
echo     pause >> start-comptabilite-offline.bat
echo ) >> start-comptabilite-offline.bat

REM Create Linux startup script
echo #!/bin/bash > start-comptabilite-offline.sh
echo echo "=================================================" >> start-comptabilite-offline.sh
echo echo "   COMPTABILITE - Offline Multi-Currency System" >> start-comptabilite-offline.sh
echo echo "   Version 1.4 - Enhanced Consolidation" >> start-comptabilite-offline.sh
echo echo "=================================================" >> start-comptabilite-offline.sh
echo echo >> start-comptabilite-offline.sh
echo echo "Checking for portable Java runtime..." >> start-comptabilite-offline.sh
echo if [ -f "jre/bin/java" ]; then >> start-comptabilite-offline.sh
echo     echo "Using portable Java runtime" >> start-comptabilite-offline.sh
echo     JAVA_CMD="./jre/bin/java" >> start-comptabilite-offline.sh
echo else >> start-comptabilite-offline.sh
echo     echo "Using system Java" >> start-comptabilite-offline.sh
echo     JAVA_CMD="java" >> start-comptabilite-offline.sh
echo     if ! command -v java ^\&^> /dev/null; then >> start-comptabilite-offline.sh
echo         echo "ERREUR: Java n'est pas installé" >> start-comptabilite-offline.sh
echo         echo "Veuillez installer Java 11 ou supérieur" >> start-comptabilite-offline.sh
echo         exit 1 >> start-comptabilite-offline.sh
echo     fi >> start-comptabilite-offline.sh
echo fi >> start-comptabilite-offline.sh
echo echo >> start-comptabilite-offline.sh
echo echo "Setting up classpath..." >> start-comptabilite-offline.sh
echo CLASSPATH="comptabilite.jar" >> start-comptabilite-offline.sh
echo for jar in dependencies/*.jar; do >> start-comptabilite-offline.sh
echo     CLASSPATH="$CLASSPATH:$jar" >> start-comptabilite-offline.sh
echo done >> start-comptabilite-offline.sh
echo echo >> start-comptabilite-offline.sh
echo echo "Starting Comptabilite application..." >> start-comptabilite-offline.sh
echo $JAVA_CMD -Xmx1024m -cp "$CLASSPATH" com.comptabilite.Main >> start-comptabilite-offline.sh

REM Make Linux script executable
echo chmod +x start-comptabilite-offline.sh > make-executable.sh

echo      ✓ Offline startup scripts created

echo.
echo [9/10] Creating comprehensive documentation...
copy "..\README.md" "README.md" >nul 2>nul
copy "..\Guide_Comptabilite_Complet.md" "Guide_Comptabilite_Complet.md" >nul 2>nul
copy "..\Guide_Comptabilite_Complet.html" "Guide_Comptabilite_Complet.html" >nul 2>nul

REM Create detailed offline deployment instructions
echo # COMPTABILITE - OFFLINE DEPLOYMENT PACKAGE > DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ## Version >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo Version: 1.4 - Multi-Currency Consolidation with Enhanced Shareholder Management >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo Date: %DATE% >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo Package Type: **COMPLETE OFFLINE DEPLOYMENT** >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ## 🎯 Nouvelles fonctionnalités de cette version >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - ✅ **Support multi-devises** (Euro/FCFA) avec détection automatique >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - ✅ **Gestion avancée des actionnaires** et libérations de capital >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - ✅ **Consolidation intelligente** avec évitement du double comptage >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - ✅ **Bilan équilibré** avec traitement des sur-libérations actionnaires >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - ✅ **Correction compte de résultat** pour comptes OHADA (CM6xxxx, CM7xxxx) >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - ✅ **Guide de comptabilité complet** avec troubleshooting >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - ✅ **Package offline complet** avec toutes les dépendances >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ## 📦 Contenu du package >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ```text >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo deployment-offline/ >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ├── comptabilite.jar                    # Application principale >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ├── dependencies/                       # Toutes les dépendances >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo │   ├── javafx-*.jar                   # Interface graphique >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo │   ├── hibernate-*.jar                # ORM base de données >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo │   ├── mysql-connector-*.jar          # Connecteur MySQL >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo │   └── ... (toutes autres dépendances) >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ├── jre/                               # Java portable (si créé) >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ├── start-comptabilite-offline.*       # Scripts de démarrage >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ├── hibernate.cfg.xml                  # Configuration BDD >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ├── schema.sql                         # Schéma de base de données >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ├── Guide_Comptabilite_Complet.*       # Documentation complète >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo └── DEPLOYMENT-INSTRUCTIONS-OFFLINE.md # Ce fichier >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ``` >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ## 🔧 Installation >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ### Prérequis minimaux >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - **Aucune connexion Internet requise** ✅ >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Java 11+ (ou utiliser le JRE portable inclus) >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Base de données MySQL/MariaDB >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - 2 GB d'espace disque libre >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ### Étapes d'installation >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo 1. **Extraire** ce package sur la machine cible >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo 2. **Configurer la base de données** dans `hibernate.cfg.xml` >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo 3. **Créer la base** avec `schema.sql` >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo 4. **Lancer** avec `start-comptabilite-offline.bat` (Windows) ou `.sh` (Linux) >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ## 🗄️ Configuration de la base de données >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo Modifier `hibernate.cfg.xml`: >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ```xml >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ^<property name="connection.url"^>jdbc:mysql://localhost:3306/comptabilite^</property^> >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ^<property name="connection.username"^>comptabilite_user^</property^> >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ^<property name="connection.password"^>votre_mot_de_passe^</property^> >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ``` >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ## 🚀 Démarrage >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ### Windows >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ```batch >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo start-comptabilite-offline.bat >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ``` >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ### Linux/Mac >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ```bash >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo chmod +x start-comptabilite-offline.sh >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ./start-comptabilite-offline.sh >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ``` >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ## 📚 Documentation >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - **Guide_Comptabilite_Complet.html** : Documentation complète (ouvrir dans navigateur) >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - **Guide_Comptabilite_Complet.md** : Version Markdown >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Contient : Setup, usage, troubleshooting, bonnes pratiques >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ## 🔍 Troubleshooting >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ### Problèmes courants >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo **Application ne démarre pas :** >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Vérifier Java avec `java -version` >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Vérifier les logs dans la console >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - S'assurer que la base de données est accessible >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo **Erreur de base de données :** >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Vérifier `hibernate.cfg.xml` >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Tester la connexion MySQL >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Importer `schema.sql` si nécessaire >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo **Bilan déséquilibré :** >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Consulter le guide de comptabilité section "Troubleshooting" >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Vérifier les écritures comptables dans la balance >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ## 🆕 Fonctionnalités spécifiques à cette version >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ### Gestion multi-devises >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Détection automatique Euro/FCFA selon le pays de l'entreprise >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Formats de saisie adaptés (virgule/espace pour décimales) >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Numérotation comptes OHADA (CMxxxxxx) vs française (xxxxxx) >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ### Gestion des actionnaires >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Stratégie par écritures comptables (recommandée) >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Gestion des sur-libérations (dettes envers actionnaires) >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - Consolidation intelligente évitant le double comptage >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ### États financiers >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - **Balance** : Vérification équilibre comptable >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - **Grand Livre** : Détail des mouvements par compte >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - **Bilan** : Actif/Passif équilibré automatiquement >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo - **Compte de Résultat** : Charges/Produits (compatible OHADA) >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo. >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo ## 📞 Support >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo 1. Consulter `Guide_Comptabilite_Complet.html` >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo 2. Vérifier les logs de l'application >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo 3. Tester sur données d'exemple >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md
echo 4. Documenter les cas particuliers rencontrés >> DEPLOYMENT-INSTRUCTIONS-OFFLINE.md

echo      ✓ Comprehensive documentation created

echo.
echo [10/10] Creating version and inventory files...
echo Version: 1.4 > VERSION.txt
echo Build Date: %DATE% %TIME% >> VERSION.txt
echo Features: Multi-Currency, Consolidation, Enhanced Shareholders, Offline Complete >> VERSION.txt
echo Package Type: Offline Deployment (All Dependencies Included) >> VERSION.txt
echo Java Runtime: %JAVA_HOME% >> VERSION.txt

REM Create inventory of all files
echo # PACKAGE INVENTORY > INVENTORY.txt
echo Generated: %DATE% %TIME% >> INVENTORY.txt
echo. >> INVENTORY.txt
dir /s /b >> INVENTORY.txt

echo      ✓ Version and inventory created

echo.
echo ====================================================
echo            OFFLINE DEPLOYMENT COMPLETED!
echo ====================================================
echo.
echo 📦 Complete Offline Package Created
echo 📁 Location: %CD%
echo 📊 Total Files:
dir /a /s | find "File(s)"
echo.
echo 🎯 Key Features:
echo   ✅ All dependencies included (no internet needed)
echo   ✅ Portable Java runtime (if available)
echo   ✅ Multi-currency support (Euro/FCFA)
echo   ✅ Enhanced shareholder management
echo   ✅ Balanced financial statements
echo   ✅ Complete documentation
echo.
echo 🚀 Next Steps:
echo   1. Configure database in hibernate.cfg.xml
echo   2. Run start-comptabilite-offline.bat
echo   3. Open Guide_Comptabilite_Complet.html for documentation
echo.
echo 💡 This package can be deployed on any machine without internet!
echo.
pause