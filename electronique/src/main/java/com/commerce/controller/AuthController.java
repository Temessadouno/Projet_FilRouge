package com.commerce.controller;

import com.commerce.model.Utilisateur;
import com.commerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;  // ← Vérifie que c'est bien présent

    @GetMapping("/login")
    public String login(@RequestParam(required=false) String error,
                        @RequestParam(required=false) String logout,
                        Model model) {
        if (error != null) model.addAttribute("erreur", "Email ou mot de passe incorrect");
        if (logout != null) model.addAttribute("message", "Déconnexion réussie");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm() { return "auth/register"; }

    @PostMapping("/register")
    public String register(@RequestParam String nom, @RequestParam String prenom,
                           @RequestParam String email, @RequestParam String motDePasse,
                           RedirectAttributes ra) {
        try {
            authService.inscrire(nom, prenom, email, motDePasse);
            ra.addFlashAttribute("success", "Compte créé, connectez-vous !");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/auth/register";
        }
    }
    
    // ============================================================
    // MOT DE PASSE OUBLIÉ
    // ============================================================

    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {
        model.addAttribute("email", "");
        model.addAttribute("showResetForm", false);
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model, RedirectAttributes ra) {
        try {
            Utilisateur user = authService.findByEmail(email);
            
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
            authService.updateUser(user);
            
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            model.addAttribute("showResetForm", true);
            model.addAttribute("message", "Email vérifié. Veuillez choisir un nouveau mot de passe.");
            
            return "auth/forgot-password";
            
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", "Cet email n'est pas associé à un compte.");
            return "redirect:/auth/forgot-password";
        }
    }

    @PostMapping("/reset-password-direct")
    public String resetPasswordDirect(@RequestParam String token,
                                      @RequestParam String motDePasse,
                                      @RequestParam String email,
                                      RedirectAttributes ra) {
        System.out.println("=== RESET PASSWORD DIRECT ===");
        System.out.println("Token: " + token);
        System.out.println("Email: " + email);
        
        Utilisateur user = authService.findByResetToken(token);
        
        if (user == null) {
            System.out.println("❌ Utilisateur non trouvé");
            ra.addFlashAttribute("erreur", "La session a expiré. Veuillez recommencer.");
            return "redirect:/auth/forgot-password";
        }
        
        System.out.println("✅ Utilisateur trouvé: " + user.getEmail());
        
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            System.out.println("❌ Token expiré");
            ra.addFlashAttribute("erreur", "La session a expiré. Veuillez recommencer.");
            return "redirect:/auth/forgot-password";
        }
        
        if (!user.getEmail().equals(email)) {
            System.out.println("❌ Email ne correspond pas");
            ra.addFlashAttribute("erreur", "Erreur de vérification. Veuillez recommencer.");
            return "redirect:/auth/forgot-password";
        }
        
        String encodedPassword = passwordEncoder.encode(motDePasse);
        System.out.println("✅ Nouveau hash BCrypt: " + encodedPassword);
        
        user.setMotDePasseHash(encodedPassword);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        authService.updateUser(user);
        
        ra.addFlashAttribute("message", "Votre mot de passe a été réinitialisé avec succès !");
        return "redirect:/auth/login";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model, RedirectAttributes ra) {
        Utilisateur user = authService.findByResetToken(token);
        
        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            ra.addFlashAttribute("erreur", "Le lien de réinitialisation est invalide ou a expiré.");
            return "redirect:/auth/forgot-password";
        }
        
        model.addAttribute("token", token);
        model.addAttribute("email", user.getEmail());
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String motDePasse,
                                RedirectAttributes ra) {
        Utilisateur user = authService.findByResetToken(token);
        
        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            ra.addFlashAttribute("erreur", "Le lien de réinitialisation est invalide ou a expiré.");
            return "redirect:/auth/forgot-password";
        }
        
        // ✅ Correction : encoder avec BCrypt
        String encodedPassword = passwordEncoder.encode(motDePasse);
        System.out.println("✅ Nouveau hash BCrypt: " + encodedPassword);
        
        user.setMotDePasseHash(encodedPassword);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        authService.updateUser(user);
        
        ra.addFlashAttribute("message", "Votre mot de passe a été réinitialisé avec succès. Connectez-vous !");
        return "redirect:/auth/login";
    }
}