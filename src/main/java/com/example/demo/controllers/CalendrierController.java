package com.example.demo.controllers;

import com.example.demo.dao.AdherentDAO;
import com.example.demo.models.Adherent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur pour le calendrier dynamique avec expirations d'abonnements
 */
public class CalendrierController {
    private AdherentDAO adherentDAO = new AdherentDAO();
    private YearMonth currentMonth = YearMonth.now();
    private GridPane calendarGrid;
    private Label monthLabel;

    public Parent getView() {
        VBox root = new VBox(20);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Titre
        Label title = new Label("Calendrier Dynamique");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Contrôles de navigation
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button prevMonthBtn = new Button("◀ Mois Précédent");
        prevMonthBtn.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendar();
        });

        Button nextMonthBtn = new Button("Mois Suivant ▶");
        nextMonthBtn.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendar();
        });

        Button todayBtn = new Button("Aujourd'hui");
        todayBtn.setOnAction(e -> {
            currentMonth = YearMonth.now();
            updateCalendar();
        });

        monthLabel = new Label();
        monthLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        monthLabel.setId("monthLabel");

        controlsBox.getChildren().addAll(prevMonthBtn, todayBtn, nextMonthBtn, monthLabel);

        // Légende
        HBox legendBox = new HBox(20);
        legendBox.setPadding(new javafx.geometry.Insets(10));
        legendBox.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        Label legendTitle = new Label("Légende :");
        legendTitle.setStyle("-fx-font-weight: bold;");

        Label legendGreen = createLegendItem("🟢 Actif", Color.GREEN);
        Label legendOrange = createLegendItem("🟠 Expire bientôt", Color.ORANGE);
        Label legendRed = createLegendItem("🔴 Expiré", Color.RED);

        legendBox.getChildren().addAll(legendTitle, legendGreen, legendOrange, legendRed);

        // Calendrier
        calendarGrid = new GridPane();
        calendarGrid.setHgap(2);
        calendarGrid.setVgap(2);
        calendarGrid.setPadding(new javafx.geometry.Insets(10));
        calendarGrid.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // En-têtes des jours
        String[] joursSemaine = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label dayHeader = new Label(joursSemaine[i]);
            dayHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-alignment: center;");
            dayHeader.setPrefWidth(100);
            dayHeader.setPrefHeight(30);
            dayHeader.setAlignment(javafx.geometry.Pos.CENTER);
            GridPane.setHalignment(dayHeader, javafx.geometry.HPos.CENTER);
            calendarGrid.add(dayHeader, i, 0);
        }

        root.getChildren().addAll(title, controlsBox, legendBox, calendarGrid);

        // Initialiser le calendrier
        updateCalendar();

        return root;
    }

    private Label createLegendItem(String text, Color color) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px;");
        return label;
    }

    private void updateCalendar() {
        // Nettoyer le calendrier (garder les en-têtes)
        calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        // Mettre à jour le label du mois
        if (monthLabel != null) {
            monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.FRENCH)));
        }

        // Charger les adhérents avec expirations
        List<Adherent> adherents = null;
        try {
            adherents = adherentDAO.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LocalDate firstDay = currentMonth.atDay(1);
        LocalDate lastDay = currentMonth.atEndOfMonth();
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue() - 1; // 0 = Lundi

        int row = 1;
        int col = firstDayOfWeek;

        // Remplir les jours du mois
        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            VBox dayCell = createDayCell(date, adherents);
            calendarGrid.add(dayCell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        // Remplir les cellules vides avant le premier jour
        for (int i = 0; i < firstDayOfWeek; i++) {
            VBox emptyCell = createEmptyCell();
            calendarGrid.add(emptyCell, i, 1);
        }
    }

    private VBox createDayCell(LocalDate date, List<Adherent> adherents) {
        VBox cell = new VBox(2);
        cell.setPrefWidth(100);
        cell.setPrefHeight(80);
        cell.setPadding(new javafx.geometry.Insets(5));
        cell.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: white;");

        // Numéro du jour
        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Marquer aujourd'hui
        if (date.equals(LocalDate.now())) {
            cell.setStyle("-fx-border-color: #3498db; -fx-border-width: 2; -fx-background-color: #e3f2fd;");
            dayNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #3498db;");
        }

        cell.getChildren().add(dayNumber);

        // Compter les expirations pour ce jour
        if (adherents != null) {
            long expired = adherents.stream()
                .filter(a -> a.getDateFin() != null && a.getDateFin().equals(date))
                .count();

            long expiringSoon = adherents.stream()
                .filter(a -> a.getDateFin() != null && 
                           a.getDateFin().isAfter(date) && 
                           a.getDateFin().isBefore(date.plusDays(8)) &&
                           !a.isAbonnementExpire())
                .count();

            if (expired > 0) {
                Label expiredLabel = new Label("🔴 " + expired + " expiré(s)");
                expiredLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: red; -fx-font-weight: bold;");
                expiredLabel.setWrapText(true);
                cell.getChildren().add(expiredLabel);
                cell.setStyle(cell.getStyle() + " -fx-background-color: #ffebee;");
            }

            if (expiringSoon > 0 && expired == 0) {
                Label expiringLabel = new Label("🟠 " + expiringSoon + " expire(nt)");
                expiringLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: orange; -fx-font-weight: bold;");
                expiringLabel.setWrapText(true);
                cell.getChildren().add(expiringLabel);
                cell.setStyle(cell.getStyle() + " -fx-background-color: #fff3e0;");
            }
        }

        // Tooltip avec détails
        if (adherents != null) {
            List<Adherent> dayAdherents = adherents.stream()
                .filter(a -> a.getDateFin() != null && 
                           (a.getDateFin().equals(date) || 
                            (a.getDateFin().isAfter(date) && a.getDateFin().isBefore(date.plusDays(8)))))
                .toList();

            if (!dayAdherents.isEmpty()) {
                StringBuilder tooltipText = new StringBuilder("Abonnements :\n");
                for (Adherent a : dayAdherents) {
                    tooltipText.append("• ").append(a.getNomComplet())
                               .append(" - ").append(a.getDateFin()).append("\n");
                }
                Tooltip tooltip = new Tooltip(tooltipText.toString());
                Tooltip.install(cell, tooltip);
            }
        }

        return cell;
    }

    private VBox createEmptyCell() {
        VBox cell = new VBox();
        cell.setPrefWidth(100);
        cell.setPrefHeight(80);
        cell.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 1; -fx-background-color: #fafafa;");
        return cell;
    }
}
