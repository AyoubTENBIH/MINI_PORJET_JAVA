# 🔧 Guide de Dépannage

## Problème : "Erreur lors du chargement du dashboard"

### Solutions à essayer :

#### 1. Vérifier que la base de données est créée

La base de données doit être créée automatiquement au premier lancement. Vérifiez que le fichier existe :
```
src/main/resources/database/gym_management.db
```

Si le fichier n'existe pas :
- Créez le dossier manuellement : `src/main/resources/database/`
- Relancez l'application

#### 2. Vérifier les logs dans la console

Dans IntelliJ IDEA, regardez la console pour voir les erreurs détaillées. Les messages d'erreur vous indiqueront :
- Si la base de données ne peut pas être créée
- Si les tables n'existent pas
- Si il y a un problème de connexion

#### 3. Réinitialiser la base de données

Si la base de données est corrompue :
1. Supprimez le fichier : `src/main/resources/database/gym_management.db`
2. Relancez l'application
3. La base de données sera recréée automatiquement

#### 4. Vérifier les permissions

Assurez-vous que l'application a les permissions d'écriture dans le dossier `src/main/resources/database/`

#### 5. Vérifier la connexion à la base de données

Le problème pourrait venir de la connexion SQLite. Vérifiez dans `DatabaseManager.java` que le chemin de la base de données est correct.

### Messages d'erreur courants

#### "SQLException: no such table"
**Solution** : La base de données n'a pas été initialisée correctement. Supprimez le fichier `.db` et relancez.

#### "SQLException: database is locked"
**Solution** : Une autre instance de l'application utilise la base de données. Fermez toutes les instances.

#### "FileNotFoundException"
**Solution** : Le répertoire `database/` n'existe pas. Créez-le manuellement.

### Test rapide

Pour tester si la base de données fonctionne :

1. Ouvrez un terminal dans IntelliJ
2. Exécutez :
```bash
sqlite3 src/main/resources/database/gym_management.db
.tables
```

Si vous voyez la liste des tables, la base de données est OK.

### Solution de contournement

Si le problème persiste, vous pouvez temporairement désactiver le chargement des données réelles dans le dashboard en modifiant `DashboardController.java` pour afficher des valeurs par défaut.

---

**Note** : Après chaque correction, n'oubliez pas de :
1. Recompiler le projet : `Build → Rebuild Project`
2. Relancer l'application




