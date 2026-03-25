package com.commerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.commerce.model.Utilisateur;
import com.commerce.repository.UtilisateurRepository;

@Service
public class UtilisateurServices {
	
	
	@Autowired
    private UtilisateurRepository utilisateurRepository;

    public Utilisateur inscription(Utilisateur u) {
        // Ici, on pourra plus tard ajouter le hachage du mot de passe
        u.setRole(Utilisateur.Role.CLIENT);
        return utilisateurRepository.save(u);
    }

    public Utilisateur trouverParEmail(String email) {
        return utilisateurRepository.findByEmail(email).orElse(null);
    }

}
