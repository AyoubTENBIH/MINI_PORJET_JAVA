# 📦 Liste des Icônes et Images Nécessaires pour le Dashboard

## 🎯 Icônes SVG Requises (Haute Qualité)

### 1. Icônes de Tendances (Trending)
- **Nom du fichier**: `trending-up.svg`
  - **Usage**: Indicateur de croissance positive (flèche vers le haut)
  - **Taille recommandée**: 16x16px ou 24x24px
  - **Couleur**: #10b981 (vert) ou transparent pour appliquer dynamiquement
  - **Style**: Outline ou filled selon préférence

- **Nom du fichier**: `trending-down.svg`
  - **Usage**: Indicateur de baisse (flèche vers le bas)
  - **Taille recommandée**: 16x16px ou 24x24px
  - **Couleur**: #ef4444 (rouge) ou transparent
  - **Style**: Outline ou filled

### 2. Icône d'Édition (Gauge Card)
- **Nom du fichier**: `icon-edit.svg` ou `icon-pencil.svg`
  - **Usage**: Bouton d'édition en haut à droite de la carte gauge
  - **Taille recommandée**: 16x16px
  - **Couleur**: #fbbf24 (jaune-or)
  - **Style**: Outline préféré

### 3. Icônes de Métriques (Mini Cards)
- **Nom du fichier**: `icon-users.svg` ou `icon-user-plus.svg`
  - **Usage**: Nouveaux adhérents
  - **Taille**: 20x20px
  - **Couleur**: Transparent (appliquée dynamiquement)

- **Nom du fichier**: `icon-dollar.svg` ou `icon-currency.svg`
  - **Usage**: Revenus/Profit
  - **Taille**: 20x20px
  - **Couleur**: Transparent

- **Nom du fichier**: `icon-alert.svg` ou `icon-warning.svg`
  - **Usage**: Expirations à venir
  - **Taille**: 20x20px
  - **Couleur**: Transparent

- **Nom du fichier**: `icon-bar-chart.svg` ou `icon-chart.svg`
  - **Usage**: Statistiques/Taux moyen
  - **Taille**: 20x20px
  - **Couleur**: Transparent

### 4. Icônes du Header (Déjà présentes)
- ✅ `menu.svg` - Menu hamburger
- ✅ `star.svg` - Étoile/Favoris
- ✅ `moon.svg` - Mode sombre
- ✅ `refresh-ccw.svg` - Actualiser
- ✅ `bell.svg` - Notifications
- ✅ `globe.svg` - Langue/Paramètres
- ✅ `icon-chevron-down.svg` - Dropdown

### 5. Icônes de Navigation Sidebar (Déjà présentes)
- ✅ `icon-dashboard.svg`
- ✅ `icon-users.svg`
- ✅ `icon-pack.svg`
- ✅ `icon-payment.svg`
- ✅ `icon-calendar.svg`
- ✅ `icon-stats.svg`
- ✅ `icon-settings.svg`
- ✅ `icon-help.svg`

---

## 🖼️ Images PNG/Background Requises

