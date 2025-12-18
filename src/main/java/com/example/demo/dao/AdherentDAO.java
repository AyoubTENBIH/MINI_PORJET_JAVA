package com.example.demo.dao;

import com.example.demo.models.Adherent;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des adhérents
 */
public class AdherentDAO {
    private static final Logger logger = Logger.getLogger(AdherentDAO.class.getName());

    /**
     * Insère un nouvel adhérent
     */
    public Adherent create(Adherent adherent) throws SQLException {
        String sql = """
            INSERT INTO adherents (cin, nom, prenom, date_naissance, telephone, email, adresse, 
                                  photo, poids, taille, objectifs, problemes_sante, pack_id, 
                                  date_debut, date_fin, actif, date_inscription)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setAdherentParameters(stmt, adherent);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion de l'adhérent");
            }

            // Récupérer l'ID généré - SQLite supporte RETURN_GENERATED_KEYS mais utilisons last_insert_rowid() pour plus de compatibilité
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    adherent.setId(generatedKeys.getInt(1));
                } else {
                    // Fallback: utiliser last_insert_rowid() si RETURN_GENERATED_KEYS ne fonctionne pas
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid() as id")) {
                        if (rs.next()) {
                            adherent.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            logger.info("Adhérent créé: " + adherent.getNomComplet());
            return adherent;
        }
    }

    /**
     * Met à jour un adhérent
     */
    public Adherent update(Adherent adherent) throws SQLException {
        String sql = """
            UPDATE adherents SET cin=?, nom=?, prenom=?, date_naissance=?, telephone=?, email=?, 
                                adresse=?, photo=?, poids=?, taille=?, objectifs=?, problemes_sante=?, 
                                pack_id=?, date_debut=?, date_fin=?, actif=?
            WHERE id=?
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setAdherentParametersForUpdate(stmt, adherent);
            stmt.setInt(17, adherent.getId());

            stmt.executeUpdate();
            logger.info("Adhérent mis à jour: " + adherent.getNomComplet());
            return adherent;
        }
    }

    /**
     * Supprime un adhérent (soft delete)
     */
    public void delete(Integer id) throws SQLException {
        String sql = "UPDATE adherents SET actif=0 WHERE id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            logger.info("Adhérent désactivé: " + id);
        }
    }

    /**
     * Récupère un adhérent par ID
     */
    public Adherent findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM adherents WHERE id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAdherent(rs);
            }
            return null;
        }
    }

    /**
     * Recherche d'adhérents par critères
     */
    public List<Adherent> search(String searchTerm) throws SQLException {
        String sql = """
            SELECT * FROM adherents 
            WHERE actif=1 AND (
                nom LIKE ? OR prenom LIKE ? OR cin LIKE ? OR telephone LIKE ? OR email LIKE ?
            )
            ORDER BY nom, prenom
        """;

        List<Adherent> adherents = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 5; i++) {
                stmt.setString(i, searchPattern);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                adherents.add(mapResultSetToAdherent(rs));
            }
        }

        return adherents;
    }

    /**
     * Récupère tous les adhérents actifs
     */
    public List<Adherent> findAll() throws SQLException {
        String sql = "SELECT * FROM adherents WHERE actif=1 ORDER BY nom, prenom";
        List<Adherent> adherents = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                adherents.add(mapResultSetToAdherent(rs));
            }
        }

        return adherents;
    }

    /**
     * Récupère les adhérents avec abonnement expiré
     */
    public List<Adherent> findExpired() throws SQLException {
        String sql = "SELECT * FROM adherents WHERE actif=1 AND date_fin < date('now') ORDER BY date_fin";
        List<Adherent> adherents = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                adherents.add(mapResultSetToAdherent(rs));
            }
        }

        return adherents;
    }

    /**
     * Récupère les adhérents dont l'abonnement expire bientôt (7 jours)
     */
    public List<Adherent> findExpiringSoon() throws SQLException {
        String sql = """
            SELECT * FROM adherents 
            WHERE actif=1 AND date_fin BETWEEN date('now') AND date('now', '+7 days')
            ORDER BY date_fin
        """;
        List<Adherent> adherents = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                adherents.add(mapResultSetToAdherent(rs));
            }
        }

        return adherents;
    }

    /**
     * Définit les paramètres d'un PreparedStatement pour un INSERT d'adhérent
     */
    private void setAdherentParameters(PreparedStatement stmt, Adherent adherent) throws SQLException {
        stmt.setString(1, adherent.getCin());
        stmt.setString(2, adherent.getNom());
        stmt.setString(3, adherent.getPrenom());
        stmt.setString(4, adherent.getDateNaissance() != null ? adherent.getDateNaissance().toString() : null);
        stmt.setString(5, adherent.getTelephone());
        stmt.setString(6, adherent.getEmail());
        stmt.setString(7, adherent.getAdresse());
        stmt.setString(8, adherent.getPhoto());
        stmt.setObject(9, adherent.getPoids());
        stmt.setObject(10, adherent.getTaille());
        stmt.setString(11, adherent.getObjectifs());
        stmt.setString(12, adherent.getProblemesSante());
        stmt.setObject(13, adherent.getPackId());
        stmt.setString(14, adherent.getDateDebut() != null ? adherent.getDateDebut().toString() : null);
        stmt.setString(15, adherent.getDateFin() != null ? adherent.getDateFin().toString() : null);
        stmt.setInt(16, adherent.getActif() != null && adherent.getActif() ? 1 : 0);
        stmt.setString(17, adherent.getDateInscription() != null ? adherent.getDateInscription().toString() : LocalDate.now().toString());
    }

    /**
     * Définit les paramètres d'un PreparedStatement pour un UPDATE d'adhérent
     */
    private void setAdherentParametersForUpdate(PreparedStatement stmt, Adherent adherent) throws SQLException {
        stmt.setString(1, adherent.getCin());
        stmt.setString(2, adherent.getNom());
        stmt.setString(3, adherent.getPrenom());
        stmt.setString(4, adherent.getDateNaissance() != null ? adherent.getDateNaissance().toString() : null);
        stmt.setString(5, adherent.getTelephone());
        stmt.setString(6, adherent.getEmail());
        stmt.setString(7, adherent.getAdresse());
        stmt.setString(8, adherent.getPhoto());
        stmt.setObject(9, adherent.getPoids());
        stmt.setObject(10, adherent.getTaille());
        stmt.setString(11, adherent.getObjectifs());
        stmt.setString(12, adherent.getProblemesSante());
        stmt.setObject(13, adherent.getPackId());
        stmt.setString(14, adherent.getDateDebut() != null ? adherent.getDateDebut().toString() : null);
        stmt.setString(15, adherent.getDateFin() != null ? adherent.getDateFin().toString() : null);
        stmt.setInt(16, adherent.getActif() != null && adherent.getActif() ? 1 : 0);
    }

    /**
     * Mappe un ResultSet vers un objet Adherent
     */
    private Adherent mapResultSetToAdherent(ResultSet rs) throws SQLException {
        Adherent adherent = new Adherent();
        adherent.setId(rs.getInt("id"));
        adherent.setCin(rs.getString("cin"));
        adherent.setNom(rs.getString("nom"));
        adherent.setPrenom(rs.getString("prenom"));
        
        String dateNaissanceStr = rs.getString("date_naissance");
        if (dateNaissanceStr != null) {
            adherent.setDateNaissance(com.example.demo.utils.DateUtils.parseDate(dateNaissanceStr));
        }
        
        adherent.setTelephone(rs.getString("telephone"));
        adherent.setEmail(rs.getString("email"));
        adherent.setAdresse(rs.getString("adresse"));
        adherent.setPhoto(rs.getString("photo"));
        
        Double poids = rs.getObject("poids", Double.class);
        if (poids != null) adherent.setPoids(poids);
        
        Double taille = rs.getObject("taille", Double.class);
        if (taille != null) adherent.setTaille(taille);
        
        adherent.setObjectifs(rs.getString("objectifs"));
        adherent.setProblemesSante(rs.getString("problemes_sante"));
        adherent.setPackId(rs.getObject("pack_id", Integer.class));
        
        String dateDebutStr = rs.getString("date_debut");
        if (dateDebutStr != null) {
            adherent.setDateDebut(com.example.demo.utils.DateUtils.parseDate(dateDebutStr));
        }
        
        String dateFinStr = rs.getString("date_fin");
        if (dateFinStr != null) {
            adherent.setDateFin(com.example.demo.utils.DateUtils.parseDate(dateFinStr));
        }
        
        adherent.setActif(rs.getInt("actif") == 1);
        
        String dateInscriptionStr = rs.getString("date_inscription");
        if (dateInscriptionStr != null) {
            adherent.setDateInscription(com.example.demo.utils.DateUtils.parseDate(dateInscriptionStr));
        }

        return adherent;
    }
}

