package com.example.demo.models;

/**
 * Modèle représentant un utilisateur du système (admin, manager, réceptionniste)
 */
public class Utilisateur {
    public enum Role {
        ADMIN("Administrateur"),
        MANAGER("Manager"),
        RECEPTIONNISTE("Réceptionniste");

        private final String libelle;

        Role(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    private Integer id;
    private String username;
    private String password;
    private Role role;
    private String nom;
    private String prenom;
    private Boolean actif;

    public Utilisateur() {
        this.actif = true;
        this.role = Role.RECEPTIONNISTE;
    }

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}




