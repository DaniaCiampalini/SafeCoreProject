package com.safecore.persistence.repository;

import com.safecore.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    // Spring capisce dal nome cosa deve cercare! Nessuna query manuale.
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    // Per l'update della password usiamo una piccola query JPQL
    @Modifying
    @Query("UPDATE UserEntity u SET u.passwordHash = :pwd WHERE u.email = :email")
    void updatePassword(@Param("email") String email, @Param("pwd") String hashedPassword);
}