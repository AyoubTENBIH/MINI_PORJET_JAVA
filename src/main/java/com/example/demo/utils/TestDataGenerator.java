package com.example.demo.utils;

import com.example.demo.dao.AdherentDAO;
import com.example.demo.dao.PackDAO;
import com.example.demo.dao.PaiementDAO;
import com.example.demo.models.Adherent;
import com.example.demo.models.Pack;
import com.example.demo.models.Paiement;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Random;

/**
 * Générateur de données de test pour l'application
 */
public class TestDataGenerator {
    private static final String[] PRENOMS = {
        "Ahmed", "Fatima", "Mohamed", "Aicha", "Hassan", "Khadija", "Omar", "Sanae",
        "Youssef", "Laila", "Ali", "Nadia", "Karim", "Souad", "Mehdi", "Salma",
        "Amine", "Imane", "Bilal", "Zineb", "Rachid", "Houda", "Said", "Nabila"
    };
    
    private static final String[] NOMS = {
        "Alaoui", "Benali", "Chraibi", "Dahbi", "El Amrani", "Fassi", "Ghazi", "Haddad",
        "Idrissi", "Jazouli", "Kadiri", "Lamrani", "Mansouri", "Naciri", "Ouali", "Qadiri",
        "Rahmani", "Saadi", "Tazi", "Zahiri", "Bennani", "Cherkaoui", "Dari", "El Fassi"
    };
    
    private static final String[] TELEPHONES = {
        "0612345678", "0623456789", "0634567890", "0645678901", "0656789012",
        "0667890123", "0678901234", "0689012345", "0690123456", "0601234567"
    };

    /**
     * Génère des données de test complètes
     */
    public static void generateTestData() {
        generateTestData(35, false);
    }
    
