package com.commerce.controller;

import com.commerce.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/panier")
@RequiredArgsConstructor
public class PanierController {

    private final PanierService panierService;
    private final AuthService authService;

    private Integer getClientId(UserDetails ud) {
        return authService.findByEmail(ud.getUsername()).getId();
    }

    @GetMapping
    public String afficher(@AuthenticationPrincipal UserDetails ud, Model model) {
        Integer cid = getClientId(ud);
        model.addAttribute("items", panierService.getItems(cid));
        model.addAttribute("total", panierService.getTotal(cid));
        return "panier/panier";
    }

    @PostMapping("/ajouter")
    public String ajouter(@AuthenticationPrincipal UserDetails ud,
                          @RequestParam Integer produitId,
                          @RequestParam(defaultValue="1") int quantite,
                          RedirectAttributes ra) {
        try {
            panierService.ajouter(getClientId(ud), produitId, quantite);
            ra.addFlashAttribute("success", "Produit ajouté au panier !");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/produits";
    }

    @PostMapping("/modifier")
    public String modifier(@AuthenticationPrincipal UserDetails ud,
                           @RequestParam Integer produitId,
                           @RequestParam int quantite) {
        panierService.modifierQuantite(getClientId(ud), produitId, quantite);
        return "redirect:/panier";
    }

    @PostMapping("/supprimer")
    public String supprimer(@AuthenticationPrincipal UserDetails ud,
                            @RequestParam Integer produitId) {
        panierService.supprimer(getClientId(ud), produitId);
        return "redirect:/panier";
    }

    @PostMapping("/vider")
    public String vider(@AuthenticationPrincipal UserDetails ud) {
        panierService.vider(getClientId(ud));
        return "redirect:/panier";
    }
}