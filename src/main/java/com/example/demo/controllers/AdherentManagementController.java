package com.example.demo.controllers;

import com.example.demo.dao.AdherentDAO;
import com.example.demo.dao.PackDAO;
import com.example.demo.models.Adherent;
import com.example.demo.models.Pack;
import com.example.demo.services.NotificationService;
import com.example.demo.services.ActivityService;
import com.example.demo.utils.SvgIcons;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur pour la gestion des adhérents
 */
public class AdherentManagementController {
    private AdherentDAO adherentDAO = new AdherentDAO();
    private PackDAO packDAO = new PackDAO();
    private NotificationService notificationService = NotificationService.getInstance();
    private ActivityService activityService = ActivityService.getInstance();
    private ObservableList<Adherent> adherentsList = FXCollections.observableArrayList();

    @FXML
    private TableView<Adherent> adherentsTable;
    @FXML
    private TextField searchField;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TableColumn<Adherent, String> cinColumn;
    @FXML
    private TableColumn<Adherent, String> nomColumn;
    @FXML
    private TableColumn<Adherent, String> prenomColumn;
    @FXML
    private TableColumn<Adherent, String> telephoneColumn;
    @FXML
    private TableColumn<Adherent, String> packColumn;
    @FXML
    private TableColumn<Adherent, String> dateDebutColumn;
    @FXML
    private TableColumn<Adherent, String> dateFinColumn;
    @FXML
    private TableColumn<Adherent, String> statutColumn;

