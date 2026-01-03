package com.safecore.business.service;

import com.safecore.persistence.dao.PasswordResetTokenDao;
import com.safecore.persistence.dao.UserDao;
import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.security.PasswordHasher;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Gestisce il reset della password tramite token.
 * Ho scelto di hashare anche il token nel DB: se bucano il database,
 * l'attaccante non può comunque usare i token attivi perché vede solo gli hash!
 */
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserDao userDao;
    private final PasswordResetTokenDao tokenDao;

    public PasswordResetServiceImpl(UserDao userDao, PasswordResetTokenDao tokenDao) {
        this.userDao = userDao;
        this.tokenDao = tokenDao;
    }

    @Override
    public String requestReset(String email) {
        if (userDao.findByEmail(email).isEmpty()) {
            throw new IllegalArgumentException("Email non registrata");
        }

        String token = generateToken();
        String tokenHash = PasswordHasher.hash(token);

        // Il token scade dopo 15 minuti per sicurezza
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity(
                email,
                tokenHash,
                LocalDateTime.now().plusMinutes(15)
        );

        tokenDao.save(entity);
        return token; // In un'app reale, questo verrebbe inviato via mail
    }

    @Override
    public void resetPassword(String email, String token, String newPassword) {
        PasswordResetTokenEntity stored = tokenDao.findValidTokenByEmail(email);

        if (stored == null || !PasswordHasher.verify(token, stored.getTokenHash())) {
            throw new IllegalArgumentException("Token non valido o scaduto");
        }

        // Aggiorniamo la password dell'utente
        userDao.updatePassword(email, PasswordHasher.hash(newPassword));

        // Importante: invalidiamo il token dopo l'uso!
        stored.markUsed();
        tokenDao.update(stored);
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}