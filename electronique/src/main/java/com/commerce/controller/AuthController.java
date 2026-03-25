package com.commerce.controller;

import com.commerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}