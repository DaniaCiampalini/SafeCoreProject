package com.safecore.persistence.dao;

import com.safecore.business.domain.User;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.util.JpaUtil;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Optional;

/**
 * Questa è l'implementazione concreta che parla con il database tramite JPA.
 * Nota: qui dentro gestiamo i rollback. Se il DB crasha
 * mentre scriviamo, non vogliamo lasciare l'app in uno stato inconsistente.
 * Inoltre, chiudiamo sempre l'EntityManager nel 'finally' per evitare memory leak!
 */
public class UserDaoJpa implements UserDao {

    @Override
    public void save(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            // Traduciamo l'utente di business in una entity che JPA capisce
            UserEntity entity = UserMapper.toEntity(user);
            em.persist(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e; // Rilanciamo l'errore così il Service sa che il salvataggio è fallito
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Usiamo COUNT perché è molto più leggero che scaricare tutto l'utente
            Long count = em.createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<UserEntity> query = em.createQuery(
                    "SELECT u FROM UserEntity u WHERE u.email = :email", UserEntity.class);
            query.setParameter("email", email);

            UserEntity entity = query.getResultStream().findFirst().orElse(null);
            // Trasformiamo di nuovo l'entità DB nel nostro oggetto pulito 'User'
            return Optional.ofNullable(entity).map(UserMapper::toDomain);
        } finally {
            em.close();
        }
    }

    @Override
    public void updatePassword(String email, String hashedPassword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("UPDATE UserEntity u SET u.passwordHash = :pwd WHERE u.email = :email")
                    .setParameter("pwd", hashedPassword)
                    .setParameter("email", email)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}