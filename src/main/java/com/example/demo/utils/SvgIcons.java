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
    
    // Gym - Weight/Barbell icon (extrait depuis gym-svgrepo-com.svg)
    // Path original avec virgules remplacées par des espaces pour compatibilité SVGPath JavaFX
    public static final String GYM = 
        "M 22.942 6.837 L 20.76 4.654 L 21.707 3.707 A 1 1 0 1 0 20.293 2.293 L 19.346 3.24 L 17.163 1.058 A 3.7 3.7 0 0 0 12.058 1.058 A 3.609 3.609 0 0 0 12.058 6.163 L 14.24 8.346 L 8.346 14.24 L 6.163 12.058 A 3.7 3.7 0 0 0 1.058 12.058 A 3.609 3.609 0 0 0 1.058 17.163 L 3.24 19.346 L 2.293 20.293 A 1 1 0 1 0 3.707 21.707 L 4.654 20.76 L 6.837 22.942 A 3.609 3.609 0 0 0 11.942 22.942 A 3.608 3.608 0 0 0 11.942 17.837 L 9.76 15.655 L 15.655 9.76 L 17.837 11.942 A 3.609 3.609 0 0 0 22.942 11.942 A 3.608 3.608 0 0 0 22.942 6.837 Z " +
        "M 11 20.39 A 1.6 1.6 0 0 1 10.528 21.528 A 1.647 1.647 0 0 1 8.251 21.528 L 2.472 15.749 A 1.61 1.61 0 1 1 4.749 13.472 L 10.528 19.251 A 1.6 1.6 0 0 1 11 20.39 Z " +
        "M 21.528 10.528 A 1.647 1.647 0 0 1 19.251 10.528 L 13.472 4.749 A 1.61 1.61 0 1 1 15.749 2.472 L 21.528 8.251 A 1.609 1.609 0 0 1 21.528 10.528 Z";
    
    // Menu - Hamburger icon (3 lignes horizontales)
    public static final String MENU = 
        "M 3 12 L 21 12 M 3 6 L 21 6 M 3 18 L 21 18";
    
    // Star icon
    public static final String STAR = 
        "M 12 2 L 15.09 8.26 L 22 9.27 L 17 14.14 L 18.18 21.02 L 12 17.77 L 5.82 21.02 L 7 14.14 L 2 9.27 L 8.91 8.26 Z";
    
    // Moon - Dark mode icon
    public static final String MOON = 
        "M 21 12.79 A 9 9 0 1 1 11.21 3 A 7 7 0 0 0 21 12.79 Z";
    
    // Refresh - Reload icon
    public static final String REFRESH = 
        "M 1 4 L 1 10 L 7 10 M 23 20 L 23 14 L 17 14 M 20.49 9 A 9 9 0 0 0 5.64 5.64 L 1 10 M 23 14 L 18.64 18.36 A 9 9 0 0 1 3.51 15";
    
    // Bell - Notification icon
    public static final String BELL = 
        "M 18 8 A 6 6 0 0 0 6 8 C 6 15 3 17 3 17 H 21 S 18 15 18 8 M 13.73 21 A 2 2 0 0 1 10.27 21";
    
    // Globe - World/Language icon
    public static final String GLOBE = 
        "M 12 2 A 10 10 0 1 1 12 22 A 10 10 0 1 1 12 2 M 2 12 L 22 12 M 12 2 A 15.3 15.3 0 0 1 16 12 A 15.3 15.3 0 0 1 12 22 A 15.3 15.3 0 0 1 8 12 A 15.3 15.3 0 0 1 12 2";
    
    // Trending Up - Chart line going up (depuis trending-up.svg)
    public static final String TRENDING_UP = 
        "M 23 6 L 13.5 15.5 L 8.5 10.5 L 1 18 M 17 6 L 23 6 L 23 12";
    
    // Trending Down - Chart line going down (depuis trending-down.svg)
    public static final String TRENDING_DOWN = 
        "M 23 18 L 13.5 8.5 L 8.5 13.5 L 1 6 M 17 18 L 23 18 L 23 12";
    
    // Edit - Pencil icon (depuis edit.svg)
    // Path 1: M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7
    // Path 2: M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z
    public static final String EDIT = 
        "M 11 4 L 4 4 A 2 2 0 0 0 2 6 L 2 20 A 2 2 0 0 0 4 22 L 18 22 A 2 2 0 0 0 20 20 L 20 13 " +
        "M 18.5 2.5 A 2.121 2.121 0 0 1 21.5 5.5 L 12 15 L 8 16 L 9 12 L 18.5 2.5 Z";
    
    // Dollar Sign - Currency icon (depuis dollar-sign.svg)
    // Line: x1="12" y1="1" x2="12" y2="23"
    // Path: M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6
    public static final String DOLLAR_SIGN = 
        "M 12 1 L 12 23 M 17 5 L 9.5 5 A 3.5 3.5 0 0 0 9.5 12 L 14.5 12 A 3.5 3.5 0 0 1 14.5 19 L 6 19";
    
    // Alert Octagon - Warning icon (depuis alert-octagon.svg)
    public static final String ALERT_OCTAGON = 
        "M 7.86 2 L 16.14 2 L 22 7.86 L 22 16.14 L 16.14 22 L 7.86 22 L 2 16.14 L 2 7.86 Z " +
        "M 12 8 L 12 12 M 12 16 L 12.01 16";
    
    // Bar Chart 2 - Statistics icon (depuis bar-chart-2.svg)
    public static final String BAR_CHART_2 = 
        "M 18 20 L 18 10 M 12 20 L 12 4 M 6 20 L 6 14";
    
    private SvgIcons() {
        // Classe utilitaire - pas d'instanciation
    }
}

