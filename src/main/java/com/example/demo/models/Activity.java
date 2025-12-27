package com.example.demo.models;

import java.time.LocalDateTime;

/**
 * Modèle représentant une activité dans le système.
 * 
 * <p>Les activités sont utilisées pour tracer les actions effectuées
 * dans le système (création de packs, archivage, modifications, etc.)</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class Activity {
    private Integer id;
    private Integer userId;
    private String type;
    private String description;
    private String entityType;
    private Integer entityId;
    private LocalDateTime createdAt;

    /**
     * Constructeur par défaut
     */
    public Activity() {
    }

    /**
     * Constructeur avec paramètres essentiels
     * 
     * @param userId ID de l'utilisateur ayant effectué l'action
     * @param type Type d'activité (STYLE_CHANGED, PRODUCT_ADDED, etc.)
     * @param description Description de l'activité
     */
    public Activity(Integer userId, String type, String description) {
        this.userId = userId;
        this.type = type;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructeur complet avec entité associée
     * 
     * @param userId ID de l'utilisateur
     * @param type Type d'activité
     * @param description Description
     * @param entityType Type d'entité (pack, adherent, page, etc.)
     * @param entityId ID de l'entité
     */
    public Activity(Integer userId, String type, String description, String entityType, Integer entityId) {
        this.userId = userId;
        this.type = type;
        this.description = description;
        this.entityType = entityType;
        this.entityId = entityId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", createdAt=" + createdAt +
                '}';
    }
}