    /**
     * Génère des données de test avec nombre personnalisé
     * @param nombreAdherents Nombre d'adhérents à générer
     * @param forcer Si true, génère même si des données existent déjà
     */
    public static void generateTestData(int nombreAdherents, boolean forcer) {
        try {
            PackDAO packDAO = new PackDAO();
            AdherentDAO adherentDAO = new AdherentDAO();
            PaiementDAO paiementDAO = new PaiementDAO();
            
            // Vérifier si des données existent déjà
            int nombreExistant = adherentDAO.findAll().size();
            System.out.println("Nombre d'adhérents existants avant génération: " + nombreExistant);
            
            if (!forcer && nombreExistant > 0) {
                System.out.println("Des adhérents existent déjà (" + nombreExistant + "). Utilisez forcer=true pour ajouter plus.");
                return;
            }
            
            System.out.println("Génération de " + nombreAdherents + " adhérents de test...");
            
            // Récupérer les packs existants
            java.util.List<Pack> packs = packDAO.findAll();
            System.out.println("Nombre de packs trouvés: " + packs.size());
            if (packs.isEmpty()) {
                System.out.println("ERREUR: Aucun pack trouvé. Veuillez d'abord créer des packs.");
                return;
            }
            
            Random random = new Random();
            
            // Générer les adhérents
            int baseIndex = forcer ? nombreExistant : 0;
            int adherentsCrees = 0;
            System.out.println("Index de base pour les nouveaux adhérents: " + baseIndex);
            
            for (int i = 0; i < nombreAdherents; i++) {
                Adherent adherent = new Adherent();
                adherent.setCin("AB" + String.format("%06d", 100000 + baseIndex + i));
                adherent.setNom(NOMS[random.nextInt(NOMS.length)]);
                adherent.setPrenom(PRENOMS[random.nextInt(PRENOMS.length)]);
                adherent.setDateNaissance(LocalDate.now().minusYears(20 + random.nextInt(40)));
                adherent.setTelephone(TELEPHONES[random.nextInt(TELEPHONES.length)]);
                adherent.setEmail(adherent.getPrenom().toLowerCase() + "." + adherent.getNom().toLowerCase() + (baseIndex + i) + "@email.com");
                adherent.setAdresse("Adresse " + (baseIndex + i + 1) + ", Casablanca");
                adherent.setPoids(60.0 + random.nextDouble() * 30);
                adherent.setTaille(160.0 + random.nextDouble() * 30);
                adherent.setObjectifs(random.nextBoolean() ? "Perte de poids" : "Prise de masse");
                
                // Assigner un pack aléatoire
                Pack pack = packs.get(random.nextInt(packs.size()));
                adherent.setPack(pack);
                
                // Dates d'abonnement variées pour avoir des statistiques intéressantes
                int joursAvecAbonnement = random.nextInt(365);
                adherent.setDateDebut(LocalDate.now().minusDays(joursAvecAbonnement));
                adherent.setDateFin(adherent.getDateDebut().plusMonths(pack.getDuree()));
                
                // Certains adhérents ont des abonnements expirés (30%)
                if (random.nextDouble() < 0.3) {
                    adherent.setDateFin(LocalDate.now().minusDays(random.nextInt(60)));
                    adherent.setActif(false);
                } else {
                    adherent.setActif(true);
                }
                
                // Certains adhérents expirent bientôt (15%)
                if (random.nextDouble() < 0.15 && adherent.getActif()) {
                    adherent.setDateFin(LocalDate.now().plusDays(random.nextInt(7)));
                }
                
                adherent.setDateInscription(adherent.getDateDebut());
                
                // Créer l'adhérent
                adherentDAO.create(adherent);
                adherentsCrees++;
                
                // Créer un paiement pour cet adhérent
                Paiement paiement = new Paiement();
                paiement.setAdherent(adherent);
                paiement.setPack(pack);
                paiement.setMontant(pack.getPrix());
                paiement.setDatePaiement(adherent.getDateDebut().atStartOfDay());
                paiement.setMethodePaiement(Paiement.MethodePaiement.values()[random.nextInt(Paiement.MethodePaiement.values().length)]);
                paiement.setDateDebutAbonnement(adherent.getDateDebut());
                paiement.setDateFinAbonnement(adherent.getDateFin());
                
                paiementDAO.create(paiement);
                
                // Créer des paiements supplémentaires pour certains adhérents (historique)
                if (random.nextDouble() < 0.4 && adherent.getDateDebut().isBefore(LocalDate.now().minusMonths(2))) {
                    // Paiement précédent
                    Paiement paiementPrecedent = new Paiement();
                    paiementPrecedent.setAdherent(adherent);
                    paiementPrecedent.setPack(pack);
                    paiementPrecedent.setMontant(pack.getPrix());
                    paiementPrecedent.setDatePaiement(adherent.getDateDebut().minusMonths(pack.getDuree()).atStartOfDay());
                    paiementPrecedent.setMethodePaiement(Paiement.MethodePaiement.values()[random.nextInt(Paiement.MethodePaiement.values().length)]);
                    paiementPrecedent.setDateDebutAbonnement(adherent.getDateDebut().minusMonths(pack.getDuree()));
                    paiementPrecedent.setDateFinAbonnement(adherent.getDateDebut());
                    paiementDAO.create(paiementPrecedent);
                }
            }
            
            String successMsg = "✓ Génération terminée : " + adherentsCrees + " adhérents créés avec leurs paiements.";
            System.out.println(successMsg);
            System.err.println(successMsg);
            
        } catch (SQLException e) {
            String errorMsg = "ERREUR lors de la génération des données de test: " + e.getMessage();
            System.err.println(errorMsg);
            System.out.println(errorMsg);
            e.printStackTrace();
        } catch (Exception e) {
            String errorMsg = "ERREUR inattendue lors de la génération: " + e.getMessage();
            System.err.println(errorMsg);
            System.out.println(errorMsg);
            e.printStackTrace();
        }
    }
    
    /**
     * Génère rapidement 50 adhérents pour les statistiques
     */
    public static void generateForStatistics() {
        generateTestData(50, true);
    }
}




