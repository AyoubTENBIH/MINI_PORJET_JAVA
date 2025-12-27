package com.example.demo;

import com.example.demo.controllers.LoginController;
import com.example.demo.utils.DatabaseManager;
import com.example.demo.utils.InsertAdherents;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Application principale de gestion de salle de sport
 * Point d'entrée pour IntelliJ IDEA
 * @author Demo Team
 */
public class HelloApplication extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialisation de la base de données
            System.out.println("=== Initialisation de la base de données ===");
            DatabaseManager.getInstance().initializeDatabase();
            System.out.println("=== Base de données initialisée ===");
            
            // Insertion automatique de 50 adhérents pour les statistiques (si aucun n'existe)
            System.out.println("=== Démarrage de l'insertion automatique ===");
            InsertAdherents.insert50();
            System.out.println("=== Fin de l'insertion automatique ===");
            
            // Chargement de la vue de connexion
            LoginController loginController = new LoginController();
            Scene scene = new Scene(loginController.getView(), 400, 500);
            scene.getStylesheets().addAll(
                getClass().getResource("/css/login-glass.css").toExternalForm()
            );
            
            primaryStage.setTitle("Gym Management System - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
            // Fermeture propre de la base de données à la fermeture de l'application
            primaryStage.setOnCloseRequest(e -> {
                DatabaseManager.getInstance().closeConnection();
                System.exit(0);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du démarrage de l'application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
