package com.commerce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests unitaires — Modèles (Commande, LigneCommande, Produit)")
class ModelTest {

    private Produit produit1;
    private Produit produit2;

    @BeforeEach
    void setUp() {
        produit1 = new Produit();
        produit1.setId(1);
        produit1.setNom("Ecran 27 pouces");
        produit1.setPrix(new BigDecimal("2500.00"));
        produit1.setStock(5);

        produit2 = new Produit();
        produit2.setId(2);
        produit2.setNom("Clavier Sans-fil");
        produit2.setPrix(new BigDecimal("450.00"));
        produit2.setStock(10);
    }

    // ── Commande.calculerTotal() ──────────────────────────────────────────────

    @Test
    @DisplayName("Commande.calculerTotal() calcule correctement avec plusieurs lignes")
    void commande_calculerTotal_avecPlusieursLignes() {
        Commande commande = new Commande();

        LigneCommande ligne1 = new LigneCommande(produit1, 2); // 2500 * 2 = 5000
        LigneCommande ligne2 = new LigneCommande(produit2, 3); // 450 * 3 = 1350

        commande.addLigne(ligne1);
        commande.addLigne(ligne2);

        // Total attendu = 6350.00
        assertThat(commande.getTotal()).isEqualByComparingTo(new BigDecimal("6350.00"));
    }

    @Test
    @DisplayName("Commande.calculerTotal() retourne 0 si aucune ligne")
    void commande_calculerTotal_sansLigne_retourneZero() {
        Commande commande = new Commande();

        assertThat(commande.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Commande.addLigne() met à jour le total automatiquement")
    void commande_addLigne_metsAJourTotalAuto() {
        Commande commande = new Commande();
        assertThat(commande.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);

        commande.addLigne(new LigneCommande(produit1, 1)); // 2500
        assertThat(commande.getTotal()).isEqualByComparingTo(new BigDecimal("2500.00"));

        commande.addLigne(new LigneCommande(produit2, 2)); // + 900
        assertThat(commande.getTotal()).isEqualByComparingTo(new BigDecimal("3400.00"));
    }

    @Test
    @DisplayName("Commande.isDefinitive() retourne true seulement pour VALIDEE")
    void commande_isDefinitive_retourneTrueSeulementPourValidee() {
        Commande commande = new Commande();

        commande.setStatut(Commande.Statut.EN_COURS);
        assertThat(commande.isDefinitive()).isFalse();

        commande.setStatut(Commande.Statut.ANNULEE);
        assertThat(commande.isDefinitive()).isFalse();

        commande.setStatut(Commande.Statut.VALIDEE);
        assertThat(commande.isDefinitive()).isTrue();
    }

    // ── LigneCommande ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("LigneCommande(Produit, quantite) fixe le prix du produit au moment de la création")
    void ligneCommande_constructeur_fixeLePrixProduit() {
        LigneCommande ligne = new LigneCommande(produit1, 3);

        assertThat(ligne.getPrixUnitaire()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(ligne.getQuantite()).isEqualTo(3);
        assertThat(ligne.getProduit()).isEqualTo(produit1);
    }

    @Test
    @DisplayName("LigneCommande.getSousTotal() calcule prix * quantité")
    void ligneCommande_getSousTotal_calculePrixFoisQuantite() {
        LigneCommande ligne = new LigneCommande(produit2, 4); // 450 * 4

        assertThat(ligne.getSousTotal()).isEqualByComparingTo(new BigDecimal("1800.00"));
    }

    @Test
    @DisplayName("LigneCommande conserve le prix original même si le prix produit change")
    void ligneCommande_conservePrixOriginal_memeAfterChangeProduit() {
        LigneCommande ligne = new LigneCommande(produit1, 1);
        BigDecimal prixOriginal = ligne.getPrixUnitaire();

        // Simuler une modification de prix
        produit1.setPrix(new BigDecimal("9999.00"));

        // Le prix dans la ligne ne doit pas changer
        assertThat(ligne.getPrixUnitaire()).isEqualByComparingTo(prixOriginal);
    }

    // ── Produit.isEnStock() ───────────────────────────────────────────────────

    @Test
    @DisplayName("Produit.isEnStock() retourne true si stock > 0 et non supprimé")
    void produit_isEnStock_retourneTrueSiStockPositif() {
        assertThat(produit1.isEnStock()).isTrue();
    }

    @Test
    @DisplayName("Produit.isEnStock() retourne false si stock = 0")
    void produit_isEnStock_retourneFalseSiStockZero() {
        produit1.setStock(0);
        assertThat(produit1.isEnStock()).isFalse();
    }

    @Test
    @DisplayName("Produit.isEnStock() retourne false si deleted = true même avec stock > 0")
    void produit_isEnStock_retourneFalseSiDeleted() {
        produit1.setDeleted(true);
        assertThat(produit1.isEnStock()).isFalse();
    }

    // ── Utilisateur ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Utilisateur.getNomComplet() retourne prénom + nom")
    void utilisateur_getNomComplet_retournePrenomPlusNom() {
        Utilisateur u = new Utilisateur();
        u.setPrenom("Youssef");
        u.setNom("Alami");

        assertThat(u.getNomComplet()).isEqualTo("Youssef Alami");
    }

    @Test
    @DisplayName("Utilisateur.isAdmin() retourne true seulement pour le rôle ADMIN")
    void utilisateur_isAdmin_retourneTrueSeulementPourAdmin() {
        Utilisateur u = new Utilisateur();
        u.setRole(Utilisateur.Role.CLIENT);
        assertThat(u.isAdmin()).isFalse();

        u.setRole(Utilisateur.Role.ADMIN);
        assertThat(u.isAdmin()).isTrue();
    }
}