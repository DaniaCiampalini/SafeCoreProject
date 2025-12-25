package com.safecore.persistence.dao;

import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.persistence.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;

public class PasswordResetTokenDaoJpa implements PasswordResetTokenDao {

    private final EntityManager em = JpaUtil.getEntityManager();

    @Override
    public void save(PasswordResetTokenEntity token) {
        em.getTransaction().begin();
        em.persist(token);
        em.getTransaction().commit();
    }

    @Override
    public PasswordResetTokenEntity findValidTokenByEmail(String email) {
        TypedQuery<PasswordResetTokenEntity> query =
                em.createQuery("""
                    SELECT t FROM PasswordResetTokenEntity t
                    WHERE t.email = :email
                    AND t.used = false
                    AND t.expiresAt > :now
                """, PasswordResetTokenEntity.class);

        query.setParameter("email", email);
        query.setParameter("now", LocalDateTime.now());

        return query.getResultStream().findFirst().orElse(null);
    }

    @Override
    public void update(PasswordResetTokenEntity token) {
        em.getTransaction().begin();
        em.merge(token);
        em.getTransaction().commit();
    }
}
