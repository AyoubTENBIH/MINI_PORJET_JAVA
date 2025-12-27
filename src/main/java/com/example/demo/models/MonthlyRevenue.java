package com.example.demo.models;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) pour représenter les revenus mensuels.
 * 
 * <p>Utilisé pour transférer les données de revenus par mois
 * depuis la base de données vers les composants UI (charts, etc.)</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class MonthlyRevenue {
    private LocalDate mois;
    private Double montant;

    /**
     * Constructeur par défaut
     */
    public MonthlyRevenue() {
    }

    /**
     * Constructeur avec paramètres
     * 
     * @param mois Le mois concerné
     * @param montant Le montant des revenus pour ce mois
     */
    public MonthlyRevenue(LocalDate mois, Double montant) {
        this.mois = mois;
        this.montant = montant;
    }

    // Getters et Setters

    public LocalDate getMois() {
        return mois;
    }

    public void setMois(LocalDate mois) {
        this.mois = mois;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    /**
     * Retourne le nom du mois formaté (ex: "janv.", "févr.", etc.)
     * 
     * @return Nom du mois formaté
     */
    public String getMoisFormatted() {
        if (mois == null) {
            return "";
        }
        
        return switch (mois.getMonthValue()) {
            case 1 -> "janv.";
            case 2 -> "févr.";
            case 3 -> "mars";
            case 4 -> "avr.";
            case 5 -> "mai";
            case 6 -> "juin";
            case 7 -> "juil.";
            case 8 -> "août";
            case 9 -> "sept.";
            case 10 -> "oct.";
            case 11 -> "nov.";
            case 12 -> "déc.";
            default -> mois.getMonth().toString().substring(0, 3).toLowerCase() + ".";
        };
    }

    @Override
    public String toString() {
        return "MonthlyRevenue{" +
                "mois=" + mois +
                ", montant=" + montant +
                '}';
    }
}



