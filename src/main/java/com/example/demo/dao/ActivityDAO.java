package com.example.demo.dao;

import com.example.demo.models.Activity;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des activités du système.
 * 
 * <p>Gère les opérations CRUD sur les activités et fournit
 * des méthodes pour récupérer l'historique des activités.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class ActivityDAO {
    private static final Logger logger = Logger.getLogger(ActivityDAO.class.getName());

    /**
     * Crée une nouvelle activité dans la base de données.
     * 
     * @param activity L'activité à créer
     * @return L'activité créée avec son ID généré
     * @throws SQLException Si une erreur survient lors de l'insertion
     */
    public Activity create(Activity activity) throws SQLException {
        String sql = """
            INSERT INTO activities (user_id, type, description, entity_type, entity_id, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setObject(1, activity.getUserId());
            stmt.setString(2, activity.getType());
            stmt.setString(3, activity.getDescription());
            stmt.setObject(4, activity.getEntityType());
            stmt.setObject(5, activity.getEntityId());
            stmt.setString(6, activity.getCreatedAt() != null 
                ? activity.getCreatedAt().toString() 
                : LocalDateTime.now().toString());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion de l'activité");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    activity.setId(generatedKeys.getInt(1));
                } else {
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT LAST_INSERT_ID() as id")) {
                        if (rs.next()) {
                            activity.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            conn.commit(); // ✅ Commiter la transaction
            logger.info("Activité créée: " + activity.getType());
            return activity;
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la création de l'activité: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Met à jour une activité existante.
     * 
     * @param activity L'activité à mettre à jour
     * @return L'activité mise à jour
     * @throws SQLException Si une erreur survient lors de la mise à jour
     */
    public Activity update(Activity activity) throws SQLException {
        String sql = """
            UPDATE activities SET user_id=?, type=?, description=?, entity_type=?, entity_id=?
            WHERE id=?
        """;

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, activity.getUserId());
            stmt.setString(2, activity.getType());
            stmt.setString(3, activity.getDescription());
            stmt.setObject(4, activity.getEntityType());
            stmt.setObject(5, activity.getEntityId());
            stmt.setInt(6, activity.getId());

            stmt.executeUpdate();
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Activité mise à jour: " + activity.getId());
            return activity;
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la mise à jour de l'activité: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime une activité.
     * 
     * @param id ID de l'activité à supprimer
     * @throws SQLException Si une erreur survient
     */
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM activities WHERE id=?";

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Activité supprimée: " + id);
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la suppression de l'activité: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Récupère une activité par son ID.
     * 
     * @param id ID de l'activité
     * @return L'activité trouvée, ou null si non trouvée
     * @throws SQLException Si une erreur survient
     */
    public Activity findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM activities WHERE id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToActivity(rs);
            }
            return null;
        }
    }

    /**
     * Récupère toutes les activités d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @return Liste des activités, triées par date décroissante
     * @throws SQLException Si une erreur survient
     */
    public List<Activity> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM activities WHERE user_id=? ORDER BY created_at DESC";
        List<Activity> activities = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                activities.add(mapResultSetToActivity(rs));
            }
        }

        return activities;
    }

    /**
     * Récupère toutes les activités récentes (dernières N).
     * 
     * @param limit Nombre maximum d'activités à récupérer
     * @return Liste des activités récentes
     * @throws SQLException Si une erreur survient
     */
    public List<Activity> findRecent(Integer limit) throws SQLException {
        String sql = "SELECT * FROM activities ORDER BY created_at DESC LIMIT ?";
        List<Activity> activities = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                activities.add(mapResultSetToActivity(rs));
            }
        }

        return activities;
    }

    /**
     * Récupère les activités par type.
     * 
     * @param type Type d'activité
     * @return Liste des activités du type spécifié
     * @throws SQLException Si une erreur survient
     */
    public List<Activity> findByType(String type) throws SQLException {
        String sql = "SELECT * FROM activities WHERE type=? ORDER BY created_at DESC";
        List<Activity> activities = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                activities.add(mapResultSetToActivity(rs));
            }
        }

        return activities;
    }

    /**
     * Mappe un ResultSet vers un objet Activity.
     * 
     * @param rs Le ResultSet à mapper
     * @return L'objet Activity mappé
     * @throws SQLException Si une erreur survient lors du mapping
     */
    private Activity mapResultSetToActivity(ResultSet rs) throws SQLException {
        Activity activity = new Activity();
        activity.setId(rs.getInt("id"));
        activity.setUserId(rs.getObject("user_id", Integer.class));
        activity.setType(rs.getString("type"));
        activity.setDescription(rs.getString("description"));
        activity.setEntityType(rs.getString("entity_type"));
        activity.setEntityId(rs.getObject("entity_id", Integer.class));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            activity.setCreatedAt(com.example.demo.utils.DateUtils.parseDateTime(createdAtStr));
        }

        return activity;
    }
}

