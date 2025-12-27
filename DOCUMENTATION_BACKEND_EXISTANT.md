# 📚 Documentation Backend Existant - Dashboard

## 📋 Vue d'ensemble

Ce document décrit l'architecture backend actuelle du dashboard, les DAOs existants, leurs fonctionnalités, et les améliorations proposées.

---

## 🗂️ STRUCTURE ACTUELLE

### Packages
```
com.example.demo
├── dao/              # Data Access Objects
├── models/           # Modèles de données
├── utils/            # Utilitaires
└── controllers/      # Contrôleurs UI
```

---

## 📦 DAOs EXISTANTS

### 1. `AdherentDAO`

#### 📍 Localisation
`src/main/java/com/example/demo/dao/AdherentDAO.java`

#### 📝 Description
DAO pour la gestion des adhérents dans la base de données. Gère les opérations CRUD et les requêtes spécialisées.

#### ✅ Méthodes Disponibles

##### `create(Adherent adherent)`
- **Description** : Insère un nouvel adhérent dans la base de données
- **Paramètres** : `Adherent` - L'objet adhérent à créer
- **Retour** : `Adherent` - L'adhérent créé avec son ID généré
- **Exceptions** : `SQLException` - Si une erreur survient lors de l'insertion
- **Utilisation** : Création d'un nouvel adhérent depuis l'interface

##### `update(Adherent adherent)`
- **Description** : Met à jour un adhérent existant
- **Paramètres** : `Adherent` - L'objet adhérent avec les modifications
- **Retour** : `Adherent` - L'adhérent mis à jour
- **Exceptions** : `SQLException`
- **Utilisation** : Modification des informations d'un adhérent

##### `delete(Integer id)`
- **Description** : Supprime un adhérent (soft delete - désactive seulement)
- **Paramètres** : `Integer id` - L'ID de l'adhérent à supprimer
- **Retour** : `void`
- **Exceptions** : `SQLException`
- **Note** : Utilise un soft delete (met `actif=0` au lieu de supprimer)

##### `findById(Integer id)`
- **Description** : Récupère un adhérent par son ID
- **Paramètres** : `Integer id` - L'ID de l'adhérent
- **Retour** : `Adherent` - L'adhérent trouvé, ou `null` si non trouvé
- **Exceptions** : `SQLException`

##### `findAll()`
- **Description** : Récupère tous les adhérents actifs
- **Paramètres** : Aucun
- **Retour** : `List<Adherent>` - Liste de tous les adhérents actifs, triés par nom/prénom
- **Exceptions** : `SQLException`
- **Utilisation** : 
  - Dashboard : Affichage de la liste des adhérents
  - KPI Card 2 : Comptage des adhérents actifs
  - Mini Card 1 : Filtrage par semaine

##### `search(String searchTerm)`
- **Description** : Recherche des adhérents par critères multiples
- **Paramètres** : `String searchTerm` - Terme de recherche
- **Retour** : `List<Adherent>` - Liste des adhérents correspondants
- **Recherche dans** : nom, prénom, CIN, téléphone, email
- **Exceptions** : `SQLException`

##### `findExpired()`
- **Description** : Récupère les adhérents dont l'abonnement est expiré
- **Paramètres** : Aucun
- **Retour** : `List<Adherent>` - Liste des adhérents expirés, triés par date de fin
- **Exceptions** : `SQLException`
- **Utilisation** : 
  - Bottom Row : Liste rouge des adhérents expirés
- **Requête SQL** : `date_fin < date('now')`

##### `findExpiringSoon()`
- **Description** : Récupère les adhérents dont l'abonnement expire dans les 7 prochains jours
- **Paramètres** : Aucun
- **Retour** : `List<Adherent>` - Liste des adhérents expirant bientôt
- **Exceptions** : `SQLException`
- **Utilisation** : 
  - Mini Card 3 : "Expirent dans 7 jours"
- **Requête SQL** : `date_fin BETWEEN date('now') AND date('now', '+7 days')`

#### ⚠️ Méthodes Manquantes / À Améliorer

##### `getMonthlyGrowth(LocalDate mois)` ❌ À créer
- **Description** : Calcule le taux de croissance mensuel des adhérents
- **Paramètres** : `LocalDate mois` - Le mois pour lequel calculer
- **Retour** : `double` - Pourcentage de changement
- **Utilisation** : KPI Card 2 - Indicateur "+X% ce mois"
- **Priorité** : ⭐⭐⭐⭐ Haute

