package com.commerce.security;

import com.commerce.model.Utilisateur;
import com.commerce.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + email));
        
        System.out.println("=== LOAD USER ===");
        System.out.println("Email: " + utilisateur.getEmail());
        System.out.println("Rôle: " + utilisateur.getRole());
        System.out.println("Mot de passe hashé: " + utilisateur.getMotDePasseHash());
        
        String role = "ROLE_" + utilisateur.getRole().name();
        
        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasseHash())
                .authorities(new SimpleGrantedAuthority(role))
                .build();
    }
}