package com.example.demo.controllers;

import com.example.demo.dao.AdherentDAO;
import com.example.demo.dao.PackDAO;
import com.example.demo.dao.PaiementDAO;
import com.example.demo.models.Adherent;
import com.example.demo.models.Pack;
import com.example.demo.utils.AnimationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur pour les statistiques et graphiques avancés avec design moderne
 */
public class StatistiquesController {
    private AdherentDAO adherentDAO = new AdherentDAO();
    private PackDAO packDAO = new PackDAO();
    private PaiementDAO paiementDAO = new PaiementDAO();

    public Parent getView() {
        VBox root = new VBox(25);
        root.setPadding(new javafx.geometry.Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #e2e8f0);");

        // En-tête moderne
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label title = new Label("📊 Statistiques & Analytics");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: 700; -fx-text-fill: #0f172a; -fx-letter-spacing: -1px;");
        
        Label subtitle = new Label("Analyse complète de vos données");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
        
        VBox titleBox = new VBox(5);
        titleBox.getChildren().addAll(title, subtitle);
        
        headerBox.getChildren().add(titleBox);

        // Onglets modernes
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");
        tabPane.getStyleClass().add("modern-tab-pane");

        // Onglet 1: Évolution des inscriptions
        Tab inscriptionsTab = createModernTab("📈 Évolution", createInscriptionsChart());
        
        // Onglet 2: Répartition des packs
        Tab packsTab = createModernTab("🥧 Répartition", createPacksPieChart());
        
        // Onglet 3: Revenus mensuels
        Tab revenusTab = createModernTab("💰 Revenus", createRevenusChart());
        
        // Onglet 4: Analyse des présences
        Tab presencesTab = createModernTab("👥 Présences", createPresencesChart());
        
        // Onglet 5: Taux de rétention
        Tab retentionTab = createModernTab("📉 Rétention", createRetentionChart());

        tabPane.getTabs().addAll(inscriptionsTab, packsTab, revenusTab, presencesTab, retentionTab);

        root.getChildren().addAll(headerBox, tabPane);
        
        // Animation
        AnimationUtils.fadeIn(root);

        return root;
    }

    /**
     * Crée un onglet moderne avec style
     */
    private Tab createModernTab(String text, Parent content) {
        Tab tab = new Tab(text, content);
        tab.setClosable(false);
        tab.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        return tab;
    }

    /**
     * Crée le graphique d'évolution des inscriptions
     */
    private Parent createInscriptionsChart() {
        VBox container = new VBox(20);
        container.setPadding(new javafx.geometry.Insets(30));
        container.setStyle("-fx-background-color: transparent;");

        try {
            List<Adherent> adherents = adherentDAO.findAll();
            Map<String, Long> inscriptionsParMois = adherents.stream()
                .filter(a -> a.getDateInscription() != null)
                .collect(Collectors.groupingBy(
                    a -> a.getDateInscription().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    Collectors.counting()
                ));

            // Créer les données pour les 12 derniers mois
            ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();
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

            series.add(dataSeries);

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Mois");
            xAxis.setStyle("-fx-tick-label-fill: #475569; -fx-font-size: 12px; -fx-font-weight: 500;");
            
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Nombre d'inscriptions");
            yAxis.setStyle("-fx-tick-label-fill: #475569; -fx-font-size: 12px; -fx-font-weight: 500;");

            LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Évolution des Inscriptions (12 derniers mois)");
            lineChart.setData(series);
            lineChart.setPrefHeight(500);
            lineChart.setLegendVisible(true);
            lineChart.getStyleClass().add("chart");
            lineChart.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 20;
                -fx-padding: 30;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 25, 0, 0, 8);
                -fx-chart-title-fill: #1e293b;
                -fx-chart-title-font-size: 24px;
                -fx-chart-title-font-weight: 700;
            """);
            
            // Style des lignes
            lineChart.lookup(".chart-series-line").setStyle("-fx-stroke: #3b82f6; -fx-stroke-width: 3px;");
            lineChart.lookup(".chart-line-symbol").setStyle("-fx-background-color: #3b82f6, white; -fx-background-radius: 5;");

            // Statistiques résumées
            HBox statsBox = createStatsSummary("Total Inscriptions", String.valueOf(adherents.size()),
                "Ce Mois", String.valueOf(inscriptionsParMois.getOrDefault(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")), 0L)));

            container.getChildren().addAll(statsBox, lineChart);
            AnimationUtils.fadeIn(lineChart);

        } catch (SQLException e) {
            e.printStackTrace();
            container.getChildren().add(createErrorLabel("Erreur lors du chargement des données"));
        }

        return container;
    }

    /**
     * Crée le graphique en camembert pour la répartition des packs
     */
    private Parent createPacksPieChart() {
        VBox container = new VBox(20);
        container.setPadding(new javafx.geometry.Insets(30));
        container.setStyle("-fx-background-color: transparent;");

        try {
            List<Pack> packs = packDAO.findAll();
            List<Adherent> adherents = adherentDAO.findAll();

            // Compter les adhérents par pack
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

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            Color[] colors = {
                Color.web("#3b82f6"), Color.web("#10b981"), Color.web("#f59e0b"),
                Color.web("#ef4444"), Color.web("#8b5cf6"), Color.web("#ec4899")
            };
            
            int colorIndex = 0;
            for (Map.Entry<String, Long> entry : adherentsParPack.entrySet()) {
                PieChart.Data data = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
                pieChartData.add(data);
                colorIndex = (colorIndex + 1) % colors.length;
            }

            PieChart pieChart = new PieChart(pieChartData);
            pieChart.setTitle("Répartition des Adhérents par Pack");
            pieChart.setPrefHeight(550);
            pieChart.setLabelsVisible(true);
            pieChart.setLegendVisible(true);
            pieChart.getStyleClass().add("chart");
            pieChart.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 20;
                -fx-padding: 30;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 25, 0, 0, 8);
                -fx-chart-title-fill: #1e293b;
                -fx-chart-title-font-size: 24px;
                -fx-chart-title-font-weight: 700;
            """);

            // Appliquer des couleurs aux segments
            int index = 0;
            for (PieChart.Data data : pieChartData) {
                data.getNode().setStyle(String.format(
                    "-fx-pie-color: %s;",
                    colors[index % colors.length].toString().replace("0x", "#")
                ));
                index++;
            }

            container.getChildren().add(pieChart);
            AnimationUtils.zoomIn(pieChart);

        } catch (SQLException e) {
            e.printStackTrace();
            container.getChildren().add(createErrorLabel("Erreur lors du chargement des données"));
        }

