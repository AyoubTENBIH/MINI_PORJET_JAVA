package com.example.demo.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Gestionnaire de connexion à la base de données MySQL
 * Implémentation du pattern Singleton
 */
public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private static DatabaseManager instance;
    private Connection connection;
    
    // Configuration MySQL par défaut (XAMPP)
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "gym_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    // Charger le driver MySQL au chargement de la classe
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("Driver MySQL chargé avec succès");
        } catch (ClassNotFoundException e) {
            logger.severe("ERREUR: Driver MySQL non trouvé. Vérifiez que la dépendance mysql-connector-j est dans pom.xml");
            e.printStackTrace();
        }
    }

    private DatabaseManager() {
        // Constructeur privé pour le pattern Singleton
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Obtient l'URL de connexion MySQL
     */
    private String getDatabaseUrl() {
        return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                DB_HOST, DB_PORT, DB_NAME);
    }
    
    /**
     * Crée la base de données si elle n'existe pas
     */
    private void createDatabaseIfNotExists() throws SQLException {
        // Se connecter sans spécifier la base de données
        String urlWithoutDb = String.format("jdbc:mysql://%s:%s/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                DB_HOST, DB_PORT);
        
        try (Connection conn = DriverManager.getConnection(urlWithoutDb, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Créer la base de données si elle n'existe pas
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + DB_NAME + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            logger.info("Base de données '" + DB_NAME + "' vérifiée/créée avec succès");
        }
    }

    /**
     * Obtient une connexion à la base de données MySQL
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Charger le driver explicitement
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    logger.severe("Driver MySQL non trouvé. Vérifiez que mysql-connector-j est dans le classpath.");
                    throw new SQLException("Driver MySQL non trouvé. Ajoutez la dépendance dans pom.xml", e);
                }
                
                // Créer la base de données si elle n'existe pas
                createDatabaseIfNotExists();
                
                String dbUrl = getDatabaseUrl();
                logger.info("Connexion à la base de données MySQL: " + dbUrl);
                
                connection = DriverManager.getConnection(dbUrl, DB_USER, DB_PASSWORD);
                // MySQL nécessite une gestion explicite des transactions
                connection.setAutoCommit(false);
                logger.info("Connexion à la base de données MySQL établie avec succès");
            } catch (SQLException e) {
                logger.severe("Erreur lors de la connexion à la base de données MySQL: " + e.getMessage());
                logger.severe("Vérifiez que XAMPP est démarré et que MySQL est actif sur le port 3306");
                e.printStackTrace();
                throw e;
            }
        }
        return connection;
    }

    /**
     * Initialise la base de données et crée les tables si elles n'existent pas
     */
    public void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Table des utilisateurs (pour le système de login)
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

            // Table des packs/abonnements
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

            // Insérer un objectif par défaut pour le taux d'occupation
            stmt.execute("""
                INSERT IGNORE INTO objectifs (type, valeur, date_debut, actif)
                VALUES ('taux_occupation', 80.0, CURDATE(), 1)
            """);

            // Créer un utilisateur admin par défaut (username: admin, password: admin)
            stmt.execute("""
                INSERT IGNORE INTO utilisateurs (username, password, role, nom, prenom)
                VALUES ('admin', 'admin', 'ADMIN', 'Administrateur', 'Système')
            """);
            
            // Commit les changements
            conn.commit();

            logger.info("Base de données initialisée avec succès");

            // Insérer des données de test
            insertTestData(stmt, conn);

        } catch (SQLException e) {
            logger.severe("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Insère des données de test pour le développement
     */
    private void insertTestData(Statement stmt, Connection conn) throws SQLException {
        // Insérer des packs de test
        stmt.execute("""
            INSERT IGNORE INTO packs (nom, prix, activites, jours_disponibilite, horaires, duree, unite_duree, seances_semaine, acces_coach, description)
            VALUES 
            ('Pack Tapis + Musculation', 200.0, 'Musculation,Tapis', 'Lundi-Vendredi', 'Matin,Apres-midi', 1, 'MOIS', 5, 0, 'Accès aux machines de musculation et tapis de course'),
            ('Pack Musculation', 150.0, 'Musculation', 'Lundi-Vendredi', 'Tous', 1, 'MOIS', 7, 0, 'Accès complet à la salle de musculation'),
            ('Pack Premium All Access', 400.0, 'Musculation,Cardio,Tapis,Cours collectifs,Piscine,Sauna', 'Tous les jours', '24h/24', 1, 'MOIS', -1, 1, 'Accès complet à toutes les installations + coach personnel'),
            ('Pack Étudiant', 120.0, 'Musculation,Cardio', 'Lundi-Vendredi', 'Matin,Apres-midi', 1, 'MOIS', 5, 0, 'Pack spécial étudiants avec tarif réduit'),
            ('Pack 3 Mois', 350.0, 'Musculation,Cardio,Tapis', 'Tous les jours', 'Tous', 3, 'MOIS', 7, 0, 'Pack trimestriel avec avantage prix'),
            ('Pack Annuel', 1200.0, 'Musculation,Cardio,Tapis,Cours collectifs', 'Tous les jours', 'Tous', 12, 'MOIS', -1, 0, 'Pack annuel avec économie importante')
        """);
        
        logger.info("Packs de test insérés");

        // Vérifier si des adhérents existent déjà pour éviter les doublons
        try (var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM adherents")) {
            if (rs.next() && rs.getInt("count") > 0) {
                logger.info("Des adhérents existent déjà. Données de test non ajoutées.");
                return;
            }
        }

        // Récupérer les IDs des packs insérés
        var packIds = new java.util.ArrayList<Integer>();
        try (var rs = stmt.executeQuery("SELECT id FROM packs ORDER BY id")) {
            while (rs.next()) {
                packIds.add(rs.getInt("id"));
            }
        }

        if (packIds.isEmpty()) {
            logger.warning("Aucun pack trouvé. Impossible d'insérer des adhérents de test.");
            return;
        }

        // Générer la date d'aujourd'hui au format SQLite
        java.time.LocalDate aujourdhui = java.time.LocalDate.now();
        String dateFormat = aujourdhui.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Insérer des adhérents de test (environ 40 adhérents)
        String[][] adherentsData = {
            // CIN, Nom, Prénom, Date Naissance, Téléphone, Email, Adresse, Poids, Taille, Objectifs, Problèmes Santé, Pack Index, Jours depuis début, Actif
            {"AB123456", "Alaoui", "Ahmed", "1990-05-15", "0612345678", "ahmed.alaoui@email.com", "123 Rue Mohammed V, Casablanca", "75.5", "175.0", "Prise de masse", "", "0", "30", "1"},
            {"AB123457", "Benali", "Fatima", "1995-08-22", "0623456789", "fatima.benali@email.com", "45 Avenue Hassan II, Rabat", "62.0", "165.0", "Perte de poids", "", "1", "15", "1"},
            {"AB123458", "Chraibi", "Mohamed", "1988-03-10", "0634567890", "mohamed.chraibi@email.com", "78 Boulevard Zerktouni, Casablanca", "82.0", "180.0", "Musculation", "", "0", "60", "1"},
            {"AB123459", "Dahbi", "Aicha", "1992-11-05", "0645678901", "aicha.dahbi@email.com", "12 Rue Allal Ben Abdellah, Fès", "58.5", "160.0", "Cardio", "Asthme léger", "2", "5", "1"},
            {"AB123460", "El Amrani", "Hassan", "1985-07-18", "0656789012", "hassan.alamrani@email.com", "89 Avenue Mohammed VI, Marrakech", "88.0", "178.0", "Prise de masse", "", "3", "90", "1"},
            {"AB123461", "Fassi", "Khadija", "1997-01-25", "0667890123", "khadija.fassi@email.com", "34 Rue Moulay Youssef, Casablanca", "55.0", "158.0", "Perte de poids", "", "1", "120", "1"},
            {"AB123462", "Ghazi", "Omar", "1991-09-12", "0678901234", "omar.ghazi@email.com", "56 Boulevard Anfa, Casablanca", "79.0", "177.0", "Musculation", "", "0", "180", "1"},
            {"AB123463", "Haddad", "Sanae", "1994-04-30", "0689012345", "sanae.haddad@email.com", "23 Rue Ziraoui, Rabat", "63.5", "168.0", "Cardio", "", "2", "45", "1"},
            {"AB123464", "Idrissi", "Youssef", "1987-12-08", "0690123456", "youssef.idrissi@email.com", "67 Avenue Al Massira, Casablanca", "85.0", "182.0", "Prise de masse", "", "0", "200", "0"},
            {"AB123465", "Jazouli", "Laila", "1993-06-20", "0601234567", "laila.jazouli@email.com", "91 Rue Oqba, Fès", "59.0", "162.0", "Perte de poids", "", "1", "30", "1"},
            {"AB123466", "Kadiri", "Ali", "1989-02-14", "0612345679", "ali.kadiri@email.com", "14 Boulevard Zerktouni, Casablanca", "77.0", "175.0", "Musculation", "", "1", "15", "1"},
            {"AB123467", "Lamrani", "Nadia", "1996-10-03", "0623456790", "nadia.lamrani@email.com", "48 Rue Mohammed V, Rabat", "61.0", "164.0", "Cardio", "", "0", "7", "1"},
            {"AB123468", "Mansouri", "Karim", "1990-08-17", "0634567901", "karim.mansouri@email.com", "72 Avenue Hassan II, Casablanca", "80.0", "179.0", "Prise de masse", "", "2", "90", "1"},
            {"AB123469", "Naciri", "Souad", "1992-05-29", "0645679012", "souad.naciri@email.com", "35 Rue Allal Ben Abdellah, Marrakech", "64.0", "169.0", "Perte de poids", "", "1", "45", "1"},
            {"AB123470", "Ouali", "Mehdi", "1986-11-11", "0656790123", "mehdi.ouali@email.com", "58 Boulevard Anfa, Casablanca", "83.0", "181.0", "Musculation", "Dos fragile", "0", "120", "1"},
            {"AB123471", "Qadiri", "Salma", "1995-03-26", "0667901234", "salma.qadiri@email.com", "82 Rue Ziraoui, Rabat", "57.0", "159.0", "Cardio", "", "1", "60", "1"},
            {"AB123472", "Rahmani", "Amine", "1988-07-09", "0679012345", "amine.rahmani@email.com", "19 Avenue Al Massira, Casablanca", "76.0", "176.0", "Prise de masse", "", "2", "180", "1"},
            {"AB123473", "Saadi", "Imane", "1993-12-21", "0689012346", "imane.saadi@email.com", "41 Rue Oqba, Fès", "60.0", "161.0", "Perte de poids", "", "0", "30", "1"},
            {"AB123474", "Tazi", "Bilal", "1991-04-07", "0690123457", "bilal.tazi@email.com", "93 Boulevard Mohammed VI, Marrakech", "78.5", "177.0", "Musculation", "", "1", "15", "1"},
            {"AB123475", "Zahiri", "Zineb", "1997-09-18", "0601234568", "zineb.zahiri@email.com", "27 Rue Mohammed V, Casablanca", "56.0", "157.0", "Cardio", "", "0", "5", "1"},
            {"AB123476", "Bennani", "Rachid", "1987-01-31", "0612345680", "rachid.bennani@email.com", "64 Avenue Hassan II, Rabat", "84.0", "180.0", "Prise de masse", "", "3", "210", "0"},
            {"AB123477", "Cherkaoui", "Houda", "1994-06-13", "0623456801", "houda.cherkaoui@email.com", "38 Boulevard Zerktouni, Casablanca", "62.5", "166.0", "Perte de poids", "", "1", "75", "1"},
            {"AB123478", "Dari", "Said", "1989-10-25", "0634568012", "said.dari@email.com", "71 Rue Allal Ben Abdellah, Fès", "81.0", "178.0", "Musculation", "", "2", "135", "1"},
            {"AB123479", "El Fassi", "Nabila", "1992-02-16", "0645680123", "nabila.elfassi@email.com", "53 Avenue Al Massira, Casablanca", "65.0", "170.0", "Cardio", "", "1", "50", "1"},
            {"AB123480", "Alaoui", "Khalid", "1986-08-28", "0656801234", "khalid.alaoui@email.com", "96 Rue Ziraoui, Rabat", "86.0", "183.0", "Prise de masse", "", "0", "240", "1"},
            {"AB123481", "Benali", "Siham", "1995-12-04", "0668012345", "siham.benali@email.com", "22 Boulevard Anfa, Casablanca", "58.5", "160.0", "Perte de poids", "", "1", "90", "1"},
            {"AB123482", "Chraibi", "Othmane", "1990-05-19", "0678123456", "othmane.chraibi@email.com", "87 Avenue Hassan II, Marrakech", "79.5", "177.0", "Musculation", "", "0", "25", "1"},
            {"AB123483", "Dahbi", "Hanane", "1993-09-01", "0681234567", "hanane.dahbi@email.com", "31 Rue Oqba, Fès", "63.0", "167.0", "Cardio", "", "2", "60", "1"},
            {"AB123484", "El Amrani", "Adil", "1988-01-14", "0692345678", "adil.alamrani@email.com", "59 Boulevard Mohammed VI, Casablanca", "82.5", "181.0", "Prise de masse", "", "1", "150", "1"},
            {"AB123485", "Fassi", "Rim", "1996-07-26", "0603456789", "rim.fassi@email.com", "44 Rue Mohammed V, Rabat", "59.5", "161.0", "Perte de poids", "", "0", "10", "1"},
            {"AB123486", "Ghazi", "Reda", "1991-11-08", "0614567890", "reda.ghazi@email.com", "76 Avenue Al Massira, Casablanca", "77.5", "176.0", "Musculation", "", "1", "35", "1"},
            {"AB123487", "Haddad", "Meriem", "1994-03-20", "0625678901", "meriem.haddad@email.com", "18 Boulevard Zerktouni, Casablanca", "61.5", "165.0", "Cardio", "", "2", "70", "1"},
            {"AB123488", "Idrissi", "Anas", "1987-08-02", "0636789012", "anas.idrissi@email.com", "63 Rue Allal Ben Abdellah, Fès", "85.5", "182.0", "Prise de masse", "", "0", "190", "1"},
            {"AB123489", "Jazouli", "Sara", "1993-12-15", "0647890123", "sara.jazouli@email.com", "92 Avenue Hassan II, Marrakech", "57.5", "158.0", "Perte de poids", "", "1", "55", "1"},
            {"AB123490", "Kadiri", "Tarik", "1989-04-27", "0658901234", "tarik.kadiri@email.com", "36 Rue Ziraoui, Rabat", "80.5", "179.0", "Musculation", "", "0", "20", "1"},
            {"AB123491", "Lamrani", "Nesrine", "1996-10-09", "0669012345", "nesrine.lamrani@email.com", "81 Boulevard Anfa, Casablanca", "64.5", "170.0", "Cardio", "", "2", "80", "1"},
            {"AB123492", "Mansouri", "Hamza", "1990-02-21", "0670123456", "hamza.mansouri@email.com", "29 Avenue Al Massira, Casablanca", "78.0", "177.5", "Prise de masse", "", "1", "100", "1"},
            {"AB123493", "Naciri", "Lina", "1992-07-04", "0681234568", "lina.naciri@email.com", "55 Rue Oqba, Fès", "60.5", "162.0", "Perte de poids", "", "0", "40", "1"},
            {"AB123494", "Ouali", "Yassin", "1985-11-16", "0692345679", "yassin.ouali@email.com", "73 Boulevard Mohammed VI, Marrakech", "87.0", "184.0", "Musculation", "", "3", "220", "1"},
            {"AB123495", "Qadiri", "Maryam", "1997-03-29", "0603456790", "maryam.qadiri@email.com", "17 Rue Mohammed V, Casablanca", "56.5", "159.0", "Cardio", "", "1", "65", "1"}
        };

        int adherentIndex = 0;
        for (String[] data : adherentsData) {
            adherentIndex++;
            int packIndex = Integer.parseInt(data[11]) % packIds.size();
            int packId = packIds.get(packIndex);
            int joursDepuisDebut = Integer.parseInt(data[12]);
            java.time.LocalDate dateDebut = aujourdhui.minusDays(joursDepuisDebut);
            java.time.LocalDate dateFin = dateDebut.plusMonths(1); // Abonnement d'1 mois par défaut
            
            // Si l'adhérent est inactif ou que la date est ancienne, marquer comme expiré
            boolean actif = data[13].equals("1") && dateFin.isAfter(aujourdhui) || dateFin.isEqual(aujourdhui);
            if (!actif && data[13].equals("1")) {
                dateFin = aujourdhui.minusDays((int)(Math.random() * 60)); // Expiré il y a 0-60 jours
            }

            String insertAdherent = String.format("""
                INSERT INTO adherents (cin, nom, prenom, date_naissance, telephone, email, adresse, 
                                     poids, taille, objectifs, problemes_sante, pack_id, 
                                     date_debut, date_fin, actif, date_inscription)
                VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', 
                        %s, %s, '%s', '%s', %d, 
                        '%s', '%s', %d, '%s')
                """,
                data[0], data[1], data[2], data[3], data[4], data[5], data[6],
                data[7], data[8], data[9], data[10], packId,
                dateDebut.toString(), dateFin.toString(), actif ? 1 : 0, dateDebut.toString()
            );
            
            stmt.execute(insertAdherent);
            
            // Récupérer l'ID de l'adhérent inséré
            int adherentId = 0;
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID() as id")) {
                if (rs.next()) {
                    adherentId = rs.getInt("id");
                }
            }
            
            // Insérer un paiement pour cet adhérent
            String[] methodesPaiement = {"ESPECE", "CARTE", "CHEQUE", "VIREMENT"};
            String methode = methodesPaiement[(int)(Math.random() * methodesPaiement.length)];
            
            // Récupérer le prix du pack
            double prixPack = 200.0;
            try (var rs = stmt.executeQuery("SELECT prix FROM packs WHERE id = " + packId)) {
                if (rs.next()) {
                    prixPack = rs.getDouble("prix");
                }
            }
            
            String insertPaiement = String.format("""
                INSERT INTO paiements (adherent_id, pack_id, montant, date_paiement, methode_paiement, 
                                     statut, date_debut_abonnement, date_fin_abonnement)
                VALUES (%d, %d, %.2f, '%s', '%s', 'VALIDE', '%s', '%s')
                """,
                adherentId, packId, prixPack, dateDebut.toString(), methode, dateDebut.toString(), dateFin.toString()
            );
            
            stmt.execute(insertPaiement);
        }
        
        // Commit toutes les insertions
        conn.commit();

        logger.info("Données de test insérées: " + adherentsData.length + " adhérents avec leurs paiements");
    }

    /**
     * Ferme la connexion à la base de données
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Connexion à la base de données fermée");
            }
        } catch (SQLException e) {
            logger.severe("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
}
