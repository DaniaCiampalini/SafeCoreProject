package com.safecore.business.service;

import com.safecore.business.domain.PasswordEntry;
import java.util.List;
import java.util.UUID;

/**
 * Interfaccia per la gestione delle password nel vault.
 * Definisce le azioni base: aggiungi, leggi, elenca ed elimina.
 */
public interface PasswordService {
    /**
     * Salva una nuova credenziale.
     */
    void addCredential(String service, String username, String plainPassword);

    /**
     * Prende una voce cifrata e restituisce la password in chiaro.
     */
    String getDecryptedPassword(PasswordEntry entry);

    /**
     * Elenca tutte le password salvate.
     */
    List<PasswordEntry> getAllEntries();

    /**
     * Elimina una voce dal vault.
     */
    void deleteEntry(UUID id);
}