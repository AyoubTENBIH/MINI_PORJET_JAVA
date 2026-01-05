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

    // Références aux composants UI (chargés depuis FXML)
    @FXML
    private HBox header;
    @FXML
    private Button menuBtn;
    @FXML
    private Label breadcrumbLabel;
    @FXML
    private Button moonBtn;
    @FXML
    private Button refreshBtn;
    @FXML
    private Button bellBtn;
    @FXML
    private Button globeBtn;
    @FXML
    private HBox titleFilterSection;
    @FXML
    private Label titleLabel;
    @FXML
    private ScrollPane contentScroll;
    @FXML
    private VBox contentWrapper;
    @FXML
    private VBox searchCard;
    @FXML
    private VBox tableCard;
    @FXML
    private TextField searchField;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TableView<Pack> packsTable;
    @FXML
    private TableColumn<Pack, String> nomColumn;
    @FXML
    private TableColumn<Pack, Double> prixColumn;
    @FXML
    private TableColumn<Pack, String> activitesColumn;
    @FXML
    private TableColumn<Pack, String> dureeColumn;
    @FXML
    private TableColumn<Pack, Boolean> actifColumn;

    /**
     * Charge la vue de gestion des packs depuis le FXML
     */
    public Parent getView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/packs.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            
            // Charger le CSS du module pack
            if (root.getScene() != null) {
                root.getScene().getStylesheets().add(
                    getClass().getResource("/css/packs.css").toExternalForm()
                );
            } else {
                // Si la scène n'existe pas encore, l'ajouter lors de l'ajout à la scène
                root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        newScene.getStylesheets().add(
                            getClass().getResource("/css/packs.css").toExternalForm()
                        );
                    }
                });
            }
            
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            // En cas d'erreur, créer une vue basique
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
        
        // Configurer les icônes SVG du header
        setupHeaderIcons();
        
        // Configurer les listeners et actions
        setupEventHandlers();
        
        // Configurer les colonnes de la table
        setupTableColumns();
        
        // Charger les packs
        loadPacks();
    }
    
    /**
     * Configure les event handlers pour les composants UI
     */
    private void setupEventHandlers() {
        // Champ de recherche
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> searchPacks(newVal));
        }
        
        // Boutons d'action
        if (addButton != null) {
            addButton.setOnAction(e -> showPackDialog(null));
        }
        if (editButton != null) {
            editButton.setOnAction(e -> editSelectedPack());
        }
        if (deleteButton != null) {
            deleteButton.setOnAction(e -> deleteSelectedPack());
        }
        
        // Table - double-clic pour modifier
        if (packsTable != null) {
            packsTable.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    editSelectedPack();
                }
            });
        }
        
        // Header buttons
        if (moonBtn != null) {
            moonBtn.setOnAction(e -> handleMoonClick());
        }
        if (refreshBtn != null) {
            refreshBtn.setOnAction(e -> loadPacks());
        }
        
        // Cacher le menu button
        if (menuBtn != null) {
            menuBtn.setVisible(false);
        }
    }
    
    /**
     * Gère le clic sur le bouton moon (thème)
     */
    private void handleMoonClick() {
        try {
            com.example.demo.services.ThemeService themeService = 
                com.example.demo.services.ThemeService.getInstance();
            if (moonBtn != null && moonBtn.getScene() != null) {
                themeService.toggleTheme(moonBtn.getScene());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Configure les colonnes de la table avec les cell factories et data bindings
     */
    private void setupTableColumns() {
        if (packsTable == null) return;
        
        // Configurer les cell value factories
        if (nomColumn != null) {
            nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        }
        
        if (prixColumn != null) {
            prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
            prixColumn.setCellFactory(column -> new TableCell<Pack, Double>() {
                @Override
                protected void updateItem(Double prix, boolean empty) {
                    super.updateItem(prix, empty);
                    if (empty || prix == null) {
                        setText("");
                        getStyleClass().clear();
                    } else {
                        setText(String.format("%.2f DH", prix));
                        getStyleClass().setAll("packs-table-cell-price");
                    }
                }
            });
        }
        
        if (activitesColumn != null) {
            activitesColumn.setCellValueFactory(new PropertyValueFactory<>("activitesAsString"));
        }
        
        if (dureeColumn != null) {
            dureeColumn.setCellValueFactory(cellData -> {
                Pack pack = cellData.getValue();
                return new javafx.beans.property.SimpleStringProperty(
                    pack.getDuree() + " " + pack.getUniteDuree().toLowerCase()
                );
            });
        }
        
        if (actifColumn != null) {
            actifColumn.setCellValueFactory(new PropertyValueFactory<>("actif"));
            actifColumn.setCellFactory(column -> new TableCell<Pack, Boolean>() {
                @Override
                protected void updateItem(Boolean actif, boolean empty) {
                    super.updateItem(actif, empty);
                    if (empty || actif == null) {
                        setText("");
                        getStyleClass().clear();
                    } else {
                        setText(actif ? "Actif" : "Inactif");
                        getStyleClass().setAll(actif ? "packs-table-cell-active" : "packs-table-cell-inactive");
                    }
                }
            });
        }
        
        // Lier les données à la table
        packsTable.setItems(packsList);
        
        // Faire en sorte que le tableau remplisse tout l'espace sans zone blanche
        packsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Ajuster dynamiquement la colonne Activités pour qu'elle prenne l'espace restant
        javafx.application.Platform.runLater(() -> {
            // Attendre que le tableau soit rendu avant de calculer
            if (packsTable != null) {
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
                if (nomColumn != null) {
                    nomColumn.widthProperty().addListener((obs, oldVal, newVal) -> updateActivitesColumnWidth());
                }
                if (prixColumn != null) {
                    prixColumn.widthProperty().addListener((obs, oldVal, newVal) -> updateActivitesColumnWidth());
                }
                if (dureeColumn != null) {
                    dureeColumn.widthProperty().addListener((obs, oldVal, newVal) -> updateActivitesColumnWidth());
                }
                if (actifColumn != null) {
                    actifColumn.widthProperty().addListener((obs, oldVal, newVal) -> updateActivitesColumnWidth());
                }
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
        if (dureeColumn != null) totalFixedWidth += dureeColumn.getWidth();
        
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
     * Vue de secours si le FXML ne charge pas
     */
    private Parent createBasicView() {
        // Créer une vue minimale en cas d'erreur de chargement FXML
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("packs-root");
        
        Label errorLabel = new Label("Erreur lors du chargement de l'interface. Veuillez vérifier le fichier FXML.");
        root.getChildren().add(errorLabel);
        
        return root;
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
        gridPane.getStyleClass().add("packs-dialog-grid");
        
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
        
        // Appliquer les classes CSS
        nomField.getStyleClass().add("packs-form-input");
        prixField.getStyleClass().add("packs-form-input");
        activitesField.getStyleClass().add("packs-form-input");
        dureeField.getStyleClass().add("packs-form-input");
        seancesField.getStyleClass().add("packs-form-input");
        uniteDureeCombo.getStyleClass().add("packs-form-combo");
        
        accesCoachCheck.getStyleClass().add("packs-form-checkbox");
        
        // Ajouter les champs au GridPane
        int row = 0;
        
        Label nomLabel = new Label("Nom:");
        nomLabel.getStyleClass().add("packs-form-label");
        gridPane.add(nomLabel, 0, row);
        gridPane.add(nomField, 1, row++);
        
        Label prixLabel = new Label("Prix (DH):");
        prixLabel.getStyleClass().add("packs-form-label");
        gridPane.add(prixLabel, 0, row);
        gridPane.add(prixField, 1, row++);
        
        Label activitesLabel = new Label("Activités:");
        activitesLabel.getStyleClass().add("packs-form-label");
        gridPane.add(activitesLabel, 0, row);
        gridPane.add(activitesField, 1, row++);
        
        // Durée et Unité sur la même ligne
        Label dureeLabel = new Label("Durée:");
        dureeLabel.getStyleClass().add("packs-form-label");
        gridPane.add(dureeLabel, 0, row);
        
        HBox dureeBox = new HBox(8);
        dureeBox.getChildren().addAll(dureeField, uniteDureeCombo);
        dureeField.setPrefWidth(100);
        uniteDureeCombo.setPrefWidth(172);
        gridPane.add(dureeBox, 1, row++);
        
        Label seancesLabel = new Label("Séances/semaine:");
        seancesLabel.getStyleClass().add("packs-form-label");
        gridPane.add(seancesLabel, 0, row);
        gridPane.add(seancesField, 1, row++);
        
        // CheckBox sur toute la largeur
        gridPane.add(accesCoachCheck, 0, row, 2, 1);

        dialog.getDialogPane().setContent(gridPane);
        dialog.getDialogPane().getStyleClass().add("packs-dialog");
        dialog.getDialogPane().setPrefWidth(620);
        dialog.getDialogPane().setPrefHeight(600);
        
        // Charger le CSS pour le dialog
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/packs.css").toExternalForm()
        );
        
        // Appliquer les styles CSS aux éléments du dialog
        Platform.runLater(() -> {
            try {
                javafx.scene.Node header = dialog.getDialogPane().lookup(".header-panel");
                if (header != null) {
                    header.getStyleClass().add("packs-dialog-header");
                }
                
                java.util.Set<javafx.scene.Node> headerLabels = dialog.getDialogPane().lookupAll(".header-panel .label");
                for (javafx.scene.Node label : headerLabels) {
                    label.getStyleClass().add("packs-dialog-header-label");
                }
                
                java.util.Set<javafx.scene.Node> contentLabels = dialog.getDialogPane().lookupAll(".content-label");
                for (javafx.scene.Node label : contentLabels) {
                    label.getStyleClass().add("packs-dialog-content-label");
                }
            } catch (Exception e) {
                // Ignorer les erreurs de lookup
            }
        });
        
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Appliquer les classes CSS aux boutons du dialog
        Platform.runLater(() -> {
            try {
                Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
                if (saveButton != null) {
                    saveButton.getStyleClass().add("packs-dialog-btn-save");
                }
                
                Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
                if (cancelButton != null) {
                    cancelButton.getStyleClass().add("packs-dialog-btn-cancel");
                }
                
                javafx.scene.Node buttonBar = dialog.getDialogPane().lookup(".button-bar");
                if (buttonBar != null) {
                    buttonBar.getStyleClass().add("packs-dialog-button-bar");
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

    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Configure les icônes SVG pour les boutons du header
     */
    private void setupHeaderIcons() {
        if (menuBtn != null) {
            menuBtn.setGraphic(createSVGIcon("icon-menu", 20));
        }
        if (moonBtn != null) {
            moonBtn.setGraphic(createSVGIcon("icon-moon", 20));
        }
        if (refreshBtn != null) {
            refreshBtn.setGraphic(createSVGIcon("icon-refresh", 20));
        }
        if (bellBtn != null) {
            bellBtn.setGraphic(createSVGIcon("icon-bell", 20));
        }
        if (globeBtn != null) {
            globeBtn.setGraphic(createSVGIcon("icon-globe", 20));
        }
    }
    
    /**
     * Crée une icône SVG pour les boutons
     */
    private Node createSVGIcon(String iconName, double size) {
        try {
            String svgPath = getSvgPathForIcon(iconName);
            if (svgPath != null && !svgPath.isEmpty()) {
                SVGPath svgPathNode = new SVGPath();
                svgPathNode.setContent(svgPath);
                svgPathNode.setFill(null);
                svgPathNode.setStroke(Color.web("#9AA4B2"));
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
                container.setBackground(Background.EMPTY);
                
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
            case "icon-moon" -> SvgIcons.MOON;
            case "icon-refresh" -> SvgIcons.REFRESH;
            case "icon-bell" -> SvgIcons.BELL;
            case "icon-globe" -> SvgIcons.GLOBE;
            default -> null;
        };
    }
}




