package com.example.demo.services;

import com.example.demo.dao.NotificationDAO;
import com.example.demo.models.Notification;
import com.example.demo.models.Adherent;
import com.example.demo.models.Paiement;
import com.example.demo.models.Pack;
import com.example.demo.utils.DashboardConstants;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service pour la gestion des notifications automatiques.
 * 
 * <p>Ce service génère automatiquement des notifications lors d'événements
 * importants (nouveaux adhérents, paiements, etc.) et fournit des méthodes
 * pour récupérer et gérer les notifications.</p>
 * 
 * @author Dashboard Team
 * @version 1.0
 */
public class NotificationService {
    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());
    private static NotificationService instance;
    private NotificationDAO notificationDAO;
    
    // ID de l'utilisateur actuel (à récupérer depuis le système d'authentification)
    private Integer currentUserId = 1; // Par défaut, à adapter selon votre système d'auth

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * Retourne l'instance unique du NotificationService (Singleton)
     * 
     * @return L'instance unique du NotificationService
     */
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * Génère automatiquement une notification lors de l'inscription d'un nouvel adhérent.
     * 
     * @param adherent Le nouvel adhérent inscrit
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyNewAdherent(Adherent adherent) throws SQLException {
        String title = "Nouvel adhérent inscrit";
        String message = adherent.getNomComplet() + " s'est inscrit";
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_NEW_USER,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Nouvel adhérent - " + adherent.getNomComplet());
    }

    /**
     * Génère automatiquement une notification lors d'un nouveau paiement.
     * 
     * @param paiement Le nouveau paiement reçu
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyNewPayment(Paiement paiement) throws SQLException {
        String adherentName = paiement.getAdherent() != null 
            ? paiement.getAdherent().getNomComplet() 
            : "Adhérent #" + paiement.getAdherentId();
        String packName = paiement.getPack() != null 
            ? paiement.getPack().getNom() 
            : "Pack #" + paiement.getPackId();
        
        String title = "Nouveau paiement reçu";
        String message = String.format("Paiement de %.2f DH de %s pour le pack %s", 
            paiement.getMontant(), adherentName, packName);
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_NEW_PAYMENT,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Nouveau paiement - " + paiement.getMontant() + " DH");
        
        // Si le paiement est important, créer une notification supplémentaire
        if (paiement.getMontant() != null && paiement.getMontant() >= DashboardConstants.NOTIFICATION_HIGH_PAYMENT_THRESHOLD) {
            notifyHighPayment(paiement);
        }
    }

    /**
     * Génère automatiquement une notification pour un retrait de fonds.
     * 
     * @param montant Le montant retiré
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyWithdrawal(double montant) throws SQLException {
        String title = "Retrait de fonds";
        String message = String.format("Retrait de %.2f DH effectué", montant);
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_WITHDRAWAL,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Retrait de fonds - " + montant + " DH");
    }

    /**
     * Génère automatiquement une notification pour des messages non lus.
     * 
     * @param nombreMessages Nombre de messages non lus
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyUnreadMessages(int nombreMessages) throws SQLException {
        String title = "Messages non lus";
        String message = nombreMessages + " message(s) non lu(s)";
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_MESSAGE,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Messages non lus - " + nombreMessages);
    }

    /**
     * Génère automatiquement une notification pour des abonnements expirant bientôt.
     * 
     * @param nombreExpirant Nombre d'abonnements expirant bientôt
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyExpiringSoon(int nombreExpirant) throws SQLException {
        if (nombreExpirant <= 0) {
            return; // Pas de notification si aucun abonnement n'expire
        }
        
        String title = "Abonnements expirant bientôt";
        String message = nombreExpirant + " abonnement(s) expire(nt) dans les " 
            + DashboardConstants.DAYS_EXPIRING_SOON + " prochains jours. Action requise !";
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_EXPIRING_SOON,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Abonnements expirant bientôt - " + nombreExpirant);
    }
    
    /**
     * Génère automatiquement une notification pour des abonnements expirés.
     * 
     * @param nombreExpires Nombre d'abonnements expirés
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyExpired(int nombreExpires) throws SQLException {
        if (nombreExpires <= 0) {
            return; // Pas de notification si aucun abonnement n'est expiré
        }
        
        String title = "Abonnements expirés";
        String message = nombreExpires + " abonnement(s) expiré(s). Renouvellement urgent requis !";
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_EXPIRED,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Abonnements expirés - " + nombreExpires);
    }
    
    /**
     * Génère automatiquement une notification lors de la modification d'un adhérent.
     * 
     * @param adherent L'adhérent modifié
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyAdherentUpdated(Adherent adherent) throws SQLException {
        String title = "Adhérent modifié";
        String message = "Les informations de " + adherent.getNomComplet() + " ont été mises à jour";
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_ADHERENT_UPDATED,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Adhérent modifié - " + adherent.getNomComplet());
    }
    
    /**
     * Génère automatiquement une notification lors de la suppression d'un adhérent.
     * 
     * @param adherent L'adhérent supprimé
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyAdherentDeleted(Adherent adherent) throws SQLException {
        String title = "Adhérent supprimé";
        String message = adherent.getNomComplet() + " a été supprimé du système";
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_ADHERENT_DELETED,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Adhérent supprimé - " + adherent.getNomComplet());
    }
    
    /**
     * Génère automatiquement une notification lors de la création d'un pack.
     * 
     * @param pack Le pack créé
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyPackCreated(Pack pack) throws SQLException {
        String title = "Nouveau pack créé";
        String message = String.format("Le pack \"%s\" (%.2f DH) a été ajouté au catalogue", 
            pack.getNom(), pack.getPrix());
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_PACK_CREATED,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Pack créé - " + pack.getNom());
    }
    
    /**
     * Génère automatiquement une notification lors de la modification d'un pack.
     * 
     * @param pack Le pack modifié
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyPackUpdated(Pack pack) throws SQLException {
        String title = "Pack modifié";
        String message = String.format("Le pack \"%s\" a été mis à jour", pack.getNom());
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_PACK_UPDATED,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Pack modifié - " + pack.getNom());
    }
    
    /**
     * Génère automatiquement une notification lors de la suppression d'un pack.
     * 
     * @param pack Le pack supprimé
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyPackDeleted(Pack pack) throws SQLException {
        String title = "Pack supprimé";
        String message = String.format("Le pack \"%s\" a été supprimé du catalogue", pack.getNom());
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_PACK_DELETED,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Pack supprimé - " + pack.getNom());
    }
    
    /**
     * Génère automatiquement une notification pour un paiement important (montant élevé).
     * 
     * @param paiement Le paiement important
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyHighPayment(Paiement paiement) throws SQLException {
        String adherentName = paiement.getAdherent() != null 
            ? paiement.getAdherent().getNomComplet() 
            : "Adhérent #" + paiement.getAdherentId();
        
        String title = "Paiement important reçu";
        String message = String.format("Paiement important de %.2f DH reçu de %s", 
            paiement.getMontant(), adherentName);
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_HIGH_PAYMENT,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Paiement important - " + paiement.getMontant() + " DH");
    }
    
    /**
     * Génère automatiquement une notification lorsqu'un objectif est atteint.
     * 
     * @param typeObjectif Type d'objectif (taux_occupation, revenus, adherents)
     * @param valeur Valeur atteinte
     * @throws SQLException Si une erreur survient lors de la création
     */
    public void notifyObjectiveReached(String typeObjectif, double valeur) throws SQLException {
        String title = "Objectif atteint !";
        String message;
        
        switch (typeObjectif) {
            case DashboardConstants.OBJECTIF_TYPE_TAUX_OCCUPATION:
                message = String.format("Taux d'occupation de %.1f%% atteint !", valeur);
                break;
            case DashboardConstants.OBJECTIF_TYPE_REVENUS:
                message = String.format("Objectif de revenus de %.2f DH atteint !", valeur);
                break;
            case DashboardConstants.OBJECTIF_TYPE_ADHERENTS:
                message = String.format("Objectif de %d adhérents atteint !", (int)valeur);
                break;
            default:
                message = String.format("Objectif de type %s avec valeur %.2f atteint !", typeObjectif, valeur);
        }
        
        Notification notification = new Notification(
            currentUserId,
            DashboardConstants.NOTIF_TYPE_OBJECTIVE_REACHED,
            title,
            message
        );
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationDAO.create(notification);
        logger.info("Notification créée: Objectif atteint - " + typeObjectif + " = " + valeur);
    }

    /**
     * Récupère les notifications récentes pour l'utilisateur actuel.
     * 
     * @param limit Nombre maximum de notifications à récupérer
     * @return Liste des notifications récentes (filtrées par utilisateur)
     * @throws SQLException Si une erreur survient
     */
    public List<Notification> getRecentNotifications(int limit) throws SQLException {
        // ✅ CORRIGÉ : Filtrer par utilisateur et limiter le nombre
        List<Notification> allNotifications = notificationDAO.findByUserId(currentUserId);
        if (allNotifications.size() <= limit) {
            return allNotifications;
        }
        // Retourner les N premières (déjà triées par date décroissante dans findByUserId)
        return allNotifications.subList(0, limit);
    }

    /**
     * Récupère les notifications non lues pour l'utilisateur actuel.
     * 
     * @return Liste des notifications non lues
     * @throws SQLException Si une erreur survient
     */
    public List<Notification> getUnreadNotifications() throws SQLException {
        return notificationDAO.findUnreadByUserId(currentUserId);
    }

    /**
     * Compte le nombre de notifications non lues pour l'utilisateur actuel.
     * 
     * @return Nombre de notifications non lues
     * @throws SQLException Si une erreur survient
     */
    public int getUnreadCount() throws SQLException {
        return notificationDAO.countUnreadByUserId(currentUserId);
    }

    /**
     * Marque une notification comme lue.
     * 
     * @param notificationId ID de la notification
     * @throws SQLException Si une erreur survient
     */
    public void markAsRead(Integer notificationId) throws SQLException {
        notificationDAO.markAsRead(notificationId);
    }

    /**
     * Marque toutes les notifications comme lues pour l'utilisateur actuel.
     * 
     * @throws SQLException Si une erreur survient
     */
    public void markAllAsRead() throws SQLException {
        notificationDAO.markAllAsRead(currentUserId);
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


