# Icônes SVG pour l'application

Ce dossier contient les icônes SVG utilisées dans l'application.

## Structure recommandée

### Icônes de menu (16×16px)
- `icon-dashboard.svg` - Dashboard principal
- `icon-stats.svg` - Statistiques
- `icon-pack.svg` - Packs/Abonnements
- `icon-users.svg` - Adhérents
- `icon-payment.svg` - Paiements
- `icon-calendar.svg` - Calendrier
- `icon-settings.svg` - Paramètres
- `icon-help.svg` - Aide

### Icônes utilitaires
- `icon-search.svg` - Recherche (14×14px)
- `icon-chevron-down.svg` - Dropdown (14×14px)

## Spécifications

- **Format**: SVG
- **Couleur**: Monochrome (blanc/gris) - sera recolorée via CSS
- **Taille menu**: 16×16px
- **Taille search**: 14×14px
- **Style**: Ligne fine, moderne

## Utilisation dans JavaFX

Pour utiliser ces icônes dans JavaFX, vous pouvez :

1. **Charger directement** :
```java
ImageView icon = new ImageView(getClass().getResource("/icons/icon-dashboard.svg").toExternalForm());
icon.setFitWidth(16);
icon.setFitHeight(16);
```

2. **Utiliser avec Ikonli** (recommandé) :
```java
FontIcon icon = new FontIcon();
icon.setIconCode(MaterialDesignM.MDI_VIEW_DASHBOARD);
icon.setIconSize(16);
icon.setIconColor(Color.web("#9AA4B2"));
```

## Notes

Actuellement, l'application utilise des emojis comme solution temporaire.
Les icônes SVG peuvent être ajoutées progressivement pour remplacer les emojis.

