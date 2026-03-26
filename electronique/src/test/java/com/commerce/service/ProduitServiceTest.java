package com.commerce.service;

import com.commerce.model.Categorie;
import com.commerce.model.Produit;
import com.commerce.repository.ProduitRepository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires — ProduitService")
class ProduitServiceTest {

    @Mock
    private ProduitRepository produitRepository;

    @InjectMocks
    private ProduitService produitService;

    private Produit produit;
    private Categorie categorie;

    @BeforeEach
    void setUp() {
        categorie = new Categorie();
        categorie.setId(1);
        categorie.setNom("Electronique");

        produit = new Produit();
        produit.setId(1);
        produit.setNom("Laptop Dell");
        produit.setPrix(new BigDecimal("8999.99"));
        produit.setStock(10);
        produit.setDeleted(false);
        produit.setCategorie(categorie);
    }

    // ── listerActifs ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-P-01 : listerActifs() retourne uniquement les produits non supprimés")
    void listerActifs_retourneProduitsActifs() {
        when(produitRepository.findByDeletedFalse()).thenReturn(List.of(produit));

        List<Produit> result = produitService.listerActifs();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("Laptop Dell");
        verify(produitRepository).findByDeletedFalse();
    }

    @Test
    @DisplayName("UT-P-01b : listerActifs() retourne liste vide si aucun produit actif")
    void listerActifs_retourneListeVide_siAucunActif() {
        when(produitRepository.findByDeletedFalse()).thenReturn(List.of());

        List<Produit> result = produitService.listerActifs();

        assertThat(result).isEmpty();
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-P-02 : findById() retourne le produit si l'ID existe")
    void findById_retourneProduit_siIdExistant() {
        when(produitRepository.findById(1)).thenReturn(Optional.of(produit));

        Produit result = produitService.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getNom()).isEqualTo("Laptop Dell");
    }

    @Test
    @DisplayName("UT-P-03 : findById() lève une exception si l'ID est inexistant")
    void findById_leveException_siIdInexistant() {
        when(produitRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produitService.findById(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Produit introuvable");
    }

    // ── supprimerLogique ──────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-P-04 : supprimerLogique() met deleted=true sans supprimer de la base")
    void supprimerLogique_setDeletedTrue() {
        when(produitRepository.findById(1)).thenReturn(Optional.of(produit));
        when(produitRepository.save(any())).thenReturn(produit);

        produitService.supprimerLogique(1);

        assertThat(produit.isDeleted()).isTrue();
        verify(produitRepository).save(produit);
        verify(produitRepository, never()).deleteById(any());
    }

    // ── ajusterStock ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-P-05 : ajusterStock() avec delta positif augmente le stock")
    void ajusterStock_avecDeltaPositif_augmenteStock() {
        when(produitRepository.findById(1)).thenReturn(Optional.of(produit));
        when(produitRepository.save(any())).thenReturn(produit);

        produitService.ajusterStock(1, 5);

        assertThat(produit.getStock()).isEqualTo(15);
        verify(produitRepository).save(produit);
    }

    @Test
    @DisplayName("UT-P-05b : ajusterStock() avec delta négatif suffisant décrémente le stock")
    void ajusterStock_avecDeltaNegatifSuffisant_decrementeStock() {
        when(produitRepository.findById(1)).thenReturn(Optional.of(produit));
        when(produitRepository.save(any())).thenReturn(produit);

        produitService.ajusterStock(1, -3);

        assertThat(produit.getStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("UT-P-06 : ajusterStock() lève une exception si stock insuffisant")
    void ajusterStock_leveException_siStockInsuffisant() {
        when(produitRepository.findById(1)).thenReturn(Optional.of(produit));

        assertThatThrownBy(() -> produitService.ajusterStock(1, -20))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stock insuffisant");

        verify(produitRepository, never()).save(any());
    }

    // ── rechercher ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-P-07 : rechercher() retourne les produits correspondant à la requête")
    void rechercher_retourneProduitsCorrespondants() {
        when(produitRepository.search("laptop")).thenReturn(List.of(produit));

        List<Produit> result = produitService.rechercher("laptop");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).containsIgnoringCase("laptop");
    }

    @Test
    @DisplayName("UT-P-07b : rechercher() retourne liste vide si aucun résultat")
    void rechercher_retourneListeVide_siAucunResultat() {
        when(produitRepository.search("xxxxxxxx")).thenReturn(List.of());

        List<Produit> result = produitService.rechercher("xxxxxxxx");

        assertThat(result).isEmpty();
    }

    // ── sauvegarder ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-P-08 : sauvegarder() appelle save du repository et retourne le produit")
    void sauvegarder_appelleRepository_etRetourneProduit() {
        when(produitRepository.save(produit)).thenReturn(produit);

        Produit result = produitService.sauvegarder(produit);

        assertThat(result).isEqualTo(produit);
        verify(produitRepository).save(produit);
    }
}