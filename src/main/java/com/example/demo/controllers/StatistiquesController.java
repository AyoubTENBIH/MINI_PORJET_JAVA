package com.example.demo.controllers;

import com.example.demo.dao.AdherentDAO;
import com.example.demo.dao.PackDAO;
import com.example.demo.dao.PaiementDAO;
import com.example.demo.models.Adherent;
import com.example.demo.models.Pack;
import com.example.demo.utils.AnimationUtils;
import com.example.demo.utils.SvgIcons;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contrôleur pour les statistiques et graphiques avancés avec design moderne
 */
public class StatistiquesController {
    private static final Logger logger = Logger.getLogger(StatistiquesController.class.getName());
    private AdherentDAO adherentDAO = new AdherentDAO();
    private PackDAO packDAO = new PackDAO();
    private PaiementDAO paiementDAO = new PaiementDAO();
    
    // État de navigation
    private String currentView = "Évolution"; // Vue actuellement affichée
    private VBox contentContainer; // Référence au conteneur de contenu pour les mises à jour
    private HBox navigationTabsContainer; // Référence au conteneur des tabs pour les mises à jour

    @FXML
    private TabPane tabPane;
    @FXML
    private VBox inscriptionsContainer;
    @FXML
    private VBox packsContainer;
    @FXML
    private VBox revenusContainer;
    @FXML
    private VBox retentionContainer;
    @FXML
    private HBox headerBox;

    public Parent getView() {
        // Utiliser toujours createBasicView() pour la nouvelle structure
        return createBasicView();
    }

    /**
     * Vue de secours si le FXML ne charge pas - Structure complète selon design dashboard
     */
    private Parent createBasicView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        
        BorderPane centerArea = new BorderPane();
        HBox header = createHeader();
        centerArea.setTop(header);
        
        HBox titleFilterSection = createTitleFilterSection();
        
        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setFitToWidth(true);
        contentScroll.setFitToHeight(true);
        contentScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox contentWrapper = new VBox();
        contentWrapper.setPadding(new Insets(20, 24, 20, 24));
        contentWrapper.setStyle("-fx-background-color: #0d0f1a;");
        contentWrapper.setMaxWidth(Double.MAX_VALUE);
        
        // Navigation tabs
        navigationTabsContainer = createNavigationTabs();
        contentWrapper.getChildren().add(navigationTabsContainer);
        
