package com.example.demo.utils;

/**
 * Classe utilitaire contenant les paths SVG des icônes.
 * Utilise SVGPath natif JavaFX pour un rendu 100% transparent sans halo.
 */
public class SvgIcons {
    
    // Dashboard - Layout avec 4 rectangles (converti en paths depuis les rects avec rx=1 pour les coins arrondis)
    // Format: M x y L x+width y L x+width y+height L x y+height Z (simplifié, les rx sont gérés par stroke-linecap)
    public static final String DASHBOARD = 
        "M 3 3 L 10 3 L 10 12 L 3 12 Z " +
        "M 14 3 L 21 3 L 21 8 L 14 8 Z " +
        "M 14 12 L 21 12 L 21 21 L 14 21 Z " +
        "M 3 16 L 10 16 L 10 21 L 3 21 Z";
    
    // Stats - Chart Bar (extrait directement des paths du SVG, en excluant le path invisible)
    public static final String STATS = 
        "M 3 13 L 3 19 L 7 19 L 7 13 Z " +
        "M 15 9 L 15 19 L 19 19 L 19 9 Z " +
        "M 9 5 L 9 19 L 13 19 L 13 5 Z " +
        "M 4 20 L 18 20";
    
    // Users (extrait directement des paths du SVG)
    public static final String USERS = 
        "M 9 7 A 4 4 0 1 0 17 7 A 4 4 0 1 0 9 7 Z " +
        "M 3 21 L 3 19 A 4 4 0 0 1 7 15 L 11 15 A 4 4 0 0 1 15 19 L 15 21 Z " +
        "M 16 3.13 A 4 4 0 0 1 16 10.88 Z " +
        "M 21 21 L 21 19 A 4 4 0 0 0 18 15.15 Z";
    
    // Pack - Package (extrait directement des paths du SVG)
    public static final String PACK = 
        "M 12 3 L 20 7.5 L 20 16.5 L 12 21 L 4 16.5 L 4 7.5 Z " +
        "M 12 12 L 20 7.5 " +
        "M 12 12 L 12 21 " +
        "M 12 12 L 4 7.5 " +
        "M 16 5.25 L 8 9.75";
    
    // Payment - Cash Move Back (extrait directement des paths du SVG)
    public static final String PAYMENT = 
        "M 7 15 L 4 15 A 1 1 0 0 1 3 14 L 3 6 A 1 1 0 0 1 4 5 L 16 5 A 1 1 0 0 1 17 6 L 17 9 " +
        "M 12 19 L 8 19 A 1 1 0 0 1 7 18 L 7 10 A 1 1 0 0 1 8 9 L 20 9 A 1 1 0 0 1 21 10 L 21 12.5 " +
        "M 15.914 13.417 A 2 2 0 1 0 13.467 15.928 " +
        "M 16 19 L 22 19 " +
        "M 19 16 L 16 19 L 19 22";
    
    // Calendar (extrait directement des paths du SVG)
    public static final String CALENDAR = 
        "M 12.5 21 L 6 21 A 2 2 0 0 1 4 19 L 4 7 A 2 2 0 0 1 6 5 L 18 5 A 2 2 0 0 1 20 7 L 20 12 " +
        "M 19 16 L 19 22 " +
        "M 22 19 L 19 22 L 19 16 " +
        "M 16 3 L 16 7 " +
        "M 8 3 L 8 7 " +
        "M 4 11 L 20 11";
    
    // Settings - Gear (extrait directement des paths du SVG)
    public static final String SETTINGS = 
        "M 10.325 4.317 A 1.724 1.724 0 0 0 12.875 5.383 A 1.543 1.543 0 0 0 15.298 3.443 A 1.724 1.724 0 0 0 16.363 5.993 A 1.756 1.756 0 0 0 20.079 5.993 A 1.724 1.724 0 0 0 21.145 7.563 A 1.543 1.543 0 0 0 18.775 9.933 A 1.724 1.724 0 0 0 20.841 12.505 A 1.756 1.756 0 0 0 20.841 16.221 A 1.724 1.724 0 0 0 19.271 17.287 A 1.543 1.543 0 0 0 16.901 14.917 A 1.724 1.724 0 0 0 14.351 16.983 A 1.756 1.756 0 0 0 14.351 20.699 A 1.724 1.724 0 0 0 12.781 21.765 A 1.543 1.543 0 0 0 10.411 19.395 A 1.724 1.724 0 0 0 7.861 20.461 A 1.756 1.756 0 0 0 4.145 20.461 A 1.724 1.724 0 0 0 3.079 18.891 A 1.543 1.543 0 0 0 5.449 16.521 A 1.724 1.724 0 0 0 3.383 13.949 A 1.756 1.756 0 0 0 3.383 10.233 A 1.724 1.724 0 0 0 4.953 9.167 A 1.543 1.543 0 0 0 7.323 11.537 A 1.724 1.724 0 0 0 9.873 10.471 A 1.756 1.756 0 0 0 9.873 6.755 A 1.724 1.724 0 0 0 11.443 5.689 A 1.543 1.543 0 0 0 13.813 8.059 A 1.724 1.724 0 0 0 16.363 7.993 A 1.756 1.756 0 0 0 16.363 4.277 Z " +
        "M 9 12 A 3 3 0 1 0 15 12 A 3 3 0 1 0 9 12 Z";
    
    // Help - Help Circle (extrait directement des paths du SVG)
    public static final String HELP = 
        "M 3 12 A 9 9 0 1 0 21 12 A 9 9 0 1 0 3 12 Z " +
        "M 12 16 L 12 16.01 " +
        "M 12 13 A 2 2 0 0 0 12.914 9.218 A 1.98 1.98 0 0 0 10.5 9.701";
    
    // Search (extrait directement des paths du SVG)
    public static final String SEARCH = 
        "M 10 10 A 7 7 0 1 0 17 10 A 7 7 0 1 0 10 10 Z " +
        "M 21 21 L 15 15";
    
    // Chevron Down - Caret Down (extrait directement des paths du SVG)
    public static final String CHEVRON_DOWN = 
        "M 6 10 L 12 16 L 18 10 L 6 10 Z";
    
    private SvgIcons() {
        // Classe utilitaire - pas d'instanciation
    }
}

