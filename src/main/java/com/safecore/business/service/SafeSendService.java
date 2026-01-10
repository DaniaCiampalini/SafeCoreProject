package com.safecore.business.service;

import com.safecore.persistence.entity.SafeSendEntry;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.SafeSendRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.EncryptionStrategy;
import com.safecore.ui.session.SessionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Questo servizio permette di condividere segreti in modo "usa e getta".
 * Funziona un po' come i messaggi che si auto-distruggono: crei un link, lo mandi
 * a qualcuno e, dopo che è stato letto o dopo un certo tempo, sparisce per sempre.
 */
@Service
public class SafeSendService {

    private final SafeSendRepository safeSendRepository;
    private final UserRepository userRepository;
    private final EncryptionStrategy encryptionStrategy;

    public SafeSendService(SafeSendRepository safeSendRepository,
                           UserRepository userRepository,
                           EncryptionStrategy encryptionStrategy) {
        this.safeSendRepository = safeSendRepository;
        this.userRepository = userRepository;
        this.encryptionStrategy = encryptionStrategy;
    }

    /**
     * Crea un nuovo segreto condivisibile.
     */
    @Transactional
    public String createSafeLink(String content, int expirationHours, boolean oneTime) {
        String email = SessionContext.getCurrentUserEmail();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        // Cifriamo il contenuto: anche i link temporanei sono protetti da AES
        byte[] encrypted = encryptionStrategy.encrypt(content);

        SafeSendEntry entry = new SafeSendEntry();
        entry.setEncryptedContent(encrypted);
        entry.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));
        entry.setOneTime(oneTime);
        entry.setCreator(user);

        SafeSendEntry saved = safeSendRepository.save(entry);
        
        // Generiamo un finto link basato sull'UUID del record sul database
        return "https://safecore.io/send/" + saved.getId().toString();
    }

    /**
     * Tenta di leggere un segreto partendo dal suo ID.
     */
    @Transactional
    public String accessSafeLink(UUID id) {
        SafeSendEntry entry = safeSendRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Il link non esiste più o è stato già usato."));

        // Controlliamo se è scaduto il tempo
        if (entry.getExpiresAt().isBefore(LocalDateTime.now())) {
            safeSendRepository.delete(entry);
            throw new RuntimeException("Questo link è scaduto.");
        }

        // Se è tutto ok, decifriamo il messaggio
        String decrypted = encryptionStrategy.decrypt(entry.getEncryptedContent());

        // Se era un link "One-Time", lo cancelliamo subito dopo il primo accesso
        if (entry.isOneTime()) {
            safeSendRepository.delete(entry);
        } else {
            // Altrimenti aumentiamo solo il contatore degli accessi
            entry.setAccessCount(entry.getAccessCount() + 1);
            safeSendRepository.save(entry);
        }

        return decrypted;
    }
}
