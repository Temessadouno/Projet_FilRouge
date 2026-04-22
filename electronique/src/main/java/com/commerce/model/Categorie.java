package com.commerce.model;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Cette classe permet de classer les produit par catégorie, pour faciliter les trie
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "categorie")
public class Categorie {
	


	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer id;

	    @Column(nullable = false, unique = true, length = 100)
	    private String nom;

	    @Column(columnDefinition = "TEXT")
	    private String description;

	    @Column(name = "created_at", updatable = false)
	    private LocalDateTime createdAt;

	    @OneToMany(mappedBy = "categorie", fetch = FetchType.LAZY)
	    private List<Produit> produits = new ArrayList<>();

	    @PrePersist
	    protected void onCreate() {
	        createdAt = LocalDateTime.now();
	    }
	    
	    @Transient  // Ne pas persister en base
	    private int nbProduits;


       //La methode qui rend une catégorie avec toutes son nom et son identifient
	    @Override
	    public String toString() {
	        return "Categorie{id=" + id + ", nom='" + nom + "'}";
	    }

}
