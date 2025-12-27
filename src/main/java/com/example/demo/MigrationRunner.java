package com.example.demo;

import com.example.demo.utils.DataMigrationTool;

/**
 * Point d'entrée pour exécuter la migration de la base de données
 * SQLite vers MySQL
 */
public class MigrationRunner {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Migration SQLite -> MySQL");
        System.out.println("========================================");
        System.out.println();
        
        // Vérifier si l'option --force est passée
        boolean force = args.length > 0 && (args[0].equals("--force") || args[0].equals("-f"));
        
        // Force la migration pour vider automatiquement les tables MySQL
        force = true; // TODO: Retirer cette ligne après migration
        
        if (force) {
            System.out.println("⚠️  Mode FORCE activé : les tables MySQL seront vidées avant la migration");
            System.out.println();
        }
        
        boolean success = DataMigrationTool.migrate(force);
        
        System.out.println();
        if (success) {
            System.out.println("✓ Migration terminée avec succès !");
            System.out.println("Vous pouvez maintenant utiliser MySQL comme base de données.");
        } else {
            System.out.println("✗ La migration a échoué ou a été ignorée.");
            System.out.println();
            System.out.println("💡 ASTUCE : Pour forcer la migration et vider les tables MySQL automatiquement,");
            System.out.println("   utilisez : MigrationRunner --force");
            System.out.println();
            System.out.println("Vérifiez les logs ci-dessus pour plus de détails.");
            System.exit(1);
        }
    }
}

