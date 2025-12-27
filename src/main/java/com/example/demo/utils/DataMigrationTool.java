package com.example.demo.utils;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Outil de migration des données de SQLite vers MySQL.
 * 
 * <p>Cet outil permet de migrer toutes les données existantes
 * depuis une base SQLite vers une base MySQL, en respectant
 * l'ordre des dépendances entre tables.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class DataMigrationTool {
    private static final Logger logger = Logger.getLogger(DataMigrationTool.class.getName());
    
    // Configuration SQLite (source)
    private static final String SQLITE_DB_PATH = "src/main/resources/database/gym_management.db";
    
    // Configuration MySQL (destination)
    private static final String MYSQL_HOST = "localhost";
    private static final String MYSQL_PORT = "3306";
    private static final String MYSQL_DB = "gym_management";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "";
    
    /**
     * Exécute la migration complète des données.
     * 
     * @param forceMigration Si true, vide les tables MySQL avant de migrer
     * @return true si la migration a réussi, false sinon
     */
    public static boolean migrate(boolean forceMigration) {
        Connection sqliteConn = null;
        Connection mysqlConn = null;
        
        try {
            // Connexion à SQLite
            logger.info("Connexion à la base SQLite...");
            String sqliteUrl = "jdbc:sqlite:" + SQLITE_DB_PATH;
            File sqliteFile = new File(SQLITE_DB_PATH);
            
            if (!sqliteFile.exists()) {
                logger.warning("Le fichier SQLite n'existe pas: " + SQLITE_DB_PATH);
                logger.info("Aucune migration nécessaire - la base MySQL sera initialisée avec des données par défaut");
                return true;
            }
            
            Class.forName("org.sqlite.JDBC");
            sqliteConn = DriverManager.getConnection(sqliteUrl);
            logger.info("Connexion SQLite établie");
            
            // Connexion à MySQL
            logger.info("Connexion à la base MySQL...");
            
            // Créer la base de données si elle n'existe pas
            String mysqlUrlWithoutDb = String.format("jdbc:mysql://%s:%s/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                    MYSQL_HOST, MYSQL_PORT);
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            try (Connection tempConn = DriverManager.getConnection(mysqlUrlWithoutDb, MYSQL_USER, MYSQL_PASSWORD);
                 Statement tempStmt = tempConn.createStatement()) {
                tempStmt.execute("CREATE DATABASE IF NOT EXISTS " + MYSQL_DB + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                logger.info("Base de données '" + MYSQL_DB + "' vérifiée/créée");
            }
            
            // Connexion à la base de données spécifique
            String mysqlUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                    MYSQL_HOST, MYSQL_PORT, MYSQL_DB);
            mysqlConn = DriverManager.getConnection(mysqlUrl, MYSQL_USER, MYSQL_PASSWORD);
            mysqlConn.setAutoCommit(false);
            logger.info("Connexion MySQL établie");
            
            // Créer les tables si elles n'existent pas (sans insérer de données)
            logger.info("Création des tables MySQL si elles n'existent pas...");
            createTablesIfNotExist(mysqlConn);
            logger.info("Tables MySQL créées/vérifiées");
            
            // Vérifier si MySQL contient déjà des données (après avoir créé les tables)
            if (hasData(mysqlConn)) {
                if (forceMigration) {
                    logger.info("La base MySQL contient déjà des données. Vidage des tables...");
                    truncateAllTables(mysqlConn);
                    logger.info("Tables vidées. Début de la migration...");
                } else {
                    logger.warning("La base MySQL contient déjà des données. La migration sera ignorée.");
                    logger.info("Pour forcer la migration, utilisez: migrate(true) ou videz manuellement les tables MySQL");
                    return false;
                }
            }
            
            // Ordre de migration (respecter les dépendances)
            String[] tables = {
                "utilisateurs",
                "packs",
                "objectifs",
                "adherents",
                "paiements",
                "cours_collectifs",
                "reservations_cours",
                "equipements",
                "notifications",
                "activities",
                "user_preferences",
                "favoris"
            };
            
            int totalRows = 0;
            for (String table : tables) {
                int rows = migrateTable(sqliteConn, mysqlConn, table);
                totalRows += rows;
                logger.info(String.format("Table '%s': %d lignes migrées", table, rows));
            }
            
            // Commit toutes les migrations
            mysqlConn.commit();
            logger.info(String.format("Migration terminée avec succès: %d lignes au total", totalRows));
            
            // Vérification de l'intégrité
            verifyIntegrity(sqliteConn, mysqlConn);
            
            return true;
            
        } catch (Exception e) {
            logger.severe("Erreur lors de la migration: " + e.getMessage());
            e.printStackTrace();
            
            if (mysqlConn != null) {
                try {
                    mysqlConn.rollback();
                    logger.info("Rollback effectué");
                } catch (SQLException ex) {
                    logger.severe("Erreur lors du rollback: " + ex.getMessage());
                }
            }
            
            return false;
            
        } finally {
            closeConnection(sqliteConn);
            closeConnection(mysqlConn);
        }
    }
    
    /**
     * Migre une table spécifique.
     */
    private static int migrateTable(Connection sqliteConn, Connection mysqlConn, String tableName) throws SQLException {
        // Récupérer toutes les données de SQLite
        List<String[]> rows = new ArrayList<>();
        String selectSql = "SELECT * FROM " + tableName;
        
        try (Statement stmt = sqliteConn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    row[i - 1] = (value == null) ? null : value.toString();
                }
                rows.add(row);
            }
        }
        
        if (rows.isEmpty()) {
            return 0;
        }
        
        // Insérer dans MySQL
        try (Statement stmt = sqliteConn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Construire la requête INSERT
            StringBuilder insertSql = new StringBuilder("INSERT INTO " + tableName + " (");
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) insertSql.append(", ");
                insertSql.append(metaData.getColumnName(i));
            }
            insertSql.append(") VALUES ");
            
            // Préparer les valeurs
            List<String> valueStrings = new ArrayList<>();
            for (String[] row : rows) {
                StringBuilder values = new StringBuilder("(");
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) values.append(", ");
                    if (row[i] == null) {
                        values.append("NULL");
                    } else {
                        // Échapper les apostrophes
                        String escaped = row[i].replace("'", "''");
                        values.append("'").append(escaped).append("'");
                    }
                }
                values.append(")");
                valueStrings.add(values.toString());
            }
            
            insertSql.append(String.join(", ", valueStrings));
            
            // Exécuter l'insertion
            try (Statement mysqlStmt = mysqlConn.createStatement()) {
                int rowsAffected = mysqlStmt.executeUpdate(insertSql.toString());
                return rowsAffected;
            }
        }
    }
    
    /**
     * Crée les tables MySQL si elles n'existent pas (sans insérer de données).
     */
    private static void createTablesIfNotExist(Connection mysqlConn) throws SQLException {
        try (Statement stmt = mysqlConn.createStatement()) {
            
            // Table des utilisateurs
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS utilisateurs (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(255) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL DEFAULT 'RECEPTIONNISTE',
                    nom VARCHAR(255),
                    prenom VARCHAR(255),
                    actif TINYINT(1) DEFAULT 1,
                    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_username (username)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
            // Table des packs
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS packs (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    nom VARCHAR(255) NOT NULL,
                    prix DECIMAL(10,2) NOT NULL,
                    activites TEXT,
                    jours_disponibilite VARCHAR(255),
                    horaires VARCHAR(255),
                    duree INT NOT NULL,
                    unite_duree VARCHAR(50) DEFAULT 'MOIS',
                    seances_semaine INT,
                    acces_coach TINYINT(1) DEFAULT 0,
                    actif TINYINT(1) DEFAULT 1,
                    description TEXT,
                    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
            // Table des adhérents
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS adherents (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    cin VARCHAR(50) UNIQUE,
                    nom VARCHAR(255) NOT NULL,
                    prenom VARCHAR(255) NOT NULL,
                    date_naissance DATE,
                    telephone VARCHAR(50),
                    email VARCHAR(255),
                    adresse TEXT,
                    photo VARCHAR(500),
                    poids DECIMAL(5,2),
                    taille DECIMAL(5,2),
                    objectifs TEXT,
                    problemes_sante TEXT,
                    pack_id INT,
                    date_debut DATE,
                    date_fin DATE,
                    actif TINYINT(1) DEFAULT 1,
                    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_adherents_pack FOREIGN KEY (pack_id) REFERENCES packs(id) ON DELETE SET NULL,
                    INDEX idx_cin (cin),
                    INDEX idx_pack_id (pack_id),
                    INDEX idx_actif (actif)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
            // Table des paiements
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS paiements (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    adherent_id INT NOT NULL,
                    pack_id INT,
                    montant DECIMAL(10,2) NOT NULL,
                    date_paiement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    methode_paiement VARCHAR(50) NOT NULL,
                    statut VARCHAR(50) DEFAULT 'VALIDE',
                    reference VARCHAR(255),
                    date_debut_abonnement DATE,
                    date_fin_abonnement DATE,
                    notes TEXT,
                    CONSTRAINT fk_paiements_adherent FOREIGN KEY (adherent_id) REFERENCES adherents(id) ON DELETE CASCADE,
                    CONSTRAINT fk_paiements_pack FOREIGN KEY (pack_id) REFERENCES packs(id) ON DELETE SET NULL,
                    INDEX idx_adherent_id (adherent_id),
                    INDEX idx_date_paiement (date_paiement),
                    INDEX idx_statut (statut)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
            // Table des cours collectifs
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS cours_collectifs (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    nom VARCHAR(255) NOT NULL,
                    description TEXT,
                    coach_id INT,
                    jour_semaine VARCHAR(50),
                    heure_debut TIME,
                    heure_fin TIME,
                    capacite_max INT DEFAULT 20,
                    actif TINYINT(1) DEFAULT 1,
                    CONSTRAINT fk_cours_coach FOREIGN KEY (coach_id) REFERENCES utilisateurs(id) ON DELETE SET NULL,
                    INDEX idx_coach_id (coach_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
            // Table des réservations de cours
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservations_cours (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    cours_id INT NOT NULL,
                    adherent_id INT NOT NULL,
                    date_reservation DATE NOT NULL,
                    statut VARCHAR(50) DEFAULT 'CONFIRME',
                    CONSTRAINT fk_reservations_cours FOREIGN KEY (cours_id) REFERENCES cours_collectifs(id) ON DELETE CASCADE,
                    CONSTRAINT fk_reservations_adherent FOREIGN KEY (adherent_id) REFERENCES adherents(id) ON DELETE CASCADE,
                    INDEX idx_cours_id (cours_id),
                    INDEX idx_adherent_id (adherent_id),
                    INDEX idx_date_reservation (date_reservation)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
            // Table des équipements
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS equipements (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    nom VARCHAR(255) NOT NULL,
                    type VARCHAR(100),
                    etat VARCHAR(50) DEFAULT 'FONCTIONNEL',
                    date_achat DATE,
                    date_maintenance DATE,
                    notes TEXT
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
            // Table des notifications
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT,
                    type VARCHAR(50) NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    message TEXT NOT NULL,
                    `read` TINYINT(1) DEFAULT 0,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
                    INDEX idx_user_id (user_id),
                    INDEX idx_read (`read`),
                    INDEX idx_created_at (created_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
            // Table des activités
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS activities (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT,
                    type VARCHAR(50) NOT NULL,
                    description TEXT NOT NULL,
                    entity_type VARCHAR(50),
                    entity_id INT,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_activities_user FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
                    INDEX idx_user_id (user_id),
                    INDEX idx_type (type),
                    INDEX idx_created_at (created_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
            // Table des objectifs
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS objectifs (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    type VARCHAR(50) NOT NULL,
                    valeur DECIMAL(10,2) NOT NULL,
                    date_debut DATE NOT NULL,
                    date_fin DATE,
                    actif TINYINT(1) DEFAULT 1,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_type (type),
                    INDEX idx_actif (actif)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
            // Table des préférences utilisateur
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_preferences (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT UNIQUE NOT NULL,
                    theme VARCHAR(50) DEFAULT 'dark',
                    language VARCHAR(10) DEFAULT 'fr',
                    sidebar_collapsed TINYINT(1) DEFAULT 0,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
                    INDEX idx_user_id (user_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
            // Table des favoris
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS favoris (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    page_name VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_favoris_user FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
                    UNIQUE KEY unique_user_page (user_id, page_name),
                    INDEX idx_user_id (user_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """);
            
        }
    }
    
    /**
     * Vérifie si MySQL contient déjà des données.
     */
    private static boolean hasData(Connection mysqlConn) throws SQLException {
        // Vérifier plusieurs tables pour être sûr qu'il y a des données
        String[] checkTables = {"utilisateurs", "adherents", "packs"};
        int totalRows = 0;
        
        for (String table : checkTables) {
            try {
                String sql = "SELECT COUNT(*) as count FROM " + table;
                try (Statement stmt = mysqlConn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        totalRows += rs.getInt("count");
                    }
                }
            } catch (SQLException e) {
                // Si la table n'existe pas encore, on ignore et on continue
                logger.fine("Table '" + table + "' non trouvée, ignorée");
            }
        }
        
        // Si au moins une table a des données, on considère que la migration n'est pas nécessaire
        return totalRows > 0;
    }
    
    /**
     * Vérifie l'intégrité des données après migration.
     */
    private static void verifyIntegrity(Connection sqliteConn, Connection mysqlConn) throws SQLException {
        String[] tables = {"utilisateurs", "packs", "adherents", "paiements"};
        
        logger.info("Vérification de l'intégrité des données...");
        
        for (String table : tables) {
            int sqliteCount = getRowCount(sqliteConn, table);
            int mysqlCount = getRowCount(mysqlConn, table);
            
            if (sqliteCount == mysqlCount) {
                logger.info(String.format("✓ Table '%s': %d lignes (OK)", table, mysqlCount));
            } else {
                logger.warning(String.format("✗ Table '%s': SQLite=%d, MySQL=%d (DIFFÉRENCE)", 
                        table, sqliteCount, mysqlCount));
            }
        }
    }
    
    /**
     * Compte les lignes d'une table.
     */
    private static int getRowCount(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    /**
     * Ferme une connexion de manière sécurisée.
     */
    private static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warning("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
    
    /**
     * Exécute la migration complète des données (sans forcer).
     * 
     * @return true si la migration a réussi, false sinon
     */
    public static boolean migrate() {
        return migrate(false);
    }
    
    /**
     * Vide toutes les tables MySQL.
     */
    private static void truncateAllTables(Connection mysqlConn) throws SQLException {
        try (Statement stmt = mysqlConn.createStatement()) {
            // Désactiver temporairement les vérifications de clés étrangères
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // Vider toutes les tables dans l'ordre inverse des dépendances
            String[] tables = {
                "favoris", "user_preferences", "activities", "notifications",
                "equipements", "reservations_cours", "cours_collectifs",
                "paiements", "adherents", "objectifs",
                "packs", "utilisateurs"
            };
            
            for (String table : tables) {
                try {
                    stmt.execute("TRUNCATE TABLE " + table);
                    logger.fine("Table '" + table + "' vidée");
                } catch (SQLException e) {
                    // Si la table n'existe pas ou est vide, on ignore
                    logger.fine("Table '" + table + "' non vidée (peut-être vide ou inexistante): " + e.getMessage());
                }
            }
            
            // Réactiver les vérifications de clés étrangères
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
    
    /**
     * Point d'entrée pour exécuter la migration manuellement.
     */
    public static void main(String[] args) {
        logger.info("=== Démarrage de la migration SQLite -> MySQL ===");
        
        // Vérifier si l'option --force est passée
        boolean force = args.length > 0 && (args[0].equals("--force") || args[0].equals("-f"));
        
        if (force) {
            logger.info("Mode FORCE activé : les tables MySQL seront vidées avant la migration");
        }
        
        boolean success = migrate(force);
        
        if (success) {
            logger.info("=== Migration terminée avec succès ===");
        } else {
            logger.severe("=== Migration échouée ===");
            System.exit(1);
        }
    }
}


