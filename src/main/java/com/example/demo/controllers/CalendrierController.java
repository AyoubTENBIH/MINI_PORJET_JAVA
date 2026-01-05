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

    /**
     * Charge la vue du calendrier depuis le FXML
     */
    public Parent getView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/calendrier.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            
            // Charger le CSS du calendrier
            if (root.getScene() != null) {
                root.getScene().getStylesheets().add(
                    getClass().getResource("/css/calendrier.css").toExternalForm()
                );
            } else {
                root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        newScene.getStylesheets().add(
                            getClass().getResource("/css/calendrier.css").toExternalForm()
                        );
                    }
                });
            }
            
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
        // Configurer les event handlers pour les boutons de navigation
        if (prevMonthBtn != null) {
            prevMonthBtn.setOnAction(e -> {
                currentMonth = currentMonth.minusMonths(1);
                updateCalendar();
            });
        }
        if (nextMonthBtn != null) {
            nextMonthBtn.setOnAction(e -> {
                currentMonth = currentMonth.plusMonths(1);
                updateCalendar();
            });
        }
        if (todayBtn != null) {
            todayBtn.setOnAction(e -> {
                currentMonth = YearMonth.now();
                updateCalendar();
            });
        }
        
        // Mettre à jour le calendrier (les en-têtes sont déjà dans le FXML)
        updateCalendar();
    }


    /**
     * Vue de secours si le FXML ne charge pas
     */
    private Parent createBasicView() {
        // Créer une vue minimale en cas d'erreur de chargement FXML
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("calendar-root");
        
        Label errorLabel = new Label("Erreur lors du chargement de l'interface. Veuillez vérifier le fichier FXML.");
        root.getChildren().add(errorLabel);
        
        return root;
    }

    /**
     * Met à jour le calendrier avec les données des adhérents
     */
    private void updateCalendar() {
        // Nettoyer le calendrier (garder les en-têtes)
        if (calendarGrid != null) {
            calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);
        }

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

        if (calendarGrid == null) return;

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

    /**
     * Crée une cellule de jour avec les informations d'expiration
     */
    private VBox createDayCell(LocalDate date, List<Adherent> adherents) {
        VBox cell = new VBox();
        // Toutes les propriétés de taille, padding et spacing sont maintenant dans le CSS
        cell.getStyleClass().add("day-cell");

        // Numéro du jour
        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyleClass().add("day-number");

        // Marquer aujourd'hui
        boolean isToday = date.equals(LocalDate.now());
        if (isToday) {
            cell.getStyleClass().add("day-cell-today");
            dayNumber.getStyleClass().add("day-number-today");
        }

        cell.getChildren().add(dayNumber);

        // Compter les expirations pour ce jour
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
                cell.getStyleClass().add("day-cell-expired");
                HBox badge = createStatusBadge("#EF4444", expired + " expiré(s)", "expired");
                cell.getChildren().add(badge);
            } else if (expiringSoon > 0) {
                cell.getStyleClass().add("day-cell-expiring");
                HBox badge = createStatusBadge("#FF6B35", expiringSoon + " expire(nt)", "expiring");
                cell.getChildren().add(badge);
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
                tooltip.getStyleClass().add("tooltip");
                Tooltip.install(cell, tooltip);
            }
        }

        return cell;
    }
    
    /**
     * Crée un badge de statut moderne
     */
    private HBox createStatusBadge(String color, String text, String statusType) {
        HBox badge = new HBox();
        // Toutes les propriétés d'alignement, padding et spacing sont maintenant dans le CSS
        badge.getStyleClass().add("status-badge");
        
        if ("expired".equals(statusType)) {
            badge.getStyleClass().add("status-badge-expired");
        } else if ("expiring".equals(statusType)) {
            badge.getStyleClass().add("status-badge-expiring");
        }
        
        Circle circle = new Circle(3);
        if ("expired".equals(statusType)) {
            circle.setFill(Color.web("#EF4444"));
        } else if ("expiring".equals(statusType)) {
            circle.setFill(Color.web("#FF6B35"));
        } else {
            // Cas par défaut (non utilisé actuellement, mais gardé pour compatibilité)
            circle.setFill(Color.web(color));
        }
        
        Label label = new Label(text);
        if ("expired".equals(statusType)) {
            label.getStyleClass().add("status-label-expired");
        } else if ("expiring".equals(statusType)) {
            label.getStyleClass().add("status-label-expiring");
        } else {
            // Cas par défaut (non utilisé actuellement, mais gardé pour compatibilité)
            label.getStyleClass().add("status-label");
            // Utiliser une classe CSS au lieu de setStyle inline
            // Note: Si une couleur dynamique est nécessaire, créer une classe CSS spécifique
        }
        label.setWrapText(true);
        
        badge.getChildren().addAll(circle, label);
        return badge;
    }

    /**
     * Crée une cellule vide pour les jours hors du mois
     */
    private VBox createEmptyCell() {
        VBox cell = new VBox();
        // Toutes les propriétés de taille sont maintenant dans le CSS
        cell.getStyleClass().add("day-cell-empty");
        return cell;
    }
}
