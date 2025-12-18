package com.example.demo.controllers;

import com.example.demo.dao.PackDAO;
import com.example.demo.models.Pack;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrôleur pour la gestion des packs/abonnements
 */
public class PackManagementController {
    private PackDAO packDAO = new PackDAO();
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
     * Charge la vue de gestion des packs
     */
    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Titre
        Label title = new Label("Gestion des Packs/Abonnements");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Barre de recherche et actions
        javafx.scene.layout.HBox topBar = new javafx.scene.layout.HBox(10);
        searchField = new TextField();
        searchField.setPromptText("Rechercher un pack...");
        searchField.setPrefWidth(400);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> searchPacks(newVal));

        addButton = new Button("+ Nouveau Pack");
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addButton.setOnAction(e -> showPackDialog(null));

        editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        editButton.setOnAction(e -> editSelectedPack());

        deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteSelectedPack());

        topBar.getChildren().addAll(searchField, addButton, editButton, deleteButton);

        // Table des packs
        packsTable = new TableView<>();
        packsTable.setPrefHeight(500);

        nomColumn = new TableColumn<>("Nom");
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomColumn.setPrefWidth(200);

        prixColumn = new TableColumn<>("Prix (DH)");
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        prixColumn.setPrefWidth(120);
        prixColumn.setCellFactory(column -> new TableCell<Pack, Double>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                setText(empty || prix == null ? "" : String.format("%.2f DH", prix));
            }
        });

        activitesColumn = new TableColumn<>("Activités");
        activitesColumn.setCellValueFactory(new PropertyValueFactory<>("activitesAsString"));
        activitesColumn.setPrefWidth(300);

        TableColumn<Pack, String> dureeColumn = new TableColumn<>("Durée");
        dureeColumn.setCellValueFactory(cellData -> {
            Pack pack = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                pack.getDuree() + " " + pack.getUniteDuree().toLowerCase()
            );
        });
        dureeColumn.setPrefWidth(100);

        actifColumn = new TableColumn<>("Statut");
        actifColumn.setCellValueFactory(new PropertyValueFactory<>("actif"));
        actifColumn.setPrefWidth(100);
        actifColumn.setCellFactory(column -> new TableCell<Pack, Boolean>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(actif ? "Actif" : "Inactif");
                    setStyle(actif ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });

        packsTable.getColumns().addAll(nomColumn, prixColumn, activitesColumn, dureeColumn, actifColumn);
        packsTable.setItems(packsList);

        // Double-clic pour modifier
        packsTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                editSelectedPack();
            }
        });

        root.getChildren().addAll(title, topBar, packsTable);

        // Charger les packs
        loadPacks();

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

        VBox vbox = new VBox(10);
        vbox.setPadding(new javafx.geometry.Insets(20));
        vbox.getChildren().addAll(
            new Label("Nom:"), nomField,
            new Label("Prix (DH):"), prixField,
            new Label("Activités:"), activitesField,
            new Label("Durée:"), dureeField,
            new Label("Unité de durée:"), uniteDureeCombo,
            new Label("Séances/semaine:"), seancesField,
            accesCoachCheck
        );

        dialog.getDialogPane().setContent(vbox);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

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
                if (result.getId() == null) {
                    packDAO.create(result);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Pack créé avec succès");
                } else {
                    packDAO.update(result);
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
                    packDAO.delete(selected.getId());
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
}




