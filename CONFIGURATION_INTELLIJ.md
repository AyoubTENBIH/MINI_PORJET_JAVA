# 🔧 Configuration IntelliJ IDEA

## Configuration du Point d'Entrée

### Option 1 : Utiliser HelloApplication (Recommandé)

1. **Ouvrir le projet dans IntelliJ IDEA**
   - File → Open → Sélectionner le dossier du projet

2. **Configurer le SDK Java**
   - File → Project Structure → Project
   - Sélectionner Java 21 ou supérieur

3. **Configurer JavaFX**
   - File → Project Structure → Libraries
   - Ajouter les modules JavaFX si nécessaire
   - Ou utiliser les dépendances Maven (déjà configurées)

4. **Créer une configuration d'exécution**
   - Run → Edit Configurations...
   - Cliquer sur "+" → Application
   - Configurer :
     - **Name**: `HelloApplication`
     - **Main class**: `com.example.demo.HelloApplication`
     - **Module**: `demo`
     - **VM options**: (optionnel, si JavaFX n'est pas dans le classpath)
       ```
       --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml
       ```

5. **Exécuter**
   - Run → Run 'HelloApplication'
   - Ou cliquer sur le bouton Run vert à côté de la classe

### Option 2 : Utiliser Maven (Alternative)

1. **Créer une configuration Maven**
   - Run → Edit Configurations...
   - Cliquer sur "+" → Maven
   - Configurer :
     - **Name**: `Run JavaFX App`
     - **Command line**: `javafx:run`
     - **Working directory**: `$PROJECT_DIR$`

2. **Exécuter**
   - Run → Run 'Run JavaFX App'

## Configuration des Modules Java

Si vous utilisez Java Modules (module-info.java) :

1. **Vérifier la configuration du module**
   - File → Project Structure → Modules
   - S'assurer que `com.example.demo` est configuré correctement

2. **Vérifier les dépendances**
   - File → Project Structure → Modules → Dependencies
   - Toutes les dépendances JavaFX doivent être présentes

## Résolution des Problèmes Courants

### Erreur : "JavaFX runtime components are missing"

**Solution 1** : Utiliser Maven (recommandé)
```bash
./mvnw clean compile javafx:run
```

**Solution 2** : Ajouter JavaFX au classpath
- Télécharger JavaFX SDK depuis https://openjfx.io/
- File → Project Structure → Libraries → "+" → Java
- Ajouter le dossier `lib` de JavaFX

**Solution 3** : Configurer les VM options
```
--module-path /chemin/vers/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```

### Erreur : "Module not found"

**Solution** : Vérifier que `module-info.java` est correct et que toutes les dépendances sont déclarées.

### Erreur : "ClassNotFoundException"

**Solution** : 
1. File → Invalidate Caches / Restart
2. Build → Rebuild Project
3. Vérifier que toutes les dépendances Maven sont téléchargées

### Erreur de base de données

**Solution** : 
- Vérifier que le répertoire `src/main/resources/database/` existe
- L'application le crée automatiquement au premier lancement
- Si problème, créer manuellement le dossier

## Configuration Recommandée pour IntelliJ

### Paramètres du Projet
- **SDK**: Java 21
- **Language level**: 21
- **Build tool**: Maven
- **Project format**: .idea (directory based)

### Plugins Recommandés
- Maven (inclus)
- JavaFX (optionnel, pour la visualisation FXML)

### Paramètres de Compilation
- **Build automatically**: Activé
- **Compile output path**: `target/classes`

## Exécution Rapide

### Méthode 1 : Bouton Run
1. Ouvrir `HelloApplication.java`
2. Cliquer sur le bouton Run vert à côté de `public static void main`
3. Sélectionner "Run 'HelloApplication'"

### Méthode 2 : Raccourci Clavier
1. Ouvrir `HelloApplication.java`
2. Appuyer sur `Shift + F10` (Windows/Linux) ou `Ctrl + R` (Mac)

### Méthode 3 : Terminal Intégré
```bash
./mvnw javafx:run
```

## Identifiants de Connexion

Une fois l'application lancée :
- **Username**: `admin`
- **Password**: `admin`

## Vérification de la Configuration

Pour vérifier que tout est bien configuré :

1. **Compiler le projet**
   ```bash
   ./mvnw clean compile
   ```
   Doit se terminer sans erreur

2. **Vérifier les dépendances**
   - File → Project Structure → Modules → Dependencies
   - Toutes les dépendances doivent être résolues (pas de rouge)

3. **Tester l'exécution**
   - Run → Run 'HelloApplication'
   - L'application doit démarrer et afficher la fenêtre de connexion

## Support

Si vous rencontrez des problèmes :
1. Vérifier les logs dans la console IntelliJ
2. Vérifier que Java 21+ est installé : `java -version`
3. Vérifier que Maven fonctionne : `./mvnw --version`
4. Nettoyer et reconstruire : `./mvnw clean install`

---

**Note** : Si vous préférez utiliser `Main.java` au lieu de `HelloApplication.java`, il suffit de changer le point d'entrée dans la configuration d'exécution.