##### `getTauxOccupation()` ❌ À créer
- **Description** : Calcule le taux d'occupation (adhérents actifs / objectif)
- **Paramètres** : Aucun (ou objectif en paramètre)
- **Retour** : `double` - Pourcentage d'occupation
- **Utilisation** : KPI Card 3 - "Taux d'Occupation"
- **Priorité** : ⭐⭐⭐⭐ Haute

##### `findWithPagination(int page, int size)` ❌ À créer
- **Description** : Récupère les adhérents avec pagination
- **Paramètres** : 
  - `int page` - Numéro de page (0-indexed)
  - `int size` - Taille de la page
- **Retour** : `List<Adherent>` - Page d'adhérents
- **Utilisation** : Table avec pagination
- **Priorité** : ⭐⭐⭐ Moyenne

##### `findWithSort(String column, String order)` ❌ À créer
- **Description** : Récupère les adhérents avec tri personnalisé
- **Paramètres** :
  - `String column` - Colonne à trier
  - `String order` - "ASC" ou "DESC"
- **Retour** : `List<Adherent>` - Liste triée
- **Utilisation** : Table avec tri par colonnes
- **Priorité** : ⭐⭐⭐ Moyenne

---

### 2. `PaiementDAO`

#### 📍 Localisation
`src/main/java/com/example/demo/dao/PaiementDAO.java`

#### 📝 Description
DAO pour la gestion des paiements. Gère les opérations CRUD et les calculs de revenus.

#### ✅ Méthodes Disponibles

##### `create(Paiement paiement)`
- **Description** : Insère un nouveau paiement
- **Paramètres** : `Paiement` - L'objet paiement à créer
- **Retour** : `Paiement` - Le paiement créé avec son ID
- **Exceptions** : `SQLException`
- **Note** : Gère les dates d'abonnement associées

##### `findByAdherentId(Integer adherentId)`
- **Description** : Récupère tous les paiements d'un adhérent
- **Paramètres** : `Integer adherentId` - ID de l'adhérent
- **Retour** : `List<Paiement>` - Liste des paiements, triés par date décroissante
- **Exceptions** : `SQLException`

##### `findAll()`
- **Description** : Récupère tous les paiements
- **Paramètres** : Aucun
- **Retour** : `List<Paiement>` - Liste de tous les paiements, triés par date décroissante
- **Exceptions** : `SQLException`
- **Utilisation** : 
  - Mini Card 2 : Calcul du profit total de la semaine

##### `getRevenusMois(LocalDate mois)`
- **Description** : Calcule les revenus totaux d'un mois spécifique
- **Paramètres** : `LocalDate mois` - Le mois pour lequel calculer
- **Retour** : `Double` - Montant total des revenus (0.0 si aucun)
- **Exceptions** : `SQLException`
- **Utilisation** : 
  - KPI Card 1 : "Revenus du Mois"
  - Area Chart : Évolution des revenus (appelé plusieurs fois)
- **Note** : Filtre uniquement les paiements avec `statut='VALIDE'`
- **Requête SQL** : Utilise `strftime('%Y-%m', datetime(date_paiement))` pour filtrer par mois

#### ⚠️ Méthodes Manquantes / À Améliorer

##### `getRevenusParMois(int nombreMois)` ❌ À créer
- **Description** : Récupère les revenus des N derniers mois
- **Paramètres** : `int nombreMois` - Nombre de mois à récupérer
- **Retour** : `List<MonthlyRevenue>` - Liste des revenus mensuels
- **Utilisation** : Area Chart - "Évolution des Revenus"
- **Priorité** : ⭐⭐⭐⭐ Haute
- **Note** : Évite d'appeler `getRevenusMois()` plusieurs fois

##### `getTauxMoyen()` ❌ À créer
- **Description** : Calcule le taux moyen des paiements
- **Paramètres** : Aucun
- **Retour** : `double` - Taux moyen
- **Utilisation** : Mini Card 4 - "Taux moyen"
- **Priorité** : ⭐⭐⭐ Moyenne

##### `getRevenusByDateRange(LocalDate debut, LocalDate fin)` ❌ À créer
- **Description** : Calcule les revenus sur une plage de dates
- **Paramètres** :
  - `LocalDate debut` - Date de début
  - `LocalDate fin` - Date de fin
