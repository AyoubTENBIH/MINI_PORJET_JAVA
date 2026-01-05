package com.example.demo.services;

import com.example.demo.dao.UserPreferencesDAO;
import com.example.demo.models.UserPreferences;
import javafx.scene.Scene;
import javafx.scene.Parent;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Service pour la gestion du thème (dark/light mode) de l'application.
 * 
 * <p>Ce service gère le basculement entre les thèmes dark et light,
 * sauvegarde les préférences utilisateur et applique les styles CSS appropriés.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class ThemeService {
    private static final Logger logger = Logger.getLogger(ThemeService.class.getName());
    private static ThemeService instance;
    private UserPreferencesDAO userPreferencesDAO;
    private String currentTheme;
    private Scene currentScene;
    
    // ID de l'utilisateur actuel (à récupérer depuis le système d'authentification)
    private Integer currentUserId = 1; // Par défaut, à adapter selon votre système d'auth

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private ThemeService() {
        this.userPreferencesDAO = new UserPreferencesDAO();
        this.currentTheme = UserPreferences.THEME_DARK; // Thème par défaut
    }

    /**
     * Retourne l'instance unique du ThemeService (Singleton)
     * 
     * @return L'instance unique du ThemeService
     */
    public static synchronized ThemeService getInstance() {
        if (instance == null) {
            instance = new ThemeService();
        }
        return instance;
    }

    /**
     * Charge le thème sauvegardé pour l'utilisateur actuel.
     * 
     * @throws SQLException Si une erreur survient lors du chargement
     */
    public void loadUserTheme() throws SQLException {
        try {
            UserPreferences preferences = userPreferencesDAO.getOrCreateDefault(currentUserId);
            this.currentTheme = preferences.getTheme();
            logger.info("Thème chargé pour l'utilisateur " + currentUserId + ": " + currentTheme);
        } catch (SQLException e) {
            logger.warning("Erreur lors du chargement du thème, utilisation du thème par défaut: " + e.getMessage());
            this.currentTheme = UserPreferences.THEME_DARK;
        }
    }

    /**
     * Bascule entre le thème dark et light.
     * 
     * @param scene La scène JavaFX à laquelle appliquer le thème
     * @throws SQLException Si une erreur survient lors de la sauvegarde
     */
    public void toggleTheme(Scene scene) throws SQLException {
        this.currentScene = scene;
        
        if (UserPreferences.THEME_DARK.equals(currentTheme)) {
            setLightTheme(scene);
        } else {
            setDarkTheme(scene);
        }
        
        // Sauvegarder la préférence
        saveThemePreference();
    }

    /**
     * Applique le thème dark.
     * 
     * @param scene La scène JavaFX à laquelle appliquer le thème
     */
    public void setDarkTheme(Scene scene) {
        this.currentScene = scene;
        this.currentTheme = UserPreferences.THEME_DARK;
        
        // Retirer le thème light s'il existe
        scene.getStylesheets().removeIf(url -> url.contains("light") || url.contains("premium-light"));
        
        // Ajouter le thème dark
        String darkTheme = getClass().getResource("/css/premium-dark.css").toExternalForm();
        if (!scene.getStylesheets().contains(darkTheme)) {
            scene.getStylesheets().add(darkTheme);
        }
        
        // Appliquer la classe CSS root dark
        if (scene.getRoot() != null) {
            scene.getRoot().getStyleClass().removeAll("light-theme");
            scene.getRoot().getStyleClass().add("dark-theme");
        }
        
        logger.info("Thème dark appliqué");
    }

    /**
     * Applique le thème light.
     * 
     * @param scene La scène JavaFX à laquelle appliquer le thème
     */
    public void setLightTheme(Scene scene) {
        this.currentScene = scene;
        this.currentTheme = UserPreferences.THEME_LIGHT;
        
        // Retirer le thème dark s'il existe
        scene.getStylesheets().removeIf(url -> url.contains("dark") || url.contains("premium-dark"));
        
        // Ajouter le thème light (si le fichier existe)
        // Pour l'instant, on garde le dark mais on pourrait créer un fichier premium-light.css
        String lightTheme = getClass().getResource("/css/premium-dark.css").toExternalForm();
        if (!scene.getStylesheets().contains(lightTheme)) {
            scene.getStylesheets().add(lightTheme);
        }
        
        // Appliquer la classe CSS root light
        if (scene.getRoot() != null) {
            scene.getRoot().getStyleClass().removeAll("dark-theme");
            scene.getRoot().getStyleClass().add("light-theme");
        }
        
        logger.info("Thème light appliqué");
    }

    /**
     * Applique le thème actuel à une scène.
     * 
     * @param scene La scène JavaFX à laquelle appliquer le thème
     */
    public void applyCurrentTheme(Scene scene) {
        this.currentScene = scene;
        
        if (UserPreferences.THEME_LIGHT.equals(currentTheme)) {
            setLightTheme(scene);
        } else {
            setDarkTheme(scene);
        }
    }

    /**
     * Sauvegarde la préférence de thème de l'utilisateur.
     * 
     * @throws SQLException Si une erreur survient lors de la sauvegarde
     */
    private void saveThemePreference() throws SQLException {
        try {
            UserPreferences preferences = userPreferencesDAO.getOrCreateDefault(currentUserId);
            preferences.setTheme(currentTheme);
            userPreferencesDAO.update(preferences);
            logger.info("Préférence de thème sauvegardée: " + currentTheme);
        } catch (SQLException e) {
            logger.severe("Erreur lors de la sauvegarde du thème: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retourne le thème actuel.
     * 
     * @return Le thème actuel ("dark" ou "light")
     */
    public String getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Vérifie si le thème actuel est dark.
     * 
     * @return true si le thème est dark, false sinon
     */
    public boolean isDarkTheme() {
        return UserPreferences.THEME_DARK.equals(currentTheme);
    }

    /**
     * Vérifie si le thème actuel est light.
     * 
     * @return true si le thème est light, false sinon
     */
    public boolean isLightTheme() {
        return UserPreferences.THEME_LIGHT.equals(currentTheme);
    }

    /**
     * Définit l'ID de l'utilisateur actuel.
     * 
     * @param userId ID de l'utilisateur
     */
    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
    }
}






