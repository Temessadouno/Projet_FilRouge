package com.commerce.service;

import com.commerce.model.*;
import com.commerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    
    public long countByDateBetween(LocalDateTime debut, LocalDateTime fin) {
        return commandeRepository.countByDateCommandeBetween(debut, fin);
    }

    public long countValideesByDateBetween(LocalDateTime debut, LocalDateTime fin) {
        return commandeRepository.countByStatutAndDateCommandeBetween(Commande.Statut.VALIDEE, debut, fin);
    }
    
    /**
     * Récupère le nombre de commandes par mois pour les X derniers mois
     */
    public Map<String, Long> getCommandesParMois(int nbMois) {
        Map<String, Long> result = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);
        
        for (int i = nbMois - 1; i >= 0; i--) {
            LocalDateTime mois = now.minusMonths(i);
            String moisKey = mois.format(formatter);
            
            LocalDateTime debutMois = mois.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finMois = debutMois.plusMonths(1).minusSeconds(1);
            
            long count = this.countByDateBetween(debutMois, finMois);
            result.put(moisKey, count);
        }
        return result;
    }
    
    /**
     * Récupère le nombre de commandes validées par jour pour les X derniers jours
     */
    public Map<String, Long> getCommandesParJour(int nbJours) {
        Map<String, Long> result = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH);
        
        for (int i = nbJours - 1; i >= 0; i--) {
            LocalDate jour = now.minusDays(i);
            String jourKey = jour.format(formatter);
            
            LocalDateTime debutJour = jour.atStartOfDay();
            LocalDateTime finJour = jour.plusDays(1).atStartOfDay().minusSeconds(1);
            
            long count = this.countValideesByDateBetween(debutJour, finJour);
            result.put(jourKey, count);
        }
        return result;
    }
    
    
//La methode qui filtre les commandes selon leur critère caractéritiques
	public List<Commande> getCommandesFiltrer(String statut, 
			                                   String client,
			                                   String dateMin,
			                                   String dateMax,
			                                    Double montantMin, 
			                                    Double montantMax, 
			                                    String sort)
	{
		
		 List<Commande> commandes = this.toutesLesCommandes();
	        
	        // Filtre par statut
	        if (statut != null && !statut.isEmpty() && !statut.equals("all")) {
	            commandes = commandes.stream()
	                    .filter(c -> c.getStatut().toString().equals(statut))
	                    .collect(Collectors.toList());
	        }
	        
	        // Filtre par client
	        if (client != null && !client.isEmpty()) {
	            commandes = commandes.stream()
	                    .filter(c -> c.getClient().getNomComplet().toLowerCase().contains(client.toLowerCase()) ||
	                                 c.getClient().getEmail().toLowerCase().contains(client.toLowerCase()))
	                    .collect(Collectors.toList());
	        }
	        
	        // Filtre par date min
	        if (dateMin != null && !dateMin.isEmpty()) {
	            LocalDate minDate = LocalDate.parse(dateMin);
	            commandes = commandes.stream()
	                    .filter(c -> c.getDateCommande().toLocalDate().isAfter(minDate.minusDays(1)))
	                    .collect(Collectors.toList());
	        }
	        
	        // Filtre par date max
	        if (dateMax != null && !dateMax.isEmpty()) {
	            LocalDate maxDate = LocalDate.parse(dateMax);
	            commandes = commandes.stream()
	                    .filter(c -> c.getDateCommande().toLocalDate().isBefore(maxDate.plusDays(1)))
	                    .collect(Collectors.toList());
	        }
	        
	        // Filtre par montant min
	        if (montantMin != null) {
	            commandes = commandes.stream()
	                    .filter(c -> c.getTotal().doubleValue() >= montantMin)
	                    .collect(Collectors.toList());
	        }
	        
	        // Filtre par montant max
	        if (montantMax != null) {
	            commandes = commandes.stream()
	                    .filter(c -> c.getTotal().doubleValue() <= montantMax)
	                    .collect(Collectors.toList());
	        }
	        
	        // Tri
	        if (sort != null) {
	            switch (sort) {
	                case "date_asc":
	                    commandes.sort(Comparator.comparing(Commande::getDateCommande));
	                    break;
	                case "date_desc":
	                    commandes.sort(Comparator.comparing(Commande::getDateCommande).reversed());
	                    break;
	                case "total_asc":
	                    commandes.sort(Comparator.comparing(Commande::getTotal));
	                    break;
	                case "total_desc":
	                    commandes.sort(Comparator.comparing(Commande::getTotal).reversed());
	                    break;
	                case "client_asc":
	                    commandes.sort(Comparator.comparing(c -> c.getClient().getNomComplet()));
	                    break;
	                case "client_desc":
	                    commandes.sort(Comparator.comparing(c -> c.getClient().getNomComplet(), Comparator.reverseOrder()));
	                    break;
	                default:
	                    commandes.sort(Comparator.comparing(Commande::getDateCommande).reversed());
	                    break;
	            }
	        } else {
	            commandes.sort(Comparator.comparing(Commande::getDateCommande).reversed());
	        }
		// TODO Auto-generated method stub
		return commandes;
	}
}