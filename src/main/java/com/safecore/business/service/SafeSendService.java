package com.safecore.business.service;

import com.safecore.persistence.entity.SafeSendEntry;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.SafeSendRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.EncryptionStrategy;
import com.safecore.security.PasswordHasher;
import com.safecore.ui.session.SessionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
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
    private final PasswordHasher passwordHasher;

    public SafeSendService(SafeSendRepository safeSendRepository,
                           UserRepository userRepository,
                           EncryptionStrategy encryptionStrategy,
                           PasswordHasher passwordHasher) {
        this.safeSendRepository = safeSendRepository;
        this.userRepository = userRepository;
        this.encryptionStrategy = encryptionStrategy;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Crea un nuovo segreto condivisibile.
     */
    @Transactional
    public String createSafeLink(String content, int expirationHours) {
        String email = SessionContext.getCurrentUserEmail();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        // Cifriamo il contenuto: anche i link temporanei sono protetti da AES
        byte[] encrypted = encryptionStrategy.encrypt(content);

        // Token usa-e-getta non reversibile, separato dall'ID interno
        String token = generateToken();
        String tokenHash = passwordHasher.hash(token);

        SafeSendEntry entry = new SafeSendEntry();
        entry.setEncryptedContent(encrypted);
        entry.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));
        entry.setOneTime(true); // i link SafeSend sono sempre monouso
        entry.setTokenHash(tokenHash);
        entry.setCreator(user);

        SafeSendEntry saved = safeSendRepository.save(entry);

        // Link realistico: ID pubblico + token segreto monouso
        return "https://safecore.io/send/" + saved.getId() + "?t=" + token;
    }

    /**
     * Tenta di leggere un segreto partendo da ID + token monouso.
     */
    @Transactional
    public String accessSafeLink(UUID id, String token) {
        SafeSendEntry entry = safeSendRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Il link non esiste più o è stato già usato."));

        // Controlliamo se è scaduto il tempo
        if (entry.getExpiresAt().isBefore(LocalDateTime.now())) {
            safeSendRepository.delete(entry);
            throw new RuntimeException("Questo link è scaduto.");
        }

        // Verifica del token monouso
        if (entry.getTokenHash() == null || !passwordHasher.verify(token, entry.getTokenHash())) {
            throw new RuntimeException("Token non valido o link manomesso.");
        }

        // Se è tutto ok, decifriamo il messaggio
        String decrypted = encryptionStrategy.decrypt(entry.getEncryptedContent());

        // Distruggiamo sempre il segreto dopo la lettura
        safeSendRepository.delete(entry);

        return decrypted;
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
