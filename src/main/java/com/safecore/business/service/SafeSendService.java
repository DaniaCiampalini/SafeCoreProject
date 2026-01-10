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

    @Transactional
    public String createSafeLink(String content, int expirationHours, boolean oneTime) {
        String email = SessionContext.getCurrentUserEmail();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        byte[] encrypted = encryptionStrategy.encrypt(content);

        SafeSendEntry entry = new SafeSendEntry();
        entry.setEncryptedContent(encrypted);
        entry.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));
        entry.setOneTime(oneTime);
        entry.setCreator(user);

        SafeSendEntry saved = safeSendRepository.save(entry);
        
        // In una app reale, qui restituiremmo un URL completo. 
        // Per questa demo, restituiamo l'ID del link.
        return "https://safecore.io/send/" + saved.getId().toString();
    }

    @Transactional
    public String accessSafeLink(UUID id) {
        SafeSendEntry entry = safeSendRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Link non trovato o scaduto"));

        if (entry.getExpiresAt().isBefore(LocalDateTime.now())) {
            safeSendRepository.delete(entry);
            throw new RuntimeException("Link scaduto");
        }

        String decrypted = encryptionStrategy.decrypt(entry.getEncryptedContent());

        if (entry.isOneTime()) {
            safeSendRepository.delete(entry);
        } else {
            entry.setAccessCount(entry.getAccessCount() + 1);
            safeSendRepository.save(entry);
        }

        return decrypted;
    }
}
