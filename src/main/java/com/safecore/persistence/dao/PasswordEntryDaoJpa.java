package com.safecore.persistence.dao;

import com.safecore.model.PasswordEntry;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.util.JpaUtil;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementazione concreta per il salvataggio delle password.
 * Qui usiamo il Mapper per trasformare i dati e JpaUtil per la connessione.
 */
public class PasswordEntryDaoJpa implements PasswordEntryDao {

    // ... existing code ...
    @Override
    public void save(PasswordEntry entry) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // CONVERSIONE: Trasformiamo il Domain Model in Entity per Hibernate
            PasswordEntryEntity entity = PasswordMapper.toEntity(entry);

            if (entity.getId() == null) {
                em.persist(entity);
            } else {
                em.merge(entity);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
// ... existing code ...

    @Override
    public List<PasswordEntry> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM PasswordEntryEntity p", PasswordEntryEntity.class)
                    .getResultList()
                    .stream()
                    .map(PasswordMapper::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(UUID id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            PasswordEntryEntity entity = em.find(PasswordEntryEntity.class, id);
            if (entity != null) em.remove(entity);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}