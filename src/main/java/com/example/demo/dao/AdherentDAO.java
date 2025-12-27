package com.example.demo.dao;

import com.example.demo.models.Adherent;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO (Data Access Object) pour la gestion des adhérents.
 * 
 * <p>Cette classe fournit toutes les opérations CRUD (Create, Read, Update, Delete)
 * pour la table des adhérents, ainsi que des méthodes spécialisées pour les recherches,
 * filtres et calculs statistiques.</p>
 * 
 * <p>Méthodes principales :
 * <ul>
 *   <li>{@link #create(Adherent)} - Créer un nouvel adhérent</li>
 *   <li>{@link #update(Adherent)} - Mettre à jour un adhérent existant</li>
 *   <li>{@link #delete(Integer)} - Désactiver un adhérent (soft delete)</li>
 *   <li>{@link #findById(Integer)} - Récupérer un adhérent par ID</li>
 *   <li>{@link #findAll()} - Récupérer tous les adhérents</li>
 *   <li>{@link #findAllActive()} - Récupérer tous les adhérents actifs</li>
 *   <li>{@link #search(String)} - Rechercher des adhérents par critères</li>
 *   <li>{@link #findExpiringSoon()} - Trouver les adhérents dont l'abonnement expire bientôt</li>
 *   <li>{@link #findWithPagination(int, int)} - Récupérer avec pagination</li>
 *   <li>{@link #findWithSort(String, String)} - Récupérer avec tri personnalisé</li>
 *   <li>{@link #getTauxOccupation()} - Calculer le taux d'occupation</li>
 *   <li>{@link #getMonthlyGrowth(LocalDate)} - Calculer la croissance mensuelle</li>
 * </ul>
 * </p>
 * 
 * @author Dashboard Team
 * @version 1.0
 * @see Adherent
 * @see DatabaseManager
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

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setAdherentParameters(stmt, adherent);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion de l'adhérent");
            }

            // Récupérer l'ID généré - MySQL supporte RETURN_GENERATED_KEYS et LAST_INSERT_ID()
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    adherent.setId(generatedKeys.getInt(1));
                } else {
                    // Fallback: utiliser LAST_INSERT_ID() si RETURN_GENERATED_KEYS ne fonctionne pas
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT LAST_INSERT_ID() as id")) {
                        if (rs.next()) {
                            adherent.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            conn.commit(); // ✅ Commiter la transaction
            logger.info("Adhérent créé: " + adherent.getNomComplet());
            return adherent;
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la création de l'adhérent: " + e.getMessage());
            throw e;
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

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            setAdherentParametersForUpdate(stmt, adherent);
            stmt.setInt(17, adherent.getId());

            stmt.executeUpdate();
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Adhérent mis à jour: " + adherent.getNomComplet());
            return adherent;
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la mise à jour de l'adhérent: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime un adhérent (soft delete)
     */
    public void delete(Integer id) throws SQLException {
        String sql = "UPDATE adherents SET actif=0 WHERE id=?";

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            conn.commit(); // ✅ Commiter la transaction
            logger.info("Adhérent désactivé: " + id);
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la désactivation de l'adhérent: " + e.getMessage());
            throw e;
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
        String sql = "SELECT * FROM adherents WHERE actif=1 AND date_fin < CURDATE() ORDER BY date_fin";
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
            WHERE actif=1 AND date_fin BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
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
     * Calcule le taux de croissance mensuel des adhérents.
     * 
     * <p>Compare le nombre d'adhérents actifs du mois spécifié avec le mois précédent
     * et retourne le pourcentage de changement.</p>
     * 
     * @param mois Le mois pour lequel calculer la croissance
     * @return Le pourcentage de changement (positif pour croissance, négatif pour décroissance)
     * @throws SQLException Si une erreur survient lors du calcul
     */
    public double getMonthlyGrowth(LocalDate mois) throws SQLException {
        try {
            // Compter les adhérents actifs du mois spécifié
            LocalDate debutMois = mois.withDayOfMonth(1);
            LocalDate finMois = mois.withDayOfMonth(mois.lengthOfMonth());
            
            String sql = """
                SELECT COUNT(*) as count FROM adherents 
                WHERE actif = 1 
                AND date_inscription >= ? 
                AND date_inscription <= ?
            """;
            
            int adherentsMois = 0;
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, debutMois.toString());
                stmt.setString(2, finMois.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    adherentsMois = rs.getInt("count");
                }
            }
            
            // Compter les adhérents actifs du mois précédent
            LocalDate moisPrecedent = mois.minusMonths(1);
            LocalDate debutMoisPrecedent = moisPrecedent.withDayOfMonth(1);
            LocalDate finMoisPrecedent = moisPrecedent.withDayOfMonth(moisPrecedent.lengthOfMonth());
            
            int adherentsMoisPrecedent = 0;
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, debutMoisPrecedent.toString());
                stmt.setString(2, finMoisPrecedent.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    adherentsMoisPrecedent = rs.getInt("count");
                }
            }
            
            // Calculer le pourcentage de changement
            if (adherentsMoisPrecedent > 0) {
                return ((adherentsMois - adherentsMoisPrecedent) / (double) adherentsMoisPrecedent) * 100.0;
            } else if (adherentsMois > 0) {
                return 100.0; // Croissance de 100% si aucun adhérent le mois précédent
            } else {
                return 0.0; // Pas de changement si aucun adhérent
            }
        } catch (SQLException e) {
            logger.severe("Erreur lors du calcul de la croissance mensuelle: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Calcule le taux d'occupation (adhérents actifs / objectif).
     * 
     * @return Le taux d'occupation en pourcentage (0-100)
     * @throws SQLException Si une erreur survient lors du calcul
     */
    public double getTauxOccupation() throws SQLException {
        try {
            // Récupérer l'objectif depuis ObjectifDAO
            com.example.demo.dao.ObjectifDAO objectifDAO = new com.example.demo.dao.ObjectifDAO();
            com.example.demo.models.Objectif objectif = objectifDAO.findActiveByType(
                com.example.demo.utils.DashboardConstants.OBJECTIF_TYPE_TAUX_OCCUPATION
            );
            
            // Si aucun objectif n'est défini, utiliser l'objectif par défaut
            double objectifValue = objectif != null 
                ? objectif.getValeur() 
                : com.example.demo.utils.DashboardConstants.OBJECTIF_ADHERENTS_DEFAULT;
            
            // Compter les adhérents actifs
            int adherentsActifs = findAll().size();
            
            // Calculer le taux d'occupation
            if (objectifValue > 0) {
                return (adherentsActifs / objectifValue) * 100.0;
            }
            return 0.0;
        } catch (SQLException e) {
            logger.severe("Erreur lors du calcul du taux d'occupation: " + e.getMessage());
            throw e;
        }
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

