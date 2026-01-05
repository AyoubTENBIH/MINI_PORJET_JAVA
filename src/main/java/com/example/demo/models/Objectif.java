package com.example.demo.models;

import java.time.LocalDate;

/**
 * Modèle représentant un objectif dans le système.
 * 
 * <p>Les objectifs sont utilisés pour définir des cibles à atteindre
 * (taux d'occupation, revenus, etc.)</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class Objectif {
    private Integer id;
    private String type;
    private Double valeur;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean actif;
    private LocalDate createdAt;

    /**
     * Constructeur par défaut
     */
    public Objectif() {
        this.actif = true;
    }

    /**
     * Constructeur avec paramètres essentiels
     * 
     * @param type Type d'objectif (taux_occupation, revenus, etc.)
     * @param valeur Valeur de l'objectif
     * @param dateDebut Date de début de l'objectif
     */
    public Objectif(String type, Double valeur, LocalDate dateDebut) {
        this.type = type;
        this.valeur = valeur;
        this.dateDebut = dateDebut;
        this.actif = true;
        this.createdAt = LocalDate.now();
    }

    // Getters et Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getValeur() {
        return valeur;
    }

    public void setValeur(Double valeur) {
        this.valeur = valeur;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Boolean isActif() {
        return actif != null && actif;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Objectif{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", valeur=" + valeur +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", actif=" + actif +
                '}';
    }
}






