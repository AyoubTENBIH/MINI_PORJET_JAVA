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

    // Références aux composants UI (chargés depuis FXML)
    @FXML private HBox header;
    @FXML private Button menuBtn;
    @FXML private Label breadcrumbLabel;
    @FXML private Button moonBtn;
    @FXML private Button refreshBtn;
    @FXML private Button bellBtn;
    @FXML private Button globeBtn;
    @FXML private HBox titleFilterSection;
    @FXML private Label titleLabel;
    @FXML private ScrollPane contentScroll;
    @FXML private VBox contentWrapper;
    @FXML private VBox searchCard;
    @FXML private VBox tableCard;
    @FXML private HBox searchBar;

    /**
     * Charge la vue de gestion des adhérents depuis le FXML
     */
    public Parent getView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/adherents.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            
            // Charger le CSS du module adhérent
            if (root.getScene() != null) {
                root.getScene().getStylesheets().add(
                    getClass().getResource("/css/adherents.css").toExternalForm()
                );
            } else {
                // Si la scène n'existe pas encore, l'ajouter lors de l'ajout à la scène
                root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        newScene.getStylesheets().add(
                            getClass().getResource("/css/adherents.css").toExternalForm()
                        );
                    }
                });
            }
            
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du FXML adherents: " + e.getMessage());
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
        
        // Configurer le header
        setupHeader();
        
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
        
        // Configurer la politique de redimensionnement des colonnes
        if (adherentsTable != null) {
            adherentsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        }

        // Charger les données
        loadAdherents();
    }
    
    /**
     * Configure le header avec les icônes SVG
     */
    private void setupHeader() {
        // Configurer les icônes SVG
        if (menuBtn != null) {
            setupHeaderIcon(menuBtn, "icon-menu");
        }
        if (moonBtn != null) {
            setupHeaderIcon(moonBtn, "icon-moon");
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
        }
        if (refreshBtn != null) {
            setupHeaderIcon(refreshBtn, "icon-refresh");
            refreshBtn.setOnAction(e -> loadAdherents());
        }
        if (bellBtn != null) {
            setupHeaderIcon(bellBtn, "icon-bell");
        }
        if (globeBtn != null) {
            setupHeaderIcon(globeBtn, "icon-globe");
        }
    }
    
    /**
     * Configure une icône SVG pour un bouton du header
     */
    private void setupHeaderIcon(Button button, String iconName) {
        Node icon = loadSVGIcon(iconName, 20, "#9AA4B2");
        if (icon != null && button.getGraphic() != null) {
            StackPane container = (StackPane) button.getGraphic();
            if (container.getChildren().size() > 0) {
                container.getChildren().set(0, icon);
            }
        }
    }

    /**
     * Configure les colonnes de la table (logique métier uniquement)
     * Les colonnes sont déjà définies dans le FXML
     */
    private void setupTableColumns() {
        if (adherentsTable == null) return;

        // Configurer les cellValueFactory et cellFactory (logique métier)
        if (cinColumn != null) {
            cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
            cinColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                    getStyleClass().setAll("adherents-table-cell");
                }
            });
        }

        if (nomColumn != null) {
            nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
            nomColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                    getStyleClass().setAll("adherents-table-cell");
                }
            });
        }

        if (prenomColumn != null) {
            prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
            prenomColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                    getStyleClass().setAll("adherents-table-cell");
                }
            });
        }

        if (telephoneColumn != null) {
            telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
            telephoneColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                    getStyleClass().setAll("adherents-table-cell");
                }
            });
        }

        if (packColumn != null) {
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
            packColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                    getStyleClass().setAll("adherents-table-cell");
                }
            });
        }

        if (dateDebutColumn != null) {
            dateDebutColumn.setCellValueFactory(cellData -> {
                Adherent adherent = cellData.getValue();
                return new javafx.beans.property.SimpleStringProperty(
                    adherent.getDateDebut() != null ? adherent.getDateDebut().toString() : "N/A"
                );
            });
            dateDebutColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                    getStyleClass().setAll("adherents-table-cell", "date-fin-cell");
                }
            });
        }

        if (dateFinColumn != null) {
            dateFinColumn.setCellValueFactory(cellData -> {
                Adherent adherent = cellData.getValue();
                return new javafx.beans.property.SimpleStringProperty(
                    adherent.getDateFin() != null ? adherent.getDateFin().toString() : "N/A"
                );
            });
            dateFinColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
                @Override
                protected void updateItem(String dateFin, boolean empty) {
                    super.updateItem(dateFin, empty);
                    getStyleClass().clear();
                    getStyleClass().add("adherents-table-cell");
                    getStyleClass().add("date-fin-cell");
                    if (empty || dateFin == null || "N/A".equals(dateFin)) {
                        setText(empty ? "" : dateFin);
                    } else {
                        setText(dateFin);
                        // Vérifier si la date est expirée (logique métier)
                        Adherent adherent = getTableView().getItems().get(getIndex());
                        if (adherent != null && adherent.isAbonnementExpire()) {
                            getStyleClass().add("expired");
                        } else if (adherent != null && adherent.isAbonnementExpireBientot()) {
                            getStyleClass().add("expiring");
                        } else {
                            getStyleClass().add("active");
                        }
                    }
                }
            });
        }

        if (statutColumn != null) {
            statutColumn.setCellValueFactory(cellData -> {
                Adherent adherent = cellData.getValue();
                String statut = adherent.isAbonnementExpire() ? "Expiré" : "Actif";
                return new javafx.beans.property.SimpleStringProperty(statut);
            });
            statutColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
                @Override
                protected void updateItem(String statut, boolean empty) {
                    super.updateItem(statut, empty);
                    getStyleClass().clear();
                    getStyleClass().add("adherents-table-cell");
                    getStyleClass().add("statut-cell");
                    if (empty || statut == null) {
                        setText("");
                    } else {
                        setText(statut);
                        if ("Actif".equals(statut)) {
                            getStyleClass().add("active");
                        } else {
                            getStyleClass().add("expired");
                        }
                    }
                }
            });
        }

        // Configurer les données et comportements
        adherentsTable.setItems(adherentsList);
        
        // Ajuster dynamiquement la colonne Pack pour qu'elle prenne l'espace restant
        Platform.runLater(() -> {
            adherentsTable.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.getWidth() > 0) {
                    updatePackColumnWidth();
                }
            });
            
            Platform.runLater(() -> {
                if (adherentsTable.getWidth() > 0) {
                    updatePackColumnWidth();
                }
            });
            
            adherentsTable.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) {
                    updatePackColumnWidth();
                }
            });
            
            // Écouter les changements de largeur des colonnes
            if (cinColumn != null) cinColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            if (nomColumn != null) nomColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            if (prenomColumn != null) prenomColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            if (telephoneColumn != null) telephoneColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            if (dateDebutColumn != null) dateDebutColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            if (dateFinColumn != null) dateFinColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
            if (statutColumn != null) statutColumn.widthProperty().addListener((obs, oldVal, newVal) -> updatePackColumnWidth());
        });
        
        // Configurer le rowFactory pour le double-clic
        adherentsTable.setRowFactory(tv -> {
            TableRow<Adherent> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    editSelectedAdherent();
                }
            });
            return row;
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
     * Vue de secours minimale si le FXML ne charge pas
     */
    private Parent createBasicView() {
        initializeServices();
        VBox errorView = new VBox(10);
        errorView.setAlignment(Pos.CENTER);
        errorView.setPadding(new Insets(20));
        Label errorLabel = new Label("Erreur lors du chargement de l'interface. Veuillez vérifier le fichier FXML.");
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 14px;");
        errorView.getChildren().add(errorLabel);
        return errorView;
    }
    
    /**
     * Charge une icône SVG (utilisé uniquement pour les icônes du header)
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
                container.getStyleClass().add("icon-container");
                container.getChildren().add(svgPathNode);
                
                svgPathNode.getStyleClass().add("icon-svg");
                
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
        
        // S'assurer que le CSS est chargé pour le dialog
        if (adherentsTable != null && adherentsTable.getScene() != null) {
            dialog.initOwner(adherentsTable.getScene().getWindow());
        }

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
        gridPane.getStyleClass().add("adherents-dialog-grid");
        
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
        
        // Appliquer les classes CSS aux champs
        cinField.getStyleClass().add("adherents-dialog-textfield");
        nomField.getStyleClass().add("adherents-dialog-textfield");
        prenomField.getStyleClass().add("adherents-dialog-textfield");
        telephoneField.getStyleClass().add("adherents-dialog-textfield");
        emailField.getStyleClass().add("adherents-dialog-textfield-email");
        poidsField.getStyleClass().add("adherents-dialog-textfield");
        tailleField.getStyleClass().add("adherents-dialog-textfield");
        adresseArea.getStyleClass().add("adherents-dialog-textarea");
        objectifsArea.getStyleClass().add("adherents-dialog-textarea");
        problemesArea.getStyleClass().add("adherents-dialog-textarea");
        
        // Appliquer les classes CSS aux DatePicker
        dateNaissancePicker.getStyleClass().add("adherents-dialog-datepicker");
        dateDebutPicker.getStyleClass().add("adherents-dialog-datepicker");
        dateFinPicker.getStyleClass().add("adherents-dialog-datepicker");
        
        // Appliquer la classe CSS au ComboBox
        packCombo.getStyleClass().add("adherents-dialog-combobox");
        
        // Styliser le ComboBox Pack pour que la valeur sélectionnée soit visible
        packCombo.setCellFactory(listView -> new ListCell<Pack>() {
            @Override
            protected void updateItem(Pack pack, boolean empty) {
                super.updateItem(pack, empty);
                getStyleClass().clear();
                if (empty || pack == null) {
                    setText(null);
                } else {
                    setText(pack.getNom());
                    getStyleClass().add("adherents-dialog-combobox-cell");
                }
            }
        });
        
        // Styliser le buttonCell (label qui affiche la valeur sélectionnée)
        packCombo.setButtonCell(new ListCell<Pack>() {
            @Override
            protected void updateItem(Pack pack, boolean empty) {
                super.updateItem(pack, empty);
                getStyleClass().clear();
                if (empty || pack == null) {
                    setText(null);
                } else {
                    setText(pack.getNom());
                    getStyleClass().add("adherents-dialog-combobox-button-cell");
                }
            }
        });
        
        
        // Ajouter les champs au GridPane
        int row = 0;
        
        // Première ligne: CIN, Nom
        Label cinLabel = new Label("CIN:");
        cinLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(cinLabel, 0, row);
        gridPane.add(cinField, 1, row);
        Label nomLabel = new Label("Nom:");
        nomLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(nomLabel, 2, row);
        gridPane.add(nomField, 3, row++);
        
        // Deuxième ligne: Prénom, Date de naissance
        Label prenomLabel = new Label("Prénom:");
        prenomLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(prenomLabel, 0, row);
        gridPane.add(prenomField, 1, row);
        Label dateNaissanceLabel = new Label("Date de naissance:");
        dateNaissanceLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(dateNaissanceLabel, 2, row);
        gridPane.add(dateNaissancePicker, 3, row++);
        
        // Troisième ligne: Téléphone, Email
        Label telephoneLabel = new Label("Téléphone:");
        telephoneLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(telephoneLabel, 0, row);
        gridPane.add(telephoneField, 1, row);
        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(emailLabel, 2, row);
        gridPane.add(emailField, 3, row++);
        
        // Quatrième ligne: Adresse (sur 4 colonnes)
        Label adresseLabel = new Label("Adresse:");
        adresseLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(adresseLabel, 0, row);
        GridPane.setColumnSpan(adresseArea, 3);
        gridPane.add(adresseArea, 1, row++);
        
        // Cinquième ligne: Poids, Taille
        Label poidsLabel = new Label("Poids (kg):");
        poidsLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(poidsLabel, 0, row);
        gridPane.add(poidsField, 1, row);
        Label tailleLabel = new Label("Taille (cm):");
        tailleLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(tailleLabel, 2, row);
        gridPane.add(tailleField, 3, row++);
        
        // Sixième ligne: Pack, Date début
        Label packLabel = new Label("Pack:");
        packLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(packLabel, 0, row);
        gridPane.add(packCombo, 1, row);
        Label dateDebutLabel = new Label("Date début:");
        dateDebutLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(dateDebutLabel, 2, row);
        gridPane.add(dateDebutPicker, 3, row++);
        
        // Septième ligne: Date fin
        Label dateFinLabel = new Label("Date fin:");
        dateFinLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(dateFinLabel, 0, row);
        gridPane.add(dateFinPicker, 1, row++);
        
        // Huitième ligne: Objectifs (sur 4 colonnes)
        Label objectifsLabel = new Label("Objectifs:");
        objectifsLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(objectifsLabel, 0, row);
        GridPane.setColumnSpan(objectifsArea, 3);
        gridPane.add(objectifsArea, 1, row++);
        
        // Neuvième ligne: Problèmes de santé (sur 4 colonnes)
        Label problemesLabel = new Label("Problèmes de santé:");
        problemesLabel.getStyleClass().add("adherents-dialog-label");
        gridPane.add(problemesLabel, 0, row);
        GridPane.setColumnSpan(problemesArea, 3);
        gridPane.add(problemesArea, 1, row);

        // Créer un ScrollPane pour que les boutons soient toujours visibles
        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.getStyleClass().add("adherents-dialog-scroll");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getStyleClass().add("adherents-dialog-pane");
        
        // Appliquer les styles CSS au header via Platform.runLater
        Platform.runLater(() -> {
            try {
                Node header = dialog.getDialogPane().lookup(".header-panel");
                if (header != null) {
                    header.getStyleClass().add("adherents-dialog-header");
                }
                Label headerText = (Label) dialog.getDialogPane().lookup(".header-panel .label");
                if (headerText != null) {
                    headerText.getStyleClass().add("adherents-dialog-header-text");
                }
            } catch (Exception e) {
                // Ignorer si le header n'est pas trouvé
            }
        });

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Appliquer les classes CSS aux boutons du dialog
        Platform.runLater(() -> {
            try {
                Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
                if (saveButton != null) {
                    saveButton.getStyleClass().add("adherents-dialog-btn-save");
                }
                
                Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
                if (cancelButton != null) {
                    cancelButton.getStyleClass().add("adherents-dialog-btn-cancel");
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
