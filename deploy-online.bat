@echo off
echo ====================================================
echo        DEPLOYMENT PACKAGE - COMPTABILITE ONLINE
echo        Version: 1.4 - Multi-Currency Consolidation
echo        Date: %DATE%
echo ====================================================
echo.

REM Create deployment directory
if not exist "deployment-online" mkdir deployment-online
cd deployment-online

echo [1/8] Cleaning previous deployment...
if exist "*" del /Q *
if exist "lib" rmdir /S /Q lib
if exist "src" rmdir /S /Q src
if exist "target" rmdir /S /Q target
echo     ✓ Cleanup completed

echo.
echo [2/8] Copying main JAR file...
copy "..\target\comptabilite-1.0-SNAPSHOT.jar" "comptabilite.jar" >nul
if exist "comptabilite.jar" (
    echo     ✓ JAR file copied successfully
) else (
    echo     ✗ JAR file not found! Please run 'mvn clean package' first
    pause
    exit /b 1
)

echo.
echo [3/8] Copying dependency libraries...
mkdir lib
copy "..\target\lib\*" "lib\" >nul
if exist "lib\*.jar" (
    echo     ✓ Dependencies copied
) else (
    echo     ✗ Dependencies not found!
    pause
    exit /b 1
)

echo.
echo [4/8] Copying configuration files...
copy "..\src\main\resources\hibernate.cfg.xml" "hibernate.cfg.xml" >nul
copy "..\src\main\resources\application.properties" "application.properties" >nul 2>nul
echo     ✓ Configuration files copied

echo.
echo [5/8] Copying database schema...
copy "..\src\main\resources\schema.sql" "schema.sql" >nul 2>nul
echo     ✓ Database schema copied

echo.
echo [6/8] Creating startup scripts...

REM Create Windows startup script
echo @echo off > start-comptabilite.bat
echo echo ================================================= >> start-comptabilite.bat
echo echo    COMPTABILITE - Système Multi-Devises >> start-comptabilite.bat
echo echo    Démarrage de l'application... >> start-comptabilite.bat
echo echo ================================================= >> start-comptabilite.bat
echo echo. >> start-comptabilite.bat
echo echo Vérification de Java... >> start-comptabilite.bat
echo java -version >> start-comptabilite.bat
echo if errorlevel 1 ( >> start-comptabilite.bat
echo     echo ERREUR: Java n'est pas installé ou non trouvé dans PATH >> start-comptabilite.bat
echo     echo Veuillez installer Java 11 ou supérieur >> start-comptabilite.bat
echo     pause >> start-comptabilite.bat
echo     exit /b 1 >> start-comptabilite.bat
echo ) >> start-comptabilite.bat
echo echo. >> start-comptabilite.bat
echo echo Démarrage de l'application comptabilité... >> start-comptabilite.bat
echo java -Xmx1024m -jar comptabilite.jar >> start-comptabilite.bat
echo if errorlevel 1 ( >> start-comptabilite.bat
echo     echo ERREUR: Échec du démarrage de l'application >> start-comptabilite.bat
echo     pause >> start-comptabilite.bat
echo ) >> start-comptabilite.bat

REM Create Linux startup script
echo #!/bin/bash > start-comptabilite.sh
echo echo "=================================================" >> start-comptabilite.sh
echo echo "   COMPTABILITE - Système Multi-Devises" >> start-comptabilite.sh
echo echo "   Démarrage de l'application..." >> start-comptabilite.sh
echo echo "=================================================" >> start-comptabilite.sh
echo echo >> start-comptabilite.sh
echo echo "Vérification de Java..." >> start-comptabilite.sh
echo if ! command -v java ^\&^> /dev/null; then >> start-comptabilite.sh
echo     echo "ERREUR: Java n'est pas installé" >> start-comptabilite.sh
echo     echo "Veuillez installer Java 11 ou supérieur" >> start-comptabilite.sh
echo     exit 1 >> start-comptabilite.sh
echo fi >> start-comptabilite.sh
echo echo >> start-comptabilite.sh
echo echo "Démarrage de l'application comptabilité..." >> start-comptabilite.sh
echo java -Xmx1024m -jar comptabilite.jar >> start-comptabilite.sh

echo     ✓ Startup scripts created

echo.
echo [7/8] Creating documentation...
copy "..\README.md" "README.md" >nul 2>nul
copy "..\Guide_Comptabilite_Complet.md" "Guide_Comptabilite_Complet.md" >nul 2>nul
copy "..\Guide_Comptabilite_Complet.html" "Guide_Comptabilite_Complet.html" >nul 2>nul

