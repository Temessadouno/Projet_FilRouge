package com.commerce.service;

import com.commerce.model.*;
import com.commerce.repository.CommandeRepository;
import com.commerce.repository.PanierItemRepository;
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
@DisplayName("Tests unitaires — CommandeService")
class CommandeServiceTest {

    @Mock private CommandeRepository commandeRepository;
    @Mock private PanierItemRepository panierItemRepository;
    @Mock private ProduitService produitService;
    @Mock private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private CommandeService commandeService;

    private Utilisateur client;
    private Produit produit;
    private PanierItem panierItem;

    @BeforeEach
    void setUp() {
        client = new Utilisateur();
        client.setId(1);
        client.setEmail("client@aql.ma");
        client.setNom("Bennani");
        client.setPrenom("Sara");
        client.setAdresseLivraison("12 Rue Hassan II, Casablanca");

        produit = new Produit();
        produit.setId(5);
        produit.setNom("Souris Gaming");
        produit.setPrix(new BigDecimal("299.00"));
        produit.setStock(10);

        panierItem = new PanierItem();
        panierItem.setId(1);
        panierItem.setClient(client);
        panierItem.setProduit(produit);
        panierItem.setQuantite(2);
    }

    // ── passerCommande ────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-C-01 : passerCommande() crée une commande, décrémente le stock, vide le panier")
    void passerCommande_creeCommande_decrementeStock_videPanier() {
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(client));
        when(panierItemRepository.findByClientId(1)).thenReturn(List.of(panierItem));

        Commande commandeSaved = new Commande();
        commandeSaved.setId(100);
        commandeSaved.setStatut(Commande.Statut.EN_COURS);
        when(commandeRepository.save(any())).thenReturn(commandeSaved);

        Commande result = commandeService.passerCommande(1, "12 Rue Hassan II, Casablanca");

        assertThat(result.getId()).isEqualTo(100);
        verify(produitService).ajusterStock(5, -2);
        verify(panierItemRepository).deleteByClientId(1);
        verify(commandeRepository).save(any(Commande.class));
    }

    @Test
    @DisplayName("UT-C-02 : passerCommande() lève une exception si le panier est vide")
    void passerCommande_leveException_siPanierVide() {
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(client));
        when(panierItemRepository.findByClientId(1)).thenReturn(List.of());

        assertThatThrownBy(() -> commandeService.passerCommande(1, "adresse"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Panier vide");

        verify(commandeRepository, never()).save(any());
        verify(panierItemRepository, never()).deleteByClientId(any());
    }

    @Test
    @DisplayName("UT-C-03 : passerCommande() propage l'exception si stock insuffisant (rollback)")
    void passerCommande_propagException_siStockInsuffisant() {
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(client));
        when(panierItemRepository.findByClientId(1)).thenReturn(List.of(panierItem));
        doThrow(new RuntimeException("Stock insuffisant pour: Souris Gaming"))
                .when(produitService).ajusterStock(5, -2);

        assertThatThrownBy(() -> commandeService.passerCommande(1, "adresse"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stock insuffisant");

        verify(commandeRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-C-01b : passerCommande() calcule correctement le total de la commande")
    void passerCommande_calculeCorrectementLeTotal() {
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(client));
        when(panierItemRepository.findByClientId(1)).thenReturn(List.of(panierItem));

        ArgumentCaptor<Commande> captor = ArgumentCaptor.forClass(Commande.class);
        when(commandeRepository.save(captor.capture())).thenAnswer(i -> {
            Commande c = captor.getValue();
            c.setId(1);
            return c;
        });

        commandeService.passerCommande(1, "12 Rue Hassan II");

        Commande saved = captor.getValue();
        // 299.00 * 2 = 598.00
        assertThat(saved.getTotal()).isEqualByComparingTo(new BigDecimal("598.00"));
        assertThat(saved.getStatut()).isEqualTo(Commande.Statut.EN_COURS);
        assertThat(saved.getAdresseLivraison()).isEqualTo("12 Rue Hassan II");
    }

    // ── changerStatut ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-C-04 : changerStatut() met à jour le statut de la commande")
    void changerStatut_metsAJourLeStatut() {
        Commande commande = new Commande();
        commande.setId(1);
        commande.setStatut(Commande.Statut.EN_COURS);

        when(commandeRepository.findById(1)).thenReturn(Optional.of(commande));
        when(commandeRepository.save(any())).thenReturn(commande);

        commandeService.changerStatut(1, Commande.Statut.VALIDEE);

        assertThat(commande.getStatut()).isEqualTo(Commande.Statut.VALIDEE);
        verify(commandeRepository).save(commande);
    }

    @Test
    @DisplayName("UT-C-04b : changerStatut() peut annuler une commande")
    void changerStatut_peutAnnulerCommande() {
        Commande commande = new Commande();
        commande.setId(2);
        commande.setStatut(Commande.Statut.EN_COURS);

        when(commandeRepository.findById(2)).thenReturn(Optional.of(commande));
        when(commandeRepository.save(any())).thenReturn(commande);

        commandeService.changerStatut(2, Commande.Statut.ANNULEE);

        assertThat(commande.getStatut()).isEqualTo(Commande.Statut.ANNULEE);
    }

    // ── mesCommandes ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-C-05 : mesCommandes() retourne les commandes du client triées par date")
    void mesCommandes_retourneCommandesClient() {
        Commande c1 = new Commande(); c1.setId(1);
        Commande c2 = new Commande(); c2.setId(2);

        when(commandeRepository.findByClientIdOrderByDateCommandeDesc(1))
                .thenReturn(List.of(c2, c1));

        List<Commande> result = commandeService.mesCommandes(1);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2); // plus récente en premier
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-C-06 : findById() lève une exception si commande introuvable")
    void findById_leveException_siCommandeInexistante() {
        when(commandeRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandeService.findById(999))
                .isInstanceOf(RuntimeException.class);
    }
}