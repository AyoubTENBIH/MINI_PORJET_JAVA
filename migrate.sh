#!/bin/bash

echo "========================================"
echo "  Migration SQLite -> MySQL"
echo "========================================"
echo ""
echo "Assurez-vous que:"
echo "  1. MySQL est démarré"
echo "  2. Le fichier SQLite existe: src/main/resources/database/gym_management.db"
echo ""
read -p "Appuyez sur Entrée pour continuer..."

./mvnw compile exec:java -Dexec.mainClass="com.example.demo.MigrationRunner"





