package com.commerce.service;

import com.commerce.model.PanierItem;
import com.commerce.model.Produit;
import com.commerce.model.Utilisateur;
import com.commerce.repository.PanierItemRepository;
import com.commerce.repository.ProduitRepository;
import com.commerce.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("Tests unitaires — PanierService")
class PanierServiceTest {

    @Mock private PanierItemRepository panierItemRepository;
    @Mock private ProduitRepository produitRepository;
    @Mock private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private PanierService panierService;

    private Utilisateur client;
    private Produit produit;
    private PanierItem item;

    @BeforeEach
    void setUp() {
        client = new Utilisateur();
        client.setId(1);
        client.setEmail("test@aql.ma");
        client.setNom("Alami");
        client.setPrenom("Youssef");

        produit = new Produit();
        produit.setId(10);
        produit.setNom("Clavier Mécanique");
        produit.setPrix(new BigDecimal("350.00"));
        produit.setStock(5);
        produit.setDeleted(false);

        item = new PanierItem();
        item.setId(1);
        item.setClient(client);
        item.setProduit(produit);
        item.setQuantite(2);
    }

    // ── getItems ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-PA-01a : getItems() retourne les items du client")
    void getItems_retourneItemsClient() {
        when(panierItemRepository.findByClientId(1)).thenReturn(List.of(item));

        List<PanierItem> result = panierService.getItems(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduit().getNom()).isEqualTo("Clavier Mécanique");
    }

    // ── ajouter ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-PA-01 : ajouter() crée un nouveau PanierItem si produit absent du panier")
    void ajouter_creNouvelItem_siProduitAbsent() {
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(client));
        when(produitRepository.findById(10)).thenReturn(Optional.of(produit));
        when(panierItemRepository.findByClientIdAndProduitId(1, 10)).thenReturn(Optional.empty());

        panierService.ajouter(1, 10, 2);

        ArgumentCaptor<PanierItem> captor = ArgumentCaptor.forClass(PanierItem.class);
        verify(panierItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantite()).isEqualTo(2);
        assertThat(captor.getValue().getProduit().getId()).isEqualTo(10);
    }

    @Test
    @DisplayName("UT-PA-02 : ajouter() incrémente la quantité si produit déjà présent")
    void ajouter_incrementeQuantite_siProduitDejaPresent() {
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(client));
        when(produitRepository.findById(10)).thenReturn(Optional.of(produit));
        when(panierItemRepository.findByClientIdAndProduitId(1, 10)).thenReturn(Optional.of(item));

        panierService.ajouter(1, 10, 1);

        assertThat(item.getQuantite()).isEqualTo(3);
        verify(panierItemRepository).save(item);
    }

    @Test
    @DisplayName("UT-PA-03 : ajouter() lève une exception si stock insuffisant")
    void ajouter_leveException_siStockInsuffisant() {
        produit.setStock(1);
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(client));
        when(produitRepository.findById(10)).thenReturn(Optional.of(produit));

        assertThatThrownBy(() -> panierService.ajouter(1, 10, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stock insuffisant");

        verify(panierItemRepository, never()).save(any());
    }

    // ── modifierQuantite ──────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-PA-04 : modifierQuantite() met à jour la quantité si > 0")
    void modifierQuantite_metsAJourQuantite_siSuperieurZero() {
        when(panierItemRepository.findByClientIdAndProduitId(1, 10)).thenReturn(Optional.of(item));

        panierService.modifierQuantite(1, 10, 4);

        assertThat(item.getQuantite()).isEqualTo(4);
        verify(panierItemRepository).save(item);
    }

    @Test
    @DisplayName("UT-PA-05 : modifierQuantite() supprime l'item si quantité = 0")
    void modifierQuantite_supprime_siQuantiteZero() {
        when(panierItemRepository.findByClientIdAndProduitId(1, 10)).thenReturn(Optional.of(item));

        panierService.modifierQuantite(1, 10, 0);

        verify(panierItemRepository).delete(item);
        verify(panierItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-PA-05b : modifierQuantite() supprime l'item si quantité négative")
    void modifierQuantite_supprime_siQuantiteNegative() {
        when(panierItemRepository.findByClientIdAndProduitId(1, 10)).thenReturn(Optional.of(item));

        panierService.modifierQuantite(1, 10, -1);

        verify(panierItemRepository).delete(item);
    }

    // ── getTotal ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-PA-06 : getTotal() calcule correctement la somme des items")
    void getTotal_calculeCorrectement() {
        Produit produit2 = new Produit();
        produit2.setId(11);
        produit2.setPrix(new BigDecimal("100.00"));

        PanierItem item2 = new PanierItem();
        item2.setProduit(produit2);
        item2.setQuantite(3);

        when(panierItemRepository.findByClientId(1)).thenReturn(List.of(item, item2));
        // item: 350 * 2 = 700, item2: 100 * 3 = 300 → total = 1000

        BigDecimal total = panierService.getTotal(1);

        assertThat(total).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("UT-PA-06b : getTotal() retourne 0 pour un panier vide")
    void getTotal_retourneZero_pourPanierVide() {
        when(panierItemRepository.findByClientId(1)).thenReturn(List.of());

        BigDecimal total = panierService.getTotal(1);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── vider ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-PA-07 : vider() supprime tous les items du client")
    void vider_supprimeTousLesItems() {
        panierService.vider(1);

        verify(panierItemRepository).deleteByClientId(1);
    }

    // ── getNombreArticles ─────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-PA-08 : getNombreArticles() retourne le compte correct")
    void getNombreArticles_retourneComptage() {
        when(panierItemRepository.countByClientId(1)).thenReturn(3);

        int result = panierService.getNombreArticles(1);

        assertThat(result).isEqualTo(3);
    }
}