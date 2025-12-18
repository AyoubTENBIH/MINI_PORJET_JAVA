package com.example.demo.controllers;

import com.example.demo.dao.AdherentDAO;
import com.example.demo.dao.PaiementDAO;
import com.example.demo.dao.PackDAO;
import com.example.demo.models.Adherent;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard Premium avec design SaaS dark UI
 */
public class DashboardController {
    private AdherentDAO adherentDAO = new AdherentDAO();
    private PaiementDAO paiementDAO = new PaiementDAO();
    private PackDAO packDAO = new PackDAO();
    
    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        
        // HEADER (Top Bar)
        HBox header = createHeader();
        root.setTop(header);
        
        // CONTENT AREA (Center)
        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setFitToWidth(true);
        contentScroll.setFitToHeight(true);
        contentScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        contentScroll.getStyleClass().add("content-area");
        
        VBox content = new VBox(24);
        content.setPadding(new Insets(24, 32, 24, 32));
        content.setStyle("-fx-background-color: #0B0F14;");
        
        // KPI Cards Row
        HBox kpiRow = createKPIRow();
        content.getChildren().add(kpiRow);
        
        // Charts Row
        HBox chartsRow = createChartsRow();
        content.getChildren().add(chartsRow);
        
        // Bottom Row (Tables & Activity)
        HBox bottomRow = createBottomRow();
        content.getChildren().add(bottomRow);
        
        contentScroll.setContent(content);
        root.setCenter(contentScroll);
        
        // RIGHT SIDEBAR (Notifications & Activity)
        VBox rightSidebar = createRightSidebar();
        root.setRight(rightSidebar);
        
