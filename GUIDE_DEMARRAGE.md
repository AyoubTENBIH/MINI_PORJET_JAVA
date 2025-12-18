# 🚀 Guide de Démarrage Rapide

## Installation

### Prérequis
- Java 21 ou supérieur
- Maven 3.6+ (ou utiliser le wrapper Maven inclus)

### Étapes d'installation

1. **Cloner/Télécharger le projet**

2. **Compiler le projet** :
   ```bash
   ./mvnw clean compile
   ```
   Ou sur Windows :
   ```bash
   mvnw.cmd clean compile
   ```

3. **Lancer l'application** :
   ```bash
   ./mvnw javafx:run
   ```
   Ou sur Windows :
   ```bash
   mvnw.cmd javafx:run
   ```

## Première Utilisation

### Connexion
- **Username** : `admin`
- **Password** : `admin`

### Création de votre premier pack

1. Connectez-vous avec les identifiants admin
2. Cliquez sur "Packs" dans la sidebar
3. Cliquez sur "+ Nouveau Pack"
4. Remplissez le formulaire :
   - Nom : "Pack Premium"
   - Prix : 400
   - Activités : "Musculation,Cardio,Piscine"
   - Jours : "Tous les jours"
   - Horaires : "24h/24"
   - Durée : 1
   - Unité : MOIS
   - Séances/semaine : -1 (illimité)
5. Cliquez sur "Enregistrer"

### Création de votre premier adhérent

1. Cliquez sur "Adhérents" dans la sidebar
2. Cliquez sur "+ Nouvel Adhérent"
3. Remplissez les informations :
   - CIN : AB123456
   - Nom : Dupont
   - Prénom : Jean
   - Téléphone : 0612345678
   - Sélectionnez un pack
   - Définissez les dates d'abonnement
4. Cliquez sur "Enregistrer"

### Enregistrer un paiement

1. Cliquez sur "Paiements" dans la sidebar
2. Allez dans l'onglet "➕ Nouveau Paiement"
3. Ou cliquez sur "Nouveau Paiement"
4. Sélectionnez l'adhérent
5. Le pack et le montant seront automatiquement remplis
6. Choisissez la méthode de paiement
7. Les dates d'abonnement seront calculées automatiquement
8. Cliquez sur "Enregistrer"

## Navigation

### Sidebar
- **Dashboard** : Vue d'ensemble avec statistiques
- **Packs** : Gestion des packs/abonnements
- **Adhérents** : Gestion des membres
- **Paiements** : Gestion des paiements et liste rouge

### Fonctionnalités Rapides

#### Recherche
- Utilisez la barre de recherche dans chaque module
- La recherche se fait en temps réel
- Support de la recherche multi-critères pour les adhérents

#### Liste Rouge
- Consultez l'onglet "🔴 Liste Rouge" dans Paiements
- Affiche tous les adhérents avec abonnement expiré
- Indicateur de jours de retard avec code couleur
- Bouton rapide pour enregistrer un paiement

#### Double-clic
- Double-cliquez sur une ligne dans les tableaux pour modifier rapidement

## Base de Données

La base de données SQLite est créée automatiquement au premier lancement dans :
```
src/main/resources/database/gym_management.db
```

### Données de Test

Des packs de test sont créés automatiquement :
- Pack Tapis + Musculation : 200 DH
- Pack Musculation : 150 DH
- Pack Premium All Access : 400 DH
- Pack Étudiant : 120 DH

Pour générer des adhérents de test, décommentez la ligne dans `DatabaseManager.java` :
```java
TestDataGenerator.generateTestData();
```

## Résolution de Problèmes

### Erreur de connexion à la base de données
- Vérifiez que le répertoire `src/main/resources/database/` existe
- L'application le crée automatiquement, mais en cas de problème, créez-le manuellement

### L'application ne démarre pas
- Vérifiez que Java 21+ est installé : `java -version`
- Vérifiez que JAVA_HOME est configuré
- Essayez de compiler d'abord : `./mvnw clean compile`

### Erreurs de modules
- Vérifiez que `module-info.java` est correct
- Certaines dépendances peuvent nécessiter des ajustements selon votre environnement

## Prochaines Étapes

1. **Personnaliser les packs** selon vos besoins
2. **Créer vos adhérents** réels
3. **Enregistrer les paiements** au fur et à mesure
4. **Consulter le dashboard** pour suivre les statistiques
5. **Utiliser la liste rouge** pour gérer les impayés

## Support

Pour toute question ou problème :
- Consultez le README.md pour plus de détails
- Vérifiez les logs dans la console
- La base de données peut être inspectée avec un outil SQLite

---

**Bon démarrage avec votre système de gestion de salle de sport ! 🏋️**




