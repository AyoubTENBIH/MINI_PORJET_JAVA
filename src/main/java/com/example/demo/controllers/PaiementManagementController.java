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
    private TableView<Paiement> paiementsTable;
    @FXML
    private TextField searchPaymentsField;
    @FXML
    private Button addPaymentButton;

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
        
        // Configurer la vue des paiements
        setupPaymentsView();
        
        // Charger les données
        Platform.runLater(() -> {
            loadPayments();
        });
    }
    
    /**
     * Configure la vue des paiements
     */
    private void setupPaymentsView() {
        // Configurer la table des paiements
        if (paiementsTable != null) {
            setupPaymentsTable();
        }
        
        // Configurer le champ de recherche
        if (searchPaymentsField != null) {
            searchPaymentsField.setStyle(
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
            searchPaymentsField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) {
                    searchPaymentsField.setStyle(
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
                    searchPaymentsField.setStyle(
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
                }
            });
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
            addPaymentButton.setOnAction(e -> showNewPaymentDialog(null));
        }
    }

    /**
     * Vue de secours si le FXML ne charge pas
     */
    private Parent createBasicView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0d0f1a;");

        Label title = new Label("Gestion des Paiements & Cotisations");
        title.setStyle("-fx-text-fill: #E6EAF0; -fx-font-size: 24px; -fx-font-weight: 700;");

        // Top Bar: Search and Actions
        HBox topBar = new HBox(10);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher par adhérent, montant...");
        searchField.setPrefWidth(400);
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
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                loadPayments();
            } else {
                searchPayments(newVal);
            }
        });
        
        Button addButton = new Button("+ Nouveau Paiement");
        addButton.setStyle(
            "-fx-background-color: #00E676; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: 600; " +
            "-fx-background-radius: 10px; " +
            "-fx-padding: 10px 20px; " +
            "-fx-cursor: hand;"
        );
        addButton.setOnAction(e -> showNewPaymentDialog(null));
        
        topBar.getChildren().addAll(searchField, addButton);
        
        // Conteneur de contenu - afficher uniquement la vue des paiements
        Parent paymentsView = createPaymentsView();
        
        root.getChildren().addAll(title, topBar, paymentsView);
        
        loadPayments();
        
        return root;
    }
    



    /**
     * Crée la vue de tous les paiements (utilisée dans createBasicView)
     */
    private Parent createPaymentsView() {
        // Table des paiements
        paiementsTable = new TableView<>();
        paiementsTable.setPrefHeight(500);
        paiementsTable.setMinHeight(400);
        paiementsTable.setMaxHeight(Double.MAX_VALUE);
        paiementsTable.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-table-cell-border-color: transparent;"
        );
        
        // Appeler setupPaymentsTable pour configurer la table avec tous les styles
        setupPaymentsTable();
        
        VBox.setVgrow(paiementsTable, Priority.ALWAYS);
        
        return paiementsTable;
    }


    /**
     * Configure la table de la liste rouge (méthode non utilisée - conservée pour compatibilité)
     */
    private void setupRedListTable() {
        // Cette méthode n'est plus utilisée dans la vue simplifiée
        // Tout le code a été supprimé car redListTable n'existe plus comme variable d'instance
    }

    /**
     * Configure la table des paiements
     */
    private void setupPaymentsTable() {
        if (paiementsTable == null) return;
        
        paiementsTable.getColumns().clear();
        // Ne pas définir de hauteur fixe - laisser le layout gérer
        paiementsTable.setMinHeight(400);
        paiementsTable.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(paiementsTable, Priority.ALWAYS);
        // Appliquer le style sombre à la table (identique à la page adhérents)
        paiementsTable.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-table-cell-border-color: transparent;"
        );
        
        // Créer un style pour les headers de colonnes (identique à la page adhérents)
        String headerStylePayments = 
            "-fx-background-color: rgba(42, 52, 65, 0.5); " +
            "-fx-text-fill: #E6EAF0; " +
            "-fx-font-weight: 600; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 16px 20px; " +
            "-fx-border-width: 0 0 1px 0; " +
            "-fx-border-color: #2A3441;";
        
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
        
        // Appliquer les styles aux rows de la table (identique à la page adhérents)
        paiementsTable.setRowFactory(tv -> {
            TableRow<Paiement> row = new TableRow<>();
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
        
        // Appliquer les styles aux headers après chaque mise à jour (identique à la page adhérents)
        paiementsTable.itemsProperty().addListener((obs, oldItems, newItems) -> {
            Platform.runLater(() -> {
                try {
                    // Style des headers
                    javafx.scene.Node header = paiementsTable.lookup(".column-header-background");
                    if (header != null) {
                        header.setStyle("-fx-background-color: rgba(42, 52, 65, 0.5);");
                    }
                    
                    // Style des column headers
                    java.util.Set<javafx.scene.Node> columnHeaders = paiementsTable.lookupAll(".column-header");
                    for (javafx.scene.Node headerNode : columnHeaders) {
                        headerNode.setStyle(
                            "-fx-background-color: rgba(42, 52, 65, 0.5); " +
                            "-fx-text-fill: #E6EAF0; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-padding: 16px 20px; " +
                            "-fx-border-width: 0 0 1px 0; " +
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
