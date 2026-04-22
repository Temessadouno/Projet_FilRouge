package com.commerce.config;


import com.commerce.model.Utilisateur;
import com.commerce.repository.UtilisateurRepository;
import com.commerce.service.PanierService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UtilisateurRepository utilisateurRepository;
    private final PanierService panierService;

    @ModelAttribute
    public void addUserToSession(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String email = userDetails.getUsername();
            
            Utilisateur user = utilisateurRepository.findByEmail(email).orElse(null);
            
            if (user != null) {
                // Stocker les infos utilisateur en session
            	session.setAttribute("estConnecter",true);
                session.setAttribute("userRole", user.getRole().name());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userNom", user.getNom());
                session.setAttribute("userPrenom", user.getPrenom());
                
                // Stocker le nombre d'articles dans le panier
                int nombreArticles = panierService.getNombreArticles(user.getId());
                session.setAttribute("nombreArticles", nombreArticles);
                
               /*
                * System.out.println("✅ Session mise à jour - Utilisateur: " + user.getEmail() + 
                                   ", Rôle: " + user.getRole().name() + 
                                   ", Panier: " + nombreArticles);*/
            }
        } else {
            // Utilisateur non connecté, nettoyer la session
            session.removeAttribute("userRole");
            session.removeAttribute("userEmail");
            session.removeAttribute("userNom");
            session.removeAttribute("userPrenom");
            session.setAttribute("nombreArticles", 0);
            session.setAttribute("estConnecter",false);
            
            //System.out.println("❌ Aucun utilisateur connecté - Session vidée");
        }
    }
}
