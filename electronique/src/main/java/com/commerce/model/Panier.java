package com.commerce.model;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Panier {
	
	    private List<LigneDePanier> items = new ArrayList<>();

	    // Ajouter un produit au panier
	    public void ajouterProduit(Produit produit, int quantite) {
	        // Vérifier si le produit est déjà dans le panier
	        for (LigneDePanier item : items) {
	            if (item.getProduit().getId().equals(produit.getId())) {
	                item.setQuantite(item.getQuantite() + quantite);
	                return;
	            }
	        }
	        // Sinon, ajouter une nouvelle ligne
	        items.add(new LigneDePanier(produit, quantite));
	    }

	    // Calculer le total général du panier
	    public BigDecimal getTotalGeneral() {
	        return items.stream()
	                    .map(LigneDePanier::getTotal)
	                    .reduce(BigDecimal.ZERO, BigDecimal::add);
	    }

	    // Nombre total d'articles
	    public int getNombreArticles() {
	        return items.stream().mapToInt(LigneDePanier::getQuantite).sum();
	    }
	

}
