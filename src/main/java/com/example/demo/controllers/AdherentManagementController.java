package com.example.demo.controllers;

import com.example.demo.dao.AdherentDAO;
import com.example.demo.dao.PackDAO;
import com.example.demo.models.Adherent;
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
import java.util.List;

/**
 * Contrôleur pour la gestion des adhérents
 */
public class AdherentManagementController {
    private AdherentDAO adherentDAO = new AdherentDAO();
    private PackDAO packDAO = new PackDAO();
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

    /**
     * Charge la vue de gestion des adhérents
     */
    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Titre
        Label title = new Label("Gestion des Adhérents");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Barre de recherche et actions
        HBox topBar = new HBox(10);
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

        addButton = new Button("+ Nouvel Adhérent");
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAdherentDialog(null));

        editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        editButton.setOnAction(e -> editSelectedAdherent());

        deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteSelectedAdherent());

        topBar.getChildren().addAll(searchField, addButton, editButton, deleteButton);

        // Table des adhérents
        adherentsTable = new TableView<>();
        adherentsTable.setPrefHeight(500);

        TableColumn<Adherent, String> cinColumn = new TableColumn<>("CIN");
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        cinColumn.setPrefWidth(120);

        TableColumn<Adherent, String> nomColumn = new TableColumn<>("Nom");
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomColumn.setPrefWidth(150);

        TableColumn<Adherent, String> prenomColumn = new TableColumn<>("Prénom");
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        prenomColumn.setPrefWidth(150);

        TableColumn<Adherent, String> telephoneColumn = new TableColumn<>("Téléphone");
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        telephoneColumn.setPrefWidth(120);

        TableColumn<Adherent, String> packColumn = new TableColumn<>("Pack");
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
        packColumn.setPrefWidth(150);

        TableColumn<Adherent, String> dateFinColumn = new TableColumn<>("Date Fin");
        dateFinColumn.setCellValueFactory(cellData -> {
            Adherent adherent = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                adherent.getDateFin() != null ? adherent.getDateFin().toString() : "N/A"
            );
        });
        dateFinColumn.setPrefWidth(120);
        dateFinColumn.setCellFactory(column -> new TableCell<Adherent, String>() {
            @Override
            protected void updateItem(String dateFin, boolean empty) {
                super.updateItem(dateFin, empty);
                if (empty || dateFin == null || "N/A".equals(dateFin)) {
                    setText("");
                    setStyle("");
                } else {
                    setText(dateFin);
                    Adherent adherent = getTableView().getItems().get(getIndex());
                    if (adherent.isAbonnementExpire()) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (adherent.isAbonnementExpireBientot()) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        TableColumn<Adherent, Boolean> actifColumn = new TableColumn<>("Statut");
        actifColumn.setCellValueFactory(new PropertyValueFactory<>("actif"));
        actifColumn.setPrefWidth(100);
        actifColumn.setCellFactory(column -> new TableCell<Adherent, Boolean>() {
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

        adherentsTable.getColumns().addAll(cinColumn, nomColumn, prenomColumn, telephoneColumn, 
                                          packColumn, dateFinColumn, actifColumn);
        adherentsTable.setItems(adherentsList);

        // Double-clic pour modifier
        adherentsTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                editSelectedAdherent();
            }
        });

        root.getChildren().addAll(title, topBar, adherentsTable);

        // Charger les adhérents
        loadAdherents();

        return root;
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
        packCombo.setPrefWidth(200);
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

        // Layout du formulaire
        VBox formLayout = new VBox(10);
        formLayout.setPadding(new javafx.geometry.Insets(20));
        
        HBox row1 = new HBox(10);
        row1.getChildren().addAll(
            new VBox(5, new Label("CIN:"), cinField),
            new VBox(5, new Label("Nom:"), nomField),
            new VBox(5, new Label("Prénom:"), prenomField)
        );
        
        HBox row2 = new HBox(10);
        row2.getChildren().addAll(
            new VBox(5, new Label("Date de naissance:"), dateNaissancePicker),
            new VBox(5, new Label("Téléphone:"), telephoneField),
            new VBox(5, new Label("Email:"), emailField)
        );
        
        formLayout.getChildren().addAll(
            row1,
            row2,
            new VBox(5, new Label("Adresse:"), adresseArea),
            new HBox(10,
                new VBox(5, new Label("Poids (kg):"), poidsField),
                new VBox(5, new Label("Taille (cm):"), tailleField),
                new VBox(5, new Label("Pack:"), packCombo)
            ),
            new HBox(10,
                new VBox(5, new Label("Date début:"), dateDebutPicker),
                new VBox(5, new Label("Date fin:"), dateFinPicker)
            ),
            new VBox(5, new Label("Objectifs:"), objectifsArea),
            new VBox(5, new Label("Problèmes de santé:"), problemesArea)
        );

        dialog.getDialogPane().setContent(formLayout);
        dialog.getDialogPane().setPrefWidth(700);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

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
                if (result.getId() == null) {
                    adherentDAO.create(result);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Adhérent créé avec succès");
                } else {
                    adherentDAO.update(result);
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
                    adherentDAO.delete(selected.getId());
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
