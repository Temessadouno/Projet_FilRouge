package com.commerce.repository;

import com.commerce.model.PanierItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PanierItemRepository extends JpaRepository<PanierItem, Integer> {
    List<PanierItem> findByClientId(Integer clientId);
    Optional<PanierItem> findByClientIdAndProduitId(Integer clientId, Integer produitId);
    void deleteByClientId(Integer clientId);
    int countByClientId(Integer clientId);
}