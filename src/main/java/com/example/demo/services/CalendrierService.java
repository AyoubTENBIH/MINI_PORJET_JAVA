package com.example.demo.services;

import com.example.demo.models.Adherent;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la logique métier du calendrier
 * Gère les calculs de dates, filtrage et comptage des expirations
 */
public class CalendrierService {
    
    /**
     * Compte les adhérents dont l'abonnement expire exactement à la date donnée
     */
    public long countExpiredOnDate(LocalDate date, List<Adherent> adherents) {
        if (adherents == null) {
            return 0;
        }
        
        return adherents.stream()
            .filter(a -> a.getDateFin() != null && a.getDateFin().equals(date))
            .count();
    }
    
    /**
     * Compte les adhérents dont l'abonnement expire bientôt (dans les 7 prochains jours)
     */
    public long countExpiringSoon(LocalDate date, List<Adherent> adherents) {
        if (adherents == null) {
            return 0;
        }
        
        return adherents.stream()
            .filter(a -> a.getDateFin() != null && 
                       a.getDateFin().isAfter(date) && 
                       a.getDateFin().isBefore(date.plusDays(8)) &&
                       !a.isAbonnementExpire())
            .count();
    }
    
    /**
     * Récupère tous les adhérents concernés par une date (expirés ou expirant bientôt)
     */
    public List<Adherent> getAdherentsForDate(LocalDate date, List<Adherent> adherents) {
        if (adherents == null) {
            return List.of();
        }
        
        return adherents.stream()
            .filter(a -> a.getDateFin() != null && 
                       (a.getDateFin().equals(date) || 
                        (a.getDateFin().isAfter(date) && a.getDateFin().isBefore(date.plusDays(8)))))
            .collect(Collectors.toList());
    }
    
    /**
     * Détermine le statut d'une date basé sur les adhérents
     * @return "expired", "expiring", ou null
     */
    public String getDateStatus(LocalDate date, List<Adherent> adherents) {
        long expired = countExpiredOnDate(date, adherents);
        if (expired > 0) {
            return "expired";
        }
        
        long expiringSoon = countExpiringSoon(date, adherents);
        if (expiringSoon > 0) {
            return "expiring";
        }
        
        return null;
    }
}




