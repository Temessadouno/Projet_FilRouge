package com.commerce.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "ligne_commande")
public class LigneCommande {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer id;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "commande_id", nullable = false)
	    private Commande commande;

	    @ManyToOne(fetch = FetchType.EAGER)
	    @JoinColumn(name = "produit_id", nullable = false)
	    private Produit produit;

	    @Column(nullable = false)
	    private int quantite;

	    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
	    private BigDecimal prixUnitaire;
	    
	    
	    public BigDecimal getPrixUnitaire() {
	        return this.prixUnitaire;
	    }
	    public LigneCommande(Produit produit, int quantite) {
	        this.produit = produit;
	        this.quantite = quantite;
	        this.prixUnitaire = produit.getPrix(); // On fige le prix du produit
	    }
	    
	  public void setCommande(Commande c) { this.commande=c;}

	    // ── Méthodes utilitaires ─────────────────────────────────
	    public BigDecimal getSousTotal() {
	        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
	    }
	    
	  public int getQuantite(){return this.quantite;}


}
