# 🏋️ Gym Management System

Application complète de gestion pour salle de sport développée avec JavaFX, FXML et CSS.

## 📋 Caractéristiques

### Architecture
- **JavaFX 21** pour l'interface graphique
- **FXML** pour la structure des vues
- **CSS** pour le styling moderne
- **Architecture MVC** (Model-View-Controller)
- **Base de données SQLite** pour le stockage des données
- **Pattern DAO** pour l'accès aux données

### Modules implémentés

#### ✅ Module 1 : Gestion
- [x] **Gestion des Packs/Abonnements** (CRUD complet)
  - Création, modification, suppression de packs
  - Recherche et filtrage en temps réel
  - Tableau interactif avec toutes les informations
  - Formulaire de création/modification avec validation
  - Gestion des activités, horaires, durées, etc.
  
- [x] **Gestion des Adhérents** (CRUD complet)
  - Création, modification, suppression d'adhérents
  - Recherche avancée (nom, CIN, téléphone, email)
  - Formulaire complet avec toutes les informations
  - Gestion des abonnements et dates d'expiration
  - Calcul automatique de l'IMC
  - Détection des abonnements expirés/expirant bientôt
  
- [x] **Gestion des Cotisations/Paiements** (Système complet)
  - Enregistrement des paiements
  - Liste rouge pour les abonnements expirés/impayés
  - Calcul automatique des dates d'expiration
  - Statistiques en temps réel (revenus, impayés)
  - Support de multiples méthodes de paiement
  - Mise à jour automatique des dates d'abonnement
  
- [ ] **Calendrier Dynamique** (À venir)

#### ✅ Module 2 : Statistiques & Analytics
- [x] **Dashboard Principal** (Fonctionnel avec données réelles)
  - Widgets KPIs en temps réel
  - Adhérents actifs
  - Revenus du mois
  - Abonnements expirés
  - Abonnements expirant bientôt
  - Packs disponibles
  - Nouveaux inscrits
  
- [ ] **Graphiques et Courbes** (À venir)
- [ ] **Rapports et Exports** (À venir)

## 🚀 Installation et Démarrage

### Prérequis
- Java 21 ou supérieur
- Maven 3.6+

### Installation

1. Cloner ou télécharger le projet

2. Compiler le projet :
```bash
mvn clean compile
```

3. Lancer l'application :
```bash
mvn javafx:run
```

Ou avec le wrapper Maven :
```bash
./mvnw javafx:run
```

### Identifiants par défaut
- **Username**: `admin`
- **Password**: `admin`

## 📁 Structure du Projet

```
src/main/java/com/example/demo/
├── Main.java                 # Point d'entrée de l'application
├── controllers/              # Contrôleurs MVC
│   ├── LoginController.java
│   ├── MainController.java
│   ├── PackManagementController.java
│   ├── AdherentManagementController.java
│   ├── PaiementManagementController.java
│   └── DashboardController.java
├── models/                   # Modèles de données
│   ├── Pack.java
│   ├── Adherent.java
│   ├── Paiement.java
│   └── Utilisateur.java
├── dao/                      # Data Access Objects
│   ├── PackDAO.java
│   ├── AdherentDAO.java
│   ├── PaiementDAO.java
│   └── UtilisateurDAO.java
└── utils/                    # Utilitaires
    └── DatabaseManager.java

src/main/resources/
├── css/                      # Feuilles de style
│   ├── login.css
│   └── main.css
├── fxml/                     # Fichiers FXML (à créer)
├── images/                   # Images et icônes
└── database/                 # Base de données SQLite
    └── gym_management.db
```

## 🗄️ Base de Données

La base de données SQLite est créée automatiquement au premier lancement avec les tables suivantes :

- `utilisateurs` - Utilisateurs du système
- `packs` - Packs/abonnements disponibles
- `adherents` - Adhérents de la salle
- `paiements` - Historique des paiements
- `presences` - Présences/check-ins
- `cours_collectifs` - Cours collectifs
- `reservations_cours` - Réservations de cours
- `equipements` - Équipements de la salle

## 🎨 Design

L'application utilise un design moderne avec :
- Palette de couleurs fitness (bleu énergétique, orange motivation, gris professionnel)
- Interface responsive
- Sidebar de navigation
- Animations fluides

## ✨ Fonctionnalités Principales Implémentées

### Système d'Authentification
- ✅ Connexion avec username/password
- ✅ Rôles utilisateurs (Admin, Manager, Réceptionniste)
- ✅ Utilisateur admin par défaut (admin/admin)

### Gestion des Packs
- ✅ CRUD complet (Créer, Lire, Modifier, Supprimer)
- ✅ Recherche en temps réel
- ✅ Gestion des activités incluses
- ✅ Configuration des horaires et jours de disponibilité
- ✅ Gestion de la durée et nombre de séances

### Gestion des Adhérents
- ✅ CRUD complet avec formulaire détaillé
- ✅ Recherche multi-critères (nom, CIN, téléphone, email)
- ✅ Gestion des informations personnelles et fitness
- ✅ Calcul automatique de l'IMC
- ✅ Détection visuelle des abonnements expirés/expirant bientôt
- ✅ Gestion des dates d'abonnement

### Gestion des Paiements
- ✅ Enregistrement des paiements
- ✅ Liste rouge avec indicateurs visuels
- ✅ Calcul automatique des dates d'expiration
- ✅ Statistiques en temps réel
- ✅ Support de 4 méthodes de paiement (Espèces, Carte, Virement, Chèque)
- ✅ Mise à jour automatique des abonnements

### Dashboard
- ✅ KPIs en temps réel
- ✅ Statistiques des revenus
- ✅ Suivi des abonnements
- ✅ Vue d'ensemble complète

## 📝 Fonctionnalités à Développer

### Priorité 1
- [ ] Calendrier dynamique avec expirations et événements
- [ ] Graphiques et statistiques avancés (LineChart, PieChart, BarChart)
- [ ] Export Excel des données

### Priorité 2
- [ ] Système de check-in avec QR code
- [ ] Génération de cartes de membre PDF avec QR code
- [ ] Rapports PDF personnalisés
- [ ] Profil détaillé d'adhérent avec onglets

### Priorité 3
- [ ] Gestion des cours collectifs
- [ ] Gestion des équipements
- [ ] Système de notifications
- [ ] Historique des présences
- [ ] Système de réservation

## 🛠️ Technologies Utilisées

- **JavaFX 21.0.6** - Interface graphique
- **ControlsFX 11.2.1** - Contrôles avancés
- **SQLite JDBC 3.44.1.0** - Base de données
- **Apache POI 5.2.5** - Export Excel
- **iTextPDF 5.5.13.3** - Génération PDF
- **ZXing 3.5.3** - Génération QR codes

## 👥 Auteur

Développé pour la gestion complète d'une salle de sport.

## 📄 Licence

Projet éducatif.

---

**Note**: Ce projet est en cours de développement actif. Les fonctionnalités sont ajoutées progressivement.

