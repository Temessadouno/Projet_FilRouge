package com.commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerce.model.LigneCommande;

public interface LigneCommandeRepository extends JpaRepository<LigneCommande,Integer> {

}
