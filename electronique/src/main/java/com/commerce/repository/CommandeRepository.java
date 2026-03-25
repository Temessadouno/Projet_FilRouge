package com.commerce.repository;

import com.commerce.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Integer> {
    List<Commande> findByClientIdOrderByDateCommandeDesc(Integer clientId);
    List<Commande> findAllByOrderByDateCommandeDesc();
}