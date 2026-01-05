# 📦 Guide Pratique de Migration - SQLite vers MySQL

Ce guide vous explique **pas à pas** comment migrer vos données de SQLite vers MySQL.

---

## 🎯 Vue d'ensemble

Vous avez **3 scénarios** possibles :

1. **Nouveau projet** : Démarrage avec MySQL (données par défaut)
2. **Migration de données** : Vous avez déjà des données SQLite à migrer
3. **Migration après corrections** : Après avoir corrigé les problèmes de transactions

---

## ✅ Prérequis

Avant de commencer, vérifiez que :

- [x] ✅ **XAMPP est installé** et MySQL est démarré
- [x] ✅ **Java 21** est installé
- [x] ✅ **Maven** est configuré
- [x] ✅ Les **corrections de transactions** ont été appliquées (commits/rollbacks)

### Vérifier que MySQL est démarré

1. Ouvrez **XAMPP Control Panel**
2. Cliquez sur **Start** pour MySQL
3. Vérifiez que l'icône est **verte** ✅

---

## 🚀 Scénario 1 : Démarrage avec MySQL (Nouveau projet)

Si vous **n'avez pas encore de données** SQLite, MySQL sera automatiquement initialisé.

### Étape 1 : Démarrer MySQL

- Démarrez MySQL dans XAMPP Control Panel

### Étape 2 : Lancer l'application

L'application créera automatiquement :
- ✅ La base de données `gym_management`
- ✅ Toutes les tables nécessaires
- ✅ Des données de test par défaut

**C'est tout !** Pas besoin de migration.

---

## 📥 Scénario 2 : Migration de Données SQLite → MySQL

Si vous avez **déjà des données** dans SQLite que vous voulez migrer vers MySQL.

### Étape 1 : Préparer l'environnement

1. **Vérifier le fichier SQLite**
   ```
   Vérifiez que le fichier existe :
   src/main/resources/database/gym_management.db
   ```

2. **Démarrer MySQL**
   - Ouvrez XAMPP Control Panel
   - Cliquez sur **Start** pour MySQL

3. **Vérifier que MySQL est vide**
   - Optionnel : Ouvrez phpMyAdmin (http://localhost/phpmyadmin)
   - Si la base `gym_management` existe déjà avec des données, videz-la ou supprimez-la

### Étape 2 : Exécuter la Migration

Vous avez **3 méthodes** pour migrer :

#### **Méthode A : Script Windows (Recommandé sur Windows)** ⚡

```bash
# Double-cliquez sur le fichier :
migrate.bat
```

Ou depuis PowerShell/CMD :
```bash
.\migrate.bat
```

#### **Méthode B : Script Linux/Mac** ⚡

```bash
chmod +x migrate.sh
./migrate.sh
```

#### **Méthode C : Commande Maven Manuelle** 🔧

```bash
mvn compile exec:java -Dexec.mainClass="com.example.demo.MigrationRunner"
```

#### **Méthode D : Depuis IntelliJ IDEA** 💡

1. Ouvrez le fichier `MigrationRunner.java`
2. Clic droit sur la classe
3. Sélectionnez **Run 'MigrationRunner.main()'**

### Étape 3 : Vérifier la Migration

#### Vérification via les logs

Après l'exécution, vous devriez voir :

```
========================================
  Migration SQLite -> MySQL
========================================

INFO: Connexion SQLite établie
INFO: Connexion MySQL établie
INFO: Table 'utilisateurs': X lignes migrées
INFO: Table 'packs': X lignes migrées
INFO: Table 'adherents': X lignes migrées
...
INFO: Migration terminée avec succès: X lignes au total
✓ Migration terminée avec succès !
```

#### Vérification via phpMyAdmin

1. Ouvrez http://localhost/phpmyadmin
2. Sélectionnez la base `gym_management`
3. Vérifiez que les tables contiennent des données :
   - `adherents` : devrait contenir vos adhérents
   - `packs` : devrait contenir vos packs
   - `paiements` : devrait contenir vos paiements
   - etc.

#### Vérification via MySQL CLI

```sql
USE gym_management;

-- Compter les enregistrements
SELECT COUNT(*) FROM adherents;
SELECT COUNT(*) FROM packs;
SELECT COUNT(*) FROM paiements;
SELECT COUNT(*) FROM utilisateurs;
```

---

## 🔄 Scénario 3 : Migration après Corrections des Transactions

Après avoir corrigé les problèmes de transactions (commits/rollbacks), vous devriez **retester la migration** pour vous assurer que tout fonctionne.

### Étape 1 : Nettoyer MySQL (si nécessaire)

Si vous avez déjà testé l'application, MySQL peut contenir des données incomplètes (sans commits). Nettoyez la base :

#### Option A : Via phpMyAdmin

1. Ouvrez http://localhost/phpmyadmin
2. Sélectionnez la base `gym_management`
3. Clic sur **Opérations** → **Supprimer la base de données**
4. Relancez l'application pour recréer la base

#### Option B : Via MySQL CLI

```sql
DROP DATABASE IF EXISTS gym_management;
CREATE DATABASE gym_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Étape 2 : Relancer la Migration

Exécutez à nouveau la migration (voir Scénario 2, Étape 2).

### Étape 3 : Tester l'Application

1. Lancez l'application
2. Créez un nouvel adhérent
3. Vérifiez qu'il est bien sauvegardé (via phpMyAdmin ou l'interface)
4. ✅ Les données doivent maintenant persister correctement !

---

## ⚠️ Dépannage

### Problème : "Le fichier SQLite n'existe pas"

**Solution :**
- C'est normal si vous démarrez un nouveau projet
- L'application créera automatiquement MySQL avec des données par défaut
- Pas besoin de migration

### Problème : "La base MySQL contient déjà des données"

**Message :**
```
WARNING: La base MySQL contient déjà des données. La migration sera ignorée.
```

**Solution :**

1. **Vider les tables MySQL** (méthode recommandée) :
   ```sql
   USE gym_management;
   TRUNCATE TABLE utilisateurs;
   TRUNCATE TABLE packs;
   TRUNCATE TABLE adherents;
   TRUNCATE TABLE paiements;
   -- Répétez pour toutes les tables
   ```

2. **Ou supprimer et recréer la base** :
   ```sql
   DROP DATABASE gym_management;
   CREATE DATABASE gym_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **Relancer la migration**

### Problème : "Connection refused" ou "Access denied"

**Solutions :**

1. **Vérifier que MySQL est démarré**
   - XAMPP Control Panel → MySQL doit être vert

2. **Vérifier les identifiants**
   - Par défaut : `root` / mot de passe vide
   - Si vous avez changé le mot de passe, modifiez dans :
     - `DataMigrationTool.java` (lignes 29-30)
     - `DatabaseManager.java` (lignes 23-24)

3. **Vérifier le port**
   - Par défaut : 3306
   - Si différent, modifiez dans les fichiers ci-dessus

### Problème : "Driver MySQL non trouvé"

**Solution :**
```bash
mvn clean install
```

Vérifiez que `pom.xml` contient :
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

### Problème : Erreurs de contraintes de clés étrangères

**Cause :** L'ordre de migration n'est pas respecté.

**Solution :**
L'outil de migration gère automatiquement l'ordre. Si problème :
1. Vérifiez les logs pour voir quelle table pose problème
2. Videz toutes les tables dans l'ordre inverse (dépendances d'abord)
3. Relancez la migration

