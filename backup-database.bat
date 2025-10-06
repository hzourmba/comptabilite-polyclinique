@echo off
setlocal enabledelayedexpansion

REM =================================================================
REM Script de Backup Intelligent pour Système Comptable Multi-Devises
REM =================================================================

echo [INFO] Demarrage du backup de la base de donnees...

REM Configuration
set DB_NAME=comptabilite_db
set DB_USER=root
set DB_HOST=localhost
set BACKUP_DIR=backups
set DATE_STAMP=%date:~-4,4%-%date:~-10,2%-%date:~-7,2%_%time:~0,2%-%time:~3,2%-%time:~6,2%
set DATE_STAMP=%DATE_STAMP: =0%

REM Créer le répertoire de backup s'il n'existe pas
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

echo [INFO] Configuration:
echo   - Base de donnees: %DB_NAME%
echo   - Utilisateur: %DB_USER%
echo   - Repertoire backup: %BACKUP_DIR%
echo   - Timestamp: %DATE_STAMP%

REM =================================================================
REM 1. BACKUP STRUCTURE SEULE (DDL) - Toujours acceptable pour Git
REM =================================================================
echo.
echo [1/4] Backup de la structure (DDL)...
mysqldump --host=%DB_HOST% --user=%DB_USER% --password ^
  --no-data ^
  --routines ^
  --triggers ^
  --events ^
  --single-transaction ^
  %DB_NAME% > "%BACKUP_DIR%\schema_structure_%DATE_STAMP%.sql"

if %errorlevel% equ 0 (
    echo [OK] Structure sauvegardee: schema_structure_%DATE_STAMP%.sql
) else (
    echo [ERREUR] Echec du backup de structure
    goto :error
)

REM =================================================================
REM 2. BACKUP DONNEES DE REFERENCE (Tables de configuration)
REM =================================================================
echo.
echo [2/4] Backup des donnees de reference...
mysqldump --host=%DB_HOST% --user=%DB_USER% --password ^
  --no-create-info ^
  --complete-insert ^
  --single-transaction ^
  %DB_NAME% ^
  compte entreprises utilisateurs exercices > "%BACKUP_DIR%\reference_data_%DATE_STAMP%.sql"

if %errorlevel% equ 0 (
    echo [OK] Donnees de reference sauvegardees: reference_data_%DATE_STAMP%.sql
) else (
    echo [ERREUR] Echec du backup des donnees de reference
    goto :error
)

REM =================================================================
REM 3. BACKUP DONNEES TRANSACTIONNELLES (Ecritures, Factures)
REM =================================================================
echo.
echo [3/4] Backup des donnees transactionnelles...
mysqldump --host=%DB_HOST% --user=%DB_USER% --password ^
  --no-create-info ^
  --complete-insert ^
  --single-transaction ^
  %DB_NAME% ^
  ecritures_comptables lignes_ecriture factures lignes_facture clients fournisseurs > "%BACKUP_DIR%\transaction_data_%DATE_STAMP%.sql"

if %errorlevel% equ 0 (
    echo [OK] Donnees transactionnelles sauvegardees: transaction_data_%DATE_STAMP%.sql
) else (
    echo [ERREUR] Echec du backup des donnees transactionnelles
    goto :error
)

REM =================================================================
REM 4. BACKUP COMPLET (Pour usage local uniquement)
REM =================================================================
echo.
echo [4/4] Backup complet (usage local)...
mysqldump --host=%DB_HOST% --user=%DB_USER% --password ^
  --routines ^
  --triggers ^
  --events ^
  --single-transaction ^
  --complete-insert ^
  %DB_NAME% > "%BACKUP_DIR%\full_backup_%DATE_STAMP%.sql"

if %errorlevel% equ 0 (
    echo [OK] Backup complet sauvegarde: full_backup_%DATE_STAMP%.sql
) else (
    echo [ERREUR] Echec du backup complet
    goto :error
)

REM =================================================================
REM 5. VERIFICATION DES TAILLES
REM =================================================================
echo.
echo [INFO] Verification des tailles de fichiers:
for %%f in ("%BACKUP_DIR%\*_%DATE_STAMP%.sql") do (
    for %%s in ("%%f") do (
        set /a size_mb=%%~zf/1024/1024
        if !size_mb! gtr 90 (
            echo [ATTENTION] %%~nxf: !size_mb! MB - Trop gros pour GitHub
        ) else if !size_mb! gtr 10 (
            echo [AVERTISSEMENT] %%~nxf: !size_mb! MB - Surveiller la taille
        ) else (
            echo [OK] %%~nxf: !size_mb! MB - Compatible GitHub
        )
    )
)

REM =================================================================
REM 6. RECOMMANDATIONS POUR GIT
REM =================================================================
echo.
echo [RECOMMANDATIONS GIT]:
echo   Pour GitHub, commitez:
echo   - schema_structure_%DATE_STAMP%.sql (toujours petit)
echo   - reference_data_%DATE_STAMP%.sql (si ^< 10 MB)
echo.
echo   Pour backup local uniquement:
echo   - transaction_data_%DATE_STAMP%.sql
echo   - full_backup_%DATE_STAMP%.sql

REM =================================================================
REM 7. CREATION D'UN BACKUP "GITHUB-READY"
REM =================================================================
echo.
echo [BONUS] Creation d'un backup optimise pour GitHub...
copy /b "%BACKUP_DIR%\schema_structure_%DATE_STAMP%.sql" + "%BACKUP_DIR%\reference_data_%DATE_STAMP%.sql" "%BACKUP_DIR%\github_ready_%DATE_STAMP%.sql"
echo [OK] Backup GitHub-ready cree: github_ready_%DATE_STAMP%.sql

echo.
echo [SUCCESS] Backup termine avec succes!
echo Fichiers crees dans: %BACKUP_DIR%\
pause
goto :end

:error
echo.
echo [ERREUR] Le backup a echoue. Verifiez:
echo   - MySQL est-il demarre?
echo   - Les parametres de connexion sont-ils corrects?
echo   - La base de donnees existe-t-elle?
pause
exit /b 1

:end
endlocal