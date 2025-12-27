@echo off
echo ========================================
echo   Migration SQLite -^> MySQL
echo ========================================
echo.
echo Assurez-vous que:
echo   1. XAMPP MySQL est demarre
echo   2. Le fichier SQLite existe: src\main\resources\database\gym_management.db
echo.
pause

call mvnw.cmd compile exec:java -Dexec.mainClass="com.example.demo.MigrationRunner"

pause


