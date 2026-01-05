-- Script SQL pour vider toutes les tables MySQL avant migration
-- Exécutez ce script dans phpMyAdmin ou MySQL CLI

USE gym_management;

-- Désactiver temporairement les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Vider toutes les tables dans l'ordre inverse des dépendances
TRUNCATE TABLE favoris;
TRUNCATE TABLE user_preferences;
TRUNCATE TABLE activities;
TRUNCATE TABLE notifications;
TRUNCATE TABLE equipements;
TRUNCATE TABLE reservations_cours;
TRUNCATE TABLE cours_collectifs;
TRUNCATE TABLE presences;
TRUNCATE TABLE paiements;
TRUNCATE TABLE adherents;
TRUNCATE TABLE objectifs;
TRUNCATE TABLE packs;
TRUNCATE TABLE utilisateurs;

-- Réactiver les vérifications de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;

-- Vérifier que les tables sont vides
SELECT 'utilisateurs' as table_name, COUNT(*) as count FROM utilisateurs
UNION ALL
SELECT 'packs', COUNT(*) FROM packs
UNION ALL
SELECT 'adherents', COUNT(*) FROM adherents
UNION ALL
SELECT 'paiements', COUNT(*) FROM paiements
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL
SELECT 'activities', COUNT(*) FROM activities;





