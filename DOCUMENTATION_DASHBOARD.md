# 📊 Documentation Complète du Dashboard

## 📑 Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture du Dashboard](#architecture-du-dashboard)
3. [Fichiers Principaux](#fichiers-principaux)
4. [Structure FXML](#structure-fxml)
5. [Controller - DashboardController](#controller---dashboardcontroller)
6. [DAO (Data Access Objects)](#dao-data-access-objects)
7. [Services](#services)
8. [Composants UI](#composants-ui)
9. [Styles CSS](#styles-css)
10. [Flux de Données](#flux-de-données)

---

## 🎯 Vue d'ensemble

Le **Dashboard** est la page principale de l'application de gestion de salle de sport. Il affiche des statistiques en temps réel, des graphiques, des notifications et des activités récentes.

**Fichier principal :** [`src/main/java/com/example/demo/controllers/DashboardController.java`](src/main/java/com/example/demo/controllers/DashboardController.java)

**Fichier FXML :** [`src/main/resources/fxml/dashboard.fxml`](src/main/resources/fxml/dashboard.fxml)

---

## 🏗️ Architecture du Dashboard

```
┌─────────────────────────────────────────────────────────────┐
│                    MainController                           │
│  [src/main/java/.../controllers/MainController.java]       │
│  - Gère la navigation entre les pages                      │
│  - Affiche le Dashboard via showDashboard()                │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                 DashboardController                          │
│  [src/main/java/.../controllers/DashboardController.java]    │
│  - Initialise l'interface                                     │
│  - Gère les interactions utilisateur                        │
│  - Charge les données depuis les DAO                         │
└──────────────┬───────────────────────┬───────────────────────┘
               │                       │
               ▼                       ▼
    ┌──────────────────┐    ┌──────────────────┐
    │   DAO Layer      │    │  Services Layer  │
    │  - AdherentDAO   │    │ - Notification   │
    │  - PaiementDAO   │    │   Service        │
    │  - PackDAO       │    │ - Activity       │
    │  - Notification  │    │   Service        │
    │    DAO           │    │ - Theme Service  │
    │  - ActivityDAO   │    └──────────────────┘
    └──────────────────┘
               │
               ▼
    ┌──────────────────┐
    │  DatabaseManager │
    │  (MySQL/SQLite)  │
    └──────────────────┘
```

---

## 📁 Fichiers Principaux

### 1. Controller Principal

**Fichier :** [`src/main/java/com/example/demo/controllers/DashboardController.java`](src/main/java/com/example/demo/controllers/DashboardController.java)

**Lignes :** 1-3675

**Responsabilités :**
- Gestion de l'interface utilisateur
- Chargement et affichage des données
- Gestion des interactions (clics, filtres, refresh)
- Création dynamique des composants UI

---

### 2. Fichier FXML

**Fichier :** [`src/main/resources/fxml/dashboard.fxml`](src/main/resources/fxml/dashboard.fxml)

**Structure :**
- `BorderPane` principal avec 3 zones :
  - `top` : Header avec boutons et breadcrumb
  - `center` : Zone de contenu scrollable
  - `right` : Sidebar droite (Notifications & Activities)

---

### 3. Controller Principal (Navigation)

**Fichier :** [`src/main/java/com/example/demo/controllers/MainController.java`](src/main/java/com/example/demo/controllers/MainController.java)

**Méthode clé :** `showDashboard()` - Ligne 655-663
```java
private void showDashboard() {
    if (dashboardController == null) {
        dashboardController = new DashboardController();
    }
    if (mainContainer != null) {
        mainContainer.setCenter(dashboardController.getView());
        dashboardController.updateBreadcrumb("dashboard");
    }
}
```

---

## 🎨 Structure FXML

### Fichier : [`src/main/resources/fxml/dashboard.fxml`](src/main/resources/fxml/dashboard.fxml)

#### Structure Hiérarchique :

```xml
<BorderPane>
  ├── <top> - Header
  │   └── <HBox fx:id="header">
  │       ├── menuBtn (Menu toggle)
  │       ├── starBtn (Favoris)
  │       ├── breadcrumbLabel
  │       ├── moonBtn (Theme toggle)
  │       ├── refreshBtn
  │       ├── bellBtn (Notifications)
  │       └── globeBtn (Langue)
  │
  ├── <center> - Contenu Principal
  │   └── <VBox>
  │       ├── titleFilterSection (Titre + Filtre)
  │       └── contentScroll (ScrollPane)
  │           └── contentWrapper
  │               ├── kpiGrid (4 KPI Cards)
  │               ├── chartsRow (Graphiques)
  │               ├── areaChartCard (Area Chart)
  │               └── bottomRow (Table + Liste)
  │
  └── <right> - Sidebar Droite
      └── <VBox fx:id="rightSidebar">
          ├── notificationPanel
          └── activityPanel
```

---

## 🎮 Controller - DashboardController

### Classe : `DashboardController`

**Fichier complet :** [`src/main/java/com/example/demo/controllers/DashboardController.java`](src/main/java/com/example/demo/controllers/DashboardController.java)

### 🔧 Méthodes Principales

#### 1. Initialisation

**Méthode :** `initialize()` - **Ligne 138-157**
- **Lien direct :** [`DashboardController.java:138`](src/main/java/com/example/demo/controllers/DashboardController.java#L138)
- **Description :** Méthode appelée automatiquement après le chargement du FXML
- **Actions :**
  - Configure le header
  - Configure la section titre/filtre
  - Configure le contenu principal
  - Configure la sidebar droite
  - Charge l'état de la sidebar gauche

**Méthode :** `getView()` - **Ligne 122-133**
- **Lien direct :** [`DashboardController.java:122`](src/main/java/com/example/demo/controllers/DashboardController.java#L122)
- **Description :** Charge le fichier FXML et retourne la vue principale
- **Retourne :** `Parent` - La vue complète du dashboard

---

#### 2. Configuration des Sections

**Méthode :** `setupHeader()` - **Ligne 162-204**
- **Lien direct :** [`DashboardController.java:162`](src/main/java/com/example/demo/controllers/DashboardController.java#L162)
- **Description :** Configure tous les boutons du header
- **Boutons configurés :**
  - `menuBtn` : Toggle sidebar gauche
  - `starBtn` : Toggle favoris (via `FavorisDAO`)
  - `moonBtn` : Toggle thème dark/light (via `ThemeService`)
  - `refreshBtn` : Rafraîchit le dashboard
  - `bellBtn` : Affiche le popup de notifications
  - `globeBtn` : Change la langue (via `LanguageService`)

**Méthode :** `setupTitleFilterSection()` - **Ligne 209-217**
- **Lien direct :** [`DashboardController.java:209`](src/main/java/com/example/demo/controllers/DashboardController.java#L209)
- **Description :** Configure le bouton de filtre temporel
- **Filtres disponibles :** Today, This Week, This Month, Last Month, This Year

**Méthode :** `setupContent()` - **Ligne 222-249**
- **Lien direct :** [`DashboardController.java:222`](src/main/java/com/example/demo/controllers/DashboardController.java#L222)
- **Description :** Crée et organise tous les composants du contenu principal
- **Composants créés :**
  1. `kpiGrid` : 4 cartes KPI (Revenus, Adhérents, Taux Occupation, Nouveaux Abonnements)
  2. `chartsRow` : Graphique donut + Mini cards
  3. `areaChartCard` : Graphique area chart des revenus
  4. `bottomRow` : Table des adhérents + Liste rouge

**Méthode :** `setupRightSidebar()` - **Ligne 254-263**
- **Lien direct :** [`DashboardController.java:254`](src/main/java/com/example/demo/controllers/DashboardController.java#L254)
- **Description :** Configure la sidebar droite avec notifications et activités

---

#### 3. Création des Composants KPI

**Méthode :** `createKPIGrid()` - **Ligne 897-982**
- **Lien direct :** [`DashboardController.java:897`](src/main/java/com/example/demo/controllers/DashboardController.java#L897)
- **Description :** Crée la grille horizontale de 4 cartes KPI
- **Cartes créées :**
  1. **Revenus du Mois** : Calcul via `paiementDAO.getRevenusMois()`
  2. **Adhérents Actifs** : Compte via `adherentDAO.findAll().size()`
  3. **Taux d'Occupation** : Calcul via `adherentDAO.getTauxOccupation()` avec gauge
  4. **Nouveaux Abonnements** : Compte des adhérents créés dans les 30 derniers jours

**Méthode :** `createKPICardExact()` - **Ligne 987-1060**
- **Lien direct :** [`DashboardController.java:987`](src/main/java/com/example/demo/controllers/DashboardController.java#L987)
- **Description :** Crée une carte KPI standard avec label, valeur et indicateur de changement
- **Paramètres :**
  - `label` : Titre de la carte
  - `value` : Valeur principale
  - `change` : Texte de changement (ex: "+5% vs mois dernier")
  - `positive` : Boolean indiquant si le changement est positif

**Méthode :** `createKPICardWithGaugeExact()` - **Ligne 1065-1133**
- **Lien direct :** [`DashboardController.java:1065`](src/main/java/com/example/demo/controllers/DashboardController.java#L1065)
- **Description :** Crée une carte KPI avec un gauge semi-circulaire
- **Utilisé pour :** Taux d'Occupation avec objectif

---

#### 4. Création des Graphiques

**Méthode :** `createChartsRowWithMiniCards()` - **Ligne 1172-1203**
- **Lien direct :** [`DashboardController.java:1172`](src/main/java/com/example/demo/controllers/DashboardController.java#L1172)
- **Description :** Crée une ligne avec graphique donut (60%) + grille de mini cards (40%)

**Méthode :** `createPackDistributionDonutCard()` - **Ligne 1367-1522**
- **Lien direct :** [`DashboardController.java:1367`](src/main/java/com/example/demo/controllers/DashboardController.java#L1367)
- **Description :** Crée un graphique donut montrant la distribution des packs
- **Données :** Récupérées via `packDAO.getDistributionByAdherents()`

**Méthode :** `createRevenueAreaChartCard()` - **Ligne 3584-3675**
- **Lien direct :** [`DashboardController.java:3584`](src/main/java/com/example/demo/controllers/DashboardController.java#L3584)
- **Description :** Crée un graphique area chart montrant l'évolution des revenus
- **Données :** Récupérées via `paiementDAO.getRevenusParMois(12)`

---

#### 5. Gestion des Notifications

**Méthode :** `updateNotificationBadge()` - **Ligne 288-311**
- **Lien direct :** [`DashboardController.java:288`](src/main/java/com/example/demo/controllers/DashboardController.java#L288)
- **Description :** Met à jour le badge de notifications non lues
- **Utilise :** `NotificationService.getUnreadCount()`

**Méthode :** `showNotificationPopup()` - **Ligne 316-441**
- **Lien direct :** [`DashboardController.java:316`](src/main/java/com/example/demo/controllers/DashboardController.java#L316)
- **Description :** Affiche un popup avec les 5 notifications les plus récentes
- **Utilise :** `NotificationService.getRecentNotifications(5)`

**Méthode :** `createNotificationPanel()` - **Ligne 2636-2709**
- **Lien direct :** [`DashboardController.java:2636`](src/main/java/com/example/demo/controllers/DashboardController.java#L2636)
- **Description :** Crée le panneau de notifications dans la sidebar droite

---

#### 6. Gestion des Activités

**Méthode :** `createActivityPanel()` - **Ligne 2867-2933**
- **Lien direct :** [`DashboardController.java:2867`](src/main/java/com/example/demo/controllers/DashboardController.java#L2867)
- **Description :** Crée le panneau d'activités dans la sidebar droite
- **Données :** Récupérées via `ActivityService.getRecentActivities(10)`

---

#### 7. Rafraîchissement

**Méthode :** `refreshDashboard()` - **Ligne 3071-3127**
- **Lien direct :** [`DashboardController.java:3071`](src/main/java/com/example/demo/controllers/DashboardController.java#L3071)
- **Description :** Rafraîchit toutes les données du dashboard
- **Actions :**
  1. Vérifie les abonnements expirant/expirés
  2. Recharge les KPI Cards
  3. Recharge les graphiques
  4. Recharge l'area chart
  5. Recharge la bottom row (table + liste)
  6. Recharge la sidebar droite
  7. Met à jour le badge de notifications

---

#### 8. Gestion de la Sidebar

**Méthode :** `toggleLeftSidebar()` - **Ligne 3158-3223**
- **Lien direct :** [`DashboardController.java:3158`](src/main/java/com/example/demo/controllers/DashboardController.java#L3158)
- **Description :** Affiche/cache la sidebar gauche avec animation
- **Utilise :** `UserPreferencesDAO` pour sauvegarder l'état

**Méthode :** `loadSidebarState()` - **Ligne 3134-3151**
- **Lien direct :** [`DashboardController.java:3134`](src/main/java/com/example/demo/controllers/DashboardController.java#L3134)
- **Description :** Charge l'état de la sidebar depuis les préférences utilisateur

---

#### 9. Filtres Temporels

**Méthode :** `createFilterMenu()` - **Ligne 446-470**
- **Lien direct :** [`DashboardController.java:446`](src/main/java/com/example/demo/controllers/DashboardController.java#L446)
- **Description :** Crée le menu contextuel de filtres temporels

**Méthode :** `applyFilter()` - **Ligne 3285-3298**
- **Lien direct :** [`DashboardController.java:3285`](src/main/java/com/example/demo/controllers/DashboardController.java#L3285)
- **Description :** Applique un filtre temporel et met à jour les données affichées

---

## 🗄️ DAO (Data Access Objects)

### 1. AdherentDAO

**Fichier :** [`src/main/java/com/example/demo/dao/AdherentDAO.java`](src/main/java/com/example/demo/dao/AdherentDAO.java)

**Méthodes utilisées par le Dashboard :**

- **`findAll()`** - **Ligne 100+**
  - **Lien :** [`AdherentDAO.java`](src/main/java/com/example/demo/dao/AdherentDAO.java)
  - **Usage :** Compte le nombre total d'adhérents actifs
  - **Utilisé dans :** `createKPIGrid()` ligne 921

- **`getTauxOccupation()`** - **Ligne 300+**
  - **Lien :** [`AdherentDAO.java`](src/main/java/com/example/demo/dao/AdherentDAO.java)
  - **Usage :** Calcule le taux d'occupation (adhérents actifs / capacité max)
  - **Utilisé dans :** `createKPIGrid()` ligne 933

- **`getMonthlyGrowth()`** - **Ligne 350+**
  - **Lien :** [`AdherentDAO.java`](src/main/java/com/example/demo/dao/AdherentDAO.java)
  - **Usage :** Calcule la croissance mensuelle des adhérents
  - **Utilisé dans :** `createKPIGrid()` ligne 922

- **`findExpiringSoon()`** - **Ligne 250+**
  - **Lien :** [`AdherentDAO.java`](src/main/java/com/example/demo/dao/AdherentDAO.java)
  - **Usage :** Trouve les adhérents dont l'abonnement expire dans les 7 prochains jours
  - **Utilisé dans :** `createMiniCardsGrid()` ligne 1262

---

### 2. PaiementDAO

**Fichier :** [`src/main/java/com/example/demo/dao/PaiementDAO.java`](src/main/java/com/example/demo/dao/PaiementDAO.java)

**Méthodes utilisées par le Dashboard :**

- **`getRevenusMois(LocalDate date)`** - **Ligne 150+**
  - **Lien :** [`PaiementDAO.java`](src/main/java/com/example/demo/dao/PaiementDAO.java)
  - **Usage :** Calcule les revenus d'un mois spécifique
  - **Utilisé dans :** `createKPIGrid()` ligne 908-909

- **`getRevenusParMois(int nbMois)`** - **Ligne 200+**
  - **Lien :** [`PaiementDAO.java`](src/main/java/com/example/demo/dao/PaiementDAO.java)
  - **Usage :** Récupère les revenus des N derniers mois
  - **Retourne :** `List<MonthlyRevenue>`
  - **Utilisé dans :** `createRevenueAreaChartCard()` ligne 3600+

- **`findAll()`** - **Ligne 100+**
  - **Lien :** [`PaiementDAO.java`](src/main/java/com/example/demo/dao/PaiementDAO.java)
  - **Usage :** Récupère tous les paiements pour calculer les revenus de la semaine
  - **Utilisé dans :** `createMiniCardsGrid()` ligne 1250

---

### 3. PackDAO

**Fichier :** [`src/main/java/com/example/demo/dao/PackDAO.java`](src/main/java/com/example/demo/dao/PackDAO.java)

**Méthodes utilisées par le Dashboard :**

- **`getDistributionByAdherents()`** - **Ligne 200+**
  - **Lien :** [`PackDAO.java`](src/main/java/com/example/demo/dao/PackDAO.java)
  - **Usage :** Récupère la distribution des packs par nombre d'adhérents
  - **Retourne :** `Map<String, Integer>` (nom du pack -> nombre d'adhérents)
  - **Utilisé dans :** `createPackDistributionDonutCard()` ligne 1400+

- **`findAll()`** - **Ligne 100+**
  - **Lien :** [`PackDAO.java`](src/main/java/com/example/demo/dao/PackDAO.java)
  - **Usage :** Récupère tous les packs actifs
  - **Utilisé dans :** Diverses méthodes du dashboard

---

### 4. NotificationDAO

**Fichier :** [`src/main/java/com/example/demo/dao/NotificationDAO.java`](src/main/java/com/example/demo/dao/NotificationDAO.java)

**Méthodes utilisées par le Dashboard :**

- **`findByUserId(Integer userId)`** - **Ligne 100+**
  - **Lien :** [`NotificationDAO.java`](src/main/java/com/example/demo/dao/NotificationDAO.java)
  - **Usage :** Récupère les notifications d'un utilisateur
  - **Utilisé via :** `NotificationService.getRecentNotifications()`

- **`countUnreadByUserId(Integer userId)`** - **Ligne 200+**
  - **Lien :** [`NotificationDAO.java`](src/main/java/com/example/demo/dao/NotificationDAO.java)
  - **Usage :** Compte les notifications non lues
  - **Utilisé via :** `NotificationService.getUnreadCount()`

---

### 5. ActivityDAO

**Fichier :** [`src/main/java/com/example/demo/dao/ActivityDAO.java`](src/main/java/com/example/demo/dao/ActivityDAO.java)

**Méthodes utilisées par le Dashboard :**

- **`findRecent(int limit)`** - **Ligne 100+**
  - **Lien :** [`ActivityDAO.java`](src/main/java/com/example/demo/dao/ActivityDAO.java)
  - **Usage :** Récupère les activités récentes
  - **Utilisé via :** `ActivityService.getRecentActivities(10)`

---

## 🔧 Services

### 1. NotificationService

**Fichier :** [`src/main/java/com/example/demo/services/NotificationService.java`](src/main/java/com/example/demo/services/NotificationService.java)

**Pattern :** Singleton

**Méthodes utilisées par le Dashboard :**

- **`getInstance()`** - **Ligne 45-50**
  - **Lien :** [`NotificationService.java:45`](src/main/java/com/example/demo/services/NotificationService.java#L45)
  - **Usage :** Récupère l'instance unique du service

- **`getRecentNotifications(int limit)`** - **Ligne 388-396**
  - **Lien :** [`NotificationService.java:388`](src/main/java/com/example/demo/services/NotificationService.java#L388)
  - **Usage :** Récupère les N notifications les plus récentes
  - **Utilisé dans :** `showNotificationPopup()` ligne 365, `createNotificationPanel()` ligne 2650+

- **`getUnreadCount()`** - **Ligne 414-416**
  - **Lien :** [`NotificationService.java:414`](src/main/java/com/example/demo/services/NotificationService.java#L414)
  - **Usage :** Compte les notifications non lues
  - **Utilisé dans :** `updateNotificationBadge()` ligne 297

- **`setCurrentUserId(Integer userId)`** - **Ligne 442-444**
  - **Lien :** [`NotificationService.java:442`](src/main/java/com/example/demo/services/NotificationService.java#L442)
  - **Usage :** Définit l'ID de l'utilisateur actuel
  - **Utilisé dans :** `initializeNotificationService()` ligne 274

---

### 2. ActivityService

**Fichier :** [`src/main/java/com/example/demo/services/ActivityService.java`](src/main/java/com/example/demo/services/ActivityService.java)

**Pattern :** Singleton

**Méthodes utilisées par le Dashboard :**

- **`getInstance()`** - **Ligne 42-47**
  - **Lien :** [`ActivityService.java:42`](src/main/java/com/example/demo/services/ActivityService.java#L42)
  - **Usage :** Récupère l'instance unique du service

- **`getRecentActivities(int limit)`** - **Ligne 232-234**
  - **Lien :** [`ActivityService.java:232`](src/main/java/com/example/demo/services/ActivityService.java#L232)
  - **Usage :** Récupère les N activités les plus récentes
  - **Utilisé dans :** `createActivityPanel()` ligne 2890+

---

### 3. ThemeService

**Fichier :** [`src/main/java/com/example/demo/services/ThemeService.java`](src/main/java/com/example/demo/services/ThemeService.java)

**Méthode utilisée :**

- **`toggleTheme(Scene scene)`** - **Ligne 50+**
  - **Lien :** [`ThemeService.java`](src/main/java/com/example/demo/services/ThemeService.java)
  - **Usage :** Bascule entre thème dark et light
  - **Utilisé dans :** `setupHeader()` ligne 188

---

### 4. LanguageService

**Fichier :** [`src/main/java/com/example/demo/services/LanguageService.java`](src/main/java/com/example/demo/services/LanguageService.java)

**Méthode utilisée :**

- **`toggleLanguage()`** - **Ligne 50+**
  - **Lien :** [`LanguageService.java`](src/main/java/com/example/demo/services/LanguageService.java)
  - **Usage :** Change la langue de l'interface
  - **Utilisé dans :** `setupHeader()` ligne 200+

---

## 🎨 Composants UI

### 1. KPI Cards

**Création :** `createKPICardExact()` - **Ligne 987**

**Structure :**
```
VBox (Card)
├── Label (Titre - 15px, #B0B0B0)
├── Label (Valeur - 32px, #FFFFFF, Bold)
└── HBox (Changement)
    ├── SVG Icon (trending-up/down)
    └── Label (Texte de changement)
```

**Styles :**
- Background : `#1c1e2d`
- Border Radius : `10px`
- Shadow : `dropshadow(gaussian, rgba(0, 0, 0, 0.4), 6, 0, 0, 1)`
- Padding : `12px 20px`

---

### 2. Graphique Donut

**Création :** `createPackDistributionDonutCard()` - **Ligne 1367**

**Technologie :** Canvas avec `GraphicsContext`

**Méthode de dessin :** `drawSalesOverviewStyleDonutChart()` - **Ligne 1523**

**Données :** Distribution des packs via `packDAO.getDistributionByAdherents()`

---

### 3. Area Chart

**Création :** `createRevenueAreaChartCard()` - **Ligne 3584**

**Type :** `AreaChart<String, Number>`

**Données :** Revenus mensuels via `paiementDAO.getRevenusParMois(12)`

**Axe X :** Mois (Jan, Fév, Mar, ...)
**Axe Y :** Montant en DH

---

### 4. Mini Cards Grid

**Création :** `createMiniCardsGrid()` - **Ligne 1204**

**Structure :** `GridPane` 2x2

**Cartes :**
1. Nouveaux adhérents (cette semaine)
2. Total profit (cette semaine)
3. Expirent dans 7 jours
4. Taux moyen des paiements

---

### 5. Table des Adhérents

**Création :** `createBottomRowWithTable()` - **Ligne 1724**

**Composant :** `TableView<Adherent>`

**Colonnes :**
- Nom complet
- Pack
- Date début
- Date fin
- Statut

---

### 6. Liste Rouge (Expirations)

**Création :** `createRedListCard()` - **Ligne 1742**

**Contenu :** Liste des adhérents dont l'abonnement expire bientôt ou est expiré

**Données :** `adherentDAO.findExpiringSoon()` + adhérents expirés

---

## 🎨 Styles CSS

### Fichiers CSS utilisés :

1. **`src/main/resources/css/main.css`**
   - Styles généraux de l'application

2. **`src/main/resources/css/dashboard-cards.css`**
   - Styles spécifiques aux cartes du dashboard

3. **`src/main/resources/css/premium-dark.css`**
   - Thème dark premium

4. **`src/main/resources/css/modern.css`**
   - Styles modernes pour les composants

5. **`src/main/resources/css/components.css`**
   - Styles pour les composants réutilisables

---

## 🔄 Flux de Données

### 1. Chargement Initial

```
1. MainController.showDashboard()
   ↓
2. DashboardController.getView()
   ↓
3. Chargement du FXML (dashboard.fxml)
   ↓
4. DashboardController.initialize()
   ↓
5. setupHeader()
   setupTitleFilterSection()
   setupContent()
   setupRightSidebar()
   ↓
6. createKPIGrid()
   - adherentDAO.findAll()
   - paiementDAO.getRevenusMois()
   - adherentDAO.getTauxOccupation()
   ↓
7. createChartsRowWithMiniCards()
   - packDAO.getDistributionByAdherents()
   ↓
8. createRevenueAreaChartCard()
   - paiementDAO.getRevenusParMois(12)
   ↓
9. createNotificationPanel()
   - notificationService.getRecentNotifications(10)
   ↓
10. createActivityPanel()
    - activityService.getRecentActivities(10)
```

---

### 2. Rafraîchissement

```
1. Utilisateur clique sur refreshBtn
   ↓
2. refreshDashboard() appelé
   ↓
3. Vérification des abonnements expirant/expirés
   ↓
4. Rechargement de chaque section :
   - kpiGrid (createKPIGrid())
   - chartsRow (createChartsRowWithMiniCards())
   - areaChartCard (createRevenueAreaChartCard())
   - bottomRow (createBottomRowWithTable())
   - rightSidebar (createRightSidebar())
   ↓
5. updateNotificationBadge()
```

---

### 3. Filtre Temporel

```
1. Utilisateur clique sur filterBtn
   ↓
2. createFilterMenu() affiche le menu contextuel
   ↓
3. Utilisateur sélectionne un filtre (Today, This Week, etc.)
   ↓
4. applyFilter() appelé
   ↓
5. currentFilter mis à jour
   ↓
6. refreshDashboard() appelé pour recharger les données filtrées
```

---

## 📊 Modèles de Données

### 1. Adherent

**Fichier :** [`src/main/java/com/example/demo/models/Adherent.java`](src/main/java/com/example/demo/models/Adherent.java)

**Propriétés utilisées :**
- `id` : Identifiant unique
- `nom`, `prenom` : Nom complet
- `dateInscription` : Date d'inscription
- `dateDebut`, `dateFin` : Dates d'abonnement
- `packId` : ID du pack associé
- `actif` : Statut actif/inactif

---

### 2. Paiement

**Fichier :** [`src/main/java/com/example/demo/models/Paiement.java`](src/main/java/com/example/demo/models/Paiement.java)

**Propriétés utilisées :**
- `montant` : Montant du paiement
- `datePaiement` : Date du paiement
- `adherentId` : ID de l'adhérent
- `packId` : ID du pack

---

### 3. Pack

**Fichier :** [`src/main/java/com/example/demo/models/Pack.java`](src/main/java/com/example/demo/models/Pack.java)

**Propriétés utilisées :**
- `id` : Identifiant unique
- `nom` : Nom du pack
- `prix` : Prix du pack
- `actif` : Statut actif/inactif

---

### 4. Notification

**Fichier :** [`src/main/java/com/example/demo/models/Notification.java`](src/main/java/com/example/demo/models/Notification.java)

**Propriétés utilisées :**
- `id` : Identifiant unique
- `type` : Type de notification
- `title` : Titre
- `message` : Message
- `createdAt` : Date de création
- `isRead` : Statut lu/non lu

---

### 5. Activity

**Fichier :** [`src/main/java/com/example/demo/models/Activity.java`](src/main/java/com/example/demo/models/Activity.java)

**Propriétés utilisées :**
- `id` : Identifiant unique
- `type` : Type d'activité
- `description` : Description
- `createdAt` : Date de création

---

## 🔗 Liens Rapides vers le Code

### Controllers
- [DashboardController.java](src/main/java/com/example/demo/controllers/DashboardController.java) - Controller principal
- [MainController.java](src/main/java/com/example/demo/controllers/MainController.java) - Controller de navigation

### FXML
- [dashboard.fxml](src/main/resources/fxml/dashboard.fxml) - Interface du dashboard
- [main.fxml](src/main/resources/fxml/main.fxml) - Interface principale

### DAO
- [AdherentDAO.java](src/main/java/com/example/demo/dao/AdherentDAO.java) - Accès aux adhérents
- [PaiementDAO.java](src/main/java/com/example/demo/dao/PaiementDAO.java) - Accès aux paiements
- [PackDAO.java](src/main/java/com/example/demo/dao/PackDAO.java) - Accès aux packs
- [NotificationDAO.java](src/main/java/com/example/demo/dao/NotificationDAO.java) - Accès aux notifications
- [ActivityDAO.java](src/main/java/com/example/demo/dao/ActivityDAO.java) - Accès aux activités

### Services
- [NotificationService.java](src/main/java/com/example/demo/services/NotificationService.java) - Service de notifications
- [ActivityService.java](src/main/java/com/example/demo/services/ActivityService.java) - Service d'activités
- [ThemeService.java](src/main/java/com/example/demo/services/ThemeService.java) - Service de thème
- [LanguageService.java](src/main/java/com/example/demo/services/LanguageService.java) - Service de langue

### Models
- [Adherent.java](src/main/java/com/example/demo/models/Adherent.java) - Modèle adhérent
- [Paiement.java](src/main/java/com/example/demo/models/Paiement.java) - Modèle paiement
- [Pack.java](src/main/java/com/example/demo/models/Pack.java) - Modèle pack
- [Notification.java](src/main/java/com/example/demo/models/Notification.java) - Modèle notification
- [Activity.java](src/main/java/com/example/demo/models/Activity.java) - Modèle activité

---

## 📝 Notes Importantes

1. **Pattern Singleton** : `NotificationService` et `ActivityService` utilisent le pattern Singleton
2. **Gestion d'erreurs** : Toutes les méthodes DAO lancent `SQLException` qui doit être gérée
3. **Thread Safety** : Les services sont thread-safe grâce au pattern Singleton synchronisé
4. **Performance** : Les données sont chargées à la demande, pas toutes en même temps
5. **Responsive** : Le dashboard s'adapte à différentes tailles d'écran grâce aux contraintes JavaFX

---

## 🚀 Prochaines Étapes

Pour comprendre les autres pages :
- [Documentation Adhérents](DOCUMENTATION_ADHERENTS.md) - À créer
- [Documentation Packs](DOCUMENTATION_PACKS.md) - À créer
- [Documentation Paiements](DOCUMENTATION_PAIEMENTS.md) - À créer
- [Documentation Calendrier](DOCUMENTATION_CALENDRIER.md) - À créer

---

**Dernière mise à jour :** $(date)
**Version :** 1.0




