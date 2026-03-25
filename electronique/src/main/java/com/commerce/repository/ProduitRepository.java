package com.commerce.repository;

import com.commerce.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Integer> {
	
    List<Produit> findByDeletedFalse();
    List<Produit> findByCategorieIdAndDeletedFalse(Integer categorieId);

    @Query("SELECT p FROM Produit p WHERE p.deleted = false AND " +
           "(LOWER(p.nom) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(p.marque) LIKE LOWER(CONCAT('%',:q,'%')))")
    List<Produit> search(String q);
    
	List<Produit> findByCategorieId(Integer categorieId);
}