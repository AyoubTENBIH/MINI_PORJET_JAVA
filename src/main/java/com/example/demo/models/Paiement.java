package com.example.demo.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modèle représentant un paiement/cotisation
 */
public class Paiement {
    public enum MethodePaiement {
        ESPECES("Espèces"),
        CARTE("Carte bancaire"),
        VIREMENT("Virement"),
        CHEQUE("Chèque");

        private final String libelle;

        MethodePaiement(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public enum StatutPaiement {
        VALIDE("Valide"),
        ANNULE("Annulé"),
        REMBOURSE("Remboursé");

        private final String libelle;

        StatutPaiement(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    private Integer id;
    private Integer adherentId;
    private Adherent adherent;
    private Integer packId;
    private Pack pack;
    private Double montant;
    private LocalDateTime datePaiement;
    private MethodePaiement methodePaiement;
    private StatutPaiement statut;
    private String reference;
    private LocalDate dateDebutAbonnement;
    private LocalDate dateFinAbonnement;
    private String notes;

    public Paiement() {
        this.statut = StatutPaiement.VALIDE;
        this.datePaiement = LocalDateTime.now();
    }

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAdherentId() {
        return adherentId;
    }

    public void setAdherentId(Integer adherentId) {
        this.adherentId = adherentId;
    }

    public Adherent getAdherent() {
        return adherent;
    }

    public void setAdherent(Adherent adherent) {
        this.adherent = adherent;
        if (adherent != null) {
            this.adherentId = adherent.getId();
        }
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

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public MethodePaiement getMethodePaiement() {
        return methodePaiement;
    }

    public void setMethodePaiement(MethodePaiement methodePaiement) {
        this.methodePaiement = methodePaiement;
    }

    public StatutPaiement getStatut() {
        return statut;
    }

    public void setStatut(StatutPaiement statut) {
        this.statut = statut;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDate getDateDebutAbonnement() {
        return dateDebutAbonnement;
    }

    public void setDateDebutAbonnement(LocalDate dateDebutAbonnement) {
        this.dateDebutAbonnement = dateDebutAbonnement;
    }

    public LocalDate getDateFinAbonnement() {
        return dateFinAbonnement;
    }

    public void setDateFinAbonnement(LocalDate dateFinAbonnement) {
        this.dateFinAbonnement = dateFinAbonnement;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return montant + " DH - " + (adherent != null ? adherent.getNomComplet() : "") + " - " + datePaiement;
    }
}




