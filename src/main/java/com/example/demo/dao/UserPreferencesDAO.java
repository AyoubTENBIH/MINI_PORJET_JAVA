package com.example.demo.dao;

import com.example.demo.models.UserPreferences;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des préférences utilisateur.
 * 
 * <p>Gère les opérations CRUD sur les préférences utilisateur
 * (thème, langue, état de la sidebar, etc.)</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class UserPreferencesDAO {
    private static final Logger logger = Logger.getLogger(UserPreferencesDAO.class.getName());

    /**
     * Crée ou met à jour les préférences d'un utilisateur.
     * 
     * @param preferences Les préférences à sauvegarder
     * @return Les préférences sauvegardées
     * @throws SQLException Si une erreur survient
     */
    public UserPreferences save(UserPreferences preferences) throws SQLException {
        // Vérifier si des préférences existent déjà pour cet utilisateur
        UserPreferences existing = findByUserId(preferences.getUserId());
        
        if (existing != null) {
            return update(preferences);
        } else {
            return create(preferences);
        }
    }

    /**
     * Crée de nouvelles préférences utilisateur.
     * 
     * @param preferences Les préférences à créer
     * @return Les préférences créées avec leur ID généré
     * @throws SQLException Si une erreur survient lors de l'insertion
     */
    public UserPreferences create(UserPreferences preferences) throws SQLException {
        String sql = """
            INSERT INTO user_preferences (user_id, theme, language, sidebar_collapsed, updated_at)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, preferences.getUserId());
            stmt.setString(2, preferences.getTheme());
            stmt.setString(3, preferences.getLanguage());
            stmt.setInt(4, preferences.isSidebarCollapsed() ? 1 : 0);
            stmt.setString(5, preferences.getUpdatedAt() != null 
                ? preferences.getUpdatedAt().toString() 
                : LocalDateTime.now().toString());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion des préférences");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    preferences.setId(generatedKeys.getInt(1));
                } else {
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT LAST_INSERT_ID() as id")) {
                        if (rs.next()) {
                            preferences.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            logger.info("Préférences créées pour l'utilisateur: " + preferences.getUserId());
            return preferences;
        }
    }

    /**
     * Met à jour les préférences d'un utilisateur.
     * 
     * @param preferences Les préférences à mettre à jour
     * @return Les préférences mises à jour
     * @throws SQLException Si une erreur survient lors de la mise à jour
     */
    public UserPreferences update(UserPreferences preferences) throws SQLException {
        String sql = """
            UPDATE user_preferences 
            SET theme=?, language=?, sidebar_collapsed=?, updated_at=?
            WHERE user_id=?
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, preferences.getTheme());
            stmt.setString(2, preferences.getLanguage());
            stmt.setInt(3, preferences.isSidebarCollapsed() ? 1 : 0);
            stmt.setString(4, LocalDateTime.now().toString());
            stmt.setInt(5, preferences.getUserId());

            stmt.executeUpdate();
            logger.info("Préférences mises à jour pour l'utilisateur: " + preferences.getUserId());
            return preferences;
        }
    }

    /**
     * Récupère les préférences d'un utilisateur par son ID.
     * 
     * @param userId ID de l'utilisateur
     * @return Les préférences trouvées, ou null si non trouvées
     * @throws SQLException Si une erreur survient
     */
    public UserPreferences findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM user_preferences WHERE user_id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUserPreferences(rs);
            }
            return null;
        }
    }

    /**
     * Récupère ou crée les préférences par défaut pour un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @return Les préférences (existantes ou créées par défaut)
     * @throws SQLException Si une erreur survient
     */
    public UserPreferences getOrCreateDefault(Integer userId) throws SQLException {
        UserPreferences preferences = findByUserId(userId);
        
        if (preferences == null) {
            preferences = new UserPreferences(userId);
            preferences = create(preferences);
        }
        
        return preferences;
    }

    /**
     * Met à jour le thème d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param theme Nouveau thème ("dark" ou "light")
     * @throws SQLException Si une erreur survient
     */
    public void updateTheme(Integer userId, String theme) throws SQLException {
        UserPreferences preferences = getOrCreateDefault(userId);
        preferences.setTheme(theme);
        update(preferences);
    }

    /**
     * Met à jour la langue d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param language Nouvelle langue ("fr", "en", "ar")
     * @throws SQLException Si une erreur survient
     */
    public void updateLanguage(Integer userId, String language) throws SQLException {
        UserPreferences preferences = getOrCreateDefault(userId);
        preferences.setLanguage(language);
        update(preferences);
    }

    /**
     * Met à jour l'état de la sidebar d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param collapsed État de la sidebar (true = fermée, false = ouverte)
     * @throws SQLException Si une erreur survient
     */
    public void updateSidebarState(Integer userId, Boolean collapsed) throws SQLException {
        UserPreferences preferences = getOrCreateDefault(userId);
        preferences.setSidebarCollapsed(collapsed);
        update(preferences);
    }

    /**
     * Mappe un ResultSet vers un objet UserPreferences.
     * 
     * @param rs Le ResultSet à mapper
     * @return L'objet UserPreferences mappé
     * @throws SQLException Si une erreur survient lors du mapping
     */
    private UserPreferences mapResultSetToUserPreferences(ResultSet rs) throws SQLException {
        UserPreferences preferences = new UserPreferences();
        preferences.setId(rs.getInt("id"));
        preferences.setUserId(rs.getInt("user_id"));
        preferences.setTheme(rs.getString("theme"));
        preferences.setLanguage(rs.getString("language"));
        preferences.setSidebarCollapsed(rs.getInt("sidebar_collapsed") == 1);

        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null) {
            preferences.setUpdatedAt(com.example.demo.utils.DateUtils.parseDateTime(updatedAtStr));
        }

        return preferences;
    }
}

