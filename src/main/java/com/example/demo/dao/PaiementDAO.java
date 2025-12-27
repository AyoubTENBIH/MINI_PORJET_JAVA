package com.example.demo.dao;

import com.example.demo.models.Paiement;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO (Data Access Object) pour la gestion des paiements.
 * 
 * <p>Cette classe fournit toutes les opérations CRUD (Create, Read, Update, Delete)
 * pour la table des paiements, ainsi que des méthodes spécialisées pour les calculs
 * de revenus et statistiques financières.</p>
 * 
 * <p>Méthodes principales :
 * <ul>
 *   <li>{@link #create(Paiement)} - Créer un nouveau paiement</li>
 *   <li>{@link #findByAdherentId(Integer)} - Récupérer les paiements d'un adhérent</li>
 *   <li>{@link #findAll()} - Récupérer tous les paiements</li>
 *   <li>{@link #getRevenusMois(LocalDate)} - Récupérer les revenus d'un mois spécifique</li>
 *   <li>{@link #getRevenusParMois(int)} - Récupérer les revenus des N derniers mois</li>
 *   <li>{@link #getTauxMoyen()} - Calculer le montant moyen des paiements</li>
 * </ul>
 * </p>
 * 
 * @author Dashboard Team
 * @version 1.0
 * @see Paiement
 * @see DatabaseManager
 * @see com.example.demo.models.MonthlyRevenue
 */
public class PaiementDAO {
    private static final Logger logger = Logger.getLogger(PaiementDAO.class.getName());

    public Paiement create(Paiement paiement) throws SQLException {
        String sql = """
            INSERT INTO paiements (adherent_id, pack_id, montant, date_paiement, methode_paiement, 
                                 statut, reference, date_debut_abonnement, date_fin_abonnement, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, paiement.getAdherentId());
            stmt.setObject(2, paiement.getPackId());
            stmt.setDouble(3, paiement.getMontant());
            stmt.setString(4, paiement.getDatePaiement().toString());
            stmt.setString(5, paiement.getMethodePaiement().name());
            stmt.setString(6, paiement.getStatut().name());
            stmt.setString(7, paiement.getReference());
            stmt.setString(8, paiement.getDateDebutAbonnement() != null ? paiement.getDateDebutAbonnement().toString() : null);
            stmt.setString(9, paiement.getDateFinAbonnement() != null ? paiement.getDateFinAbonnement().toString() : null);
            stmt.setString(10, paiement.getNotes());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion du paiement");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    paiement.setId(generatedKeys.getInt(1));
                } else {
                    // Fallback: utiliser LAST_INSERT_ID() si RETURN_GENERATED_KEYS ne fonctionne pas
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT LAST_INSERT_ID() as id")) {
                        if (rs.next()) {
                            paiement.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            conn.commit(); // ✅ Commiter la transaction
            logger.info("Paiement créé: " + paiement.getId());
            return paiement;
        } catch (SQLException e) {
            conn.rollback(); // ✅ Rollback en cas d'erreur
            logger.severe("Erreur lors de la création du paiement: " + e.getMessage());
            throw e;
        }
    }

    public List<Paiement> findByAdherentId(Integer adherentId) throws SQLException {
        String sql = "SELECT * FROM paiements WHERE adherent_id=? ORDER BY date_paiement DESC";
        List<Paiement> paiements = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, adherentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }
        }

        return paiements;
    }

    public List<Paiement> findAll() throws SQLException {
        String sql = "SELECT * FROM paiements ORDER BY date_paiement DESC";
        List<Paiement> paiements = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }
        }

        return paiements;
    }

    /**
     * Récupère les revenus pour un mois spécifique.
     * 
     * @param mois Le mois pour lequel récupérer les revenus
     * @return Le montant total des revenus pour ce mois
     * @throws SQLException Si une erreur survient lors de la requête
     */
    public Double getRevenusMois(LocalDate mois) throws SQLException {
        // MySQL utilise DATE_FORMAT pour formater les dates
        String sql = """
            SELECT SUM(montant) as total FROM paiements 
            WHERE statut='VALIDE' 
            AND DATE_FORMAT(date_paiement, '%Y-%m') = ?
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String moisStr = mois.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            stmt.setString(1, moisStr);
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Double total = rs.getDouble("total");
                return rs.wasNull() ? 0.0 : total;
            }
            return 0.0;
        }
    }

    /**
     * Récupère les revenus par mois pour les N derniers mois.
     * 
     * <p>Cette méthode retourne une liste de MonthlyRevenue triée chronologiquement,
     * avec un objet pour chaque mois (même si le revenu est 0).</p>
     * 
     * @param nombreMois Nombre de mois à récupérer (en remontant depuis aujourd'hui)
     * @return Liste des revenus mensuels, triée du plus ancien au plus récent
     * @throws SQLException Si une erreur survient lors de la requête
     */
    public List<com.example.demo.models.MonthlyRevenue> getRevenusParMois(int nombreMois) throws SQLException {
        List<com.example.demo.models.MonthlyRevenue> revenus = new ArrayList<>();
        
        // Calculer les dates de début et de fin
        LocalDate aujourdhui = LocalDate.now();
        LocalDate premierMois = aujourdhui.minusMonths(nombreMois - 1).withDayOfMonth(1);
        
        // Pour chaque mois, récupérer les revenus
        for (int i = 0; i < nombreMois; i++) {
            LocalDate mois = premierMois.plusMonths(i);
            Double montant = getRevenusMois(mois);
            
            com.example.demo.models.MonthlyRevenue monthlyRevenue = 
                new com.example.demo.models.MonthlyRevenue(mois, montant);
            revenus.add(monthlyRevenue);
        }
        
        logger.info("Revenus par mois récupérés: " + revenus.size() + " mois");
        return revenus;
    }

    /**
     * Calcule le taux moyen des paiements.
     * 
     * <p>Le taux moyen est calculé comme la moyenne des montants
     * de tous les paiements valides.</p>
     * 
     * @return Le taux moyen des paiements, ou 0.0 si aucun paiement
     * @throws SQLException Si une erreur survient lors de la requête
     */
    public Double getTauxMoyen() throws SQLException {
        String sql = "SELECT AVG(montant) as moyenne FROM paiements WHERE statut='VALIDE'";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                Double moyenne = rs.getDouble("moyenne");
                return rs.wasNull() ? 0.0 : moyenne;
            }
            return 0.0;
        }
    }
    
    private Paiement mapResultSetToPaiement(ResultSet rs) throws SQLException {
        Paiement paiement = new Paiement();
        paiement.setId(rs.getInt("id"));
        paiement.setAdherentId(rs.getInt("adherent_id"));
        paiement.setPackId(rs.getObject("pack_id", Integer.class));
        paiement.setMontant(rs.getDouble("montant"));
        
        String datePaiementStr = rs.getString("date_paiement");
        if (datePaiementStr != null && !datePaiementStr.isEmpty()) {
            LocalDateTime dateTime = com.example.demo.utils.DateUtils.parseDateTime(datePaiementStr);
            paiement.setDatePaiement(dateTime != null ? dateTime : LocalDateTime.now());
        }
        
        try {
            paiement.setMethodePaiement(Paiement.MethodePaiement.valueOf(rs.getString("methode_paiement")));
        } catch (Exception e) {
            paiement.setMethodePaiement(Paiement.MethodePaiement.ESPECES);
        }
        
        try {
            paiement.setStatut(Paiement.StatutPaiement.valueOf(rs.getString("statut")));
        } catch (Exception e) {
            paiement.setStatut(Paiement.StatutPaiement.VALIDE);
        }
        
        paiement.setReference(rs.getString("reference"));
        
        String dateDebutStr = rs.getString("date_debut_abonnement");
        if (dateDebutStr != null) {
            paiement.setDateDebutAbonnement(com.example.demo.utils.DateUtils.parseDate(dateDebutStr));
        }
        
        String dateFinStr = rs.getString("date_fin_abonnement");
        if (dateFinStr != null) {
            paiement.setDateFinAbonnement(com.example.demo.utils.DateUtils.parseDate(dateFinStr));
        }
        
        paiement.setNotes(rs.getString("notes"));
        return paiement;
    }
}

