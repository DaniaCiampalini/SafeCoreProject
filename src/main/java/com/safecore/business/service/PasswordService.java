package com.safecore.business.service;

import com.safecore.business.domain.PasswordEntry;
import java.util.List;
import java.util.UUID;

public interface PasswordService {
    void addCredential(String service, String username, String plainPassword);

    // Ora l'interfaccia e l'implementazione useranno lo stesso tipo di oggetto
    String getDecryptedPassword(PasswordEntry entry);

    List<PasswordEntry> getAllEntries();

    void deleteEntry(UUID id);
}