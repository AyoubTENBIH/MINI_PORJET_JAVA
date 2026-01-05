# Plan de Refactorisation - Module Statistiques

## 📋 Analyse Complète du Module

### 🎯 Structure Actuelle
Le module statistiques contient :
1. **Header** : Menu, breadcrumb, icônes (moon, refresh, bell, globe)
2. **Section Titre/Filtre** : Titre "Statistiques & Analytics"
3. **Navigation Tabs** : 4 boutons (Évolution, Répartition, Revenus, Rétention)
4. **Vues avec Graphiques** :
   - **Évolution** : 2 KPI Cards + LineChart
   - **Répartition** : PieChart
   - **Revenus** : 2 KPI Cards + BarChart
   - **Rétention** : LineChart

### 📊 Éléments à Séparer

## 🎨 Éléments à Déplacer vers CSS

### 1. Styles Inline (67 occurrences de `setStyle()`)
- ✅ Header : background, border, text-fill
- ✅ Breadcrumb : text-fill, font-size, font-weight
- ✅ Title Section : background, text-fill, font-size
- ✅ Navigation Tabs : background, text-fill, border, hover states
- ✅ KPI Cards : background, border-radius, effect, text-fill
- ✅ Chart Cards : background, border-radius, effect, text-fill
- ✅ Charts (LineChart, PieChart, BarChart) : 
  - Background transparent
  - Axis styles (tick-label-fill, font-size)
  - Legend styles
  - Plot background
  - Pie colors
  - Bar colors
  - Grid lines
- ✅ Error Label : background, border, text-fill

### 2. Dimensions et Layout
- ✅ Padding, spacing, alignment
- ✅ Min/Max/Pref sizes
- ✅ Border radius, effects

## 📄 Éléments à Déplacer vers FXML

### 1. Structure Principale
- ✅ BorderPane root
- ✅ Header (HBox avec boutons et breadcrumb)
- ✅ Title Section (HBox avec titre)
- ✅ ScrollPane contentScroll
- ✅ VBox contentWrapper
- ✅ Navigation Tabs Container (HBox)

### 2. Structure des Vues (Conteneurs statiques)
- ✅ VBox pour chaque vue (Évolution, Répartition, Revenus, Rétention)
- ✅ HBox pour les KPI Cards
- ✅ VBox pour les Chart Cards (structure, pas les graphiques)

### 3. Composants Statiques
- ✅ Labels de titre
- ✅ Structure des cartes KPI (sans les valeurs dynamiques)
- ✅ Conteneurs pour les graphiques (sans les graphiques eux-mêmes)

### ⚠️ Note Importante sur les Graphiques
Les graphiques (LineChart, PieChart, BarChart) doivent rester dans le controller car :
- Ils sont créés dynamiquement avec des données
- Ils nécessitent une configuration complexe (axes, séries, données)
- Ils sont mis à jour dynamiquement

## 💻 Ce qui Reste dans le Controller (Logique Métier)

### 1. Création et Configuration des Graphiques
- ✅ `createEvolutionLineChart()` - Logique de création LineChart
- ✅ `createPacksPieChart()` - Logique de création PieChart
- ✅ `createRevenusBarChart()` - Logique de création BarChart
- ✅ `createRetentionLineChart()` - Logique de création LineChart
- ✅ Configuration des axes, séries, données

### 2. Logique de Données
- ✅ `loadEvolutionData()` - Calcul des données pour Évolution
- ✅ `loadRepartitionData()` - Calcul des données pour Répartition
- ✅ `loadRevenusData()` - Calcul des données pour Revenus
- ✅ `loadRetentionData()` - Calcul des données pour Rétention
- ✅ Calculs statistiques (totaux, moyennes, pourcentages)

### 3. Event Handlers
- ✅ `switchView()` - Changement de vue
- ✅ Navigation tabs onClick
- ✅ Header buttons onClick

### 4. Méthodes Utilitaires
- ✅ `loadSVGIcon()` - Chargement des icônes SVG
- ✅ `getSvgPathForIcon()` - Mapping des icônes
- ✅ `setIconColor()` - Changement de couleur d'icône
- ✅ `styleAllTextNodes()` - Stylisation récursive des textes
- ✅ `createKPICard()` - Création dynamique des cartes KPI (valeurs calculées)

