package com.commerce.controller;

import com.commerce.model.Commande;
import com.commerce.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/commande")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;
    private final AuthService authService;
    private final PanierService panierService;

    private Integer getClientId(UserDetails ud) {
        return authService.findByEmail(ud.getUsername()).getId();
    }

    @GetMapping("/confirmer")
    public String confirmerForm(@AuthenticationPrincipal UserDetails ud, 
                                Model model,
                                HttpServletRequest request) {  // ✅ Ajouter HttpServletRequest
        Integer cid = getClientId(ud);
        
        // ✅ Ajouter explicitement le token CSRF au modèle
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        
        model.addAttribute("items", panierService.getItems(cid));
        model.addAttribute("total", panierService.getTotal(cid));
        model.addAttribute("client", authService.findByEmail(ud.getUsername()));
        
        return "commande/confirmer";
    }

    @PostMapping("/passer")
    public String passer(@AuthenticationPrincipal UserDetails ud,
                         @RequestParam String adresse) {
        Integer cid = getClientId(ud);
        Commande c = commandeService.passerCommande(cid, adresse);
        return "redirect:/commande/recu/" + c.getId();
    }

    @GetMapping("/recu/{id}")
    public String recu(@PathVariable Integer id, Model model) {
        model.addAttribute("commande", commandeService.findById(id));
        return "commande/recu";
    }

    @GetMapping("/mes-commandes")
    public String mesCommandes(@AuthenticationPrincipal UserDetails ud, Model model) {
        model.addAttribute("commandes", commandeService.mesCommandes(getClientId(ud)));
        return "commande/liste";
    }
}