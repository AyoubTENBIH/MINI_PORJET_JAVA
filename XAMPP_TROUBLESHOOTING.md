# Guide de Dépannage XAMPP - MySQL ne démarre pas

## ⚠️ PROBLÈME CRITIQUE : Corruption InnoDB

Si vous voyez l'erreur `InnoDB: Missing MLOG_CHECKPOINT`, consultez d'abord **[XAMPP_INNODB_FIX.md](XAMPP_INNODB_FIX.md)** pour la solution spécifique.

## Problème : "MySQL shutdown unexpectedly"

Ce guide vous aidera à résoudre le problème de démarrage MySQL dans XAMPP.

## Solutions par ordre de priorité

### Solution 1 : Vérifier si le port 3306 est déjà utilisé

Le port 3306 est peut-être déjà utilisé par un autre service MySQL.

#### Étape 1 : Vérifier les processus utilisant le port 3306

Ouvrez PowerShell en tant qu'administrateur et exécutez :

```powershell
netstat -ano | findstr :3306
```

Si vous voyez une entrée, notez le PID (dernier nombre).

#### Étape 2 : Arrêter le service MySQL Windows (si présent)

1. Appuyez sur `Win + R`, tapez `services.msc` et appuyez sur Entrée
2. Cherchez "MySQL" dans la liste des services
3. Si vous trouvez un service MySQL (pas XAMPP), faites un clic droit → Arrêter
4. Optionnel : Désactiver le démarrage automatique (Propriétés → Type de démarrage : Désactivé)

#### Étape 3 : Tuer le processus (si nécessaire)

Si un processus utilise toujours le port 3306 :

```powershell
taskkill /PID <PID_NUMBER> /F
```

Remplacez `<PID_NUMBER>` par le numéro trouvé à l'étape 1.

### Solution 2 : Vérifier les logs MySQL

1. Dans XAMPP Control Panel, cliquez sur **Logs** à côté de MySQL
2. Ou ouvrez manuellement : `C:\xampp\mysql\data\mysql_error.log`
3. Cherchez les erreurs récentes

Erreurs courantes :
- `InnoDB: Cannot open file` → Fichiers de données corrompus
- `Port already in use` → Port 3306 occupé
- `Access denied` → Problème de permissions

### Solution 3 : Réparer les fichiers de données MySQL

**⚠️ ATTENTION : Cette solution supprime toutes les données MySQL existantes dans XAMPP !**

1. **Arrêtez MySQL** dans XAMPP (si en cours)
2. **Sauvegardez vos données** (si vous en avez) :
   - Copiez le dossier `C:\xampp\mysql\data\` vers un autre emplacement
3. **Supprimez les fichiers de données** :
   - Supprimez le contenu de `C:\xampp\mysql\data\`
   - **GARDEZ** les dossiers `mysql` et `performance_schema` (ne supprimez que leur contenu)
4. **Réinitialisez MySQL** :
   - Ouvrez PowerShell en tant qu'administrateur
   - Naviguez vers XAMPP : `cd C:\xampp\mysql\bin`
   - Exécutez : `.\mysqld --initialize-insecure --console`
5. **Redémarrez MySQL** dans XAMPP

### Solution 4 : Changer le port MySQL (si 3306 est bloqué)

Si le port 3306 est définitivement occupé :

1. Ouvrez : `C:\xampp\mysql\bin\my.ini`
2. Cherchez la ligne : `port=3306`
3. Changez en : `port=3307` (ou un autre port libre)
4. **Important** : Mettez à jour `DatabaseManager.java` avec le nouveau port :
   ```java
   private static final String DB_PORT = "3307"; // Au lieu de "3306"
   ```
5. Redémarrez MySQL dans XAMPP

### Solution 5 : Vérifier les permissions

1. Faites un clic droit sur `C:\xampp\mysql\data\`
2. Propriétés → Sécurité
3. Assurez-vous que votre utilisateur a les droits "Contrôle total"
4. Si nécessaire, cliquez sur "Modifier" et accordez les permissions

### Solution 6 : Réinstaller MySQL dans XAMPP

Si rien ne fonctionne :

1. **Sauvegardez vos données** (si importantes)
2. **Désinstallez XAMPP** (ou seulement MySQL)
3. **Réinstallez XAMPP**
4. **Réinitialisez MySQL** (Solution 3)

## Vérification rapide

Après avoir appliqué une solution, vérifiez :

1. **MySQL démarre** dans XAMPP (icône verte)
2. **Test de connexion** :
   ```powershell
   cd C:\xampp\mysql\bin
   .\mysql.exe -u root
   ```
   Si vous voyez `mysql>`, c'est bon !

3. **Test depuis l'application Java** :
   - Lancez votre application
   - Vérifiez les logs pour "Connexion à la base de données MySQL établie avec succès"

## Commandes utiles

### Vérifier les services MySQL
```powershell
Get-Service | Where-Object {$_.DisplayName -like "*MySQL*"}
```

### Vérifier les processus MySQL
```powershell
Get-Process | Where-Object {$_.ProcessName -like "*mysql*"}
```

### Tester la connexion MySQL
```powershell
cd C:\xampp\mysql\bin
.\mysql.exe -u root -e "SELECT 1;"
```

## Solution recommandée pour votre cas

Basé sur l'erreur, je recommande dans cet ordre :

1. **Vérifier le port 3306** (Solution 1) - Le plus probable
2. **Vérifier les logs** (Solution 2) - Pour identifier la cause exacte
3. **Réparer les données** (Solution 3) - Si les fichiers sont corrompus

## Après correction

Une fois MySQL démarré :

1. Créez la base de données (si nécessaire) :
   ```sql
   CREATE DATABASE IF NOT EXISTS gym_management 
       CHARACTER SET utf8mb4 
       COLLATE utf8mb4_unicode_ci;
   ```

2. Lancez votre application Java
3. La base de données sera initialisée automatiquement

## Support supplémentaire

Si le problème persiste :
- Consultez les logs MySQL : `C:\xampp\mysql\data\mysql_error.log`
- Vérifiez l'Event Viewer Windows : `Win + R` → `eventvwr.msc`
- Forum XAMPP : https://community.apachefriends.org/

