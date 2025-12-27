-- Script de création de la base de données MySQL pour le système de gestion de gym
-- Compatible avec XAMPP (MySQL par défaut)

-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS gym_management 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE gym_management;

-- Note: Les tables seront créées automatiquement par DatabaseManager.initializeDatabase()
-- Ce script sert uniquement à créer la base de données elle-même.



