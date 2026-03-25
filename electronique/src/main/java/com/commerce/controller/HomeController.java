package com.commerce.controller;

import com.commerce.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ProduitService produitService;
    private final CategorieService categorieService;
    private final PanierService panierService;
    private final AuthService authService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("produits", produitService.listerActifs());
        model.addAttribute("categories", categorieService.listerToutes());
        if (ud != null) {
            Integer cid = authService.findByEmail(ud.getUsername()).getId();
            model.addAttribute("nbPanier", panierService.getNombreArticles(cid));
        }
        return "index";
    }
}