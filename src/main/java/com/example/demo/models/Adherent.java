package com.example.demo.models;

import java.time.LocalDate;

/**
 * Modèle représentant un adhérent/athlète de la salle de sport
 */
public class Adherent {
    private Integer id;
    private String cin;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String telephone;
    private String email;
    private String adresse;
    private String photo; // Chemin vers la photo
    private Double poids;
    private Double taille;
    private String objectifs;
    private String problemesSante;
    private Integer packId;
    private Pack pack;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean actif;
    private LocalDate dateInscription;

    public Adherent() {
        this.actif = true;
        this.dateInscription = LocalDate.now();
    }

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Double getPoids() {
        return poids;
    }

    public void setPoids(Double poids) {
        this.poids = poids;
    }

    public Double getTaille() {
        return taille;
    }

    public void setTaille(Double taille) {
        this.taille = taille;
    }

    public String getObjectifs() {
        return objectifs;
    }

    public void setObjectifs(String objectifs) {
        this.objectifs = objectifs;
    }

    public String getProblemesSante() {
        return problemesSante;
    }

    public void setProblemesSante(String problemesSante) {
        this.problemesSante = problemesSante;
    }

    public Integer getPackId() {
        return packId;
    }

    public void setPackId(Integer packId) {
        this.packId = packId;
    }

    public Pack getPack() {
        return pack;
    }

    public void setPack(Pack pack) {
        this.pack = pack;
        if (pack != null) {
            this.packId = pack.getId();
        }
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

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    /**
     * Calcule l'IMC (Indice de Masse Corporelle)
     */
    public Double getIMC() {
        if (poids != null && taille != null && taille > 0) {
            double tailleEnMetres = taille / 100.0;
            return poids / (tailleEnMetres * tailleEnMetres);
        }
        return null;
    }

    /**
     * Vérifie si l'abonnement est expiré
     */
    public Boolean isAbonnementExpire() {
        if (dateFin == null) {
            return true;
        }
        return LocalDate.now().isAfter(dateFin);
    }

    /**
     * Vérifie si l'abonnement expire bientôt (dans les 7 prochains jours)
     */
    public Boolean isAbonnementExpireBientot() {
        if (dateFin == null) {
            return false;
        }
        LocalDate maintenant = LocalDate.now();
        LocalDate dans7Jours = maintenant.plusDays(7);
        return !dateFin.isBefore(maintenant) && !dateFin.isAfter(dans7Jours);
    }

    @Override
    public String toString() {
        return getNomComplet() + " (" + cin + ")";
    }
}




