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

    private final UserDao userDao;
    private final PasswordResetTokenDao tokenDao;

    public PasswordResetServiceImpl(
            UserDao userDao,
            PasswordResetTokenDao tokenDao
    ) {
        this.userDao = userDao;
        this.tokenDao = tokenDao;
    }

    @Override
    public String requestReset(String email) {

        if (userDao.findByEmail(email).isEmpty()) {
            throw new IllegalArgumentException("Email not registered");
        }

        String token = generateToken();
        String tokenHash = PasswordHasher.hash(token);

        PasswordResetTokenEntity entity =
                new PasswordResetTokenEntity(
                        email,
                        tokenHash,
                        LocalDateTime.now().plusMinutes(15)
                );

        tokenDao.save(entity);

        return token; // simulazione email
    }

    @Override
    public void resetPassword(String email, String token, String newPassword) {

        PasswordResetTokenEntity stored =
                tokenDao.findValidTokenByEmail(email);

        if (stored == null ||
                !PasswordHasher.verify(token, stored.getTokenHash())) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        userDao.updatePassword(email, PasswordHasher.hash(newPassword));

        stored.markUsed();
        tokenDao.update(stored);
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
