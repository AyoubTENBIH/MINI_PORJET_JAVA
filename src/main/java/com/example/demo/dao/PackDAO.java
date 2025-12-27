package com.example.demo.dao;

import com.example.demo.models.Pack;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO (Data Access Object) pour la gestion des packs/abonnements.
 * 
 * <p>Cette classe fournit toutes les opérations CRUD (Create, Read, Update, Delete)
 * pour la table des packs, ainsi que des méthodes spécialisées pour les recherches
 * et analyses de distribution.</p>
 * 
 * <p>Méthodes principales :
 * <ul>
 *   <li>{@link #create(Pack)} - Créer un nouveau pack</li>
 *   <li>{@link #update(Pack)} - Mettre à jour un pack existant</li>
 *   <li>{@link #delete(Integer)} - Désactiver un pack (soft delete)</li>
 *   <li>{@link #findById(Integer)} - Récupérer un pack par ID</li>
 *   <li>{@link #findAll()} - Récupérer tous les packs</li>
 *   <li>{@link #findAllActive()} - Récupérer tous les packs actifs</li>
 *   <li>{@link #search(String)} - Rechercher des packs par nom</li>
 *   <li>{@link #getDistributionByAdherents()} - Obtenir la distribution des packs par nombre d'adhérents</li>
 * </ul>
 * </p>
 * 
 * @author Dashboard Team
 * @version 1.0
 * @see Pack
 * @see DatabaseManager
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

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
                    // Fallback: utiliser LAST_INSERT_ID() si RETURN_GENERATED_KEYS ne fonctionne pas
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT LAST_INSERT_ID() as id")) {
                        if (rs.next()) {
                            pack.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            conn.commit(); // ✅ Commiter la transaction
            logger.info("Pack créé avec succès: " + pack.getNom());
            return pack;
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la création du pack: " + e.getMessage());
            throw e;
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

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

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
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Pack mis à jour avec succès: " + pack.getNom());
            return pack;
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la mise à jour du pack: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime un pack (soft delete)
     */
    public void delete(Integer id) throws SQLException {
        String sql = "UPDATE packs SET actif=0 WHERE id=?";

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Pack désactivé: " + id);
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la désactivation du pack: " + e.getMessage());
            throw e;
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
     * Récupère la distribution des packs par nombre d'adhérents.
     * 
     * <p>Cette méthode joint la table des packs avec la table des adhérents
     * pour compter le nombre d'adhérents par pack.</p>
     * 
     * @return Une Map associant chaque Pack à son nombre d'adhérents
     * @throws SQLException Si une erreur survient lors de la requête
     */
    public java.util.Map<Pack, Integer> getDistributionByAdherents() throws SQLException {
        java.util.Map<Pack, Integer> distribution = new java.util.HashMap<>();
        
        String sql = """
            SELECT p.id, p.nom, p.prix, p.activites, p.jours_disponibilite, p.horaires, 
                   p.duree, p.unite_duree, p.seances_semaine, p.acces_coach, p.actif, 
                   p.description, p.date_creation,
                   COUNT(a.id) as nombre_adherents
            FROM packs p
            LEFT JOIN adherents a ON p.id = a.pack_id AND a.actif = 1
            WHERE p.actif = 1
            GROUP BY p.id, p.nom, p.prix, p.activites, p.jours_disponibilite, p.horaires, 
                     p.duree, p.unite_duree, p.seances_semaine, p.acces_coach, p.actif, 
                     p.description, p.date_creation
            ORDER BY nombre_adherents DESC
        """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Pack pack = mapResultSetToPack(rs);
                int nombreAdherents = rs.getInt("nombre_adherents");
                distribution.put(pack, nombreAdherents);
            }
        }
        
        logger.info("Distribution des packs récupérée: " + distribution.size() + " packs");
        return distribution;
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

