package com.safecore.persistence.dao;

import com.safecore.persistence.entity.PasswordResetTokenEntity;

public interface PasswordResetTokenDao {

    void save(PasswordResetTokenEntity token);

    PasswordResetTokenEntity findValidTokenByEmail(String email);

    void update(PasswordResetTokenEntity token);
}
