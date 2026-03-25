package com.commerce.model;



import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "commande")
public class Commande {

	    public enum Statut { EN_COURS, VALIDEE, ANNULEE }

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer id;

	    @ManyToOne(fetch = FetchType.EAGER)
	    @JoinColumn(name = "client_id", nullable = false)
	    private Utilisateur client;

	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false, length = 10)
	    private Statut statut = Statut.EN_COURS;

	    @Column(name = "adresse_livraison", nullable = false, columnDefinition = "TEXT")
	    private String adresseLivraison;

	    @Column(nullable = false, precision = 10, scale = 2)
	    private BigDecimal total = BigDecimal.ZERO;

	    @Column(name = "date_commande", updatable = false)
	    private LocalDateTime dateCommande;

	    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	    private List<LigneCommande> lignes = new ArrayList<>();
	    
	    public void addLigne(LigneCommande ligne) {
	        this.lignes.add(ligne);
	        ligne.setCommande(this);
	        this.calculerTotal(); // On recalcule automatiquement
	    }


	    @PrePersist
	    protected void onCreate() {
	        dateCommande = LocalDateTime.now();
	    }
	    public void calculerTotal() {
	        BigDecimal nouveauTotal = BigDecimal.ZERO;
	        
	        if (this.lignes != null) {
	            for (LigneCommande ligne : this.lignes) {
	                if (ligne.getPrixUnitaire() != null) {
	                    BigDecimal sousTotal = ligne.getPrixUnitaire().multiply(BigDecimal.valueOf(ligne.getQuantite()));
	                    nouveauTotal = nouveauTotal.add(sousTotal);
	                }
	            }
	        }
	        
	        this.total = nouveauTotal;
	    }
	    



	    public boolean isDefinitive() {
	        return Statut.VALIDEE.equals(this.statut);
	    }

	    
	    public Date getDateCommandeAsDate() {
	        if (dateCommande == null) return null;
	        return Date.from(dateCommande.atZone(ZoneId.systemDefault()).toInstant());
	    }

	    @Override
	    public String toString() {
	        return "Commande{id=" + id + ", statut=" + statut + ", total=" + total + "}";
	    }


		public void setStatut(Statut nouveauStatut) {
			
			
			// TODO Auto-generated method stub
			this.statut=nouveauStatut;
			
		}


		public Object getStatut() {
			// TODO Auto-generated method stub
			return this.statut;
		}

}
