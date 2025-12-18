package com.example.demo.dao;

import com.example.demo.models.Pack;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des packs/abonnements
 */
public class PackDAO {
    private static final Logger logger = Logger.getLogger(PackDAO.class.getName());

    /**
     * Insère un nouveau pack dans la base de données
     */
    public Pack create(Pack pack) throws SQLException {
        String sql = """
            INSERT INTO packs (nom, prix, activites, jours_disponibilite, horaires, duree, 
                             unite_duree, seances_semaine, acces_coach, actif, description, date_creation)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, pack.getNom());
            stmt.setDouble(2, pack.getPrix());
            stmt.setString(3, pack.getActivitesAsString());
            stmt.setString(4, pack.getJoursDisponibilite());
            stmt.setString(5, pack.getHoraires());
            stmt.setInt(6, pack.getDuree());
            stmt.setString(7, pack.getUniteDuree());
            stmt.setInt(8, pack.getSeancesSemaine() != null ? pack.getSeancesSemaine() : -1);
            stmt.setInt(9, pack.getAccesCoach() != null && pack.getAccesCoach() ? 1 : 0);
            stmt.setInt(10, pack.getActif() != null && pack.getActif() ? 1 : 0);
            stmt.setString(11, pack.getDescription());
            stmt.setString(12, LocalDate.now().toString());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion du pack");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pack.setId(generatedKeys.getInt(1));
                } else {
                    // Fallback: utiliser last_insert_rowid() si RETURN_GENERATED_KEYS ne fonctionne pas
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid() as id")) {
                        if (rs.next()) {
                            pack.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            logger.info("Pack créé avec succès: " + pack.getNom());
            return pack;
        }
    }

    /**
     * Met à jour un pack existant
     */
    public Pack update(Pack pack) throws SQLException {
        String sql = """
            UPDATE packs SET nom=?, prix=?, activites=?, jours_disponibilite=?, horaires=?, 
                           duree=?, unite_duree=?, seances_semaine=?, acces_coach=?, actif=?, description=?
            WHERE id=?
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pack.getNom());
            stmt.setDouble(2, pack.getPrix());
            stmt.setString(3, pack.getActivitesAsString());
            stmt.setString(4, pack.getJoursDisponibilite());
            stmt.setString(5, pack.getHoraires());
            stmt.setInt(6, pack.getDuree());
            stmt.setString(7, pack.getUniteDuree());
            stmt.setInt(8, pack.getSeancesSemaine() != null ? pack.getSeancesSemaine() : -1);
            stmt.setInt(9, pack.getAccesCoach() != null && pack.getAccesCoach() ? 1 : 0);
            stmt.setInt(10, pack.getActif() != null && pack.getActif() ? 1 : 0);
            stmt.setString(11, pack.getDescription());
            stmt.setInt(12, pack.getId());

            stmt.executeUpdate();
            logger.info("Pack mis à jour avec succès: " + pack.getNom());
            return pack;
        }
    }

    /**
     * Supprime un pack (soft delete)
     */
    public void delete(Integer id) throws SQLException {
        String sql = "UPDATE packs SET actif=0 WHERE id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            logger.info("Pack désactivé: " + id);
        }
    }

    /**
     * Récupère un pack par son ID
     */
    public Pack findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM packs WHERE id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPack(rs);
            }
            return null;
        }
    }

    /**
     * Récupère tous les packs actifs
     */
    public List<Pack> findAll() throws SQLException {
        return findAll(true);
    }

    /**
     * Récupère tous les packs (actifs ou tous)
     */
    public List<Pack> findAll(Boolean actifsSeulement) throws SQLException {
        String sql = actifsSeulement 
            ? "SELECT * FROM packs WHERE actif=1 ORDER BY nom"
            : "SELECT * FROM packs ORDER BY nom";

        List<Pack> packs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                packs.add(mapResultSetToPack(rs));
            }
        }

        return packs;
    }

    /**
     * Recherche des packs par nom
     */
    public List<Pack> searchByNom(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM packs WHERE actif=1 AND nom LIKE ? ORDER BY nom";
        List<Pack> packs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                packs.add(mapResultSetToPack(rs));
            }
        }

        return packs;
    }

    /**
     * Mappe un ResultSet vers un objet Pack
     */
    private Pack mapResultSetToPack(ResultSet rs) throws SQLException {
        Pack pack = new Pack();
        pack.setId(rs.getInt("id"));
        pack.setNom(rs.getString("nom"));
        pack.setPrix(rs.getDouble("prix"));
        pack.setActivitesFromString(rs.getString("activites"));
        pack.setJoursDisponibilite(rs.getString("jours_disponibilite"));
        pack.setHoraires(rs.getString("horaires"));
        pack.setDuree(rs.getInt("duree"));
        pack.setUniteDuree(rs.getString("unite_duree"));
        pack.setSeancesSemaine(rs.getInt("seances_semaine"));
        pack.setAccesCoach(rs.getInt("acces_coach") == 1);
        pack.setActif(rs.getInt("actif") == 1);
        pack.setDescription(rs.getString("description"));
        
        String dateCreationStr = rs.getString("date_creation");
        if (dateCreationStr != null) {
            pack.setDateCreation(com.example.demo.utils.DateUtils.parseDate(dateCreationStr));
        }

        return pack;
    }
}

