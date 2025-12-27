package com.example.demo.controllers;

import com.example.demo.dao.AdherentDAO;
import com.example.demo.dao.PackDAO;
import com.example.demo.dao.PaiementDAO;
import com.example.demo.models.Adherent;
import com.example.demo.models.Paiement;
import com.example.demo.models.Pack;
import com.example.demo.services.NotificationService;
import com.example.demo.services.ActivityService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.DialogPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des paiements avec liste rouge
 */
public class PaiementManagementController {
    private PaiementDAO paiementDAO = new PaiementDAO();
    private AdherentDAO adherentDAO = new AdherentDAO();
    private PackDAO packDAO = new PackDAO();
    private NotificationService notificationService = NotificationService.getInstance();
    private ActivityService activityService = ActivityService.getInstance();
    private ObservableList<Paiement> paiementsList = FXCollections.observableArrayList();
    private ObservableList<Adherent> redList = FXCollections.observableArrayList();

    @FXML
    private HBox navigationContainer;
    @FXML
    private Button navListeRouge;
    @FXML
    private Button navTousPaiements;
    @FXML
    private Button navNouveauPaiement;
    @FXML
    private VBox contentContainer;
    @FXML
    private TableView<Paiement> paiementsTable;
    @FXML
    private TableView<Adherent> redListTable;
    @FXML
    private HBox statsBox;
    @FXML
    private VBox redListContainer;
    @FXML
    private VBox paymentsContainer;
    @FXML
    private VBox newPaymentContainer;
    @FXML
    private Button payerButton;
    @FXML
    private Button refreshRedListButton;
    @FXML
    private TextField searchPaymentsField;
    @FXML
    private Button refreshPaymentsButton;
    @FXML
    private Button newPaymentButton;
    
    private String currentView = "listeRouge";

