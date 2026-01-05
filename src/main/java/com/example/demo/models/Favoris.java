package com.example.demo.models;

import java.time.LocalDateTime;

/**
 * Modèle représentant un favori/bookmark utilisateur.
 * 
 * <p>Permet aux utilisateurs de marquer des pages comme favorites
 * pour un accès rapide.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class Favoris {
    private Integer id;
    private Integer userId;
    private String pageName;
    private LocalDateTime createdAt;

    /**
     * Constructeur par défaut
     */
    public Favoris() {
    }

    /**
     * Constructeur avec paramètres essentiels
     * 
     * @param userId ID de l'utilisateur
     * @param pageName Nom de la page à marquer comme favorite
     */
    public Favoris(Integer userId, String pageName) {
        this.userId = userId;
        this.pageName = pageName;
        this.createdAt = LocalDateTime.now();
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

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Favoris{" +
                "id=" + id +
                ", userId=" + userId +
                ", pageName='" + pageName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}