### 1. Image de Background pour Carte Gauge
- **Nom du fichier**: `gauge_needle_card.png` (déjà présent)
  - **Usage**: Effet de glow vert dans le coin inférieur droit de la carte gauge
  - **Taille recommandée**: 420x100px (même taille que la carte)
  - **Format**: PNG avec transparence
  - **Style**: Gradient radial vert subtil (#9EFF00 avec opacité)
  - **Position**: Coin inférieur droit, effet de glow diffus

### 2. Images de Background Optionnelles (Pour futures cartes)
- **Nom du fichier**: `card-background-gradient-1.png`
  - **Usage**: Background optionnel pour cartes spéciales
  - **Taille**: 170x100px ou 420x100px selon carte
  - **Style**: Gradient subtil, très sombre

---

## 📋 Checklist d'Installation

### Icônes à Télécharger/Installer :

#### Priorité HAUTE (Nécessaires immédiatement) :
1. ✅ `trending-up.svg` - **DÉJÀ PRÉSENT**
2. ✅ `trending-down.svg` - **DÉJÀ PRÉSENT**
3. ⚠️ `icon-edit.svg` ou `icon-pencil.svg` - **À AJOUTER**
   - Source recommandée: Feather Icons, Heroicons, ou Material Icons
   - Format SVG avec path simple

#### Priorité MOYENNE (Pour mini cards) :
4. ⚠️ `icon-dollar.svg` ou `icon-currency.svg` - **À AJOUTER**
5. ⚠️ `icon-alert.svg` ou `icon-warning.svg` - **À AJOUTER**
6. ⚠️ `icon-bar-chart.svg` - **À AJOUTER**

#### Priorité BASSE (Déjà présentes ou optionnelles) :
7. ✅ Toutes les autres icônes sont déjà présentes dans `/icons/`

---

## 🔗 Sources Recommandées pour Télécharger les Icônes

### 1. Feather Icons (Recommandé)
- **URL**: https://feathericons.com/
- **Format**: SVG
- **Style**: Outline, minimaliste
- **Icônes disponibles**:
  - `trending-up` ✅
  - `trending-down` ✅
  - `edit` ou `edit-2` ⚠️
  - `dollar-sign` ⚠️
  - `alert-circle` ⚠️
  - `bar-chart-2` ⚠️

### 2. Heroicons
- **URL**: https://heroicons.com/
- **Format**: SVG (Outline ou Solid)
- **Style**: Moderne, épuré

### 3. Material Icons
- **URL**: https://fonts.google.com/icons
- **Format**: SVG
- **Style**: Material Design

### 4. Lucide Icons
- **URL**: https://lucide.dev/
- **Format**: SVG
- **Style**: Similaire à Feather, très complet

---

## 📐 Spécifications Techniques des Icônes

### Format SVG Requis :
```xml
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
  <path d="..."/>
</svg>
```

### Caractéristiques :
- **ViewBox**: `0 0 24 24` (standard)
- **Stroke**: `currentColor` (pour appliquer la couleur dynamiquement)
- **Stroke width**: `2` (pour outline) ou `fill` pour filled
- **Pas de couleur fixe**: Utiliser `currentColor` ou `none` avec stroke

### Conversion pour JavaFX :
- Extraire le `d` du `<path>`
- Convertir les virgules en espaces si nécessaire
- Utiliser avec `SVGPath` JavaFX

---

## 🎨 Couleurs à Appliquer Dynamiquement

### Couleurs des Icônes selon le Contexte :

**Icônes de Tendances** :
- Positif: `#10b981` (vert)
- Négatif: `#ef4444` (rouge)

**Icône Edit** :
- Couleur: `#fbbf24` (jaune-or)

**Icônes Mini Cards** :
- Couleur de fond: `rgba(0, 230, 118, 0.15)` (vert transparent)
- Couleur icône: `#00E676` ou `#10b981`

**Icônes Header** :
- Couleur par défaut: `#9AA4B2`
- Couleur hover: `#9EFF00`

---

## 📁 Structure du Dossier Icons

```
src/main/resources/icons/
├── trending-up.svg          ✅ (déjà présent)
├── trending-down.svg        ✅ (déjà présent)
├── icon-edit.svg            ⚠️ (à ajouter)
├── icon-dollar.svg          ⚠️ (à ajouter)
├── icon-alert.svg           ⚠️ (à ajouter)
├── icon-bar-chart.svg       ⚠️ (à ajouter)
├── gauge_needle_card.png    ✅ (déjà présent)
├── menu.svg                 ✅
├── star.svg                 ✅
├── moon.svg                 ✅
├── refresh-ccw.svg          ✅
├── bell.svg                 ✅
├── globe.svg                ✅
├── icon-chevron-down.svg   ✅
├── icon-dashboard.svg       ✅
├── icon-users.svg           ✅
├── icon-pack.svg            ✅
├── icon-payment.svg         ✅
├── icon-calendar.svg        ✅
├── icon-stats.svg           ✅
├── icon-settings.svg        ✅
├── icon-help.svg            ✅
└── gym-svgrepo-com.svg      ✅
```

---

## ✅ Actions Requises

1. **Télécharger les icônes manquantes** :
   - `icon-edit.svg` ou `icon-pencil.svg`
   - `icon-dollar.svg` ou `icon-currency.svg`
   - `icon-alert.svg` ou `icon-warning.svg`
   - `icon-bar-chart.svg`

2. **Placer dans** : `src/main/resources/icons/`

3. **Ajouter les paths SVG dans** : `SvgIcons.java`

4. **Mettre à jour** : `getSvgPathForIcon()` dans `DashboardController.java`

---

## 🔍 Noms Exactes des Icônes à Chercher

Pour faciliter la recherche, voici les noms exacts à utiliser :

1. **Edit/Pencil**: `edit`, `edit-2`, `pencil`, `pencil-square`
2. **Dollar/Currency**: `dollar-sign`, `currency-dollar`, `money`
3. **Alert/Warning**: `alert-circle`, `alert-triangle`, `warning`
4. **Bar Chart**: `bar-chart`, `bar-chart-2`, `chart-bar`

---

## 📝 Notes Importantes

- Toutes les icônes doivent être en format **SVG**
- Préférer les icônes **outline** (stroke) plutôt que filled pour plus de flexibilité
- Utiliser `currentColor` dans le SVG pour permettre la coloration dynamique
- Taille standard recommandée : **24x24px** (viewBox)
- Les icônes seront redimensionnées dans le code JavaFX selon les besoins
