package com.commerce.controller;

import com.commerce.model.Utilisateur;
import com.commerce.service.AuthService;
import com.commerce.service.PanierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // ✅
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(PanierController.class)
@DisplayName("Tests contrôleur — PanierController")
class PanierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private PanierService panierService;   // ✅
    @MockitoBean private AuthService authService;       // ✅

    private Utilisateur client;

    @BeforeEach
    void setUp() {
        client = new Utilisateur();
        client.setId(1);
        client.setEmail("client@aql.ma");
        client.setRole(Utilisateur.Role.CLIENT);
    }

    // ── GET /panier ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("IT-02 : GET /panier sans authentification redirige vers login")
    void getPanier_sansAuth_redirigueVersLogin() throws Exception {
        mockMvc.perform(get("/panier"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    @WithMockUser(username = "client@aql.ma", roles = "CLIENT")
    @DisplayName("IT-02b : GET /panier avec CLIENT authentifié retourne HTTP 200")
    void getPanier_avecClientAuth_retourneOk() throws Exception {
        when(authService.findByEmail("client@aql.ma")).thenReturn(client);
        when(panierService.getItems(1)).thenReturn(List.of());
        when(panierService.getTotal(1)).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/panier"))
                .andExpect(status().isOk())
                .andExpect(view().name("panier/panier"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"));
    }

    // ── POST /panier/ajouter ──────────────────────────────────────────────────

    @Test
    @DisplayName("IT-03 : POST /panier/ajouter sans authentification redirige vers login")
    void postPanierAjouter_sansAuth_redirigueVersLogin() throws Exception {
        mockMvc.perform(post("/panier/ajouter")
                        .param("produitId", "1")
                        .param("quantite", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    @WithMockUser(username = "client@aql.ma", roles = "CLIENT")
    @DisplayName("IT-03b : POST /panier/ajouter avec CLIENT redirige vers /produits")
    void postPanierAjouter_avecClient_redirigueVersProduits() throws Exception {
        when(authService.findByEmail("client@aql.ma")).thenReturn(client);

        mockMvc.perform(post("/panier/ajouter")
                        .param("produitId", "1")
                        .param("quantite", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produits"));

        verify(panierService).ajouter(1, 1, 1);
    }

    @Test
    @WithMockUser(username = "admin@aql.ma", roles = "ADMIN")
    @DisplayName("IT-04 : POST /panier/ajouter avec ADMIN retourne 403 (accès interdit)")
    void postPanierAjouter_avecAdmin_retourne403() throws Exception {
        mockMvc.perform(post("/panier/ajouter")
                        .param("produitId", "1")
                        .param("quantite", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── POST /panier/modifier ─────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "client@aql.ma", roles = "CLIENT")
    @DisplayName("IT-05 : POST /panier/modifier met à jour la quantité et redirige")
    void postPanierModifier_metsAJourEtRedirige() throws Exception {
        when(authService.findByEmail("client@aql.ma")).thenReturn(client);

        mockMvc.perform(post("/panier/modifier")
                        .param("produitId", "1")
                        .param("quantite", "3")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/panier"));

        verify(panierService).modifierQuantite(1, 1, 3);
    }

    // ── POST /panier/supprimer ────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "client@aql.ma", roles = "CLIENT")
    @DisplayName("IT-06 : POST /panier/supprimer supprime l'item et redirige")
    void postPanierSupprimer_supprimeEtRedirige() throws Exception {
        when(authService.findByEmail("client@aql.ma")).thenReturn(client);

        mockMvc.perform(post("/panier/supprimer")
                        .param("produitId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/panier"));

        verify(panierService).supprimer(1, 1);
    }

    // ── POST /panier/vider ────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "client@aql.ma", roles = "CLIENT")
    @DisplayName("IT-07 : POST /panier/vider vide le panier et redirige")
    void postPanierVider_viderEtRedirige() throws Exception {
        when(authService.findByEmail("client@aql.ma")).thenReturn(client);

        mockMvc.perform(post("/panier/vider").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/panier"));

        verify(panierService).vider(1);
    }
}