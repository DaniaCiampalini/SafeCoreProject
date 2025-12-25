package com.safecore.business.service;

import com.safecore.persistence.dao.PasswordResetTokenDao;
import com.safecore.persistence.dao.PasswordResetTokenDaoJpa;
import com.safecore.persistence.dao.UserDao;
import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.security.PasswordHasher;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenDao tokenDao = new PasswordResetTokenDaoJpa();
    private final UserDao userDao;
    private final PasswordHasher hasher;

    public PasswordResetServiceImpl(UserDao userDao, PasswordHasher hasher) {
        this.userDao = userDao;
        this.hasher = hasher;
    }

    @Override
    public String requestReset(String email) {
        if (!userDao.existsByEmail(email)) {
            throw new IllegalArgumentException("Email not registered");
        }

        String token = generateToken();
        String tokenHash = hasher.hash(token);

        var expiresAt = LocalDateTime.now().plusMinutes(15);

        tokenDao.save(new PasswordResetTokenEntity(email, tokenHash, expiresAt));

        return token; // simulazione email
    }

    @Override
    public void resetPassword(String email, String token, String newPassword) {
        var storedToken = tokenDao.findValidTokenByEmail(email);

        if (storedToken == null ||
                !hasher.verify(token, storedToken.getTokenHash())) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        userDao.updatePassword(email, hasher.hash(newPassword));

        storedToken.setUsed(true);
        tokenDao.update(storedToken);
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
