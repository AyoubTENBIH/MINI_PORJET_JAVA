package com.example.demo.controllers;

import com.example.demo.dao.PackDAO;
import com.example.demo.models.Pack;
import com.example.demo.services.NotificationService;
import com.example.demo.services.ActivityService;
import com.example.demo.utils.SvgIcons;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrôleur pour la gestion des packs/abonnements
 */
public class PackManagementController {
    private PackDAO packDAO = new PackDAO();
    private NotificationService notificationService = NotificationService.getInstance();
    private ActivityService activityService = ActivityService.getInstance();
    private ObservableList<Pack> packsList = FXCollections.observableArrayList();

    @FXML
    private TableView<Pack> packsTable;
    @FXML
    private TableColumn<Pack, String> nomColumn;
    @FXML
    private TableColumn<Pack, Double> prixColumn;
    @FXML
    private TableColumn<Pack, String> activitesColumn;
    @FXML
    private TableColumn<Pack, Boolean> actifColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    /**
     * Charge la vue de gestion des packs - Utilise toujours createBasicView() pour le nouveau design
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
        
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> searchPacks(newVal));
        }
        if (addButton != null) {
            addButton.setOnAction(e -> showPackDialog(null));
        }
        if (editButton != null) {
            editButton.setOnAction(e -> editSelectedPack());
        }
        if (deleteButton != null) {
            deleteButton.setOnAction(e -> deleteSelectedPack());
        }
        setupTableColumns();
        loadPacks();
    }

    /**
     * Configure les colonnes de la table avec styles dark theme
     */
    private void setupTableColumns() {
        if (packsTable == null) return;
        packsTable.getColumns().clear();
        packsTable.setPrefHeight(500);
        packsTable.setMaxWidth(Double.MAX_VALUE);
        packsTable.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-table-cell-border-color: transparent;"
        );

        nomColumn = new TableColumn<>("Nom");
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomColumn.setMinWidth(150);
        nomColumn.setPrefWidth(180);
        nomColumn.setMaxWidth(250);
        nomColumn.setResizable(true);
        nomColumn.setStyle("-fx-alignment: center-left;");

