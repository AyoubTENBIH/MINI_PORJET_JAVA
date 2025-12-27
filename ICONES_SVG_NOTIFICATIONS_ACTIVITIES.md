# 📦 Liste des Icônes SVG Nécessaires pour Notifications & Activities Panel

## 🎯 Icônes à installer dans `/src/main/resources/icons/`

### **NOTIFICATIONS (Badges circulaires verts #10b981)**

1. **`icon-users.svg`** ✅ (Déjà disponible)
   - **Usage** : "66 New users registered"
   - **Description** : Icône de personnes/utilisateurs

2. **`icon-shopping-bag.svg`** ou **`icon-shopping-cart.svg`**
   - **Usage** : "132 Orders placed"
   - **Description** : Icône de sac de courses ou panier d'achat
   - **Alternative** : `icon-package.svg` si disponible

3. **`icon-dollar-sign.svg`** ✅ (Déjà disponible)
   - **Usage** : "Funds have been withdrawn"
   - **Description** : Icône de dollar/argent

4. **`icon-mail.svg`** ou **`icon-message.svg`** ou **`icon-envelope.svg`**
   - **Usage** : "5 Unread messages"
   - **Description** : Icône d'enveloppe ou message
   - **Alternative** : `icon-bell.svg` si pas de mail disponible

---

### **ACTIVITIES (Avatars circulaires colorés)**

5. **`icon-edit.svg`** ✅ (Déjà disponible)
   - **Usage** : "Changed the style"
   - **Description** : Icône de crayon/édition
   - **Alternative** : `icon-brush.svg` ou `icon-palette.svg`

6. **`icon-package.svg`** ou **`icon-box.svg`**
   - **Usage** : "177 New products added"
   - **Description** : Icône de boîte/paquet
   - **Note** : `icon-pack.svg` existe déjà mais peut être utilisé

7. **`icon-archive.svg`** ou **`icon-folder-archive.svg`**
   - **Usage** : "11 Products have been archived"
   - **Description** : Icône d'archive/dossier archivé

8. **`icon-file-x.svg`** ou **`icon-trash.svg`** ou **`icon-x-circle.svg`**
   - **Usage** : "Page 'Tags' has been removed"
   - **Description** : Icône de suppression/fichier supprimé
   - **Alternative** : `icon-alert-octagon.svg` si pas de trash

---

## 📋 Récapitulatif des Icônes

| # | Nom Fichier | Usage | Statut |
|---|-------------|-------|--------|
| 1 | `icon-users.svg` | New users registered | ✅ Disponible |
| 2 | `icon-shopping-bag.svg` | Orders placed | ❌ À créer |
| 3 | `icon-dollar-sign.svg` | Funds withdrawn | ✅ Disponible |
| 4 | `icon-mail.svg` | Unread messages | ❌ À créer |
| 5 | `icon-edit.svg` | Changed the style | ✅ Disponible |
| 6 | `icon-package.svg` | New products added | ⚠️ `icon-pack.svg` existe |
| 7 | `icon-archive.svg` | Products archived | ❌ À créer |
| 8 | `icon-file-x.svg` | Page removed | ❌ À créer |

---

## 🎨 Spécifications des Icônes SVG

### **Format requis :**
- **Format** : SVG
- **ViewBox** : `0 0 24 24` (standard)
- **Style** : Outline (stroke) ou filled selon préférence
- **Couleur** : Monochrome (sera recolorée via code)
- **Taille** : 16x16px dans le cercle de 32px

### **Icônes à créer (priorité) :**

1. **`icon-shopping-bag.svg`** - Priorité HAUTE
2. **`icon-mail.svg`** - Priorité HAUTE  
3. **`icon-archive.svg`** - Priorité MOYENNE
4. **`icon-file-x.svg`** - Priorité MOYENNE

---

## 💡 Sources recommandées pour les icônes SVG

- **Heroicons** : https://heroicons.com/
- **Lucide Icons** : https://lucide.dev/icons/
- **Feather Icons** : https://feathericons.com/
- **Tabler Icons** : https://tabler.io/icons

### **Noms équivalents dans différentes librairies :**

| Usage | Heroicons | Lucide | Feather | Tabler |
|-------|-----------|--------|---------|--------|
| Shopping bag | `shopping-bag` | `shopping-bag` | `shopping-bag` | `shopping-bag` |
| Mail | `envelope` | `mail` | `mail` | `mail` |
| Archive | `archive-box` | `archive` | `archive` | `archive` |
| File X | `document-minus` | `file-x` | `file-minus` | `file-x` |

---

## ✅ Checklist Installation

- [ ] Télécharger `icon-shopping-bag.svg` (24x24 viewBox)
- [ ] Télécharger `icon-mail.svg` (24x24 viewBox)
- [ ] Télécharger `icon-archive.svg` (24x24 viewBox)
- [ ] Télécharger `icon-file-x.svg` (24x24 viewBox)
- [ ] Placer dans `/src/main/resources/icons/`
- [ ] Ajouter les paths SVG dans `SvgIcons.java`
- [ ] Ajouter les mappings dans `getSvgPathForIcon()`

---

## 🔧 Code à ajouter dans SvgIcons.java

```java
// Shopping Bag (pour Orders placed)
public static final String SHOPPING_BAG = 
    "M 16 6 L 8 6 A 2 2 0 0 0 6 8 L 6 19 A 2 2 0 0 0 8 21 L 16 21 A 2 2 0 0 0 18 19 L 18 8 A 2 2 0 0 0 16 6 Z " +
    "M 6 8 L 18 8 M 9 11 L 9 16 M 15 11 L 15 16";

// Mail (pour Unread messages)
public static final String MAIL = 
    "M 3 8 L 12 13 L 21 8 M 3 8 L 3 18 A 2 2 0 0 0 5 20 L 19 20 A 2 2 0 0 0 21 18 L 21 8";

// Archive (pour Products archived)
public static final String ARCHIVE = 
    "M 5 8 L 5 19 A 2 2 0 0 0 7 21 L 17 21 A 2 2 0 0 0 19 19 L 19 8 M 5 8 L 12 3 L 19 8 M 8 12 L 16 12";

// File X (pour Page removed)
public static final String FILE_X = 
    "M 14 2 L 6 2 A 2 2 0 0 0 4 4 L 4 20 A 2 2 0 0 0 6 22 L 18 22 A 2 2 0 0 0 20 20 L 20 8 Z " +
    "M 14 2 L 14 8 L 20 8 M 9 15 L 15 9 M 15 15 L 9 9";
```

---

## 📝 Mapping dans DashboardController.java

```java
case "icon-shopping-bag" -> SvgIcons.SHOPPING_BAG;
case "icon-mail" -> SvgIcons.MAIL;
case "icon-archive" -> SvgIcons.ARCHIVE;
case "icon-file-x" -> SvgIcons.FILE_X;
```



