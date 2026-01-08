package com.safecore.business.service;

import com.safecore.business.exception.InvalidTokenException; // Da creare
import com.safecore.business.exception.UserNotFoundException;  // Da creare
import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.persistence.repository.PasswordResetTokenRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordHasher passwordHasher; // Iniettato!

    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordResetTokenRepository tokenRepository,
                                    PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public String requestReset(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException(email);
        }

        String token = generateToken();
        String tokenHash = passwordHasher.hash(token); // Uso dell'istanza iniettata

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
        PasswordResetTokenEntity stored = tokenRepository.findByEmailAndUsedFalse(email)
                .orElseThrow(() -> new InvalidTokenException("Nessun token attivo trovato"));

        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Il token è scaduto");
        }

        if (!passwordHasher.verify(token, stored.getTokenHash())) {
            throw new InvalidTokenException("Token non valido");
        }

        // Hash della nuova password usando l'istanza iniettata
        userRepository.updatePassword(email, passwordHasher.hash(newPassword));

        stored.setUsed(true);
        tokenRepository.save(stored);
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}