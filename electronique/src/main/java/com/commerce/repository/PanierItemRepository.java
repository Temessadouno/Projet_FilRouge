package com.commerce.repository;

import com.commerce.model.PanierItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PanierItemRepository extends JpaRepository<PanierItem, Integer> {
    List<PanierItem> findByClientId(Integer clientId);
    Optional<PanierItem> findByClientIdAndProduitId(Integer clientId, Integer produitId);
    void deleteByClientId(Integer clientId);
    int countByClientId(Integer clientId);
    
    @Query("SELECT COUNT(p) FROM PanierItem p WHERE p.addedAt BETWEEN :debut AND :fin")
    long countByAddedAtBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}