### 5. Méthodes de Rafraîchissement
- ✅ `refreshContent()` - Rafraîchissement du contenu
- ✅ `refreshEvolutionView()` - Rafraîchissement vue Évolution
- ✅ `refreshRepartitionView()` - Rafraîchissement vue Répartition
- ✅ `refreshRevenusView()` - Rafraîchissement vue Revenus
- ✅ `refreshRetentionView()` - Rafraîchissement vue Rétention

## 🎯 Plan d'Action Détaillé

### Étape 1 : Créer le FXML (`statistiques.fxml`)
- [ ] Créer BorderPane root avec styleClass
- [ ] Créer Header (HBox) avec tous les boutons et breadcrumb
- [ ] Créer Title Section (HBox) avec titre
- [ ] Créer ScrollPane contentScroll
- [ ] Créer VBox contentWrapper
- [ ] Créer Navigation Tabs Container (HBox) avec 4 boutons
- [ ] Créer conteneurs pour chaque vue (VBox)
- [ ] Créer conteneurs pour KPI Cards (HBox)
- [ ] Créer conteneurs pour Chart Cards (VBox avec titre)
- [ ] Ajouter tous les fx:id nécessaires

### Étape 2 : Créer le CSS (`statistiques.css`)
- [ ] Styles pour root et layout principal
- [ ] Styles pour header et breadcrumb
- [ ] Styles pour title section
- [ ] Styles pour navigation tabs (normal, active, hover)
- [ ] Styles pour KPI cards (container, label, value, change)
- [ ] Styles pour chart cards (container, title)
- [ ] Styles pour les graphiques :
  - LineChart (background, axis, legend, plot-background)
  - PieChart (background, pie-colors, labels, legend)
  - BarChart (background, axis, legend, bar-colors, grid-lines)
- [ ] Styles pour error label
- [ ] Styles pour icônes SVG

### Étape 3 : Refactoriser le Controller
- [ ] Modifier `getView()` pour charger `statistiques.fxml` et `statistiques.css`
- [ ] Ajouter `@FXML` annotations pour tous les composants
- [ ] Créer `@FXML initialize()` pour configurer les event handlers
- [ ] Refactoriser `createBasicView()` en vue de secours minimale
- [ ] Supprimer `createHeader()` - utiliser FXML
- [ ] Supprimer `createTitleFilterSection()` - utiliser FXML
- [ ] Refactoriser `createNavigationTabs()` pour utiliser les boutons FXML
- [ ] Refactoriser `createNavigationTabButton()` pour utiliser CSS classes
- [ ] Refactoriser `switchView()` pour utiliser les boutons FXML
- [ ] Refactoriser `createKPICard()` pour utiliser CSS classes
- [ ] Refactoriser les méthodes de création de graphiques pour utiliser CSS classes
- [ ] Remplacer tous les `setStyle()` par `getStyleClass().add()`
- [ ] Simplifier `loadSVGIcon()` pour utiliser CSS classes

### Étape 4 : Gestion des Graphiques
- [ ] Les graphiques sont créés dans le controller (logique métier)
- [ ] Les graphiques sont ajoutés aux conteneurs FXML via `fx:id`
- [ ] Les styles des graphiques sont appliqués via CSS classes
- [ ] Les couleurs dynamiques (pie colors, bar colors) restent dans le controller

### Étape 5 : Vérifications Finales
- [ ] Vérifier qu'il ne reste plus de `setStyle()` inline
- [ ] Vérifier que tous les composants FXML ont des `fx:id`
- [ ] Vérifier que tous les styles sont dans le CSS
- [ ] Vérifier que la logique métier est dans le controller
- [ ] Tester que tous les graphiques s'affichent correctement
- [ ] Tester la navigation entre les vues
- [ ] Tester le rafraîchissement des données

## 📝 Notes Importantes

### Graphiques
- Les graphiques JavaFX (LineChart, PieChart, BarChart) sont créés programmatiquement
- Les conteneurs pour les graphiques sont dans le FXML
- Les styles des graphiques sont dans le CSS
- Les couleurs dynamiques (basées sur les données) restent dans le controller

### Navigation Tabs
- Les boutons de navigation sont dans le FXML
- Les styles (normal, active, hover) sont dans le CSS
- La logique de changement de vue reste dans le controller

### KPI Cards
- La structure des cartes peut être dans le FXML (optionnel)
- Les valeurs sont calculées dynamiquement dans le controller
- Les styles sont dans le CSS

