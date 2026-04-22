package com.commerce.repository;

import com.commerce.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Integer> {
    List<Commande> findByClientIdOrderByDateCommandeDesc(Integer clientId);
    List<Commande> findAllByOrderByDateCommandeDesc();
    
    @Query("SELECT COUNT(c) FROM Commande c WHERE c.dateCommande BETWEEN :debut AND :fin")
    long countByDateCommandeBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(c) FROM Commande c WHERE c.statut = :statut AND c.dateCommande BETWEEN :debut AND :fin")
    long countByStatutAndDateCommandeBetween(@Param("statut") Commande.Statut statut, 
                                              @Param("debut") LocalDateTime debut, 
                                              @Param("fin") LocalDateTime fin);
}