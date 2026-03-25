package com.commerce.service;

import com.commerce.model.*;
import com.commerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final PanierItemRepository panierItemRepository;
    private final ProduitService produitService;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public Commande passerCommande(Integer clientId, String adresse) {
        Utilisateur client = utilisateurRepository.findById(clientId).orElseThrow();
        List<PanierItem> items = panierItemRepository.findByClientId(clientId);

        if (items.isEmpty()) throw new RuntimeException("Panier vide");

        Commande commande = new Commande();
        commande.setClient(client);
        commande.setAdresseLivraison(adresse);
        commande.setStatut(Commande.Statut.EN_COURS);

        for (PanierItem item : items) {
            produitService.ajusterStock(item.getProduit().getId(), -item.getQuantite());
            LigneCommande ligne = new LigneCommande(item.getProduit(), item.getQuantite());
            commande.addLigne(ligne);
        }

        commande.calculerTotal();
        Commande saved = commandeRepository.save(commande);
        panierItemRepository.deleteByClientId(clientId);
        return saved;
    }

    public List<Commande> mesCommandes(Integer clientId) {
        return commandeRepository.findByClientIdOrderByDateCommandeDesc(clientId);
    }

    public Commande findById(Integer id) {
        return commandeRepository.findById(id).orElseThrow();
    }

    public List<Commande> toutesLesCommandes() {
        return commandeRepository.findAllByOrderByDateCommandeDesc();
    }

    @Transactional
    public void changerStatut(Integer commandeId, Commande.Statut statut) {
        Commande c = findById(commandeId);
        c.setStatut(statut);
        commandeRepository.save(c);
    }
}