package com.example.demo.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle représentant un pack/abonnement de la salle de sport
 */
public class Pack {
    private Integer id;
    private String nom;
    private Double prix;
    private List<String> activites;
    private String joursDisponibilite;
    private String horaires;
    private Integer duree;
    private String uniteDuree; // JOUR, SEMAINE, MOIS, ANNEE
    private Integer seancesSemaine; // -1 pour illimité
    private Boolean accesCoach;
    private Boolean actif;
    private String description;
    private LocalDate dateCreation;

    public Pack() {
        this.activites = new ArrayList<>();
        this.actif = true;
        this.uniteDuree = "MOIS";
    }

    public Pack(String nom, Double prix) {
        this();
        this.nom = nom;
        this.prix = prix;
    }

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public List<String> getActivites() {
        return activites;
    }

    public void setActivites(List<String> activites) {
        this.activites = activites;
    }

    public String getJoursDisponibilite() {
        return joursDisponibilite;
    }

    public void setJoursDisponibilite(String joursDisponibilite) {
        this.joursDisponibilite = joursDisponibilite;
    }

    public String getHoraires() {
        return horaires;
    }

    public void setHoraires(String horaires) {
        this.horaires = horaires;
    }

    public Integer getDuree() {
        return duree;
    }

    public void setDuree(Integer duree) {
        this.duree = duree;
    }

    public String getUniteDuree() {
        return uniteDuree;
    }

    public void setUniteDuree(String uniteDuree) {
        this.uniteDuree = uniteDuree;
    }

    public Integer getSeancesSemaine() {
        return seancesSemaine;
    }

    public void setSeancesSemaine(Integer seancesSemaine) {
        this.seancesSemaine = seancesSemaine;
    }

    public Boolean getAccesCoach() {
        return accesCoach;
    }

    public void setAccesCoach(Boolean accesCoach) {
        this.accesCoach = accesCoach;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    /**
     * Retourne les activités sous forme de string séparée par des virgules
     */
    public String getActivitesAsString() {
        return String.join(",", activites);
    }

    /**
     * Définit les activités depuis une string séparée par des virgules
     */
    public void setActivitesFromString(String activitesString) {
        if (activitesString != null && !activitesString.isEmpty()) {
            this.activites = new ArrayList<>(List.of(activitesString.split(",")));
        }
    }

    @Override
    public String toString() {
        return nom + " - " + prix + " DH";
    }
}




