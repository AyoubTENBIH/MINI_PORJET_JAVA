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
        try {
            PackDAO packDAO = new PackDAO();
            AdherentDAO adherentDAO = new AdherentDAO();
            PaiementDAO paiementDAO = new PaiementDAO();
            
            // Vérifier si des données existent déjà
            if (!packDAO.findAll().isEmpty()) {
                System.out.println("Des données existent déjà. Génération annulée.");
                return;
            }
            
            System.out.println("Génération des données de test...");
            
            // Récupérer les packs existants
            java.util.List<Pack> packs = packDAO.findAll();
            if (packs.isEmpty()) {
                System.out.println("Aucun pack trouvé. Veuillez d'abord créer des packs.");
                return;
            }
            
            Random random = new Random();
            
            // Générer 30-40 adhérents
            int nombreAdherents = 35;
            for (int i = 0; i < nombreAdherents; i++) {
                Adherent adherent = new Adherent();
                adherent.setCin("AB" + String.format("%06d", 100000 + i));
                adherent.setNom(NOMS[random.nextInt(NOMS.length)]);
                adherent.setPrenom(PRENOMS[random.nextInt(PRENOMS.length)]);
                adherent.setDateNaissance(LocalDate.now().minusYears(20 + random.nextInt(40)));
                adherent.setTelephone(TELEPHONES[random.nextInt(TELEPHONES.length)]);
                adherent.setEmail(adherent.getPrenom().toLowerCase() + "." + adherent.getNom().toLowerCase() + "@email.com");
                adherent.setAdresse("Adresse " + (i + 1) + ", Casablanca");
                adherent.setPoids(60.0 + random.nextDouble() * 30);
                adherent.setTaille(160.0 + random.nextDouble() * 30);
                adherent.setObjectifs(random.nextBoolean() ? "Perte de poids" : "Prise de masse");
                
                // Assigner un pack aléatoire
                Pack pack = packs.get(random.nextInt(packs.size()));
                adherent.setPack(pack);
                
                // Dates d'abonnement variées
                int joursAvecAbonnement = random.nextInt(365);
                adherent.setDateDebut(LocalDate.now().minusDays(joursAvecAbonnement));
                adherent.setDateFin(adherent.getDateDebut().plusMonths(pack.getDuree()));
                
                // Certains adhérents ont des abonnements expirés
                if (random.nextDouble() < 0.3) {
                    adherent.setDateFin(LocalDate.now().minusDays(random.nextInt(60)));
                }
                
                adherent.setActif(true);
                adherent.setDateInscription(adherent.getDateDebut());
                
                // Créer l'adhérent
                adherentDAO.create(adherent);
                
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
            }
            
            System.out.println("Génération terminée : " + nombreAdherents + " adhérents créés avec leurs paiements.");
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la génération des données de test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}




