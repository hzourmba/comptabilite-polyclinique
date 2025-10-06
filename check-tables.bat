@echo off
echo [INFO] Verification des tables dans la base de donnees...
echo.

set DB_NAME=comptabilite_db
set DB_USER=root
set DB_HOST=localhost

echo [TABLES DISPONIBLES]:
mysql --host=%DB_HOST% --user=%DB_USER% --password -e "USE %DB_NAME%; SHOW TABLES;" 2>nul

echo.
echo [STRUCTURE DE LA BASE]:
mysql --host=%DB_HOST% --user=%DB_USER% --password -e "USE %DB_NAME%; SELECT TABLE_NAME, TABLE_ROWS, ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'SIZE_MB' FROM information_schema.TABLES WHERE TABLE_SCHEMA = '%DB_NAME%' ORDER BY TABLE_NAME;" 2>nul

pause