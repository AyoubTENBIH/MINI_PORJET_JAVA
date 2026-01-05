package com.example.demo.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Utilitaire pour gérer les filtres de plage de dates.
 * 
 * <p>Fournit des méthodes statiques pour obtenir des plages de dates
 * courantes (Today, This Week, This Month, etc.) utilisées dans le dashboard.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class DateRangeFilter {
    
    /**
     * Enumération des types de filtres temporels disponibles
     */
    public enum FilterType {
        TODAY,
        THIS_WEEK,
        THIS_MONTH,
        LAST_MONTH,
        THIS_YEAR,
        LAST_YEAR,
        CUSTOM
    }
    
    /**
     * Classe interne pour représenter une plage de dates
     */
    public static class DateRange {
        private LocalDate startDate;
        private LocalDate endDate;
        
        public DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public LocalDate getStartDate() {
            return startDate;
        }
        
        public LocalDate getEndDate() {
            return endDate;
        }
        
        @Override
        public String toString() {
            return "DateRange{" +
                    "startDate=" + startDate +
                    ", endDate=" + endDate +
                    '}';
        }
    }
    
    /**
     * Retourne la plage de dates pour "Today".
     * 
     * @return DateRange avec startDate et endDate égales à aujourd'hui
     */
    public static DateRange getToday() {
        LocalDate today = LocalDate.now();
        return new DateRange(today, today);
    }
    
    /**
     * Retourne la plage de dates pour "This Week".
     * 
     * @return DateRange du début de la semaine (lundi) à aujourd'hui
     */
    public static DateRange getThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return new DateRange(startOfWeek, today);
    }
    
    /**
     * Retourne la plage de dates pour "This Month".
     * 
     * @return DateRange du début du mois à aujourd'hui
     */
    public static DateRange getThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        return new DateRange(startOfMonth, today);
    }
    
    /**
     * Retourne la plage de dates pour "Last Month".
     * 
     * @return DateRange du mois précédent (du 1er au dernier jour)
     */
    public static DateRange getLastMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = today.withDayOfMonth(1).minusDays(1);
        return new DateRange(firstDayOfLastMonth, lastDayOfLastMonth);
    }
    
    /**
     * Retourne la plage de dates pour "This Year".
     * 
     * @return DateRange du début de l'année à aujourd'hui
     */
    public static DateRange getThisYear() {
        LocalDate today = LocalDate.now();
        LocalDate startOfYear = today.withDayOfYear(1);
        return new DateRange(startOfYear, today);
    }
    
    /**
     * Retourne la plage de dates pour "Last Year".
     * 
     * @return DateRange de l'année précédente (du 1er janvier au 31 décembre)
     */
    public static DateRange getLastYear() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfLastYear = LocalDate.of(today.getYear() - 1, 1, 1);
        LocalDate lastDayOfLastYear = LocalDate.of(today.getYear() - 1, 12, 31);
        return new DateRange(firstDayOfLastYear, lastDayOfLastYear);
    }
    
    /**
     * Retourne la plage de dates pour les N derniers mois.
     * 
     * @param numberOfMonths Nombre de mois à remonter
     * @return DateRange du début du mois N mois avant à aujourd'hui
     */
    public static DateRange getLastNMonths(int numberOfMonths) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(numberOfMonths).withDayOfMonth(1);
        return new DateRange(startDate, today);
    }
    
    /**
     * Retourne la plage de dates pour les N derniers jours.
     * 
     * @param numberOfDays Nombre de jours à remonter
     * @return DateRange de N jours avant à aujourd'hui
     */
    public static DateRange getLastNDays(int numberOfDays) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(numberOfDays);
        return new DateRange(startDate, today);
    }
    
    /**
     * Retourne une plage de dates personnalisée.
     * 
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return DateRange avec les dates spécifiées
     */
    public static DateRange getCustomRange(LocalDate startDate, LocalDate endDate) {
        return new DateRange(startDate, endDate);
    }
    
    /**
     * Retourne la plage de dates selon le type de filtre.
     * 
     * @param filterType Type de filtre
     * @return DateRange correspondant au type de filtre
     */
    public static DateRange getDateRange(FilterType filterType) {
        return switch (filterType) {
            case TODAY -> getToday();
            case THIS_WEEK -> getThisWeek();
            case THIS_MONTH -> getThisMonth();
            case LAST_MONTH -> getLastMonth();
            case THIS_YEAR -> getThisYear();
            case LAST_YEAR -> getLastYear();
            case CUSTOM -> null; // Nécessite des dates personnalisées
        };
    }
    
    /**
     * Retourne le libellé affiché pour un type de filtre.
     * 
     * @param filterType Type de filtre
     * @return Libellé en français
     */
    public static String getLabel(FilterType filterType) {
        return switch (filterType) {
            case TODAY -> "Aujourd'hui";
            case THIS_WEEK -> "Cette semaine";
            case THIS_MONTH -> "Ce mois";
            case LAST_MONTH -> "Mois dernier";
            case THIS_YEAR -> "Cette année";
            case LAST_YEAR -> "Année dernière";
            case CUSTOM -> "Personnalisé";
        };
    }
    
    /**
     * Retourne le libellé court pour un type de filtre.
     * 
     * @param filterType Type de filtre
     * @return Libellé court en français
     */
    public static String getShortLabel(FilterType filterType) {
        return switch (filterType) {
            case TODAY -> "Aujourd'hui";
            case THIS_WEEK -> "Semaine";
            case THIS_MONTH -> "Mois";
            case LAST_MONTH -> "Mois dernier";
            case THIS_YEAR -> "Année";
            case LAST_YEAR -> "Année dernière";
            case CUSTOM -> "Personnalisé";
        };
    }
    
    /**
     * Retourne le libellé en anglais pour un type de filtre.
     * 
     * @param filterType Type de filtre
     * @return Libellé en anglais
     */
    public static String getEnglishLabel(FilterType filterType) {
        return switch (filterType) {
            case TODAY -> "Today";
            case THIS_WEEK -> "This Week";
            case THIS_MONTH -> "This Month";
            case LAST_MONTH -> "Last Month";
            case THIS_YEAR -> "This Year";
            case LAST_YEAR -> "Last Year";
            case CUSTOM -> "Custom";
        };
    }
}






