package com.commerce.controller;

import com.commerce.model.*;
import com.commerce.repository.UtilisateurRepository;
import com.commerce.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProduitService produitService;
    private final CategorieService categorieService;
    private final CommandeService commandeService;
    private final UtilisateurServices utilisateurService;
    private final PanierService panierService;
    

    @Value("${upload.path:src/main/resources/static/images/}")
    private String uploadPath;

    @Value("${upload.url:/images/}")
    private String uploadUrl;

    @GetMapping
    public String dashboard(Model model) {
        // Statistiques de base
        long nbProduits = produitService.listerActifs().size();
        long nbCommandes = commandeService.toutesLesCommandes().size();
        long nbClients = utilisateurService.count();
        
        // Calcul du CA total
        BigDecimal caTotal = commandeService.toutesLesCommandes().stream()
                .filter(c -> c.getStatut() == Commande.Statut.VALIDEE)
                .map(Commande::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("nbProduits", nbProduits);
        model.addAttribute("nbCommandes", nbCommandes);
        model.addAttribute("nbClients", nbClients);
        model.addAttribute("caTotal", caTotal);
        model.addAttribute("dernieresCommandes", commandeService.toutesLesCommandes().stream().limit(5).toList());
        
        // ========================================================
        // DONNÉES POUR LES GRAPHIQUES
        // ========================================================
        
        // 1. Graphique des inscriptions (6 derniers mois)
        Map<String, Long> inscriptionsParMois = utilisateurService.getInscriptionsParMois(6);
        model.addAttribute("inscriptionsLabels", new ArrayList<>(inscriptionsParMois.keySet()));
        model.addAttribute("inscriptionsData", new ArrayList<>(inscriptionsParMois.values()));
        
        // 2. Graphique des commandes (6 derniers mois)
        Map<String, Long> commandesParMois = commandeService.getCommandesParMois(6);
        model.addAttribute("commandesLabels", new ArrayList<>(commandesParMois.keySet()));
        model.addAttribute("commandesData", new ArrayList<>(commandesParMois.values()));
        
        // 3. Graphique de fréquentation du panier (7 derniers jours)
        Map<String, Long> ajoutsPanierParJour = panierService.getAjoutsPanierParJour(7);
        Map<String, Long> commandesParJour =commandeService.getCommandesParJour(7);
        
        model.addAttribute("semaineLabels", new ArrayList<>(ajoutsPanierParJour.keySet()));
        model.addAttribute("ajoutsPanierData", new ArrayList<>(ajoutsPanierParJour.values()));
        model.addAttribute("commandesValideesData", new ArrayList<>(commandesParJour.values()));
        
        return "admin/dashboard";
    }
    
 

    // ── PRODUITS ──────────────────────────────────────────────
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
    public String sauvegarderProduit(
            @ModelAttribute Produit produit,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes ra) {
        
        try {
            // 🔥 CORRECTION : Gérer l'image AVANT de sauvegarder le produit
            String imageUrl = null;
            
            // Cas 1: Un fichier a été uploadé
            if (imageFile != null && !imageFile.isEmpty()) {
                // Générer un nom unique pour l'image
                String originalFilename = imageFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String newFilename = UUID.randomUUID().toString() + extension;
                
                // Créer le dossier si inexistant
                Path uploadDir = Paths.get(uploadPath);
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                
                // Sauvegarder le fichier
                Path filePath = uploadDir.resolve(newFilename);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                imageUrl = uploadUrl + newFilename;
            }
            
            // Cas 2: Pas de nouvelle image mais produit existant (conserver l'ancienne)
            if (imageUrl == null && produit.getId() != null) {
                Produit existing = produitService.findById(produit.getId());
                imageUrl = existing.getImageUrl();
            }
            
            // 🔥 IMPORTANT: Mettre à jour l'URL de l'image
            produit.setImageUrl(imageUrl);
            
            // Sauvegarder le produit
            produitService.sauvegarder(produit);
            ra.addFlashAttribute("success", "Produit sauvegardé !");
            
        } catch (IOException e) {
            ra.addFlashAttribute("erreur", "Erreur lors de l'upload de l'image : " + e.getMessage());
            return "redirect:/admin/produits/editer/" + (produit.getId() != null ? produit.getId() : "");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", "Erreur : " + e.getMessage());
            e.printStackTrace();
        }
        
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
    public String commandes(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String client,
            @RequestParam(required = false) String dateMin,
            @RequestParam(required = false) String dateMax,
            @RequestParam(required = false) Double montantMin,
            @RequestParam(required = false) Double montantMax,
            @RequestParam(required = false) String sort,
            Model model) {
        
    	List<Commande> commandes=commandeService.getCommandesFiltrer(statut,client,dateMin,dateMax,montantMin,montantMax,sort);
    	
        model.addAttribute("commandes", commandes);
        model.addAttribute("statutFiltre", statut);
        model.addAttribute("clientFiltre", client);
        model.addAttribute("dateMinFiltre", dateMin);
        model.addAttribute("dateMaxFiltre", dateMax);
        model.addAttribute("montantMinFiltre", montantMin);
        model.addAttribute("montantMaxFiltre", montantMax);
        model.addAttribute("sortFiltre", sort);
        
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
        model.addAttribute("clients", this.utilisateurService.findAll());
        return "admin/clients";
    }

    @PostMapping("/clients/{id}/toggle")
    public String toggleClient(@PathVariable Integer id, RedirectAttributes ra) {
    	this.utilisateurService.findById(id);
        ra.addFlashAttribute("success", "Statut client modifié.");
        return "redirect:/admin/clients";
    }
}