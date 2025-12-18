package com.example.demo.controllers;

import com.example.demo.utils.AnimationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import com.example.demo.utils.SvgIcons;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur principal de l'application avec navigation sidebar premium
 */
public class MainController {
    @FXML
    private BorderPane mainContainer;
    @FXML
    private VBox sidebar;
    @FXML
    private Label welcomeLabel;

    private PackManagementController packController;
    private AdherentManagementController adherentController;
    private PaiementManagementController paiementController;
    private DashboardController dashboardController;
    private CalendrierController calendrierController;
    private StatistiquesController statistiquesController;
    
    private Button activeButton;
    private Map<String, Button> menuButtons = new HashMap<>();

    /**
     * Charge la vue principale
     */
    public Parent getView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setController(this);
            return loader.load();
        } catch (Exception e) {
            // Si le FXML n'existe pas, créer une vue basique
            return createBasicMainView();
        }
    }

    private Parent createBasicMainView() {
        BorderPane root = new BorderPane();
        root.setPrefSize(1280, 720);
        root.getStyleClass().add("root");

        // Créer la sidebar premium
        VBox sidebar = createPremiumSidebar();
        root.setLeft(sidebar);

        // Zone de contenu principale
        this.mainContainer = root;
        showDashboard();

        return root;
    }
    
    /**
     * Crée la sidebar premium avec tous les éléments demandés
     */
    private VBox createPremiumSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(260);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.getStyleClass().add("sidebar");
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #0A0D12, #070A0E);");

        // 1. LOGO & BRANDING (Haut)
        HBox logoSection = createLogoSection();
        sidebar.getChildren().add(logoSection);
        
        // Espacement (24px entre sections)
        Region spacer1 = new Region();
        spacer1.setPrefHeight(24);
        sidebar.getChildren().add(spacer1);

        // 2. USER CARD
        VBox userCard = createUserCard();
        sidebar.getChildren().add(userCard);
        
        // Espacement (24px entre sections)
        Region spacer2 = new Region();
        spacer2.setPrefHeight(24);
        sidebar.getChildren().add(spacer2);

        // 3. SEARCH BAR
        HBox searchBar = createSearchBar();
        sidebar.getChildren().add(searchBar);
        
        // Espacement (24px entre sections)
        Region spacer3 = new Region();
        spacer3.setPrefHeight(24);
        sidebar.getChildren().add(spacer3);

        // 4. DASHBOARDS SECTION
        Label dashboardsLabel = createSectionTitle("DASHBOARDS");
        sidebar.getChildren().add(dashboardsLabel);
        
        Button dashboardBtn = createMenuItem("Dashboard", "mdi-view-dashboard", "dashboard");
        Button statistiquesBtn = createMenuItem("Statistiques", "mdi-chart-line", "statistiques");
        
        sidebar.getChildren().addAll(dashboardBtn, statistiquesBtn);
        
        // Espacement (24px entre sections)
        Region spacer4 = new Region();
        spacer4.setPrefHeight(24);
        sidebar.getChildren().add(spacer4);

        // 5. GESTION SECTION
        Label gestionLabel = createSectionTitle("GESTION");
        sidebar.getChildren().add(gestionLabel);
        
        Button packsBtn = createMenuItem("Packs", "mdi-package-variant", "packs");
        Button adherentsBtn = createMenuItem("Adhérents", "mdi-account-group", "adherents");
        Button paiementsBtn = createMenuItem("Paiements", "mdi-credit-card", "paiements");
        Button calendrierBtn = createMenuItem("Calendrier", "mdi-calendar", "calendrier");
        
        sidebar.getChildren().addAll(packsBtn, adherentsBtn, paiementsBtn, calendrierBtn);
        
        // Espacement (24px entre sections)
        Region spacer5 = new Region();
        spacer5.setPrefHeight(24);
        sidebar.getChildren().add(spacer5);

        // 6. SETTINGS SECTION
        Label settingsLabel = createSectionTitle("SETTINGS");
        sidebar.getChildren().add(settingsLabel);
        
        Button settingsBtn = createMenuItem("Paramètres", "mdi-cog", "settings");
        Button helpBtn = createMenuItem("Aide", "mdi-help-circle", "help");
        
        sidebar.getChildren().addAll(settingsBtn, helpBtn);
        
        // Spacer pour pousser le footer en bas
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
        sidebar.getChildren().add(bottomSpacer);

        // 7. FOOTER (Logo/Version)
        HBox footer = createFooter();
        sidebar.getChildren().add(footer);

        // Animer la sidebar
        AnimationUtils.slideInLeft(sidebar);

        return sidebar;
    }
    
    /**
     * Crée la section logo et branding
     */
    private HBox createLogoSection() {
        HBox logoSection = new HBox(12);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        
        // Icône/Logo
        Label logoIcon = new Label("🏋️");
        logoIcon.setStyle("-fx-font-size: 24px;");
        
        VBox logoText = new VBox(2);
        Label appName = new Label("GYM");
        appName.getStyleClass().add("sidebar-logo-title");
        
        Label appSubtitle = new Label("Management");
        appSubtitle.getStyleClass().add("sidebar-logo-subtitle");
        
        logoText.getChildren().addAll(appName, appSubtitle);
        
        logoSection.getChildren().addAll(logoIcon, logoText);
        
        return logoSection;
    }
    
    /**
     * Crée la user card avec avatar et nom
     */
    private VBox createUserCard() {
        VBox userCard = new VBox(12);
        userCard.setPadding(new Insets(16));
        userCard.getStyleClass().add("user-card");
        
        HBox userInfo = new HBox(12);
        userInfo.setAlignment(Pos.CENTER_LEFT);
        
        // Avatar circulaire (32x32px)
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(32, 32);
        Circle avatarCircle = new Circle(16);
        avatarCircle.setFill(Color.web("#9EFF00"));
        avatarCircle.setOpacity(0.2);
        
        Circle avatarBorder = new Circle(16);
        avatarBorder.setFill(Color.TRANSPARENT);
        avatarBorder.setStroke(Color.web("#9EFF00"));
        avatarBorder.setStrokeWidth(2);
        
        Label avatarText = new Label("GH");
        avatarText.setStyle("-fx-text-fill: #9EFF00; -fx-font-weight: 700; -fx-font-size: 12px;");
        
        avatarContainer.getChildren().addAll(avatarCircle, avatarBorder, avatarText);
        
        // Nom utilisateur
        VBox userNameBox = new VBox(2);
        Label userName = new Label("Guy Hawkins");
        userName.getStyleClass().add("user-name");
        
        Label userRole = new Label("Manager");
        userRole.getStyleClass().add("user-role");
        
        userNameBox.getChildren().addAll(userName, userRole);
        
        // Flèche dropdown avec animation - SVG
        Node dropdownIcon = loadSVGIcon("icon-chevron-down", 14);
        dropdownIcon.getStyleClass().add("user-dropdown");
        dropdownIcon.setOnMouseClicked(e -> {
            // Toggle dropdown menu avec rotation
            if (dropdownIcon.getRotate() == 180) {
                dropdownIcon.setRotate(0);
            } else {
                dropdownIcon.setRotate(180);
            }
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        userInfo.getChildren().addAll(avatarContainer, userNameBox, spacer, dropdownIcon);
        userCard.getChildren().add(userInfo);
        
        return userCard;
    }
    
    /**
     * Crée la barre de recherche
     */
    private HBox createSearchBar() {
        HBox searchBarContainer = new HBox(8);
        searchBarContainer.setAlignment(Pos.CENTER_LEFT);
        searchBarContainer.getStyleClass().add("search-bar-container");
        
        // Icône de recherche (14×14px) - SVG
        Node searchIcon = loadSVGIcon("icon-search", 14);
        searchIcon.getStyleClass().add("search-icon");
        
        // Champ de recherche
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        // Raccourci clavier
        Label shortcutLabel = new Label("⌘K");
        shortcutLabel.getStyleClass().add("search-shortcut");
        
        searchBarContainer.getChildren().addAll(searchIcon, searchField, shortcutLabel);
        
        return searchBarContainer;
    }
    
    /**
     * Crée un titre de section
     */
    private Label createSectionTitle(String text) {
        Label sectionTitle = new Label(text);
        sectionTitle.getStyleClass().add("sidebar-section-title");
        return sectionTitle;
    }
    
    /**
     * Crée un item de menu avec icône
     */
    private Button createMenuItem(String text, String iconCode, String action) {
        Button menuItem = new Button();
        menuItem.setPrefWidth(228);
        menuItem.setPrefHeight(44);
        menuItem.setAlignment(Pos.CENTER_LEFT);
        menuItem.setContentDisplay(ContentDisplay.LEFT);
        menuItem.setPadding(new Insets(0, 16, 0, 16));
        menuItem.getStyleClass().add("sidebar-button");
        
        // Icône SVG (16x16px)
        Node iconView = loadSVGIcon(getIconFileNameForAction(action), 16);
        iconView.getStyleClass().add("menu-icon");
        
        // Texte (14px)
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("menu-text");
        
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().addAll(iconView, textLabel);
        
        menuItem.setGraphic(content);
        
        // Action
        menuItem.setOnAction(e -> handleMenuAction(action, menuItem));
        
        // Stocker dans la map
        menuButtons.put(action, menuItem);
        
        return menuItem;
    }
    
    /**
     * Charge une icône SVG en utilisant SVGPath natif JavaFX (100% transparent, sans halo)
     * Solution PRO : remplace WebView par SVGPath pour un rendu parfait
     * 
     * @param iconName Nom de l'icône (ex: "icon-dashboard")
     * @param size Taille de l'icône en pixels
     * @param color Couleur optionnelle (par défaut: #9AA4B2)
     * @return Node contenant l'icône SVGPath
     */
    private Node loadSVGIcon(String iconName, double size) {
        return loadSVGIcon(iconName, size, "#9AA4B2");
    }
    
    /**
     * Charge une icône SVG avec une couleur spécifique
     */
    private Node loadSVGIcon(String iconName, double size, String color) {
        try {
            // Récupérer le path SVG depuis la classe SvgIcons
            String svgPath = getSvgPathForIcon(iconName);
            
            if (svgPath != null && !svgPath.isEmpty()) {
                // Créer un SVGPath natif JavaFX
                SVGPath svgPathNode = new SVGPath();
                svgPathNode.setContent(svgPath);
                
                // Configuration du style (stroke pour les icônes outline)
                svgPathNode.setFill(null); // Pas de fill pour les icônes outline
                svgPathNode.setStroke(Color.web(color)); // Couleur personnalisée
                svgPathNode.setStrokeWidth(2.0);
                svgPathNode.setStrokeLineCap(StrokeLineCap.ROUND);
                svgPathNode.setStrokeLineJoin(StrokeLineJoin.ROUND);
                
                // Calculer le scale pour obtenir la taille désirée
                // Les SVG sont conçus pour un viewBox de 24x24
                double scale = size / 24.0;
                svgPathNode.setScaleX(scale);
                svgPathNode.setScaleY(scale);
                
                // Centrer l'icône dans un container de taille fixe
                StackPane container = new StackPane();
                container.setPrefSize(size, size);
                container.setMaxSize(size, size);
                container.setMinSize(size, size);
                container.setAlignment(javafx.geometry.Pos.CENTER);
                container.getChildren().add(svgPathNode);
                
                // 100% transparent - aucun background possible
                container.setStyle("-fx-background-color: transparent;");
                
                // Stocker la référence au SVGPath pour pouvoir changer la couleur plus tard
                container.setUserData(svgPathNode);
                
                return container;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'icône " + iconName + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        // Fallback : emoji si le SVG ne peut pas être chargé
        return createEmojiIcon(getEmojiForAction(iconName), size);
    }
    
    /**
     * Change la couleur d'une icône SVGPath
     */
    private void setIconColor(Node iconContainer, String color) {
        if (iconContainer.getUserData() instanceof SVGPath) {
            SVGPath svgPath = (SVGPath) iconContainer.getUserData();
            svgPath.setStroke(Color.web(color));
        }
    }
    
    /**
     * Retourne le path SVG correspondant au nom de l'icône
     */
    private String getSvgPathForIcon(String iconName) {
        return switch (iconName) {
            case "icon-dashboard" -> SvgIcons.DASHBOARD;
            case "icon-stats" -> SvgIcons.STATS;
            case "icon-users" -> SvgIcons.USERS;
            case "icon-pack" -> SvgIcons.PACK;
            case "icon-payment" -> SvgIcons.PAYMENT;
            case "icon-calendar" -> SvgIcons.CALENDAR;
            case "icon-settings" -> SvgIcons.SETTINGS;
            case "icon-help" -> SvgIcons.HELP;
            case "icon-search" -> SvgIcons.SEARCH;
            case "icon-chevron-down" -> SvgIcons.CHEVRON_DOWN;
            default -> null;
        };
    }
    
    /**
     * Crée une icône emoji comme fallback
     */
    private Node createEmojiIcon(String emoji, double size) {
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: " + size + "px; -fx-text-fill: #9AA4B2;");
        emojiLabel.setPrefSize(size, size);
        emojiLabel.setMinSize(size, size);
        emojiLabel.setMaxSize(size, size);
        return emojiLabel;
    }
    
    /**
     * Retourne le nom du fichier SVG pour une action
     */
    private String getIconFileNameForAction(String action) {
        switch (action) {
            case "dashboard": return "icon-dashboard";
            case "statistiques": return "icon-stats";
            case "packs": return "icon-pack";
            case "adherents": return "icon-users";
            case "paiements": return "icon-payment";
            case "calendrier": return "icon-calendar";
            case "settings": return "icon-settings";
            case "help": return "icon-help";
            case "search": return "icon-search";
            case "dropdown": return "icon-chevron-down";
            default: return "icon-dashboard";
        }
    }
    
    /**
     * Retourne l'emoji de fallback pour un nom de fichier SVG
     */
    private String getEmojiForAction(String iconName) {
        // Mapper les noms de fichiers SVG vers les emojis
        switch (iconName) {
            case "icon-dashboard": return "📊";
            case "icon-stats": return "📈";
            case "icon-pack": return "📦";
            case "icon-users": return "👥";
            case "icon-payment": return "💳";
            case "icon-calendar": return "📅";
            case "icon-settings": return "⚙️";
            case "icon-help": return "❓";
            case "icon-search": return "🔍";
            case "icon-chevron-down": return "⌄";
            default: return "•";
        }
    }
    
    /**
     * Gère l'action du menu
     */
    private void handleMenuAction(String action, Button button) {
        // Désactiver l'ancien bouton actif
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-button-active");
            
            // Remettre l'icône en couleur normale (#9AA4B2)
            HBox oldContent = (HBox) activeButton.getGraphic();
            if (oldContent != null && oldContent.getChildren().size() > 0) {
                Node oldIcon = oldContent.getChildren().get(0);
                setIconColor(oldIcon, "#9AA4B2");
            }
        }
        
        // Activer le nouveau bouton
        activeButton = button;
        button.getStyleClass().add("sidebar-button-active");
        
        // Changer la couleur de l'icône active en noir (#0B0F14)
        HBox content = (HBox) button.getGraphic();
        if (content != null && content.getChildren().size() > 0) {
            Node icon = content.getChildren().get(0);
            setIconColor(icon, "#0B0F14");
        }
        
        // Exécuter l'action
        switch (action) {
            case "dashboard":
                showDashboard();
                break;
            case "statistiques":
                showStatistiques();
                break;
            case "packs":
                showPacksManagement();
                break;
            case "adherents":
                showAdherentsManagement();
                break;
            case "paiements":
                showPaiementsManagement();
                break;
            case "calendrier":
                showCalendrier();
                break;
            case "settings":
                // TODO: Implémenter les paramètres
                break;
            case "help":
                // TODO: Implémenter l'aide
                break;
        }
    }
    
    /**
     * Crée le footer
     */
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(16, 0, 0, 0));
        
        Label versionLabel = new Label("v1.0.0");
        versionLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        
        footer.getChildren().add(versionLabel);
        
        return footer;
    }

    @FXML
    private void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue dans le système de gestion");
        }
    }

    private void showDashboard() {
        if (dashboardController == null) {
            dashboardController = new DashboardController();
        }
        if (mainContainer != null) {
            mainContainer.setCenter(dashboardController.getView());
        }
    }

    private void showPacksManagement() {
        if (packController == null) {
            packController = new PackManagementController();
        }
        if (mainContainer != null) {
            mainContainer.setCenter(packController.getView());
        }
    }

    private void showAdherentsManagement() {
        if (adherentController == null) {
            adherentController = new AdherentManagementController();
        }
        if (mainContainer != null) {
            mainContainer.setCenter(adherentController.getView());
        }
    }

    private void showPaiementsManagement() {
        if (paiementController == null) {
            paiementController = new PaiementManagementController();
        }
        if (mainContainer != null) {
            mainContainer.setCenter(paiementController.getView());
        }
    }

    private void showCalendrier() {
        if (calendrierController == null) {
            calendrierController = new CalendrierController();
        }
        if (mainContainer != null) {
            mainContainer.setCenter(calendrierController.getView());
        }
    }

    private void showStatistiques() {
        if (statistiquesController == null) {
            statistiquesController = new StatistiquesController();
        }
        if (mainContainer != null) {
            mainContainer.setCenter(statistiquesController.getView());
        }
    }
}
