package com.example.demo.controllers;

import com.example.demo.dao.AdherentDAO;
import com.example.demo.models.Adherent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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

    @FXML
    private GridPane calendarGrid;
    @FXML
    private Label monthLabel;
    @FXML
    private Button prevMonthBtn;
    @FXML
    private Button nextMonthBtn;
    @FXML
    private Button todayBtn;
    @FXML
    private HBox controlsBox;
    @FXML
    private HBox legendBox;
    @FXML
    private Label legendGreen;
    @FXML
    private Label legendOrange;
    @FXML
    private Label legendRed;

    public Parent getView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/calendrier.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            initialize();
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return createBasicView();
        }
    }

    /**
     * Initialise les composants après le chargement du FXML
     */
    @FXML
    private void initialize() {
        // Appliquer le fond sombre au container principal
        Parent root = monthLabel != null ? monthLabel.getParent() : null;
        if (root != null) {
            while (root != null && !(root instanceof VBox)) {
                root = root.getParent();
            }
            if (root != null) {
                root.setStyle("-fx-background-color: #0d0f1a;");
                
                // Styliser le titre de la page
                root.lookupAll(".page-title-large").forEach(node -> {
                    if (node instanceof Label) {
                        Label titleLabel = (Label) node;
                        titleLabel.setStyle(
                            "-fx-text-fill: #FFFFFF; " +
                            "-fx-font-size: 28px; " +
                            "-fx-font-weight: 700; " +
                            "-fx-font-family: 'Segoe UI', sans-serif;"
                        );
                    }
                });
            }
        }
        
        // Styliser les boutons de navigation
        if (prevMonthBtn != null) {
            styleNavigationButton(prevMonthBtn);
            prevMonthBtn.setOnAction(e -> {
                currentMonth = currentMonth.minusMonths(1);
                updateCalendar();
            });
        }
        if (nextMonthBtn != null) {
            styleNavigationButton(nextMonthBtn);
            nextMonthBtn.setOnAction(e -> {
                currentMonth = currentMonth.plusMonths(1);
                updateCalendar();
            });
        }
        if (todayBtn != null) {
            styleNavigationButton(todayBtn);
            todayBtn.setOnAction(e -> {
                currentMonth = YearMonth.now();
                updateCalendar();
            });
        }
        
        // Styliser le label du mois
        if (monthLabel != null) {
            monthLabel.setStyle(
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 20px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 0 20px;"
            );
        }
        
        // Configurer la légende avec design moderne
        if (legendBox != null) {
            styleLegendBox();
        }
        
        // Initialiser les en-têtes du calendrier
        if (calendarGrid != null) {
            setupCalendarHeaders();
            styleCalendarGrid();
        }
        
        updateCalendar();
    }
    
    /**
     * Stylise un bouton de navigation
     */
    private void styleNavigationButton(Button button) {
        String baseStyle = 
            "-fx-background-color: #1A2332; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-background-radius: 8px; " +
            "-fx-padding: 10px 20px; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 6, 0, 0, 2);";
        
        button.setStyle(baseStyle);
        
        button.setOnMouseEntered(e -> {
            button.setStyle(baseStyle + 
                " -fx-background-color: #2A3342; " +
                " -fx-translate-y: -1px;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle);
        });
    }
    
    /**
     * Stylise la boîte de légende
     */
    private void styleLegendBox() {
        legendBox.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 12px; " +
            "-fx-padding: 16px 24px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 6, 0, 0, 2); " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(255, 255, 255, 0.1); " +
            "-fx-border-radius: 12px;"
        );
        
        // Styliser le titre de la légende
        for (javafx.scene.Node node : legendBox.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getText().equals("Légende :")) {
                    label.setStyle(
                        "-fx-text-fill: #8B92A8; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 500;"
                    );
                } else {
                    // Remplacer les emojis par des indicateurs visuels
                    String text = label.getText();
                    if (text.contains("Actif")) {
                        createModernLegendItem(legendBox, label, "#00E676", "Actif");
                    } else if (text.contains("Expire bientôt")) {
                        createModernLegendItem(legendBox, label, "#FF6B35", "Expire bientôt");
                    } else if (text.contains("Expiré")) {
                        createModernLegendItem(legendBox, label, "#EF4444", "Expiré");
                    }
                }
            }
        }
    }
    
    /**
     * Crée un item de légende moderne avec cercle coloré
     */
    private void createModernLegendItem(HBox legendBox, Label oldLabel, String color, String text) {
        int index = legendBox.getChildren().indexOf(oldLabel);
        legendBox.getChildren().remove(oldLabel);
        
        HBox itemBox = new HBox(8);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        
        Circle circle = new Circle(4);
        circle.setFill(Color.web(color));
        
        Label newLabel = new Label(text);
        newLabel.setStyle(
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 400;"
        );
        
        itemBox.getChildren().addAll(circle, newLabel);
        legendBox.getChildren().add(index, itemBox);
    }
    
    /**
     * Stylise la grille du calendrier
     */
    private void styleCalendarGrid() {
        calendarGrid.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px; " +
            "-fx-padding: 16px; " +
            "-fx-hgap: 8px; " +
            "-fx-vgap: 8px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 12, 0, 0, 4);"
        );
    }

    /**
     * Configure les en-têtes du calendrier
     */
    private void setupCalendarHeaders() {
        if (calendarGrid == null) return;
        
        String[] joursSemaine = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label dayHeader = new Label(joursSemaine[i]);
            dayHeader.setStyle(
                "-fx-background-color: #1A2332; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 12px; " +
                "-fx-background-radius: 8px; " +
                "-fx-alignment: center;"
            );
            dayHeader.setPrefWidth(100);
            dayHeader.setPrefHeight(30);
            dayHeader.setAlignment(Pos.CENTER);
            GridPane.setHalignment(dayHeader, javafx.geometry.HPos.CENTER);
            calendarGrid.add(dayHeader, i, 0);
        }
    }

    /**
     * Vue de secours si le FXML ne charge pas
     */
    private Parent createBasicView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #0d0f1a;");

        // Titre
        Label title = new Label("Calendrier Dynamique");
        title.setStyle(
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 28px; " +
            "-fx-font-weight: 700; " +
            "-fx-font-family: 'Segoe UI', sans-serif;"
        );

        // Contrôles de navigation
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        Button prevMonthBtn = new Button("◀ Mois Précédent");
        styleNavigationButton(prevMonthBtn);
        prevMonthBtn.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendar();
        });

        Button nextMonthBtn = new Button("Mois Suivant ▶");
        styleNavigationButton(nextMonthBtn);
        nextMonthBtn.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendar();
        });

        Button todayBtn = new Button("Aujourd'hui");
        styleNavigationButton(todayBtn);
        todayBtn.setOnAction(e -> {
            currentMonth = YearMonth.now();
            updateCalendar();
        });

        monthLabel = new Label();
        monthLabel.setStyle(
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 20px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 0 20px;"
        );
        monthLabel.setId("monthLabel");

        controlsBox.getChildren().addAll(prevMonthBtn, todayBtn, nextMonthBtn, monthLabel);

        // Légende
        HBox legendBox = new HBox(20);
        legendBox.setPadding(new Insets(16, 24, 16, 24));
        legendBox.setAlignment(Pos.CENTER_LEFT);
        legendBox.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 12px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 6, 0, 0, 2); " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(255, 255, 255, 0.1); " +
            "-fx-border-radius: 12px;"
        );

        Label legendTitle = new Label("Légende :");
        legendTitle.setStyle(
            "-fx-text-fill: #8B92A8; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500;"
        );

        HBox activeItem = createModernLegendItemBox("#00E676", "Actif");
        HBox expiringItem = createModernLegendItemBox("#FF6B35", "Expire bientôt");
        HBox expiredItem = createModernLegendItemBox("#EF4444", "Expiré");

        legendBox.getChildren().addAll(legendTitle, activeItem, expiringItem, expiredItem);

        // Calendrier
        calendarGrid = new GridPane();
        calendarGrid.setHgap(8);
        calendarGrid.setVgap(8);
        calendarGrid.setPadding(new Insets(16));
        calendarGrid.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 12, 0, 0, 4);"
        );

        // En-têtes des jours
        String[] joursSemaine = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label dayHeader = new Label(joursSemaine[i]);
            dayHeader.setStyle(
                "-fx-background-color: #1A2332; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 12px; " +
                "-fx-background-radius: 8px; " +
                "-fx-alignment: center;"
            );
            dayHeader.setPrefWidth(130);
            dayHeader.setPrefHeight(30);
            dayHeader.setAlignment(Pos.CENTER);
            GridPane.setHalignment(dayHeader, javafx.geometry.HPos.CENTER);
            calendarGrid.add(dayHeader, i, 0);
        }

        root.getChildren().addAll(title, controlsBox, legendBox, calendarGrid);

        // Initialiser le calendrier
        updateCalendar();

        return root;
    }
    
    /**
     * Crée un item de légende moderne avec cercle coloré
     */
    private HBox createModernLegendItemBox(String color, String text) {
        HBox itemBox = new HBox(8);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        
        Circle circle = new Circle(4);
        circle.setFill(Color.web(color));
        
        Label label = new Label(text);
        label.setStyle(
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 400;"
        );
        
        itemBox.getChildren().addAll(circle, label);
        return itemBox;
    }

    private Label createLegendItem(String text, Color color) {
        Label label = new Label(text);
        label.setStyle(
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: 400;"
        );
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
        VBox cell = new VBox(3);
        cell.setPrefWidth(130);
        cell.setMinHeight(80);
        cell.setPrefHeight(80);
        cell.setPadding(new Insets(7, 3, 7, 3));
        
        String baseStyle = 
            "-fx-background-color: #1A2332; " +
            "-fx-border-color: rgba(255, 255, 255, 0.1); " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 4, 0, 0, 1);";
        
        cell.setStyle(baseStyle);

        // Numéro du jour
        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.setStyle(
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600;"
        );

        // Marquer aujourd'hui
        boolean isToday = date.equals(LocalDate.now());
        if (isToday) {
            cell.setStyle(
                "-fx-background-color: rgba(0, 230, 118, 0.15); " +
                "-fx-border-color: #00E676; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 230, 118, 0.3), 8, 0, 0, 2);"
            );
            dayNumber.setStyle(
                "-fx-text-fill: #00E676; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 700;"
            );
        }

        cell.getChildren().add(dayNumber);

        // Compter les expirations pour ce jour
        String statusColor = null;
        String statusBg = null;
        long expired = 0;
        long expiringSoon = 0;
        
        if (adherents != null) {
            expired = adherents.stream()
                .filter(a -> a.getDateFin() != null && a.getDateFin().equals(date))
                .count();

            expiringSoon = adherents.stream()
                .filter(a -> a.getDateFin() != null && 
                           a.getDateFin().isAfter(date) && 
                           a.getDateFin().isBefore(date.plusDays(8)) &&
                           !a.isAbonnementExpire())
                .count();

            if (expired > 0) {
                statusColor = "#EF4444";
                statusBg = "rgba(239, 68, 68, 0.1)";
                HBox badge = createStatusBadge("#EF4444", expired + " expiré(s)");
                cell.getChildren().add(badge);
            } else if (expiringSoon > 0) {
                statusColor = "#FF6B35";
                statusBg = "rgba(255, 107, 53, 0.1)";
                HBox badge = createStatusBadge("#FF6B35", expiringSoon + " expire(nt)");
                cell.getChildren().add(badge);
            }
            
            // Appliquer le background overlay selon le statut
            if (statusBg != null && !isToday) {
                cell.setStyle(baseStyle + " -fx-background-color: " + statusBg + ";");
            }
        }

        // Créer des copies finales pour les lambdas
        final String finalBaseStyle = baseStyle;
        final String finalStatusBg = statusBg;
        final boolean finalIsToday = isToday;

        // Effet hover
        cell.setOnMouseEntered(e -> {
            if (!finalIsToday) {
                cell.setStyle(
                    "-fx-background-color: #2A3342; " +
                    "-fx-border-color: rgba(255, 255, 255, 0.15); " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-radius: 8px; " +
                    "-fx-background-radius: 8px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.25), 6, 0, 0, 2); " +
                    "-fx-translate-y: -2px;"
                );
            }
        });
        
        cell.setOnMouseExited(e -> {
            if (!finalIsToday) {
                if (finalStatusBg != null) {
                    cell.setStyle(finalBaseStyle + " -fx-background-color: " + finalStatusBg + ";");
                } else {
                    cell.setStyle(finalBaseStyle);
                }
            }
        });

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
                tooltip.setStyle(
                    "-fx-background-color: #1A2332; " +
                    "-fx-text-fill: #E6EAF0; " +
                    "-fx-background-radius: 8px; " +
                    "-fx-padding: 12px; " +
                    "-fx-font-size: 13px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 10, 0, 0, 3); " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-color: rgba(0, 230, 118, 0.3); " +
                    "-fx-border-radius: 8px;"
                );
                Tooltip.install(cell, tooltip);
            }
        }

        return cell;
    }
    
    /**
     * Crée un badge de statut moderne
     */
    private HBox createStatusBadge(String color, String text) {
        HBox badge = new HBox(4);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(3, 6, 3, 6));
        badge.setStyle(
            "-fx-background-color: rgba(" + 
            (color.equals("#EF4444") ? "239, 68, 68" : 
             color.equals("#FF6B35") ? "255, 107, 53" : "0, 230, 118") + 
            ", 0.15); " +
            "-fx-background-radius: 6px;"
        );
        
        Circle circle = new Circle(3);
        circle.setFill(Color.web(color));
        
        Label label = new Label(text);
        label.setStyle(
            "-fx-text-fill: " + color + "; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: 600;"
        );
        label.setWrapText(true);
        
        badge.getChildren().addAll(circle, label);
        return badge;
    }

    private VBox createEmptyCell() {
        VBox cell = new VBox();
        cell.setPrefWidth(130);
        cell.setMinHeight(80);
        cell.setPrefHeight(80);
        cell.setStyle(
            "-fx-background-color: rgba(26, 35, 50, 0.3); " +
            "-fx-border-color: rgba(255, 255, 255, 0.05); " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;"
        );
        return cell;
    }
}
