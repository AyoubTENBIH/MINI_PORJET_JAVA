# Guide de Migration de Base de Données

Ce guide vous explique comment migrer vos données de SQLite vers MySQL.

## Prérequis

1. **MySQL/XAMPP démarré**
   - Assurez-vous que MySQL est actif dans XAMPP
   - Port par défaut : 3306
   - Utilisateur : `root`
   - Mot de passe : (vide par défaut)

2. **Fichier SQLite source**
   - Le fichier doit exister : `src/main/resources/database/gym_management.db`
   - Si le fichier n'existe pas, la migration sera ignorée et MySQL sera initialisé avec des données par défaut

## Méthode 1 : Script automatique (Recommandé)

### Windows
```bash
migrate.bat
```

### Linux/Mac
```bash
chmod +x migrate.sh
./migrate.sh
```

## Méthode 2 : Commande Maven

```bash
mvn compile exec:java -Dexec.mainClass="com.example.demo.MigrationRunner"
```

## Méthode 3 : Depuis IntelliJ IDEA

1. Ouvrez `MigrationRunner.java`
2. Clic droit → Run 'MigrationRunner.main()'

## Ce que fait la migration

La migration va :

1. ✅ Se connecter à SQLite (lecture)
2. ✅ Se connecter à MySQL (écriture)
3. ✅ Migrer toutes les tables dans l'ordre correct :
   - utilisateurs
   - packs
   - objectifs
   - adherents
   - paiements
   - presences
   - cours_collectifs
   - reservations_cours
   - equipements
   - notifications
   - activities
   - user_preferences
   - favoris
4. ✅ Vérifier l'intégrité des données
5. ✅ Afficher un rapport de migration

## Sécurité

- ⚠️ La migration ne s'exécutera **PAS** si MySQL contient déjà des données
- ⚠️ Pour forcer la migration, videz d'abord les tables MySQL
- ✅ En cas d'erreur, toutes les modifications sont annulées (rollback)

## Vérification après migration

### Via phpMyAdmin
1. Ouvrez http://localhost/phpmyadmin
2. Sélectionnez la base `gym_management`
3. Vérifiez que toutes les tables contiennent des données

### Via MySQL CLI
```sql
USE gym_management;
SELECT COUNT(*) FROM utilisateurs;
SELECT COUNT(*) FROM adherents;
SELECT COUNT(*) FROM paiements;
```

## Dépannage

### Erreur : "Driver SQLite non trouvé"
**Solution :** Exécutez `mvn clean install` pour télécharger les dépendances

### Erreur : "MySQL contient déjà des données"
**Solution :** 
- Option 1 : Videz les tables MySQL manuellement
- Option 2 : Supprimez et recréez la base :
  ```sql
  DROP DATABASE gym_management;
  CREATE DATABASE gym_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```

### Erreur : "Connection refused"
**Solution :** Vérifiez que MySQL est démarré dans XAMPP

### Erreur : "Access denied"
**Solution :** Vérifiez les identifiants MySQL dans `DataMigrationTool.java` :
- MYSQL_USER = "root"
- MYSQL_PASSWORD = ""

## Configuration

Si vous devez modifier les paramètres de connexion, éditez :
- `src/main/java/com/example/demo/utils/DataMigrationTool.java`
- `src/main/java/com/example/demo/utils/DatabaseManager.java`

## Notes importantes

- La migration est **idempotente** : elle ne migrera pas si MySQL contient déjà des données
- Les données SQLite ne sont **pas modifiées** (lecture seule)
- Toutes les migrations sont dans une **transaction** : en cas d'erreur, tout est annulé
- La migration respecte les **contraintes de clés étrangères**


