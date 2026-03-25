package com.commerce.controller;

import com.commerce.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;
    private final CategorieService categorieService;

    @GetMapping
    public String liste(@RequestParam(required=false) String q,
                        @RequestParam(required=false) Integer categorieId,
                        Model model) {
        var produits = (q != null && !q.isBlank())   ? produitService.rechercher(q)
                     : (categorieId != null)          ? produitService.parCategorie(categorieId)
                     : produitService.listerActifs();
        model.addAttribute("produits", produits);
        model.addAttribute("categories", categorieService.listerToutes());
        model.addAttribute("q", q);
        model.addAttribute("categorieId", categorieId);
        return "produits/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("produit", produitService.findById(id));
        return "produits/detail";
    }
}