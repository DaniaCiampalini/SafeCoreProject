package com.safecore.business.service;

import com.safecore.business.domain.PasswordEntry;

import java.util.List;
import java.util.UUID;

/**
 * Interfaccia per la gestione delle password nel vault.
 * Fornisce metodi per aggiungere, recuperare, elencare ed eliminare password.
 */

public interface PasswordService {

    void addCredential(String service, String username, String plainPassword);

    String getDecryptedPassword(PasswordEntry entry);

    List<PasswordEntry> getAllEntries();

    void deleteEntry(UUID id);
}