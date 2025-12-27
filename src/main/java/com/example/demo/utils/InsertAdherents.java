package com.example.demo.utils;

import java.util.logging.Logger;

/**
 * Classe utilitaire pour insérer rapidement des adhérents pour les statistiques
 * 
 * Usage:
 * - Appeler InsertAdherents.insert50() pour insérer 50 adhérents
 * - Appeler InsertAdherents.insert100() pour insérer 100 adhérents
 * - Appeler InsertAdherents.insertCustom(nombre) pour un nombre personnalisé
 */
public class InsertAdherents {
    private static final Logger logger = Logger.getLogger(InsertAdherents.class.getName());
    
    /**
     * Insère 50 adhérents automatiquement (complète jusqu'à 50 si nécessaire)
     */
    public static void insert50() {
        try {
            logger.info("=== Début de l'insertion automatique ===");
            System.out.println("=== Début de l'insertion automatique ===");
            System.err.println("=== Début de l'insertion automatique ===");
            
            com.example.demo.dao.AdherentDAO adherentDAO = new com.example.demo.dao.AdherentDAO();
            int nombreExistant = adherentDAO.findAll().size();
            
            logger.info("Nombre d'adhérents existants: " + nombreExistant);
            System.out.println("Nombre d'adhérents existants: " + nombreExistant);
            System.err.println("Nombre d'adhérents existants: " + nombreExistant);
            
            int nombreSouhaite = 50;
            int nombreAInserer = Math.max(0, nombreSouhaite - nombreExistant);
            
            if (nombreAInserer > 0) {
                String msg = "=== Insertion de " + nombreAInserer + " nouveaux adhérents (pour atteindre " + nombreSouhaite + " au total) ===";
                logger.info(msg);
                System.out.println(msg);
                System.err.println(msg);
                
                TestDataGenerator.generateTestData(nombreAInserer, true);
                
                logger.info("=== Insertion automatique terminée ===");
                System.out.println("=== Insertion automatique terminée ===");
                System.err.println("=== Insertion automatique terminée ===");
            } else {
                String msg = "=== " + nombreExistant + " adhérents déjà présents (objectif de " + nombreSouhaite + " atteint). Insertion automatique ignorée. ===";
                logger.info(msg);
                System.out.println(msg);
                System.err.println(msg);
            }
        } catch (Exception e) {
            String errorMsg = "ERREUR lors de l'insertion automatique: " + e.getMessage();
            logger.severe(errorMsg);
            System.err.println(errorMsg);
            e.printStackTrace();
        }
    }
    
    /**
     * Insère 100 adhérents automatiquement
     */
    public static void insert100() {
        System.out.println("=== Insertion de 100 adhérents ===");
        TestDataGenerator.generateTestData(100, true);
    }
    
    /**
     * Insère un nombre personnalisé d'adhérents
     * @param nombre Nombre d'adhérents à insérer
     */
    public static void insertCustom(int nombre) {
        System.out.println("=== Insertion de " + nombre + " adhérents ===");
        TestDataGenerator.generateTestData(nombre, true);
    }
    
    /**
     * Point d'entrée pour tester depuis la ligne de commande
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                int nombre = Integer.parseInt(args[0]);
                insertCustom(nombre);
            } catch (NumberFormatException e) {
                System.err.println("Erreur: Le nombre doit être un entier.");
                System.out.println("Usage: java InsertAdherents [nombre]");
                System.out.println("Exemples:");
                System.out.println("  java InsertAdherents 50   -> Insère 50 adhérents");
                System.out.println("  java InsertAdherents 100  -> Insère 100 adhérents");
            }
        } else {
            // Par défaut, insérer 50 adhérents
            insert50();
        }
    }
}
