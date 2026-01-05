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
    @FXML private TableView<Paiement> paiementsTable;
    @FXML private TextField searchPaymentsField;
    @FXML private Button addPaymentButton;
    @FXML private TableColumn<Paiement, String> adherentColumn;
    @FXML private TableColumn<Paiement, Double> montantColumn;
    @FXML private TableColumn<Paiement, String> dateColumn;
    @FXML private TableColumn<Paiement, String> methodeColumn;
    @FXML private TableColumn<Paiement, String> dateFinColumn;

    /**
     * Charge la vue de gestion des paiements depuis FXML
     */
    public Parent getView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/paiement.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            
            // Charger le CSS du module paiement
            if (root.getScene() != null) {
                root.getScene().getStylesheets().add(
                    getClass().getResource("/css/paiements.css").toExternalForm()
                );
            } else {
                // Si la scène n'existe pas encore, l'ajouter lors de l'ajout à la scène
                root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null && !newScene.getStylesheets().contains(
                        getClass().getResource("/css/paiements.css").toExternalForm())) {
                        newScene.getStylesheets().add(
                            getClass().getResource("/css/paiements.css").toExternalForm()
                        );
                    }
                });
            }
            
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
        
        // Configurer la vue des paiements
        setupPaymentsView();
        
        // Charger les données
        Platform.runLater(() -> {
            loadPayments();
        });
    }
    
    /**
     * Configure la vue des paiements (logique métier uniquement)
     */
    private void setupPaymentsView() {
        // Configurer la table des paiements
        if (paiementsTable != null) {
            setupPaymentsTable();
        }
        
        // Configurer le champ de recherche (logique métier)
        if (searchPaymentsField != null) {
            searchPaymentsField.getStyleClass().add("paiements-search-field");
            searchPaymentsField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.trim().isEmpty()) {
                    loadPayments();
                } else {
                    searchPayments(newVal);
                }
            });
        }
        
        // Configurer le bouton Ajouter Paiement
        if (addPaymentButton != null) {
            addPaymentButton.getStyleClass().add("paiements-btn-success");
            addPaymentButton.setOnAction(e -> showNewPaymentDialog(null));
        }
        
        // Configurer le header (icônes SVG)
        setupHeader();
    }
    
    /**
     * Configure les icônes SVG du header
     */
    private void setupHeader() {
        if (menuBtn != null) {
            Node icon = loadSVGIcon("icon-menu", 20, "#9AA4B2");
            if (icon != null) menuBtn.setGraphic(icon);
        }
        if (moonBtn != null) {
            Node icon = loadSVGIcon("icon-moon", 20, "#9AA4B2");
            if (icon != null) moonBtn.setGraphic(icon);
            moonBtn.setOnAction(e -> handleMoonClick());
        }
        if (refreshBtn != null) {
            Node icon = loadSVGIcon("icon-refresh", 20, "#9AA4B2");
            if (icon != null) refreshBtn.setGraphic(icon);
            refreshBtn.setOnAction(e -> loadPayments());
        }
        if (bellBtn != null) {
            Node icon = loadSVGIcon("icon-bell", 20, "#9AA4B2");
            if (icon != null) bellBtn.setGraphic(icon);
        }
        if (globeBtn != null) {
            Node icon = loadSVGIcon("icon-globe", 20, "#9AA4B2");
            if (icon != null) globeBtn.setGraphic(icon);
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
     * Charge une icône SVG
     */
    private Node loadSVGIcon(String iconName, double size, String color) {
        try {
            String svgPath = getSvgPathForIcon(iconName);
            
            if (svgPath != null && !svgPath.isEmpty()) {
                javafx.scene.shape.SVGPath svgPathNode = new javafx.scene.shape.SVGPath();
                svgPathNode.setContent(svgPath);
                svgPathNode.setFill(null);
                svgPathNode.setStroke(javafx.scene.paint.Color.web(color));
                svgPathNode.setStrokeWidth(2.0);
                svgPathNode.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                svgPathNode.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                
                double scale = size / 24.0;
                svgPathNode.setScaleX(scale);
                svgPathNode.setScaleY(scale);
                
                StackPane container = new StackPane();
                container.getStyleClass().add("icon-container");
                container.setPrefSize(size, size);
                container.setMaxSize(size, size);
                container.setMinSize(size, size);
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
            case "icon-menu" -> com.example.demo.utils.SvgIcons.MENU;
            case "icon-moon" -> com.example.demo.utils.SvgIcons.MOON;
            case "icon-refresh" -> com.example.demo.utils.SvgIcons.REFRESH;
            case "icon-bell" -> com.example.demo.utils.SvgIcons.BELL;
            case "icon-globe" -> com.example.demo.utils.SvgIcons.GLOBE;
            default -> null;
        };
    }

    /**
     * Vue de secours si le FXML ne charge pas
     */
    private Parent createBasicView() {
        initializeServices();
        VBox errorView = new VBox(10);
        errorView.getStyleClass().add("paiements-error-view");
        Label errorLabel = new Label("Erreur lors du chargement de l'interface. Veuillez vérifier le fichier FXML.");
        errorLabel.getStyleClass().add("paiements-error-label");
        errorView.getChildren().add(errorLabel);
        return errorView;
    }
    





    /**
     * Configure la table de la liste rouge (méthode non utilisée - conservée pour compatibilité)
     */
    private void setupRedListTable() {
        // Cette méthode n'est plus utilisée dans la vue simplifiée
        // Tout le code a été supprimé car redListTable n'existe plus comme variable d'instance
    }

    /**
     * Configure la table des paiements (logique métier uniquement)
     * Les colonnes sont déjà définies dans le FXML
     */
    private void setupPaymentsTable() {
        if (paiementsTable == null) return;
        
        // Configurer les cellValueFactory et cellFactory (logique métier)
        if (adherentColumn != null) {
            adherentColumn.setCellValueFactory(cellData -> {
                try {
                    Adherent adherent = adherentDAO.findById(cellData.getValue().getAdherentId());
                    return new javafx.beans.property.SimpleStringProperty(
                        adherent != null ? adherent.getNomComplet() : "N/A");
                } catch (SQLException e) {
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                }
            });
            adherentColumn.setCellFactory(column -> new TableCell<Paiement, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                    getStyleClass().setAll("paiements-table-cell", "paiements-table-cell-adherent");
                }
            });
        }
        
        if (montantColumn != null) {
            montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
            montantColumn.setCellFactory(column -> new TableCell<Paiement, Double>() {
                @Override
                protected void updateItem(Double montant, boolean empty) {
                    super.updateItem(montant, empty);
                    getStyleClass().clear();
                    if (empty || montant == null) {
                        setText("");
                    } else {
                        setText(String.format("%.2f DH", montant));
                        getStyleClass().setAll("paiements-table-cell", "paiements-table-cell-montant");
                    }
                }
            });
        }
        
        if (dateColumn != null) {
            dateColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getDatePaiement() != null ?
                        cellData.getValue().getDatePaiement().toLocalDate().toString() : "N/A"));
            dateColumn.setCellFactory(column -> new TableCell<Paiement, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                    getStyleClass().setAll("paiements-table-cell", "paiements-table-cell-date");
                }
            });
        }
        
        if (methodeColumn != null) {
            methodeColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getMethodePaiement() != null ?
                        cellData.getValue().getMethodePaiement().getLibelle() : "N/A"));
            methodeColumn.setCellFactory(column -> new TableCell<Paiement, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                    getStyleClass().setAll("paiements-table-cell", "paiements-table-cell-methode");
                }
            });
        }
        
        if (dateFinColumn != null) {
            dateFinColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getDateFinAbonnement() != null ?
                        cellData.getValue().getDateFinAbonnement().toString() : "N/A"));
            dateFinColumn.setCellFactory(column -> new TableCell<Paiement, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                    getStyleClass().setAll("paiements-table-cell", "paiements-table-cell-date");
                }
            });
        }
        
        // Configurer les données et comportements
        paiementsTable.setItems(paiementsList);
        
        // Configurer la politique de redimensionnement des colonnes
        paiementsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Ajuster dynamiquement la colonne "Adhérent" pour qu'elle prenne l'espace restant
        Platform.runLater(() -> {
            paiementsTable.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0 && adherentColumn != null && montantColumn != null && 
                    dateColumn != null && methodeColumn != null && dateFinColumn != null) {
                    double usedWidth = montantColumn.getWidth() + dateColumn.getWidth() + 
                                      methodeColumn.getWidth() + dateFinColumn.getWidth();
                    double remainingWidth = newVal.doubleValue() - usedWidth;
                    if (remainingWidth > 150) {
                        adherentColumn.setPrefWidth(remainingWidth);
                    }
                }
            });
            
            // Calculer initialement
            Platform.runLater(() -> {
                if (paiementsTable.getWidth() > 0 && adherentColumn != null && montantColumn != null && 
                    dateColumn != null && methodeColumn != null && dateFinColumn != null) {
                    double usedWidth = montantColumn.getWidth() + dateColumn.getWidth() + 
                                      methodeColumn.getWidth() + dateFinColumn.getWidth();
                    double remainingWidth = paiementsTable.getWidth() - usedWidth;
                    if (remainingWidth > 150) {
                        adherentColumn.setPrefWidth(remainingWidth);
                    }
                }
            });
        });
    }

    /**
     * Charge la liste rouge (méthode conservée pour compatibilité avec les méthodes de fallback)
     */
    private void loadRedList() {
        try {
            List<Adherent> expired = adherentDAO.findExpired();
            Platform.runLater(() -> {
                redList.clear();
                redList.addAll(expired);
                // Note: redListTable n'est plus utilisé dans la vue simplifiée
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
        
        // S'assurer que le CSS est chargé pour le dialog
        if (paiementsTable != null && paiementsTable.getScene() != null) {
            dialog.initOwner(paiementsTable.getScene().getWindow());
        }
        
        // Styliser le dialog pane avec le thème sombre
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("paiements-dialog-pane");
        
        // Appliquer les styles CSS au header via Platform.runLater
        Platform.runLater(() -> {
            try {
                Node header = dialogPane.lookup(".header-panel");
                if (header != null) {
                    header.getStyleClass().add("paiements-dialog-header");
                }
                Node headerText = dialogPane.lookup(".header-panel .label");
                if (headerText != null) {
                    headerText.getStyleClass().add("paiements-dialog-header-text");
                }
            } catch (Exception e) {
                // Ignorer si le header n'est pas trouvé
            }
        });

        // Formulaire
        ComboBox<Adherent> adherentCombo = new ComboBox<>();
        adherentCombo.getStyleClass().add("paiements-dialog-combobox");
        try {
            List<Adherent> adherents = adherentDAO.findAll();
            adherentCombo.getItems().addAll(adherents);
            if (preselectedAdherent != null) {
                adherentCombo.setValue(preselectedAdherent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Styliser le ComboBox Adhérent pour que la valeur sélectionnée soit visible
        adherentCombo.setCellFactory(listView -> new ListCell<Adherent>() {
            @Override
            protected void updateItem(Adherent adherent, boolean empty) {
                super.updateItem(adherent, empty);
                getStyleClass().clear();
                if (empty || adherent == null) {
                    setText(null);
                } else {
                    setText(adherent.getNomComplet());
                    getStyleClass().add("paiements-dialog-combobox-cell");
                }
            }
        });
        
        // Styliser le buttonCell (label qui affiche la valeur sélectionnée)
        adherentCombo.setButtonCell(new ListCell<Adherent>() {
            @Override
            protected void updateItem(Adherent adherent, boolean empty) {
                super.updateItem(adherent, empty);
                getStyleClass().clear();
                if (empty || adherent == null) {
                    setText(null);
                } else {
                    setText(adherent.getNomComplet());
                    getStyleClass().add("paiements-dialog-combobox-button-cell");
                }
            }
        });

        ComboBox<Pack> packCombo = new ComboBox<>();
        packCombo.getStyleClass().add("paiements-dialog-combobox");
        
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
                    getStyleClass().add("paiements-dialog-combobox-cell");
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
                    getStyleClass().add("paiements-dialog-combobox-button-cell");
                }
            }
        });
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
        montantField.getStyleClass().add("paiements-dialog-textfield");

        DatePicker datePaiementPicker = new DatePicker();
        datePaiementPicker.setValue(LocalDate.now());
        datePaiementPicker.getStyleClass().add("paiements-dialog-datepicker");

        ComboBox<Paiement.MethodePaiement> methodeCombo = new ComboBox<>();
        methodeCombo.getItems().addAll(Paiement.MethodePaiement.values());
        methodeCombo.setValue(Paiement.MethodePaiement.ESPECES);
        methodeCombo.getStyleClass().add("paiements-dialog-combobox");
        
        // Styliser le ComboBox Méthode pour que la valeur sélectionnée soit visible
        methodeCombo.setCellFactory(listView -> new ListCell<Paiement.MethodePaiement>() {
            @Override
            protected void updateItem(Paiement.MethodePaiement methode, boolean empty) {
                super.updateItem(methode, empty);
                getStyleClass().clear();
                if (empty || methode == null) {
                    setText(null);
                } else {
                    setText(methode.getLibelle());
                    getStyleClass().add("paiements-dialog-combobox-cell");
                }
            }
        });
        
        // Styliser le buttonCell (label qui affiche la valeur sélectionnée)
        methodeCombo.setButtonCell(new ListCell<Paiement.MethodePaiement>() {
            @Override
            protected void updateItem(Paiement.MethodePaiement methode, boolean empty) {
                super.updateItem(methode, empty);
                getStyleClass().clear();
                if (empty || methode == null) {
                    setText(null);
                } else {
                    setText(methode.getLibelle());
                    getStyleClass().add("paiements-dialog-combobox-button-cell");
                }
            }
        });

        DatePicker dateDebutPicker = new DatePicker();
        dateDebutPicker.setValue(LocalDate.now());
        dateDebutPicker.setPromptText("Date début abonnement");
        dateDebutPicker.getStyleClass().add("paiements-dialog-datepicker");

        DatePicker dateFinPicker = new DatePicker();
        dateFinPicker.setPromptText("Date fin abonnement");
        dateFinPicker.getStyleClass().add("paiements-dialog-datepicker");

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
        notesArea.getStyleClass().add("paiements-dialog-textarea");

        // Créer GridPane pour une structure compacte et maîtrisée
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("paiements-dialog-grid");
        
        // Définir les contraintes de colonnes (configuration de layout, pas de style)
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
        adherentLabel.getStyleClass().add("paiements-dialog-label");
        gridPane.add(adherentLabel, 0, row);
        gridPane.add(adherentCombo, 1, row++);
        
        Label packLabel = new Label("Pack:");
        packLabel.getStyleClass().add("paiements-dialog-label");
        gridPane.add(packLabel, 0, row);
        gridPane.add(packCombo, 1, row++);
        
        Label montantLabel = new Label("Montant (DH):");
        montantLabel.getStyleClass().add("paiements-dialog-label");
        gridPane.add(montantLabel, 0, row);
        gridPane.add(montantField, 1, row++);
        
        Label datePaiementLabel = new Label("Date de paiement:");
        datePaiementLabel.getStyleClass().add("paiements-dialog-label");
        gridPane.add(datePaiementLabel, 0, row);
        gridPane.add(datePaiementPicker, 1, row++);
        
        Label methodeLabel = new Label("Méthode de paiement:");
        methodeLabel.getStyleClass().add("paiements-dialog-label");
        gridPane.add(methodeLabel, 0, row);
        gridPane.add(methodeCombo, 1, row++);
        
        Label dateDebutLabel = new Label("Date début abonnement:");
        dateDebutLabel.getStyleClass().add("paiements-dialog-label");
        gridPane.add(dateDebutLabel, 0, row);
        gridPane.add(dateDebutPicker, 1, row++);
        
        Label dateFinLabel = new Label("Date fin abonnement:");
        dateFinLabel.getStyleClass().add("paiements-dialog-label");
        gridPane.add(dateFinLabel, 0, row);
        gridPane.add(dateFinPicker, 1, row++);
        
        Label notesLabel = new Label("Notes:");
        notesLabel.getStyleClass().add("paiements-dialog-label");
        gridPane.add(notesLabel, 0, row);
        GridPane.setColumnSpan(notesArea, 1);
        gridPane.add(notesArea, 1, row);

        dialog.getDialogPane().setContent(gridPane);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Appliquer les classes CSS aux boutons du dialog
        Platform.runLater(() -> {
            Node saveButton = dialogPane.lookupButton(saveButtonType);
            if (saveButton != null) {
                saveButton.getStyleClass().add("paiements-dialog-btn-save");
            }
            Node cancelButton = dialogPane.lookupButton(ButtonType.CANCEL);
            if (cancelButton != null) {
                cancelButton.getStyleClass().add("paiements-dialog-btn-cancel");
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Appliquer les classes CSS à l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("paiements-alert-pane");
        
        // Appliquer les classes CSS aux boutons
        Platform.runLater(() -> {
            dialogPane.getButtonTypes().forEach(buttonType -> {
                Node button = dialogPane.lookupButton(buttonType);
                if (button != null) {
                    button.getStyleClass().add("paiements-alert-button");
                }
            });
        });
        
        alert.showAndWait();
    }
}
