package com.example.demo.controllers;

import com.example.demo.dao.AdherentDAO;
import com.example.demo.dao.PaiementDAO;
import com.example.demo.dao.PackDAO;
import com.example.demo.models.Adherent;
import com.example.demo.models.Paiement;
import com.example.demo.models.Pack;
import com.example.demo.utils.SvgIcons;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Popup;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.chart.AreaChart;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Dashboard Premium avec design SaaS dark UI
 */
public class DashboardController {
    private static final Logger logger = Logger.getLogger(DashboardController.class.getName());
    private AdherentDAO adherentDAO = new AdherentDAO();
    private PaiementDAO paiementDAO = new PaiementDAO();
    private PackDAO packDAO = new PackDAO();
    private com.example.demo.dao.NotificationDAO notificationDAO = new com.example.demo.dao.NotificationDAO();
    private com.example.demo.services.NotificationService notificationService = 
        com.example.demo.services.NotificationService.getInstance();
    private com.example.demo.dao.ActivityDAO activityDAO = new com.example.demo.dao.ActivityDAO();
    private com.example.demo.services.ActivityService activityService = 
        com.example.demo.services.ActivityService.getInstance();
    
    // Références aux composants pour le refresh (chargés depuis FXML)
    @FXML private VBox content;
    @FXML private HBox kpiGrid;
    @FXML private HBox chartsRow;
    @FXML private VBox areaChartCard;
    @FXML private VBox bottomRow;
    
    // Composants KPI (chargés depuis FXML)
    @FXML private Label kpiRevenuValue;
    @FXML private Label kpiRevenuChange;
    @FXML private SVGPath kpiRevenuIcon;
    @FXML private Label kpiAdherentsValue;
    @FXML private Label kpiAdherentsChange;
    @FXML private SVGPath kpiAdherentsIcon;
    @FXML private Label kpiTauxValue;
    @FXML private Label kpiTauxGoal;
    @FXML private Canvas gaugeCanvas;
    @FXML private Label kpiNouveauxValue;
    @FXML private Label kpiNouveauxChange;
    @FXML private SVGPath kpiNouveauxIcon;
    
    // Composants Charts (chargés depuis FXML)
    @FXML private Canvas donutCanvas;
    @FXML private Label donutCenterValue;
    @FXML private VBox donutLegend;
    @FXML private GridPane miniCardsGrid;
    @FXML private SVGPath miniCard1Icon;
    @FXML private Label miniCard1Value;
    @FXML private Label miniCard1Badge;
    @FXML private SVGPath miniCard2Icon;
    @FXML private Label miniCard2Value;
    @FXML private Label miniCard2Badge;
    @FXML private SVGPath miniCard3Icon;
    @FXML private Label miniCard3Value;
    @FXML private Label miniCard3Badge;
    @FXML private SVGPath miniCard4Icon;
    @FXML private Label miniCard4Value;
    @FXML private Label miniCard4Badge;
    @FXML private StackPane areaChartContainer;
    
    // Composants Table (chargés depuis FXML)
    @FXML private ScrollPane tableScrollPane;
    @FXML private VBox tableBody;
    
    // Composants Sidebar (chargés depuis FXML)
    @FXML private VBox combinedList;
    @FXML private ScrollPane combinedScrollPane;
    @FXML private VBox combinedPanel;
    
    // Composants Header Icons (chargés depuis FXML)
    @FXML private SVGPath menuIcon;
    @FXML private SVGPath moonIcon;
    @FXML private SVGPath refreshIcon;
    @FXML private SVGPath bellIcon;
    @FXML private SVGPath globeIcon;
    
    // Filtre temporel actuel
    private com.example.demo.utils.DateRangeFilter.FilterType currentFilter = 
        com.example.demo.utils.DateRangeFilter.FilterType.TODAY;
    
    // Références aux composants UI (chargés depuis FXML)
    @FXML private HBox header;
    @FXML private Button menuBtn;
    @FXML private Label breadcrumbLabel;
    @FXML private Button moonBtn;
    @FXML private Button refreshBtn;
    @FXML private StackPane bellContainer;
    @FXML private Button bellBtn;
    @FXML private Label notificationBadge;
    @FXML private Button globeBtn;
    @FXML private HBox titleFilterSection;
    @FXML private Label titleLabel;
    @FXML private Button filterBtn;
    @FXML private Label filterLabel;
    @FXML private SVGPath chevronIcon;
    @FXML private ScrollPane contentScroll;
    @FXML private VBox contentWrapper;
    @FXML private VBox rightSidebar;
    
