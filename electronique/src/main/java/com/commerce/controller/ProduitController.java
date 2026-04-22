package com.commerce.controller;

import com.commerce.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;
    private final CategorieService categorieService;

    @GetMapping
    public String liste(@RequestParam(required=false) String q,
                        @RequestParam(required=false) Integer categorieId,
                        @RequestParam(required=false) Double prixMin,
                        @RequestParam(required=false) Double prixMax,
                        @RequestParam(required=false) Boolean enStock,
                        Model model) {
        
        var produits = produitService.listerActifs();
        
        // Filtre par recherche
        if (q != null && !q.isBlank()) {
            produits = produitService.rechercher(q);
        }
        
        // Filtre par catégorie
        if (categorieId != null) {
            produits = produitService.parCategorie(categorieId);
        }
        
        // Filtre par prix (conversion Double -> BigDecimal)
        if (prixMin != null) {
            BigDecimal min = BigDecimal.valueOf(prixMin);
            produits = produits.stream()
                    .filter(p -> p.getPrix().compareTo(min) >= 0)
                    .toList();
        }
        
        if (prixMax != null) {
            BigDecimal max = BigDecimal.valueOf(prixMax);
            produits = produits.stream()
                    .filter(p -> p.getPrix().compareTo(max) <= 0)
                    .toList();
        }
        
        // Filtre par stock
        if (enStock != null) {
            produits = produits.stream()
                    .filter(p -> p.isEnStock() == enStock)
                    .toList();
        }
        
        // Calculer le nombre de produits par catégorie
        var categories = categorieService.listerToutes();
        for (var cat : categories) {
            long count = produitService.parCategorie(cat.getId()).stream()
                    .filter(p -> !p.isDeleted())
                    .count();
            cat.setNbProduits((int) count);
        }
        
        model.addAttribute("produits", produits);
        model.addAttribute("categories", categories);
        model.addAttribute("q", q);
        model.addAttribute("categorieId", categorieId);
        model.addAttribute("totalProduits", produitService.countActifs());
        
        return "produits/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("produit", produitService.findById(id));
        return "produits/detail";
    }
}