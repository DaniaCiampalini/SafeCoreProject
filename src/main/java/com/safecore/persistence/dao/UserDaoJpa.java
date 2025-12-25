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
 * - Gestione dell'accesso ai dati utente
 * - Gestione esplicita di EntityManager e transazioni
 * - Conversione Domain ↔ Entity tramite Mapper
 *
 * Motivazione di Ingegneria del Software:
 * - Il Service Layer non deve conoscere JPA né EntityManager
 * - Le query sono centralizzate e isolate
 * - Favorisce testabilità e manutenibilità
 */
public class UserDaoJpa implements UserDao {

    /**
     * Salva un nuovo utente nel database.
     */
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
            throw e; // Propagazione controllata verso il Service
        } finally {
            em.close(); // Chiusura sempre garantita
        }
    }

    /**
     * Recupera un utente tramite email.
     *
     * @param email email dell'utente
     * @return Optional<User> se presente
     */
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

    /**
     * Verifica se esiste un utente registrato con una data email.
     *
     * Metodo ottimizzato: usa COUNT per evitare il caricamento di entità.
     */
    @Override
    public boolean existsByEmail(String email) {

        EntityManager em = JpaUtil.getEntityManager();

        try {
            Long count = em.createQuery(
                            "SELECT COUNT(u) FROM UserEntity u WHERE u.email = :email",
                            Long.class
                    )
                    .setParameter("email", email)
                    .getSingleResult();

            return count > 0;

        } finally {
            em.close();
        }
    }

    /**
     * Aggiorna la password di un utente esistente.
     *
     * Usato nel flusso di reset password.
     *
     * @param email email dell'utente
     * @param hashedPassword nuova password già hashata
     */
    @Override
    public void updatePassword(String email, String hashedPassword) {

        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            em.createQuery(
                            "UPDATE UserEntity u SET u.passwordHash = :pwd WHERE u.email = :email"
                    )
                    .setParameter("pwd", hashedPassword)
                    .setParameter("email", email)
                    .executeUpdate();

            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;

        } finally {
            em.close();
        }
    }
}
