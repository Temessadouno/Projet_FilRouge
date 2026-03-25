package com.commerce.service;

import com.commerce.model.*;
import com.commerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PanierService {

    private final PanierItemRepository panierItemRepository;
    private final ProduitRepository produitRepository;
    private final UtilisateurRepository utilisateurRepository;

    public List<PanierItem> getItems(Integer clientId) {
        return panierItemRepository.findByClientId(clientId);
    }

    public int getNombreArticles(Integer clientId) {
        return panierItemRepository.countByClientId(clientId);
    }

    @Transactional
    public void ajouter(Integer clientId, Integer produitId, int quantite) {
        Utilisateur client = utilisateurRepository.findById(clientId).orElseThrow();
        Produit produit = produitRepository.findById(produitId).orElseThrow();

        if (produit.getStock() < quantite) throw new RuntimeException("Stock insuffisant");

        panierItemRepository.findByClientIdAndProduitId(clientId, produitId)
            .ifPresentOrElse(item -> {
                item.setQuantite(item.getQuantite() + quantite);
                panierItemRepository.save(item);
            }, () -> {
                PanierItem item = new PanierItem();
                item.setClient(client);
                item.setProduit(produit);
                item.setQuantite(quantite);
                panierItemRepository.save(item);
            });
    }

    @Transactional
    public void modifierQuantite(Integer clientId, Integer produitId, int nouvelleQte) {
        if (nouvelleQte <= 0) {
            supprimer(clientId, produitId);
            return;
        }
        panierItemRepository.findByClientIdAndProduitId(clientId, produitId)
            .ifPresent(item -> { item.setQuantite(nouvelleQte); panierItemRepository.save(item); });
    }

    @Transactional
    public void supprimer(Integer clientId, Integer produitId) {
        panierItemRepository.findByClientIdAndProduitId(clientId, produitId)
            .ifPresent(panierItemRepository::delete);
    }

    @Transactional
    public void vider(Integer clientId) {
        panierItemRepository.deleteByClientId(clientId);
    }

    public BigDecimal getTotal(Integer clientId) {
        return getItems(clientId).stream()
            .map(i -> i.getProduit().getPrix().multiply(BigDecimal.valueOf(i.getQuantite())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}