REM Create deployment instructions
echo # COMPTABILITE - ONLINE DEPLOYMENT PACKAGE > DEPLOYMENT-INSTRUCTIONS.md
echo. >> DEPLOYMENT-INSTRUCTIONS.md
echo ## Version >> DEPLOYMENT-INSTRUCTIONS.md
echo Version: 1.4 - Multi-Currency Consolidation with Enhanced Shareholder Management >> DEPLOYMENT-INSTRUCTIONS.md
echo Date: %DATE% >> DEPLOYMENT-INSTRUCTIONS.md
echo. >> DEPLOYMENT-INSTRUCTIONS.md
echo ## Nouvelles fonctionnalités >> DEPLOYMENT-INSTRUCTIONS.md
echo - ✅ Support multi-devises (Euro/FCFA) avec détection automatique >> DEPLOYMENT-INSTRUCTIONS.md
echo - ✅ Gestion avancée des actionnaires et libérations de capital >> DEPLOYMENT-INSTRUCTIONS.md
echo - ✅ Consolidation des comptes avec évitement du double comptage >> DEPLOYMENT-INSTRUCTIONS.md
echo - ✅ Bilan équilibré avec traitement des sur-libérations >> DEPLOYMENT-INSTRUCTIONS.md
echo - ✅ Correction du compte de résultat pour comptes OHADA >> DEPLOYMENT-INSTRUCTIONS.md
echo - ✅ Guide de comptabilité complet inclus >> DEPLOYMENT-INSTRUCTIONS.md
echo. >> DEPLOYMENT-INSTRUCTIONS.md
echo ## Prérequis >> DEPLOYMENT-INSTRUCTIONS.md
echo - Java 11 ou supérieur >> DEPLOYMENT-INSTRUCTIONS.md
echo - Connexion Internet pour téléchargement des dépendances manquantes >> DEPLOYMENT-INSTRUCTIONS.md
echo - Base de données MySQL/MariaDB >> DEPLOYMENT-INSTRUCTIONS.md
echo. >> DEPLOYMENT-INSTRUCTIONS.md
echo ## Installation >> DEPLOYMENT-INSTRUCTIONS.md
echo 1. Extraire ce package sur le serveur cible >> DEPLOYMENT-INSTRUCTIONS.md
echo 2. Configurer la base de données dans `hibernate.cfg.xml` >> DEPLOYMENT-INSTRUCTIONS.md
echo 3. Exécuter `start-comptabilite.bat` (Windows) ou `start-comptabilite.sh` (Linux) >> DEPLOYMENT-INSTRUCTIONS.md
echo 4. L'application sera accessible via l'interface JavaFX >> DEPLOYMENT-INSTRUCTIONS.md
echo. >> DEPLOYMENT-INSTRUCTIONS.md
echo ## Configuration de la base de données >> DEPLOYMENT-INSTRUCTIONS.md
echo Modifier les paramètres dans `hibernate.cfg.xml`: >> DEPLOYMENT-INSTRUCTIONS.md
echo ```xml >> DEPLOYMENT-INSTRUCTIONS.md
echo ^<property name="connection.url"^>jdbc:mysql://localhost:3306/comptabilite^</property^> >> DEPLOYMENT-INSTRUCTIONS.md
echo ^<property name="connection.username"^>votre_utilisateur^</property^> >> DEPLOYMENT-INSTRUCTIONS.md
echo ^<property name="connection.password"^>votre_mot_de_passe^</property^> >> DEPLOYMENT-INSTRUCTIONS.md
echo ``` >> DEPLOYMENT-INSTRUCTIONS.md
echo. >> DEPLOYMENT-INSTRUCTIONS.md
echo ## Support >> DEPLOYMENT-INSTRUCTIONS.md
echo - Consulter le `Guide_Comptabilite_Complet.html` pour la documentation complète >> DEPLOYMENT-INSTRUCTIONS.md
echo - Vérifier les logs de l'application en cas de problème >> DEPLOYMENT-INSTRUCTIONS.md

echo     ✓ Documentation created

echo.
echo [8/8] Creating version info...
echo Version: 1.4 > VERSION.txt
echo Build Date: %DATE% %TIME% >> VERSION.txt
echo Features: Multi-Currency, Consolidation, Enhanced Shareholders Management >> VERSION.txt
echo Package Type: Online Deployment >> VERSION.txt

echo     ✓ Version info created

echo.
echo ====================================================
echo               DEPLOYMENT COMPLETED!
echo ====================================================
echo.
echo Package Contents:
echo   - comptabilite.jar         : Main application
echo   - lib/                     : Dependencies
echo   - start-comptabilite.*     : Startup scripts
echo   - hibernate.cfg.xml        : Database config
echo   - Guide_Comptabilite_*     : Complete documentation
echo   - DEPLOYMENT-INSTRUCTIONS.md : Setup guide
echo.
echo Location: %CD%
echo.
echo Next Steps:
echo 1. Configure database settings in hibernate.cfg.xml
echo 2. Run start-comptabilite.bat to launch application
echo 3. Consult Guide_Comptabilite_Complet.html for usage
echo.
pause