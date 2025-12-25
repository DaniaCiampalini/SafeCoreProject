package com.safecore.persistence.dao;

import com.safecore.business.domain.User;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Optional;

/**
 * Implementazione JPA del UserDao.
 *
 * Responsabilità:
 * - Gestione EntityManager
 * - Gestione transazioni
 * - Conversione Domain ↔ Entity
 *
 * Motivazione SE:
 * - Il Service NON deve conoscere JPA
 */
public class UserDaoJpa implements UserDao {

    @Override
    public void save(User user) {

        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            UserEntity entity = UserMapper.toEntity(user);
            em.persist(entity);

            em.getTransaction().commit();

        } catch (Exception e) {
            // Rollback obbligatorio in caso di errore
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e; // Propagazione controllata
        } finally {
            em.close(); // Sempre chiudere
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {

        EntityManager em = JpaUtil.getEntityManager();

        try {
            TypedQuery<UserEntity> query =
                    em.createQuery(
                            "SELECT u FROM UserEntity u WHERE u.email = :email",
                            UserEntity.class);

            query.setParameter("email", email);

            UserEntity entity = query.getResultStream()
                    .findFirst()
                    .orElse(null);

            return Optional.ofNullable(entity)
                    .map(UserMapper::toDomain);

        } finally {
            em.close();
        }
    }
}
