package com.commerce.service;

import com.commerce.model.*;
import com.commerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PanierService {

    private final PanierItemRepository panierItemRepository;
    private final ProduitRepository produitRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Récupérer tous les items du panier d'un client
     */
    public List<PanierItem> getItems(Integer clientId) {
        return panierItemRepository.findByClientId(clientId);
    }

    /**
     * Compter le nombre d'articles dans le panier
     */
    public int getNombreArticles(Integer clientId) {
        return panierItemRepository.countByClientId(clientId);
    }

    /**
     * Ajouter un produit au panier
     */
    @Transactional
    public void ajouter(Integer clientId, Integer produitId, int quantite) {
        Utilisateur client = utilisateurRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        if (produit.getStock() < quantite) {
            throw new RuntimeException("Stock insuffisant. Stock disponible: " + produit.getStock());
        }

        panierItemRepository.findByClientIdAndProduitId(clientId, produitId)
            .ifPresentOrElse(item -> {
                int nouvelleQte = item.getQuantite() + quantite;
                if (produit.getStock() < nouvelleQte) {
                    throw new RuntimeException("Stock insuffisant pour ajouter cette quantité");
                }
                item.setQuantite(nouvelleQte);
                panierItemRepository.save(item);
            }, () -> {
                PanierItem item = new PanierItem();
                item.setClient(client);
                item.setProduit(produit);
                item.setQuantite(quantite);
                item.setEnabled(true);  // Par défaut activé
                panierItemRepository.save(item);
            });
    }

    /**
     * Modifier la quantité d'un produit
     */
    @Transactional
    public void modifierQuantite(Integer clientId, Integer produitId, int nouvelleQte) {
        if (nouvelleQte <= 0) {
            supprimer(clientId, produitId);
            return;
        }
        
        panierItemRepository.findByClientIdAndProduitId(clientId, produitId)
            .ifPresent(item -> {
                Produit produit = item.getProduit();
                if (produit.getStock() < nouvelleQte) {
                    throw new RuntimeException("Stock insuffisant. Maximum: " + produit.getStock());
                }
                item.setQuantite(nouvelleQte);
                panierItemRepository.save(item);
            });
    }

    /**
     * Supprimer un produit du panier
     */
    @Transactional
    public void supprimer(Integer clientId, Integer produitId) {
        panierItemRepository.findByClientIdAndProduitId(clientId, produitId)
            .ifPresent(panierItemRepository::delete);
    }

    /**
     * Vider complètement le panier
     */
    @Transactional
    public void vider(Integer clientId) {
        panierItemRepository.deleteByClientId(clientId);
    }

    /**
     * Calculer le total du panier (uniquement produits ACTIVÉS)
     */
    public BigDecimal getTotal(Integer clientId) {
        return getItems(clientId).stream()
                .filter(PanierItem::isEnabled)  // ← Seulement les produits activés
                .map(item -> item.getProduit().getPrix()
                        .multiply(BigDecimal.valueOf(item.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Activer ou désactiver un produit dans le panier
     */
    @Transactional
    public void toggleProduit(Integer clientId, Integer produitId, Boolean enabled) {
        PanierItem item = panierItemRepository
                .findByClientIdAndProduitId(clientId, produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé dans le panier"));
        
        item.setEnabled(enabled);
        panierItemRepository.save(item);
    }

    /**
     * Vérifier si un produit est activé
     */
    public boolean isProduitEnabled(Integer clientId, Integer produitId) {
        return panierItemRepository.findByClientIdAndProduitId(clientId, produitId)
                .map(PanierItem::isEnabled)
                .orElse(false);
    }
    
    public long countAjoutsByDateBetween(LocalDateTime debut, LocalDateTime fin) {
        return panierItemRepository.countByAddedAtBetween(debut, fin);
    }
    
    
    /**
     * Récupère le nombre d'ajouts au panier par jour pour les X derniers jours
     */
    public Map<String, Long> getAjoutsPanierParJour(int nbJours) {
        Map<String, Long> result = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH);
        
        for (int i = nbJours - 1; i >= 0; i--) {
            LocalDate jour = now.minusDays(i);
            String jourKey = jour.format(formatter);
            
            LocalDateTime debutJour = jour.atStartOfDay();
            LocalDateTime finJour = jour.plusDays(1).atStartOfDay().minusSeconds(1);
            
            long count = this.countAjoutsByDateBetween(debutJour, finJour);
            result.put(jourKey, count);
        }
        return result;
    }
}