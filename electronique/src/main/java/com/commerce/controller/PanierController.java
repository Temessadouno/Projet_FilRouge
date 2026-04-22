package com.commerce.controller;

import com.commerce.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/panier")
@RequiredArgsConstructor
public class PanierController {

    private final PanierService panierService;
    private final AuthService authService;

    private Integer getClientId(UserDetails ud) {
        return authService.findByEmail(ud.getUsername()).getId();
    }
    
    // Méthode pour mettre à jour le nombre d'articles dans la session
    private void updateSessionNombreArticles(HttpSession session, Integer clientId) {
        int nombreArticles = panierService.getNombreArticles(clientId);
        session.setAttribute("nombreArticles", nombreArticles);
    }

    @GetMapping
    public String afficher(@AuthenticationPrincipal UserDetails ud, Model model, HttpSession session) {
        Integer cid = getClientId(ud);
        model.addAttribute("items", panierService.getItems(cid));
        model.addAttribute("total", panierService.getTotal(cid));
        
        // Mettre à jour la session
        updateSessionNombreArticles(session, cid);
        
        return "panier/panier";
    }

    @PostMapping("/ajouter")
    public String ajouter(@AuthenticationPrincipal UserDetails ud,
                          @RequestParam Integer produitId,
                          @RequestParam(defaultValue="1") int quantite,
                          RedirectAttributes ra,
                          HttpSession session) {
        try {
            Integer cid = getClientId(ud);
            panierService.ajouter(cid, produitId, quantite);
            
            // Mettre à jour la session après ajout
            updateSessionNombreArticles(session, cid);
            
            ra.addFlashAttribute("success", "Produit ajouté au panier !");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/produits";
    }

    @PostMapping("/modifier")
    public String modifier(@AuthenticationPrincipal UserDetails ud,
                           @RequestParam Integer produitId,
                           @RequestParam int quantite,
                           HttpSession session) {
        Integer cid = getClientId(ud);
        panierService.modifierQuantite(cid, produitId, quantite);
        
        // Mettre à jour la session après modification
        updateSessionNombreArticles(session, cid);
        
        return "redirect:/panier";
    }

    @PostMapping("/supprimer")
    public String supprimer(@AuthenticationPrincipal UserDetails ud,
                            @RequestParam Integer produitId,
                            HttpSession session) {
        Integer cid = getClientId(ud);
        panierService.supprimer(cid, produitId);
        
        // Mettre à jour la session après suppression
        updateSessionNombreArticles(session, cid);
        
        return "redirect:/panier";
    }

    @PostMapping("/vider")
    public String vider(@AuthenticationPrincipal UserDetails ud, HttpSession session) {
        Integer cid = getClientId(ud);
        panierService.vider(cid);
        
        // Mettre à jour la session après vidage
        updateSessionNombreArticles(session, cid);
        
        return "redirect:/panier";
    }

    // ============================================================
    // ENDPOINTS API (pour les appels AJAX du JavaScript)
    // ============================================================

    /**
     * API : Modifier la quantité d'un produit (AJAX)
     */
    @PostMapping("/api/modifier")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiModifierQuantite(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Map<String, Object> payload,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer produitId = (Integer) payload.get("produitId");
            Integer quantite = (Integer) payload.get("quantite");
            
            if (produitId == null || quantite == null) {
                response.put("success", false);
                response.put("message", "Paramètres invalides");
                return ResponseEntity.badRequest().body(response);
            }
            
            Integer cid = getClientId(ud);
            panierService.modifierQuantite(cid, produitId, quantite);
            
            // Mettre à jour la session
            int nombreArticles = panierService.getNombreArticles(cid);
            session.setAttribute("nombreArticles", nombreArticles);
            
            response.put("success", true);
            response.put("message", "Quantité mise à jour");
            response.put("nombreArticles", nombreArticles);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API : Activer/Désactiver un produit (AJAX)
     */
    @PostMapping("/api/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiToggleProduit(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Map<String, Object> payload,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer produitId = (Integer) payload.get("produitId");
            Boolean enabled = (Boolean) payload.get("enabled");
            
            if (produitId == null || enabled == null) {
                response.put("success", false);
                response.put("message", "Paramètres invalides");
                return ResponseEntity.badRequest().body(response);
            }
            
            Integer cid = getClientId(ud);
            panierService.toggleProduit(cid, produitId, enabled);
            
            // Mettre à jour la session
            int nombreArticles = panierService.getNombreArticles(cid);
            session.setAttribute("nombreArticles", nombreArticles);
            
            response.put("success", true);
            response.put("message", enabled ? "Produit activé" : "Produit désactivé");
            response.put("enabled", enabled);
            response.put("nombreArticles", nombreArticles);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API : Supprimer un produit (AJAX)
     */
    @DeleteMapping("/api/supprimer")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiSupprimerProduit(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody Map<String, Object> payload,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer produitId = (Integer) payload.get("produitId");
            
            if (produitId == null) {
                response.put("success", false);
                response.put("message", "Paramètres invalides");
                return ResponseEntity.badRequest().body(response);
            }
            
            Integer cid = getClientId(ud);
            panierService.supprimer(cid, produitId);
            
            // Mettre à jour la session
            int nombreArticles = panierService.getNombreArticles(cid);
            session.setAttribute("nombreArticles", nombreArticles);
            
            response.put("success", true);
            response.put("message", "Produit supprimé");
            response.put("nombreArticles", nombreArticles);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}