    /**
     * Charge la vue de gestion des paiements depuis FXML
     */
    public Parent getView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/paiement.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            initialize();
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: créer la vue programmatiquement si FXML échoue
            return createBasicView();
        }
    }

    /**
     * Initialise les services avec l'ID de l'utilisateur connecté
     */
    private void initializeServices() {
        try {
            com.example.demo.models.Utilisateur currentUser = LoginController.getCurrentUser();
            if (currentUser != null) {
                notificationService.setCurrentUserId(currentUser.getId());
                activityService.setCurrentUserId(currentUser.getId());
            } else {
                // Si aucun utilisateur connecté, utiliser l'ID par défaut (admin)
                notificationService.setCurrentUserId(1);
                activityService.setCurrentUserId(1);
            }
        } catch (Exception e) {
            // En cas d'erreur, utiliser l'ID par défaut
            notificationService.setCurrentUserId(1);
            activityService.setCurrentUserId(1);
        }
    }

    /**
     * Initialise les composants après le chargement du FXML
     */
    @FXML
    private void initialize() {
        // Initialiser les services
        initializeServices();
        
        // Initialiser les statistiques
        if (statsBox != null) {
            statsBox.getChildren().clear();
            HBox stats = createStatsBox();
            statsBox.getChildren().addAll(stats.getChildren());
        }
        
        // Configurer les vues de contenu (tables doivent être configurées AVANT le chargement des données)
        setupContentViews();
        
        // Configurer les boutons de navigation
        setupNavigationButtons();
        
        // Charger les données (après configuration des tables)
        Platform.runLater(() -> {
            loadRedList();
            loadPayments();
        });
        
        // Afficher la vue par défaut
        switchView("listeRouge");
    }
    
    /**
     * Configure les boutons de navigation avec styles personnalisés
     */
    private void setupNavigationButtons() {
        if (navListeRouge != null) {
            navListeRouge.setOnAction(e -> switchView("listeRouge"));
        }
        if (navTousPaiements != null) {
            navTousPaiements.setOnAction(e -> switchView("tousPaiements"));
        }
        if (navNouveauPaiement != null) {
            navNouveauPaiement.setOnAction(e -> switchView("nouveauPaiement"));
        }
        
        // Mettre à jour les styles des boutons
        updateNavigationStyles();
    }
    
    /**
     * Met à jour les styles des boutons de navigation selon la vue active
     */
    private void updateNavigationStyles() {
        String activeStyle = 
            "-fx-background-color: linear-gradient(to bottom, #00E676, #00C96A); " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: 700; " +
            "-fx-padding: 12px 24px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 230, 118, 0.6), 12, 0, 0, 4);";
        
        String inactiveStyle = 
            "-fx-background-color: #1E2329; " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #B0B8C4; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 12px 24px; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-color: rgba(158, 255, 0, 0.2); " +
            "-fx-border-radius: 12px;";
        
        String inactiveHoverStyle = 
            "-fx-background-color: #2A3140; " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 12px 24px; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-color: rgba(0, 230, 118, 0.4); " +
            "-fx-border-radius: 12px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 8, 0, 0, 2);";
        
        String activeHoverStyle = 
            "-fx-background-color: linear-gradient(to bottom, #00F57C, #00E676); " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: 700; " +
            "-fx-padding: 12px 24px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 230, 118, 0.8), 15, 0, 0, 5);";
        
        if (navListeRouge != null) {
            if ("listeRouge".equals(currentView)) {
                navListeRouge.setStyle(activeStyle);
                navListeRouge.setOnMouseEntered(e -> navListeRouge.setStyle(activeHoverStyle));
                navListeRouge.setOnMouseExited(e -> navListeRouge.setStyle(activeStyle));
            } else {
                navListeRouge.setStyle(inactiveStyle);
                navListeRouge.setOnMouseEntered(e -> navListeRouge.setStyle(inactiveHoverStyle));
                navListeRouge.setOnMouseExited(e -> navListeRouge.setStyle(inactiveStyle));
            }
        }
        
        if (navTousPaiements != null) {
            if ("tousPaiements".equals(currentView)) {
                navTousPaiements.setStyle(activeStyle);
                navTousPaiements.setOnMouseEntered(e -> navTousPaiements.setStyle(activeHoverStyle));
                navTousPaiements.setOnMouseExited(e -> navTousPaiements.setStyle(activeStyle));
            } else {
                navTousPaiements.setStyle(inactiveStyle);
                navTousPaiements.setOnMouseEntered(e -> navTousPaiements.setStyle(inactiveHoverStyle));
                navTousPaiements.setOnMouseExited(e -> navTousPaiements.setStyle(inactiveStyle));
            }
        }
        
        if (navNouveauPaiement != null) {
            if ("nouveauPaiement".equals(currentView)) {
                navNouveauPaiement.setStyle(activeStyle);
                navNouveauPaiement.setOnMouseEntered(e -> navNouveauPaiement.setStyle(activeHoverStyle));
                navNouveauPaiement.setOnMouseExited(e -> navNouveauPaiement.setStyle(activeStyle));
            } else {
                navNouveauPaiement.setStyle(inactiveStyle);
                navNouveauPaiement.setOnMouseEntered(e -> navNouveauPaiement.setStyle(inactiveHoverStyle));
                navNouveauPaiement.setOnMouseExited(e -> navNouveauPaiement.setStyle(inactiveStyle));
            }
        }
    }
    
    /**
     * Change de vue
     */
    private void switchView(String viewName) {
        if (viewName.equals(currentView)) return;
        
        currentView = viewName;
        
        // Afficher/masquer les conteneurs appropriés
        if (redListContainer != null) {
            boolean showRedList = "listeRouge".equals(viewName);
            redListContainer.setVisible(showRedList);
            redListContainer.setManaged(showRedList);
        }
        
        if (paymentsContainer != null) {
            boolean showPayments = "tousPaiements".equals(viewName);
            paymentsContainer.setVisible(showPayments);
            paymentsContainer.setManaged(showPayments);
        }
        
        if (newPaymentContainer != null) {
            boolean showNewPayment = "nouveauPaiement".equals(viewName);
            newPaymentContainer.setVisible(showNewPayment);
            newPaymentContainer.setManaged(showNewPayment);
        }
        
        // Mettre à jour les styles des boutons
        updateNavigationStyles();
    }
    
    /**
     * Configure les vues de contenu (tables, boutons, etc.)
     */
    private void setupContentViews() {
        // Configurer la vue Liste Rouge (même si le conteneur est caché, la table doit être configurée)
        if (redListTable != null) {
            setupRedListTable();
        }
        
        // Configurer les boutons de la vue Liste Rouge
        if (redListContainer != null) {
            if (payerButton != null) {
                payerButton.setStyle(
                    "-fx-background-color: #00E676; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: 600; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-cursor: hand;"
                );
                payerButton.setOnMouseEntered(e -> payerButton.setStyle(
                    "-fx-background-color: #16A34A; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: 600; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-cursor: hand;"
                ));
                payerButton.setOnMouseExited(e -> payerButton.setStyle(
                    "-fx-background-color: #00E676; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: 600; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-cursor: hand;"
                ));
                payerButton.setOnAction(e -> {
                    Adherent selected = redListTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        showNewPaymentDialog(selected);
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un adhérent");
                    }
                });
            }
            if (refreshRedListButton != null) {
                refreshRedListButton.setStyle(
                    "-fx-background-color: #1c1e2d; " +
                    "-fx-text-fill: #E6EAF0; " +
                    "-fx-font-weight: 600; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-color: rgba(158, 255, 0, 0.2); " +
                    "-fx-border-radius: 10px; " +
                    "-fx-cursor: hand;"
                );
                refreshRedListButton.setOnMouseEntered(e -> refreshRedListButton.setStyle(
                    "-fx-background-color: #2f3640; " +
                    "-fx-text-fill: #E6EAF0; " +
                    "-fx-font-weight: 600; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-color: rgba(158, 255, 0, 0.2); " +
                    "-fx-border-radius: 10px; " +
                    "-fx-cursor: hand;"
                ));
                refreshRedListButton.setOnMouseExited(e -> refreshRedListButton.setStyle(
                    "-fx-background-color: #1c1e2d; " +
                    "-fx-text-fill: #E6EAF0; " +
                    "-fx-font-weight: 600; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-color: rgba(158, 255, 0, 0.2); " +
                    "-fx-border-radius: 10px; " +
                    "-fx-cursor: hand;"
                ));
                refreshRedListButton.setOnAction(e -> loadRedList());
            }
        }
        
        // Configurer la vue Tous les Paiements (même si le conteneur est caché)
        if (paiementsTable != null) {
            setupPaymentsTable();
            if (searchPaymentsField != null) {
                searchPaymentsField.setStyle(
                    "-fx-background-color: #0A0D12; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-text-fill: #E6EAF0; " +
                    "-fx-font-size: 14px; " +
                    "-fx-padding: 12px 16px; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-color: rgba(158, 255, 0, 0.2); " +
                    "-fx-border-radius: 10px; " +
                    "-fx-prompt-text-fill: #9AA4B2;"
                );
                searchPaymentsField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (isFocused) {
                        searchPaymentsField.setStyle(
                            "-fx-background-color: #141A22; " +
                            "-fx-background-radius: 10px; " +
                            "-fx-text-fill: #E6EAF0; " +
                            "-fx-font-size: 14px; " +
                            "-fx-padding: 12px 16px; " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-color: #00E676; " +
                            "-fx-border-radius: 10px; " +
                            "-fx-prompt-text-fill: #9AA4B2;"
                        );
                    } else {
                        searchPaymentsField.setStyle(
                            "-fx-background-color: #0A0D12; " +
                            "-fx-background-radius: 10px; " +
                            "-fx-text-fill: #E6EAF0; " +
                            "-fx-font-size: 14px; " +
                            "-fx-padding: 12px 16px; " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-color: rgba(158, 255, 0, 0.2); " +
                            "-fx-border-radius: 10px; " +
                            "-fx-prompt-text-fill: #9AA4B2;"
                        );
                    }
                });
                searchPaymentsField.textProperty().addListener((obs, oldVal, newVal) -> {
                    // TODO: Implémenter la recherche
                    loadPayments();
                });
            }
            if (refreshPaymentsButton != null) {
                refreshPaymentsButton.setStyle(
                    "-fx-background-color: #1c1e2d; " +
                    "-fx-text-fill: #E6EAF0; " +
                    "-fx-font-weight: 600; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-color: rgba(158, 255, 0, 0.2); " +
                    "-fx-border-radius: 10px; " +
                    "-fx-cursor: hand;"
                );
                refreshPaymentsButton.setOnMouseEntered(e -> refreshPaymentsButton.setStyle(
                    "-fx-background-color: #2f3640; " +
                    "-fx-text-fill: #E6EAF0; " +
                    "-fx-font-weight: 600; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-color: rgba(158, 255, 0, 0.2); " +
                    "-fx-border-radius: 10px; " +
                    "-fx-cursor: hand;"
                ));
                refreshPaymentsButton.setOnMouseExited(e -> refreshPaymentsButton.setStyle(
                    "-fx-background-color: #1c1e2d; " +
                    "-fx-text-fill: #E6EAF0; " +
                    "-fx-font-weight: 600; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-color: rgba(158, 255, 0, 0.2); " +
                    "-fx-border-radius: 10px; " +
                    "-fx-cursor: hand;"
                ));
                refreshPaymentsButton.setOnAction(e -> loadPayments());
            }
        }
        
        // Configurer la vue Nouveau Paiement
        if (newPaymentButton != null) {
            newPaymentButton.setStyle(
                "-fx-background-color: #00E676; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 15px 30px; " +
                "-fx-background-radius: 10px; " +
                "-fx-cursor: hand;"
            );
            newPaymentButton.setOnMouseEntered(e -> newPaymentButton.setStyle(
                "-fx-background-color: #16A34A; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 15px 30px; " +
                "-fx-background-radius: 10px; " +
                "-fx-cursor: hand;"
            ));
            newPaymentButton.setOnMouseExited(e -> newPaymentButton.setStyle(
                "-fx-background-color: #00E676; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 15px 30px; " +
                "-fx-background-radius: 10px; " +
                "-fx-cursor: hand;"
            ));
            newPaymentButton.setOnAction(e -> showNewPaymentDialog(null));
        }
    }

    /**
     * Vue de secours si le FXML ne charge pas
     */
    private Parent createBasicView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0d0f1a;"); // Background sombre cohérent avec Dashboard

        Label title = new Label("Gestion des Paiements & Cotisations");
        title.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 24px; -fx-font-weight: 700;");

        HBox statsBox = createStatsBox();
        
        // Créer les boutons de navigation
        HBox navContainer = new HBox(12);
        navContainer.setPadding(new Insets(10, 0, 10, 0));
        
        Button navListeRouge = createNavButton("🔴 Liste Rouge", true);
        Button navTousPaiements = createNavButton("💳 Tous les Paiements", false);
        Button navNouveauPaiement = createNavButton("➕ Nouveau Paiement", false);
        
        navContainer.getChildren().addAll(navListeRouge, navTousPaiements, navNouveauPaiement);
        
        // Conteneur de contenu
        VBox contentContainer = new VBox(15);
        contentContainer.setPadding(new Insets(20));
        contentContainer.setStyle("-fx-background-color: #0d0f1a;");
        
        Parent redListView = createRedListView();
        Parent paymentsView = createPaymentsView();
        Parent newPaymentView = createNewPaymentView();
        
        redListView.setVisible(true);
        paymentsView.setVisible(false);
        newPaymentView.setVisible(false);
        
        contentContainer.getChildren().addAll(redListView, paymentsView, newPaymentView);
        
        // Actions des boutons
        navListeRouge.setOnAction(e -> {
            redListView.setVisible(true);
            paymentsView.setVisible(false);
            newPaymentView.setVisible(false);
            updateNavButtonStyles(navListeRouge, navTousPaiements, navNouveauPaiement, true, false, false);
        });
        
        navTousPaiements.setOnAction(e -> {
            redListView.setVisible(false);
            paymentsView.setVisible(true);
            newPaymentView.setVisible(false);
            updateNavButtonStyles(navListeRouge, navTousPaiements, navNouveauPaiement, false, true, false);
        });
        
        navNouveauPaiement.setOnAction(e -> {
            redListView.setVisible(false);
            paymentsView.setVisible(false);
            newPaymentView.setVisible(true);
            updateNavButtonStyles(navListeRouge, navTousPaiements, navNouveauPaiement, false, false, true);
        });
        
        root.getChildren().addAll(title, statsBox, navContainer, contentContainer);
        
        loadRedList();
        loadPayments();
        
        return root;
    }
    
    /**
     * Crée un bouton de navigation pour la vue de secours
     */
    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(56);
        
        if (active) {
            btn.setStyle(
                "-fx-background-color: #00E676; " +
                "-fx-background-radius: 12px; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 15px; " +
                "-fx-font-weight: 700; " +
                "-fx-padding: 14px 24px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 230, 118, 0.5), 10, 0, 0, 3);"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: #2A2F3A; " +
                "-fx-background-radius: 12px; " +
                "-fx-text-fill: #E6EAF0; " +
                "-fx-font-size: 15px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 14px 24px; " +
                "-fx-cursor: hand; " +
                "-fx-border-width: 1px; " +
                "-fx-border-color: rgba(158, 255, 0, 0.15); " +
                "-fx-border-radius: 12px;"
            );
        }
        
        return btn;
    }
    
    /**
     * Met à jour les styles des boutons de navigation pour la vue de secours
     */
    private void updateNavButtonStyles(Button btn1, Button btn2, Button btn3, 
                                      boolean active1, boolean active2, boolean active3) {
        String activeStyle = 
            "-fx-background-color: #00E676; " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: 700; " +
            "-fx-padding: 14px 24px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 230, 118, 0.5), 10, 0, 0, 3);";
        
        String inactiveStyle = 
            "-fx-background-color: #2A2F3A; " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 14px 24px; " +
            "-fx-cursor: hand; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(158, 255, 0, 0.15); " +
            "-fx-border-radius: 12px;";
        
        btn1.setStyle(active1 ? activeStyle : inactiveStyle);
        btn2.setStyle(active2 ? activeStyle : inactiveStyle);
        btn3.setStyle(active3 ? activeStyle : inactiveStyle);
    }

    /**
     * Crée la boîte de statistiques
     */
    private HBox createStatsBox() {
        HBox statsBox = new HBox(16); // Espacement horizontal 16px comme Dashboard
        statsBox.setPadding(new Insets(0));
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setStyle("-fx-background-color: transparent;");
        HBox.setHgrow(statsBox, Priority.ALWAYS);
        
        VBox revenusWidget = createStatWidget("Revenus du Mois", "0 DH", "#00E676"); // Vert accent cohérent
        VBox impayesWidget = createStatWidget("Impayés", "0", "#EF4444"); // Rouge danger cohérent
        VBox expireBientotWidget = createStatWidget("Expire Bientôt", "0", "#F59E0B"); // Orange warning cohérent
        
        // Permettre aux cartes de grandir
        HBox.setHgrow(revenusWidget, Priority.ALWAYS);
        HBox.setHgrow(impayesWidget, Priority.ALWAYS);
        HBox.setHgrow(expireBientotWidget, Priority.ALWAYS);
        
        statsBox.getChildren().addAll(revenusWidget, impayesWidget, expireBientotWidget);
        
        // Mettre à jour les stats
        updateStats(revenusWidget, impayesWidget, expireBientotWidget);
        
        return statsBox;
    }

    private VBox createStatWidget(String title, String value, String color) {
        VBox widget = new VBox(8); // Spacing vertical modéré comme Dashboard
        widget.setPadding(new Insets(12, 20, 12, 20)); // Padding cohérent avec Dashboard
        // Dimensions flexibles pour remplir l'espace disponible
        widget.setMinWidth(220); // Largeur minimale comme Dashboard
        widget.setPrefWidth(Region.USE_COMPUTED_SIZE); // Utiliser la largeur calculée
        widget.setMaxWidth(Double.MAX_VALUE); // Permettre l'expansion maximale
        widget.setMinHeight(140);
        widget.setPrefHeight(140);
        widget.setMaxHeight(140);
        widget.setStyle(
            "-fx-background-color: #1c1e2d; " +
            "-fx-background-radius: 10px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0, 0, 2);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #9AA4B2; -fx-font-size: 12px; -fx-font-weight: 500;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 32px; -fx-font-weight: 700;");
        valueLabel.setId("stat-value-" + title.replaceAll("\\s", "-"));
        
        widget.getChildren().addAll(titleLabel, valueLabel);
        return widget;
    }

    private void updateStats(VBox revenusWidget, VBox impayesWidget, VBox expireBientotWidget) {
        try {
            // Revenus du mois
            Double revenus = paiementDAO.getRevenusMois(LocalDate.now());
            Label revenusLabel = (Label) revenusWidget.getChildren().get(1);
            revenusLabel.setText(String.format("%.2f DH", revenus));
            
            // Impayés (adhérents expirés)
            List<Adherent> expired = adherentDAO.findExpired();
            Label impayesLabel = (Label) impayesWidget.getChildren().get(1);
            impayesLabel.setText(String.valueOf(expired.size()));
            
            // Expire bientôt
            List<Adherent> expiringSoon = adherentDAO.findExpiringSoon();
            Label expireLabel = (Label) expireBientotWidget.getChildren().get(1);
            expireLabel.setText(String.valueOf(expiringSoon.size()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Crée la vue de la liste rouge
     */
    private Parent createRedListView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0d0f1a;");
        
        Label subtitle = new Label("Adhérents avec abonnement expiré ou impayé");
        subtitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #EF4444;");
        
        // Table de la liste rouge
        redListTable = new TableView<>();
        redListTable.setPrefHeight(500);
        
        // Style pour les headers de colonnes
        String headerStyle = 
            "-fx-background-color: #0A0D12; " +
            "-fx-text-fill: #9AA4B2; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 0 0 1px 0; " +
            "-fx-border-color: rgba(158, 255, 0, 0.1);";
        
        TableColumn<Adherent, String> cinColumn = new TableColumn<>("CIN");
        cinColumn.setStyle(headerStyle);
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        cinColumn.setPrefWidth(120);
        
        TableColumn<Adherent, String> nomColumn = new TableColumn<>("Nom Complet");
        nomColumn.setStyle(headerStyle);
        nomColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomComplet()));
        nomColumn.setPrefWidth(200);
        
        TableColumn<Adherent, String> telephoneColumn = new TableColumn<>("Téléphone");
        telephoneColumn.setStyle(headerStyle);
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        telephoneColumn.setPrefWidth(120);
        
        TableColumn<Adherent, String> dateFinColumn = new TableColumn<>("Date Expiration");
        dateFinColumn.setStyle(headerStyle);
        dateFinColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateFin() != null ? 
                    cellData.getValue().getDateFin().toString() : "N/A"));
        dateFinColumn.setMinWidth(130);
        dateFinColumn.setPrefWidth(180);
        dateFinColumn.setMaxWidth(250);
        dateFinColumn.setResizable(true);
        
        TableColumn<Adherent, Integer> joursRetardColumn = new TableColumn<>("Jours de Retard");
        joursRetardColumn.setStyle(headerStyle);
        joursRetardColumn.setCellValueFactory(cellData -> {
            Adherent adherent = cellData.getValue();
            if (adherent.getDateFin() != null && adherent.isAbonnementExpire()) {
                long jours = java.time.temporal.ChronoUnit.DAYS.between(adherent.getDateFin(), LocalDate.now());
                return new javafx.beans.property.ReadOnlyObjectWrapper<>((int) jours);
            }
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(0);
        });
        joursRetardColumn.setPrefWidth(120);
        joursRetardColumn.setCellFactory(column -> new TableCell<Adherent, Integer>() {
            @Override
            protected void updateItem(Integer jours, boolean empty) {
                super.updateItem(jours, empty);
                if (empty || jours == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(String.valueOf(jours));
                    // Styles avec le thème sombre
                    if (jours > 30) {
                        setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                    } else if (jours > 7) {
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #FCD34D; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        redListTable.getColumns().addAll(cinColumn, nomColumn, telephoneColumn, dateFinColumn, joursRetardColumn);
        redListTable.setItems(redList);
        
        // Bouton action rapide
        Button payerButton = new Button("Enregistrer Paiement");
        payerButton.setStyle(
            "-fx-background-color: #00E676; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-background-radius: 10px; " +
            "-fx-padding: 10px 20px; " +
            "-fx-cursor: hand;"
        );
        payerButton.setOnMouseEntered(e -> payerButton.setStyle(
            "-fx-background-color: #16A34A; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-background-radius: 10px; " +
            "-fx-padding: 10px 20px; " +
            "-fx-cursor: hand;"
        ));
        payerButton.setOnMouseExited(e -> payerButton.setStyle(
            "-fx-background-color: #00E676; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-background-radius: 10px; " +
            "-fx-padding: 10px 20px; " +
            "-fx-cursor: hand;"
        ));
        payerButton.setOnAction(e -> {
            Adherent selected = redListTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showNewPaymentDialog(selected);
            } else {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un adhérent");
            }
        });
        
        Button refreshButton = new Button("Actualiser");
        refreshButton.setStyle(
            "-fx-background-color: #1c1e2d; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-weight: 600; " +
            "-fx-background-radius: 10px; " +
            "-fx-padding: 10px 20px; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(158, 255, 0, 0.2); " +
            "-fx-border-radius: 10px; " +
            "-fx-cursor: hand;"
        );
        refreshButton.setOnMouseEntered(e -> refreshButton.setStyle(
            "-fx-background-color: #2f3640; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-weight: 600; " +
            "-fx-background-radius: 10px; " +
            "-fx-padding: 10px 20px; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(158, 255, 0, 0.2); " +
            "-fx-border-radius: 10px; " +
            "-fx-cursor: hand;"
        ));
        refreshButton.setOnMouseExited(e -> refreshButton.setStyle(
            "-fx-background-color: #1c1e2d; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-weight: 600; " +
            "-fx-background-radius: 10px; " +
            "-fx-padding: 10px 20px; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(158, 255, 0, 0.2); " +
            "-fx-border-radius: 10px; " +
            "-fx-cursor: hand;"
        ));
        refreshButton.setOnAction(e -> loadRedList());
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(payerButton, refreshButton);
        
        root.getChildren().addAll(subtitle, buttonBox, redListTable);
        
        return root;
    }

    /**
     * Crée la vue de tous les paiements
     */
    private Parent createPaymentsView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0d0f1a;");
        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);
        
        // Top bar avec recherche et bouton actualiser
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher par adhérent, montant...");
        searchField.setPrefWidth(400);
        searchField.setStyle(getInputStyle());
        searchField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                searchField.setStyle(
                    "-fx-background-color: rgba(15, 23, 42, 0.8); " +
                    "-fx-background-radius: 12px; " +
                    "-fx-text-fill: #E6EAF0; " +
                    "-fx-font-size: 14px; " +
                    "-fx-padding: 12px 16px; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-color: rgba(16, 185, 129, 0.5); " +
                    "-fx-border-radius: 12px; " +
                    "-fx-prompt-text-fill: #9AA4B2;"
                );
            } else {
                searchField.setStyle(getInputStyle());
            }
        });
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                loadPayments();
            } else {
                searchPayments(newVal);
            }
        });
        
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Button refreshButton = new Button("Actualiser");
        refreshButton.setStyle(
            "-fx-background-color: #1c1e2d; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-weight: 600; " +
            "-fx-background-radius: 12px; " +
            "-fx-padding: 12px 24px; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(158, 255, 0, 0.2); " +
            "-fx-border-radius: 12px; " +
            "-fx-cursor: hand;"
        );
        refreshButton.setOnMouseEntered(e -> refreshButton.setStyle(
            "-fx-background-color: #2f3640; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-weight: 600; " +
            "-fx-background-radius: 12px; " +
            "-fx-padding: 12px 24px; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(158, 255, 0, 0.2); " +
            "-fx-border-radius: 12px; " +
            "-fx-cursor: hand;"
        ));
        refreshButton.setOnMouseExited(e -> refreshButton.setStyle(
            "-fx-background-color: #1c1e2d; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-weight: 600; " +
            "-fx-background-radius: 12px; " +
            "-fx-padding: 12px 24px; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(158, 255, 0, 0.2); " +
            "-fx-border-radius: 12px; " +
            "-fx-cursor: hand;"
        ));
        refreshButton.setOnAction(e -> loadPayments());
        
        topBar.getChildren().addAll(searchField, refreshButton);
        
        // Container pour la table avec card style
        VBox tableCard = new VBox(0);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0, 0, 2);"
        );
        tableCard.setMinHeight(400);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        
        // Table des paiements
        paiementsTable = new TableView<>();
        paiementsTable.setPrefHeight(600);
        paiementsTable.setMinHeight(400);
        paiementsTable.setMaxHeight(800);
        paiementsTable.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-table-cell-border-color: transparent;"
        );
        
        // Appeler setupPaymentsTable pour configurer la table avec tous les styles
        setupPaymentsTable();
        
        tableCard.getChildren().add(paiementsTable);
        
        root.getChildren().addAll(topBar, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        
        // Charger les paiements
        loadPayments();
        
        return root;
    }

    /**
     * Crée la vue de nouveau paiement
     */
    private Parent createNewPaymentView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setPrefWidth(600);
        root.setStyle("-fx-background-color: #0d0f1a;");
        
        Label subtitle = new Label("Enregistrer un nouveau paiement");
        subtitle.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 18px; -fx-font-weight: 700;");
        
        Button newPaymentButton = new Button("Nouveau Paiement");
        newPaymentButton.setStyle(
            "-fx-background-color: #00E676; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 15px 30px; " +
            "-fx-background-radius: 10px; " +
            "-fx-cursor: hand;"
        );
        newPaymentButton.setOnMouseEntered(e -> newPaymentButton.setStyle(
            "-fx-background-color: #16A34A; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 15px 30px; " +
            "-fx-background-radius: 10px; " +
            "-fx-cursor: hand;"
        ));
        newPaymentButton.setOnMouseExited(e -> newPaymentButton.setStyle(
            "-fx-background-color: #00E676; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 15px 30px; " +
            "-fx-background-radius: 10px; " +
            "-fx-cursor: hand;"
        ));
        newPaymentButton.setOnAction(e -> showNewPaymentDialog(null));
        
        root.getChildren().addAll(subtitle, newPaymentButton);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        
        return root;
    }

    /**
     * Configure la table de la liste rouge
     */
    private void setupRedListTable() {
        if (redListTable == null) return;
        
        redListTable.getColumns().clear();
        redListTable.setPrefHeight(600);
        redListTable.setMinHeight(400);
        redListTable.setMaxHeight(800);
        // Appliquer le style sombre à la table
        redListTable.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 12px; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(158, 255, 0, 0.1); " +
            "-fx-border-radius: 12px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 15, 0, 0, 3); " +
            "-fx-table-cell-border-color: transparent;"
        );
        
        // Créer un style pour les headers de colonnes
        String headerStyle = 
            "-fx-background-color: #0A0D12; " +
            "-fx-text-fill: #9AA4B2; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 0 0 1px 0; " +
            "-fx-border-color: rgba(158, 255, 0, 0.1);";
        
        TableColumn<Adherent, String> cinColumn = new TableColumn<>("CIN");
        cinColumn.setStyle(headerStyle);
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        cinColumn.setMinWidth(100);
        cinColumn.setPrefWidth(120);
        cinColumn.setMaxWidth(150);
        cinColumn.setResizable(true);
        
        TableColumn<Adherent, String> nomColumn = new TableColumn<>("Nom Complet");
        nomColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomComplet()));
        nomColumn.setMinWidth(150);
        nomColumn.setPrefWidth(250);
        nomColumn.setMaxWidth(400);
        nomColumn.setResizable(true);
        
        TableColumn<Adherent, String> telephoneColumn = new TableColumn<>("Téléphone");
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        telephoneColumn.setMinWidth(100);
        telephoneColumn.setPrefWidth(130);
        telephoneColumn.setMaxWidth(180);
        telephoneColumn.setResizable(true);
        
        TableColumn<Adherent, String> dateFinColumn = new TableColumn<>("Date Expiration");
        dateFinColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateFin() != null ? 
                    cellData.getValue().getDateFin().toString() : "N/A"));
        dateFinColumn.setMinWidth(130);
        dateFinColumn.setPrefWidth(160);
        dateFinColumn.setMaxWidth(200);
        dateFinColumn.setResizable(true);
        
        TableColumn<Adherent, Integer> joursRetardColumn = new TableColumn<>("Jours de Retard");
        joursRetardColumn.setCellValueFactory(cellData -> {
            Adherent adherent = cellData.getValue();
            if (adherent.getDateFin() != null && adherent.isAbonnementExpire()) {
                long jours = java.time.temporal.ChronoUnit.DAYS.between(adherent.getDateFin(), LocalDate.now());
                return new javafx.beans.property.ReadOnlyObjectWrapper<>((int) jours);
            }
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(0);
        });
        joursRetardColumn.setMinWidth(130);
        joursRetardColumn.setPrefWidth(150);
        joursRetardColumn.setMaxWidth(200);
        joursRetardColumn.setResizable(true);
        joursRetardColumn.setCellFactory(column -> new TableCell<Adherent, Integer>() {
            @Override
            protected void updateItem(Integer jours, boolean empty) {
                super.updateItem(jours, empty);
                if (empty || jours == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(String.valueOf(jours));
                    // Styles avec le thème sombre - couleurs claires pour meilleure visibilité
                    if (jours > 30) {
                        setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;"); // Rouge danger
                    } else if (jours > 7) {
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;"); // Orange warning
                    } else {
                        setStyle("-fx-text-fill: #FCD34D; -fx-font-weight: bold;"); // Jaune clair pour visibilité
                    }
                }
            }
        });
        
        // Ajouter des cell factories pour les autres colonnes pour garantir la lisibilité
        cinColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #E6EAF0;");
            }
        });
        
        nomColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #E6EAF0;");
            }
        });
        
        telephoneColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #E6EAF0;");
            }
        });
        
        dateFinColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #E6EAF0;");
            }
        });
        
        redListTable.getColumns().addAll(cinColumn, nomColumn, telephoneColumn, dateFinColumn, joursRetardColumn);
        
        // Lier la table à la liste observable (doit être fait après les colonnes)
        redListTable.setItems(redList);
        
        // Utiliser UNCONSTRAINED_RESIZE_POLICY pour que le tableau remplisse tout l'espace
        redListTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Faire en sorte que la colonne "Nom Complet" prenne l'espace restant
        Platform.runLater(() -> {
            double totalWidth = redListTable.getWidth();
            if (totalWidth > 0) {
                double usedWidth = cinColumn.getWidth() + telephoneColumn.getWidth() + 
                                  dateFinColumn.getWidth() + joursRetardColumn.getWidth();
                double remainingWidth = totalWidth - usedWidth;
                if (remainingWidth > 150) {
                    nomColumn.setPrefWidth(remainingWidth);
                }
            }
        });
        
        // Écouter les changements de taille de la table pour ajuster dynamiquement
        redListTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                double usedWidth = cinColumn.getWidth() + telephoneColumn.getWidth() + 
                                  dateFinColumn.getWidth() + joursRetardColumn.getWidth();
                double remainingWidth = newVal.doubleValue() - usedWidth;
                if (remainingWidth > 150) {
                    nomColumn.setPrefWidth(remainingWidth);
                }
            }
        });
        
        // Appliquer les styles aux rows de la table via CSS inline
        redListTable.setRowFactory(tv -> {
            TableRow<Adherent> row = new TableRow<>();
            row.setStyle(
                "-fx-background-color: #1A2332; " +
                "-fx-text-fill: #E6EAF0;"
            );
            row.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                if (isHovered && !row.isEmpty()) {
                    row.setStyle(
                        "-fx-background-color: rgba(0, 230, 118, 0.15); " +
                        "-fx-text-fill: #E6EAF0;"
                    );
                } else if (!row.isEmpty()) {
                    row.setStyle(
                        "-fx-background-color: #1A2332; " +
                        "-fx-text-fill: #E6EAF0;"
                    );
                }
            });
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    row.setStyle(
                        "-fx-background-color: rgba(0, 230, 118, 0.2); " +
                        "-fx-text-fill: #E6EAF0;"
                    );
                }
            });
            return row;
        });
    }

    /**
     * Configure la table des paiements
     */
    private void setupPaymentsTable() {
        if (paiementsTable == null) return;
        
        paiementsTable.getColumns().clear();
        paiementsTable.setPrefHeight(600);
        paiementsTable.setMinHeight(400);
        paiementsTable.setMaxHeight(800);
        // Appliquer le style sombre à la table
        paiementsTable.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 12px; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(158, 255, 0, 0.1); " +
            "-fx-border-radius: 12px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 15, 0, 0, 3); " +
            "-fx-table-cell-border-color: transparent;"
        );
        
        // Créer un style pour les headers de colonnes
        String headerStylePayments = 
            "-fx-background-color: #0A0D12; " +
            "-fx-text-fill: #9AA4B2; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 0 0 1px 0; " +
            "-fx-border-color: rgba(158, 255, 0, 0.1);";
        
        TableColumn<Paiement, String> adherentColumn = new TableColumn<>("Adhérent");
        adherentColumn.setStyle(headerStylePayments);
        adherentColumn.setCellValueFactory(cellData -> {
            try {
                Adherent adherent = adherentDAO.findById(cellData.getValue().getAdherentId());
                return new javafx.beans.property.SimpleStringProperty(
                    adherent != null ? adherent.getNomComplet() : "N/A");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
        adherentColumn.setMinWidth(150);
        adherentColumn.setPrefWidth(250);
        adherentColumn.setMaxWidth(400);
        adherentColumn.setResizable(true);
        adherentColumn.setCellFactory(column -> new TableCell<Paiement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px;");
            }
        });
        
        TableColumn<Paiement, Double> montantColumn = new TableColumn<>("Montant");
        montantColumn.setStyle(headerStylePayments);
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        montantColumn.setMinWidth(100);
        montantColumn.setPrefWidth(130);
        montantColumn.setMaxWidth(180);
        montantColumn.setResizable(true);
        montantColumn.setCellFactory(column -> new TableCell<Paiement, Double>() {
            @Override
            protected void updateItem(Double montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(String.format("%.2f DH", montant));
                    setStyle("-fx-text-fill: #00E676; -fx-font-size: 14px; -fx-font-weight: 600;");
                }
            }
        });
        
        TableColumn<Paiement, String> dateColumn = new TableColumn<>("Date Paiement");
        dateColumn.setStyle(headerStylePayments);
        dateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDatePaiement() != null ?
                    cellData.getValue().getDatePaiement().toLocalDate().toString() : "N/A"));
        dateColumn.setMinWidth(130);
        dateColumn.setPrefWidth(160);
        dateColumn.setMaxWidth(200);
        dateColumn.setResizable(true);
        dateColumn.setCellFactory(column -> new TableCell<Paiement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 14px;");
            }
        });
        
        TableColumn<Paiement, String> methodeColumn = new TableColumn<>("Méthode");
        methodeColumn.setStyle(headerStylePayments);
        methodeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getMethodePaiement() != null ?
                    cellData.getValue().getMethodePaiement().getLibelle() : "N/A"));
        methodeColumn.setMinWidth(100);
        methodeColumn.setPrefWidth(130);
        methodeColumn.setMaxWidth(180);
        methodeColumn.setResizable(true);
        methodeColumn.setCellFactory(column -> new TableCell<Paiement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px;");
            }
        });
        
        TableColumn<Paiement, String> dateFinColumn = new TableColumn<>("Date Fin Abonnement");
        dateFinColumn.setStyle(headerStylePayments);
        dateFinColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateFinAbonnement() != null ?
                    cellData.getValue().getDateFinAbonnement().toString() : "N/A"));
        dateFinColumn.setMinWidth(130);
        dateFinColumn.setPrefWidth(180);
        dateFinColumn.setMaxWidth(250);
        dateFinColumn.setResizable(true);
        dateFinColumn.setCellFactory(column -> new TableCell<Paiement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 14px;");
            }
        });
        
        paiementsTable.getColumns().addAll(adherentColumn, montantColumn, dateColumn, methodeColumn, dateFinColumn);
        
        // Lier la table à la liste observable (doit être fait après les colonnes)
        paiementsTable.setItems(paiementsList);
        
        // Utiliser UNCONSTRAINED_RESIZE_POLICY pour que le tableau remplisse tout l'espace
        paiementsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Faire en sorte que la colonne "Adhérent" prenne l'espace restant
        Platform.runLater(() -> {
            double totalWidth = paiementsTable.getWidth();
            if (totalWidth > 0) {
                double usedWidth = montantColumn.getWidth() + dateColumn.getWidth() + 
                                  methodeColumn.getWidth() + dateFinColumn.getWidth();
                double remainingWidth = totalWidth - usedWidth;
                if (remainingWidth > 150) {
                    adherentColumn.setPrefWidth(remainingWidth);
                }
            }
        });
        
        // Écouter les changements de taille de la table pour ajuster dynamiquement
        paiementsTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                double usedWidth = montantColumn.getWidth() + dateColumn.getWidth() + 
                                  methodeColumn.getWidth() + dateFinColumn.getWidth();
                double remainingWidth = newVal.doubleValue() - usedWidth;
                if (remainingWidth > 150) {
                    adherentColumn.setPrefWidth(remainingWidth);
                }
            }
        });
        
        // Appliquer les styles aux rows de la table via CSS inline
        paiementsTable.setRowFactory(tv -> {
            TableRow<Paiement> row = new TableRow<>();
            row.setStyle(
                "-fx-background-color: #1A2332; " +
                "-fx-text-fill: #E6EAF0;"
            );
            row.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
                if (isHovered && !row.isEmpty()) {
                    row.setStyle(
                        "-fx-background-color: rgba(0, 230, 118, 0.15); " +
                        "-fx-text-fill: #E6EAF0;"
                    );
                } else if (!row.isEmpty()) {
                    row.setStyle(
                        "-fx-background-color: #1A2332; " +
                        "-fx-text-fill: #E6EAF0;"
                    );
                }
            });
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    row.setStyle(
                        "-fx-background-color: rgba(0, 230, 118, 0.2); " +
                        "-fx-text-fill: #E6EAF0;"
                    );
                }
            });
            return row;
        });
    }

    /**
     * Charge la liste rouge
     */
    private void loadRedList() {
        try {
            List<Adherent> expired = adherentDAO.findExpired();
            Platform.runLater(() -> {
                redList.clear();
                redList.addAll(expired);
                // Forcer le rafraîchissement de la table
                if (redListTable != null) {
                    redListTable.refresh();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la liste rouge");
        }
    }

    /**
     * Charge tous les paiements
     */
    private void loadPayments() {
        try {
            List<Paiement> paiements = paiementDAO.findAll();
            Platform.runLater(() -> {
                paiementsList.clear();
                paiementsList.addAll(paiements);
                // Forcer le rafraîchissement de la table
                if (paiementsTable != null) {
                    paiementsTable.refresh();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des paiements");
        }
    }

    /**
     * Recherche dans les paiements
     */
    private void searchPayments(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadPayments();
            return;
        }
        
        String lowerSearch = searchTerm.toLowerCase().trim();
        Platform.runLater(() -> {
            paiementsList.clear();
            try {
                List<Paiement> allPayments = paiementDAO.findAll();
                List<Paiement> filtered = allPayments.stream()
                    .filter(p -> {
                        try {
                            Adherent adherent = adherentDAO.findById(p.getAdherentId());
                            if (adherent != null) {
                                String fullName = adherent.getNomComplet().toLowerCase();
                                if (fullName.contains(lowerSearch)) {
                                    return true;
                                }
                            }
                            if (String.format("%.2f", p.getMontant()).contains(lowerSearch)) {
                                return true;
                            }
                            return false;
                        } catch (SQLException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
                paiementsList.addAll(filtered);
                if (paiementsTable != null) {
                    paiementsTable.refresh();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Affiche le dialogue pour enregistrer un nouveau paiement
     */
    private void showNewPaymentDialog(Adherent preselectedAdherent) {
        Dialog<Paiement> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Paiement");
        dialog.setHeaderText("Enregistrer un nouveau paiement");
        
        // Styliser le dialog pane avec le thème sombre
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-text-fill: #E6EAF0;"
        );
        
        // Styliser le header text
        Node headerText = dialogPane.lookup(".header-panel .label");
        if (headerText != null) {
            headerText.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 18px; -fx-font-weight: 700;");
        }

        // Formulaire
        ComboBox<Adherent> adherentCombo = new ComboBox<>();
        adherentCombo.setPrefWidth(400);
        adherentCombo.setStyle(getInputStyle());
        adherentCombo.getStylesheets().clear();
        try {
            List<Adherent> adherents = adherentDAO.findAll();
            adherentCombo.getItems().addAll(adherents);
            if (preselectedAdherent != null) {
                adherentCombo.setValue(preselectedAdherent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ComboBox<Pack> packCombo = new ComboBox<>();
        packCombo.setStyle(getInputStyle());
        packCombo.getStylesheets().clear();
        try {
            List<Pack> packs = packDAO.findAll();
            packCombo.getItems().addAll(packs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Mettre à jour le pack quand un adhérent est sélectionné
        adherentCombo.setOnAction(e -> {
            Adherent selected = adherentCombo.getValue();
            if (selected != null && selected.getPackId() != null) {
                try {
                    Pack pack = packDAO.findById(selected.getPackId());
                    if (pack != null) {
                        packCombo.setValue(pack);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        TextField montantField = new TextField();
        montantField.setPromptText("Montant (DH)");
        montantField.setStyle(getInputStyle());

        DatePicker datePaiementPicker = new DatePicker();
        datePaiementPicker.setValue(LocalDate.now());
        datePaiementPicker.setStyle(getInputStyle());

        ComboBox<Paiement.MethodePaiement> methodeCombo = new ComboBox<>();
        methodeCombo.getItems().addAll(Paiement.MethodePaiement.values());
        methodeCombo.setValue(Paiement.MethodePaiement.ESPECES);
        methodeCombo.setStyle(getInputStyle());
        methodeCombo.getStylesheets().clear();

        DatePicker dateDebutPicker = new DatePicker();
        dateDebutPicker.setValue(LocalDate.now());
        dateDebutPicker.setPromptText("Date début abonnement");
        dateDebutPicker.setStyle(getInputStyle());

        DatePicker dateFinPicker = new DatePicker();
        dateFinPicker.setPromptText("Date fin abonnement");
        dateFinPicker.setStyle(getInputStyle());

        // Calculer la date de fin basée sur le pack
        packCombo.setOnAction(e -> {
            Pack selectedPack = packCombo.getValue();
            Adherent selectedAdherent = adherentCombo.getValue();
            if (selectedPack != null && selectedAdherent != null) {
                LocalDate dateDebut = dateDebutPicker.getValue();
                if (dateDebut == null) {
                    dateDebut = LocalDate.now();
                    dateDebutPicker.setValue(dateDebut);
                }
                
                // Calculer la date de fin selon la durée du pack
                LocalDate dateFin = dateDebut.plusMonths(selectedPack.getDuree());
                dateFinPicker.setValue(dateFin);
                
                // Mettre à jour le montant
                montantField.setText(String.valueOf(selectedPack.getPrix()));
            }
        });

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes");
        notesArea.setPrefRowCount(3);
        notesArea.setStyle(getInputStyle());

        // Créer GridPane pour une structure compacte et maîtrisée
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(12);
        gridPane.setVgap(12);
        
        // Définir les contraintes de colonnes
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(150);
        labelColumn.setPrefWidth(150);
        labelColumn.setMaxWidth(150);
        
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setMinWidth(280);
        fieldColumn.setPrefWidth(280);
        fieldColumn.setHgrow(Priority.ALWAYS);
        
        gridPane.getColumnConstraints().addAll(labelColumn, fieldColumn);
        
        // Ajouter les champs au GridPane avec labels stylisés
        int row = 0;
        
        Label adherentLabel = new Label("Adhérent:");
        adherentLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px; -fx-font-weight: 500;");
        gridPane.add(adherentLabel, 0, row);
        gridPane.add(adherentCombo, 1, row++);
        
        Label packLabel = new Label("Pack:");
        packLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px; -fx-font-weight: 500;");
        gridPane.add(packLabel, 0, row);
        gridPane.add(packCombo, 1, row++);
        
        Label montantLabel = new Label("Montant (DH):");
        montantLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px; -fx-font-weight: 500;");
        gridPane.add(montantLabel, 0, row);
        gridPane.add(montantField, 1, row++);
        
        Label datePaiementLabel = new Label("Date de paiement:");
        datePaiementLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px; -fx-font-weight: 500;");
        gridPane.add(datePaiementLabel, 0, row);
        gridPane.add(datePaiementPicker, 1, row++);
        
        Label methodeLabel = new Label("Méthode de paiement:");
        methodeLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px; -fx-font-weight: 500;");
        gridPane.add(methodeLabel, 0, row);
        gridPane.add(methodeCombo, 1, row++);
        
        Label dateDebutLabel = new Label("Date début abonnement:");
        dateDebutLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px; -fx-font-weight: 500;");
        gridPane.add(dateDebutLabel, 0, row);
        gridPane.add(dateDebutPicker, 1, row++);
        
        Label dateFinLabel = new Label("Date fin abonnement:");
        dateFinLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px; -fx-font-weight: 500;");
        gridPane.add(dateFinLabel, 0, row);
        gridPane.add(dateFinPicker, 1, row++);
        
        Label notesLabel = new Label("Notes:");
        notesLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px; -fx-font-weight: 500;");
        gridPane.add(notesLabel, 0, row);
        GridPane.setColumnSpan(notesArea, 1);
        gridPane.add(notesArea, 1, row);

        gridPane.setStyle("-fx-background-color: #1A2332;");
        dialog.getDialogPane().setContent(gridPane);
        dialog.getDialogPane().setPrefWidth(460);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Styliser les boutons du dialog après leur création
        Platform.runLater(() -> {
            Node saveButton = dialogPane.lookupButton(saveButtonType);
            if (saveButton != null) {
                saveButton.setStyle(
                    "-fx-background-color: #00E676; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 8px 16px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-cursor: hand;"
                );
            }
            Node cancelButton = dialogPane.lookupButton(ButtonType.CANCEL);
            if (cancelButton != null) {
                cancelButton.setStyle(
                    "-fx-background-color: #1c1e2d; " +
                    "-fx-text-fill: #E6EAF0; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 8px 16px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-color: rgba(158, 255, 0, 0.2); " +
                    "-fx-border-radius: 10px; " +
                    "-fx-cursor: hand;"
                );
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Paiement paiement = new Paiement();
                    paiement.setAdherent(adherentCombo.getValue());
                    paiement.setPack(packCombo.getValue());
                    paiement.setMontant(Double.parseDouble(montantField.getText()));
                    paiement.setDatePaiement(datePaiementPicker.getValue().atStartOfDay());
                    paiement.setMethodePaiement(methodeCombo.getValue());
                    paiement.setDateDebutAbonnement(dateDebutPicker.getValue());
                    paiement.setDateFinAbonnement(dateFinPicker.getValue());
                    paiement.setNotes(notesArea.getText());
                    
                    return paiement;
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Données invalides: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(paiement -> {
            try {
                initializeServices(); // S'assurer que les services sont initialisés
                
                // Charger l'adhérent et le pack pour les notifications
                Adherent adherent = adherentDAO.findById(paiement.getAdherentId());
                Pack pack = packDAO.findById(paiement.getPackId());
                
                if (adherent != null) {
                    paiement.setAdherent(adherent);
                }
                if (pack != null) {
                    paiement.setPack(pack);
                }
                
                // Créer le paiement
                paiementDAO.create(paiement);
                
                // Créer notification et activité
                notificationService.notifyNewPayment(paiement);
                activityService.logPaymentRecorded(paiement.getId(), paiement.getMontant());
                
                // Mettre à jour les dates d'abonnement de l'adhérent
                if (adherent != null) {
                    adherent.setDateDebut(paiement.getDateDebutAbonnement());
                    adherent.setDateFin(paiement.getDateFinAbonnement());
                    adherent.setPackId(paiement.getPackId());
                    adherentDAO.update(adherent);
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Paiement enregistré avec succès");
                loadPayments();
                loadRedList();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            }
        });
    }

    /**
     * Retourne le style pour les inputs (TextField, ComboBox, DatePicker, TextArea)
     */
    private String getInputStyle() {
        return "-fx-background-color: #0A0D12; " +
               "-fx-background-radius: 10px; " +
               "-fx-text-fill: #E6EAF0; " +
               "-fx-font-size: 14px; " +
               "-fx-padding: 12px 16px; " +
               "-fx-border-width: 1px; " +
               "-fx-border-color: rgba(158, 255, 0, 0.2); " +
               "-fx-border-radius: 10px;";
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Styliser l'alerte avec le thème sombre
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-text-fill: #E6EAF0;"
        );
        
        // Styliser les boutons
        dialogPane.getButtonTypes().forEach(buttonType -> {
            Node button = dialogPane.lookupButton(buttonType);
            if (button != null) {
                button.setStyle(
                    "-fx-background-color: #00E676; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-padding: 8px 16px; " +
                    "-fx-font-weight: 600;"
                );
            }
        });
        
        alert.showAndWait();
    }
}