        return container;
    }

    /**
     * Crée le graphique en barres pour les revenus mensuels
     */
    private Parent createRevenusChart() {
        VBox container = new VBox(20);
        container.setPadding(new javafx.geometry.Insets(30));
        container.setStyle("-fx-background-color: transparent;");

        try {
            ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();
            XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
            dataSeries.setName("Revenus (DH)");

            double totalRevenus = 0.0;
            LocalDate now = LocalDate.now();
            for (int i = 11; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                Double revenus = paiementDAO.getRevenusMois(month);
                totalRevenus += revenus;
                String monthLabel = month.format(DateTimeFormatter.ofPattern("MMM yyyy", java.util.Locale.FRENCH));
                dataSeries.getData().add(new XYChart.Data<>(monthLabel, revenus));
            }

            series.add(dataSeries);

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Mois");
            xAxis.setStyle("-fx-tick-label-fill: #475569; -fx-font-size: 12px; -fx-font-weight: 500;");
            
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Revenus (DH)");
            yAxis.setStyle("-fx-tick-label-fill: #475569; -fx-font-size: 12px; -fx-font-weight: 500;");

            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Revenus Mensuels (12 derniers mois)");
            barChart.setData(series);
            barChart.setPrefHeight(500);
            barChart.setLegendVisible(true);
            barChart.getStyleClass().add("chart");
            barChart.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 20;
                -fx-padding: 30;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 25, 0, 0, 8);
                -fx-chart-title-fill: #1e293b;
                -fx-chart-title-font-size: 24px;
                -fx-chart-title-font-weight: 700;
            """);

            // Statistiques résumées
            HBox statsBox = createStatsSummary("Total 12 Mois", String.format("%.2f DH", totalRevenus),
                "Moyenne/Mois", String.format("%.2f DH", totalRevenus / 12));

            container.getChildren().addAll(statsBox, barChart);
            AnimationUtils.slideInUp(barChart);

        } catch (SQLException e) {
            e.printStackTrace();
            container.getChildren().add(createErrorLabel("Erreur lors du chargement des données"));
        }

        return container;
    }

    /**
     * Crée un graphique d'analyse des présences
     */
    private Parent createPresencesChart() {
        VBox container = new VBox(20);
        container.setPadding(new javafx.geometry.Insets(30));
        container.setStyle("-fx-background-color: transparent;");

        try {
            List<Adherent> adherents = adherentDAO.findAll();
            int totalAdherents = adherents.size();

            String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
            ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();
            XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
            dataSeries.setName("Taux de Présence (%)");

            Random random = new Random();
            for (String jour : jours) {
                double taux = 40 + random.nextDouble() * 40;
                dataSeries.getData().add(new XYChart.Data<>(jour, taux));
            }

            series.add(dataSeries);

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Jour de la Semaine");
            xAxis.setStyle("-fx-tick-label-fill: #475569; -fx-font-size: 12px; -fx-font-weight: 500;");
            
            NumberAxis yAxis = new NumberAxis(0, 100, 10);
            yAxis.setLabel("Taux (%)");
            yAxis.setStyle("-fx-tick-label-fill: #475569; -fx-font-size: 12px; -fx-font-weight: 500;");

            AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
            areaChart.setTitle("Taux de Présence par Jour de la Semaine");
            areaChart.setData(series);
            areaChart.setPrefHeight(500);
            areaChart.setLegendVisible(true);
            areaChart.getStyleClass().add("chart");
            areaChart.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 20;
                -fx-padding: 30;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 25, 0, 0, 8);
                -fx-chart-title-fill: #1e293b;
                -fx-chart-title-font-size: 24px;
                -fx-chart-title-font-weight: 700;
            """);

            Label info = new Label("ℹ️ Note: Les données de présences sont simulées. Le module de check-in sera implémenté prochainement.");
            info.setStyle("""
                -fx-font-size: 13px;
                -fx-text-fill: #64748b;
                -fx-font-style: italic;
                -fx-padding: 15;
                -fx-background-color: #f1f5f9;
                -fx-background-radius: 10;
                -fx-border-width: 1;
                -fx-border-color: #e2e8f0;
                -fx-border-radius: 10;
            """);

            container.getChildren().addAll(areaChart, info);
            AnimationUtils.fadeIn(areaChart);

        } catch (SQLException e) {
            e.printStackTrace();
            container.getChildren().add(createErrorLabel("Erreur lors du chargement des données"));
        }

        return container;
    }

    /**
     * Crée un graphique de taux de rétention
     */
    private Parent createRetentionChart() {
        VBox container = new VBox(20);
        container.setPadding(new javafx.geometry.Insets(30));
        container.setStyle("-fx-background-color: transparent;");

        try {
            List<Adherent> adherents = adherentDAO.findAll();
            
            ObservableList<XYChart.Series<String, Number>> series = FXCollections.observableArrayList();
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

            series.add(retentionSeries);

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Mois");
            xAxis.setStyle("-fx-tick-label-fill: #475569; -fx-font-size: 12px; -fx-font-weight: 500;");
            
            NumberAxis yAxis = new NumberAxis(0, 100, 10);
            yAxis.setLabel("Taux (%)");
            yAxis.setStyle("-fx-tick-label-fill: #475569; -fx-font-size: 12px; -fx-font-weight: 500;");

            LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Taux de Rétention des Abonnements (%)");
            lineChart.setData(series);
            lineChart.setPrefHeight(500);
            lineChart.setLegendVisible(true);
            lineChart.getStyleClass().add("chart");
            lineChart.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 20;
                -fx-padding: 30;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 25, 0, 0, 8);
                -fx-chart-title-fill: #1e293b;
                -fx-chart-title-font-size: 24px;
                -fx-chart-title-font-weight: 700;
            """);

            container.getChildren().add(lineChart);
            AnimationUtils.fadeIn(lineChart);

        } catch (SQLException e) {
            e.printStackTrace();
            container.getChildren().add(createErrorLabel("Erreur lors du chargement des données"));
        }

        return container;
    }

    /**
     * Crée une boîte de statistiques résumées
     */
    private HBox createStatsSummary(String label1, String value1, String label2, String value2) {
        HBox statsBox = new HBox(20);
        
        VBox stat1 = createStatCard(label1, value1, "#3b82f6");
        VBox stat2 = createStatCard(label2, value2, "#10b981");
        
        statsBox.getChildren().addAll(stat1, stat2);
        
        return statsBox;
    }

    /**
     * Crée une carte de statistique
     */
    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(8);
        card.setPadding(new javafx.geometry.Insets(20));
        card.setStyle(String.format("""
            -fx-background-color: white;
            -fx-background-radius: 16;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 4);
            -fx-border-width: 0;
        """));
        card.setPrefWidth(250);
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
        
        Label valueText = new Label(value);
        valueText.setStyle(String.format(
            "-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: %s;",
            color
        ));
        
        card.getChildren().addAll(labelText, valueText);
        
        return card;
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
