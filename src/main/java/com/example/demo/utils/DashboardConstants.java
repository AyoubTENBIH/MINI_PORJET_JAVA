package com.example.demo.utils;

/**
 * Constantes centralisées pour le dashboard.
 * 
 * <p>Cette classe regroupe toutes les valeurs magiques utilisées
 * dans le dashboard pour éviter la duplication et faciliter la maintenance.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class DashboardConstants {
    
    // ============================================
    // OBJECTIFS PAR DÉFAUT
    // ============================================
    
    /**
     * Objectif par défaut pour le nombre d'adhérents (taux d'occupation)
     */
    public static final int OBJECTIF_ADHERENTS_DEFAULT = 80;
    
    /**
     * Objectif par défaut pour les revenus mensuels (en DH)
     */
    public static final double OBJECTIF_REVENUS_DEFAULT = 100000.0;
    
    // ============================================
    // PÉRIODES DE FILTRAGE
    // ============================================
    
    /**
     * Nombre de jours pour considérer qu'un abonnement expire bientôt
     */
    public static final int DAYS_EXPIRING_SOON = 7;
    
    /**
     * Nombre de mois à afficher dans le graphique d'évolution des revenus
     */
    public static final int MONTHS_REVENUE_CHART = 6;
    
    /**
     * Nombre de jours pour considérer un adhérent comme "nouveau" (cette semaine)
     */
    public static final int DAYS_NEW_ADHERENT_WEEK = 7;
    
    /**
     * Nombre de jours pour considérer un adhérent comme "nouveau" (ce mois)
     */
    public static final int DAYS_NEW_ADHERENT_MONTH = 30;
    
    // ============================================
    // TYPES DE NOTIFICATIONS
    // ============================================
    
    /**
     * Type de notification : Nouvel adhérent inscrit
     */
    public static final String NOTIF_TYPE_NEW_USER = "NEW_USER";
    
    /**
     * Type de notification : Nouveau paiement reçu
     */
    public static final String NOTIF_TYPE_NEW_PAYMENT = "NEW_PAYMENT";
    
    /**
     * Type de notification : Retrait de fonds
     */
    public static final String NOTIF_TYPE_WITHDRAWAL = "WITHDRAWAL";
    
    /**
     * Type de notification : Message non lu
     */
    public static final String NOTIF_TYPE_MESSAGE = "MESSAGE";
    
    /**
     * Type de notification : Abonnement expirant bientôt
     */
    public static final String NOTIF_TYPE_EXPIRING_SOON = "EXPIRING_SOON";
    
    /**
     * Type de notification : Abonnement expiré
     */
    public static final String NOTIF_TYPE_EXPIRED = "EXPIRED";
    
    /**
     * Type de notification : Adhérent modifié
     */
    public static final String NOTIF_TYPE_ADHERENT_UPDATED = "ADHERENT_UPDATED";
    
    /**
     * Type de notification : Adhérent supprimé
     */
    public static final String NOTIF_TYPE_ADHERENT_DELETED = "ADHERENT_DELETED";
    
    /**
     * Type de notification : Pack créé
     */
    public static final String NOTIF_TYPE_PACK_CREATED = "PACK_CREATED";
    
    /**
     * Type de notification : Pack modifié
     */
    public static final String NOTIF_TYPE_PACK_UPDATED = "PACK_UPDATED";
    
    /**
     * Type de notification : Pack supprimé
     */
    public static final String NOTIF_TYPE_PACK_DELETED = "PACK_DELETED";
    
    /**
     * Type de notification : Objectif atteint
     */
    public static final String NOTIF_TYPE_OBJECTIVE_REACHED = "OBJECTIVE_REACHED";
    
    /**
     * Type de notification : Paiement important (montant élevé)
     */
    public static final String NOTIF_TYPE_HIGH_PAYMENT = "HIGH_PAYMENT";
    
    // ============================================
    // SEUILS ET CONFIGURATIONS
    // ============================================
    
    /**
     * Seuil pour considérer un paiement comme important (en DH)
     */
    public static final double NOTIFICATION_HIGH_PAYMENT_THRESHOLD = 1000.0;
    
    // ============================================
    // TYPES D'ACTIVITÉS
    // ============================================
    
    /**
     * Type d'activité : Changement de style
     */
    public static final String ACTIVITY_TYPE_STYLE_CHANGED = "STYLE_CHANGED";
    
    /**
     * Type d'activité : Nouveau produit/pack ajouté
     */
    public static final String ACTIVITY_TYPE_PRODUCT_ADDED = "PRODUCT_ADDED";
    
    /**
     * Type d'activité : Produit archivé
     */
    public static final String ACTIVITY_TYPE_PRODUCT_ARCHIVED = "PRODUCT_ARCHIVED";
    
    /**
     * Type d'activité : Page supprimée
     */
    public static final String ACTIVITY_TYPE_PAGE_REMOVED = "PAGE_REMOVED";
    
    /**
     * Type d'activité : Adhérent créé
     */
    public static final String ACTIVITY_TYPE_ADHERENT_CREATED = "ADHERENT_CREATED";
    
    /**
     * Type d'activité : Adhérent modifié
     */
    public static final String ACTIVITY_TYPE_ADHERENT_UPDATED = "ADHERENT_UPDATED";
    
    /**
     * Type d'activité : Paiement enregistré
     */
    public static final String ACTIVITY_TYPE_PAYMENT_RECORDED = "PAYMENT_RECORDED";
    
    /**
     * Type d'activité : Pack créé
     */
    public static final String ACTIVITY_TYPE_PACK_CREATED = "PACK_CREATED";
    
    /**
     * Type d'activité : Pack modifié
     */
    public static final String ACTIVITY_TYPE_PACK_UPDATED = "PACK_UPDATED";
    
    // ============================================
    // TYPES D'OBJECTIFS
    // ============================================
    
    /**
     * Type d'objectif : Taux d'occupation
     */
    public static final String OBJECTIF_TYPE_TAUX_OCCUPATION = "taux_occupation";
    
    /**
     * Type d'objectif : Revenus mensuels
     */
    public static final String OBJECTIF_TYPE_REVENUS = "revenus";
    
    /**
     * Type d'objectif : Nombre d'adhérents
     */
    public static final String OBJECTIF_TYPE_ADHERENTS = "adherents";
    
    // ============================================
    // TYPES D'ENTITÉS (pour les activités)
    // ============================================
    
    /**
     * Type d'entité : Pack
     */
    public static final String ENTITY_TYPE_PACK = "pack";
    
    /**
     * Type d'entité : Adhérent
     */
    public static final String ENTITY_TYPE_ADHERENT = "adherent";
    
    /**
     * Type d'entité : Page
     */
    public static final String ENTITY_TYPE_PAGE = "page";
    
    /**
     * Type d'entité : Paiement
     */
    public static final String ENTITY_TYPE_PAYMENT = "payment";
    
    // ============================================
    // LIMITES ET PARAMÈTRES D'AFFICHAGE
    // ============================================
    
    /**
     * Nombre maximum de notifications à afficher dans le panel
     */
    public static final int MAX_NOTIFICATIONS_DISPLAY = 4;
    
    /**
     * Nombre maximum d'activités à afficher dans le panel
     */
    public static final int MAX_ACTIVITIES_DISPLAY = 4;
    
    /**
     * Nombre d'éléments par page pour la pagination
     */
    public static final int DEFAULT_PAGE_SIZE = 10;
    
    // ============================================
    // NOMS DE PAGES (pour les favoris)
    // ============================================
    
    /**
     * Nom de la page Dashboard
     */
    public static final String PAGE_DASHBOARD = "Dashboard";
    
    /**
     * Nom de la page Adhérents
     */
    public static final String PAGE_ADHERENTS = "Adhérents";
    
    /**
     * Nom de la page Packs
     */
    public static final String PAGE_PACKS = "Packs";
    
    /**
     * Nom de la page Paiements
     */
    public static final String PAGE_PAIEMENTS = "Paiements";
    
    /**
     * Nom de la page Statistiques
     */
    public static final String PAGE_STATISTIQUES = "Statistiques";
    
    /**
     * Nom de la page Calendrier
     */
    public static final String PAGE_CALENDRIER = "Calendrier";
    
    // ============================================
    // COULEURS ET STYLES (si nécessaire)
    // ============================================
    
    /**
     * Couleur verte pour les indicateurs positifs
     */
    public static final String COLOR_POSITIVE = "#10b981";
    
    /**
     * Couleur rouge pour les indicateurs négatifs
     */
    public static final String COLOR_NEGATIVE = "#ef4444";
    
    /**
     * Couleur orange pour les alertes
     */
    public static final String COLOR_WARNING = "#f59e0b";
    
    /**
     * Constructeur privé pour empêcher l'instanciation
     */
    private DashboardConstants() {
        throw new UnsupportedOperationException("Cette classe ne peut pas être instanciée");
    }
}


