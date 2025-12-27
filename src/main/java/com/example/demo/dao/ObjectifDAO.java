package com.example.demo.dao;

import com.example.demo.models.Objectif;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des objectifs du système.
 * 
 * <p>Gère les opérations CRUD sur les objectifs et fournit
 * des méthodes pour récupérer les objectifs actifs par type.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class ObjectifDAO {
    private static final Logger logger = Logger.getLogger(ObjectifDAO.class.getName());

    /**
     * Crée un nouvel objectif dans la base de données.
     * 
     * @param objectif L'objectif à créer
     * @return L'objectif créé avec son ID généré
     * @throws SQLException Si une erreur survient lors de l'insertion
     */
    public Objectif create(Objectif objectif) throws SQLException {
        String sql = """
            INSERT INTO objectifs (type, valeur, date_debut, date_fin, actif, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, objectif.getType());
            stmt.setDouble(2, objectif.getValeur());
            stmt.setString(3, objectif.getDateDebut() != null 
                ? objectif.getDateDebut().toString() 
                : LocalDate.now().toString());
            stmt.setString(4, objectif.getDateFin() != null 
                ? objectif.getDateFin().toString() 
                : null);
            stmt.setInt(5, objectif.isActif() ? 1 : 0);
            stmt.setString(6, objectif.getCreatedAt() != null 
                ? objectif.getCreatedAt().toString() 
                : LocalDate.now().toString());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion de l'objectif");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    objectif.setId(generatedKeys.getInt(1));
                } else {
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT LAST_INSERT_ID() as id")) {
                        if (rs.next()) {
                            objectif.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            conn.commit(); // ✅ Commiter la transaction
            logger.info("Objectif créé: " + objectif.getType());
            return objectif;
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la création de l'objectif: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Met à jour un objectif existant.
     * 
     * @param objectif L'objectif à mettre à jour
     * @return L'objectif mis à jour
     * @throws SQLException Si une erreur survient lors de la mise à jour
     */
    public Objectif update(Objectif objectif) throws SQLException {
        String sql = """
            UPDATE objectifs SET type=?, valeur=?, date_debut=?, date_fin=?, actif=?
            WHERE id=?
        """;

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, objectif.getType());
            stmt.setDouble(2, objectif.getValeur());
            stmt.setString(3, objectif.getDateDebut() != null 
                ? objectif.getDateDebut().toString() 
                : null);
            stmt.setString(4, objectif.getDateFin() != null 
                ? objectif.getDateFin().toString() 
                : null);
            stmt.setInt(5, objectif.isActif() ? 1 : 0);
            stmt.setInt(6, objectif.getId());

            stmt.executeUpdate();
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Objectif mis à jour: " + objectif.getId());
            return objectif;
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la mise à jour de l'objectif: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Désactive un objectif (soft delete).
     * 
     * @param id ID de l'objectif à désactiver
     * @throws SQLException Si une erreur survient
     */
    public void delete(Integer id) throws SQLException {
        String sql = "UPDATE objectifs SET actif=0 WHERE id=?";

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Objectif désactivé: " + id);
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la désactivation de l'objectif: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Récupère un objectif par son ID.
     * 
     * @param id ID de l'objectif
     * @return L'objectif trouvé, ou null si non trouvé
     * @throws SQLException Si une erreur survient
     */
    public Objectif findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM objectifs WHERE id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToObjectif(rs);
            }
            return null;
        }
    }

    /**
     * Récupère l'objectif actif d'un type donné.
     * 
     * @param type Type d'objectif (ex: "taux_occupation")
     * @return L'objectif actif trouvé, ou null si non trouvé
     * @throws SQLException Si une erreur survient
     */
    public Objectif findActiveByType(String type) throws SQLException {
        String sql = """
            SELECT * FROM objectifs 
            WHERE type=? AND actif=1 
            ORDER BY date_debut DESC 
            LIMIT 1
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToObjectif(rs);
            }
            return null;
        }
    }

    /**
     * Récupère tous les objectifs actifs.
     * 
     * @return Liste des objectifs actifs
     * @throws SQLException Si une erreur survient
     */
    public List<Objectif> findAllActive() throws SQLException {
        String sql = "SELECT * FROM objectifs WHERE actif=1 ORDER BY type, date_debut DESC";
        List<Objectif> objectifs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                objectifs.add(mapResultSetToObjectif(rs));
            }
        }

        return objectifs;
    }

    /**
     * Récupère tous les objectifs.
     * 
     * @return Liste de tous les objectifs
     * @throws SQLException Si une erreur survient
     */
    public List<Objectif> findAll() throws SQLException {
        String sql = "SELECT * FROM objectifs ORDER BY type, date_debut DESC";
        List<Objectif> objectifs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                objectifs.add(mapResultSetToObjectif(rs));
            }
        }

        return objectifs;
    }

    /**
     * Mappe un ResultSet vers un objet Objectif.
     * 
     * @param rs Le ResultSet à mapper
     * @return L'objet Objectif mappé
     * @throws SQLException Si une erreur survient lors du mapping
     */
    private Objectif mapResultSetToObjectif(ResultSet rs) throws SQLException {
        Objectif objectif = new Objectif();
        objectif.setId(rs.getInt("id"));
        objectif.setType(rs.getString("type"));
        objectif.setValeur(rs.getDouble("valeur"));

        String dateDebutStr = rs.getString("date_debut");
        if (dateDebutStr != null) {
            objectif.setDateDebut(com.example.demo.utils.DateUtils.parseDate(dateDebutStr));
        }

        String dateFinStr = rs.getString("date_fin");
        if (dateFinStr != null) {
            objectif.setDateFin(com.example.demo.utils.DateUtils.parseDate(dateFinStr));
        }

        objectif.setActif(rs.getInt("actif") == 1);

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            objectif.setCreatedAt(com.example.demo.utils.DateUtils.parseDate(createdAtStr));
        }

        return objectif;
    }
}

