package com.safecore.persistence.repository;

import com.safecore.persistence.entity.EmailAliasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailAliasRepository extends JpaRepository<EmailAliasEntity, UUID> {

    @Query("SELECT e FROM EmailAliasEntity e WHERE e.user.email = :email")
    List<EmailAliasEntity> findByUserEmail(@Param("email") String email);
}