        // Content area avec la vue actuelle
        contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(0));
        contentContainer.setStyle("-fx-background-color: transparent;");
        contentContainer.setMaxWidth(Double.MAX_VALUE);
        
        // Afficher la vue par défaut
        contentContainer.getChildren().addAll(createViewContent(currentView));
        
        contentWrapper.getChildren().add(contentContainer);
        contentScroll.setContent(contentWrapper);
        
        VBox centerContent = new VBox(0);
        centerContent.getChildren().addAll(titleFilterSection, contentScroll);
        VBox.setVgrow(contentScroll, Priority.ALWAYS);
        
        centerArea.setCenter(centerContent);
        root.setCenter(centerArea);
        
        return root;
    }

    /**
     * Crée le header avec menu, star, breadcrumb et icônes utilitaires (identique au dashboard)
     */
    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(16, 32, 16, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(70);
        header.setStyle("-fx-background-color: #0A0D12; -fx-border-width: 0 0 1 0; -fx-border-color: rgba(154, 164, 178, 0.1);");
        
        // Menu icon (pas de fonction pour statistiques, peut être vide)
        Button menuBtn = createHeaderIconButton("icon-menu", 20);
        menuBtn.setVisible(false); // Caché pour statistiques
        
        // Star icon (Favoris)
        Button starBtn = createHeaderIconButton("icon-star", 20);
        starBtn.setOnAction(e -> {
            try {
                com.example.demo.dao.FavorisDAO favorisDAO = new com.example.demo.dao.FavorisDAO();
                String pageName = "STATISTIQUES";
                boolean isFavorite = favorisDAO.toggleFavorite(1, pageName);
                if (isFavorite) {
                    starBtn.setStyle(starBtn.getStyle() + "; -fx-opacity: 1.0;");
                } else {
                    starBtn.setStyle(starBtn.getStyle() + "; -fx-opacity: 0.5;");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        
        // Breadcrumb
        Label breadcrumbLabel = new Label("Statistiques & Analytics");
        breadcrumbLabel.setStyle("-fx-text-fill: #9AA4B2; -fx-font-size: 13px; -fx-font-weight: 500;");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Moon icon (Dark mode)
        Button moonBtn = createHeaderIconButton("icon-moon", 20);
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
        
        // Refresh icon
        Button refreshBtn = createHeaderIconButton("icon-refresh", 20);
        refreshBtn.setOnAction(e -> refreshContent());
        
        // Bell icon (Notifications) - simplifié pour statistiques
        Button bellBtn = createHeaderIconButton("icon-bell", 20);
        
        // Globe icon
        Button globeBtn = createHeaderIconButton("icon-globe", 20);
        
        header.getChildren().addAll(menuBtn, starBtn, breadcrumbLabel, spacer, moonBtn, refreshBtn, bellBtn, globeBtn);
        
        return header;
    }
    
    /**
     * Crée la section titre + filtre sous le header (identique au dashboard)
     */
    private HBox createTitleFilterSection() {
        HBox section = new HBox();
        section.setPadding(new Insets(24, 32, 24, 32));
        section.setAlignment(Pos.CENTER_LEFT);
        section.setPrefHeight(60);
        section.setStyle("-fx-background-color: #0B0F14;");
        
        // Titre "Statistiques & Analytics" à gauche
        Label titleLabel = new Label("Statistiques & Analytics");
        titleLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 24px; -fx-font-weight: 700;");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        section.getChildren().addAll(titleLabel, spacer);
        
        return section;
    }
    
    /**
     * Crée les boutons de navigation (remplace TabPane)
     */
    private HBox createNavigationTabs() {
        HBox tabsContainer = new HBox(8);
        tabsContainer.setPadding(new Insets(0));
        tabsContainer.setStyle("-fx-background-color: transparent;");
        
        String[] tabLabels = {"Évolution", "Répartition", "Revenus", "Rétention"};
        
        for (String label : tabLabels) {
            Button tabBtn = createNavigationTabButton(label);
            tabsContainer.getChildren().add(tabBtn);
        }
        
        return tabsContainer;
    }
    
    /**
     * Crée un bouton de navigation
     */
    private Button createNavigationTabButton(String label) {
        Button btn = new Button(label);
        btn.setPadding(new Insets(12, 20, 12, 20));
        btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #9AA4B2; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500; " +
            "-fx-border-width: 0 0 2 0; " +
            "-fx-border-color: transparent; " +
            "-fx-cursor: hand;"
        );
        
        // Style selon si c'est la vue active
        if (label.equals(currentView)) {
            btn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 600; " +
                "-fx-border-width: 0 0 2 0; " +
                "-fx-border-color: #00E676; " +
                "-fx-cursor: hand;"
            );
        }
        
        // Hover effect
        btn.setOnMouseEntered(e -> {
            if (!label.equals(currentView)) {
                btn.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.05); " +
                    "-fx-text-fill: #FFFFFF; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: 500; " +
                    "-fx-border-width: 0 0 2 0; " +
                    "-fx-border-color: transparent; " +
                    "-fx-cursor: hand;"
                );
            }
        });
        
        btn.setOnMouseExited(e -> {
            if (!label.equals(currentView)) {
                btn.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-text-fill: #9AA4B2; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: 500; " +
                    "-fx-border-width: 0 0 2 0; " +
                    "-fx-border-color: transparent; " +
                    "-fx-cursor: hand;"
                );
            }
        });
        
        // Action
        btn.setOnAction(e -> switchView(label));
        
        return btn;
    }
    
    /**
     * Change de vue
     */
    private void switchView(String viewName) {
        if (viewName.equals(currentView)) return;
        
        currentView = viewName;
        
        // Mettre à jour le contenu
        if (contentContainer != null) {
            contentContainer.getChildren().clear();
            VBox newContent = createViewContent(currentView);
            contentContainer.getChildren().add(newContent);
        }
        
        // Mettre à jour les styles des boutons de navigation
        if (navigationTabsContainer != null) {
            for (Node node : navigationTabsContainer.getChildren()) {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    String btnText = btn.getText();
                    if (btnText.equals(currentView)) {
                        // Bouton actif
                        btn.setStyle(
                            "-fx-background-color: transparent; " +
                            "-fx-text-fill: #FFFFFF; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-border-width: 0 0 2 0; " +
                            "-fx-border-color: #00E676; " +
                            "-fx-cursor: hand;"
                        );
                    } else {
                        // Boutons inactifs
                        btn.setStyle(
                            "-fx-background-color: transparent; " +
                            "-fx-text-fill: #9AA4B2; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-border-width: 0 0 2 0; " +
                            "-fx-border-color: transparent; " +
                            "-fx-cursor: hand;"
                        );
                    }
                }
            }
        }
    }
    
    /**
     * Crée le contenu pour une vue donnée
     */
    private VBox createViewContent(String viewName) {
        switch (viewName) {
            case "Évolution":
                return createEvolutionView();
            case "Répartition":
                return createRepartitionView();
            case "Revenus":
                return createRevenusView();
            case "Rétention":
                return createRetentionView();
            default:
                return createEvolutionView();
        }
    }
    
    /**
     * Rafraîchit le contenu
     */
    private void refreshContent() {
        if (contentContainer != null) {
            contentContainer.getChildren().clear();
            VBox newContent = createViewContent(currentView);
            contentContainer.getChildren().add(newContent);
        }
    }
    
    /**
     * Crée un bouton d'icône pour le header
     */
    private Button createHeaderIconButton(String iconName, double size) {
        Button button = new Button();
        button.setPrefSize(size + 8, size + 8);
        button.setMinSize(size + 8, size + 8);
        button.setMaxSize(size + 8, size + 8);
        button.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-background-radius: 6px; " +
            "-fx-padding: 4px; " +
            "-fx-cursor: hand;"
        );
        
        Node icon = loadSVGIcon(iconName, size, "#9AA4B2");
        if (icon != null) {
            button.setGraphic(icon);
            
            button.setOnMouseEntered(e -> {
                button.setStyle(
                    "-fx-background-color: rgba(27, 34, 44, 0.8); " +
                    "-fx-background-radius: 6px; " +
                    "-fx-padding: 4px; " +
                    "-fx-cursor: hand;"
                );
                setIconColor(icon, "#9EFF00");
            });
            
            button.setOnMouseExited(e -> {
                button.setStyle(
                    "-fx-background-color: transparent; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-padding: 4px; " +
                    "-fx-cursor: hand;"
                );
                setIconColor(icon, "#9AA4B2");
            });
        }
        
        return button;
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
                svgPathNode.setFill(null);
                svgPathNode.setStroke(Color.web(color));
                svgPathNode.setStrokeWidth(2.0);
                svgPathNode.setStrokeLineCap(StrokeLineCap.ROUND);
                svgPathNode.setStrokeLineJoin(StrokeLineJoin.ROUND);
                
                double scale = size / 24.0;
                svgPathNode.setScaleX(scale);
                svgPathNode.setScaleY(scale);
                
                StackPane container = new StackPane();
                container.setPrefSize(size, size);
                container.setMaxSize(size, size);
                container.setMinSize(size, size);
                container.setAlignment(Pos.CENTER);
                container.getChildren().add(svgPathNode);
                container.setStyle("-fx-background-color: transparent;");
                
                return container;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'icône " + iconName + ": " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Retourne le path SVG correspondant au nom de l'icône
     */
    private String getSvgPathForIcon(String iconName) {
        return switch (iconName) {
            case "icon-menu" -> SvgIcons.MENU;
            case "icon-star" -> SvgIcons.STAR;
            case "icon-moon" -> SvgIcons.MOON;
            case "icon-refresh" -> SvgIcons.REFRESH;
            case "icon-bell" -> SvgIcons.BELL;
            case "icon-globe" -> SvgIcons.GLOBE;
            case "icon-trending-up" -> SvgIcons.TRENDING_UP;
            case "icon-trending-down" -> SvgIcons.TRENDING_DOWN;
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
     * Crée la vue Évolution avec cartes KPI et graphique
     */
    private VBox createEvolutionView() {
        VBox view = new VBox(20);
        view.setStyle("-fx-background-color: transparent;");
        
        // Cartes KPI
        HBox statCards = createEvolutionStatCards();
        view.getChildren().add(statCards);
        
        // Graphique
        VBox chartCard = createEvolutionChartCard();
        view.getChildren().add(chartCard);
        
        return view;
    }
    
    /**
     * Crée les cartes KPI pour la vue Évolution
     */
    private HBox createEvolutionStatCards() {
        HBox container = new HBox(16);
        container.setStyle("-fx-background-color: transparent;");
        
        try {
            List<Adherent> adherents = adherentDAO.findAll();
            Map<String, Long> inscriptionsParMois = adherents.stream()
                .filter(a -> a.getDateInscription() != null)
                .collect(Collectors.groupingBy(
                    a -> a.getDateInscription().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    Collectors.counting()
                ));
            
            long totalInscriptions = adherents.size();
            long ceMois = inscriptionsParMois.getOrDefault(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")), 0L);
            
            VBox card1 = createKPICard("Total Inscriptions", String.valueOf(totalInscriptions), "", true);
            VBox card2 = createKPICard("Ce Mois", String.valueOf(ceMois), "", true);
            
            HBox.setHgrow(card1, Priority.ALWAYS);
            HBox.setHgrow(card2, Priority.ALWAYS);
            
            container.getChildren().addAll(card1, card2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return container;
    }
    
    /**
     * Crée la carte KPI selon le style dashboard
     */
    private VBox createKPICard(String label, String value, String change, boolean positive) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setMinHeight(140);
        card.setPrefHeight(140);
        card.setMaxHeight(140);
        card.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0, 0, 2);"
        );
        
        // Label
        Label labelLabel = new Label(label);
        labelLabel.setStyle(
            "-fx-text-fill: #8B92A8; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500;"
        );
        
        // Value
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 36px; " +
            "-fx-font-weight: 700;"
        );
        
        card.getChildren().addAll(labelLabel, valueLabel);
        
        // Ajouter le changement si fourni
        if (change != null && !change.isEmpty()) {
            HBox changeContainer = new HBox(6);
            changeContainer.setAlignment(Pos.CENTER_LEFT);
            
            String iconName = positive ? "icon-trending-up" : "icon-trending-down";
            String iconColor = positive ? "#00E676" : "#EF4444";
            Node trendIcon = loadSVGIcon(iconName, 14, iconColor);
            
            if (trendIcon != null) {
                Label changeLabel = new Label(change);
                changeLabel.setStyle(
                    "-fx-text-fill: " + iconColor + "; " +
                    "-fx-font-size: 12px; " +
                    "-fx-font-weight: 600;"
                );
                changeContainer.getChildren().addAll(trendIcon, changeLabel);
                card.getChildren().add(changeContainer);
            }
        }
        
        return card;
    }
    
    /**
     * Crée la carte de graphique Évolution
     */
    private VBox createEvolutionChartCard() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0, 0, 2);"
        );
        container.setMinHeight(400);
        
        // Titre
        Label titleLabel = new Label("Évolution des Inscriptions (12 derniers mois)");
        titleLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 18px; -fx-font-weight: 600;");
        
        // Graphique
        LineChart<String, Number> chart = createEvolutionLineChart();
        chart.setPrefHeight(350);
        
        container.getChildren().addAll(titleLabel, chart);
        
        return container;
    }
    
    /**
     * Crée le LineChart pour l'évolution
     */
    private LineChart<String, Number> createEvolutionLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Mois");
        xAxis.setStyle("-fx-tick-label-fill: #FFFFFF; -fx-font-size: 11px; -fx-tick-label-font-size: 11px;");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre d'inscriptions");
        yAxis.setStyle("-fx-tick-label-fill: #FFFFFF; -fx-font-size: 11px; -fx-tick-label-font-size: 11px;");
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        lineChart.setLegendVisible(true);
        lineChart.setAnimated(true);
        lineChart.setStyle("-fx-background-color: transparent;");
        
        try {
            List<Adherent> adherents = adherentDAO.findAll();
            Map<String, Long> inscriptionsParMois = adherents.stream()
                .filter(a -> a.getDateInscription() != null)
                .collect(Collectors.groupingBy(
                    a -> a.getDateInscription().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    Collectors.counting()
                ));
            
            XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
            dataSeries.setName("Nouveaux Inscrits");
            
            LocalDate now = LocalDate.now();
            for (int i = 11; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                String monthKey = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                String monthLabel = month.format(DateTimeFormatter.ofPattern("MMM yyyy", java.util.Locale.FRENCH));
                long count = inscriptionsParMois.getOrDefault(monthKey, 0L);
                dataSeries.getData().add(new XYChart.Data<>(monthLabel, count));
            }
            
            lineChart.getData().add(dataSeries);
            
            // Appliquer les styles après que le graphique soit rendu
            Platform.runLater(() -> {
                try {
                    Node chartNode = lineChart.lookup(".chart-plot-background");
                    if (chartNode != null) {
                        chartNode.setStyle("-fx-background-color: transparent;");
                    }
                    Node legend = lineChart.lookup(".chart-legend");
                    if (legend != null) {
                        legend.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 12px;");
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs de style
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return lineChart;
    }
    
    /**
     * Crée la vue Répartition
     */
    private VBox createRepartitionView() {
        VBox view = new VBox(20);
        view.setStyle("-fx-background-color: transparent;");
        
        VBox chartCard = createRepartitionChartCard();
        view.getChildren().add(chartCard);
        
        return view;
    }
    
    /**
     * Crée la vue Revenus
     */
    private VBox createRevenusView() {
        VBox view = new VBox(20);
        view.setStyle("-fx-background-color: transparent;");
        
        // Cartes KPI
        HBox statCards = createRevenusStatCards();
        view.getChildren().add(statCards);
        
        // Graphique
        VBox chartCard = createRevenusChartCard();
        view.getChildren().add(chartCard);
        
        return view;
    }
    
    /**
     * Crée la vue Rétention
     */
    private VBox createRetentionView() {
        VBox view = new VBox(20);
        view.setStyle("-fx-background-color: transparent;");
        
        VBox chartCard = createRetentionChartCard();
        view.getChildren().add(chartCard);
        
        return view;
    }
    
    /**
     * Crée la carte PieChart pour la répartition
     */
    private VBox createRepartitionChartCard() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0, 0, 2);"
        );
        container.setMinHeight(500);
        
        Label titleLabel = new Label("Répartition des Adhérents par Pack");
        titleLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 18px; -fx-font-weight: 600;");
        
        PieChart pieChart = createPacksPieChart();
        pieChart.setPrefHeight(450);
        
        container.getChildren().addAll(titleLabel, pieChart);
        
        return container;
    }
    
    /**
     * Crée le PieChart pour la répartition
     */
    private PieChart createPacksPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        try {
            List<Pack> packs = packDAO.findAll();
            List<Adherent> adherents = adherentDAO.findAll();
            
            Map<String, Long> adherentsParPack = adherents.stream()
                .filter(a -> a.getPackId() != null)
                .collect(Collectors.groupingBy(
                    a -> {
                        try {
                            Pack pack = packDAO.findById(a.getPackId());
                            return pack != null ? pack.getNom() : "Aucun";
                        } catch (SQLException e) {
                            return "Aucun";
                        }
                    },
                    Collectors.counting()
                ));
            
            Color[] colors = {
                Color.web("#FF6B35"), Color.web("#4ECDC4"), Color.web("#6A0572"),
                Color.web("#45B7D1"), Color.web("#82E0AA"), Color.web("#00E676")
            };
            
            int colorIndex = 0;
            for (Map.Entry<String, Long> entry : adherentsParPack.entrySet()) {
                PieChart.Data data = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
                pieChartData.add(data);
                colorIndex++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-pie-label-visible: true;"
        );
        
        // Appliquer les couleurs après que le graphique soit rendu (avec délai pour laisser les labels se créer)
        PauseTransition pause = new PauseTransition(Duration.millis(150));
        pause.setOnFinished(event -> {
            Platform.runLater(() -> {
                try {
                    Color[] colors = {
                        Color.web("#FF6B35"), Color.web("#4ECDC4"), Color.web("#6A0572"),
                        Color.web("#45B7D1"), Color.web("#82E0AA"), Color.web("#00E676")
                    };
                    int colorIndex = 0;
                    for (PieChart.Data data : pieChartData) {
                        if (data.getNode() != null) {
                            data.getNode().setStyle(String.format(
                                "-fx-pie-color: %s;",
                                colors[colorIndex % colors.length].toString().replace("0x", "#")
                            ));
                        }
                        colorIndex++;
                    }
                    
                    // Styliser spécifiquement les labels du PieChart avec couleur claire (blanc)
                    java.util.Set<Node> labelNodes = pieChart.lookupAll(".chart-pie-label");
                    for (Node node : labelNodes) {
                        if (node instanceof Text) {
                            ((Text) node).setFill(Color.WHITE);
                            node.setStyle("-fx-fill: #FFFFFF; -fx-font-size: 13px; -fx-font-weight: 600;");
                        }
                    }
                    
                    // Styliser tous les Text nodes dans le PieChart (labels inclus) avec couleur claire
                    styleAllTextNodes(pieChart, Color.WHITE);
                    
                    // Styliser aussi via les sélecteurs CSS
                    java.util.Set<Node> allTextNodes = pieChart.lookupAll(".text");
                    for (Node node : allTextNodes) {
                        if (node instanceof Text) {
                            ((Text) node).setFill(Color.WHITE);
                            node.setStyle("-fx-fill: #FFFFFF; -fx-font-size: 13px; -fx-font-weight: 600;");
                        }
                    }
                    
                    Node legend = pieChart.lookup(".chart-legend");
                    if (legend != null) {
                        legend.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 12px;");
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs de style
                    e.printStackTrace();
                }
            });
        });
        pause.play();
        
        return pieChart;
    }
    
    /**
     * Crée les cartes KPI pour la vue Revenus
     */
    private HBox createRevenusStatCards() {
        HBox container = new HBox(16);
        container.setStyle("-fx-background-color: transparent;");
        
        try {
            double totalRevenus = 0.0;
            LocalDate now = LocalDate.now();
            for (int i = 11; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                totalRevenus += paiementDAO.getRevenusMois(month);
            }
            
            double moyenneMois = totalRevenus / 12;
            
            VBox card1 = createKPICard("Total 12 Mois", String.format("%.2f DH", totalRevenus), "", true);
            VBox card2 = createKPICard("Moyenne/Mois", String.format("%.2f DH", moyenneMois), "", true);
            
            HBox.setHgrow(card1, Priority.ALWAYS);
            HBox.setHgrow(card2, Priority.ALWAYS);
            
            container.getChildren().addAll(card1, card2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return container;
    }
    
    /**
     * Crée la carte BarChart pour les revenus
     */
    private VBox createRevenusChartCard() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0, 0, 2);"
        );
        container.setMinHeight(400);
        
        Label titleLabel = new Label("Revenus Mensuels (12 derniers mois)");
        titleLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 18px; -fx-font-weight: 600;");
        
        BarChart<String, Number> barChart = createRevenusBarChart();
        barChart.setPrefHeight(350);
        
        container.getChildren().addAll(titleLabel, barChart);
        
        return container;
    }
    
    /**
     * Crée le BarChart pour les revenus
     */
    private BarChart<String, Number> createRevenusBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Mois");
        xAxis.setStyle("-fx-tick-label-fill: #FFFFFF; -fx-font-size: 11px; -fx-tick-label-font-size: 11px;");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenus (DH)");
        yAxis.setStyle("-fx-tick-label-fill: #FFFFFF; -fx-font-size: 11px; -fx-tick-label-font-size: 11px;");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("");
        barChart.setLegendVisible(true);
        barChart.setAnimated(true);
        barChart.setStyle(
            "-fx-background-color: transparent; " +
            "-chart-bar-fill: #10b981;"
        );
        
        try {
            XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
            dataSeries.setName("Revenus (DH)");
            
            LocalDate now = LocalDate.now();
            for (int i = 11; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                Double revenus = paiementDAO.getRevenusMois(month);
                String monthLabel = month.format(DateTimeFormatter.ofPattern("MMM yyyy", java.util.Locale.FRENCH));
                dataSeries.getData().add(new XYChart.Data<>(monthLabel, revenus));
            }
            
            barChart.getData().add(dataSeries);
            
            // Appliquer les styles après que le graphique soit rendu (avec délai pour laisser les barres se créer)
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(event -> {
                Platform.runLater(() -> {
                    try {
                        // Style du fond du graphique
                        Node chartNode = barChart.lookup(".chart-plot-background");
                        if (chartNode != null) {
                            chartNode.setStyle("-fx-background-color: transparent;");
                        }
                        
                        // Style de la légende
                        Node legend = barChart.lookup(".chart-legend");
                        if (legend != null) {
                            legend.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 12px;");
                        }
                        
                        // Style des barres - couleur verte visible sur fond sombre
                        // Méthode 1: Via les séries de données (plus fiable)
                        for (XYChart.Series<String, Number> series : barChart.getData()) {
                            for (XYChart.Data<String, Number> data : series.getData()) {
                                Node node = data.getNode();
                                if (node != null) {
                                    node.setStyle("-fx-bar-fill: #10b981;");
                                }
                            }
                        }
                        
                        // Méthode 2: Via lookupAll (backup)
                        java.util.Set<Node> bars = barChart.lookupAll(".chart-bar");
                        for (Node bar : bars) {
                            bar.setStyle("-fx-bar-fill: #10b981;");
                        }
                        
                        // Méthode 3: Via default-color0 (classe CSS par défaut)
                        java.util.Set<Node> defaultBars = barChart.lookupAll(".default-color0.chart-bar");
                        for (Node bar : defaultBars) {
                            bar.setStyle("-fx-bar-fill: #10b981;");
                        }
                        
                        // Style des grilles
                        Node verticalGrid = barChart.lookup(".chart-vertical-grid-lines");
                        if (verticalGrid != null) {
                            verticalGrid.setStyle("-fx-stroke: rgba(255, 255, 255, 0.1);");
                        }
                        Node horizontalGrid = barChart.lookup(".chart-horizontal-grid-lines");
                        if (horizontalGrid != null) {
                            horizontalGrid.setStyle("-fx-stroke: rgba(255, 255, 255, 0.1);");
                        }
                    } catch (Exception e) {
                        // Ignorer les erreurs de style
                        e.printStackTrace();
                    }
                });
            });
            pause.play();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return barChart;
    }
    
    /**
     * Crée la carte LineChart pour la rétention
     */
    private VBox createRetentionChartCard() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0, 0, 2);"
        );
        container.setMinHeight(400);
        
        Label titleLabel = new Label("Taux de Rétention des Abonnements (%)");
        titleLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 18px; -fx-font-weight: 600;");
        
        LineChart<String, Number> lineChart = createRetentionLineChart();
        lineChart.setPrefHeight(350);
        
        container.getChildren().addAll(titleLabel, lineChart);
        
        return container;
    }
    
    /**
     * Crée le LineChart pour la rétention
     */
    private LineChart<String, Number> createRetentionLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Mois");
        xAxis.setStyle("-fx-tick-label-fill: #FFFFFF; -fx-font-size: 11px; -fx-tick-label-font-size: 11px;");
        
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Taux (%)");
        yAxis.setStyle("-fx-tick-label-fill: #FFFFFF; -fx-font-size: 11px; -fx-tick-label-font-size: 11px;");
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        lineChart.setLegendVisible(true);
        lineChart.setAnimated(true);
        lineChart.setStyle("-fx-background-color: transparent;");
        
        try {
            List<Adherent> adherents = adherentDAO.findAll();
            XYChart.Series<String, Number> retentionSeries = new XYChart.Series<>();
            retentionSeries.setName("Taux de Rétention (%)");
            
            LocalDate now = LocalDate.now();
            for (int i = 11; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                String monthLabel = month.format(DateTimeFormatter.ofPattern("MMM yyyy", java.util.Locale.FRENCH));
                
                long actifs = adherents.stream()
                    .filter(a -> a.getDateFin() == null || !a.getDateFin().isBefore(month))
                    .count();
                
                double taux = adherents.isEmpty() ? 0 : (actifs * 100.0 / adherents.size());
                retentionSeries.getData().add(new XYChart.Data<>(monthLabel, taux));
            }
            
            lineChart.getData().add(retentionSeries);
            
            // Appliquer les styles après que le graphique soit rendu
            Platform.runLater(() -> {
                try {
                    Node chartNode = lineChart.lookup(".chart-plot-background");
                    if (chartNode != null) {
                        chartNode.setStyle("-fx-background-color: transparent;");
                    }
                    Node legend = lineChart.lookup(".chart-legend");
                    if (legend != null) {
                        legend.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 12px;");
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs de style
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return lineChart;
    }


    /**
     * Stylise récursivement tous les Text nodes dans un Node avec une couleur donnée
     */
    private void styleAllTextNodes(Node node, Color color) {
        if (node instanceof Text) {
            Text textNode = (Text) node;
            textNode.setFill(color);
            // Convertir la couleur en format hex pour CSS
            String colorHex = String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
            // Utiliser une taille de police plus grande et un poids plus fort pour meilleure visibilité
            textNode.setStyle("-fx-fill: " + colorHex + "; -fx-font-size: 13px; -fx-font-weight: 600;");
        }
        
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                styleAllTextNodes(child, color);
            }
        }
    }
    
    /**
     * Crée un label d'erreur stylisé
     */
    private Label createErrorLabel(String message) {
        Label error = new Label("❌ " + message);
        error.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #ef4444;
            -fx-font-weight: 600;
            -fx-padding: 20;
            -fx-background-color: #fee2e2;
            -fx-background-radius: 12;
            -fx-border-width: 1;
            -fx-border-color: #fecaca;
            -fx-border-radius: 12;
        """);
        return error;
    }
}
