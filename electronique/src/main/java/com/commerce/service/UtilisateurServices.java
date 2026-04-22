package com.commerce.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.aspectj.weaver.patterns.ThisOrTargetAnnotationPointcut;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.commerce.model.Utilisateur;
import com.commerce.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UtilisateurServices {

    private final AuthService authService; // déléguer
    private final UtilisateurRepository utilisateurRepository;

    public Utilisateur inscription(String nom, String prenom,
                                   String email, String motDePasse) {
        return authService.inscrire(nom, prenom, email, motDePasse);
    }
    
    
    /**
     * Récupère le nombre d'inscriptions par mois pour les X derniers mois
     */
    public Map<String, Long> getInscriptionsParMois(int nbMois) {
        Map<String, Long> result = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);
        
        for (int i = nbMois - 1; i >= 0; i--) {
            LocalDateTime mois = now.minusMonths(i);
            String moisKey = mois.format(formatter);
            
            LocalDateTime debutMois = mois.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finMois = debutMois.plusMonths(1).minusSeconds(1);
            
            long count = utilisateurRepository.countByCreatedAtBetween(debutMois, finMois);
            result.put(moisKey, count);
        }
        return result;
    }


	public long count() {
		// TODO Auto-generated method stub
		return utilisateurRepository.count();
	}


	public  void findById(Integer id) {
		// TODO Auto-generated method stub
		this.utilisateurRepository.findById(id).ifPresent(u -> {
            u.setActif(!u.isActif());
            utilisateurRepository.save(u);
        });
		
	}


	public @Nullable Object findAll() {
		// TODO Auto-generated method stub
		return this.utilisateurRepository.findAll();
	}
    
}
