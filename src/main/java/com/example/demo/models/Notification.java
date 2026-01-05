package com.example.demo.models;

import java.time.LocalDateTime;

/**
 * Modèle représentant une notification utilisateur dans le système.
 * 
 * <p>Les notifications sont utilisées pour informer les utilisateurs
 * des événements importants (nouveaux adhérents, paiements, etc.)</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class Notification {
    private Integer id;
    private Integer userId;
    private String type;
    private String title;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;

    /**
     * Constructeur par défaut
     */
    public Notification() {
        this.read = false;
    }

    /**
     * Constructeur avec paramètres essentiels
     * 
     * @param userId ID de l'utilisateur destinataire
     * @param type Type de notification (NEW_USER, NEW_PAYMENT, etc.)
     * @param title Titre de la notification
     * @param message Message de la notification
     */
    public Notification(Integer userId, String type, String title, String message) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.read = false;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean isRead() {
        return read != null && read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", read=" + read +
                ", createdAt=" + createdAt +
                '}';
    }
}






