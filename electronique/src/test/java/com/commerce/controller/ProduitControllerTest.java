package com.commerce.controller;

import com.commerce.model.Categorie;
import com.commerce.model.Produit;
import com.commerce.service.AuthService;
import com.commerce.service.CategorieService;
import com.commerce.service.PanierService;
import com.commerce.service.ProduitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // ✅ Boot 4
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(ProduitController.class)
@DisplayName("Tests contrôleur — ProduitController")
class ProduitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ProduitService produitService;      // 
    @MockitoBean private CategorieService categorieService;  // 
    @MockitoBean private PanierService panierService;        // 
    @MockitoBean private AuthService authService;            // 
    private Produit produit;
    private Categorie categorie;

    @BeforeEach
    void setUp() {
        categorie = new Categorie();
        categorie.setId(1);
        categorie.setNom("Informatique");

        produit = new Produit();
        produit.setId(1);
        produit.setNom("MacBook Pro");
        produit.setPrix(new BigDecimal("15000.00"));
        produit.setStock(3);
        produit.setDeleted(false);
        produit.setCategorie(categorie);
    }

    // ── GET /produits ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("IT-01 : GET /produits est accessible sans authentification (HTTP 200)")
    void getProduits_estAccessibleSansAuth() throws Exception {
        when(produitService.listerActifs()).thenReturn(List.of(produit));
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mockMvc.perform(get("/produits"))
                .andExpect(status().isOk())
                .andExpect(view().name("produits/liste"))
                .andExpect(model().attributeExists("produits"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("IT-01b : GET /produits avec recherche appelle rechercher()")
    void getProduits_avecRecherche_appelleProduitServiceRechercher() throws Exception {
        when(produitService.rechercher("macbook")).thenReturn(List.of(produit));
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mockMvc.perform(get("/produits").param("q", "macbook"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("q", "macbook"));

        verify(produitService).rechercher("macbook");
    }

    @Test
    @DisplayName("IT-01c : GET /produits avec filtre catégorie appelle parCategorie()")
    void getProduits_avecFiltreCat_appelleParCategorie() throws Exception {
        when(produitService.parCategorie(1)).thenReturn(List.of(produit));
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mockMvc.perform(get("/produits").param("categorieId", "1"))
                .andExpect(status().isOk());

        verify(produitService).parCategorie(1);
    }

    // ── GET /produits/{id} ────────────────────────────────────────────────────

    @Test
    @DisplayName("IT-01d : GET /produits/{id} retourne la page détail")
    void getProduitDetail_retourneVueDetail() throws Exception {
        when(produitService.findById(1)).thenReturn(produit);

        mockMvc.perform(get("/produits/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("produits/detail"))
                .andExpect(model().attributeExists("produit"));
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// PanierControllerTest — dans le même fichier pour concision
// En production : séparer dans PanierControllerTest.java
// ─────────────────────────────────────────────────────────────────────────────