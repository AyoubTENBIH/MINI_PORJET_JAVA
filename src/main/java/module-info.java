module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.logging;
    requires java.sql;

    // Ouvrir les packages pour la réflexion et FXML
    opens com.example.demo to javafx.fxml;
    opens com.example.demo.controllers to javafx.fxml;
    opens com.example.demo.models to javafx.base;
    opens com.example.demo.utils;
    
    // Exporter les packages principaux
    exports com.example.demo;
    exports com.example.demo.controllers;
    exports com.example.demo.models;
    exports com.example.demo.dao;
    exports com.example.demo.utils;
}