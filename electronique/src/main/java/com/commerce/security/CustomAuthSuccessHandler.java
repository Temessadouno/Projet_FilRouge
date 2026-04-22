package com.commerce.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // Ajouter des logs pour debug
        System.out.println("=== AUTHENTIFICATION RÉUSSIE ===");
        System.out.println("Utilisateur: " + authentication.getName());
        
        // Déterminer le rôle
        String role = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse("ROLE_CLIENT");
        
        System.out.println("Rôle: " + role);
        
        // Créer une session si elle n'existe pas
        HttpSession session = request.getSession();
        session.setAttribute("userRole", role.replace("ROLE_", ""));
        session.setAttribute("estConnecter", true);
        
        // Redirection selon le rôle
        if (role.equals("ROLE_ADMIN")) {
            response.sendRedirect("/admin");
        } else {
            response.sendRedirect("/");
        }
    }
}