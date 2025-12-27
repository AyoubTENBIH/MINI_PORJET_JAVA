package com.example.demo.services;

import com.example.demo.dao.ActivityDAO;
import com.example.demo.models.Activity;
import com.example.demo.utils.DashboardConstants;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service pour la gestion des activités automatiques.
 * 
 * <p>Ce service génère automatiquement des activités lors d'événements
 * importants (création de packs, archivage, modifications, etc.) et fournit
 * des méthodes pour récupérer et gérer les activités.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class ActivityService {
    private static final Logger logger = Logger.getLogger(ActivityService.class.getName());
    private static ActivityService instance;
    private ActivityDAO activityDAO;
    
    // ID de l'utilisateur actuel (à récupérer depuis le système d'authentification)
    private Integer currentUserId = 1; // Par défaut, à adapter selon votre système d'auth

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private ActivityService() {
        this.activityDAO = new ActivityDAO();
    }

    /**
     * Retourne l'instance unique du ActivityService (Singleton)
     * 
     * @return L'instance unique du ActivityService
     */
    public static synchronized ActivityService getInstance() {
        if (instance == null) {
            instance = new ActivityService();
        }
        return instance;
    }

    /**
     * Enregistre une activité de changement de style.
     * 
     * @param description Description de l'activité
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void logStyleChanged(String description) throws SQLException {
        Activity activity = new Activity(
            currentUserId,
            DashboardConstants.ACTIVITY_TYPE_STYLE_CHANGED,
            description
        );
        activity.setCreatedAt(LocalDateTime.now());
        activityDAO.create(activity);
        logger.info("Activité enregistrée: Style changé - " + description);
    }

    /**
     * Enregistre une activité d'ajout de produit/pack.
     * 
     * @param packId ID du pack ajouté
     * @param packName Nom du pack
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void logProductAdded(Integer packId, String packName) throws SQLException {
        Activity activity = new Activity(
            currentUserId,
            DashboardConstants.ACTIVITY_TYPE_PRODUCT_ADDED,
            "Nouveau pack ajouté: " + packName,
            DashboardConstants.ENTITY_TYPE_PACK,
            packId
        );
        activity.setCreatedAt(LocalDateTime.now());
        activityDAO.create(activity);
        logger.info("Activité enregistrée: Produit ajouté - " + packName);
    }

    /**
     * Enregistre une activité d'archivage de produit.
     * 
     * @param packId ID du pack archivé
     * @param packName Nom du pack
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void logProductArchived(Integer packId, String packName) throws SQLException {
        Activity activity = new Activity(
            currentUserId,
            DashboardConstants.ACTIVITY_TYPE_PRODUCT_ARCHIVED,
            "Pack archivé: " + packName,
            DashboardConstants.ENTITY_TYPE_PACK,
            packId
        );
        activity.setCreatedAt(LocalDateTime.now());
        activityDAO.create(activity);
        logger.info("Activité enregistrée: Produit archivé - " + packName);
    }

    /**
     * Enregistre une activité de suppression de page.
     * 
     * @param pageName Nom de la page supprimée
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void logPageRemoved(String pageName) throws SQLException {
        Activity activity = new Activity(
            currentUserId,
            DashboardConstants.ACTIVITY_TYPE_PAGE_REMOVED,
            "Page supprimée: " + pageName,
            DashboardConstants.ENTITY_TYPE_PAGE,
            null
        );
        activity.setCreatedAt(LocalDateTime.now());
        activityDAO.create(activity);
        logger.info("Activité enregistrée: Page supprimée - " + pageName);
    }

    /**
     * Enregistre une activité de création d'adhérent.
     * 
     * @param adherentId ID de l'adhérent créé
     * @param adherentName Nom de l'adhérent
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void logAdherentCreated(Integer adherentId, String adherentName) throws SQLException {
        Activity activity = new Activity(
            currentUserId,
            DashboardConstants.ACTIVITY_TYPE_ADHERENT_CREATED,
            "Nouvel adhérent créé: " + adherentName,
            DashboardConstants.ENTITY_TYPE_ADHERENT,
            adherentId
        );
        activity.setCreatedAt(LocalDateTime.now());
        activityDAO.create(activity);
        logger.info("Activité enregistrée: Adhérent créé - " + adherentName);
    }

    /**
     * Enregistre une activité de modification d'adhérent.
     * 
     * @param adherentId ID de l'adhérent modifié
     * @param adherentName Nom de l'adhérent
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void logAdherentUpdated(Integer adherentId, String adherentName) throws SQLException {
        Activity activity = new Activity(
            currentUserId,
            DashboardConstants.ACTIVITY_TYPE_ADHERENT_UPDATED,
            "Adhérent modifié: " + adherentName,
            DashboardConstants.ENTITY_TYPE_ADHERENT,
            adherentId
        );
        activity.setCreatedAt(LocalDateTime.now());
        activityDAO.create(activity);
        logger.info("Activité enregistrée: Adhérent modifié - " + adherentName);
    }

    /**
     * Enregistre une activité d'enregistrement de paiement.
     * 
     * @param paymentId ID du paiement
     * @param montant Montant du paiement
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void logPaymentRecorded(Integer paymentId, Double montant) throws SQLException {
        Activity activity = new Activity(
            currentUserId,
            DashboardConstants.ACTIVITY_TYPE_PAYMENT_RECORDED,
            String.format("Paiement enregistré: %.2f DH", montant),
            DashboardConstants.ENTITY_TYPE_PAYMENT,
            paymentId
        );
        activity.setCreatedAt(LocalDateTime.now());
        activityDAO.create(activity);
        logger.info("Activité enregistrée: Paiement enregistré - " + montant + " DH");
    }

    /**
     * Enregistre une activité de création de pack.
     * 
     * @param packId ID du pack créé
     * @param packName Nom du pack
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void logPackCreated(Integer packId, String packName) throws SQLException {
        Activity activity = new Activity(
            currentUserId,
            DashboardConstants.ACTIVITY_TYPE_PACK_CREATED,
            "Pack créé: " + packName,
            DashboardConstants.ENTITY_TYPE_PACK,
            packId
        );
        activity.setCreatedAt(LocalDateTime.now());
        activityDAO.create(activity);
        logger.info("Activité enregistrée: Pack créé - " + packName);
    }

    /**
     * Enregistre une activité de modification de pack.
     * 
     * @param packId ID du pack modifié
     * @param packName Nom du pack
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void logPackUpdated(Integer packId, String packName) throws SQLException {
        Activity activity = new Activity(
            currentUserId,
            DashboardConstants.ACTIVITY_TYPE_PACK_UPDATED,
            "Pack modifié: " + packName,
            DashboardConstants.ENTITY_TYPE_PACK,
            packId
        );
        activity.setCreatedAt(LocalDateTime.now());
        activityDAO.create(activity);
        logger.info("Activité enregistrée: Pack modifié - " + packName);
    }

    /**
     * Récupère les activités récentes pour l'utilisateur actuel.
     * 
     * @param limit Nombre maximum d'activités à récupérer
     * @return Liste des activités récentes
     * @throws SQLException Si une erreur survient
     */
    public List<Activity> getRecentActivities(int limit) throws SQLException {
        return activityDAO.findRecent(limit);
    }

    /**
     * Récupère toutes les activités d'un utilisateur.
     * 
     * @return Liste des activités de l'utilisateur
     * @throws SQLException Si une erreur survient
     */
    public List<Activity> getUserActivities() throws SQLException {
        return activityDAO.findByUserId(currentUserId);
    }

    /**
     * Définit l'ID de l'utilisateur actuel.
     * 
     * @param userId ID de l'utilisateur
     */
    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
    }
}



