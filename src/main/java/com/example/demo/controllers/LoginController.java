package com.example.demo.controllers;

import com.example.demo.dao.UtilisateurDAO;
import com.example.demo.models.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Contrôleur pour la page de connexion
 */
public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private static Utilisateur currentUser;

    /**
     * Initialise le contrôleur
     */
    @FXML
    private void initialize() {
        // Permettre la connexion avec la touche Entrée
        passwordField.setOnKeyPressed(this::handleKeyPress);
        usernameField.setOnKeyPressed(this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    /**
     * Charge la vue de connexion
     */
    public Parent getView() {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/fxml/login.fxml");
            if (fxmlUrl == null) {
                // Le fichier FXML n'existe pas, utiliser la vue basique
                return createBasicLoginView();
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setController(this);
            return loader.load();
        } catch (Exception e) {
            e.printStackTrace();
            // Si le FXML n'existe pas ou erreur de chargement, créer une vue basique
            return createBasicLoginView();
        }
    }

    private Parent createBasicLoginView() {
        // Créer une vue basique si le FXML n'existe pas encore
        usernameField = new TextField();
        usernameField.setPromptText("Nom d'utilisateur");
        usernameField.getStyleClass().add("text-field");
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.getStyleClass().add("text-field");
        
        loginButton = new Button("Se connecter");
        loginButton.getStyleClass().add("login-button");
        
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        usernameField.setPrefWidth(300);
        passwordField.setPrefWidth(300);

        loginButton.setOnAction(e -> handleLogin());

        Label titleLabel = new Label("🏋️ Gym Management System");
        titleLabel.getStyleClass().add("app-title");
        
        Label subtitleLabel = new Label("Système de gestion de salle de sport");
        subtitleLabel.getStyleClass().add("app-subtitle");

        javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(20);
        container.setAlignment(javafx.geometry.Pos.CENTER);
        container.getStyleClass().add("login-container");
        container.setPrefWidth(400);
        container.setPrefHeight(500);
        
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(15);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        vbox.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            new Label("Nom d'utilisateur"),
            usernameField,
            new Label("Mot de passe"),
            passwordField,
            loginButton,
            errorLabel
        );
        
        container.getChildren().add(vbox);
        vbox.setPadding(new javafx.geometry.Insets(30));

        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane();
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.getChildren().add(container);
        root.getStyleClass().add("root");

        return root;
    }

    /**
     * Gère la tentative de connexion
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez saisir un nom d'utilisateur et un mot de passe");
            return;
        }

        try {
            Utilisateur utilisateur = utilisateurDAO.authenticate(username, password);
            
            if (utilisateur != null) {
                currentUser = utilisateur;
                errorLabel.setText("");
                openMainApplication();
            } else {
                showError("Nom d'utilisateur ou mot de passe incorrect");
                passwordField.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de connexion à la base de données");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Ouvre l'application principale après connexion réussie
     */
    private void openMainApplication() {
        try {
            MainController mainController = new MainController();
            Scene mainScene = new Scene(mainController.getView(), 1280, 720);
            mainScene.getStylesheets().addAll(
                getClass().getResource("/css/premium-dark.css").toExternalForm()
            );

            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.setScene(mainScene);
            currentStage.setTitle("Gym Management System - Gestion");
            currentStage.setResizable(true);
            currentStage.centerOnScreen();
            currentStage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de l'application principale");
        }
    }

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }
}

