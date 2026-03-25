package com.commerce.service;

import com.commerce.model.Utilisateur;
import com.commerce.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public Utilisateur inscrire(String nom, String prenom, String email, String motDePasse) {
        if (utilisateurRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email déjà utilisé");
        Utilisateur u = new Utilisateur();
        u.setNom(nom); u.setPrenom(prenom); u.setEmail(email);
        u.setMotDePasseHash(passwordEncoder.encode(motDePasse));
        u.setRole(Utilisateur.Role.CLIENT);
        u.setActif(true);
        return utilisateurRepository.save(u);
    }

    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }
}