        return root;
    }
    
    /**
     * Crée le header avec titre, breadcrumb, date et icônes
     */
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(16, 32, 16, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");
        header.setPrefHeight(70);
        
        // Breadcrumb
        Label breadcrumb = new Label("Dashboard / Overview");
        breadcrumb.getStyleClass().add("header-breadcrumb");
        
        // Title
        Label title = new Label("Overview");
        title.getStyleClass().add("header-title");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Date
        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        dateLabel.getStyleClass().add("header-breadcrumb");
        
        // Icon buttons (simplified - you can add actual icons with Ikonli)
        Button refreshBtn = new Button("↻");
        refreshBtn.getStyleClass().add("header-icon-button");
        
        Button notificationBtn = new Button("🔔");
        notificationBtn.getStyleClass().add("header-icon-button");
        
        Button settingsBtn = new Button("⚙");
        settingsBtn.getStyleClass().add("header-icon-button");
        
        // User avatar
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("header-user-avatar");
        Label avatarText = new Label("GH");
        avatarText.setStyle("-fx-text-fill: #9EFF00; -fx-font-weight: 700; -fx-font-size: 14px;");
        avatar.getChildren().add(avatarText);
        
        header.getChildren().addAll(breadcrumb, title, spacer, dateLabel, refreshBtn, notificationBtn, settingsBtn, avatar);
        
        return header;
    }
    
    /**
     * Crée la ligne de KPI Cards
     */
    private HBox createKPIRow() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        
        try {
            // KPI 1: Revenus du mois
            double revenusMois = paiementDAO.getRevenusMois(LocalDate.now());
            double revenusMoisPrecedent = paiementDAO.getRevenusMois(LocalDate.now().minusMonths(1));
            double changeRevenus = revenusMoisPrecedent > 0 ? ((revenusMois - revenusMoisPrecedent) / revenusMoisPrecedent) * 100 : 0;
            
            VBox kpi1 = createKPICard(
                "💰",
                "Revenus du Mois",
                String.format("%.0f DH", revenusMois),
                String.format("%.1f%% vs mois dernier", changeRevenus),
                changeRevenus >= 0
            );
            
            // KPI 2: Adhérents actifs
            int adherentsActifs = adherentDAO.findAll().size();
            int adherentsActifsPrecedent = adherentsActifs; // Simplified
            double changeAdherents = 5.2; // Example
            
            VBox kpi2 = createKPICard(
                "👥",
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
            
            VBox kpi3 = createKPICard(
                "⭐",
                "Nouveaux Abonnements",
                String.valueOf(nouveauxAbonnements),
                "Ce mois",
                true
            );
            
            // KPI 4: Abonnements expirés
            int abonnementsExpires = adherentDAO.findExpired().size();
            
            VBox kpi4 = createKPICard(
                "⚠️",
                "Abonnements Expirés",
                String.valueOf(abonnementsExpires),
                "Action requise",
                false
            );
            
            // KPI 5: Taux de renouvellement (simplified)
            double tauxRenouvellement = 75.5;
            
            VBox kpi5 = createKPICard(
                "🔄",
                "Taux de Renouvellement",
                String.format("%.1f%%", tauxRenouvellement),
                "Objectif: 80%",
                tauxRenouvellement >= 75
            );
            
            row.getChildren().addAll(kpi1, kpi2, kpi3, kpi4, kpi5);
            
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des données");
            errorLabel.setStyle("-fx-text-fill: #EF4444;");
            row.getChildren().add(errorLabel);
        }
        
        return row;
    }
    
    /**
     * Crée une carte KPI premium
     */
    private VBox createKPICard(String icon, String label, String value, String change, boolean positive) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setPrefWidth(220);
        card.setPrefHeight(140);
        card.getStyleClass().add("kpi-card");
        
        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        // Value
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("kpi-value");
        
        // Label
        Label labelLabel = new Label(label);
        labelLabel.getStyleClass().add("kpi-label");
        
        // Change
        Label changeLabel = new Label(change);
        changeLabel.getStyleClass().add("kpi-change");
        if (!positive) {
            changeLabel.getStyleClass().add("negative");
        }
        
        card.getChildren().addAll(iconLabel, valueLabel, labelLabel, changeLabel);
        
        // Animation on load
        FadeTransition fade = new FadeTransition(Duration.millis(500), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        
        return card;
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
     * Crée une carte avec tableau
     */
    private VBox createTableCard(String title) {
        VBox container = new VBox(16);
        container.setPadding(new Insets(24));
        container.getStyleClass().add("card");
        container.setPrefHeight(300);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        
        // Table simplifiée (vous pouvez utiliser TableView ici)
        VBox tableContent = new VBox(12);
        
        try {
            List<Adherent> recentAdherents = adherentDAO.findAll().stream()
                .limit(5)
                .toList();
            
            for (Adherent adherent : recentAdherents) {
                HBox row = new HBox(16);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8));
                row.setStyle("-fx-background-color: #0A0D12; -fx-background-radius: 8;");
                
                Label nameLabel = new Label(adherent.getNomComplet());
                nameLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-weight: 600;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Label packLabel = new Label(adherent.getPack() != null ? adherent.getPack().getNom() : "N/A");
                packLabel.setStyle("-fx-text-fill: #9AA4B2;");
                
                row.getChildren().addAll(nameLabel, spacer, packLabel);
                tableContent.getChildren().add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        container.getChildren().addAll(titleLabel, tableContent);
        
        return container;
    }
    
    /**
     * Crée une carte promotionnelle
     */
    private VBox createPromoCard() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: linear-gradient(135deg, rgba(158, 255, 0, 0.15), rgba(158, 255, 0, 0.05)); " +
                          "-fx-background-radius: 16; -fx-border-width: 1; -fx-border-color: rgba(158, 255, 0, 0.3); " +
                          "-fx-border-radius: 16;");
        container.setPrefHeight(300);
        
        Label iconLabel = new Label("⚡");
        iconLabel.setStyle("-fx-font-size: 32px;");
        
        Label titleLabel = new Label("Pack Premium");
        titleLabel.setStyle("-fx-text-fill: #9EFF00; -fx-font-size: 20px; -fx-font-weight: 700;");
        
        Label priceLabel = new Label("500 DH / Mois");
        priceLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 24px; -fx-font-weight: 700;");
        
        Label descLabel = new Label("Accès complet à toutes les installations + coach personnel");
        descLabel.setStyle("-fx-text-fill: #9AA4B2; -fx-font-size: 13px;");
        descLabel.setWrapText(true);
        
        Button btn = new Button("En savoir plus");
        btn.getStyleClass().add("btn-primary");
        
        container.getChildren().addAll(iconLabel, titleLabel, priceLabel, descLabel, btn);
        container.setAlignment(Pos.CENTER);
        
        return container;
    }
    
    /**
     * Crée la sidebar droite avec notifications et activité
     */
    private VBox createRightSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(24, 20, 24, 20));
        sidebar.setPrefWidth(320);
        sidebar.setStyle("-fx-background-color: #0A0D12;");
        
        // Notifications
        VBox notificationsContainer = createNotificationPanel("Notifications");
        sidebar.getChildren().add(notificationsContainer);
        
        // Activité récente
        VBox activityContainer = createActivityPanel("Activité Récente");
        sidebar.getChildren().add(activityContainer);
        
        return sidebar;
    }
    
    /**
     * Crée le panneau de notifications
     */
    private VBox createNotificationPanel(String title) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("notification-panel");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 16px; -fx-font-weight: 700;");
        
        VBox notifications = new VBox(8);
        
        // Notifications exemple
        notifications.getChildren().addAll(
            createNotificationItem("👥", "5 nouveaux adhérents inscrits", "Il y a 2 min"),
            createNotificationItem("💰", "Paiement reçu: 500 DH", "Il y a 15 min"),
            createNotificationItem("⚠️", "3 abonnements expirent aujourd'hui", "Il y a 1h"),
            createNotificationItem("📊", "Rapport mensuel disponible", "Aujourd'hui")
        );
        
        container.getChildren().addAll(titleLabel, notifications);
        
        return container;
    }
    
    /**
     * Crée un élément de notification
     */
    private HBox createNotificationItem(String icon, String text, String time) {
        HBox item = new HBox(12);
        item.setPadding(new Insets(12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("notification-item");
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");
        
        VBox textBox = new VBox(4);
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("notification-text");
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("notification-time");
        textBox.getChildren().addAll(textLabel, timeLabel);
        
        item.getChildren().addAll(iconLabel, textBox);
        
        return item;
    }
    
    /**
     * Crée le panneau d'activité
     */
    private VBox createActivityPanel(String title) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("notification-panel");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 16px; -fx-font-weight: 700;");
        
        VBox activities = new VBox(8);
        
        // Activités exemple
        activities.getChildren().addAll(
            createActivityItem("✅", "Pack Premium créé", "Il y a 5 min"),
            createActivityItem("👤", "Adhérent modifié: Ahmed Alaoui", "Il y a 30 min"),
            createActivityItem("📅", "Événement ajouté au calendrier", "Il y a 2h"),
            createActivityItem("💰", "Paiement enregistré", "Il y a 3h")
        );
        
        container.getChildren().addAll(titleLabel, activities);
        
        return container;
    }
    
    /**
     * Crée un élément d'activité
     */
    private HBox createActivityItem(String icon, String text, String time) {
        HBox item = new HBox(12);
        item.setPadding(new Insets(12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("notification-item");
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px;");
        
        VBox textBox = new VBox(4);
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 13px;");
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");
        textBox.getChildren().addAll(textLabel, timeLabel);
        
        item.getChildren().addAll(iconLabel, textBox);
        
        return item;
    }
}
