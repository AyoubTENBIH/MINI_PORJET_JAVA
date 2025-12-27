package com.example.demo.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;

/**
 * Exemples de méthodes pour créer un effet de graphique en arrière-plan
 * comme dans l'image "Total Profit" avec un graphique décoratif en background
 */
public class ChartBackgroundEffectExample {

    /**
     * APPROCHE 1: Utiliser un StackPane avec AreaChart en arrière-plan
     * Le graphique est placé derrière le contenu avec une opacité réduite
     */
    public static StackPane createChartBackgroundWithAreaChart() {
        StackPane container = new StackPane();
        container.setPadding(new Insets(20));
        container.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px;"
        );
        
        // Créer un AreaChart en arrière-plan (opacité réduite)
        // Note: Vous devrez importer AreaChart et configurer les axes
        // AreaChart<String, Number> backgroundChart = new AreaChart<>(xAxis, yAxis);
        // backgroundChart.setOpacity(0.3); // Opacité réduite pour effet background
        // backgroundChart.setMouseTransparent(true); // Désactiver les interactions
        
        // Contenu au premier plan
        VBox content = new VBox(10);
        content.setAlignment(Pos.TOP_LEFT);
        
        Label title = new Label("Total Profit:");
        title.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 18px; -fx-font-weight: 600;");
        
        Label date = new Label("February 2024");
        date.setStyle("-fx-text-fill: #8B92A8; -fx-font-size: 14px;");
        
        Label value = new Label("$136,755.77");
        value.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 32px; -fx-font-weight: 700;");
        
        content.getChildren().addAll(title, date, value);
        
        // container.getChildren().addAll(backgroundChart, content);
        container.getChildren().add(content);
        