    public Parent getView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            
            // S'assurer que le BorderPane prend toute la hauteur disponible
            if (root instanceof javafx.scene.layout.BorderPane) {
                javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) root;
                borderPane.setMinHeight(0);
                borderPane.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
                borderPane.setMaxHeight(Double.MAX_VALUE);
            }
            
            // Charger le CSS du dashboard
            if (root.getScene() != null) {
                root.getScene().getStylesheets().add(
                    getClass().getResource("/css/dashboard.css").toExternalForm()
                );
            } else {
                // Si la scène n'existe pas encore, l'ajouter lors de l'ajout à la scène
                root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        newScene.getStylesheets().add(
                            getClass().getResource("/css/dashboard.css").toExternalForm()
                        );
                    }
                });
            }
            
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur détaillée lors du chargement du FXML dashboard: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            return createBasicView();
        }
    }

    /**
     * Initialise les composants après le chargement du FXML
     */
    @FXML
    private void initialize() {
        // Configurer le header
        setupHeader();
        
        // Configurer la section titre/filtre
        setupTitleFilterSection();
        
        // Configurer le contenu (charger les données et créer les éléments dynamiques)
        setupContent();
        
        // Configurer la sidebar droite
        setupRightSidebar();
        
        // Initialiser les icônes SVG
        setupHeaderIcons();
        
        // Charger les données initiales
        refreshDashboard();
    }

    /**
     * Configure les icônes SVG pour les boutons du header
     */
    private void setupHeaderIcons() {
        if (menuIcon != null) {
            menuIcon.setContent(getSvgPathForIcon("icon-menu"));
        }
        if (moonIcon != null) {
            moonIcon.setContent(getSvgPathForIcon("icon-moon"));
        }
        if (refreshIcon != null) {
            refreshIcon.setContent(getSvgPathForIcon("icon-refresh"));
        }
        if (bellIcon != null) {
            bellIcon.setContent(getSvgPathForIcon("icon-bell"));
        }
        if (globeIcon != null) {
            globeIcon.setContent(getSvgPathForIcon("icon-globe"));
        }
        if (chevronIcon != null) {
            chevronIcon.setContent(getSvgPathForIcon("icon-chevron-down"));
        }
    }
    
    /**
     * Configure le header
     */
    private void setupHeader() {
        if (menuBtn != null) {
            menuBtn.setOnAction(e -> toggleLeftSidebar(menuBtn));
        }
        if (moonBtn != null) {
            moonBtn.setOnAction(e -> {
                try {
                    com.example.demo.services.ThemeService themeService = com.example.demo.services.ThemeService.getInstance();
                    javafx.scene.Scene scene = moonBtn.getScene();
                    if (scene != null) {
                        themeService.toggleTheme(scene);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
        if (refreshBtn != null) {
            refreshBtn.setOnAction(e -> refreshDashboard());
        }
        if (bellBtn != null) {
            bellBtn.setOnAction(e -> showNotificationPopup(bellBtn));
        }
        
        // Charger le badge de notifications
        updateNotificationBadge();
    }

    /**
     * Configure la section titre/filtre
     */
    private void setupTitleFilterSection() {
        if (filterBtn != null) {
            ContextMenu filterMenu = createFilterMenu();
            filterBtn.setOnAction(e -> {
                javafx.geometry.Bounds bounds = filterBtn.localToScreen(filterBtn.getBoundsInLocal());
                filterMenu.show(filterBtn, bounds.getMinX(), bounds.getMaxY());
            });
        }
    }

    /**
     * Configure le contenu principal
     * Les composants sont déjà dans le FXML, on configure seulement les éléments dynamiques
     */
    private void setupContent() {
        if (content == null || contentWrapper == null) return;
        
        // Initialiser le graphique Area Chart dans le conteneur FXML
        if (areaChartContainer != null) {
            updateRevenueAreaChart();
        }
        
        // Les autres éléments dynamiques seront créés dans refreshDashboard()
    }

    /**
     * Configure la sidebar droite
     */
    private void setupRightSidebar() {
        // Remplir directement les listes existantes dans le FXML
        refreshSidebar();
    }

    /**
     * Initialise le NotificationService avec l'ID de l'utilisateur connecté
     */
    private void initializeNotificationService() {
        try {
            com.example.demo.models.Utilisateur currentUser = 
                com.example.demo.controllers.LoginController.getCurrentUser();
            
            if (currentUser != null) {
                notificationService.setCurrentUserId(currentUser.getId());
            } else {
                // Si aucun utilisateur connecté, utiliser l'ID par défaut (admin)
                notificationService.setCurrentUserId(1);
            }
        } catch (Exception e) {
            // En cas d'erreur, utiliser l'ID par défaut
            notificationService.setCurrentUserId(1);
        }
    }
    
    /**
     * Met à jour le badge de notifications
     */
    private void updateNotificationBadge() {
        if (notificationBadge == null || bellContainer == null) return;
        
        try {
            // ✅ CORRIGÉ : Initialiser le userId avant d'utiliser le service
            initializeNotificationService();
            
            com.example.demo.services.NotificationService notifService = 
                com.example.demo.services.NotificationService.getInstance();
            int unreadCount = notifService.getUnreadCount();
            if (unreadCount > 0) {
                notificationBadge.setText(String.valueOf(unreadCount));
                notificationBadge.setVisible(true);
                if (!bellContainer.getChildren().contains(notificationBadge)) {
                    bellContainer.getChildren().add(notificationBadge);
                }
            } else {
                notificationBadge.setVisible(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            notificationBadge.setVisible(false);
        }
    }
    
    /**
     * Affiche un popup avec la liste minimisée des notifications
     */
    private void showNotificationPopup(Button bellButton) {
        // Initialiser le service de notifications
        initializeNotificationService();
        
        // Créer le popup
        Popup notificationPopup = new Popup();
        notificationPopup.setAutoHide(true);
        
        // Container principal avec style dark
        VBox popupContainer = new VBox(0);
        // Tous les styles sont maintenant dans le CSS
        popupContainer.getStyleClass().add("notification-popup-container");
        
        // En-tête "Notifications"
        Label headerLabel = new Label("Notifications");
        headerLabel.getStyleClass().add("notification-popup-header");
        
        // Container scrollable pour la liste
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setMaxHeight(300);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("notification-popup-scroll");
        
        VBox notificationsList = new VBox(0);
        notificationsList.setPadding(new Insets(0));
        
        try {
            // Récupérer les notifications récentes (limité à 5 pour une liste minimisée)
            List<com.example.demo.models.Notification> dbNotifications = 
                notificationService.getRecentNotifications(5);
            
            if (dbNotifications.isEmpty()) {
                // Message si aucune notification
                Label emptyLabel = new Label("Aucune notification");
                emptyLabel.getStyleClass().add("notification-popup-empty");
                notificationsList.getChildren().add(emptyLabel);
            } else {
                // Convertir les notifications en items UI
                for (int i = 0; i < dbNotifications.size(); i++) {
                    com.example.demo.models.Notification notif = dbNotifications.get(i);
                    String iconName = getIconForNotificationType(notif.getType());
                    String timestamp = formatNotificationTimestamp(notif.getCreatedAt());
                    String displayText = (notif.getTitle() != null && !notif.getTitle().isEmpty()) 
                        ? notif.getTitle() 
                        : notif.getMessage();
                    
                    HBox notificationItem = createNotificationItem(iconName, displayText, timestamp);
                    
                    // Réduire la largeur max du texte pour le popup
                    if (notificationItem.getChildren().size() > 1) {
                        VBox textContent = (VBox) notificationItem.getChildren().get(1);
                        if (textContent.getChildren().size() > 0) {
                            Label msgLabel = (Label) textContent.getChildren().get(0);
                            msgLabel.setMaxWidth(240); // Ajuster pour le popup
                        }
                    }
                    
                    notificationsList.getChildren().add(notificationItem);
                    
                    // Ajouter une ligne de séparation sauf pour le dernier
                    if (i < dbNotifications.size() - 1) {
                        Region separator = new Region();
                        separator.setPrefHeight(1);
                        separator.getStyleClass().add("notification-popup-separator");
                        VBox.setMargin(separator, new Insets(8, 0, 8, 0));
                        notificationsList.getChildren().add(separator);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur de chargement");
            errorLabel.getStyleClass().add("notification-popup-error");
            notificationsList.getChildren().add(errorLabel);
        }
        
        scrollPane.setContent(notificationsList);
        popupContainer.getChildren().addAll(headerLabel, scrollPane);
        
        // Configurer le contenu du popup
        notificationPopup.getContent().add(popupContainer);
        
        // Calculer la position du popup (en dessous du bouton, aligné à droite)
        javafx.geometry.Bounds bounds = bellButton.localToScreen(bellButton.getBoundsInLocal());
        double popupWidth = 320;
        double buttonWidth = bellButton.getWidth();
        // Aligner le popup à droite du bouton (le côté droit du popup aligné avec le côté droit du bouton)
        double x = bounds.getMaxX() - popupWidth;
        double y = bounds.getMaxY() + 5; // 5px en dessous du bouton
        
        // S'assurer que le popup ne dépasse pas à gauche de l'écran
        if (x < bounds.getMinX() - buttonWidth) {
            x = bounds.getMinX() - buttonWidth; // Aligner à gauche du bouton si nécessaire
        }
        
        // Afficher le popup
        notificationPopup.show(bellButton.getScene().getWindow(), x, y);
    }

    /**
     * Crée le menu de filtre
     */
    private ContextMenu createFilterMenu() {
        ContextMenu filterMenu = new ContextMenu();
        
        MenuItem todayItem = new MenuItem(com.example.demo.utils.DateRangeFilter.getLabel(
            com.example.demo.utils.DateRangeFilter.FilterType.TODAY));
        MenuItem thisWeekItem = new MenuItem(com.example.demo.utils.DateRangeFilter.getLabel(
            com.example.demo.utils.DateRangeFilter.FilterType.THIS_WEEK));
        MenuItem thisMonthItem = new MenuItem(com.example.demo.utils.DateRangeFilter.getLabel(
            com.example.demo.utils.DateRangeFilter.FilterType.THIS_MONTH));
        MenuItem lastMonthItem = new MenuItem(com.example.demo.utils.DateRangeFilter.getLabel(
            com.example.demo.utils.DateRangeFilter.FilterType.LAST_MONTH));
        MenuItem thisYearItem = new MenuItem(com.example.demo.utils.DateRangeFilter.getLabel(
            com.example.demo.utils.DateRangeFilter.FilterType.THIS_YEAR));
        
        todayItem.setOnAction(e -> applyFilter(com.example.demo.utils.DateRangeFilter.FilterType.TODAY, filterLabel));
        thisWeekItem.setOnAction(e -> applyFilter(com.example.demo.utils.DateRangeFilter.FilterType.THIS_WEEK, filterLabel));
        thisMonthItem.setOnAction(e -> applyFilter(com.example.demo.utils.DateRangeFilter.FilterType.THIS_MONTH, filterLabel));
        lastMonthItem.setOnAction(e -> applyFilter(com.example.demo.utils.DateRangeFilter.FilterType.LAST_MONTH, filterLabel));
        thisYearItem.setOnAction(e -> applyFilter(com.example.demo.utils.DateRangeFilter.FilterType.THIS_YEAR, filterLabel));
        
        filterMenu.getItems().addAll(todayItem, thisWeekItem, thisMonthItem, lastMonthItem, 
            new SeparatorMenuItem(), thisYearItem);
        
        return filterMenu;
    }

    /**
     * Vue de secours si le FXML ne charge pas
     */
    private Parent createBasicView() {
        VBox root = new VBox(20);
        // Tous les styles sont maintenant dans le CSS
        root.getStyleClass().add("dashboard-root");
        
        Label errorLabel = new Label("Erreur lors du chargement de l'interface. Veuillez vérifier le fichier FXML.");
        errorLabel.getStyleClass().add("error-label");
        root.getChildren().add(errorLabel);
        
        return root;
    }
    
    /**
     * Charge une icône SVG
     */
    private Node loadSVGIcon(String iconName, double size, String color) {
        try {
            String svgPath = getSvgPathForIcon(iconName);
            
            if (svgPath != null && !svgPath.isEmpty()) {
                SVGPath svgPathNode = new SVGPath();
                svgPathNode.setContent(svgPath);
                
                // Configuration du style (toutes les icônes du header sont des outlines)
                svgPathNode.setFill(null);
                svgPathNode.setStroke(Color.web(color));
                svgPathNode.setStrokeWidth(2.0);
                svgPathNode.setStrokeLineCap(StrokeLineCap.ROUND);
                svgPathNode.setStrokeLineJoin(StrokeLineJoin.ROUND);
                
                double scale = size / 24.0;
                svgPathNode.setScaleX(scale);
                svgPathNode.setScaleY(scale);
                
                StackPane container = new StackPane();
                // Toutes les propriétés de taille et style sont maintenant dans le CSS
                container.getStyleClass().add("icon-container");
                container.getChildren().add(svgPathNode);
                
                return container;
            } else {
                // Fallback si le path SVG n'est pas trouvé
                Label fallback = new Label("•");
                fallback.getStyleClass().add("icon-fallback");
                return fallback;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'icône " + iconName + ": " + e.getMessage());
            e.printStackTrace();
            // Fallback en cas d'erreur
            Label fallback = new Label("•");
            fallback.getStyleClass().add("icon-fallback");
            return fallback;
        }
    }
    
    /**
     * Retourne le path SVG correspondant au nom de l'icône
     */
    private String getSvgPathForIcon(String iconName) {
        return switch (iconName) {
            case "icon-menu" -> SvgIcons.MENU;
            case "icon-moon" -> SvgIcons.MOON;
            case "icon-refresh" -> SvgIcons.REFRESH;
            case "icon-bell" -> SvgIcons.BELL;
            case "icon-globe" -> SvgIcons.GLOBE;
            case "icon-chevron-down" -> SvgIcons.CHEVRON_DOWN;
            case "icon-trending-up" -> SvgIcons.TRENDING_UP;
            case "icon-trending-down" -> SvgIcons.TRENDING_DOWN;
            case "icon-edit" -> SvgIcons.EDIT;
            case "icon-dollar" -> SvgIcons.DOLLAR_SIGN;
            case "icon-alert" -> SvgIcons.ALERT_OCTAGON;
            case "icon-bar-chart" -> SvgIcons.BAR_CHART_2;
            case "icon-users" -> SvgIcons.USERS;
            case "icon-shopping-bag" -> SvgIcons.PACK; // Utiliser PACK temporairement, à remplacer par SHOPPING_BAG
            case "icon-mail" -> SvgIcons.BELL; // Utiliser BELL temporairement, à remplacer par MAIL
            case "icon-package" -> SvgIcons.PACK;
            case "icon-archive" -> SvgIcons.PACK; // Utiliser PACK temporairement, à remplacer par ARCHIVE
            case "icon-file-x" -> SvgIcons.ALERT_OCTAGON; // Utiliser ALERT temporairement, à remplacer par FILE_X
            default -> null;
        };
    }
    
    /**
     * Change la couleur d'une icône SVGPath
     */
    private void setIconColor(Node iconContainer, String color) {
        if (iconContainer instanceof StackPane) {
            StackPane container = (StackPane) iconContainer;
            if (container.getChildren().size() > 0 && container.getChildren().get(0) instanceof SVGPath) {
                SVGPath svgPath = (SVGPath) container.getChildren().get(0);
                svgPath.setStroke(Color.web(color));
            }
        }
    }
    
    /**
     * Crée la grille de 4 KPI Cards (Row 1) - Dimensions exactes selon spécifications
     */
    private HBox createKPIGrid() {
        HBox container = new HBox(16); // Espacement horizontal entre cartes KPI : 16px (uniforme et maîtrisé)
        container.setPadding(new Insets(0)); // Pas de padding interne, géré par le conteneur parent
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("kpi-grid");
        
        // Faire en sorte que le container remplisse toute la largeur disponible
        HBox.setHgrow(container, Priority.ALWAYS);
        
        try {
            // Card 1: Revenus du Mois
            double revenusMois = paiementDAO.getRevenusMois(LocalDate.now());
            double revenusMoisPrecedent = paiementDAO.getRevenusMois(LocalDate.now().minusMonths(1));
            double changeRevenus = revenusMoisPrecedent > 0 ? ((revenusMois - revenusMoisPrecedent) / revenusMoisPrecedent) * 100 : 0;
            
            VBox card1 = createKPICardExact(
                "Revenus du Mois",
                String.format("%.0f DH", revenusMois),
                String.format("%.1f%% vs mois dernier", changeRevenus),
                changeRevenus >= 0
            );
            HBox.setHgrow(card1, Priority.ALWAYS); // Faire grandir la carte
            
            // Card 2: Adhérents Actifs
            int adherentsActifs = adherentDAO.findAll().size();
            double changeAdherents = adherentDAO.getMonthlyGrowth(LocalDate.now()); // Calculé depuis historique
            
            VBox card2 = createKPICardExact(
                "Adhérents Actifs",
                String.valueOf(adherentsActifs),
                String.format("%s%.1f%% ce mois", changeAdherents >= 0 ? "+" : "", changeAdherents),
                changeAdherents >= 0
            );
            HBox.setHgrow(card2, Priority.ALWAYS); // Faire grandir la carte
            
            // Card 3: Taux d'Occupation (avec gauge)
            double tauxOccupation = adherentDAO.getTauxOccupation();
            
            // Récupérer l'objectif depuis ObjectifDAO
            com.example.demo.dao.ObjectifDAO objectifDAO = new com.example.demo.dao.ObjectifDAO();
            com.example.demo.models.Objectif objectif = objectifDAO.findActiveByType(
                com.example.demo.utils.DashboardConstants.OBJECTIF_TYPE_TAUX_OCCUPATION
            );
            int objectifAdherents = objectif != null 
                ? objectif.getValeur().intValue() 
                : com.example.demo.utils.DashboardConstants.OBJECTIF_ADHERENTS_DEFAULT;
            
            StackPane card3 = createKPICardWithGaugeExact(
                "Taux d'Occupation",
                String.format("%.0f%%", tauxOccupation),
                String.format("Objectif: %d", objectifAdherents),
                tauxOccupation,
                objectifAdherents
            );
            HBox.setHgrow(card3, Priority.ALWAYS); // Faire grandir la carte
            
            // Card 4: Nouveaux Abonnements
            int nouveauxAbonnements = (int) adherentDAO.findAll().stream()
                .filter(a -> a.getDateInscription() != null && 
                    a.getDateInscription().isAfter(LocalDate.now().minusDays(30)))
                .count();
            
            // Calculer les nouveaux abonnements de cette semaine pour le badge
            int nouveauxCetteSemaine = (int) adherentDAO.findAll().stream()
                .filter(a -> a.getDateInscription() != null && 
                    a.getDateInscription().isAfter(LocalDate.now().minusDays(7)))
                .count();
            
            VBox card4 = createKPICardExact(
                "Nouveaux Abonnements",
                String.valueOf(nouveauxAbonnements),
                nouveauxCetteSemaine > 0 ? String.format("+%d cette semaine", nouveauxCetteSemaine) : "Ce mois",
                true
            );
            
            container.getChildren().addAll(card1, card2, card3, card4);
            
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des données");
            errorLabel.getStyleClass().add("kpi-error-label");
            container.getChildren().add(errorLabel);
        }
        
        return container;
    }
    
    /**
     * Crée une KPI Card avec dimensions selon le nouveau design
     */
    private VBox createKPICardExact(String label, String value, String change, boolean positive) {
        VBox card = new VBox(8); // Spacing vertical modéré
        card.setPadding(new Insets(12, 20, 12, 20)); // Padding augmenté: 12px top/bottom (+2px), 20px left/right (+2px)
        // Dimensions flexibles pour remplir l'espace disponible
        card.setMinWidth(220); // Largeur minimale augmentée pour éviter l'espace vide
        card.setPrefWidth(Region.USE_COMPUTED_SIZE); // Utiliser la largeur calculée
        card.setMaxWidth(Double.MAX_VALUE); // Permettre l'expansion maximale
        card.setMinHeight(140);
        card.setPrefHeight(140);
        card.setMaxHeight(140);
        card.getStyleClass().add("kpi-card");
        
        // Label titre (plus grand et bold)
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("kpi-label");
        labelLabel.setWrapText(true); // Permet le wrap pour éviter la troncature
        labelLabel.setMaxWidth(Double.MAX_VALUE); // Permet d'utiliser toute la largeur disponible
        VBox.setVgrow(labelLabel, Priority.NEVER); // Empêche le titre de prendre trop de place verticale
        HBox.setHgrow(labelLabel, Priority.ALWAYS); // Permet au titre d'utiliser toute la largeur horizontale
        
        // Valeur principale (30-36px, white bold)
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("kpi-value");
        
        // Indicateur de changement (12-13px, vert pour positif, BOLD)
        HBox changeContainer = new HBox(6); // Spacing de 6px avec l'icône
        changeContainer.setAlignment(Pos.CENTER_LEFT);
        changeContainer.getStyleClass().add("kpi-change");
        
        // Utiliser les icônes SVG trending-up/trending-down
        String iconName = positive ? "icon-trending-up" : "icon-trending-down";
        String iconColor = positive ? "#82E0AA" : "#ef4444"; // Vert plus clair pour positif
        Node trendIcon = loadSVGIcon(iconName, 14, iconColor);
        
        // Ajouter les classes CSS à l'icône
        if (trendIcon instanceof StackPane) {
            ((StackPane) trendIcon).getStyleClass().add("icon-container-tiny");
            Node svgNode = ((StackPane) trendIcon).getChildren().get(0);
            if (svgNode instanceof SVGPath) {
                ((SVGPath) svgNode).getStyleClass().add("icon-svg-tiny");
            }
        }
        
        // Vérifier que l'icône est bien créée
        if (trendIcon == null) {
            // Fallback si l'icône n'est pas chargée
            trendIcon = new Label(positive ? "↑" : "↓");
            trendIcon.getStyleClass().add(positive ? "kpi-change-label-positive" : "kpi-change-label-negative");
        }
        
        Label changeLabel = new Label(change);
        // Utiliser les classes CSS selon le signe
        if (positive) {
            changeLabel.getStyleClass().add("kpi-change-label-positive");
        } else {
            changeLabel.getStyleClass().add("kpi-change-label-negative");
        }
        
        changeContainer.getChildren().addAll(trendIcon, changeLabel);
        card.getChildren().addAll(labelLabel, valueLabel, changeContainer);
        
        // Animation
        FadeTransition fade = new FadeTransition(Duration.millis(500), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        
        return card;
    }
    
    /**
     * Crée une KPI Card avec gauge semi-circulaire sur le côté droit (selon nouveau design)
     */
    private StackPane createKPICardWithGaugeExact(String label, String value, String goal, double percentage, double goalValue) {
        StackPane cardContainer = new StackPane();
        // Dimensions flexibles pour remplir l'espace disponible
        cardContainer.setMinWidth(220); // Largeur minimale augmentée pour éviter l'espace vide
        cardContainer.setPrefWidth(Region.USE_COMPUTED_SIZE); // Utiliser la largeur calculée
        cardContainer.setMaxWidth(Double.MAX_VALUE); // Permettre l'expansion maximale
        cardContainer.setMinHeight(140);
        cardContainer.setPrefHeight(140);
        cardContainer.setMaxHeight(140);
        
        cardContainer.getStyleClass().add("kpi-card-with-gauge");
        
        // VBox principal avec seulement le texte (gauge temporairement supprimé pour tester)
        VBox mainContent = new VBox(8);
        mainContent.setPadding(new Insets(12, 20, 12, 20)); // Padding augmenté: 12px top/bottom (+2px), 20px left/right (+2px)
        mainContent.setAlignment(Pos.TOP_LEFT);
        mainContent.getStyleClass().add("kpi-card-content");
        
        Label titleLabel = new Label(label);
        titleLabel.getStyleClass().add("kpi-label");
        titleLabel.setWrapText(true); // Permet le wrap pour éviter la troncature
        titleLabel.setMaxWidth(Double.MAX_VALUE); // Permet d'utiliser toute la largeur disponible
        VBox.setVgrow(titleLabel, Priority.NEVER); // Empêche le titre de prendre trop de place verticale
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("kpi-value");
        
        Label goalLabel = new Label(goal);
        goalLabel.getStyleClass().add("kpi-goal-label");
        
        mainContent.getChildren().addAll(titleLabel, valueLabel, goalLabel);
        cardContainer.getChildren().add(mainContent);
        
        // Animation
        FadeTransition fade = new FadeTransition(Duration.millis(500), cardContainer);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        
        return cardContainer;
    }
    
    /**
     * Dessine un gauge semi-circulaire selon le nouveau design (vert avec marqueur)
     */
    private void drawSemiCircularGaugeNew(GraphicsContext gc, double percentage, double size) {
        double centerX = size / 2.0;
        double centerY = size / 2.0;
        double radius = size / 2.0 - 8; // Laisse de l'espace pour le stroke
        double strokeWidth = 3; // Arc fin
        
        // Arc de fond (light grey/white très clair)
        gc.setStroke(Color.web("#E0E0E0", 0.3)); // Très léger
        gc.setLineWidth(strokeWidth);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.strokeArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 
                     180, 180, ArcType.OPEN); // Semi-cercle de 180° (bas vers haut)
        
        // Arc rempli (vert vibrant)
        double filledAngle = (percentage / 100.0) * 180.0;
        gc.setStroke(Color.web("#82E0AA")); // Vert vibrant
        gc.strokeArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                     180, filledAngle, ArcType.OPEN);
        
        // Marqueur circulaire à la position du pourcentage
        double markerAngle = 180 + filledAngle;
        double markerRadius = radius + strokeWidth / 2;
        double markerX = centerX + Math.cos(Math.toRadians(markerAngle)) * markerRadius;
        double markerY = centerY + Math.sin(Math.toRadians(markerAngle)) * markerRadius;
        
        // Cercle marqueur (vert plus foncé)
        gc.setFill(Color.web("#4CAF50"));
        gc.fillOval(markerX - 3, markerY - 3, 6, 6);
        
        // Ligne aiguille (fine, vert foncé) du centre au marqueur
        gc.setStroke(Color.web("#4CAF50"));
        gc.setLineWidth(1.5);
        gc.strokeLine(centerX, centerY, markerX, markerY);
    }
    
    /**
     * Crée la ligne avec Donut Chart (60%) + Mini Cards Grid 2x2 (40%)
     */
    private HBox createChartsRowWithMiniCards() {
        HBox row = new HBox(20); // Espacement entre Sales Overview et Mini Cards : 20px (cohérent)
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(0)); // Pas de padding interne
        
        // Donut Chart Card (60% width)
        VBox donutCard = createPackDistributionDonutCard();
        HBox.setHgrow(donutCard, Priority.ALWAYS);
        donutCard.setPrefWidth(Region.USE_COMPUTED_SIZE);
        donutCard.setPrefHeight(Region.USE_COMPUTED_SIZE);
        donutCard.setMaxHeight(Double.MAX_VALUE);
        
        // Mini Cards Grid 2x2 (40% width) - prend toute la largeur et hauteur disponible
        GridPane miniCardsGrid = createMiniCardsGrid();
        HBox.setHgrow(miniCardsGrid, Priority.ALWAYS); // Faire grandir la grille pour prendre toute la largeur disponible
        miniCardsGrid.setPrefWidth(Region.USE_COMPUTED_SIZE);
        miniCardsGrid.setMaxWidth(Double.MAX_VALUE); // Permettre d'étendre jusqu'à la largeur maximale
        // Faire en sorte que le GridPane prenne la même hauteur que la carte Sales Overview
        miniCardsGrid.setPrefHeight(Region.USE_COMPUTED_SIZE);
        miniCardsGrid.setMaxHeight(Double.MAX_VALUE);
        // Lier la hauteur du GridPane à celle de la carte Sales Overview
        miniCardsGrid.prefHeightProperty().bind(donutCard.heightProperty());
        miniCardsGrid.minHeightProperty().bind(donutCard.heightProperty());
        
        row.getChildren().addAll(donutCard, miniCardsGrid);
        
        return row;
    }
    
    /**
     * Crée la grille de mini cards 2x2 - hauteur conforme à Sales Overview
     */
    private GridPane createMiniCardsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(16); // Espacement horizontal entre mini cards : 16px (uniforme)
        grid.setVgap(16); // Espacement vertical entre mini cards : 16px (uniforme)
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setPadding(new Insets(0)); // Pas de padding interne
        
        // Configurer les colonnes pour qu'elles prennent toute la largeur disponible (50% chacune)
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);
        
        // Configurer les lignes pour qu'elles prennent toute la hauteur disponible (50% chacune)
        // Cela permet aux cartes de s'adapter à la hauteur de la carte Sales Overview
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(50);
        row1.setVgrow(Priority.ALWAYS);
        row1.setMinHeight(Region.USE_PREF_SIZE);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        row2.setVgrow(Priority.ALWAYS);
        row2.setMinHeight(Region.USE_PREF_SIZE);
        grid.getRowConstraints().addAll(row1, row2);
        
        // Faire en sorte que le GridPane prenne toute la hauteur disponible
        grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
        grid.setMaxHeight(Double.MAX_VALUE);
        
        try {
            // Card 1: Nouveaux Adhérents
            int nouveauxAdherents = (int) adherentDAO.findAll().stream()
                .filter(a -> a.getDateInscription() != null && 
                    a.getDateInscription().isAfter(LocalDate.now().minusDays(7)))
                .count();
            
            VBox card1 = createMiniCard("Nouveaux adhérents", String.valueOf(nouveauxAdherents), "Cette semaine", true, "icon-users");
            GridPane.setColumnIndex(card1, 0);
            GridPane.setRowIndex(card1, 0);
            GridPane.setHgrow(card1, Priority.ALWAYS); // Prendre toute la largeur de la colonne
            GridPane.setVgrow(card1, Priority.ALWAYS); // Prendre toute la hauteur de la ligne
            
            // Card 2: Revenus Totaux
            double revenusSemaine = paiementDAO.findAll().stream()
                .filter(p -> p.getDatePaiement().toLocalDate().isAfter(LocalDate.now().minusDays(7)))
                .mapToDouble(Paiement::getMontant)
                .sum();
            
            VBox card2 = createMiniCard("Total profit", String.format("%.1fk DH", revenusSemaine / 1000), "+42% Weekly Profit", true, "icon-dollar");
            GridPane.setColumnIndex(card2, 1);
            GridPane.setRowIndex(card2, 0);
            GridPane.setHgrow(card2, Priority.ALWAYS); // Prendre toute la largeur de la colonne
            GridPane.setVgrow(card2, Priority.ALWAYS); // Prendre toute la hauteur de la ligne
            
            // Card 3: Expirations à venir
            int expirentBientot = adherentDAO.findExpiringSoon().size();
            
            VBox card3 = createMiniCard("Expirent dans 7 jours", String.valueOf(expirentBientot), "Action requise", false, "icon-alert");
            GridPane.setColumnIndex(card3, 0);
            GridPane.setRowIndex(card3, 1);
            GridPane.setHgrow(card3, Priority.ALWAYS); // Prendre toute la largeur de la colonne
            GridPane.setVgrow(card3, Priority.ALWAYS); // Prendre toute la hauteur de la ligne
            
            // Card 4: Taux moyen (montant moyen des paiements)
            double tauxMoyen = paiementDAO.getTauxMoyen();
            VBox card4 = createMiniCard("Taux moyen", String.format("%.0f DH", tauxMoyen), "Ce mois", true, "icon-bar-chart");
            GridPane.setColumnIndex(card4, 1);
            GridPane.setRowIndex(card4, 1);
            GridPane.setHgrow(card4, Priority.ALWAYS); // Prendre toute la largeur de la colonne
            GridPane.setVgrow(card4, Priority.ALWAYS); // Prendre toute la hauteur de la ligne
            
            grid.getChildren().addAll(card1, card2, card3, card4);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return grid;
    }
    
    /**
     * Crée une mini card - prend toute la largeur et hauteur disponible pour correspondre à Sales Overview
     * @param iconName Nom de l'icône SVG (ex: "icon-users", "icon-dollar", "icon-alert", "icon-bar-chart")
     */
    private VBox createMiniCard(String label, String value, String badge, boolean positive, String iconName) {
        VBox card = new VBox(10); // Espacement augmenté entre les éléments pour meilleure lisibilité
        card.setPadding(new Insets(12)); // Padding réduit de 16px à 12px
        // Retirer les hauteurs fixes pour permettre l'étirement vertical
        card.setPrefHeight(Region.USE_COMPUTED_SIZE);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setMaxHeight(Double.MAX_VALUE); // Permettre d'étendre jusqu'à la hauteur maximale
        card.setPrefWidth(Region.USE_COMPUTED_SIZE); // Utiliser la largeur calculée
        card.setMaxWidth(Double.MAX_VALUE); // Permettre d'étendre jusqu'à la largeur maximale
        VBox.setVgrow(card, Priority.ALWAYS); // Permettre à la carte de grandir verticalement
        card.getStyleClass().add("mini-card");
        
        HBox header = new HBox(10); // Espacement entre icône et titre
        header.setAlignment(Pos.TOP_LEFT); // Alignement en haut pour gérer les retours à la ligne
        
        // Icône SVG dans un container avec fond vert
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(32, 32);
        iconContainer.setMinSize(32, 32);
        iconContainer.setMaxSize(32, 32);
        iconContainer.getStyleClass().add("mini-card-icon-container");
        
        // Charger l'icône SVG au lieu d'un emoji
        Node iconNode = loadSVGIcon(iconName, 18, "#00E676"); // Vert clair pour les icônes
        if (iconNode instanceof StackPane) {
            Node svgNode = ((StackPane) iconNode).getChildren().get(0);
            if (svgNode instanceof SVGPath) {
                ((SVGPath) svgNode).getStyleClass().add("mini-card-icon");
            }
        }
        iconContainer.getChildren().add(iconNode);
        
        // Titre de la carte - plus grand et bold avec retour à la ligne automatique
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("mini-card-title");
        labelLabel.setWrapText(true); // Permettre le retour à la ligne automatique
        labelLabel.setMaxWidth(Double.MAX_VALUE); // Permettre l'expansion
        HBox.setHgrow(labelLabel, Priority.ALWAYS); // Prendre l'espace disponible
        
        header.getChildren().addAll(iconContainer, labelLabel);
        
        // Valeur principale - très grande et bold
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("mini-card-value");
        
        // Badge/Sous-titre - taille moyenne, weight selon le contexte
        Label badgeLabel = new Label(badge);
        if (positive) {
            badgeLabel.getStyleClass().add("mini-card-badge-positive");
        } else {
            badgeLabel.getStyleClass().add("mini-card-badge-warning");
        }
        
        // Ajouter un spacer pour permettre l'étirement vertical et une meilleure distribution
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        card.getChildren().addAll(header, valueLabel, badgeLabel, spacer);
        
        return card;
    }
    
    /**
     * Crée la card avec Donut Chart pour répartition des packs (style Sales Overview exact)
     */
    private VBox createPackDistributionDonutCard() {
        // Container principal avec fond sombre (style Sales Overview original) - padding réduit
        VBox container = new VBox(0);
        container.setPadding(new Insets(12)); // Padding réduit de 20px à 12px
        container.getStyleClass().add("sales-overview-card");
        container.setPrefWidth(Region.USE_COMPUTED_SIZE);
        container.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(container, Priority.ALWAYS); // Prendre toute la largeur disponible
        
        // Header avec titre "Sales Overview" style (texte clair sur fond sombre)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0)); // Padding réduit
        
        Label titleLabel = new Label("Sales Overview");
        titleLabel.getStyleClass().add("card-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Bouton menu (3 points verticaux)
        Button menuBtn = new Button("⋮");
        menuBtn.getStyleClass().add("menu-button");
        
        header.getChildren().addAll(titleLabel, spacer, menuBtn);
        
        // Content: Donut Chart au centre + Légende à droite
        HBox content = new HBox(20); // Espacement réduit de 30px à 20px
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10, 0, 0, 0)); // Padding réduit
        
        // ===== PARTIE GAUCHE : GRAPHE DONUT (style Sales Overview) =====
        StackPane chartContainer = new StackPane();
        chartContainer.getStyleClass().add("donut-container");
        chartContainer.setPrefSize(250, 250); // Taille réduite mais raisonnable
        chartContainer.setAlignment(Pos.CENTER);
        
        // Canvas pour le donut chart (dimensions réduites pour correspondre à l'image)
        Canvas donutCanvas = new Canvas(250, 250);
        GraphicsContext gc = donutCanvas.getGraphicsContext2D();
        
        // Dessiner le donut chart avec les 4 couleurs spécifiées
        drawSalesOverviewStyleDonutChart(gc, 250, 250);
        
        // Cercle central blanc avec texte sombre (style Sales Overview de l'image)
        Circle centerCircle = new Circle(60); // Rayon ajusté
        centerCircle.getStyleClass().add("donut-center-circle");
        
        // Texte central (style Sales Overview - texte sombre sur fond blanc)
        VBox centerText = new VBox(4);
        centerText.setAlignment(Pos.CENTER);
        centerText.getStyleClass().add("donut-center-text");
        
        Label centerTitle = new Label("ABONNEMENTS");
        centerTitle.getStyleClass().add("donut-center-label");
        
        Label centerValue = new Label("TOTAL: 357");
        centerValue.getStyleClass().add("donut-center-value");
        
        centerText.getChildren().addAll(centerTitle, centerValue);
        
        chartContainer.getChildren().addAll(donutCanvas, centerCircle, centerText);
        
        // ===== PARTIE DROITE : LÉGENDE MINIMALISTE (texte clair sur fond sombre) =====
        VBox legendSection = new VBox(10); // Espacement réduit
        legendSection.getStyleClass().add("donut-legend");
        legendSection.setPrefWidth(150); // Largeur réduite
        legendSection.setAlignment(Pos.CENTER_LEFT);
        legendSection.setPadding(new Insets(0, 0, 0, 10)); // Padding réduit
        
        // Liste verticale des catégories
        VBox legendList = new VBox(10); // Espacement réduit de 14px à 10px
        legendList.setAlignment(Pos.TOP_LEFT);
        
        try {
            List<Pack> packs = packDAO.findAll();
            List<Adherent> adherents = adherentDAO.findAll();
            
            // Palette de couleurs vertes (variations de vert selon l'image)
            // Du vert clair/jaunâtre au vert foncé/saturé
            String[] colors = {
                "#A8E063",  // Vert clair/jaunâtre (pale lime)
                "#4ECDC4",  // Vert moyen/cyan (teal)
                "#10B981",  // Vert vibrant (bright green)
                "#059669"   // Vert foncé/saturé (dark green)
            };
            
            // Créer une map pour associer pack ID à couleur
            java.util.Map<String, String> packColorMap = new java.util.HashMap<>();
            java.util.List<java.util.Map.Entry<Pack, Long>> packCountsSorted = new java.util.ArrayList<>();
            
            for (Pack pack : packs) {
                long count = adherents.stream()
                    .filter(a -> a.getPackId() != null && a.getPackId().equals(pack.getId()))
                    .count();
                if (count > 0) {
                    packCountsSorted.add(new java.util.AbstractMap.SimpleEntry<>(pack, count));
                }
            }
            
            // Trier par nombre décroissant et limiter à 4 catégories
            packCountsSorted.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));
            int maxCategories = Math.min(4, packCountsSorted.size());
            
            // Mettre à jour le total dans le cercle central
            int totalAdherents = adherents.size();
            centerValue.setText("TOTAL: " + totalAdherents);
            
            for (int i = 0; i < maxCategories; i++) {
                java.util.Map.Entry<Pack, Long> entry = packCountsSorted.get(i);
                Pack pack = entry.getKey();
                long count = entry.getValue();
                String color = colors[i % colors.length];
                packColorMap.put(pack.getId().toString(), color);
                
                // Créer l'item de légende minimaliste (texte clair sur fond sombre)
                VBox legendItem = createSalesOverviewLegendItem(pack.getNom(), (int)count, color);
                legendList.getChildren().add(legendItem);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        legendSection.getChildren().add(legendList);
        
        // Assembler le contenu
        content.getChildren().addAll(chartContainer, legendSection);
        container.getChildren().addAll(header, content);
        
        return container;
    }
    
    /**
     * Dessine un donut chart style Sales Overview avec 4 couleurs spécifiques
     */
    private void drawSalesOverviewStyleDonutChart(GraphicsContext gc, double width, double height) {
        try {
            // Utiliser la méthode getDistributionByAdherents() pour obtenir la distribution réelle
            java.util.Map<Pack, Integer> distribution = packDAO.getDistributionByAdherents();
            
            // Convertir la Map en liste triée par nombre d'adhérents décroissant
            java.util.List<java.util.Map.Entry<Pack, Integer>> packData = new java.util.ArrayList<>(distribution.entrySet());
            
            // Trier par nombre décroissant et limiter à 4
            packData.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));
            int maxCategories = Math.min(4, packData.size());
            
            // Palette de couleurs vertes (variations de vert selon l'image)
            // Du vert clair/jaunâtre au vert foncé/saturé
            String[] colors = {
                "#A8E063",  // Vert clair/jaunâtre (pale lime)
                "#4ECDC4",  // Vert moyen/cyan (teal)
                "#10B981",  // Vert vibrant (bright green)
                "#059669"   // Vert foncé/saturé (dark green)
            };
            
            if (maxCategories == 0) {
                // Dessiner un cercle vide si pas de données
                gc.setFill(Color.web("#e0e0e0"));
                gc.fillOval(20, 20, width - 40, height - 40);
                return;
            }
            
            // Paramètres du donut (style Sales Overview - dimensions ajustées)
            double centerX = width / 2;
            double centerY = height / 2;
            double outerRadius = 120; // Rayon extérieur (ajusté pour 300x300)
            double innerRadius = 75; // Rayon intérieur (trou central pour le texte)
            
            // Calculer le total
            int total = packData.stream()
                .limit(maxCategories)
                .mapToInt(java.util.Map.Entry::getValue)
                .sum();
            
            // Dessiner les segments avec bordures blanches fines
            double startAngle = -90; // Commencer en haut
            
            for (int i = 0; i < maxCategories; i++) {
                java.util.Map.Entry<Pack, Integer> entry = packData.get(i);
                int value = entry.getValue();
                double angle = (value * 360.0) / total;
                
                if (angle <= 0) continue;
                
                // Couleur du segment (variations de vert)
                Color segmentColor = Color.web(colors[i % colors.length]);
                gc.setFill(segmentColor);
                gc.setStroke(Color.WHITE); // Bordure blanche fine entre segments
                gc.setLineWidth(1.5); // Bordure plus fine et discrète
                
                // Point de départ sur l'arc extérieur
                double startRad = Math.toRadians(startAngle);
                double endRad = Math.toRadians(startAngle + angle);
                
                double startXOuter = centerX + outerRadius * Math.cos(startRad);
                double startYOuter = centerY + outerRadius * Math.sin(startRad);
                
                // Dessiner le segment donut
                gc.beginPath();
                gc.moveTo(startXOuter, startYOuter);
                
                // Arc extérieur
                for (double a = startAngle; a <= startAngle + angle; a += 0.5) {
                    double rad = Math.toRadians(a);
                    double x = centerX + outerRadius * Math.cos(rad);
                    double y = centerY + outerRadius * Math.sin(rad);
                    gc.lineTo(x, y);
                }
                
                // Ligne vers l'intérieur
                double endXInner = centerX + innerRadius * Math.cos(endRad);
                double endYInner = centerY + innerRadius * Math.sin(endRad);
                gc.lineTo(endXInner, endYInner);
                
                // Arc intérieur (sens inverse)
                for (double a = startAngle + angle; a >= startAngle; a -= 0.5) {
                    double rad = Math.toRadians(a);
                    double x = centerX + innerRadius * Math.cos(rad);
                    double y = centerY + innerRadius * Math.sin(rad);
                    gc.lineTo(x, y);
                }
                
                gc.closePath();
                gc.fill();
                gc.stroke();
                
                startAngle += angle;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Crée un item de légende style Sales Overview (carré de couleur + nom complet + valeur)
     */
    private VBox createSalesOverviewLegendItem(String packName, int value, String color) {
        VBox item = new VBox(4);
        item.setAlignment(Pos.TOP_LEFT);
        
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        // Carré de couleur (style Sales Overview)
        Rectangle colorSquare = new Rectangle(10, 10);
        colorSquare.setFill(Color.web(color));
        colorSquare.setArcWidth(2);
        colorSquare.setArcHeight(2);
        
        // Nom COMPLET du pack (pas tronqué, style Sales Overview - texte clair sur fond sombre)
        Label nameLabel = new Label(packName);
        nameLabel.getStyleClass().add("legend-item-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(120); // Largeur ajustée pour la nouvelle taille
        
        headerRow.getChildren().addAll(colorSquare, nameLabel);
        
        // Valeur avec unité (style Sales Overview - texte clair sur fond sombre)
        String unitText = value == 1 ? "abonné" : "abonnés";
        Label valueLabel = new Label(value + " " + unitText);
        valueLabel.getStyleClass().add("legend-item-value");
        valueLabel.setPadding(new Insets(2, 0, 0, 18)); // Aligné avec le texte
        
        item.getChildren().addAll(headerRow, valueLabel);
        
        return item;
    }
    
    /**
     * Crée un item de catégorie pour la grille (design amélioré avec texte complet et formatage)
     */
    private VBox createCategoryItemNew(String label, int value, String color) {
        VBox item = new VBox(8);
        item.setAlignment(Pos.TOP_LEFT);
        item.setPrefWidth(Region.USE_COMPUTED_SIZE);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setMinWidth(120); // Largeur minimale pour éviter les troncatures
        
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        // Point de couleur (légèrement plus grand pour meilleure visibilité)
        Circle dot = new Circle(7);
        dot.setFill(Color.web(color));
        
        // Label avec texte complet (pas de troncature)
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("category-item-label");
        labelLabel.setWrapText(true);
        labelLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(labelLabel, Priority.ALWAYS);
        
        headerRow.getChildren().addAll(dot, labelLabel);
        
        // Valeur formatée avec unité "abonnés"
        String formattedValue = formatNumber(value);
        Label valueLabel = new Label(formattedValue + " abonné" + (value > 1 ? "s" : ""));
        valueLabel.getStyleClass().add("category-item-value");
        
        item.getChildren().addAll(headerRow, valueLabel);
        
        return item;
    }
    
    /**
     * Formate un nombre avec séparateurs de milliers si nécessaire
     */
    private String formatNumber(int number) {
        if (number >= 1000) {
            return String.format("%,d", number).replace(",", " ");
        }
        return String.valueOf(number);
    }
    
    /**
     * Crée la ligne avec Table (100% de largeur)
     * Note: Cette méthode retourne un VBox pour correspondre au FXML
     */
    private VBox createBottomRowWithTable() {
        // Table Adhérents Récents (100% de largeur)
        VBox tableCard = createTableCard("Adhérents Récents");
        tableCard.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        return tableCard;
    }
    
    /**
     * Crée la card Liste Rouge pour les impayés
     */
    private VBox createRedListCard() {
        VBox container = new VBox(16);
        // Tous les styles sont maintenant dans le CSS
        container.getStyleClass().add("red-list-card");
        
        Label titleLabel = new Label("⚠️ Impayés - Action Requise");
        titleLabel.getStyleClass().add("red-list-title");
        
        try {
            int count = adherentDAO.findExpired().size();
            Label countLabel = new Label(String.valueOf(count) + " adhérents");
            countLabel.getStyleClass().add("red-list-count");
            
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("table-scroll");
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            
            VBox listContainer = new VBox(12);
            listContainer.setPadding(new Insets(8, 0, 8, 0));
            
            List<Adherent> expired = adherentDAO.findExpired();
            int maxItems = Math.min(5, expired.size());
            
            for (int i = 0; i < maxItems; i++) {
                Adherent a = expired.get(i);
                long joursRetard = java.time.temporal.ChronoUnit.DAYS.between(a.getDateFin(), LocalDate.now());
                
                HBox item = new HBox(12);
                item.setPadding(new Insets(12));
                item.setAlignment(Pos.CENTER_LEFT);
                item.getStyleClass().add("red-list-item");
                
                // Avatar
                Circle avatar = new Circle(20);
                avatar.setFill(Color.web("#EF4444"));
                
                VBox itemContent = new VBox(4);
                Label nameLabel = new Label(a.getNomComplet());
                nameLabel.getStyleClass().add("red-list-name");
                
                Label detailLabel = new Label("Retard: " + joursRetard + " jours");
                detailLabel.getStyleClass().add("red-list-detail");
                
                itemContent.getChildren().addAll(nameLabel, detailLabel);
                item.getChildren().addAll(avatar, itemContent);
                
                listContainer.getChildren().add(item);
            }
            
            scrollPane.setContent(listContainer);
            
            Button voirTousBtn = new Button("Voir tous");
            voirTousBtn.getStyleClass().add("red-list-button");
            
            container.getChildren().addAll(titleLabel, countLabel, scrollPane, voirTousBtn);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return container;
    }
    
    /**
     * Crée la ligne de KPI Cards (ancienne méthode - gardée pour compatibilité)
     */
    private HBox createKPIRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        
        try {
            // KPI 1: Revenus du mois
            double revenusMois = paiementDAO.getRevenusMois(LocalDate.now());
            double revenusMoisPrecedent = paiementDAO.getRevenusMois(LocalDate.now().minusMonths(1));
            double changeRevenus = revenusMoisPrecedent > 0 ? ((revenusMois - revenusMoisPrecedent) / revenusMoisPrecedent) * 100 : 0;
            
            StackPane kpi1 = createKPICard(
                null,
                "Revenus du Mois",
                String.format("%.0f DH", revenusMois),
                String.format("%.1f%% vs mois dernier", changeRevenus),
                changeRevenus >= 0
            );
            
            // KPI 2: Adhérents actifs
            int adherentsActifs = adherentDAO.findAll().size();
            int adherentsActifsPrecedent = adherentsActifs; // Simplified
            double changeAdherents = 5.2; // Example
            
            StackPane kpi2 = createKPICard(
                null,
                "Adhérents Actifs",
                String.valueOf(adherentsActifs),
                String.format("+%.1f%% ce mois", changeAdherents),
                true
            );
            
            // KPI 3: Nouveaux abonnements
            int nouveauxAbonnements = 0;
            try {
                List<Adherent> allAdherents = adherentDAO.findAll();
                nouveauxAbonnements = (int) allAdherents.stream()
                    .filter(a -> a.getDateInscription() != null && 
                        a.getDateInscription().isAfter(LocalDate.now().minusDays(30)))
                    .count();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            StackPane kpi3 = createKPICard(
                null,
                "Nouveaux Abonnements",
                String.valueOf(nouveauxAbonnements),
                "Ce mois",
                true
            );
            
            // KPI 4: Abonnements expirés
            int abonnementsExpires = adherentDAO.findExpired().size();
            
            StackPane kpi4 = createKPICard(
                null,
                "Abonnements Expirés",
                String.valueOf(abonnementsExpires),
                "Action requise",
                false
            );
            
            // KPI 5: Taux de renouvellement avec gauge (carte spéciale avec glow)
            double tauxRenouvellement = 75.5;
            
            StackPane kpi5 = createKPICardWithGauge(
                "Taux de Renouvellement",
                String.format("%.1f%%", tauxRenouvellement),
                "Objectif: 80%",
                tauxRenouvellement,
                80.0
            );
            
            row.getChildren().addAll(kpi1, kpi2, kpi3, kpi4, kpi5);
            
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des données");
            errorLabel.getStyleClass().add("kpi-error-label");
            row.getChildren().add(errorLabel);
        }
        
        return row;
    }
    
    /**
     * Crée une carte KPI premium selon le design de l'image
     */
    private StackPane createKPICard(String icon, String label, String value, String change, boolean positive) {
        // Container avec StackPane pour le contenu
        StackPane cardContainer = new StackPane();
        cardContainer.setPrefWidth(260);
        cardContainer.setPrefHeight(130);
        cardContainer.setMinWidth(260);
        cardContainer.setMinHeight(130);
        cardContainer.setMaxWidth(260);
        cardContainer.setMaxHeight(130);
        
        // Background de la carte
        Region cardBackground = new Region();
        cardBackground.getStyleClass().add("kpi-card");
        cardBackground.setPrefSize(260, 130);
        
        // Contenu de la carte
        VBox card = new VBox(6);
        card.setPadding(new Insets(16));
        card.setAlignment(Pos.TOP_LEFT);
        
        // Titre (petit, gris clair)
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("kpi-label");
        
        // Valeur principale (grande, blanche, bold)
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("kpi-value");
        
        // Comparaison avec icône trending-up/trending-down
        HBox changeContainer = new HBox(6);
        changeContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Icône trending-up ou trending-down selon le signe
        String iconName = positive ? "icon-trending-up" : "icon-trending-down";
        String iconColor = positive ? "#9EFF00" : "#EF4444";
        Node trendIcon = loadSVGIcon(iconName, 12, iconColor);
        
        Label changeLabel = new Label(change);
        // Utiliser les classes CSS selon le signe
        if (positive) {
            changeLabel.getStyleClass().add("kpi-change-label-positive");
        } else {
            changeLabel.getStyleClass().add("kpi-change-label-negative");
        }
        
        changeContainer.getChildren().addAll(trendIcon, changeLabel);
        
        card.getChildren().addAll(labelLabel, valueLabel, changeContainer);
        
        // Ajouter le background et le contenu au container
        cardContainer.getChildren().addAll(cardBackground, card);
        
        // Animation on load
        FadeTransition fade = new FadeTransition(Duration.millis(500), cardContainer);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        
        return cardContainer;
    }
    
    /**
     * Crée une carte KPI avec gauge circulaire et effet de glow vert
     */
    private StackPane createKPICardWithGauge(String label, String value, String goal, double percentage, double goalValue) {
        // Container avec StackPane pour l'effet de glow
        StackPane cardContainer = new StackPane();
        cardContainer.setPrefWidth(260);
        cardContainer.setPrefHeight(130);
        cardContainer.setMinWidth(260);
        cardContainer.setMinHeight(130);
        cardContainer.setMaxWidth(260);
        cardContainer.setMaxHeight(130);
        
        // Background sans image PNG (effet de glow annulé)
        Region cardBackground = new Region();
        cardBackground.getStyleClass().add("kpi-card-with-gauge");
        cardBackground.setPrefSize(260, 130);
        
        // Contenu de la carte
        VBox card = new VBox(6);
        card.setPadding(new Insets(16));
        card.setAlignment(Pos.TOP_LEFT);
        
        // Titre
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("kpi-label");
        
        // Valeur principale
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("kpi-value");
        
        // Goal text
        Label goalLabel = new Label(goal);
        goalLabel.getStyleClass().add("kpi-goal-label");
        
        // Container pour le contenu et le gauge
        HBox contentWithGauge = new HBox();
        contentWithGauge.setAlignment(Pos.CENTER_LEFT);
        
        VBox leftContent = new VBox(8);
        leftContent.getChildren().addAll(labelLabel, valueLabel, goalLabel);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Gauge circulaire (coin inférieur droit)
        StackPane gaugeContainer = createCircularGauge(percentage, goalValue);
        gaugeContainer.setAlignment(Pos.BOTTOM_RIGHT);
        
        contentWithGauge.getChildren().addAll(leftContent, spacer, gaugeContainer);
        
        card.getChildren().add(contentWithGauge);
        
        // Ajouter le background et le contenu au container (sans image PNG)
        cardContainer.getChildren().addAll(cardBackground, card);
        
        // Animation on load
        FadeTransition fade = new FadeTransition(Duration.millis(500), cardContainer);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        
        return cardContainer;
    }
    
    /**
     * Crée un gauge circulaire avec progression
     */
    private StackPane createCircularGauge(double percentage, double maxValue) {
        StackPane gaugeContainer = new StackPane();
        gaugeContainer.setPrefSize(65, 65);
        
        // Cercle de fond (gris clair)
        Circle backgroundCircle = new Circle(28);
        backgroundCircle.setFill(Color.TRANSPARENT);
        backgroundCircle.setStroke(Color.web("#2A2F38"));
        backgroundCircle.setStrokeWidth(6);
        
        // Arc de progression (vert)
        double angle = (percentage / maxValue) * 270; // 270 degrés pour 3/4 de cercle
        javafx.scene.shape.Arc progressArc = new javafx.scene.shape.Arc();
        progressArc.setCenterX(0);
        progressArc.setCenterY(0);
        progressArc.setRadiusX(28);
        progressArc.setRadiusY(28);
        progressArc.setStartAngle(135); // Commence en bas à gauche
        progressArc.setLength(angle);
        progressArc.setType(javafx.scene.shape.ArcType.OPEN);
        progressArc.setFill(Color.TRANSPARENT);
        progressArc.setStroke(Color.web("#9EFF00"));
        progressArc.setStrokeWidth(6);
        progressArc.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Aiguille/indicateur pointant vers le pourcentage
        double needleAngle = 135 + angle;
        double needleLength = 24;
        double needleX = Math.cos(Math.toRadians(needleAngle)) * needleLength;
        double needleY = Math.sin(Math.toRadians(needleAngle)) * needleLength;
        
        javafx.scene.shape.Line needle = new javafx.scene.shape.Line(0, 0, needleX, needleY);
        needle.setStroke(Color.web("#9EFF00"));
        needle.setStrokeWidth(2);
        
        gaugeContainer.getChildren().addAll(backgroundCircle, progressArc, needle);
        
        return gaugeContainer;
    }
    
    /**
     * Crée la ligne avec les graphiques
     */
    private HBox createChartsRow() {
        HBox row = new HBox(20);
        
        // Chart 1: Revenus mensuels (Line Chart)
        VBox chart1Container = createChartCard("Revenus Mensuels", createRevenueChart());
        HBox.setHgrow(chart1Container, Priority.ALWAYS);
        
        // Chart 2: Répartition des packs (Pie Chart)
        VBox chart2Container = createChartCard("Répartition des Packs", createPackDistributionChart());
        chart2Container.setPrefWidth(400);
        
        row.getChildren().addAll(chart1Container, chart2Container);
        
        return row;
    }
    
    /**
     * Crée un conteneur de carte pour graphique
     */
    private VBox createChartCard(String title, Chart chart) {
        VBox container = new VBox(16);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("card");
        container.setPrefHeight(400);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        
        chart.setPrefHeight(350);
        
        container.getChildren().addAll(titleLabel, chart);
        
        return container;
    }
    
    /**
     * Crée le graphique des revenus mensuels
     */
    private LineChart<String, Number> createRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Montant (DH)");
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(true);
        lineChart.getStyleClass().add("chart");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        try {
            // Récupérer les revenus des 6 derniers mois
            for (int i = 5; i >= 0; i--) {
                LocalDate month = LocalDate.now().minusMonths(i);
                double revenus = paiementDAO.getRevenusMois(month);
                String monthName = month.format(DateTimeFormatter.ofPattern("MMM"));
                series.getData().add(new XYChart.Data<>(monthName, revenus));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        lineChart.getData().add(series);
        
        return lineChart;
    }
    
    /**
     * Crée le graphique de répartition des packs
     */
    /**
     * Dessine un donut chart personnalisé avec Canvas (amélioré)
     */
    private void drawCustomDonutChart(GraphicsContext gc, double width, double height, Label centerValueLabel) {
        try {
            List<Pack> packs = packDAO.findAll();
            List<Adherent> adherents = adherentDAO.findAll();
            
            // Calculer les données
            java.util.List<java.util.Map.Entry<String, Long>> packData = new java.util.ArrayList<>();
            java.util.Map<String, String> packNames = new java.util.HashMap<>();
            
            for (Pack pack : packs) {
                long count = adherents.stream()
                    .filter(a -> a.getPackId() != null && a.getPackId().equals(pack.getId()))
                    .count();
                if (count > 0) {
                    packData.add(new java.util.AbstractMap.SimpleEntry<>(pack.getId().toString(), count));
                    packNames.put(pack.getId().toString(), pack.getNom());
                }
            }
            
            if (packData.isEmpty()) {
                // Dessiner un cercle vide si pas de données
                gc.setFill(Color.web("#2D2D2D"));
                gc.fillOval(20, 20, width - 40, height - 40);
                gc.setFill(Color.web("#1A2332"));
                gc.fillOval(60, 60, width - 120, height - 120);
                return;
            }
            
            // Couleurs pour les segments (orange, bleu, violet, vert selon l'exemple)
            String[] colors = {"#FF6B35", "#4ECDC4", "#6A0572", "#45B7D1", "#82E0AA", "#00E676"};
            
            // Paramètres du donut
            double centerX = width / 2;
            double centerY = height / 2;
            double outerRadius = 90;
            double innerRadius = 50;
            
            // Calculer le total
            long total = packData.stream().mapToLong(java.util.Map.Entry::getValue).sum();
            centerValueLabel.setText(String.valueOf(total));
            
            // Dessiner les segments avec Path2D pour créer un vrai donut
            double startAngle = -90; // Commencer en haut
            int colorIndex = 0;
            
            for (java.util.Map.Entry<String, Long> entry : packData) {
                long value = entry.getValue();
                double angle = (value * 360.0) / total;
                
                if (angle <= 0) continue;
                
                // Couleur du segment
                Color segmentColor = Color.web(colors[colorIndex % colors.length]);
                gc.setFill(segmentColor);
                gc.setStroke(Color.web("#1A2332"));
                gc.setLineWidth(2);
                
                // Point de départ sur l'arc extérieur
                double startRad = Math.toRadians(startAngle);
                double endRad = Math.toRadians(startAngle + angle);
                
                double startXOuter = centerX + outerRadius * Math.cos(startRad);
                double startYOuter = centerY + outerRadius * Math.sin(startRad);
                
                // Ligne vers l'intérieur
                double endXInner = centerX + innerRadius * Math.cos(endRad);
                double endYInner = centerY + innerRadius * Math.sin(endRad);
                
                // Dessiner le segment donut avec Canvas
                gc.beginPath();
                gc.moveTo(startXOuter, startYOuter);
                
                // Arc extérieur
                for (double a = startAngle; a <= startAngle + angle; a += 1) {
                    double rad = Math.toRadians(a);
                    double x = centerX + outerRadius * Math.cos(rad);
                    double y = centerY + outerRadius * Math.sin(rad);
                    gc.lineTo(x, y);
                }
                
                // Ligne vers l'intérieur
                gc.lineTo(endXInner, endYInner);
                
                // Arc intérieur (sens inverse)
                for (double a = startAngle + angle; a >= startAngle; a -= 1) {
                    double rad = Math.toRadians(a);
                    double x = centerX + innerRadius * Math.cos(rad);
                    double y = centerY + innerRadius * Math.sin(rad);
                    gc.lineTo(x, y);
                }
                
                gc.closePath();
                gc.fill();
                gc.stroke();
                
                // Dessiner le label dans le segment (si l'angle est suffisant)
                if (angle > 20) {
                    double midAngle = startAngle + angle / 2;
                    double midRad = Math.toRadians(midAngle);
                    double labelRadius = (outerRadius + innerRadius) / 2;
                    double labelX = centerX + labelRadius * Math.cos(midRad);
                    double labelY = centerY + labelRadius * Math.sin(midRad);
                    
                    // Texte du label (nom complet du pack)
                    String packName = packNames.get(entry.getKey());
                    
                    // Tronquer le nom si trop long pour le segment
                    String displayName = packName.length() > 15 ? packName.substring(0, 12) + "..." : packName;
                    
                    gc.setFill(Color.WHITE);
                    gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 11));
                    
                    // Mesurer le texte pour le centrer
                    javafx.scene.text.Text text = new javafx.scene.text.Text(displayName);
                    text.setFont(gc.getFont());
                    double textWidth = text.getLayoutBounds().getWidth();
                    
                    gc.fillText(displayName, labelX - textWidth / 2, labelY - 5);
                    
                    // Valeur formatée
                    String formattedValue = formatNumber((int)value);
                    gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.NORMAL, 10));
                    javafx.scene.text.Text valueText = new javafx.scene.text.Text(formattedValue);
                    valueText.setFont(gc.getFont());
                    double valueWidth = valueText.getLayoutBounds().getWidth();
                    gc.fillText(formattedValue, labelX - valueWidth / 2, labelY + 8);
                }
                
                startAngle += angle;
                colorIndex++;
            }
            
            // Dessiner le cercle intérieur (trou central) - déjà fait par le background
            gc.setFill(Color.web("#1A2332"));
            gc.fillOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Ancienne méthode - conservée pour compatibilité (non utilisée)
     */
    @Deprecated
    private PieChart createPackDistributionChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setAnimated(true);
        pieChart.getStyleClass().add("chart");
        
        try {
            List<com.example.demo.models.Pack> packs = packDAO.findAll();
            List<Adherent> adherents = adherentDAO.findAll();
            
            for (com.example.demo.models.Pack pack : packs) {
                long count = adherents.stream()
                    .filter(a -> a.getPackId() != null && a.getPackId().equals(pack.getId()))
                    .count();
                
                if (count > 0) {
                    PieChart.Data slice = new PieChart.Data(pack.getNom() + " (" + count + ")", count);
                    pieChart.getData().add(slice);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return pieChart;
    }
    
    /**
     * Crée la ligne du bas avec tableaux et activité
     */
    private HBox createBottomRow() {
        HBox row = new HBox(20);
        
        // Table des adhérents récents (simplified)
        VBox tableContainer = createTableCard("Adhérents Récents");
        HBox.setHgrow(tableContainer, Priority.ALWAYS);
        
        // Card promotionnelle (simplified)
        VBox promoCard = createPromoCard();
        promoCard.setPrefWidth(350);
        
        row.getChildren().addAll(tableContainer, promoCard);
        
        return row;
    }
    
    /**
     * Crée une carte avec tableau (100% width, Row 4)
     */
    private VBox createTableCard(String title) {
        VBox container = new VBox(0); // Pas d'espacement entre les éléments pour un contrôle précis
        container.setPadding(new Insets(20));
        container.getStyleClass().add("table-card");
        container.setPrefHeight(500);
        container.setMaxHeight(500);
        container.setMinHeight(500);
        
        // Titre avec menu (inspiré du design moderne)
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getStyleClass().add("table-header");
        HBox.setHgrow(titleRow, Priority.ALWAYS);
        titleRow.setPadding(new Insets(0, 0, 16, 0)); // Espacement en bas seulement
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        
        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        
        titleRow.getChildren().addAll(titleLabel, titleSpacer);
        
        // En-têtes de colonnes avec design moderne (FIXE - en dehors du ScrollPane)
        // Structure identique aux lignes pour un alignement parfait
        HBox headerRow = new HBox(24);
        headerRow.setPadding(new Insets(16, 20, 16, 20));
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.getStyleClass().add("table-column-headers");
        
        // Colonne Adhérent - Structure identique à la cellule adherentCell des lignes
        HBox headerAdherentCell = new HBox(12); // Même spacing que dans les lignes
        headerAdherentCell.setAlignment(Pos.CENTER_LEFT);
        headerAdherentCell.getStyleClass().add("table-header-cell");
        // Espaceur pour correspondre exactement à l'avatar (Circle radius 24 = diamètre 48px)
        Region avatarSpacer = new Region();
        avatarSpacer.setPrefWidth(48);
        avatarSpacer.setMinWidth(48);
        avatarSpacer.setMaxWidth(48);
        // Label pour correspondre au VBox nameInfo des lignes
        Label headerAdherent = new Label("Adhérent");
        headerAdherent.getStyleClass().add("table-header-label");
        headerAdherentCell.getChildren().addAll(avatarSpacer, headerAdherent);
        headerAdherentCell.setPrefWidth(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(headerAdherentCell, Priority.SOMETIMES);
        headerAdherentCell.setMinWidth(200);
        
        // Colonne Pack - Structure identique aux lignes
        Label headerPack = new Label("Pack");
        headerPack.getStyleClass().add("table-header-label");
        headerPack.setPrefWidth(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(headerPack, Priority.SOMETIMES);
        headerPack.setMinWidth(120);
        
        // Colonne Statut - Structure identique aux lignes
        Label headerStatut = new Label("Statut");
        headerStatut.getStyleClass().add("table-header-label");
        headerStatut.setPrefWidth(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(headerStatut, Priority.SOMETIMES);
        headerStatut.setMinWidth(120);
        
        // Colonne Expiration - Structure identique aux lignes
        Label headerExpiration = new Label("Expiration");
        headerExpiration.getStyleClass().add("table-header-label");
        headerExpiration.setPrefWidth(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(headerExpiration, Priority.SOMETIMES);
        headerExpiration.setMinWidth(120);
        
        headerRow.getChildren().addAll(headerAdherentCell, headerPack, headerStatut, headerExpiration);
        
        // Table avec ScrollPane pour le BODY uniquement (header fixe en dehors)
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false); // Important : ne pas ajuster à la hauteur du contenu
        scrollPane.getStyleClass().add("table-scroll");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVvalue(0); // Commencer en haut
        scrollPane.setMinHeight(200); // Hauteur minimale pour garantir le scroll
        
        // VBox pour le body scrollable (sans le header) - padding en bas pour voir le dernier élément
        VBox tableBody = new VBox(0);
        tableBody.setPadding(new Insets(0, 0, 8, 0)); // Petit padding en bas pour voir le dernier élément
        tableBody.getStyleClass().add("table-body");
        
        try {
            List<Adherent> recentAdherents = adherentDAO.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getDateInscription() == null) return 1;
                    if (b.getDateInscription() == null) return -1;
                    return b.getDateInscription().compareTo(a.getDateInscription());
                })
                .limit(50) // Augmenter la limite pour permettre le scroll
                .toList();
            
            for (Adherent adherent : recentAdherents) {
                HBox row = new HBox(24);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(16, 20, 16, 20));
                // Tous les styles sont maintenant dans le CSS
                row.getStyleClass().add("table-row");
                
                // Avatar + Nom + CIN (40% de largeur)
                HBox adherentCell = new HBox(12);
                adherentCell.setAlignment(Pos.CENTER_LEFT);
                Circle avatar = new Circle(24);
                avatar.setFill(Color.web("#3B82F6"));
                VBox nameInfo = new VBox(4);
                nameInfo.setMinWidth(0);
                nameInfo.setPrefWidth(Region.USE_COMPUTED_SIZE);
                nameInfo.setMaxWidth(Double.MAX_VALUE);
                Label nameLabel = new Label(adherent.getNomComplet());
                nameLabel.getStyleClass().add("table-row-name");
                nameLabel.setMinWidth(0);
                nameLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
                nameLabel.setMaxWidth(Double.MAX_VALUE);
                Label cinLabel = new Label(adherent.getCin() != null ? adherent.getCin() : "");
                cinLabel.getStyleClass().add("table-row-cin");
                cinLabel.setMinWidth(0);
                cinLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
                cinLabel.setMaxWidth(Double.MAX_VALUE);
                nameInfo.getChildren().addAll(nameLabel, cinLabel);
                adherentCell.getChildren().addAll(avatar, nameInfo);
                adherentCell.setMinWidth(0);
                adherentCell.setPrefWidth(Region.USE_COMPUTED_SIZE);
                adherentCell.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(adherentCell, Priority.ALWAYS);
                adherentCell.setMinWidth(150); // Réduit de 200 à 150
                
                // Pack (20% de largeur)
                Label packLabel = new Label(adherent.getPack() != null ? adherent.getPack().getNom() : "N/A");
                packLabel.getStyleClass().add("table-row-pack");
                packLabel.setMinWidth(0);
                packLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
                packLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(packLabel, Priority.SOMETIMES);
                packLabel.setMinWidth(80); // Réduit de 120 à 80
                
                // Statut (20% de largeur)
                String statutText = "";
                String statutColor = "";
                if (adherent.getDateFin() != null && adherent.getDateFin().isBefore(LocalDate.now())) {
                    statutText = "Expiré";
                    statutColor = "#EF4444";
                } else if (adherent.getDateFin() != null && adherent.getDateFin().isBefore(LocalDate.now().plusDays(7))) {
                    statutText = "Expire bientôt";
                    statutColor = "#FFB020";
                } else {
                    statutText = "Actif";
                    statutColor = "#00E676";
                }
                Label statutLabel = new Label(statutText);
                statutLabel.getStyleClass().add("table-row-status");
                // Ajouter la classe CSS selon le statut
                if (statutColor.equals("#EF4444")) {
                    statutLabel.getStyleClass().add("table-row-status-expired");
                } else if (statutColor.equals("#FFB020")) {
                    statutLabel.getStyleClass().add("table-row-status-expiring");
                } else {
                    statutLabel.getStyleClass().add("table-row-status-active");
                }
                statutLabel.setMinWidth(0);
                statutLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
                statutLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(statutLabel, Priority.SOMETIMES);
                statutLabel.setMinWidth(100); // Réduit de 120 à 100
                
                // Expiration (20% de largeur)
                Label expirationLabel = new Label(
                    adherent.getDateFin() != null ? 
                    adherent.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : 
                    "N/A"
                );
                expirationLabel.getStyleClass().add("table-row-expiration");
                expirationLabel.setMinWidth(0);
                expirationLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
                expirationLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(expirationLabel, Priority.SOMETIMES);
                expirationLabel.setMinWidth(100); // Réduit de 120 à 100
                
                row.getChildren().addAll(adherentCell, packLabel, statutLabel, expirationLabel);
                tableBody.getChildren().add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        scrollPane.setContent(tableBody);
        VBox.setVgrow(scrollPane, Priority.ALWAYS); // Prendre tout l'espace disponible après le titre et le header
        
        // Ajouter le header fixe et le body scrollable
        container.getChildren().addAll(titleRow, headerRow, scrollPane);
        
        return container;
    }
    
    /**
     * Crée une carte promotionnelle
     */
    private VBox createPromoCard() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        // Tous les styles sont maintenant dans le CSS
        container.getStyleClass().add("promo-card-container");
        container.setPrefHeight(300);
        
        Label iconLabel = new Label("⚡");
        iconLabel.getStyleClass().add("promo-card-icon");
        
        Label titleLabel = new Label("Pack Premium");
        titleLabel.getStyleClass().add("promo-card-title");
        
        Label priceLabel = new Label("500 DH / Mois");
        priceLabel.getStyleClass().add("promo-card-price");
        
        Label descLabel = new Label("Accès complet à toutes les installations + coach personnel");
        descLabel.getStyleClass().add("promo-card-desc");
        descLabel.setWrapText(true);
        
        Button btn = new Button("En savoir plus");
        btn.getStyleClass().add("btn-primary");
        
        container.getChildren().addAll(iconLabel, titleLabel, priceLabel, descLabel, btn);
        container.setAlignment(Pos.CENTER);
        
        return container;
    }
    
    /**
     * Crée la sidebar droite avec notifications et activité - Design selon image avec fond uniforme et lignes de séparation
     */
    private VBox createRightSidebar() {
        VBox sidebar = new VBox(0); // Pas d'espacement entre sections, utilisation de lignes de séparation
        sidebar.setPadding(new Insets(24, 20, 24, 20));
        sidebar.setPrefWidth(250); // Largeur fixe : 250px selon spécifications
        sidebar.getStyleClass().add("right-sidebar");
        sidebar.setPrefHeight(Double.MAX_VALUE); // Prendre 100% de la hauteur
        
        // Notifications
        VBox notificationsContainer = createNotificationPanel("Notifications");
        sidebar.getChildren().add(notificationsContainer);
        
        // Ligne de séparation moderne entre Notifications et Activities
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.getStyleClass().add("sidebar-separator");
        VBox.setMargin(separator, new Insets(20, 0, 20, 0)); // Espacement avant et après la ligne
        sidebar.getChildren().add(separator);
        
        // Activities
        VBox activityContainer = createActivityPanel("Activities");
        sidebar.getChildren().add(activityContainer);
        
        return sidebar;
    }
    
    /**
     * Crée le panneau de notifications - Design selon image avec fond transparent et lignes de séparation
     */
    private VBox createNotificationPanel(String title) {
        VBox container = new VBox(12); // Espacement entre titre et liste : 12px
        container.setPadding(new Insets(0)); // Pas de padding, géré par le parent
        container.getStyleClass().add("sidebar-panel");
        container.setPrefWidth(Region.USE_COMPUTED_SIZE); // Prendre la largeur disponible
        
        // Section Header
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("sidebar-panel-title");
        
        VBox notifications = new VBox(0); // Pas d'espacement vertical, utilisation de lignes de séparation
        notifications.getStyleClass().add("notifications-list");
        
        // ✅ CORRIGÉ : Initialiser le userId avant de charger les notifications
        initializeNotificationService();
        
        // Charger les notifications depuis la base de données
        try {
            List<com.example.demo.models.Notification> dbNotifications = 
                notificationService.getRecentNotifications(com.example.demo.utils.DashboardConstants.MAX_NOTIFICATIONS_DISPLAY);
            
            if (dbNotifications.isEmpty()) {
                // Si aucune notification, créer des notifications de démonstration
                dbNotifications = createDemoNotifications();
            }
            
            // Convertir les notifications en items UI
            List<HBox> notificationItems = new java.util.ArrayList<>();
            for (com.example.demo.models.Notification notif : dbNotifications) {
                String iconName = getIconForNotificationType(notif.getType());
                String timestamp = formatNotificationTimestamp(notif.getCreatedAt());
                // ✅ CORRIGÉ : Utiliser le titre (ou le message si le titre est vide)
                String displayText = (notif.getTitle() != null && !notif.getTitle().isEmpty()) 
                    ? notif.getTitle() 
                    : notif.getMessage();
                notificationItems.add(createNotificationItem(iconName, displayText, timestamp));
            }
            
            // Ajouter les items avec des lignes de séparation modernes
            for (int i = 0; i < notificationItems.size(); i++) {
                notifications.getChildren().add(notificationItems.get(i));
                // Ajouter une ligne de séparation après chaque item sauf le dernier
                if (i < notificationItems.size() - 1) {
                    Region itemSeparator = new Region();
                    itemSeparator.setPrefHeight(1);
                    itemSeparator.getStyleClass().add("separator");
                    VBox.setMargin(itemSeparator, new Insets(14, 0, 14, 0)); // Espacement autour de la ligne
                    notifications.getChildren().add(itemSeparator);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // En cas d'erreur, utiliser des notifications de démonstration
            List<HBox> notificationItems = List.of(
                createNotificationItem("icon-users", "Erreur de chargement", "Maintenant"),
                createNotificationItem("icon-alert", "Vérifiez la connexion", "Maintenant")
            );
            for (HBox item : notificationItems) {
                notifications.getChildren().add(item);
            }
        }
        
        container.getChildren().addAll(titleLabel, notifications);
        
        return container;
    }
    
    /**
     * Crée des notifications de démonstration quand aucune notification n'est disponible
     */
    private List<com.example.demo.models.Notification> createDemoNotifications() {
        List<com.example.demo.models.Notification> notifications = new java.util.ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        // Notification 1: Nouvel adhérent
        com.example.demo.models.Notification notif1 = new com.example.demo.models.Notification(
            1, 
            com.example.demo.utils.DashboardConstants.NOTIF_TYPE_NEW_PAYMENT,
            "Nouveau paiement",
            "Un nouveau paiement de 500 DH a été enregistré"
        );
        notif1.setCreatedAt(now.minusMinutes(5));
        notifications.add(notif1);
        
        // Notification 2: Abonnement expirant
        com.example.demo.models.Notification notif2 = new com.example.demo.models.Notification(
            1,
            com.example.demo.utils.DashboardConstants.NOTIF_TYPE_EXPIRING_SOON,
            "Abonnement expirant",
            "3 abonnements expirent dans les 7 prochains jours"
        );
        notif2.setCreatedAt(now.minusHours(2));
        notifications.add(notif2);
        
        // Notification 3: Nouveau paiement
        com.example.demo.models.Notification notif3 = new com.example.demo.models.Notification(
            1,
            com.example.demo.utils.DashboardConstants.NOTIF_TYPE_NEW_PAYMENT,
            "Paiement reçu",
            "Paiement de 300 DH reçu pour Ahmed Alaoui"
        );
        notif3.setCreatedAt(now.minusDays(1));
        notifications.add(notif3);
        
        return notifications;
    }
    
    /**
     * Obtient l'icône correspondant au type de notification
     */
    private String getIconForNotificationType(String type) {
        if (type == null) return "icon-bell";
        
        switch (type) {
            case "NEW_PAYMENT":
                return "icon-credit-card";
            case "EXPIRING_SOON":
            case "EXPIRED":
                return "icon-alert";
            case "WITHDRAWAL":
                return "icon-dollar-sign";
            case "MESSAGE":
                return "icon-message";
            default:
                return "icon-bell";
        }
    }
    
    /**
     * Formate le timestamp de la notification en texte lisible
     */
    private String formatNotificationTimestamp(java.time.LocalDateTime timestamp) {
        if (timestamp == null) return "Maintenant";
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(timestamp, now);
        
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        
        if (minutes < 1) {
            return "Maintenant";
        } else if (minutes < 60) {
            return minutes + " min";
        } else if (hours < 24) {
            return hours + " h";
        } else if (days < 7) {
            return days + " j";
        } else {
            return timestamp.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }
    
    /**
     * Crée un élément de notification - Design exact selon spécifications
     */
    private HBox createNotificationItem(String iconName, String message, String timestamp) {
        HBox item = new HBox(10); // Spacing entre icône et texte : 10px selon spécifications
        item.setAlignment(Pos.TOP_LEFT); // Alignement en haut selon spécifications
        item.setPadding(new Insets(8)); // Padding fixe pour éviter les mouvements au hover
        item.getStyleClass().add("notification-item");
        
        // Icône circulaire verte (badge) - 32px diameter selon spécifications
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(32, 32);
        iconContainer.setMinSize(32, 32);
        iconContainer.setMaxSize(32, 32);
        iconContainer.getStyleClass().add("notification-icon-container");
        
        // Charger l'icône SVG (blanc, 16px)
        Node iconNode = loadSVGIcon(iconName, 16, "#FFFFFF");
        iconContainer.getChildren().add(iconNode);
        
        // Contenu texte (VBox)
        VBox textContent = new VBox(3); // Spacing entre message et timestamp : 3px selon spécifications
        textContent.setAlignment(Pos.TOP_LEFT);
        textContent.getStyleClass().add("notification-text");
        HBox.setHgrow(textContent, Priority.ALWAYS); // Prendre l'espace disponible
        
        // Message principal
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("notification-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(180); // Max-width : 180px selon spécifications
        
        // Timestamp
        Label timestampLabel = new Label(timestamp);
        timestampLabel.getStyleClass().add("notification-timestamp");
        
        textContent.getChildren().addAll(messageLabel, timestampLabel);
        
        item.getChildren().addAll(iconContainer, textContent);
        
        return item;
    }
    
    /**
     * Crée le panneau d'activité - Design selon image avec fond transparent et lignes de séparation
     */
    private VBox createActivityPanel(String title) {
        VBox container = new VBox(12); // Espacement entre titre et liste : 12px
        container.setPadding(new Insets(0)); // Pas de padding, géré par le parent
        container.getStyleClass().add("sidebar-panel");
        container.setPrefWidth(Region.USE_COMPUTED_SIZE); // Prendre la largeur disponible
        
        // Section Header
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("sidebar-panel-title");
        
        VBox activities = new VBox(0); // Pas d'espacement vertical, utilisation de lignes de séparation
        activities.getStyleClass().add("activities-list");
        
        // Charger les activités depuis la base de données
        try {
            List<com.example.demo.models.Activity> dbActivities = 
                activityService.getRecentActivities(com.example.demo.utils.DashboardConstants.MAX_ACTIVITIES_DISPLAY);
            
            if (dbActivities.isEmpty()) {
                // Si aucune activité, créer des activités de démonstration
                dbActivities = createDemoActivities();
            }
            
            // Convertir les activités en items UI
            List<HBox> activityItems = new java.util.ArrayList<>();
            for (com.example.demo.models.Activity activity : dbActivities) {
                String iconName = getIconForActivityType(activity.getType());
                String timestamp = formatActivityTimestamp(activity.getCreatedAt());
                activityItems.add(createActivityItem(iconName, activity.getDescription(), timestamp));
            }
            
            // Ajouter les items avec des lignes de séparation modernes
            for (int i = 0; i < activityItems.size(); i++) {
                activities.getChildren().add(activityItems.get(i));
                // Ajouter une ligne de séparation après chaque item sauf le dernier
                if (i < activityItems.size() - 1) {
                    Region itemSeparator = new Region();
                    itemSeparator.setPrefHeight(1);
                    itemSeparator.getStyleClass().add("separator");
                    VBox.setMargin(itemSeparator, new Insets(14, 0, 14, 0)); // Espacement autour de la ligne
                    activities.getChildren().add(itemSeparator);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // En cas d'erreur, utiliser des activités de démonstration
            List<HBox> activityItems = List.of(
                createActivityItem("icon-edit", "Erreur de chargement", "Maintenant"),
                createActivityItem("icon-alert", "Vérifiez la connexion", "Maintenant")
            );
            for (HBox item : activityItems) {
                activities.getChildren().add(item);
            }
        }
        
        container.getChildren().addAll(titleLabel, activities);
        
        return container;
    }
    
    /**
     * Crée un élément d'activité - Design exact selon spécifications
     */
    private HBox createActivityItem(String iconName, String message, String timestamp) {
        HBox item = new HBox(10); // Spacing entre icône et texte : 10px selon spécifications
        item.setAlignment(Pos.TOP_LEFT); // Alignement en haut selon spécifications
        item.setPadding(new Insets(8)); // Padding fixe pour éviter les mouvements au hover
        item.getStyleClass().add("activity-item");
        
        // Avatar circulaire coloré - 32px diameter selon spécifications
        // Utiliser un gradient coloré pour les activities avec LinearGradient JavaFX
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(32, 32);
        iconContainer.setMinSize(32, 32);
        iconContainer.setMaxSize(32, 32);
        iconContainer.getStyleClass().add("activity-avatar");
        
        // Créer un cercle avec gradient coloré selon le type d'icône
        Circle avatarCircle = new Circle(16); // 32px diameter = 16px radius
        LinearGradient gradient = getActivityGradient(iconName);
        avatarCircle.setFill(gradient);
        
        // Charger l'icône SVG (blanc, 16px)
        Node iconNode = loadSVGIcon(iconName, 16, "#FFFFFF");
        iconContainer.getChildren().addAll(avatarCircle, iconNode);
        
        // Contenu texte (VBox) - identique aux notifications
        VBox textContent = new VBox(3); // Spacing entre message et timestamp : 3px selon spécifications
        textContent.setAlignment(Pos.TOP_LEFT);
        textContent.getStyleClass().add("activity-text");
        HBox.setHgrow(textContent, Priority.ALWAYS); // Prendre l'espace disponible
        
        // Message principal
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("activity-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(180); // Max-width : 180px selon spécifications
        
        // Timestamp
        Label timestampLabel = new Label(timestamp);
        timestampLabel.getStyleClass().add("activity-timestamp");
        
        textContent.getChildren().addAll(messageLabel, timestampLabel);
        
        item.getChildren().addAll(iconContainer, textContent);
        
        return item;
    }
    
    /**
     * Rafraîchit toutes les données du dashboard.
     * 
     * <p>Cette méthode recharge tous les composants du dashboard :
     * - KPI Cards
     * - Charts (Donut et Area)
     * - Mini Cards
     * - Table des adhérents
     * - Notifications et Activities panels</p>
     */
    /**
     * Vérifie et notifie les abonnements expirant bientôt (évite les doublons)
     */
    private void checkAndNotifyExpiringSubscriptions() {
        try {
            initializeNotificationService();
            
            List<com.example.demo.models.Adherent> expiringSoon = adherentDAO.findExpiringSoon();
            int count = expiringSoon.size();
            
            if (count > 0) {
                // Vérifier si une notification similaire existe déjà aujourd'hui
                com.example.demo.models.Utilisateur currentUser = LoginController.getCurrentUser();
                Integer userId = currentUser != null ? currentUser.getId() : 1;
                
                boolean alreadyNotified = notificationDAO.existsTodayByType(
                    userId, 
                    com.example.demo.utils.DashboardConstants.NOTIF_TYPE_EXPIRING_SOON
                );
                
                if (!alreadyNotified) {
                    notificationService.notifyExpiringSoon(count);
                }
            }
        } catch (Exception e) {
            logger.warning("Erreur lors de la vérification des abonnements expirant bientôt: " + e.getMessage());
        }
    }
    
    /**
     * Vérifie et notifie les abonnements expirés (évite les doublons)
     */
    private void checkAndNotifyExpiredSubscriptions() {
        try {
            initializeNotificationService();
            
            List<com.example.demo.models.Adherent> expired = adherentDAO.findExpired();
            int count = expired.size();
            
            if (count > 0) {
                // Vérifier si une notification similaire existe déjà aujourd'hui
                com.example.demo.models.Utilisateur currentUser = LoginController.getCurrentUser();
                Integer userId = currentUser != null ? currentUser.getId() : 1;
                
                boolean alreadyNotified = notificationDAO.existsTodayByType(
                    userId, 
                    com.example.demo.utils.DashboardConstants.NOTIF_TYPE_EXPIRED
                );
                
                if (!alreadyNotified) {
                    notificationService.notifyExpired(count);
                }
            }
        } catch (Exception e) {
            logger.warning("Erreur lors de la vérification des abonnements expirés: " + e.getMessage());
        }
    }

    private void refreshDashboard() {
        try {
            // Vérifications périodiques des abonnements
            checkAndNotifyExpiringSubscriptions();
            checkAndNotifyExpiredSubscriptions();
            
            // Mettre à jour les valeurs KPI dans les labels existants
            updateKPIValues();
            
            // Recharger les Charts Row (les charts doivent être recréés car ils sont dynamiques)
            if (chartsRow != null && content != null) {
                int chartsIndex = content.getChildren().indexOf(chartsRow);
                if (chartsIndex >= 0) {
                    content.getChildren().remove(chartsIndex);
                    chartsRow = createChartsRowWithMiniCards();
                    content.getChildren().add(chartsIndex, chartsRow);
                }
            }
            
            // Mettre à jour l'Area Chart dans le conteneur FXML
            if (areaChartContainer != null) {
                updateRevenueAreaChart();
            }
            
            // Recharger la Bottom Row (la table doit être recréée car elle est dynamique)
            if (bottomRow != null && content != null) {
                int bottomIndex = content.getChildren().indexOf(bottomRow);
                if (bottomIndex >= 0) {
                    content.getChildren().remove(bottomIndex);
                    bottomRow = createBottomRowWithTable();
                    content.getChildren().add(bottomIndex, bottomRow);
                }
            }
            
            // Recharger la Sidebar (Notifications & Activities)
            refreshSidebar();
            
            // Mettre à jour le badge de notifications
            updateNotificationBadge();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du rafraîchissement du dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Met à jour les valeurs KPI dans les labels existants
     */
    private void updateKPIValues() {
        try {
            // Card 1: Revenus du Mois
            double revenusMois = paiementDAO.getRevenusMois(LocalDate.now());
            double revenusMoisPrecedent = paiementDAO.getRevenusMois(LocalDate.now().minusMonths(1));
            double changeRevenus = revenusMoisPrecedent > 0 ? ((revenusMois - revenusMoisPrecedent) / revenusMoisPrecedent) * 100 : 0;
            
            if (kpiRevenuValue != null) {
                kpiRevenuValue.setText(String.format("%.0f DH", revenusMois));
            }
            if (kpiRevenuChange != null) {
                kpiRevenuChange.setText(String.format("%.1f%% vs mois dernier", changeRevenus));
            }
            
            // Card 2: Adhérents Actifs
            int adherentsActifs = adherentDAO.findAll().size();
            double changeAdherents = adherentDAO.getMonthlyGrowth(LocalDate.now());
            
            if (kpiAdherentsValue != null) {
                kpiAdherentsValue.setText(String.valueOf(adherentsActifs));
            }
            if (kpiAdherentsChange != null) {
                kpiAdherentsChange.setText(String.format("%s%.1f%% ce mois", changeAdherents >= 0 ? "+" : "", changeAdherents));
            }
            
            // Card 3: Taux d'Occupation
            double tauxOccupation = adherentDAO.getTauxOccupation();
            com.example.demo.dao.ObjectifDAO objectifDAO = new com.example.demo.dao.ObjectifDAO();
            com.example.demo.models.Objectif objectif = objectifDAO.findActiveByType(
                com.example.demo.utils.DashboardConstants.OBJECTIF_TYPE_TAUX_OCCUPATION
            );
            int objectifAdherents = objectif != null 
                ? objectif.getValeur().intValue() 
                : com.example.demo.utils.DashboardConstants.OBJECTIF_ADHERENTS_DEFAULT;
            
            if (kpiTauxValue != null) {
                kpiTauxValue.setText(String.format("%.0f%%", tauxOccupation));
            }
            if (kpiTauxGoal != null) {
                kpiTauxGoal.setText(String.format("Objectif: %d", objectifAdherents));
            }
            
            // Dessiner le gauge
            if (gaugeCanvas != null) {
                GraphicsContext gc = gaugeCanvas.getGraphicsContext2D();
                gc.clearRect(0, 0, gaugeCanvas.getWidth(), gaugeCanvas.getHeight());
                drawSemiCircularGaugeNew(gc, tauxOccupation, 65);
            }
            
            // Card 4: Nouveaux Abonnements
            // Compter les nouveaux abonnements du mois (adhérents inscrits ce mois)
            int nouveauxAbonnements = 0;
            try {
                LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
                LocalDate finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
                
                String sql = """
                    SELECT COUNT(*) as count FROM adherents 
                    WHERE actif = 1 
                    AND date_inscription >= ? 
                    AND date_inscription <= ?
                """;
                
                try (Connection conn = com.example.demo.utils.DatabaseManager.getInstance().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    
                    stmt.setString(1, debutMois.toString());
                    stmt.setString(2, finMois.toString());
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        nouveauxAbonnements = rs.getInt("count");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                nouveauxAbonnements = 0;
            }
            
            if (kpiNouveauxValue != null) {
                kpiNouveauxValue.setText(String.valueOf(nouveauxAbonnements));
            }
            if (kpiNouveauxChange != null) {
                kpiNouveauxChange.setText("Ce mois");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Classe interne pour représenter un item combiné (Notification ou Activity)
     */
    private static class CombinedItem {
        HBox item;
        java.time.LocalDateTime timestamp;
        
        CombinedItem(HBox item, java.time.LocalDateTime timestamp) {
            this.item = item;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Rafraîchit la sidebar (Notifications & Activities combinées et mélangées)
     */
    private void refreshSidebar() {
        // Initialiser le service de notifications
        initializeNotificationService();
        
        if (combinedList != null) {
            combinedList.getChildren().clear();
            try {
                // Récupérer les notifications
                List<com.example.demo.models.Notification> notifications = 
                    notificationService.getRecentNotifications(com.example.demo.utils.DashboardConstants.MAX_NOTIFICATIONS_DISPLAY);
                
                if (notifications.isEmpty()) {
                    notifications = createDemoNotifications();
                }
                
                // Récupérer les activités
                List<com.example.demo.models.Activity> activities = 
                    activityService.getRecentActivities(com.example.demo.utils.DashboardConstants.MAX_ACTIVITIES_DISPLAY);
                
                if (activities.isEmpty()) {
                    activities = createDemoActivities();
                }
                
                // Convertir les notifications en items UI avec timestamps
                List<CombinedItem> combinedItems = new java.util.ArrayList<>();
                
                for (com.example.demo.models.Notification notif : notifications) {
                    String iconName = getIconForNotificationType(notif.getType());
                    String timestamp = formatNotificationTimestamp(notif.getCreatedAt());
                    String displayText = (notif.getTitle() != null && !notif.getTitle().isEmpty()) 
                        ? notif.getTitle() 
                        : notif.getMessage();
                    HBox item = createNotificationItem(iconName, displayText, timestamp);
                    combinedItems.add(new CombinedItem(item, notif.getCreatedAt()));
                }
                
                // Convertir les activités en items UI avec timestamps
                for (com.example.demo.models.Activity activity : activities) {
                    String iconName = getIconForActivityType(activity.getType());
                    String timestamp = formatActivityTimestamp(activity.getCreatedAt());
                    HBox item = createActivityItem(iconName, activity.getDescription(), timestamp);
                    combinedItems.add(new CombinedItem(item, activity.getCreatedAt()));
                }
                
                // Trier par date (plus récent en premier)
                combinedItems.sort((a, b) -> b.timestamp.compareTo(a.timestamp));
                
                // Ajouter les items triés avec des lignes de séparation
                for (int i = 0; i < combinedItems.size(); i++) {
                    combinedList.getChildren().add(combinedItems.get(i).item);
                    // Ajouter une ligne de séparation après chaque item sauf le dernier
                    if (i < combinedItems.size() - 1) {
                        Region itemSeparator = new Region();
                        itemSeparator.setPrefHeight(1);
                        itemSeparator.getStyleClass().add("separator");
                        VBox.setMargin(itemSeparator, new Insets(14, 0, 14, 0));
                        combinedList.getChildren().add(itemSeparator);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Retourne l'icône correspondant au type de notification
     */
    private String getNotificationIcon(String type) {
        return switch (type) {
            case com.example.demo.utils.DashboardConstants.NOTIF_TYPE_NEW_PAYMENT -> "icon-dollar";
            case com.example.demo.utils.DashboardConstants.NOTIF_TYPE_EXPIRING_SOON -> "icon-alert";
            case com.example.demo.utils.DashboardConstants.NOTIF_TYPE_EXPIRED -> "icon-alert";
            default -> "icon-bell";
        };
    }
    
    /**
     * Retourne l'icône correspondant au type d'activité
     */
    private String getActivityIcon(String type) {
        return getIconForActivityType(type);
    }
    
    /**
     * Formate un timestamp en texte lisible
     */
    private String formatTimestamp(java.time.LocalDateTime timestamp) {
        return formatActivityTimestamp(timestamp);
    }
    
    /**
     * Charge l'état initial de la sidebar gauche depuis les préférences utilisateur.
     * 
     * @param root Le BorderPane principal contenant la sidebar
     */
    private void loadSidebarState(BorderPane root) {
        try {
            com.example.demo.dao.UserPreferencesDAO prefsDAO = new com.example.demo.dao.UserPreferencesDAO();
            com.example.demo.models.UserPreferences prefs = prefsDAO.getOrCreateDefault(1);
            
            Node leftSidebar = root.getLeft();
            if (leftSidebar != null && prefs.isSidebarCollapsed()) {
                // Si la sidebar doit être cachée, la cacher immédiatement (sans animation)
                leftSidebar.setVisible(false);
                leftSidebar.setManaged(false);
                double width = leftSidebar instanceof Region ? ((Region) leftSidebar).getPrefWidth() : 260;
                leftSidebar.setTranslateX(-width);
            }
        } catch (SQLException e) {
            // Ignorer l'erreur au démarrage
            e.printStackTrace();
        }
    }
    
    /**
     * Toggle la sidebar gauche (show/hide avec animation).
     * 
     * @param menuBtn Le bouton menu qui déclenche l'action
     */
    private void toggleLeftSidebar(Button menuBtn) {
        try {
            // Trouver le parent BorderPane (mainContainer)
            Node currentNode = menuBtn;
            BorderPane mainContainer = null;
            
            // Remonter dans la hiérarchie pour trouver le BorderPane principal
            while (currentNode != null) {
                Parent parent = currentNode.getParent();
                if (parent instanceof BorderPane) {
                    mainContainer = (BorderPane) parent;
                    break;
                }
                currentNode = parent instanceof Node ? (Node) parent : null;
            }
            
            if (mainContainer == null) {
                // Essayer de trouver via la scène
                if (menuBtn.getScene() != null && menuBtn.getScene().getRoot() instanceof BorderPane) {
                    mainContainer = (BorderPane) menuBtn.getScene().getRoot();
                }
            }
            
            if (mainContainer != null) {
                Node leftSidebar = mainContainer.getLeft();
                
                if (leftSidebar != null) {
                    // Récupérer l'état actuel depuis les préférences
                    com.example.demo.dao.UserPreferencesDAO prefsDAO = new com.example.demo.dao.UserPreferencesDAO();
                    com.example.demo.models.UserPreferences prefs = prefsDAO.getOrCreateDefault(1);
                    boolean isCollapsed = prefs.isSidebarCollapsed();
                    
                    // Toggle l'état
                    boolean newState = !isCollapsed;
                    
                    // Animer le toggle
                    if (newState) {
                        // Cacher la sidebar avec animation
                        hideSidebar(leftSidebar);
                    } else {
                        // Afficher la sidebar avec animation
                        showSidebar(leftSidebar);
                    }
                    
                    // Sauvegarder l'état
                    prefs.setSidebarCollapsed(newState);
                    prefsDAO.update(prefs);
                }
            } else {
                // Si on ne trouve pas le parent, juste sauvegarder l'état
                com.example.demo.dao.UserPreferencesDAO prefsDAO = new com.example.demo.dao.UserPreferencesDAO();
                com.example.demo.models.UserPreferences prefs = prefsDAO.getOrCreateDefault(1);
                prefs.toggleSidebar();
                prefsDAO.update(prefs);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Erreur lors du toggle de la sidebar: " + ex.getMessage());
        }
    }
    
    /**
     * Cache la sidebar avec une animation de translation.
     * 
     * @param sidebar La sidebar à cacher
     */
    private void hideSidebar(Node sidebar) {
        if (sidebar == null) return;
        
        // Obtenir la largeur de la sidebar (utiliser prefWidth si disponible, sinon bounds)
        double width = sidebar instanceof Region ? ((Region) sidebar).getPrefWidth() : sidebar.getBoundsInLocal().getWidth();
        if (width <= 0) {
            width = 260; // Largeur par défaut de la sidebar
        }
        
        // Animation de translation vers la gauche
        TranslateTransition transition = new TranslateTransition(
            Duration.millis(300), sidebar
        );
        transition.setFromX(0);
        transition.setToX(-width);
        
        transition.setOnFinished(e -> {
            sidebar.setVisible(false);
            sidebar.setManaged(false);
        });
        
        transition.play();
    }
    
    /**
     * Affiche la sidebar avec une animation de translation.
     * 
     * @param sidebar La sidebar à afficher
     */
    private void showSidebar(Node sidebar) {
        if (sidebar == null) return;
        
        // Obtenir la largeur de la sidebar (utiliser prefWidth si disponible, sinon bounds)
        double width = sidebar instanceof Region ? ((Region) sidebar).getPrefWidth() : sidebar.getBoundsInLocal().getWidth();
        if (width <= 0) {
            width = 260; // Largeur par défaut de la sidebar
        }
        
        // S'assurer que la sidebar est visible et gérée
        sidebar.setVisible(true);
        sidebar.setManaged(true);
        
        // Réinitialiser la translation avant l'animation
        sidebar.setTranslateX(-width);
        
        // Animation de translation depuis la gauche
        TranslateTransition transition = new TranslateTransition(
            Duration.millis(300), sidebar
        );
        transition.setFromX(-width);
        transition.setToX(0);
        
        transition.play();
    }
    
    /**
     * Applique un filtre temporel au dashboard.
     * 
     * @param filterType Type de filtre à appliquer
     * @param filterLabel Label à mettre à jour avec le nom du filtre
     */
    private void applyFilter(com.example.demo.utils.DateRangeFilter.FilterType filterType, Label filterLabel) {
        currentFilter = filterType;
        filterLabel.setText(com.example.demo.utils.DateRangeFilter.getShortLabel(filterType));
        
        // Rafraîchir le dashboard avec le nouveau filtre
        refreshDashboard();
    }
    
    /**
     * Retourne le nom de l'icône correspondant au type d'activité.
     * 
     * @param activityType Type d'activité
     * @return Nom de l'icône à utiliser
     */
    private String getIconForActivityType(String activityType) {
        return switch (activityType) {
            case com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_STYLE_CHANGED -> "icon-edit";
            case com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_PRODUCT_ADDED -> "icon-package";
            case com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_PRODUCT_ARCHIVED -> "icon-archive";
            case com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_PAGE_REMOVED -> "icon-file-x";
            case com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_ADHERENT_CREATED -> "icon-users";
            case com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_ADHERENT_UPDATED -> "icon-edit";
            case com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_PAYMENT_RECORDED -> "icon-dollar";
            case com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_PACK_CREATED -> "icon-package";
            case com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_PACK_UPDATED -> "icon-edit";
            default -> "icon-bar-chart";
        };
    }
    
    /**
     * Formate le timestamp d'une activité en texte lisible.
     * 
     * @param createdAt Date de création de l'activité
     * @return Texte formaté (ex: "12 hour ago", "20 Minutes ago")
     */
    private String formatActivityTimestamp(java.time.LocalDateTime createdAt) {
        if (createdAt == null) {
            return "Just now";
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long hoursAgo = java.time.Duration.between(createdAt, now).toHours();
        long minutesAgo = java.time.Duration.between(createdAt, now).toMinutes();
        
        if (minutesAgo < 1) {
            return "Just now";
        } else if (minutesAgo < 60) {
            return minutesAgo + " Minute" + (minutesAgo > 1 ? "s" : "") + " ago";
        } else if (hoursAgo < 24) {
            return hoursAgo + " hour" + (hoursAgo > 1 ? "s" : "") + " ago";
        } else {
            long daysAgo = java.time.Duration.between(createdAt, now).toDays();
            return daysAgo + " day" + (daysAgo > 1 ? "s" : "") + " ago";
        }
    }
    
    /**
     * Crée des activités de démonstration si aucune activité n'existe.
     * 
     * @return Liste d'activités de démonstration
     */
    private List<com.example.demo.models.Activity> createDemoActivities() {
        List<com.example.demo.models.Activity> demoActivities = new java.util.ArrayList<>();
        
        try {
            // Générer quelques activités de démonstration
            com.example.demo.models.Activity act1 = new com.example.demo.models.Activity(
                1, com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_STYLE_CHANGED,
                "Changed the style"
            );
            act1.setCreatedAt(java.time.LocalDateTime.now().minusHours(12));
            demoActivities.add(act1);
            
            com.example.demo.models.Activity act2 = new com.example.demo.models.Activity(
                1, com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_PRODUCT_ADDED,
                "177 New products added"
            );
            act2.setCreatedAt(java.time.LocalDateTime.now().minusMinutes(20));
            demoActivities.add(act2);
            
            com.example.demo.models.Activity act3 = new com.example.demo.models.Activity(
                1, com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_PRODUCT_ARCHIVED,
                "11 Products have been archived"
            );
            act3.setCreatedAt(java.time.LocalDateTime.now().minusHours(1));
            demoActivities.add(act3);
            
            com.example.demo.models.Activity act4 = new com.example.demo.models.Activity(
                1, com.example.demo.utils.DashboardConstants.ACTIVITY_TYPE_PAGE_REMOVED,
                "Page \"Tags\" has been removed"
            );
            act4.setCreatedAt(java.time.LocalDateTime.now().minusHours(3));
            demoActivities.add(act4);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return demoActivities;
    }
    
    /**
     * Retourne un LinearGradient pour les avatars d'activité selon le type d'icône
     */
    private LinearGradient getActivityGradient(String iconName) {
        Stop[] stops = switch (iconName) {
            case "icon-edit" -> new Stop[]{
                new Stop(0, Color.web("#667eea")), // Violet
                new Stop(1, Color.web("#764ba2"))  // Pourpre
            };
            case "icon-package" -> new Stop[]{
                new Stop(0, Color.web("#f093fb")), // Rose
                new Stop(1, Color.web("#f5576c"))  // Rouge
            };
            case "icon-archive" -> new Stop[]{
                new Stop(0, Color.web("#fa709a")), // Rose
                new Stop(1, Color.web("#fee140"))  // Jaune
            };
            case "icon-file-x" -> new Stop[]{
                new Stop(0, Color.web("#30cfd0")), // Cyan
                new Stop(1, Color.web("#330867"))  // Violet foncé
            };
            default -> new Stop[]{
                new Stop(0, Color.web("#10b981")), // Vert
                new Stop(1, Color.web("#059669"))  // Vert foncé
            };
        };
        
        return new LinearGradient(0, 0, 0, 1, true, null, stops);
    }
    
    /**
     * Met à jour le breadcrumb selon la page active
     */
    public void updateBreadcrumb(String action) {
        if (breadcrumbLabel == null) return;
        
        String breadcrumb = switch (action) {
            case "dashboard" -> "Dashboard / Overview";
            case "statistiques" -> "Dashboard / Statistiques";
            case "packs" -> "Gestion / Packs";
            case "adherents" -> "Gestion / Adhérents";
            case "paiements" -> "Gestion / Paiements";
            case "calendrier" -> "Gestion / Calendrier";
            case "settings" -> "Settings / Paramètres";
            case "help" -> "Settings / Aide";
            default -> "Dashboard / Overview";
        };
        
        breadcrumbLabel.setText(breadcrumb);
    }
    
    /**
     * Crée une mini-KPI card (pour les cards empilées)
     */
    private StackPane createMiniKPICard(String label, String value, String change, boolean positive) {
        StackPane cardContainer = new StackPane();
        cardContainer.setPrefWidth(260);
        cardContainer.setPrefHeight(130);
        cardContainer.setMinWidth(260);
        cardContainer.setMinHeight(130);
        cardContainer.setMaxWidth(260);
        cardContainer.setMaxHeight(130);
        
        Region cardBackground = new Region();
        cardBackground.getStyleClass().add("kpi-card");
        cardBackground.setPrefSize(260, 130);
        
        VBox card = new VBox(6);
        card.setPadding(new Insets(16));
        card.setAlignment(Pos.TOP_LEFT);
        
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("mini-kpi-card-label");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("mini-kpi-card-value");
        
        HBox changeContainer = new HBox(6);
        changeContainer.setAlignment(Pos.CENTER_LEFT);
        
        String iconName = positive ? "icon-trending-up" : "icon-trending-down";
        String iconColor = positive ? "#9EFF00" : "#EF4444";
        Node trendIcon = loadSVGIcon(iconName, 12, iconColor);
        
        Label changeLabel = new Label(change);
        // Utiliser les classes CSS selon le signe
        if (positive) {
            changeLabel.getStyleClass().add("kpi-change-label-positive");
        } else {
            changeLabel.getStyleClass().add("kpi-change-label-negative");
        }
        
        changeContainer.getChildren().addAll(trendIcon, changeLabel);
        card.getChildren().addAll(labelLabel, valueLabel, changeContainer);
        
        cardContainer.getChildren().addAll(cardBackground, card);
        
        return cardContainer;
    }
    
    /**
     * Crée une grande card avec donut chart pour la répartition des statuts d'adhérents
     */
    private VBox createStatusDistributionCard() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("area-chart-card");
        container.setPrefHeight(300);
        
        Label titleLabel = new Label("Répartition Statut Adhérents");
        titleLabel.getStyleClass().add("area-chart-title");
        
        HBox content = new HBox(24);
        content.setAlignment(Pos.CENTER_LEFT);
        
        // Donut chart
        PieChart pieChart = new PieChart();
        pieChart.setLabelsVisible(false);
        pieChart.setLegendVisible(false);
        pieChart.setPrefSize(150, 150);
        
        try {
            List<Adherent> allAdherents = adherentDAO.findAll();
            int actifs = (int) allAdherents.stream()
                .filter(a -> a.getDateFin() != null && a.getDateFin().isAfter(LocalDate.now()))
                .count();
            int expires = adherentDAO.findExpired().size();
            int expirentBientot = adherentDAO.findExpiringSoon().size();
            
            if (actifs > 0) {
                PieChart.Data actifsData = new PieChart.Data("Actifs", actifs);
                pieChart.getData().add(actifsData);
            }
            if (expires > 0) {
                PieChart.Data expiresData = new PieChart.Data("Expirés", expires);
                pieChart.getData().add(expiresData);
            }
            if (expirentBientot > 0) {
                PieChart.Data expirentData = new PieChart.Data("Expirent bientôt", expirentBientot);
                pieChart.getData().add(expirentData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Liste des catégories à droite
        VBox categoriesList = new VBox(12);
        categoriesList.setAlignment(Pos.TOP_LEFT);
        
        try {
            List<Adherent> allAdherents = adherentDAO.findAll();
            int actifs = (int) allAdherents.stream()
                .filter(a -> a.getDateFin() != null && a.getDateFin().isAfter(LocalDate.now()))
                .count();
            int expires = adherentDAO.findExpired().size();
            int expirentBientot = adherentDAO.findExpiringSoon().size();
            
            categoriesList.getChildren().add(createCategoryItem("Actifs", actifs, "#9EFF00"));
            categoriesList.getChildren().add(createCategoryItem("Expirés", expires, "#EF4444"));
            categoriesList.getChildren().add(createCategoryItem("Expirent bientôt", expirentBientot, "#F59E0B"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        content.getChildren().addAll(pieChart, categoriesList);
        container.getChildren().addAll(titleLabel, content);
        
        return container;
    }
    
    /**
     * Crée un item de catégorie pour la liste
     */
    private HBox createCategoryItem(String label, int value, String color) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Circle dot = new Circle(6);
        dot.setFill(Color.web(color));
        
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("category-item-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.getStyleClass().add("category-item-value");
        
        item.getChildren().addAll(dot, labelLabel, spacer, valueLabel);
        
        return item;
    }
    
    /**
     * Met à jour le graphique Area Chart dans le conteneur FXML
     */
    private void updateRevenueAreaChart() {
        if (areaChartContainer == null) return;
        
        // Vider le conteneur s'il contient déjà un graphique
        areaChartContainer.getChildren().clear();
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("");
        
        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setTitle("");
        areaChart.setLegendVisible(false);
        areaChart.setAnimated(true);
        // Configurer la hauteur du graphique pour qu'il reste dans son container
        // Card: 300px - padding (40px) - spacing (12px) - label (~25px) = ~223px disponible
        // On utilise 220px pour laisser une petite marge
        areaChart.setMinHeight(220);
        areaChart.setPrefHeight(220);
        areaChart.setMaxHeight(220);
        // S'assurer que le graphique prend toute la largeur disponible
        areaChart.setMinWidth(0);
        areaChart.setPrefWidth(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        areaChart.setMaxWidth(Double.MAX_VALUE);
        // S'assurer que le graphique est centré dans son container
        StackPane.setAlignment(areaChart, javafx.geometry.Pos.CENTER);
        areaChart.getStyleClass().add("chart");
        
        // Configurer le conteneur avec une hauteur fixe correspondante
        areaChartContainer.setMinHeight(220);
        areaChartContainer.setPrefHeight(220);
        areaChartContainer.setMaxHeight(220);
        // S'assurer que le conteneur prend toute la largeur disponible
        areaChartContainer.setMinWidth(0);
        areaChartContainer.setPrefWidth(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        areaChartContainer.setMaxWidth(Double.MAX_VALUE);
        
        // Style pour les axes - utiliser classes CSS
        xAxis.getStyleClass().add("chart-axis");
        yAxis.getStyleClass().add("chart-axis");
        
        // Appliquer le CSS pour les styles du graphique
        try {
            String cssPath = getClass().getResource("/css/dashboard-cards.css").toExternalForm();
            areaChart.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Impossible de charger le CSS pour le graphique: " + e.getMessage());
        }
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        try {
            // Utiliser getRevenusParMois() pour récupérer les revenus des 6 derniers mois en une seule requête
            int nombreMois = com.example.demo.utils.DashboardConstants.MONTHS_REVENUE_CHART;
            List<com.example.demo.models.MonthlyRevenue> revenusMensuels = paiementDAO.getRevenusParMois(nombreMois);
            
            double totalRevenus = 0;
            for (com.example.demo.models.MonthlyRevenue monthlyRevenue : revenusMensuels) {
                double montant = monthlyRevenue.getMontant() != null ? monthlyRevenue.getMontant() : 0.0;
                totalRevenus += montant;
                String monthName = monthlyRevenue.getMoisFormatted();
                series.getData().add(new XYChart.Data<>(monthName, montant));
            }
            
            // Si toutes les données sont à zéro, ajouter des données de test pour visualiser le design
            if (totalRevenus == 0) {
                series.getData().clear();
                // Données de test avec une tendance ascendante pour visualiser le design
                String[] mois = {"juil.", "août", "sept.", "oct.", "nov.", "déc."};
                double[] donneesTest = {15000, 22000, 18000, 28000, 35000, 42000}; // Données de test
                
                for (int i = 0; i < mois.length; i++) {
                    series.getData().add(new XYChart.Data<>(mois[i], donneesTest[i]));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // En cas d'erreur, utiliser des données de test
            series.getData().clear();
            String[] mois = {"juil.", "août", "sept.", "oct.", "nov.", "déc."};
            double[] donneesTest = {15000, 22000, 18000, 28000, 35000, 42000};
            for (int i = 0; i < mois.length; i++) {
                series.getData().add(new XYChart.Data<>(mois[i], donneesTest[i]));
            }
        }
        
        areaChart.getData().add(series);
        areaChartContainer.getChildren().add(areaChart);
    }
    
    /**
     * Crée une card avec area chart pour la tendance des revenus (Row 3 - 100% width)
     * NOTE: Cette méthode est conservée pour compatibilité mais n'est plus utilisée
     * car le graphique est maintenant géré directement dans areaChartContainer via updateRevenueAreaChart()
     */
    private VBox createRevenueAreaChartCard() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("area-chart-card");
        container.setPrefHeight(300);
        
        Label titleLabel = new Label("Évolution des Revenus");
        titleLabel.getStyleClass().add("card-title");
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("");
        
        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setTitle("");
        areaChart.setLegendVisible(false);
        areaChart.setAnimated(true);
        areaChart.setPrefHeight(220);
        areaChart.getStyleClass().add("chart");
        
        // Container pour le graphique avec style CSS
        StackPane chartContainer = new StackPane();
        chartContainer.setPrefHeight(220);
        chartContainer.getStyleClass().add("chart-container");
        chartContainer.getChildren().add(areaChart);
        
        // Style pour les axes - utiliser classes CSS
        xAxis.getStyleClass().add("chart-axis");
        yAxis.getStyleClass().add("chart-axis");
        
        // Appliquer le CSS pour les styles du graphique
        try {
            String cssPath = getClass().getResource("/css/dashboard-cards.css").toExternalForm();
            areaChart.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Impossible de charger le CSS pour le graphique: " + e.getMessage());
        }
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        try {
            // Utiliser getRevenusParMois() pour récupérer les revenus des 6 derniers mois en une seule requête
            int nombreMois = com.example.demo.utils.DashboardConstants.MONTHS_REVENUE_CHART;
            List<com.example.demo.models.MonthlyRevenue> revenusMensuels = paiementDAO.getRevenusParMois(nombreMois);
            
            double totalRevenus = 0;
            for (com.example.demo.models.MonthlyRevenue monthlyRevenue : revenusMensuels) {
                double montant = monthlyRevenue.getMontant() != null ? monthlyRevenue.getMontant() : 0.0;
                totalRevenus += montant;
                String monthName = monthlyRevenue.getMoisFormatted();
                series.getData().add(new XYChart.Data<>(monthName, montant));
            }
            
            // Si toutes les données sont à zéro, ajouter des données de test pour visualiser le design
            if (totalRevenus == 0) {
                series.getData().clear();
                // Données de test avec une tendance ascendante pour visualiser le design
                String[] mois = {"juil.", "août", "sept.", "oct.", "nov.", "déc."};
                double[] donneesTest = {15000, 22000, 18000, 28000, 35000, 42000}; // Données de test
                
                for (int i = 0; i < mois.length; i++) {
                    series.getData().add(new XYChart.Data<>(mois[i], donneesTest[i]));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // En cas d'erreur, utiliser des données de test
            series.getData().clear();
            String[] mois = {"juil.", "août", "sept.", "oct.", "nov.", "déc."};
            double[] donneesTest = {15000, 22000, 18000, 28000, 35000, 42000};
            for (int i = 0; i < mois.length; i++) {
                series.getData().add(new XYChart.Data<>(mois[i], donneesTest[i]));
            }
        }
        
        areaChart.getData().add(series);
        // areaChart est déjà ajouté au chartContainer à la ligne 3473
        container.getChildren().addAll(titleLabel, chartContainer);
        
        return container;
    }
}
