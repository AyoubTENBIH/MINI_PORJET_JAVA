package com.example.demo.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Utilitaires pour les animations modernes avec JavaFX natif
 */
public class AnimationUtils {
    
    /**
     * Animation de fade in
     */
    public static void fadeIn(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(500), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
    
    /**
     * Animation de slide in depuis la gauche
     */
    public static void slideInLeft(Node node) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(400), node);
        slide.setFromX(-300);
        slide.setToX(0);
        FadeTransition fade = new FadeTransition(Duration.millis(400), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        ParallelTransition parallel = new ParallelTransition(slide, fade);
        parallel.play();
    }
    
    /**
     * Animation de slide in depuis la droite
     */
    public static void slideInRight(Node node) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(400), node);
        slide.setFromX(300);
        slide.setToX(0);
        FadeTransition fade = new FadeTransition(Duration.millis(400), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        ParallelTransition parallel = new ParallelTransition(slide, fade);
        parallel.play();
    }
    
    /**
     * Animation de slide in depuis le haut
     */
    public static void slideInUp(Node node) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(400), node);
        slide.setFromY(50);
        slide.setToY(0);
        FadeTransition fade = new FadeTransition(Duration.millis(400), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        ParallelTransition parallel = new ParallelTransition(slide, fade);
        parallel.play();
    }
    
    /**
     * Animation de zoom in
     */
    public static void zoomIn(Node node) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), node);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);
        FadeTransition fade = new FadeTransition(Duration.millis(400), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.play();
    }
    
    /**
     * Animation de bounce
     */
    public static void bounce(Node node) {
        ScaleTransition bounce = new ScaleTransition(Duration.millis(600), node);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.2);
        bounce.setToY(1.2);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);
        bounce.setInterpolator(javafx.animation.Interpolator.SPLINE(0.68, -0.55, 0.265, 1.55));
        bounce.play();
    }
    
    /**
     * Animation de pulse
     */
    public static void pulse(Node node) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), node);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.1);
        pulse.setToY(1.1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
    }
    
    /**
     * Animation de shake
     */
    public static void shake(Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
    
    /**
     * Animation de fade in avec délai
     */
    public static void fadeInWithDelay(Node node, int delayMs) {
        PauseTransition delay = new PauseTransition(Duration.millis(delayMs));
        delay.setOnFinished(e -> fadeIn(node));
        delay.play();
    }
    
    /**
     * Animation séquentielle pour une liste de nodes
     */
    public static void fadeInSequence(javafx.collections.ObservableList<Node> nodes, int delayBetween) {
        for (int i = 0; i < nodes.size(); i++) {
            final int index = i;
            PauseTransition delay = new PauseTransition(Duration.millis(i * delayBetween));
            delay.setOnFinished(e -> {
                if (index < nodes.size()) {
                    fadeIn(nodes.get(index));
                }
            });
            delay.play();
        }
    }
    
    /**
     * Animation de hover pour les boutons
     */
    public static void addHoverEffect(Node node) {
        node.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
        
        node.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }
}
