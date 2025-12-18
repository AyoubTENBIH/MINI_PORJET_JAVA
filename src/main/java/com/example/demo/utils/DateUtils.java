package com.example.demo.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utilitaires pour le parsing des dates depuis la base de données
 */
public class DateUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Parse une date depuis la base de données (peut être au format date seule ou datetime)
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Essayer d'abord le format date seule
            if (dateStr.length() == 10) {
                return LocalDate.parse(dateStr, DATE_FORMATTER);
            }
            // Sinon, essayer le format datetime et extraire seulement la date
            else if (dateStr.length() >= 10) {
                // Prendre seulement les 10 premiers caractères (la date)
                return LocalDate.parse(dateStr.substring(0, 10), DATE_FORMATTER);
            }
        } catch (DateTimeParseException e) {
            System.err.println("Erreur lors du parsing de la date: " + dateStr + " - " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Parse un datetime depuis la base de données
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Si le format contient un 'T', c'est un format ISO
            if (dateTimeStr.contains("T")) {
                return LocalDateTime.parse(dateTimeStr);
            }
            // Sinon, essayer le format SQLite standard
            else if (dateTimeStr.contains(" ")) {
                return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
            }
            // Sinon, c'est juste une date, ajouter minuit
            else {
                return LocalDate.parse(dateTimeStr, DATE_FORMATTER).atStartOfDay();
            }
        } catch (DateTimeParseException e) {
            System.err.println("Erreur lors du parsing du datetime: " + dateTimeStr + " - " + e.getMessage());
            // En cas d'erreur, essayer de parser comme date seule
            LocalDate date = parseDate(dateTimeStr);
            return date != null ? date.atStartOfDay() : null;
        }
    }
}




