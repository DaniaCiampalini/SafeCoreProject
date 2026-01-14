package com.safecore.business.service;

import com.safecore.business.exception.InvalidTokenException;
import com.safecore.business.exception.UserNotFoundException;
import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.persistence.repository.PasswordResetTokenRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordHasher passwordHasher; // Iniettato!
    private final PasswordResetEventPublisher eventPublisher;

    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordResetTokenRepository tokenRepository,
                                    PasswordHasher passwordHasher,
                                    PasswordResetEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public PasswordResetRequestResult requestReset(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException(email);
        }

        String token = generateToken();
        String tokenHash = passwordHasher.hash(token); // Uso dell'istanza iniettata
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setEmail(email);
        entity.setTokenHash(tokenHash);
        entity.setExpiryDate(expiresAt);
        entity.setUsed(false);

        tokenRepository.save(entity);
        return new PasswordResetRequestResult(token, expiresAt);
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

        // Pubblica l'evento solo dopo il commit della transazione per evitare inconsistenze
        PasswordResetCompletedEvent event = new PasswordResetCompletedEvent(email, LocalDateTime.now());
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publish(event);
                }
            });
        } else {
            // Se non c'è transazione attiva, pubblica immediatamente
            eventPublisher.publish(event);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}