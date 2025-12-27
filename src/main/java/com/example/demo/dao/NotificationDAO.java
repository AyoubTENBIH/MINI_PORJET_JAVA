package com.example.demo.dao;

import com.example.demo.models.Notification;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des notifications utilisateur.
 * 
 * <p>Gère les opérations CRUD sur les notifications et fournit
 * des méthodes spécialisées pour récupérer les notifications non lues,
 * par utilisateur, etc.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class NotificationDAO {
    private static final Logger logger = Logger.getLogger(NotificationDAO.class.getName());

    /**
     * Crée une nouvelle notification dans la base de données.
     * 
     * @param notification La notification à créer
     * @return La notification créée avec son ID généré
     * @throws SQLException Si une erreur survient lors de l'insertion
     */
    public Notification create(Notification notification) throws SQLException {
        String sql = """
            INSERT INTO notifications (user_id, type, title, message, `read`, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setObject(1, notification.getUserId());
            stmt.setString(2, notification.getType());
            stmt.setString(3, notification.getTitle());
            stmt.setString(4, notification.getMessage());
            stmt.setInt(5, notification.isRead() ? 1 : 0);
            stmt.setString(6, notification.getCreatedAt() != null 
                ? notification.getCreatedAt().toString() 
                : LocalDateTime.now().toString());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion de la notification");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notification.setId(generatedKeys.getInt(1));
                } else {
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT LAST_INSERT_ID() as id")) {
                        if (rs.next()) {
                            notification.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            conn.commit(); // ✅ Commiter la transaction
            logger.info("Notification créée: " + notification.getTitle());
            return notification;
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la création de la notification: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Met à jour une notification existante.
     * 
     * @param notification La notification à mettre à jour
     * @return La notification mise à jour
     * @throws SQLException Si une erreur survient lors de la mise à jour
     */
    public Notification update(Notification notification) throws SQLException {
        String sql = """
            UPDATE notifications SET user_id=?, type=?, title=?, message=?, `read`=?
            WHERE id=?
        """;

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, notification.getUserId());
            stmt.setString(2, notification.getType());
            stmt.setString(3, notification.getTitle());
            stmt.setString(4, notification.getMessage());
            stmt.setInt(5, notification.isRead() ? 1 : 0);
            stmt.setInt(6, notification.getId());

            stmt.executeUpdate();
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Notification mise à jour: " + notification.getId());
            return notification;
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la mise à jour de la notification: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Marque une notification comme lue.
     * 
     * @param id ID de la notification à marquer comme lue
     * @throws SQLException Si une erreur survient
     */
    public void markAsRead(Integer id) throws SQLException {
        String sql = "UPDATE notifications SET `read`=1 WHERE id=?";

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Notification marquée comme lue: " + id);
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors du marquage de la notification: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues.
     * 
     * @param userId ID de l'utilisateur
     * @throws SQLException Si une erreur survient
     */
    public void markAllAsRead(Integer userId) throws SQLException {
        String sql = "UPDATE notifications SET `read`=1 WHERE user_id=?";

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Toutes les notifications marquées comme lues pour l'utilisateur: " + userId);
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors du marquage des notifications: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime une notification.
     * 
     * @param id ID de la notification à supprimer
     * @throws SQLException Si une erreur survient
     */
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM notifications WHERE id=?";

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Notification supprimée: " + id);
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la suppression de la notification: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Récupère une notification par son ID.
     * 
     * @param id ID de la notification
     * @return La notification trouvée, ou null si non trouvée
     * @throws SQLException Si une erreur survient
     */
    public Notification findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToNotification(rs);
            }
            return null;
        }
    }

    /**
     * Récupère toutes les notifications d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @return Liste des notifications, triées par date décroissante
     * @throws SQLException Si une erreur survient
     */
    public List<Notification> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id=? ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        }

        return notifications;
    }

    /**
     * Récupère toutes les notifications non lues d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @return Liste des notifications non lues, triées par date décroissante
     * @throws SQLException Si une erreur survient
     */
    public List<Notification> findUnreadByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id=? AND `read`=0 ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        }

        return notifications;
    }

    /**
     * Compte le nombre de notifications non lues d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications non lues
     * @throws SQLException Si une erreur survient
     */
    public int countUnreadByUserId(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM notifications WHERE user_id=? AND `read`=0";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        }
    }

    /**
     * Récupère toutes les notifications récentes (dernières N).
     * 
     * @param limit Nombre maximum de notifications à récupérer
     * @return Liste des notifications récentes
     * @throws SQLException Si une erreur survient
     */
    public List<Notification> findRecent(Integer limit) throws SQLException {
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC LIMIT ?";
        List<Notification> notifications = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        }

        return notifications;
    }

    /**
     * Vérifie si une notification similaire existe déjà aujourd'hui pour un utilisateur.
     * Utilisé pour éviter les doublons lors des vérifications périodiques.
     * 
     * @param userId ID de l'utilisateur
     * @param type Type de notification
     * @return true si une notification similaire existe aujourd'hui, false sinon
     * @throws SQLException Si une erreur survient
     */
    public boolean existsTodayByType(Integer userId, String type) throws SQLException {
        String sql = """
            SELECT COUNT(*) as count FROM notifications 
            WHERE user_id=? AND type=? 
            AND DATE(created_at) = CURDATE()
        """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        }
        
        return false;
    }

    /**
     * Mappe un ResultSet vers un objet Notification.
     * 
     * @param rs Le ResultSet à mapper
     * @return L'objet Notification mappé
     * @throws SQLException Si une erreur survient lors du mapping
     */
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setUserId(rs.getObject("user_id", Integer.class));
        notification.setType(rs.getString("type"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getInt("read") == 1);

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            notification.setCreatedAt(com.example.demo.utils.DateUtils.parseDateTime(createdAtStr));
        }

        return notification;
    }
}

