package com.safecore.business.service;

import com.safecore.model.PasswordEntry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordService {
    void addCredential(String service, String username, String plainPassword);
    String getDecryptedPassword(PasswordEntry entry);
    List<PasswordEntry> getAllEntries();
    void deleteEntry(UUID id);
}