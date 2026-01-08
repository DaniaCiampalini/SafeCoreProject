package com.safecore.business.service;

import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.persistence.repository.PasswordResetTokenRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.safecore.persistence.repository.PasswordResetTokenRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;

    // Dependency Injection: Spring inietta automaticamente i Repository
    public PasswordResetServiceImpl(UserRepository userRepository, PasswordResetTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Override
    @Transactional
    public String requestReset(String email) {
        // Controllo esistenza utente tramite UserRepository
        if (!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email non registrata");
        }

        String token = generateToken();
        String tokenHash = PasswordHasher.hash(token);

        // Creazione entità token
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setEmail(email);
        entity.setTokenHash(tokenHash);
        entity.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        entity.setUsed(false);

        tokenRepository.save(entity);
        return token;
    }

    @Override
    @Transactional
    public void resetPassword(String email, String token, String newPassword) {
        // Cerchiamo un token valido nel DB
        PasswordResetTokenEntity stored = tokenRepository.findByEmailAndUsedFalse(email)
                .orElseThrow(() -> new IllegalArgumentException("Nessun token attivo trovato per questa email"));

        // Verifica scadenza
        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Il token è scaduto");
        }

        // Verifica hash del token
        if (!PasswordHasher.verify(token, stored.getTokenHash())) {
            throw new IllegalArgumentException("Token non valido");
        }

        // 1. Aggiorniamo la password dell'utente
        userRepository.updatePassword(email, PasswordHasher.hash(newPassword));

        // 2. Invalidiamo il token (Consumazione)
        stored.setUsed(true);
        tokenRepository.save(stored);

        // Grazie a @Transactional, se l'update della password fallisce,
        // il token non verrà segnato come usato!
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}