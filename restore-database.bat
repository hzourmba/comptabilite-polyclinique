@echo off
setlocal enabledelayedexpansion

REM =================================================================
REM Script de Restauration pour Système Comptable Multi-Devises
REM =================================================================

echo [INFO] Script de restauration de base de donnees

REM Configuration
set DB_NAME=comptabilite_db
set DB_USER=root
set DB_HOST=localhost
set BACKUP_DIR=backups

echo [INFO] Configuration:
echo   - Base de donnees: %DB_NAME%
echo   - Utilisateur: %DB_USER%
echo   - Repertoire backup: %BACKUP_DIR%

REM Lister les backups disponibles
echo.
echo [BACKUPS DISPONIBLES]:
echo.
echo [STRUCTURE (DDL)]:
for %%f in ("%BACKUP_DIR%\schema_structure_*.sql") do echo   - %%~nxf

echo.
echo [DONNEES DE REFERENCE]:
for %%f in ("%BACKUP_DIR%\reference_data_*.sql") do echo   - %%~nxf

echo.
echo [DONNEES TRANSACTIONNELLES]:
for %%f in ("%BACKUP_DIR%\transaction_data_*.sql") do echo   - %%~nxf

echo.
echo [BACKUPS COMPLETS]:
for %%f in ("%BACKUP_DIR%\full_backup_*.sql") do echo   - %%~nxf

echo.
echo [BACKUPS GITHUB-READY]:
for %%f in ("%BACKUP_DIR%\github_ready_*.sql") do echo   - %%~nxf

echo.
echo [OPTIONS DE RESTAURATION]:
echo   1. Restauration rapide (structure + reference)
echo   2. Restauration complete avec donnees transactionnelles
echo   3. Restauration depuis backup complet
echo   4. Restauration personnalisee
echo   5. Quitter

set /p choice="Choisissez une option (1-5): "

if "%choice%"=="1" goto :restore_quick
if "%choice%"=="2" goto :restore_with_data
if "%choice%"=="3" goto :restore_full
if "%choice%"=="4" goto :restore_custom
if "%choice%"=="5" goto :end
echo [ERREUR] Option invalide
pause
goto :end

:restore_quick
echo.
echo [RESTAURATION RAPIDE] Structure + donnees de reference...
REM Trouver les derniers fichiers
for /f %%f in ('dir /b /o-d "%BACKUP_DIR%\schema_structure_*.sql" 2^>nul') do (
    set LATEST_SCHEMA=%BACKUP_DIR%\%%f
    goto :found_schema
)
:found_schema

for /f %%f in ('dir /b /o-d "%BACKUP_DIR%\reference_data_*.sql" 2^>nul') do (
    set LATEST_REF=%BACKUP_DIR%\%%f
    goto :found_ref
)
:found_ref

echo [INFO] Restoration de la structure: !LATEST_SCHEMA!
mysql --host=%DB_HOST% --user=%DB_USER% --password %DB_NAME% < "!LATEST_SCHEMA!"

echo [INFO] Restoration des donnees de reference: !LATEST_REF!
mysql --host=%DB_HOST% --user=%DB_USER% --password %DB_NAME% < "!LATEST_REF!"

echo [SUCCESS] Restauration rapide terminee!
goto :end

:restore_with_data
echo.
echo [RESTAURATION AVEC DONNEES] Structure + reference + transactions...
REM Similar logic but include transaction data
goto :end

:restore_full
echo.
echo [RESTAURATION COMPLETE] Depuis backup complet...
for /f %%f in ('dir /b /o-d "%BACKUP_DIR%\full_backup_*.sql" 2^>nul') do (
    set LATEST_FULL=%BACKUP_DIR%\%%f
    goto :found_full
)
:found_full

echo [INFO] Restoration complete: !LATEST_FULL!
mysql --host=%DB_HOST% --user=%DB_USER% --password %DB_NAME% < "!LATEST_FULL!"

echo [SUCCESS] Restauration complete terminee!
goto :end

:restore_custom
echo.
echo [RESTAURATION PERSONNALISEE]
echo Entrez le nom du fichier de backup dans le repertoire %BACKUP_DIR%:
set /p custom_file="Nom du fichier: "

if exist "%BACKUP_DIR%\%custom_file%" (
    echo [INFO] Restoration personnalisee: %custom_file%
    mysql --host=%DB_HOST% --user=%DB_USER% --password %DB_NAME% < "%BACKUP_DIR%\%custom_file%"
    echo [SUCCESS] Restauration personnalisee terminee!
) else (
    echo [ERREUR] Fichier non trouve: %BACKUP_DIR%\%custom_file%
)
goto :end

:end
echo.
pause
endlocal