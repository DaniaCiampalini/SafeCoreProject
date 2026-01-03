package com.safecore.persistence.dao;

import com.safecore.model.PasswordEntry;
import java.util.List;
import java.util.UUID;

/**
 * Contratto per la persistenza delle credenziali nel Vault.
 * Nota: uso il Domain Model (PasswordEntry).
 */
public interface PasswordEntryDao {
    void save(PasswordEntry entry);
    List<PasswordEntry> findAll();
    void deleteById(UUID id);
}