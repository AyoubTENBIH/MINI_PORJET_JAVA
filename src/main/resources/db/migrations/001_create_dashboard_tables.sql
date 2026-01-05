-- Migration script pour créer les tables du dashboard
-- Date: 2024-01-XX
-- Description: Crée les tables nécessaires pour les fonctionnalités du dashboard

-- Table des notifications
CREATE TABLE IF NOT EXISTS notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    read INTEGER DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id)
);

-- Table des activités
CREATE TABLE IF NOT EXISTS activities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    type TEXT NOT NULL,
    description TEXT NOT NULL,
    entity_type TEXT,
    entity_id INTEGER,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id)
);

-- Table des objectifs
CREATE TABLE IF NOT EXISTS objectifs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,
    valeur REAL NOT NULL,
    date_debut TEXT NOT NULL,
    date_fin TEXT,
    actif INTEGER DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table des préférences utilisateur
CREATE TABLE IF NOT EXISTS user_preferences (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER UNIQUE NOT NULL,
    theme TEXT DEFAULT 'dark',
    language TEXT DEFAULT 'fr',
    sidebar_collapsed INTEGER DEFAULT 0,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id)
);

-- Table des favoris
CREATE TABLE IF NOT EXISTS favoris (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    page_name TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id),
    UNIQUE(user_id, page_name)
);

-- Insérer un objectif par défaut pour le taux d'occupation
INSERT OR IGNORE INTO objectifs (type, valeur, date_debut, actif)
VALUES ('taux_occupation', 80.0, CURRENT_TIMESTAMP, 1);






