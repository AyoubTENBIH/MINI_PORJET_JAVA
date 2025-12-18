package com.example.demo.dao;

import com.example.demo.models.Utilisateur;
import com.example.demo.utils.DatabaseManager;

import java.sql.*;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des utilisateurs (authentification)
 */
public class UtilisateurDAO {
    private static final Logger logger = Logger.getLogger(UtilisateurDAO.class.getName());

    /**
     * Authentifie un utilisateur
     */
    public Utilisateur authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE username=? AND password=? AND actif=1";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // Dans une vraie app, utiliser un hash

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("id"));
                utilisateur.setUsername(rs.getString("username"));
                utilisateur.setPassword(rs.getString("password"));
                utilisateur.setNom(rs.getString("nom"));
                utilisateur.setPrenom(rs.getString("prenom"));
                
                try {
                    utilisateur.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
                } catch (Exception e) {
                    utilisateur.setRole(Utilisateur.Role.RECEPTIONNISTE);
                }
                
                utilisateur.setActif(rs.getInt("actif") == 1);
                logger.info("Utilisateur authentifié: " + username);
                return utilisateur;
            }
        }

        return null;
    }

    public Utilisateur findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE id=?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
        }

        return null;
    }

    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getInt("id"));
        utilisateur.setUsername(rs.getString("username"));
        utilisateur.setPassword(rs.getString("password"));
        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setPrenom(rs.getString("prenom"));
        
        try {
            utilisateur.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
        } catch (Exception e) {
            utilisateur.setRole(Utilisateur.Role.RECEPTIONNISTE);
        }
        
        utilisateur.setActif(rs.getInt("actif") == 1);
        return utilisateur;
    }
}




