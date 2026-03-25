package com.commerce.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LigneDePanier {
	

	    private Produit produit;
	    private int quantite;

	    // Calcul automatique du total pour cette ligne
	    public BigDecimal getTotal() {
	        return produit.getPrix().multiply(new BigDecimal(quantite));
	    }
	}


