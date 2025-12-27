package com.example.demo.services;

import com.example.demo.dao.UserPreferencesDAO;
import com.example.demo.models.UserPreferences;

import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Service pour la gestion des langues et traductions de l'application.
 * 
 * <p>Ce service gère le changement de langue, charge les fichiers de traduction
 * et sauvegarde les préférences utilisateur.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class LanguageService {
    private static final Logger logger = Logger.getLogger(LanguageService.class.getName());
    private static LanguageService instance;
    private UserPreferencesDAO userPreferencesDAO;
    private ResourceBundle currentBundle;
    private String currentLanguage;
    
    // ID de l'utilisateur actuel (à récupérer depuis le système d'authentification)
    private Integer currentUserId = 1; // Par défaut, à adapter selon votre système d'auth

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private LanguageService() {
        this.userPreferencesDAO = new UserPreferencesDAO();
        this.currentLanguage = UserPreferences.LANGUAGE_FR;
        loadLanguageBundle(this.currentLanguage);
    }

    /**
     * Retourne l'instance unique du LanguageService (Singleton)
     * 
     * @return L'instance unique du LanguageService
     */
    public static synchronized LanguageService getInstance() {
        if (instance == null) {
            instance = new LanguageService();
        }
        return instance;
    }

    /**
     * Charge le bundle de traduction pour une langue donnée.
     * 
     * @param language Code de langue ("fr", "en", "ar")
     */
    private void loadLanguageBundle(String language) {
        try {
            Locale locale = switch (language) {
                case "en" -> Locale.ENGLISH;
                case "ar" -> new Locale("ar");
                default -> Locale.FRENCH;
            };
            
            currentBundle = ResourceBundle.getBundle("messages", locale);
            this.currentLanguage = language;
            logger.info("Bundle de langue chargé: " + language);
        } catch (Exception e) {
            logger.warning("Impossible de charger le bundle pour la langue " + language + ", utilisation du français par défaut: " + e.getMessage());
            currentBundle = ResourceBundle.getBundle("messages", Locale.FRENCH);
            this.currentLanguage = UserPreferences.LANGUAGE_FR;
        }
    }

    /**
     * Charge la langue sauvegardée pour l'utilisateur actuel.
     * 
     * @throws SQLException Si une erreur survient lors du chargement
     */
    public void loadUserLanguage() throws SQLException {
        try {
            UserPreferences preferences = userPreferencesDAO.getOrCreateDefault(currentUserId);
            String language = preferences.getLanguage();
            loadLanguageBundle(language);
            logger.info("Langue chargée pour l'utilisateur " + currentUserId + ": " + language);
        } catch (SQLException e) {
            logger.warning("Erreur lors du chargement de la langue, utilisation de la langue par défaut: " + e.getMessage());
            loadLanguageBundle(UserPreferences.LANGUAGE_FR);
        }
    }

    /**
     * Change la langue de l'application.
     * 
     * @param language Code de langue ("fr", "en", "ar")
     * @throws SQLException Si une erreur survient lors de la sauvegarde
     */
    public void setLanguage(String language) throws SQLException {
        loadLanguageBundle(language);
        
        // Sauvegarder la préférence
        try {
            UserPreferences preferences = userPreferencesDAO.getOrCreateDefault(currentUserId);
            preferences.setLanguage(language);
            userPreferencesDAO.update(preferences);
            logger.info("Langue changée et sauvegardée: " + language);
        } catch (SQLException e) {
            logger.severe("Erreur lors de la sauvegarde de la langue: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retourne la traduction d'une clé.
     * 
     * @param key Clé de traduction
     * @return La traduction, ou la clé si non trouvée
     */
    public String getString(String key) {
        try {
            if (currentBundle != null && currentBundle.containsKey(key)) {
                return currentBundle.getString(key);
            }
            return key; // Retourner la clé si non trouvée
        } catch (Exception e) {
            logger.warning("Erreur lors de la récupération de la traduction pour la clé " + key + ": " + e.getMessage());
            return key;
        }
    }

    /**
     * Retourne la langue actuelle.
     * 
     * @return Le code de langue actuel
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Vérifie si la langue actuelle est le français.
     * 
     * @return true si la langue est le français, false sinon
     */
    public boolean isFrench() {
        return UserPreferences.LANGUAGE_FR.equals(currentLanguage);
    }

    /**
     * Vérifie si la langue actuelle est l'anglais.
     * 
     * @return true si la langue est l'anglais, false sinon
     */
    public boolean isEnglish() {
        return UserPreferences.LANGUAGE_EN.equals(currentLanguage);
    }

    /**
     * Vérifie si la langue actuelle est l'arabe.
     * 
     * @return true si la langue est l'arabe, false sinon
     */
    public boolean isArabic() {
        return UserPreferences.LANGUAGE_AR.equals(currentLanguage);
    }

    /**
     * Retourne la liste des langues disponibles.
     * 
     * @return Tableau des codes de langues disponibles
     */
    public String[] getAvailableLanguages() {
        return new String[]{
            UserPreferences.LANGUAGE_FR,
            UserPreferences.LANGUAGE_EN,
            UserPreferences.LANGUAGE_AR
        };
    }

    /**
     * Retourne le nom affiché d'une langue.
     * 
     * @param languageCode Code de langue
     * @return Nom affiché de la langue
     */
    public String getLanguageDisplayName(String languageCode) {
        return switch (languageCode) {
            case UserPreferences.LANGUAGE_FR -> "Français";
            case UserPreferences.LANGUAGE_EN -> "English";
            case UserPreferences.LANGUAGE_AR -> "العربية";
            default -> languageCode;
        };
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



