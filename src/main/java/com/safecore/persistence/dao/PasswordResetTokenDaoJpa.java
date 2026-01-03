package com.safecore.persistence.dao;

import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.persistence.util.JpaUtil;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;

/**
 * Gestione dei token per il reset password.
 * Cerchiamo solo token che: appartengono alla mail giusta, non sono già stati usati
 * e non sono ancora scaduti.
 */
public class PasswordResetTokenDaoJpa implements PasswordResetTokenDao {

    @Override
    public void save(PasswordResetTokenEntity token) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(token);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public PasswordResetTokenEntity findValidTokenByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<PasswordResetTokenEntity> query = em.createQuery(
                    "SELECT t FROM PasswordResetTokenEntity t " +
                            "WHERE t.email = :email AND t.used = false " +
                            "AND t.expiresAt > :now", PasswordResetTokenEntity.class);

            query.setParameter("email", email);
            query.setParameter("now", LocalDateTime.now());

            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(PasswordResetTokenEntity token) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(token);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}