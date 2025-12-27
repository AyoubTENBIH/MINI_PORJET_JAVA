package com.example.demo.models;

import java.time.LocalDateTime;

/**
 * Modèle représentant les préférences utilisateur.
 * 
 * <p>Stocke les préférences de l'utilisateur comme le thème (dark/light),
 * la langue, l'état de la sidebar, etc.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class UserPreferences {
    private Integer id;
    private Integer userId;
    private String theme;
    private String language;
    private Boolean sidebarCollapsed;
    private LocalDateTime updatedAt;

    /**
     * Constantes pour les thèmes
     */
    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";

    /**
     * Constantes pour les langues
     */
    public static final String LANGUAGE_FR = "fr";
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_AR = "ar";

    /**
     * Constructeur par défaut avec valeurs par défaut
     */
    public UserPreferences() {
        this.theme = THEME_DARK;
        this.language = LANGUAGE_FR;
        this.sidebarCollapsed = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructeur avec userId
     * 
     * @param userId ID de l'utilisateur
     */
    public UserPreferences(Integer userId) {
        this.userId = userId;
        this.theme = THEME_DARK;
        this.language = LANGUAGE_FR;
        this.sidebarCollapsed = false;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters et Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getSidebarCollapsed() {
        return sidebarCollapsed;
    }

    public void setSidebarCollapsed(Boolean sidebarCollapsed) {
        this.sidebarCollapsed = sidebarCollapsed;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean isSidebarCollapsed() {
        return sidebarCollapsed != null && sidebarCollapsed;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Toggle le thème entre dark et light
     */
    public void toggleTheme() {
        this.theme = THEME_DARK.equals(this.theme) ? THEME_LIGHT : THEME_DARK;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Toggle l'état de la sidebar
     */
    public void toggleSidebar() {
        this.sidebarCollapsed = !isSidebarCollapsed();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "UserPreferences{" +
                "id=" + id +
                ", userId=" + userId +
                ", theme='" + theme + '\'' +
                ", language='" + language + '\'' +
                ", sidebarCollapsed=" + sidebarCollapsed +
                ", updatedAt=" + updatedAt +
                '}';
    }
}



