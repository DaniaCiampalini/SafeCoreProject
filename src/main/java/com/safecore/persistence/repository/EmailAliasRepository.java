package com.safecore.persistence.repository;

import com.safecore.persistence.entity.EmailAliasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailAliasRepository extends JpaRepository<EmailAliasEntity, UUID> {
    List<EmailAliasEntity> findByUserEmail(String email);
}