---

## 📊 Ordre de Migration

L'outil migre automatiquement dans cet ordre (pour respecter les dépendances) :

1. `utilisateurs` (pas de dépendances)
2. `packs` (pas de dépendances)
3. `objectifs` (pas de dépendances)
4. `adherents` (dépend de `packs`)
5. `paiements` (dépend de `adherents` et `packs`)
6. `presences` (dépend de `adherents`)
7. `cours_collectifs` (dépend de `utilisateurs`)
8. `reservations_cours` (dépend de `cours_collectifs` et `adherents`)
9. `equipements` (pas de dépendances)
10. `notifications` (dépend de `utilisateurs`)
11. `activities` (dépend de `utilisateurs`)
12. `user_preferences` (dépend de `utilisateurs`)
13. `favoris` (dépend de `utilisateurs`)

---

## ✅ Checklist Post-Migration

Après la migration, vérifiez :

- [ ] ✅ Toutes les tables ont été créées
- [ ] ✅ Les données ont été migrées (comptez les lignes)
- [ ] ✅ Les relations (clés étrangères) sont intactes
- [ ] ✅ L'application se connecte à MySQL
- [ ] ✅ Vous pouvez créer/modifier/supprimer des données
- [ ] ✅ Les nouvelles données sont bien persistées (testez avec un commit)

---

## 🔍 Vérification Rapide

### Script SQL de Vérification

Exécutez dans phpMyAdmin ou MySQL CLI :

```sql
USE gym_management;

-- Vérifier les tables
SHOW TABLES;

-- Compter les enregistrements
SELECT 
    'utilisateurs' as table_name, COUNT(*) as count FROM utilisateurs
UNION ALL
SELECT 'packs', COUNT(*) FROM packs
UNION ALL
SELECT 'adherents', COUNT(*) FROM adherents
UNION ALL
SELECT 'paiements', COUNT(*) FROM paiements
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL
SELECT 'activities', COUNT(*) FROM activities;

-- Vérifier les relations
SELECT 
    a.id, a.nom, a.prenom, p.nom as pack_nom
FROM adherents a
LEFT JOIN packs p ON a.pack_id = p.id
LIMIT 5;
```

---

## 📝 Notes Importantes

1. **Sauvegarde** : Toujours sauvegarder vos données avant migration
2. **Transaction** : La migration utilise des transactions - en cas d'erreur, tout est annulé (rollback)
3. **Idempotence** : La migration ne s'exécute pas si MySQL contient déjà des données (protection)
4. **Données SQLite** : Le fichier SQLite n'est **pas modifié** (lecture seule)
5. **Performance** : MySQL est généralement plus performant pour les applications multi-utilisateurs

---

## 🆘 Support

Si vous rencontrez des problèmes :

1. Consultez les logs détaillés dans la console
2. Vérifiez la documentation MySQL
3. Vérifiez que XAMPP est correctement configuré
4. Vérifiez les paramètres de connexion dans `DataMigrationTool.java` et `DatabaseManager.java`

---

**Date de création :** 2024  
**Dernière mise à jour :** Après corrections des transactions DAO