- **Retour** : `Double` - Montant total
- **Utilisation** : Filtre temporel du dashboard
- **Priorité** : ⭐⭐⭐⭐ Haute

---

### 3. `PackDAO`

#### 📍 Localisation
`src/main/java/com/example/demo/dao/PackDAO.java`

#### 📝 Description
DAO pour la gestion des packs/abonnements. Gère les opérations CRUD et la recherche.

#### ✅ Méthodes Disponibles

##### `create(Pack pack)`
- **Description** : Insère un nouveau pack
- **Paramètres** : `Pack` - L'objet pack à créer
- **Retour** : `Pack` - Le pack créé avec son ID
- **Exceptions** : `SQLException`

##### `update(Pack pack)`
- **Description** : Met à jour un pack existant
- **Paramètres** : `Pack` - Le pack avec les modifications
- **Retour** : `Pack` - Le pack mis à jour
- **Exceptions** : `SQLException`

##### `delete(Integer id)`
- **Description** : Supprime un pack (soft delete)
- **Paramètres** : `Integer id` - ID du pack
- **Retour** : `void`
- **Exceptions** : `SQLException`
- **Note** : Met `actif=0` au lieu de supprimer

##### `findById(Integer id)`
- **Description** : Récupère un pack par son ID
- **Paramètres** : `Integer id` - ID du pack
- **Retour** : `Pack` - Le pack trouvé, ou `null`
- **Exceptions** : `SQLException`

##### `findAll()`
- **Description** : Récupère tous les packs actifs
- **Paramètres** : Aucun
- **Retour** : `List<Pack>` - Liste des packs actifs, triés par nom
- **Exceptions** : `SQLException`

##### `findAll(Boolean actifsSeulement)`
- **Description** : Récupère tous les packs (actifs ou tous)
- **Paramètres** : `Boolean actifsSeulement` - Si true, seulement les actifs
- **Retour** : `List<Pack>` - Liste des packs
- **Exceptions** : `SQLException`

##### `searchByNom(String searchTerm)`
- **Description** : Recherche des packs par nom
- **Paramètres** : `String searchTerm` - Terme de recherche
- **Retour** : `List<Pack>` - Liste des packs correspondants
- **Exceptions** : `SQLException`

#### ⚠️ Méthodes Manquantes / À Améliorer

##### `getDistributionByAdherents()` ❌ À créer
- **Description** : Récupère la distribution des packs par nombre d'adhérents
- **Paramètres** : Aucun
- **Retour** : `Map<Pack, Integer>` ou `List<PackDistribution>` - Distribution
- **Utilisation** : Donut Chart - "Sales Overview"
- **Priorité** : ⭐⭐⭐⭐ Haute
- **Note** : Joint avec la table `adherents` pour compter les adhérents par pack

---

## 🏗️ MODÈLES EXISTANTS

### 1. `Adherent`

#### Propriétés Principales
- `id` : Integer
- `cin` : String
- `nom` : String
- `prenom` : String
- `dateNaissance` : LocalDate
- `telephone` : String
- `email` : String
- `adresse` : String
- `photo` : String
- `poids` : Double
- `taille` : Double
- `objectifs` : String
- `problemesSante` : String
- `packId` : Integer
- `dateDebut` : LocalDate
- `dateFin` : LocalDate
- `actif` : Boolean
- `dateInscription` : LocalDate

#### Méthodes Utiles
- `getNomComplet()` : Retourne "nom prénom"

---

### 2. `Paiement`

#### Propriétés Principales
- `id` : Integer
- `adherentId` : Integer
- `packId` : Integer
- `montant` : Double
- `datePaiement` : LocalDateTime
- `methodePaiement` : Enum (ESPECES, CARTE, CHEQUE, VIREMENT)
- `statut` : Enum (VALIDE, ANNULE, EN_ATTENTE)
- `reference` : String
- `dateDebutAbonnement` : LocalDate
- `dateFinAbonnement` : LocalDate
- `notes` : String

---

### 3. `Pack`

#### Propriétés Principales
- `id` : Integer
- `nom` : String
- `prix` : Double
- `activites` : List<String>
- `joursDisponibilite` : String
- `horaires` : String
- `duree` : Integer
- `uniteDuree` : String
- `seancesSemaine` : Integer
- `accesCoach` : Boolean
- `actif` : Boolean
- `description` : String
- `dateCreation` : LocalDate

---

## 🛠️ UTILITAIRES EXISTANTS