    /**
     * Charge la vue de gestion des adhérents - Utilise toujours createBasicView() pour le nouveau design
     */
    public Parent getView() {
        // Toujours utiliser createBasicView() pour le nouveau design dark
        return createBasicView();
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
        
        // Configurer la recherche
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.trim().isEmpty()) {
                    loadAdherents();
                } else {
                    searchAdherents(newVal);
                }
            });
        }

        // Configurer les boutons
        if (addButton != null) {
            addButton.setOnAction(e -> showAdherentDialog(null));
        }
        if (editButton != null) {
            editButton.setOnAction(e -> editSelectedAdherent());
        }
        if (deleteButton != null) {
            deleteButton.setOnAction(e -> deleteSelectedAdherent());
        }

        // Configurer les colonnes de la table
        setupTableColumns();

        // Charger les données
        loadAdherents();
    }

    /**
     * Configure les colonnes de la table
     */
    private void setupTableColumns() {
        if (adherentsTable == null) return;

        adherentsTable.getColumns().clear();
        adherentsTable.setPrefHeight(500);
        adherentsTable.setMaxWidth(Double.MAX_VALUE);
        adherentsTable.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-table-cell-border-color: transparent;"
        );

        cinColumn = new TableColumn<>("CIN");
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        cinColumn.setMinWidth(100);
        cinColumn.setPrefWidth(120);
        cinColumn.setMaxWidth(150);
        cinColumn.setResizable(true);
        cinColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px;");
            }
        });

        nomColumn = new TableColumn<>("Nom");
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomColumn.setMinWidth(120);
        nomColumn.setPrefWidth(150);
        nomColumn.setMaxWidth(200);
        nomColumn.setResizable(true);
        nomColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px;");
            }
        });

        prenomColumn = new TableColumn<>("Prénom");
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        prenomColumn.setMinWidth(120);
        prenomColumn.setPrefWidth(150);
        prenomColumn.setMaxWidth(200);
        prenomColumn.setResizable(true);
        prenomColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px;");
            }
        });

        telephoneColumn = new TableColumn<>("Téléphone");
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        telephoneColumn.setMinWidth(100);
        telephoneColumn.setPrefWidth(120);
        telephoneColumn.setMaxWidth(150);
        telephoneColumn.setResizable(true);
        telephoneColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px;");
            }
        });

        packColumn = new TableColumn<>("Pack");
        packColumn.setCellValueFactory(cellData -> {
            Adherent adherent = cellData.getValue();
            if (adherent.getPackId() != null) {
                try {
                    Pack pack = packDAO.findById(adherent.getPackId());
                    return new javafx.beans.property.SimpleStringProperty(
                        pack != null ? pack.getNom() : "N/A"
                    );
                } catch (SQLException e) {
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                }
            }
            return new javafx.beans.property.SimpleStringProperty("Aucun");
        });
        packColumn.setMinWidth(150);
        packColumn.setPrefWidth(200);
        // Pas de maxWidth pour permettre à cette colonne de prendre l'espace restant
        packColumn.setResizable(true);
        packColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px;");
            }
        });

        dateDebutColumn = new TableColumn<>("Date Début");
        dateDebutColumn.setCellValueFactory(cellData -> {
            Adherent adherent = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                adherent.getDateDebut() != null ? adherent.getDateDebut().toString() : "N/A"
            );
        });
        dateDebutColumn.setMinWidth(100);
        dateDebutColumn.setPrefWidth(120);
        dateDebutColumn.setMaxWidth(150);
        dateDebutColumn.setResizable(true);
        dateDebutColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 14px;");
            }
        });

        dateFinColumn = new TableColumn<>("Date Fin");
        dateFinColumn.setCellValueFactory(cellData -> {
            Adherent adherent = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                adherent.getDateFin() != null ? adherent.getDateFin().toString() : "N/A"
            );
        });
        dateFinColumn.setMinWidth(100);
        dateFinColumn.setPrefWidth(120);
        dateFinColumn.setMaxWidth(150);
        dateFinColumn.setResizable(true);
        dateFinColumn.setStyle("-fx-alignment: center-left;");
        dateFinColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String dateFin, boolean empty) {
                super.updateItem(dateFin, empty);
                if (empty || dateFin == null || "N/A".equals(dateFin)) {
                    setText(empty ? "" : dateFin);
                    setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 14px;");
                } else {
                    setText(dateFin);
                    // Vérifier si la date est expirée
                    Adherent adherent = getTableView().getItems().get(getIndex());
                    if (adherent != null && adherent.isAbonnementExpire()) {
                        setStyle("-fx-text-fill: #EF4444; -fx-font-size: 14px; -fx-font-weight: 600;");
                    } else if (adherent != null && adherent.isAbonnementExpireBientot()) {
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 14px; -fx-font-weight: 600;");
                    } else {
                        setStyle("-fx-text-fill: #10b981; -fx-font-size: 14px; -fx-font-weight: 600;");
                    }
                }
            }
        });

        statutColumn = new TableColumn<>("Statut");
        statutColumn.setCellValueFactory(cellData -> {
            Adherent adherent = cellData.getValue();
            String statut = adherent.isAbonnementExpire() ? "Expiré" : "Actif";
            return new javafx.beans.property.SimpleStringProperty(statut);
        });
        statutColumn.setMinWidth(80);
        statutColumn.setPrefWidth(100);
        statutColumn.setMaxWidth(120);
        statutColumn.setResizable(true);
        statutColumn.setStyle("-fx-alignment: center-left;");
        statutColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(statut);
                    setStyle(
                        "Actif".equals(statut) ? 
                        "-fx-text-fill: #10b981; -fx-font-size: 14px; -fx-font-weight: 600;" :
                        "-fx-text-fill: #ef4444; -fx-font-size: 14px; -fx-font-weight: 600;"
                    );
                }
            }
        });

        adherentsTable.getColumns().addAll(cinColumn, nomColumn, prenomColumn, telephoneColumn, 
                                          packColumn, dateDebutColumn, dateFinColumn, statutColumn);
        adherentsTable.setItems(adherentsList);
        
        // Faire en sorte que le tableau remplisse tout l'espace sans zone blanche
        adherentsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Ajuster dynamiquement la colonne Pack pour qu'elle prenne l'espace restant
        // et éliminer la zone blanche
        Platform.runLater(() -> {
            // Attendre que le tableau soit rendu avant de calculer
            adherentsTable.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.getWidth() > 0) {
                    updatePackColumnWidth();
                }
            });
            
            // Calculer initialement après un court délai
            Platform.runLater(() -> {
                if (adherentsTable.getWidth() > 0) {
                    updatePackColumnWidth();
                }
            });
            
            // Écouter les changements de largeur du tableau
            adherentsTable.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) {
                    updatePackColumnWidth();
                }
            });
            
            // Écouter aussi les changements de largeur des autres colonnes
            cinColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            nomColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            prenomColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            telephoneColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            dateDebutColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            dateFinColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            statutColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            
            // Appliquer les styles aux colonnes via CSS
            try {
                // Style des headers
                javafx.scene.Node header = adherentsTable.lookup(".column-header-background");
                if (header != null) {
                    header.setStyle("-fx-background-color: rgba(42, 52, 65, 0.5);");
                }
                
                // Style des column headers
                java.util.Set<javafx.scene.Node> columnHeaders = adherentsTable.lookupAll(".column-header");
                for (javafx.scene.Node headerNode : columnHeaders) {
                    headerNode.setStyle(
                        "-fx-background-color: rgba(42, 52, 65, 0.5); " +
                        "-fx-text-fill: #E6EAF0; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-padding: 16px 20px; " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-border-color: #2A3441;"
                    );
                }
                
                // Style des rows
                java.util.Set<javafx.scene.Node> rows = adherentsTable.lookupAll(".table-row-cell");
                for (javafx.scene.Node row : rows) {
                    row.setStyle(
                        "-fx-background-color: rgba(42, 52, 65, 0.3); " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.05);"
                    );
                }
                
                // Style des cells
                java.util.Set<javafx.scene.Node> cells = adherentsTable.lookupAll(".table-cell");
                for (javafx.scene.Node cell : cells) {
                    cell.setStyle(
                        "-fx-text-fill: #E6EAF0; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 12px 16px; " +
                        "-fx-border-width: 0; " +
                        "-fx-background-color: transparent;"
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        // Appliquer les styles aux rows via rowFactory
        adherentsTable.setRowFactory(tv -> {
            TableRow<Adherent> row = new TableRow<>();
            row.setStyle(
                "-fx-background-color: rgba(42, 52, 65, 0.3); " +
                "-fx-border-width: 0 0 1 0; " +
                "-fx-border-color: rgba(255, 255, 255, 0.05);"
            );
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) {
                    row.setStyle(
                        "-fx-background-color: rgba(0, 230, 118, 0.15); " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.05);"
                    );
                }
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty() && !row.isSelected()) {
                    row.setStyle(
                        "-fx-background-color: rgba(42, 52, 65, 0.3); " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.05);"
                    );
                }
            });
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected && !row.isEmpty()) {
                    row.setStyle(
                        "-fx-background-color: rgba(0, 230, 118, 0.2); " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.05);"
                    );
                } else if (!row.isEmpty()) {
                    row.setStyle(
                        "-fx-background-color: rgba(42, 52, 65, 0.3); " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.05);"
                    );
                }
            });
            return row;
        });
        
        // Réappliquer les styles après chaque mise à jour des données
        adherentsTable.itemsProperty().addListener((obs, oldItems, newItems) -> {
            Platform.runLater(() -> {
                try {
                    // Style des headers
                    javafx.scene.Node header = adherentsTable.lookup(".column-header-background");
                    if (header != null) {
                        header.setStyle("-fx-background-color: rgba(42, 52, 65, 0.5);");
                    }
                    
                    // Style des column headers
                    java.util.Set<javafx.scene.Node> columnHeaders = adherentsTable.lookupAll(".column-header");
                    for (javafx.scene.Node headerNode : columnHeaders) {
                        headerNode.setStyle(
                            "-fx-background-color: rgba(42, 52, 65, 0.5); " +
                            "-fx-text-fill: #E6EAF0; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-padding: 16px 20px; " +
                            "-fx-border-width: 0 0 1 0; " +
                            "-fx-border-color: #2A3441;"
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * Met à jour la largeur de la colonne Pack pour qu'elle prenne l'espace restant
     * et élimine la zone blanche dans le tableau
     */
    private void updatePackColumnWidth() {
        if (adherentsTable == null || packColumn == null) return;
        
        double tableWidth = adherentsTable.getWidth();
        if (tableWidth <= 0) return;
        
        // Calculer la largeur totale des autres colonnes
        double totalFixedWidth = 0;
        if (cinColumn != null) totalFixedWidth += cinColumn.getWidth();
        if (nomColumn != null) totalFixedWidth += nomColumn.getWidth();
        if (prenomColumn != null) totalFixedWidth += prenomColumn.getWidth();
        if (telephoneColumn != null) totalFixedWidth += telephoneColumn.getWidth();
        if (dateDebutColumn != null) totalFixedWidth += dateDebutColumn.getWidth();
        if (dateFinColumn != null) totalFixedWidth += dateFinColumn.getWidth();
        if (statutColumn != null) totalFixedWidth += statutColumn.getWidth();
        
        // Largeur approximative de la scrollbar verticale
        double scrollbarWidth = 18;
        
        // Calculer l'espace disponible pour la colonne Pack
        double availableWidth = tableWidth - totalFixedWidth - scrollbarWidth;
        
        // Ajuster la largeur de la colonne Pack si l'espace disponible est suffisant
        if (availableWidth > packColumn.getMinWidth()) {
            packColumn.setPrefWidth(availableWidth);
        } else {
            packColumn.setPrefWidth(packColumn.getMinWidth());
        }
    }

    /**
     * Vue de secours si le FXML ne charge pas - Structure complète selon design dashboard
     */
    private Parent createBasicView() {
        // Initialiser les services
        initializeServices();
        
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
        
        VBox contentWrapper = new VBox(20);
        contentWrapper.setPadding(new Insets(20, 24, 20, 24));
        contentWrapper.setStyle("-fx-background-color: #0d0f1a;");
        contentWrapper.setMaxWidth(Double.MAX_VALUE);
        
        // Card container pour la recherche et les boutons
        VBox searchCard = createSearchCard();
        contentWrapper.getChildren().add(searchCard);
        
        // Card container pour la table
        VBox tableCard = createTableCard();
        contentWrapper.getChildren().add(tableCard);
        
        contentScroll.setContent(contentWrapper);
        
        VBox centerContent = new VBox(0);
        centerContent.getChildren().addAll(titleFilterSection, contentScroll);
        VBox.setVgrow(contentScroll, Priority.ALWAYS);
        
        centerArea.setCenter(centerContent);
        root.setCenter(centerArea);
        
        // Charger les adhérents
        loadAdherents();
        
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
        
        // Menu icon (pas de fonction pour adhérents, peut être caché)
        Button menuBtn = createHeaderIconButton("icon-menu", 20);
        menuBtn.setVisible(false); // Caché pour adhérents
        
        // Star icon (Favoris)
        Button starBtn = createHeaderIconButton("icon-star", 20);
        starBtn.setOnAction(e -> {
            try {
                com.example.demo.dao.FavorisDAO favorisDAO = new com.example.demo.dao.FavorisDAO();
                String pageName = "ADHERENTS";
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
        Label breadcrumbLabel = new Label("Gestion des Adhérents");
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
        refreshBtn.setOnAction(e -> loadAdherents());
        
        // Bell icon (Notifications) - simplifié pour adhérents
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
        
        // Titre "Gestion des Adhérents" à gauche
        Label titleLabel = new Label("Gestion des Adhérents");
        titleLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 24px; -fx-font-weight: 700;");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        section.getChildren().addAll(titleLabel, spacer);
        
        return section;
    }
    
    /**
     * Crée la card pour la recherche et les boutons d'action
     */
    private VBox createSearchCard() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(20));
        container.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0, 0, 2);"
        );
        
        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        // Champ de recherche
        searchField = new TextField();
        searchField.setPromptText("Rechercher par nom, CIN, téléphone...");
        searchField.setPrefWidth(400);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                loadAdherents();
            } else {
                searchAdherents(newVal);
            }
        });
        searchField.setStyle(
            "-fx-background-color: rgba(15, 23, 42, 0.6); " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(255, 255, 255, 0.1); " +
            "-fx-border-radius: 12px; " +
            "-fx-prompt-text-fill: #9AA4B2;"
        );
        
        // Styliser le prompt text et le focus
        String baseTextFieldStyle = searchField.getStyle();
        searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
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
                searchField.setStyle(baseTextFieldStyle);
            }
        });
        
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        // Bouton "+ Nouvel Adhérent" (Success)
        addButton = new Button("+ Nouvel Adhérent");
        addButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #10b981, #059669); " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 12px 24px; " +
            "-fx-background-radius: 12px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.4), 10, 0, 0, 4);"
        );
        addButton.setOnAction(e -> showAdherentDialog(null));
        addButton.setOnMouseEntered(e -> {
            addButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #059669, #047857); " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 12px 24px; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.6), 15, 0, 0, 6);"
            );
        });
        addButton.setOnMouseExited(e -> {
            addButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #10b981, #059669); " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 12px 24px; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.4), 10, 0, 0, 4);"
            );
        });
        
        // Bouton "Modifier" (Primary)
        editButton = new Button("Modifier");
        editButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 12px 24px; " +
            "-fx-background-radius: 12px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 10, 0, 0, 4);"
        );
        editButton.setOnAction(e -> editSelectedAdherent());
        editButton.setOnMouseEntered(e -> {
            editButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #2563eb, #1d4ed8); " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 12px 24px; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.6), 15, 0, 0, 6);"
            );
        });
        editButton.setOnMouseExited(e -> {
            editButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 12px 24px; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 10, 0, 0, 4);"
            );
        });
        
        // Bouton "Supprimer" (Danger)
        deleteButton = new Button("Supprimer");
        deleteButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #ef4444, #dc2626); " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 12px 24px; " +
            "-fx-background-radius: 12px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 10, 0, 0, 4);"
        );
        deleteButton.setOnAction(e -> deleteSelectedAdherent());
        deleteButton.setOnMouseEntered(e -> {
            deleteButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #dc2626, #b91c1c); " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 12px 24px; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.6), 15, 0, 0, 6);"
            );
        });
        deleteButton.setOnMouseExited(e -> {
            deleteButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #ef4444, #dc2626); " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 12px 24px; " +
                "-fx-background-radius: 12px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 10, 0, 0, 4);"
            );
        });
        
        searchBar.getChildren().addAll(searchField, addButton, editButton, deleteButton);
        container.getChildren().add(searchBar);
        
        return container;
    }
    
    /**
     * Crée la card pour la table des adhérents
     */
    private VBox createTableCard() {
        VBox container = new VBox(0);
        container.setPadding(new Insets(20));
        container.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background-radius: 16px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0, 0, 2);"
        );
        container.setMinHeight(400);
        
        // Table des adhérents
        adherentsTable = new TableView<>();
        adherentsTable.setPrefHeight(500);
        adherentsTable.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-table-cell-border-color: transparent;"
        );
        
        setupTableColumns();
        
        // Double-clic pour modifier
        adherentsTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                editSelectedAdherent();
            }
        });
        
        container.getChildren().add(adherentsTable);
        
        return container;
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
     * Charge tous les adhérents
     */
    private void loadAdherents() {
        try {
            List<Adherent> adherents = adherentDAO.findAll();
            adherentsList.clear();
            adherentsList.addAll(adherents);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des adhérents: " + e.getMessage());
        }
    }

    /**
     * Recherche des adhérents
     */
    private void searchAdherents(String searchTerm) {
        try {
            List<Adherent> adherents = adherentDAO.search(searchTerm);
            adherentsList.clear();
            adherentsList.addAll(adherents);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    /**
     * Affiche le dialogue pour créer/modifier un adhérent
     */
    private void showAdherentDialog(Adherent adherent) {
        Dialog<Adherent> dialog = new Dialog<>();
        dialog.setTitle(adherent == null ? "Nouvel Adhérent" : "Modifier Adhérent");
        dialog.setHeaderText(adherent == null ? "Créer un nouvel adhérent" : "Modifier l'adhérent");

        // Formulaire
        TextField cinField = new TextField();
        cinField.setPromptText("CIN");
        
        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        
        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        
        DatePicker dateNaissancePicker = new DatePicker();
        dateNaissancePicker.setPromptText("Date de naissance");
        
        TextField telephoneField = new TextField();
        telephoneField.setPromptText("Téléphone");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        
        TextArea adresseArea = new TextArea();
        adresseArea.setPromptText("Adresse");
        adresseArea.setPrefRowCount(3);
        
        TextField poidsField = new TextField();
        poidsField.setPromptText("Poids (kg)");
        
        TextField tailleField = new TextField();
        tailleField.setPromptText("Taille (cm)");
        
        TextArea objectifsArea = new TextArea();
        objectifsArea.setPromptText("Objectifs");
        objectifsArea.setPrefRowCount(2);
        
        TextArea problemesArea = new TextArea();
        problemesArea.setPromptText("Problèmes de santé");
        problemesArea.setPrefRowCount(2);

        // Combo box pour les packs
        ComboBox<Pack> packCombo = new ComboBox<>();
        try {
            List<Pack> packs = packDAO.findAll();
            packCombo.getItems().addAll(packs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DatePicker dateDebutPicker = new DatePicker();
        dateDebutPicker.setPromptText("Date début");
        
        DatePicker dateFinPicker = new DatePicker();
        dateFinPicker.setPromptText("Date fin");

        if (adherent != null) {
            cinField.setText(adherent.getCin());
            nomField.setText(adherent.getNom());
            prenomField.setText(adherent.getPrenom());
            if (adherent.getDateNaissance() != null) {
                dateNaissancePicker.setValue(adherent.getDateNaissance());
            }
            telephoneField.setText(adherent.getTelephone());
            emailField.setText(adherent.getEmail());
            adresseArea.setText(adherent.getAdresse());
            if (adherent.getPoids() != null) {
                poidsField.setText(String.valueOf(adherent.getPoids()));
            }
            if (adherent.getTaille() != null) {
                tailleField.setText(String.valueOf(adherent.getTaille()));
            }
            objectifsArea.setText(adherent.getObjectifs());
            problemesArea.setText(adherent.getProblemesSante());
            if (adherent.getPackId() != null) {
                try {
                    Pack pack = packDAO.findById(adherent.getPackId());
                    if (pack != null) {
                        packCombo.setValue(pack);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (adherent.getDateDebut() != null) {
                dateDebutPicker.setValue(adherent.getDateDebut());
            }
            if (adherent.getDateFin() != null) {
                dateFinPicker.setValue(adherent.getDateFin());
            }
        }

        // Créer GridPane pour une structure compacte et maîtrisée
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(12);
        gridPane.setVgap(12);
        
        // Définir les contraintes de colonnes (4 colonnes pour une disposition compacte)
        ColumnConstraints labelCol1 = new ColumnConstraints();
        labelCol1.setMinWidth(120);
        labelCol1.setPrefWidth(120);
        
        ColumnConstraints fieldCol1 = new ColumnConstraints();
        fieldCol1.setMinWidth(150);
        fieldCol1.setPrefWidth(150);
        fieldCol1.setHgrow(Priority.ALWAYS);
        
        ColumnConstraints labelCol2 = new ColumnConstraints();
        labelCol2.setMinWidth(120);
        labelCol2.setPrefWidth(120);
        
        ColumnConstraints fieldCol2 = new ColumnConstraints();
        fieldCol2.setMinWidth(150);
        fieldCol2.setPrefWidth(150);
        fieldCol2.setHgrow(Priority.ALWAYS);
        
        gridPane.getColumnConstraints().addAll(labelCol1, fieldCol1, labelCol2, fieldCol2);
        
        // Style commun pour les labels
        String labelStyle = "-fx-text-fill: #E6EAF0; -fx-font-size: 14px; -fx-font-weight: 500;";
        
        // Style commun pour les TextFields - background amélioré (pas blanc strict)
        String textFieldStyle = 
            "-fx-background-color: rgba(42, 52, 65, 0.8); " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-color: rgba(255, 255, 255, 0.15); " +
            "-fx-border-radius: 12px; " +
            "-fx-prompt-text-fill: rgba(230, 234, 240, 0.6);";
        
        // Style spécial pour l'input email avec background clair mais pas blanc strict
        String emailFieldStyle = 
            "-fx-background-color: rgba(220, 220, 220, 0.9); " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #1A2332; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-color: rgba(16, 185, 129, 0.4); " +
            "-fx-border-radius: 12px; " +
            "-fx-prompt-text-fill: rgba(26, 35, 50, 0.6);";
        
        // Style amélioré pour TextArea - background plus visible et texte clair
        String textAreaStyle = 
            "-fx-background-color: rgba(42, 52, 65, 0.85); " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-color: rgba(255, 255, 255, 0.2); " +
            "-fx-border-radius: 12px; " +
            "-fx-prompt-text-fill: rgba(230, 234, 240, 0.6);";
        
        // Style amélioré pour DatePicker avec meilleur design
        String datePickerStyle = 
            "-fx-background-color: rgba(42, 52, 65, 0.8); " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-color: rgba(255, 255, 255, 0.15); " +
            "-fx-border-radius: 12px;";
        
        // Style pour ComboBox avec background sombre pour les options
        String comboBoxStyle =
            "-fx-background-color: rgba(15, 23, 42, 0.6); " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(255, 255, 255, 0.1); " +
            "-fx-border-radius: 12px;";
        
        // Style pour la liste déroulante du ComboBox (options)
        String comboBoxListStyle = 
            "-fx-background-color: rgba(15, 23, 42, 0.95); " +
            "-fx-text-fill: #E6EAF0;";
        
        // Appliquer les styles aux champs
        cinField.setStyle(textFieldStyle);
        nomField.setStyle(textFieldStyle);
        prenomField.setStyle(textFieldStyle);
        telephoneField.setStyle(textFieldStyle);
        emailField.setStyle(emailFieldStyle); // Style spécial pour email avec texte sombre
        poidsField.setStyle(textFieldStyle);
        tailleField.setStyle(textFieldStyle);
        adresseArea.setStyle(textAreaStyle);
        objectifsArea.setStyle(textAreaStyle);
        problemesArea.setStyle(textAreaStyle);
        
        // Améliorer la visibilité des TextArea avec styles supplémentaires
        Platform.runLater(() -> {
            try {
                for (TextArea ta : new TextArea[]{adresseArea, objectifsArea, problemesArea}) {
                    // S'assurer que le texte est bien visible
                    ta.setStyle(textAreaStyle + 
                        " -fx-control-inner-background: rgba(42, 52, 65, 0.85); " +
                        " -fx-text-box-border: rgba(255, 255, 255, 0.2);");
                }
            } catch (Exception e) {
                // Ignorer les erreurs
            }
        });
        
        // Appliquer les styles aux DatePicker avec styles supplémentaires pour les composants internes
        dateNaissancePicker.setStyle(datePickerStyle);
        dateDebutPicker.setStyle(datePickerStyle);
        dateFinPicker.setStyle(datePickerStyle);
        
        // Améliorer le style des DatePicker via Platform.runLater pour styliser les composants internes
        Platform.runLater(() -> {
            try {
                // Styliser les DatePicker - bouton calendrier et texte
                for (DatePicker dp : new DatePicker[]{dateNaissancePicker, dateDebutPicker, dateFinPicker}) {
                    // Styliser le bouton calendrier
                    javafx.scene.Node arrowButton = dp.lookup(".arrow-button");
                    if (arrowButton != null) {
                        arrowButton.setStyle(
                            "-fx-background-color: rgba(16, 185, 129, 0.3); " +
                            "-fx-background-radius: 0 12px 12px 0; " +
                            "-fx-border-color: transparent;"
                        );
                    }
                    
                    // Styliser l'icône du calendrier
                    javafx.scene.Node arrow = dp.lookup(".arrow");
                    if (arrow != null) {
                        arrow.setStyle("-fx-background-color: #E6EAF0;");
                    }
                    
                    // Styliser le champ de texte du DatePicker
                    javafx.scene.Node textField = dp.lookup(".text-field");
                    if (textField != null) {
                        textField.setStyle(
                            "-fx-background-color: transparent; " +
                            "-fx-text-fill: #E6EAF0; " +
                            "-fx-font-size: 14px;"
                        );
                    }
                }
            } catch (Exception e) {
                // Ignorer les erreurs de lookup
            }
        });
        
        packCombo.setStyle(comboBoxStyle);
        
        // Styliser le ComboBox Pack pour que la valeur sélectionnée soit visible
        packCombo.setCellFactory(listView -> new ListCell<Pack>() {
            @Override
            protected void updateItem(Pack pack, boolean empty) {
                super.updateItem(pack, empty);
                if (empty || pack == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(pack.getNom());
                    // Style avec background sombre et texte clair pour les options
                    setStyle(
                        "-fx-background-color: rgba(15, 23, 42, 0.95); " +
                        "-fx-text-fill: #E6EAF0; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8px 12px;"
                    );
                }
                // Style au survol
                setOnMouseEntered(e -> {
                    if (!empty && pack != null) {
                        setStyle(
                            "-fx-background-color: rgba(42, 52, 65, 0.95); " +
                            "-fx-text-fill: #E6EAF0; " +
                            "-fx-font-size: 14px; " +
                            "-fx-padding: 8px 12px;"
                        );
                    }
                });
                setOnMouseExited(e -> {
                    if (!empty && pack != null) {
                        setStyle(
                            "-fx-background-color: rgba(15, 23, 42, 0.95); " +
                            "-fx-text-fill: #E6EAF0; " +
                            "-fx-font-size: 14px; " +
                            "-fx-padding: 8px 12px;"
                        );
                    }
                });
            }
        });
        
        // Styliser le buttonCell (label qui affiche la valeur sélectionnée)
        packCombo.setButtonCell(new ListCell<Pack>() {
            @Override
            protected void updateItem(Pack pack, boolean empty) {
                super.updateItem(pack, empty);
                if (empty || pack == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(pack.getNom());
                    setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px;");
                }
            }
        });
        
        
        // Ajouter les champs au GridPane
        int row = 0;
        
        // Première ligne: CIN, Nom
        Label cinLabel = new Label("CIN:");
        cinLabel.setStyle(labelStyle);
        gridPane.add(cinLabel, 0, row);
        gridPane.add(cinField, 1, row);
        Label nomLabel = new Label("Nom:");
        nomLabel.setStyle(labelStyle);
        gridPane.add(nomLabel, 2, row);
        gridPane.add(nomField, 3, row++);
        
        // Deuxième ligne: Prénom, Date de naissance
        Label prenomLabel = new Label("Prénom:");
        prenomLabel.setStyle(labelStyle);
        gridPane.add(prenomLabel, 0, row);
        gridPane.add(prenomField, 1, row);
        Label dateNaissanceLabel = new Label("Date de naissance:");
        dateNaissanceLabel.setStyle(labelStyle);
        gridPane.add(dateNaissanceLabel, 2, row);
        gridPane.add(dateNaissancePicker, 3, row++);
        
        // Troisième ligne: Téléphone, Email
        Label telephoneLabel = new Label("Téléphone:");
        telephoneLabel.setStyle(labelStyle);
        gridPane.add(telephoneLabel, 0, row);
        gridPane.add(telephoneField, 1, row);
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle(labelStyle);
        gridPane.add(emailLabel, 2, row);
        gridPane.add(emailField, 3, row++);
        
        // Quatrième ligne: Adresse (sur 4 colonnes)
        Label adresseLabel = new Label("Adresse:");
        adresseLabel.setStyle(labelStyle);
        gridPane.add(adresseLabel, 0, row);
        GridPane.setColumnSpan(adresseArea, 3);
        gridPane.add(adresseArea, 1, row++);
        
        // Cinquième ligne: Poids, Taille
        Label poidsLabel = new Label("Poids (kg):");
        poidsLabel.setStyle(labelStyle);
        gridPane.add(poidsLabel, 0, row);
        gridPane.add(poidsField, 1, row);
        Label tailleLabel = new Label("Taille (cm):");
        tailleLabel.setStyle(labelStyle);
        gridPane.add(tailleLabel, 2, row);
        gridPane.add(tailleField, 3, row++);
        
        // Sixième ligne: Pack, Date début
        Label packLabel = new Label("Pack:");
        packLabel.setStyle(labelStyle);
        gridPane.add(packLabel, 0, row);
        gridPane.add(packCombo, 1, row);
        Label dateDebutLabel = new Label("Date début:");
        dateDebutLabel.setStyle(labelStyle);
        gridPane.add(dateDebutLabel, 2, row);
        gridPane.add(dateDebutPicker, 3, row++);
        
        // Septième ligne: Date fin
        Label dateFinLabel = new Label("Date fin:");
        dateFinLabel.setStyle(labelStyle);
        gridPane.add(dateFinLabel, 0, row);
        gridPane.add(dateFinPicker, 1, row++);
        
        // Huitième ligne: Objectifs (sur 4 colonnes)
        Label objectifsLabel = new Label("Objectifs:");
        objectifsLabel.setStyle(labelStyle);
        gridPane.add(objectifsLabel, 0, row);
        GridPane.setColumnSpan(objectifsArea, 3);
        gridPane.add(objectifsArea, 1, row++);
        
        // Neuvième ligne: Problèmes de santé (sur 4 colonnes)
        Label problemesLabel = new Label("Problèmes de santé:");
        problemesLabel.setStyle(labelStyle);
        gridPane.add(problemesLabel, 0, row);
        GridPane.setColumnSpan(problemesArea, 3);
        gridPane.add(problemesArea, 1, row);

        // Créer un ScrollPane pour que les boutons soient toujours visibles
        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-background: #1A2332; " +
            "-fx-border-color: transparent;"
        );
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(450);
        scrollPane.setMaxHeight(450);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setStyle(
            "-fx-background-color: #1A2332; " +
            "-fx-text-fill: #E6EAF0;"
        );
        dialog.getDialogPane().setPrefWidth(650);
        dialog.getDialogPane().setPrefHeight(550);
        
        // Styliser le header text pour la visibilité
        Platform.runLater(() -> {
            try {
                Node header = dialog.getDialogPane().lookup(".header-panel");
                if (header != null) {
                    header.setStyle("-fx-background-color: #1A2332;");
                }
                Label headerText = (Label) dialog.getDialogPane().lookup(".header-panel .label");
                if (headerText != null) {
                    headerText.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 16px; -fx-font-weight: 600;");
                }
            } catch (Exception e) {
                // Ignorer si le header n'est pas trouvé
            }
        });

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Styliser les boutons du dialog
        Platform.runLater(() -> {
            try {
                Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
                if (saveButton != null) {
                    saveButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #10b981, #059669); " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-size: 15px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-padding: 12px 28px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-min-width: 120px;"
                    );
                }
                
                Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
                if (cancelButton != null) {
                    cancelButton.setStyle(
                        "-fx-background-color: rgba(42, 52, 65, 0.8); " +
                        "-fx-text-fill: #E6EAF0; " +
                        "-fx-font-size: 15px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-padding: 12px 28px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-min-width: 120px;"
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Adherent newAdherent = adherent != null ? adherent : new Adherent();
                    newAdherent.setCin(cinField.getText());
                    newAdherent.setNom(nomField.getText());
                    newAdherent.setPrenom(prenomField.getText());
                    newAdherent.setDateNaissance(dateNaissancePicker.getValue());
                    newAdherent.setTelephone(telephoneField.getText());
                    newAdherent.setEmail(emailField.getText());
                    newAdherent.setAdresse(adresseArea.getText());
                    
                    if (!poidsField.getText().isEmpty()) {
                        newAdherent.setPoids(Double.parseDouble(poidsField.getText()));
                    }
                    if (!tailleField.getText().isEmpty()) {
                        newAdherent.setTaille(Double.parseDouble(tailleField.getText()));
                    }
                    
                    newAdherent.setObjectifs(objectifsArea.getText());
                    newAdherent.setProblemesSante(problemesArea.getText());
                    
                    Pack selectedPack = packCombo.getValue();
                    if (selectedPack != null) {
                        newAdherent.setPack(selectedPack);
                    }
                    
                    newAdherent.setDateDebut(dateDebutPicker.getValue());
                    newAdherent.setDateFin(dateFinPicker.getValue());
                    
                    return newAdherent;
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Données invalides: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                initializeServices(); // S'assurer que les services sont initialisés
                
                if (result.getId() == null) {
                    // Création d'un nouvel adhérent
                    adherentDAO.create(result);
                    
                    // Créer notification et activité
                    notificationService.notifyNewAdherent(result);
                    activityService.logAdherentCreated(result.getId(), result.getNomComplet());
                    
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Adhérent créé avec succès");
                } else {
                    // Modification d'un adhérent existant
                    adherentDAO.update(result);
                    
                    // Créer notification et activité
                    notificationService.notifyAdherentUpdated(result);
                    activityService.logAdherentUpdated(result.getId(), result.getNomComplet());
                    
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Adhérent modifié avec succès");
                }
                loadAdherents();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            }
        });
    }

    /**
     * Modifie l'adhérent sélectionné
     */
    private void editSelectedAdherent() {
        Adherent selected = adherentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un adhérent à modifier");
            return;
        }
        showAdherentDialog(selected);
    }

    /**
     * Supprime l'adhérent sélectionné
     */
    private void deleteSelectedAdherent() {
        Adherent selected = adherentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un adhérent à supprimer");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'adhérent");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'adhérent \"" + selected.getNomComplet() + "\" ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    initializeServices(); // S'assurer que les services sont initialisés
                    
                    // Conserver les informations de l'adhérent avant suppression pour la notification
                    Adherent adherentToDelete = new Adherent();
                    adherentToDelete.setId(selected.getId());
                    adherentToDelete.setNom(selected.getNom());
                    adherentToDelete.setPrenom(selected.getPrenom());
                    
                    // Supprimer l'adhérent
                    adherentDAO.delete(selected.getId());
                    
                    // Créer notification
                    notificationService.notifyAdherentDeleted(adherentToDelete);
                    
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Adhérent supprimé avec succès");
                    loadAdherents();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
