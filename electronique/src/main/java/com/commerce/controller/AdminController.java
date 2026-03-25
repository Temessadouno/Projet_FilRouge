package com.commerce.controller;

import com.commerce.model.*;
import com.commerce.repository.UtilisateurRepository;
import com.commerce.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProduitService produitService;
    private final CategorieService categorieService;
    private final CommandeService commandeService;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("nbProduits", produitService.listerActifs().size());
        model.addAttribute("nbCommandes", commandeService.toutesLesCommandes().size());
        model.addAttribute("nbClients", utilisateurRepository.count());
        model.addAttribute("dernieresCommandes", commandeService.toutesLesCommandes().stream().limit(5).toList());
        return "admin/dashboard";
    }

    // ── PRODUITS ──────────────────────────────────────────────
    @GetMapping("/produits")
    public String produits(Model model) {
        model.addAttribute("produits", produitService.listerTous());
        return "admin/produits/liste";
    }

    @GetMapping("/produits/nouveau")
    public String nouveauProduit(Model model) {
        model.addAttribute("produit", new Produit());
        model.addAttribute("categories", categorieService.listerToutes());
        return "admin/produits/form";
    }

    @GetMapping("/produits/editer/{id}")
    public String editerProduit(@PathVariable Integer id, Model model) {
        model.addAttribute("produit", produitService.findById(id));
        model.addAttribute("categories", categorieService.listerToutes());
        return "admin/produits/form";
    }

    @PostMapping("/produits/sauvegarder")
    public String sauvegarderProduit(@ModelAttribute Produit produit, RedirectAttributes ra) {
        produitService.sauvegarder(produit);
        ra.addFlashAttribute("success", "Produit sauvegardé !");
        return "redirect:/admin/produits";
    }

    @PostMapping("/produits/supprimer/{id}")
    public String supprimerProduit(@PathVariable Integer id, RedirectAttributes ra) {
        produitService.supprimerLogique(id);
        ra.addFlashAttribute("success", "Produit supprimé.");
        return "redirect:/admin/produits";
    }

    // ── CATÉGORIES ───────────────────────────────────────────
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categorieService.listerToutes());
        model.addAttribute("categorie", new Categorie());
        return "admin/categories";
    }

    @PostMapping("/categories/sauvegarder")
    public String sauvegarderCategorie(@ModelAttribute Categorie categorie, RedirectAttributes ra) {
        categorieService.sauvegarder(categorie);
        ra.addFlashAttribute("success", "Catégorie sauvegardée !");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/supprimer/{id}")
    public String supprimerCategorie(@PathVariable Integer id, RedirectAttributes ra) {
        categorieService.supprimer(id);
        ra.addFlashAttribute("success", "Catégorie supprimée.");
        return "redirect:/admin/categories";
    }

    // ── COMMANDES ────────────────────────────────────────────
    @GetMapping("/commandes")
    public String commandes(Model model) {
        model.addAttribute("commandes", commandeService.toutesLesCommandes());
        return "admin/commandes";
    }

    @PostMapping("/commandes/{id}/statut")
    public String changerStatut(@PathVariable Integer id,
                                @RequestParam Commande.Statut statut,
                                RedirectAttributes ra) {
        commandeService.changerStatut(id, statut);
        ra.addFlashAttribute("success", "Statut mis à jour.");
        return "redirect:/admin/commandes";
    }

    // ── CLIENTS ──────────────────────────────────────────────
    @GetMapping("/clients")
    public String clients(Model model) {
        model.addAttribute("clients", utilisateurRepository.findAll());
        return "admin/clients";
    }

    @PostMapping("/clients/{id}/toggle")
    public String toggleClient(@PathVariable Integer id, RedirectAttributes ra) {
        utilisateurRepository.findById(id).ifPresent(u -> {
            u.setActif(!u.isActif());
            utilisateurRepository.save(u);
        });
        ra.addFlashAttribute("success", "Statut client modifié.");
        return "redirect:/admin/clients";
    }
}