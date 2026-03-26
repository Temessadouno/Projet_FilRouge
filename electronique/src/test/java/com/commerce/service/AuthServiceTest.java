package com.commerce.service;

import com.commerce.model.Utilisateur;
import com.commerce.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires — AuthService")
class AuthServiceTest {

    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1);
        utilisateur.setNom("Alami");
        utilisateur.setPrenom("Youssef");
        utilisateur.setEmail("youssef@aql.ma");
        utilisateur.setMotDePasseHash("$2a$10$hashed");
        utilisateur.setRole(Utilisateur.Role.CLIENT);
        utilisateur.setActif(true);
    }

    // ── inscrire ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-A-01 : inscrire() crée un nouvel utilisateur CLIENT avec mot de passe haché")
    void inscrire_creUtilisateur_avecMotDePasseHache() {
        when(utilisateurRepository.existsByEmail("youssef@aql.ma")).thenReturn(false);
        when(passwordEncoder.encode("motdepasse123")).thenReturn("$2a$10$hashed");
        when(utilisateurRepository.save(any())).thenReturn(utilisateur);

        Utilisateur result = authService.inscrire("Alami", "Youssef", "youssef@aql.ma", "motdepasse123");

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());

        Utilisateur saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("youssef@aql.ma");
        assertThat(saved.getRole()).isEqualTo(Utilisateur.Role.CLIENT);
        assertThat(saved.isActif()).isTrue();
        assertThat(saved.getMotDePasseHash()).isEqualTo("$2a$10$hashed");
        verify(passwordEncoder).encode("motdepasse123");
    }

    @Test
    @DisplayName("UT-A-02 : inscrire() lève une exception si l'email est déjà utilisé")
    void inscrire_leveException_siEmailDejaUtilise() {
        when(utilisateurRepository.existsByEmail("youssef@aql.ma")).thenReturn(true);

        assertThatThrownBy(() -> authService.inscrire("Alami", "Youssef", "youssef@aql.ma", "mdp"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email déjà utilisé");

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-A-03 : inscrire() ne donne jamais le rôle ADMIN automatiquement")
    void inscrire_neDonneJamaisRoleAdmin() {
        when(utilisateurRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Utilisateur result = authService.inscrire("Test", "User", "test@test.ma", "mdp");

        assertThat(result.getRole()).isEqualTo(Utilisateur.Role.CLIENT);
        assertThat(result.getRole()).isNotEqualTo(Utilisateur.Role.ADMIN);
    }

    // ── findByEmail ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("UT-A-04 : findByEmail() retourne l'utilisateur si l'email existe")
    void findByEmail_retourneUtilisateur_siEmailExistant() {
        when(utilisateurRepository.findByEmail("youssef@aql.ma")).thenReturn(Optional.of(utilisateur));

        Utilisateur result = authService.findByEmail("youssef@aql.ma");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("youssef@aql.ma");
    }

    @Test
    @DisplayName("UT-A-05 : findByEmail() lève une exception si l'email est introuvable")
    void findByEmail_leveException_siEmailInexistant() {
        when(utilisateurRepository.findByEmail("inconnu@aql.ma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.findByEmail("inconnu@aql.ma"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }
}