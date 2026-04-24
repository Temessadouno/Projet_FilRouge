package com.commerce.controller;

import com.commerce.model.Produit;
import com.commerce.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ProduitService produitService;
    private final CategorieService categorieService;
    private final PanierService panierService;
    private final AuthService authService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails ud, Model model) {
        // 5 derniers produits ajoutés (pour la colonne de droite)
        List<Produit> derniersProduits = produitService.findTop5ByOrderByCreatedAtDesc();
       // System.out.println("Nombre de derniers produits: " + (derniersProduits != null ? derniersProduits.size() : 0));
        model.addAttribute("derniersProduits", derniersProduits);
        
        // Tous les produits actifs (pour les stats)
        model.addAttribute("produits", produitService.listerActifs());
        
        // Catégories (pour les filtres si besoin)
        model.addAttribute("categories", categorieService.listerToutes());
        
        // Nombre d'articles dans le panier (si utilisateur connecté)
        if (ud != null) {
            Integer cid = authService.findByEmail(ud.getUsername()).getId();
            model.addAttribute("nbPanier", panierService.getNombreArticles(cid));
        }
        
        return "index";
    }
    
    @GetMapping("/api/derniers-produits")
    @ResponseBody
    public List<Produit> getDerniersProduits() {
        return produitService.findTop5ByOrderByCreatedAtDesc();
    }
}