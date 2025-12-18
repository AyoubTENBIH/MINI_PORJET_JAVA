package com.example.demo.dao;

import com.example.demo.models.Paiement;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des paiements
 */
public class PaiementDAO {
    private static final Logger logger = Logger.getLogger(PaiementDAO.class.getName());

    public Paiement create(Paiement paiement) throws SQLException {
        String sql = """
            INSERT INTO paiements (adherent_id, pack_id, montant, date_paiement, methode_paiement, 
                                 statut, reference, date_debut_abonnement, date_fin_abonnement, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
                    // Fallback: utiliser last_insert_rowid() si RETURN_GENERATED_KEYS ne fonctionne pas
                    try (Statement idStmt = conn.createStatement();
                         ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid() as id")) {
                        if (rs.next()) {
                            paiement.setId(rs.getInt("id"));
                        }
                    }
                }
            }

            logger.info("Paiement créé: " + paiement.getId());
            return paiement;
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

    public Double getRevenusMois(LocalDate mois) throws SQLException {
        String sql = """
            SELECT SUM(montant) as total FROM paiements 
            WHERE statut='VALIDE' AND strftime('%Y-%m', date_paiement) = ?
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mois.toString().substring(0, 7)); // Format YYYY-MM
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
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

