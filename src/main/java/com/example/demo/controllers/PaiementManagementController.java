package com.example.demo.controllers;

import com.example.demo.dao.AdherentDAO;
import com.example.demo.dao.PackDAO;
import com.example.demo.dao.PaiementDAO;
import com.example.demo.models.Adherent;
import com.example.demo.models.Paiement;
import com.example.demo.models.Pack;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    private ObservableList<Paiement> paiementsList = FXCollections.observableArrayList();
    private ObservableList<Adherent> redList = FXCollections.observableArrayList();

    @FXML
    private TabPane tabPane;
    @FXML
    private TableView<Paiement> paiementsTable;
    @FXML
    private TableView<Adherent> redListTable;

    /**
     * Charge la vue de gestion des paiements
     */
    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Titre
        Label title = new Label("Gestion des Paiements & Cotisations");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Statistiques KPIs
        HBox statsBox = createStatsBox();
        
        // Onglets
        TabPane tabs = new TabPane();
        
        // Onglet 1: Liste Rouge (Abonnements expirés)
        Tab redListTab = new Tab("🔴 Liste Rouge", createRedListView());
        redListTab.setClosable(false);
        
        // Onglet 2: Tous les Paiements
        Tab paymentsTab = new Tab("💳 Tous les Paiements", createPaymentsView());
        paymentsTab.setClosable(false);
        
        // Onglet 3: Nouveau Paiement
        Tab newPaymentTab = new Tab("➕ Nouveau Paiement", createNewPaymentView());
        newPaymentTab.setClosable(false);
        
        tabs.getTabs().addAll(redListTab, paymentsTab, newPaymentTab);
        
        root.getChildren().addAll(title, statsBox, tabs);
        
        // Charger les données
        loadRedList();
        loadPayments();
        
        return root;
    }

    /**
     * Crée la boîte de statistiques
     */
    private HBox createStatsBox() {
        HBox statsBox = new HBox(15);
        
        VBox revenusWidget = createStatWidget("Revenus du Mois", "0 DH", "#27ae60");
        VBox impayesWidget = createStatWidget("Impayés", "0", "#e74c3c");
        VBox expireBientotWidget = createStatWidget("Expire Bientôt", "0", "#f39c12");
        
        statsBox.getChildren().addAll(revenusWidget, impayesWidget, expireBientotWidget);
        
        // Mettre à jour les stats
        updateStats(revenusWidget, impayesWidget, expireBientotWidget);
        
        return statsBox;
    }

    private VBox createStatWidget(String title, String value, String color) {
        VBox widget = new VBox(5);
        widget.setPadding(new javafx.geometry.Insets(15));
        widget.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        widget.setPrefWidth(200);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
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
        root.setPadding(new javafx.geometry.Insets(20));
        
        Label subtitle = new Label("Adhérents avec abonnement expiré ou impayé");
        subtitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        // Table de la liste rouge
        redListTable = new TableView<>();
        redListTable.setPrefHeight(500);
        
        TableColumn<Adherent, String> cinColumn = new TableColumn<>("CIN");
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        cinColumn.setPrefWidth(120);
        
        TableColumn<Adherent, String> nomColumn = new TableColumn<>("Nom Complet");
        nomColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomComplet()));
        nomColumn.setPrefWidth(200);
        
        TableColumn<Adherent, String> telephoneColumn = new TableColumn<>("Téléphone");
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        telephoneColumn.setPrefWidth(120);
        
        TableColumn<Adherent, String> dateFinColumn = new TableColumn<>("Date Expiration");
        dateFinColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateFin() != null ? 
                    cellData.getValue().getDateFin().toString() : "N/A"));
        dateFinColumn.setPrefWidth(150);
        
        TableColumn<Adherent, Integer> joursRetardColumn = new TableColumn<>("Jours de Retard");
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
                    if (jours > 30) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (jours > 7) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f39c12;");
                    }
                }
            }
        });
        
        redListTable.getColumns().addAll(cinColumn, nomColumn, telephoneColumn, dateFinColumn, joursRetardColumn);
        redListTable.setItems(redList);
        
        // Bouton action rapide
        Button payerButton = new Button("Enregistrer Paiement");
        payerButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        payerButton.setOnAction(e -> {
            Adherent selected = redListTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showNewPaymentDialog(selected);
            } else {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un adhérent");
            }
        });
        
        Button refreshButton = new Button("Actualiser");
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
        VBox root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        
        HBox topBar = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher...");
        searchField.setPrefWidth(300);
        
        Button refreshButton = new Button("Actualiser");
        refreshButton.setOnAction(e -> loadPayments());
        
        topBar.getChildren().addAll(searchField, refreshButton);
        
        // Table des paiements
        paiementsTable = new TableView<>();
        paiementsTable.setPrefHeight(500);
        
        TableColumn<Paiement, String> adherentColumn = new TableColumn<>("Adhérent");
        adherentColumn.setCellValueFactory(cellData -> {
            try {
                Adherent adherent = adherentDAO.findById(cellData.getValue().getAdherentId());
                return new javafx.beans.property.SimpleStringProperty(
                    adherent != null ? adherent.getNomComplet() : "N/A");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
        adherentColumn.setPrefWidth(200);
        
        TableColumn<Paiement, Double> montantColumn = new TableColumn<>("Montant");
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        montantColumn.setPrefWidth(120);
        montantColumn.setCellFactory(column -> new TableCell<Paiement, Double>() {
            @Override
            protected void updateItem(Double montant, boolean empty) {
                super.updateItem(montant, empty);
                setText(empty || montant == null ? "" : String.format("%.2f DH", montant));
            }
        });
        
        TableColumn<Paiement, String> dateColumn = new TableColumn<>("Date Paiement");
        dateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDatePaiement() != null ?
                    cellData.getValue().getDatePaiement().toLocalDate().toString() : "N/A"));
        dateColumn.setPrefWidth(150);
        
        TableColumn<Paiement, String> methodeColumn = new TableColumn<>("Méthode");
        methodeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getMethodePaiement() != null ?
                    cellData.getValue().getMethodePaiement().getLibelle() : "N/A"));
        methodeColumn.setPrefWidth(120);
        
        TableColumn<Paiement, String> dateFinColumn = new TableColumn<>("Date Fin Abonnement");
        dateFinColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateFinAbonnement() != null ?
                    cellData.getValue().getDateFinAbonnement().toString() : "N/A"));
        dateFinColumn.setPrefWidth(150);
        
        paiementsTable.getColumns().addAll(adherentColumn, montantColumn, dateColumn, methodeColumn, dateFinColumn);
        paiementsTable.setItems(paiementsList);
        
        root.getChildren().addAll(topBar, paiementsTable);
        
        return root;
    }

    /**
     * Crée la vue de nouveau paiement
     */
    private Parent createNewPaymentView() {
        VBox root = new VBox(20);
        root.setPadding(new javafx.geometry.Insets(30));
        root.setPrefWidth(600);
        
        Label subtitle = new Label("Enregistrer un nouveau paiement");
        subtitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Button newPaymentButton = new Button("Nouveau Paiement");
        newPaymentButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 15 30;");
        newPaymentButton.setOnAction(e -> showNewPaymentDialog(null));
        
        root.getChildren().addAll(subtitle, newPaymentButton);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        
        return root;
    }

    /**
     * Charge la liste rouge
     */
    private void loadRedList() {
        try {
            List<Adherent> expired = adherentDAO.findExpired();
            redList.clear();
            redList.addAll(expired);
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
            paiementsList.clear();
            paiementsList.addAll(paiements);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des paiements");
        }
    }

    /**
     * Affiche le dialogue pour enregistrer un nouveau paiement
     */
    private void showNewPaymentDialog(Adherent preselectedAdherent) {
        Dialog<Paiement> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Paiement");
        dialog.setHeaderText("Enregistrer un nouveau paiement");

        // Formulaire
        ComboBox<Adherent> adherentCombo = new ComboBox<>();
        adherentCombo.setPrefWidth(400);
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
        packCombo.setPrefWidth(400);
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

        DatePicker datePaiementPicker = new DatePicker();
        datePaiementPicker.setValue(LocalDate.now());

        ComboBox<Paiement.MethodePaiement> methodeCombo = new ComboBox<>();
        methodeCombo.getItems().addAll(Paiement.MethodePaiement.values());
        methodeCombo.setValue(Paiement.MethodePaiement.ESPECES);

        DatePicker dateDebutPicker = new DatePicker();
        dateDebutPicker.setValue(LocalDate.now());
        dateDebutPicker.setPromptText("Date début abonnement");

        DatePicker dateFinPicker = new DatePicker();
        dateFinPicker.setPromptText("Date fin abonnement");

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

        VBox formLayout = new VBox(10);
        formLayout.setPadding(new javafx.geometry.Insets(20));
        formLayout.getChildren().addAll(
            new Label("Adhérent:"), adherentCombo,
            new Label("Pack:"), packCombo,
            new Label("Montant (DH):"), montantField,
            new Label("Date de paiement:"), datePaiementPicker,
            new Label("Méthode de paiement:"), methodeCombo,
            new Label("Date début abonnement:"), dateDebutPicker,
            new Label("Date fin abonnement:"), dateFinPicker,
            new Label("Notes:"), notesArea
        );

        dialog.getDialogPane().setContent(formLayout);
        dialog.getDialogPane().setPrefWidth(500);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

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
                // Créer le paiement
                paiementDAO.create(paiement);
                
                // Mettre à jour les dates d'abonnement de l'adhérent
                Adherent adherent = adherentDAO.findById(paiement.getAdherentId());
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
        alert.showAndWait();
    }
}