        return container;
    }

    /**
     * APPROCHE 2: Utiliser Canvas pour dessiner le graphique en arrière-plan
     * Plus de contrôle sur le style et les couleurs
     */
    public static StackPane createChartBackgroundWithCanvas() {
        StackPane container = new StackPane();
        container.setPadding(new Insets(20));
        container.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px;"
        );
        
        // Canvas pour le graphique en arrière-plan
        Canvas chartCanvas = new Canvas(400, 200);
        GraphicsContext gc = chartCanvas.getGraphicsContext2D();
        
        // Données du graphique (exemple)
        double[] dataPoints = {50, 80, 60, 120, 100, 140, 110};
        int width = (int) chartCanvas.getWidth();
        int height = (int) chartCanvas.getHeight();
        int padding = 40;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;
        
        // Trouver les valeurs min et max pour la normalisation
        double minValue = 0;
        double maxValue = 150;
        
        // Dessiner la zone remplie (area chart)
        gc.setFill(Color.web("#00E676", 0.3)); // Vert avec opacité
        gc.beginPath();
        
        double xStep = chartWidth / (dataPoints.length - 1);
        double startX = padding;
        double startY = height - padding;
        
        // Commencer en bas à gauche
        gc.moveTo(startX, startY);
        
        // Dessiner la ligne supérieure
        for (int i = 0; i < dataPoints.length; i++) {
            double x = padding + i * xStep;
            double normalizedValue = (dataPoints[i] - minValue) / (maxValue - minValue);
            double y = height - padding - (normalizedValue * chartHeight);
            gc.lineTo(x, y);
        }
        
        // Fermer le path en bas à droite
        gc.lineTo(padding + (dataPoints.length - 1) * xStep, height - padding);
        gc.closePath();
        gc.fill();
        
        // Dessiner la ligne du graphique
        gc.setStroke(Color.web("#00E676")); // Vert vif pour la ligne
        gc.setLineWidth(2);
        gc.beginPath();
        
        for (int i = 0; i < dataPoints.length; i++) {
            double x = padding + i * xStep;
            double normalizedValue = (dataPoints[i] - minValue) / (maxValue - minValue);
            double y = height - padding - (normalizedValue * chartHeight);
            
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }
        gc.stroke();
        
        // Dessiner les points sur la ligne
        gc.setFill(Color.web("#00E676"));
        for (int i = 0; i < dataPoints.length; i++) {
            double x = padding + i * xStep;
            double normalizedValue = (dataPoints[i] - minValue) / (maxValue - minValue);
            double y = height - padding - (normalizedValue * chartHeight);
            
            gc.fillOval(x - 3, y - 3, 6, 6);
            gc.setFill(Color.WHITE);
            gc.fillOval(x - 1.5, y - 1.5, 3, 3);
            gc.setFill(Color.web("#00E676"));
        }
        
        // Rendre le canvas en arrière-plan
        chartCanvas.setOpacity(0.4);
        chartCanvas.setMouseTransparent(true);
        
        // Contenu au premier plan
        VBox content = new VBox(10);
        content.setAlignment(Pos.TOP_LEFT);
        
        Label title = new Label("Total Profit:");
        title.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 18px; -fx-font-weight: 600;");
        
        Label date = new Label("February 2024");
        date.setStyle("-fx-text-fill: #8B92A8; -fx-font-size: 14px;");
        
        Label value = new Label("$136,755.77");
        value.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 32px; -fx-font-weight: 700;");
        
        content.getChildren().addAll(title, date, value);
        
        container.getChildren().addAll(chartCanvas, content);
        
        return container;
    }

    /**
     * APPROCHE 3: Utiliser Path avec LinearGradient pour créer l'effet de zone remplie
     * Plus flexible et performant que Canvas pour des formes simples
     */
    public static StackPane createChartBackgroundWithPath() {
        StackPane container = new StackPane();
        container.setPadding(new Insets(20));
        container.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px;"
        );
        
        // Données du graphique
        double[] dataPoints = {50, 80, 60, 120, 100, 140, 110};
        double width = 400;
        double height = 200;
        double padding = 40;
        double chartWidth = width - 2 * padding;
        double chartHeight = height - 2 * padding;
        double minValue = 0;
        double maxValue = 150;
        
        // Créer le Path pour la zone remplie
        Path areaPath = new Path();
        areaPath.getElements().add(new MoveTo(padding, height - padding));
        
        double xStep = chartWidth / (dataPoints.length - 1);
        for (int i = 0; i < dataPoints.length; i++) {
            double x = padding + i * xStep;
            double normalizedValue = (dataPoints[i] - minValue) / (maxValue - minValue);
            double y = height - padding - (normalizedValue * chartHeight);
            areaPath.getElements().add(new LineTo(x, y));
        }
        
        areaPath.getElements().add(new LineTo(padding + (dataPoints.length - 1) * xStep, height - padding));
        areaPath.getElements().add(new ClosePath());
        
        // Créer un gradient pour la zone remplie
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1,
            true,
            null,
            new Stop(0, Color.web("#00E676", 0.4)),
            new Stop(1, Color.web("#00E676", 0.1))
        );
        areaPath.setFill(gradient);
        areaPath.setStroke(null);
        areaPath.setOpacity(0.6);
        
        // Créer le Path pour la ligne
        Path linePath = new Path();
        linePath.setStroke(Color.web("#00E676"));
        linePath.setStrokeWidth(2);
        linePath.setFill(null);
        
        double xStepLine = chartWidth / (dataPoints.length - 1);
        for (int i = 0; i < dataPoints.length; i++) {
            double x = padding + i * xStepLine;
            double normalizedValue = (dataPoints[i] - minValue) / (maxValue - minValue);
            double y = height - padding - (normalizedValue * chartHeight);
            
            if (i == 0) {
                linePath.getElements().add(new MoveTo(x, y));
            } else {
                linePath.getElements().add(new LineTo(x, y));
            }
        }
        linePath.setOpacity(0.8);
        
        // Créer les cercles pour les points
        Group chartGroup = new Group();
        chartGroup.getChildren().addAll(areaPath, linePath);
        
        for (int i = 0; i < dataPoints.length; i++) {
            double x = padding + i * xStep;
            double normalizedValue = (dataPoints[i] - minValue) / (maxValue - minValue);
            double y = height - padding - (normalizedValue * chartHeight);
            
            Circle outerCircle = new Circle(x, y, 4, Color.web("#00E676"));
            Circle innerCircle = new Circle(x, y, 2, Color.WHITE);
            chartGroup.getChildren().addAll(outerCircle, innerCircle);
        }
        
        chartGroup.setMouseTransparent(true);
        
        // Contenu au premier plan
        VBox content = new VBox(10);
        content.setAlignment(Pos.TOP_LEFT);
        
        Label title = new Label("Total Profit:");
        title.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 18px; -fx-font-weight: 600;");
        
        Label date = new Label("February 2024");
        date.setStyle("-fx-text-fill: #8B92A8; -fx-font-size: 14px;");
        
        Label value = new Label("$136,755.77");
        value.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 32px; -fx-font-weight: 700;");
        
        content.getChildren().addAll(title, date, value);
        
        container.getChildren().addAll(chartGroup, content);
        
        return container;
    }

    /**
     * APPROCHE 4: Utiliser SVG Path pour un rendu plus précis
     * Permet d'importer des chemins SVG directement
     */
    public static StackPane createChartBackgroundWithSVG() {
        StackPane container = new StackPane();
        container.setPadding(new Insets(20));
        container.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px;"
        );
        
        // Créer un SVGPath pour la zone remplie
        SVGPath areaSVG = new SVGPath();
        // Exemple de path SVG pour une courbe
        String svgPath = "M 40 160 L 100 100 L 160 120 L 220 40 L 280 80 L 340 20 L 360 160 Z";
        areaSVG.setContent(svgPath);
        
        // Appliquer un gradient
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1,
            true,
            null,
            new Stop(0, Color.web("#00E676", 0.4)),
            new Stop(1, Color.web("#00E676", 0.1))
        );
        areaSVG.setFill(gradient);
        areaSVG.setStroke(null);
        areaSVG.setOpacity(0.5);
        areaSVG.setMouseTransparent(true);
        
        // Contenu au premier plan
        VBox content = new VBox(10);
        content.setAlignment(Pos.TOP_LEFT);
        
        Label title = new Label("Total Profit:");
        title.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 18px; -fx-font-weight: 600;");
        
        Label date = new Label("February 2024");
        date.setStyle("-fx-text-fill: #8B92A8; -fx-font-size: 14px;");
        
        Label value = new Label("$136,755.77");
        value.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 32px; -fx-font-weight: 700;");
        
        content.getChildren().addAll(title, date, value);
        
        container.getChildren().addAll(areaSVG, content);
        
        return container;
    }
}