### 1. `DatabaseManager`

#### Description
Singleton pour gérer la connexion à la base de données SQLite.

#### Méthodes
- `getInstance()` : Retourne l'instance unique
- `getConnection()` : Retourne une connexion à la base de données

---

### 2. `DateUtils`

#### Description
Utilitaires pour le parsing des dates depuis la base de données.

#### Méthodes
- `parseDate(String dateStr)` : Parse une date depuis une chaîne
- `parseDateTime(String dateTimeStr)` : Parse une date/heure depuis une chaîne

---

## ⚠️ PROBLÈMES IDENTIFIÉS

### 1. **Répétition de Code**
- Pattern try-with-resources répété dans chaque méthode
- Mapping ResultSet répété
- Gestion d'erreurs similaire partout

### 2. **Manque de Documentation**
- Pas de JavaDoc sur les méthodes
- Commentaires manquants pour la logique complexe
- Pas de documentation des paramètres

### 3. **Valeurs Hardcodées**
- `changeAdherents = 5.2` dans DashboardController (ligne 422)
- `objectifAdherents = 80` dans DashboardController (ligne 433)
- `nombreMois = 6` pour l'area chart

### 4. **Performance**
- `getRevenusMois()` appelé plusieurs fois pour l'area chart
- Pas de cache pour les données fréquemment accédées
- Pas de pagination pour les grandes listes

### 5. **Gestion d'Erreurs**
- Erreurs loggées mais pas toujours propagées correctement
- Pas de gestion centralisée des erreurs
- Pas de messages d'erreur utilisateur-friendly

---

## 🔧 AMÉLIORATIONS PROPOSÉES

### 1. **Créer des Services Métier**
- `DashboardService` : Centralise la récupération des données
- `StatisticsService` : Centralise les calculs statistiques
- `NotificationService` : Gère les notifications automatiques

### 2. **Créer des DTOs**
- `MonthlyRevenue` : Pour les revenus mensuels
- `PackDistribution` : Pour la distribution des packs
- `DashboardData` : Pour toutes les données du dashboard

### 3. **Améliorer la Documentation**
- Ajouter JavaDoc à toutes les méthodes publiques
- Documenter les paramètres et valeurs de retour
- Ajouter des exemples d'utilisation

### 4. **Créer des Constantes**
- `DashboardConstants` : Toutes les valeurs magiques
- `NotificationTypes` : Types de notifications
- `ActivityTypes` : Types d'activités

### 5. **Optimiser les Requêtes**
- Créer des méthodes batch pour récupérer plusieurs données
- Implémenter un cache simple pour les données fréquentes
- Utiliser des requêtes optimisées avec JOINs

---

## 📊 STATISTIQUES DU CODE

### DAOs
- **AdherentDAO** : 8 méthodes publiques
- **PaiementDAO** : 4 méthodes publiques
- **PackDAO** : 6 méthodes publiques

### Méthodes Manquantes
- **AdherentDAO** : 4 méthodes à créer
- **PaiementDAO** : 3 méthodes à créer
- **PackDAO** : 1 méthode à créer

### Nouveaux DAOs Nécessaires
- **NotificationDAO** : À créer complètement
- **ActivityDAO** : À créer complètement
- **ObjectifDAO** : À créer complètement
- **UserPreferencesDAO** : À créer complètement
- **FavorisDAO** : À créer complètement

---

## ✅ CHECKLIST D'AMÉLIORATION

### Documentation
- [ ] Ajouter JavaDoc à toutes les classes DAO
- [ ] Ajouter JavaDoc à toutes les méthodes publiques
- [ ] Documenter les paramètres et valeurs de retour
- [ ] Ajouter des exemples d'utilisation

### Code Quality
- [ ] Créer des constantes pour les valeurs magiques
- [ ] Éliminer la répétition de code
- [ ] Améliorer la gestion d'erreurs
- [ ] Ajouter la validation des données

### Performance
- [ ] Optimiser les requêtes SQL
- [ ] Implémenter la pagination
- [ ] Créer des méthodes batch
- [ ] Ajouter un cache simple

### Fonctionnalités
- [ ] Créer les méthodes manquantes
- [ ] Créer les nouveaux DAOs
- [ ] Créer les services métier
- [ ] Créer les DTOs nécessaires

---

**Date de création** : 2024-01-XX  
**Dernière mise à jour** : 2024-01-XX  
**Version** : 1.0



