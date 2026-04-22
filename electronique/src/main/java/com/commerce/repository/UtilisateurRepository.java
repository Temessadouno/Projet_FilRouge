package com.commerce.repository;

import com.commerce.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.createdAt BETWEEN :debut AND :fin")
    long countByCreatedAtBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    
    Optional<Utilisateur> findByResetToken(String resetToken);
}