        prixColumn = new TableColumn<>("Prix (DH)");
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        prixColumn.setMinWidth(100);
        prixColumn.setPrefWidth(120);
        prixColumn.setMaxWidth(150);
        prixColumn.setResizable(true);
        prixColumn.setStyle("-fx-alignment: center-left;");
        prixColumn.setCellFactory(column -> new TableCell<Pack, Double>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(String.format("%.2f DH", prix));
                    setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 14px;");
                }
            }
        });

        activitesColumn = new TableColumn<>("Activités");
        activitesColumn.setCellValueFactory(new PropertyValueFactory<>("activitesAsString"));
        activitesColumn.setMinWidth(200);
        activitesColumn.setPrefWidth(300);
        // Pas de maxWidth pour permettre à cette colonne de prendre l'espace restant
        activitesColumn.setResizable(true);
        activitesColumn.setStyle("-fx-alignment: center-left;");

        TableColumn<Pack, String> dureeColumn = new TableColumn<>("Durée");
        dureeColumn.setCellValueFactory(cellData -> {
            Pack pack = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                pack.getDuree() + " " + pack.getUniteDuree().toLowerCase()
            );
        });
        dureeColumn.setMinWidth(80);
        dureeColumn.setPrefWidth(100);
        dureeColumn.setMaxWidth(150);
        dureeColumn.setResizable(true);
        dureeColumn.setStyle("-fx-alignment: center-left;");

        actifColumn = new TableColumn<>("Statut");
        actifColumn.setCellValueFactory(new PropertyValueFactory<>("actif"));
        actifColumn.setMinWidth(80);
        actifColumn.setPrefWidth(100);
        actifColumn.setMaxWidth(120);
        actifColumn.setResizable(true);
        actifColumn.setStyle("-fx-alignment: center-left;");
        actifColumn.setCellFactory(column -> new TableCell<Pack, Boolean>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(actif ? "Actif" : "Inactif");
                    setStyle(
                        actif ? 
                        "-fx-text-fill: #10b981; -fx-font-size: 14px; -fx-font-weight: 600;" :
                        "-fx-text-fill: #ef4444; -fx-font-size: 14px; -fx-font-weight: 600;"
                    );
                }
            }
        });

        packsTable.getColumns().addAll(nomColumn, prixColumn, activitesColumn, dureeColumn, actifColumn);
        packsTable.setItems(packsList);
        
        // Faire en sorte que le tableau remplisse tout l'espace sans zone blanche
        packsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Ajuster dynamiquement la colonne Activités pour qu'elle prenne l'espace restant
        // et éliminer la zone blanche
        Platform.runLater(() -> {
            // Attendre que le tableau soit rendu avant de calculer
            packsTable.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.getWidth() > 0) {
                    updateActivitesColumnWidth();
                }
            });
            
            // Calculer initialement après un court délai
            javafx.application.Platform.runLater(() -> {
                if (packsTable.getWidth() > 0) {
                    updateActivitesColumnWidth();
                }
            });
            
            // Écouter les changements de largeur du tableau
            packsTable.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) {
                    updateActivitesColumnWidth();
                }
            });
            
            // Écouter aussi les changements de largeur des autres colonnes
            nomColumn.widthProperty().addListener((obs, oldVal, newVal) -> updateActivitesColumnWidth());
            prixColumn.widthProperty().addListener((obs, oldVal, newVal) -> updateActivitesColumnWidth());
            dureeColumn.widthProperty().addListener((obs, oldVal, newVal) -> updateActivitesColumnWidth());
            actifColumn.widthProperty().addListener((obs, oldVal, newVal) -> updateActivitesColumnWidth());
        });
        
        // Appliquer les styles aux colonnes via CSS
        Platform.runLater(() -> {
            try {
                // Style des headers
                Node header = packsTable.lookup(".column-header-background");
                if (header != null) {
                    header.setStyle("-fx-background-color: rgba(42, 52, 65, 0.5);");
                }
                
                // Style des column headers
                java.util.Set<Node> columnHeaders = packsTable.lookupAll(".column-header");
                for (Node headerNode : columnHeaders) {
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
                java.util.Set<Node> rows = packsTable.lookupAll(".table-row-cell");
                for (Node row : rows) {
                    row.setStyle(
                        "-fx-background-color: rgba(42, 52, 65, 0.3); " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.05);"
                    );
                }
                
                // Style des cells
                java.util.Set<Node> cells = packsTable.lookupAll(".table-cell");
                for (Node cell : cells) {
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
    }

    /**
     * Met à jour la largeur de la colonne Activités pour qu'elle prenne l'espace restant
     * et élimine la zone blanche dans le tableau
     */
    private void updateActivitesColumnWidth() {
        if (packsTable == null || activitesColumn == null) return;
        
        double tableWidth = packsTable.getWidth();
        if (tableWidth <= 0) return;
        
        // Calculer la largeur totale des autres colonnes
        double totalFixedWidth = 0;
        if (nomColumn != null) totalFixedWidth += nomColumn.getWidth();
        if (prixColumn != null) totalFixedWidth += prixColumn.getWidth();
        if (actifColumn != null) totalFixedWidth += actifColumn.getWidth();
        
        // Trouver la colonne Durée
        TableColumn<Pack, String> dureeColumn = null;
        for (TableColumn<Pack, ?> col : packsTable.getColumns()) {
            if ("Durée".equals(col.getText())) {
                dureeColumn = (TableColumn<Pack, String>) col;
                break;
            }
        }
        if (dureeColumn != null) {
            totalFixedWidth += dureeColumn.getWidth();
        }
        
        // Largeur approximative de la scrollbar verticale
        double scrollbarWidth = 18;
        
        // Calculer l'espace disponible pour la colonne Activités
        double availableWidth = tableWidth - totalFixedWidth - scrollbarWidth;
        
        // Ajuster la largeur de la colonne Activités si l'espace disponible est suffisant
        if (availableWidth > activitesColumn.getMinWidth()) {
            activitesColumn.setPrefWidth(availableWidth);
        } else {
            activitesColumn.setPrefWidth(activitesColumn.getMinWidth());
        }
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
        
        // Charger les packs
        loadPacks();
        
        return root;
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
        searchField.setPromptText("Rechercher un pack...");
        searchField.setPrefWidth(400);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> searchPacks(newVal));
        searchField.setStyle(
            "-fx-background-color: rgba(15, 23, 42, 0.6); " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 1px; " +
            "-fx-border-color: rgba(255, 255, 255, 0.1); " +
            "-fx-border-radius: 12px;"
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
                    "-fx-border-radius: 12px;"
                );
            } else {
                searchField.setStyle(baseTextFieldStyle);
            }
        });
        
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        // Bouton "+ Nouveau Pack" (Success)
        addButton = new Button("+ Nouveau Pack");
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
        addButton.setOnAction(e -> showPackDialog(null));
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
        editButton.setOnAction(e -> editSelectedPack());
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
        deleteButton.setOnAction(e -> deleteSelectedPack());
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
     * Crée la card pour la table des packs
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
        
        // Table des packs
        packsTable = new TableView<>();
        packsTable.setPrefHeight(500);
        packsTable.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-table-cell-border-color: transparent;"
        );
        
        setupTableColumns();
        
        // Double-clic pour modifier
        packsTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                editSelectedPack();
            }
        });
        
        container.getChildren().add(packsTable);
        
        return container;
    }

    /**
     * Charge tous les packs
     */
    private void loadPacks() {
        try {
            List<Pack> packs = packDAO.findAll();
            packsList.clear();
            packsList.addAll(packs);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des packs: " + e.getMessage());
        }
    }

    /**
     * Recherche des packs
     */
    private void searchPacks(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadPacks();
            return;
        }

        try {
            List<Pack> packs = packDAO.searchByNom(searchTerm);
            packsList.clear();
            packsList.addAll(packs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Affiche le dialogue pour créer/modifier un pack
     */
    private void showPackDialog(Pack pack) {
        // Créer un dialogue simple pour l'instant
        Dialog<Pack> dialog = new Dialog<>();
        dialog.setTitle(pack == null ? "Nouveau Pack" : "Modifier Pack");
        dialog.setHeaderText(pack == null ? "Créer un nouveau pack" : "Modifier le pack");

        // Champs du formulaire
        TextField nomField = new TextField();
        nomField.setPromptText("Nom du pack");
        
        TextField prixField = new TextField();
        prixField.setPromptText("Prix (DH)");

        TextField activitesField = new TextField();
        activitesField.setPromptText("Activités (séparées par des virgules)");

        TextField dureeField = new TextField();
        dureeField.setPromptText("Durée (ex: 1)");

        ComboBox<String> uniteDureeCombo = new ComboBox<>();
        uniteDureeCombo.getItems().addAll("MOIS", "SEMAINE", "JOUR", "ANNEE");
        uniteDureeCombo.setValue("MOIS");

        TextField seancesField = new TextField();
        seancesField.setPromptText("Séances/semaine (-1 pour illimité)");

        CheckBox accesCoachCheck = new CheckBox("Accès coach personnel");

        if (pack != null) {
            nomField.setText(pack.getNom());
            prixField.setText(String.valueOf(pack.getPrix()));
            activitesField.setText(pack.getActivitesAsString());
            dureeField.setText(String.valueOf(pack.getDuree()));
            uniteDureeCombo.setValue(pack.getUniteDuree());
            seancesField.setText(pack.getSeancesSemaine() != null ? String.valueOf(pack.getSeancesSemaine()) : "-1");
            accesCoachCheck.setSelected(pack.getAccesCoach() != null && pack.getAccesCoach());
        }

        // Créer GridPane pour une structure améliorée avec espacement généreux
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(24, 32, 24, 32)); // 24px haut/bas, 32px côtés
        gridPane.setHgap(12); // Gap entre label et input
        gridPane.setVgap(24); // Espacement vertical de 24px entre les champs
        gridPane.setStyle("-fx-background-color: #1A2332;");
        
        // Définir les contraintes de colonnes
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(140);
        labelColumn.setPrefWidth(140);
        labelColumn.setMaxWidth(140);
        
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setMinWidth(320);
        fieldColumn.setPrefWidth(320);
        fieldColumn.setHgrow(Priority.ALWAYS);
        
        gridPane.getColumnConstraints().addAll(labelColumn, fieldColumn);
        
        // Style amélioré pour les labels - blanc cassé, plus grande
        String labelStyle = "-fx-text-fill: #f3f4f6; -fx-font-size: 15px; -fx-font-weight: 500;";
        
        // Style amélioré pour les TextFields - meilleur contraste et focus
        String textFieldStyle = 
            "-fx-background-color: #374151; " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #f3f4f6; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-color: #4b5563; " +
            "-fx-border-radius: 12px; " +
            "-fx-prompt-text-fill: #9ca3af;";
        
        // Style pour ComboBox amélioré
        String comboBoxStyle =
            "-fx-background-color: #374151; " +
            "-fx-background-radius: 12px; " +
            "-fx-text-fill: #f3f4f6; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 12px 16px; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-color: #4b5563; " +
            "-fx-border-radius: 12px;";
        
        // Appliquer les styles avec gestion du focus pour meilleur contraste
        nomField.setStyle(textFieldStyle);
        prixField.setStyle(textFieldStyle);
        activitesField.setStyle(textFieldStyle);
        dureeField.setStyle(textFieldStyle);
        seancesField.setStyle(textFieldStyle);
        uniteDureeCombo.setStyle(comboBoxStyle);
        
        // Ajouter les styles de focus pour tous les TextFields
        String focusStyle = "-fx-border-color: #6366f1; -fx-border-width: 2px;";
        nomField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                nomField.setStyle(textFieldStyle + focusStyle);
            } else {
                nomField.setStyle(textFieldStyle);
            }
        });
        prixField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                prixField.setStyle(textFieldStyle + focusStyle);
            } else {
                prixField.setStyle(textFieldStyle);
            }
        });
        activitesField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                activitesField.setStyle(textFieldStyle + focusStyle);
            } else {
                activitesField.setStyle(textFieldStyle);
            }
        });
        dureeField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                dureeField.setStyle(textFieldStyle + focusStyle);
            } else {
                dureeField.setStyle(textFieldStyle);
            }
        });
        seancesField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                seancesField.setStyle(textFieldStyle + focusStyle);
            } else {
                seancesField.setStyle(textFieldStyle);
            }
        });
        
        // Styliser le label interne du ComboBox pour que la valeur sélectionnée soit visible
        uniteDureeCombo.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(
                        "-fx-background-color: #374151; " +
                        "-fx-text-fill: #f3f4f6; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8px 12px;"
                    );
                }
            }
        });
        
        // Styliser le buttonCell (label qui affiche la valeur sélectionnée)
        uniteDureeCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #f3f4f6; -fx-font-size: 14px;");
                }
            }
        });
        
        // Ajouter le style de focus pour le ComboBox
        uniteDureeCombo.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                uniteDureeCombo.setStyle(comboBoxStyle + " -fx-border-color: #6366f1; -fx-border-width: 2px;");
            } else {
                uniteDureeCombo.setStyle(comboBoxStyle);
            }
        });
        accesCoachCheck.setStyle("-fx-text-fill: #f3f4f6; -fx-font-size: 14px;");
        
        // Ajouter les champs au GridPane
        int row = 0;
        
        Label nomLabel = new Label("Nom:");
        nomLabel.setStyle(labelStyle);
        gridPane.add(nomLabel, 0, row);
        gridPane.add(nomField, 1, row++);
        
        Label prixLabel = new Label("Prix (DH):");
        prixLabel.setStyle(labelStyle);
        gridPane.add(prixLabel, 0, row);
        gridPane.add(prixField, 1, row++);
        
        Label activitesLabel = new Label("Activités:");
        activitesLabel.setStyle(labelStyle);
        gridPane.add(activitesLabel, 0, row);
        gridPane.add(activitesField, 1, row++);
        
        // Durée et Unité sur la même ligne
        Label dureeLabel = new Label("Durée:");
        dureeLabel.setStyle(labelStyle);
        gridPane.add(dureeLabel, 0, row);
        
        HBox dureeBox = new HBox(8);
        dureeBox.getChildren().addAll(dureeField, uniteDureeCombo);
        dureeField.setPrefWidth(100);
        uniteDureeCombo.setPrefWidth(172);
        gridPane.add(dureeBox, 1, row++);
        
        Label seancesLabel = new Label("Séances/semaine:");
        seancesLabel.setStyle(labelStyle);
        gridPane.add(seancesLabel, 0, row);
        gridPane.add(seancesField, 1, row++);
        
        // CheckBox sur toute la largeur
        gridPane.add(accesCoachCheck, 0, row, 2, 1);

        dialog.getDialogPane().setContent(gridPane);
        dialog.getDialogPane().setStyle("-fx-background-color: #1A2332;");
        dialog.getDialogPane().setPrefWidth(620);
        dialog.getDialogPane().setPrefHeight(600);
        
        // Styliser l'en-tête du modal avec gris ardoise élégant
        Platform.runLater(() -> {
            try {
                // Styliser le header du DialogPane
                javafx.scene.Node header = dialog.getDialogPane().lookup(".header-panel");
                if (header != null) {
                    header.setStyle(
                        "-fx-background-color: #475569; " +
                        "-fx-background: #475569; " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.1); " +
                        "-fx-padding: 20px 32px;"
                    );
                }
                
                // Styliser tous les labels dans le header
                java.util.Set<javafx.scene.Node> headerLabels = dialog.getDialogPane().lookupAll(".header-panel .label");
                for (javafx.scene.Node label : headerLabels) {
                    label.setStyle(
                        "-fx-text-fill: #ffffff; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: 600;"
                    );
                }
                
                // Styliser le content label (sous-titre)
                java.util.Set<javafx.scene.Node> contentLabels = dialog.getDialogPane().lookupAll(".content-label");
                for (javafx.scene.Node label : contentLabels) {
                    label.setStyle(
                        "-fx-text-fill: rgba(255, 255, 255, 0.9); " +
                        "-fx-font-size: 14px;"
                    );
                }
                
                // Styliser aussi via le style du DialogPane directement
                dialog.getDialogPane().getStylesheets().clear();
            } catch (Exception e) {
                // Ignorer les erreurs de lookup
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
                        "-fx-background-color: #10b981; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-size: 15px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-padding: 12px 32px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-min-width: 140px; " +
                        "-fx-min-height: 44px;"
                    );
                    
                    // Effet hover pour le bouton Enregistrer
                    saveButton.setOnMouseEntered(e -> {
                        saveButton.setStyle(
                            "-fx-background-color: #059669; " +
                            "-fx-text-fill: #FFFFFF; " +
                            "-fx-font-size: 15px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-padding: 12px 32px; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-cursor: hand; " +
                            "-fx-min-width: 140px; " +
                            "-fx-min-height: 44px;"
                        );
                    });
                    saveButton.setOnMouseExited(e -> {
                        saveButton.setStyle(
                            "-fx-background-color: #10b981; " +
                            "-fx-text-fill: #FFFFFF; " +
                            "-fx-font-size: 15px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-padding: 12px 32px; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-cursor: hand; " +
                            "-fx-min-width: 140px; " +
                            "-fx-min-height: 44px;"
                        );
                    });
                }
                
                Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
                if (cancelButton != null) {
                    cancelButton.setStyle(
                        "-fx-background-color: #4b5563; " +
                        "-fx-text-fill: #f3f4f6; " +
                        "-fx-font-size: 15px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-padding: 12px 32px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-min-width: 140px; " +
                        "-fx-min-height: 44px;"
                    );
                    
                    // Effet hover pour le bouton Annuler
                    cancelButton.setOnMouseEntered(e -> {
                        cancelButton.setStyle(
                            "-fx-background-color: #6b7280; " +
                            "-fx-text-fill: #f3f4f6; " +
                            "-fx-font-size: 15px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-padding: 12px 32px; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-cursor: hand; " +
                            "-fx-min-width: 140px; " +
                            "-fx-min-height: 44px;"
                        );
                    });
                    cancelButton.setOnMouseExited(e -> {
                        cancelButton.setStyle(
                            "-fx-background-color: #4b5563; " +
                            "-fx-text-fill: #f3f4f6; " +
                            "-fx-font-size: 15px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-padding: 12px 32px; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-cursor: hand; " +
                            "-fx-min-width: 140px; " +
                            "-fx-min-height: 44px;"
                        );
                    });
                }
                
                // Espacement de 16px entre les boutons
                javafx.scene.Node buttonBar = dialog.getDialogPane().lookup(".button-bar");
                if (buttonBar != null) {
                    buttonBar.setStyle("-fx-spacing: 16px; -fx-padding: 20px 32px;");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Pack newPack = pack != null ? pack : new Pack();
                    newPack.setNom(nomField.getText());
                    newPack.setPrix(Double.parseDouble(prixField.getText()));
                    newPack.setActivitesFromString(activitesField.getText());
                    newPack.setDuree(Integer.parseInt(dureeField.getText()));
                    newPack.setUniteDuree(uniteDureeCombo.getValue());
                    newPack.setSeancesSemaine(Integer.parseInt(seancesField.getText()));
                    newPack.setAccesCoach(accesCoachCheck.isSelected());
                    return newPack;
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
                    // Création d'un nouveau pack
                    packDAO.create(result);
                    
                    // Créer notification et activité
                    notificationService.notifyPackCreated(result);
                    activityService.logPackCreated(result.getId(), result.getNom());
                    
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Pack créé avec succès");
                } else {
                    // Modification d'un pack existant
                    packDAO.update(result);
                    
                    // Créer notification et activité
                    notificationService.notifyPackUpdated(result);
                    activityService.logPackUpdated(result.getId(), result.getNom());
                    
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Pack modifié avec succès");
                }
                loadPacks();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            }
        });
    }

    /**
     * Modifie le pack sélectionné
     */
    private void editSelectedPack() {
        Pack selected = packsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un pack à modifier");
            return;
        }
        showPackDialog(selected);
    }

    /**
     * Supprime le pack sélectionné
     */
    private void deleteSelectedPack() {
        Pack selected = packsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un pack à supprimer");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le pack");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer le pack \"" + selected.getNom() + "\" ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    initializeServices(); // S'assurer que les services sont initialisés
                    
                    // Conserver les informations du pack avant suppression pour la notification
                    Pack packToDelete = new Pack();
                    packToDelete.setId(selected.getId());
                    packToDelete.setNom(selected.getNom());
                    
                    // Supprimer le pack
                    packDAO.delete(selected.getId());
                    
                    // Créer notification et activité
                    notificationService.notifyPackDeleted(packToDelete);
                    activityService.logProductArchived(packToDelete.getId(), packToDelete.getNom());
                    
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Pack supprimé avec succès");
                    loadPacks();
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
    
    /**
     * Crée le header avec menu, star, breadcrumb et icônes utilitaires (identique au dashboard)
     */
    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(16, 32, 16, 32));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(70);
        header.setStyle("-fx-background-color: #0A0D12; -fx-border-width: 0 0 1 0; -fx-border-color: rgba(154, 164, 178, 0.1);");
        
        // Menu icon (pas de fonction pour packs, peut être caché)
        Button menuBtn = createHeaderIconButton("icon-menu", 20);
        menuBtn.setVisible(false); // Caché pour packs
        
        // Star icon (Favoris)
        Button starBtn = createHeaderIconButton("icon-star", 20);
        starBtn.setOnAction(e -> {
            try {
                com.example.demo.dao.FavorisDAO favorisDAO = new com.example.demo.dao.FavorisDAO();
                String pageName = "PACKS";
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
        Label breadcrumbLabel = new Label("Gestion des Packs");
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
        refreshBtn.setOnAction(e -> loadPacks());
        
        // Bell icon (Notifications) - simplifié pour packs
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
        
        // Titre "Gestion des Packs/Abonnements" à gauche
        Label titleLabel = new Label("Gestion des Packs/Abonnements");
        titleLabel.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 24px; -fx-font-weight: 700;");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        section.getChildren().addAll(titleLabel, spacer);
        
        return section;
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
}




