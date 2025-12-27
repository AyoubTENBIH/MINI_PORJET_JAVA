package com.example.demo.dao;

import com.example.demo.models.Favoris;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des favoris/bookmarks utilisateur.
 * 
 * <p>Gère les opérations CRUD sur les favoris et permet aux utilisateurs
 * de marquer des pages comme favorites pour un accès rapide.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class FavorisDAO {
    private static final Logger logger = Logger.getLogger(FavorisDAO.class.getName());

    /**
     * Ajoute une page aux favoris d'un utilisateur.
     * 
     * @param favoris Le favori à créer
     * @return Le favori créé avec son ID généré
     * @throws SQLException Si une erreur survient lors de l'insertion
     */
    public Favoris create(Favoris favoris) throws SQLException {
        String sql = """
            INSERT INTO favoris (user_id, page_name, created_at)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, favoris.getUserId());
            stmt.setString(2, favoris.getPageName());
            stmt.setString(3, favoris.getCreatedAt() != null 
                ? favoris.getCreatedAt().toString() 
                : LocalDateTime.now().toString());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion du favori");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    favoris.setId(generatedKeys.getInt(1));
                } else {
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT LAST_INSERT_ID() as id")) {
                        if (rs.next()) {
                            favoris.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            logger.info("Favori ajouté: " + favoris.getPageName() + " pour l'utilisateur " + favoris.getUserId());
            return favoris;
        }
    }

    /**
     * Supprime un favori.
     * 
     * @param id ID du favori à supprimer
     * @throws SQLException Si une erreur survient
     */
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM favoris WHERE id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            logger.info("Favori supprimé: " + id);
        }
    }

    /**
     * Supprime un favori par utilisateur et nom de page.
     * 
     * @param userId ID de l'utilisateur
     * @param pageName Nom de la page
     * @throws SQLException Si une erreur survient
     */
    public void deleteByUserAndPage(Integer userId, String pageName) throws SQLException {
        String sql = "DELETE FROM favoris WHERE user_id=? AND page_name=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, pageName);
            stmt.executeUpdate();
            logger.info("Favori supprimé: " + pageName + " pour l'utilisateur " + userId);
        }
    }

    /**
     * Vérifie si une page est dans les favoris d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param pageName Nom de la page
     * @return true si la page est dans les favoris, false sinon
     * @throws SQLException Si une erreur survient
     */
    public boolean isFavorite(Integer userId, String pageName) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM favoris WHERE user_id=? AND page_name=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, pageName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            return false;
        }
    }

    /**
     * Toggle l'état d'un favori (ajoute s'il n'existe pas, supprime s'il existe).
     * 
     * @param userId ID de l'utilisateur
     * @param pageName Nom de la page
     * @return true si la page est maintenant dans les favoris, false sinon
     * @throws SQLException Si une erreur survient
     */
    public boolean toggleFavorite(Integer userId, String pageName) throws SQLException {
        boolean isFavorite = isFavorite(userId, pageName);
        
        if (isFavorite) {
            deleteByUserAndPage(userId, pageName);
            return false;
        } else {
            Favoris favoris = new Favoris(userId, pageName);
            create(favoris);
            return true;
        }
    }

    /**
     * Récupère un favori par son ID.
     * 
     * @param id ID du favori
     * @return Le favori trouvé, ou null si non trouvé
     * @throws SQLException Si une erreur survient
     */
    public Favoris findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM favoris WHERE id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToFavoris(rs);
            }
            return null;
        }
    }

    /**
     * Récupère tous les favoris d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @return Liste des favoris, triés par date de création décroissante
     * @throws SQLException Si une erreur survient
     */
    public List<Favoris> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM favoris WHERE user_id=? ORDER BY created_at DESC";
        List<Favoris> favoris = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                favoris.add(mapResultSetToFavoris(rs));
            }
        }

        return favoris;
    }

    /**
     * Mappe un ResultSet vers un objet Favoris.
     * 
     * @param rs Le ResultSet à mapper
     * @return L'objet Favoris mappé
     * @throws SQLException Si une erreur survient lors du mapping
     */
    private Favoris mapResultSetToFavoris(ResultSet rs) throws SQLException {
        Favoris favoris = new Favoris();
        favoris.setId(rs.getInt("id"));
        favoris.setUserId(rs.getInt("user_id"));
        favoris.setPageName(rs.getString("page_name"));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            favoris.setCreatedAt(com.example.demo.utils.DateUtils.parseDateTime(createdAtStr));
        }

        return favoris;
    }
}

