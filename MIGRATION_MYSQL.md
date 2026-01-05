# Guide de Migration SQLite vers MySQL avec XAMPP

Ce guide vous accompagne dans la migration de la base de données SQLite vers MySQL en utilisant XAMPP.

## Prérequis

1. **XAMPP installé** sur votre système
   - Télécharger depuis : https://www.apachefriends.org/
   - Version recommandée : XAMPP 8.0 ou supérieure

2. **Java et Maven** configurés
   - Java 21 ou supérieur
   - Maven 3.6 ou supérieur

## Étape 1 : Installation et Configuration de XAMPP

### 1.1 Installation de XAMPP

1. Téléchargez XAMPP depuis le site officiel
2. Installez XAMPP dans un répertoire (par exemple : `C:\xampp`)
3. Lancez le **XAMPP Control Panel**

### 1.2 Démarrage de MySQL

1. Dans le XAMPP Control Panel, cliquez sur **Start** pour le service **MySQL**
2. Vérifiez que MySQL est actif (icône verte)
3. Par défaut, MySQL écoute sur le port **3306**

### 1.3 Configuration MySQL (Optionnel)

Si vous souhaitez changer le mot de passe root ou la configuration :

1. Ouvrez **phpMyAdmin** depuis le XAMPP Control Panel
2. URL : http://localhost/phpmyadmin
3. Connectez-vous avec :
   - Utilisateur : `root`
   - Mot de passe : (vide par défaut)

## Étape 2 : Configuration du Projet

### 2.1 Mise à jour des Dépendances

Les dépendances Maven ont déjà été mises à jour :
- `sqlite-jdbc` → `mysql-connector-j` (version 8.0.33)

### 2.2 Configuration de la Connexion

La configuration MySQL est définie dans `DatabaseManager.java` :

```java
private static final String DB_HOST = "localhost";
private static final String DB_PORT = "3306";
private static final String DB_NAME = "gym_management";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "";
```

**Pour modifier ces paramètres**, éditez `src/main/java/com/example/demo/utils/DatabaseManager.java`.

## Étape 3 : Migration des Données

### 3.1 Migration Automatique

Lors du premier lancement de l'application avec MySQL :

1. La base de données `gym_management` sera créée automatiquement
2. Toutes les tables seront créées avec le schéma MySQL adapté
3. Les données par défaut seront insérées

### 3.2 Migration des Données Existantes (SQLite → MySQL)

Si vous avez des données existantes dans SQLite que vous souhaitez migrer :

#### Option A : Migration Automatique (Recommandée)

1. Assurez-vous que le fichier SQLite existe : `src/main/resources/database/gym_management.db`
2. Exécutez l'outil de migration :

```bash
# Depuis le répertoire du projet
mvn compile exec:java -Dexec.mainClass="com.example.demo.utils.DataMigrationTool"
```

L'outil va :
- Se connecter à SQLite (lecture)
- Se connecter à MySQL (écriture)
- Migrer toutes les tables dans l'ordre correct
- Vérifier l'intégrité des données

#### Option B : Migration Manuelle via phpMyAdmin

1. Exportez les données SQLite (si possible)
2. Importez dans MySQL via phpMyAdmin
3. Adaptez les requêtes SQL si nécessaire

## Étape 4 : Vérification

### 4.1 Test de Connexion

Lancez l'application et vérifiez les logs :

```
INFO: Driver MySQL chargé avec succès
INFO: Connexion à la base de données MySQL: jdbc:mysql://localhost:3306/gym_management...
INFO: Connexion à la base de données MySQL établie avec succès
```

### 4.2 Vérification des Tables

Via phpMyAdmin ou MySQL CLI :

```sql
USE gym_management;
SHOW TABLES;
```

Vous devriez voir toutes les tables :
- utilisateurs
- packs
- adherents
- paiements
- presences
- cours_collectifs
- reservations_cours
- equipements
- notifications
- activities
- objectifs
- user_preferences
- favoris

## Dépannage

### Problème : "Driver MySQL non trouvé"

**Solution :**
```bash
mvn clean install
```

Vérifiez que `mysql-connector-j` est dans `pom.xml`.

### Problème : "Access denied for user 'root'@'localhost'"

**Solutions :**
1. Vérifiez que MySQL est démarré dans XAMPP
2. Vérifiez le mot de passe root dans `DatabaseManager.java`
3. Si vous avez changé le mot de passe, mettez à jour `DB_PASSWORD`

### Problème : "Unknown database 'gym_management'"

**Solution :**
La base sera créée automatiquement au premier lancement. Si le problème persiste :

```sql
CREATE DATABASE gym_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Problème : "Table already exists"

**Solution :**
Si vous voulez réinitialiser complètement :

```sql
DROP DATABASE gym_management;
CREATE DATABASE gym_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Puis relancez l'application.

### Problème : Erreurs de migration

**Solutions :**
1. Vérifiez que le fichier SQLite existe et n'est pas corrompu
2. Vérifiez que MySQL est accessible
3. Consultez les logs détaillés dans la console
4. En cas d'échec, la transaction sera annulée (rollback)

## Différences SQLite vs MySQL

### Types de Données

| SQLite | MySQL |
|--------|-------|
| `INTEGER PRIMARY KEY AUTOINCREMENT` | `INT PRIMARY KEY AUTO_INCREMENT` |
| `TEXT` | `VARCHAR(255)` ou `TEXT` |
| `REAL` | `DECIMAL(10,2)` |
| `INTEGER DEFAULT 1` | `TINYINT(1) DEFAULT 1` |

### Fonctions

| SQLite | MySQL |
|--------|-------|
| `strftime('%Y-%m', date)` | `DATE_FORMAT(date, '%Y-%m')` |
| `date('now')` | `CURDATE()` |
| `last_insert_rowid()` | `LAST_INSERT_ID()` |
| `INSERT OR IGNORE` | `INSERT IGNORE` |

### Dates

- SQLite : Stocke les dates en `TEXT`
- MySQL : Utilise `DATE`, `DATETIME`, ou `TIMESTAMP`

## Rollback vers SQLite

Si vous souhaitez revenir à SQLite :

1. Remplacez dans `pom.xml` :
   ```xml
   <dependency>
       <groupId>org.xerial</groupId>
       <artifactId>sqlite-jdbc</artifactId>
       <version>3.44.1.0</version>
   </dependency>
   ```

2. Restaurez `DatabaseManager.java` depuis Git

3. Exécutez : `mvn clean install`

## Support

Pour toute question ou problème :
1. Consultez les logs de l'application
2. Vérifiez la documentation MySQL
3. Vérifiez que XAMPP est correctement configuré

## Notes Importantes

- **Sauvegarde** : Toujours sauvegarder vos données avant migration
- **Charset** : MySQL utilise `utf8mb4` pour un support Unicode complet
- **Transactions** : MySQL nécessite une gestion explicite des transactions
- **Performance** : MySQL est généralement plus performant pour les applications multi-utilisateurs






