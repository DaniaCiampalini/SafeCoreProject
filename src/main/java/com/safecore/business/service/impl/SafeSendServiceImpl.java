package com.safecore.business.service.impl;

import com.safecore.business.service.SafeSendService;
import com.safecore.business.service.VaultService;
import com.safecore.persistence.entity.SafeSendEntryEntity;
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
 * Implementazione del servizio SafeSend per la condivisione sicura e temporanea di segreti.
 * Gestisce la cifratura del contenuto, la generazione di token monouso e l'autodistruzione.
 * Utilizza transazioni per garantire la coerenza dei dati.
 */

@Service
public class SafeSendServiceImpl implements SafeSendService {

    private final SafeSendRepository safeSendRepository;
    private final UserRepository userRepository;
    private final EncryptionStrategy encryptionStrategy;
    private final PasswordHasher passwordHasher;
    private final VaultService vaultService;

    public SafeSendServiceImpl(SafeSendRepository safeSendRepository,
                               UserRepository userRepository,
                               EncryptionStrategy encryptionStrategy,
                               PasswordHasher passwordHasher,
                               VaultService vaultService) {
        this.safeSendRepository = safeSendRepository;
        this.userRepository = userRepository;
        this.encryptionStrategy = encryptionStrategy;
        this.passwordHasher = passwordHasher;
        this.vaultService = vaultService;
    }

    /**
     * Crea un nuovo segreto condivisibile cifrato.
     * Al termine dell'operazione, notifica gli osservatori per aggiornare la dashboard.
     *
     * @param content il testo segreto da condividere
     * @param expirationHours ore di validità del link
     * @return URL completo contenente l'ID e il token segreto
     */
    @Override
    @Transactional
    public String createSafeLink(String content, int expirationHours) {
        String email = SessionContext.getCurrentUserEmail();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        byte[] encrypted = encryptionStrategy.encrypt(content);

        String token = generateToken();
        String tokenHash = passwordHasher.hash(token);

        SafeSendEntryEntity entry = new SafeSendEntryEntity();
        entry.setEncryptedContent(encrypted);
        entry.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));
        entry.setOneTime(true);
        entry.setTokenHash(tokenHash);
        entry.setUser(user);

        SafeSendEntryEntity saved = safeSendRepository.save(entry);

        vaultService.notifyObservers();

        // Ritorna il link completo: il token "t" è l'unica chiave per l'accesso
        return "https://safecore.io/send/" + saved.getId() + "?t=" + token;
    }

    /**
     * Tenta l'accesso a un segreto. Se il segreto viene letto, viene eliminato istantaneamente.
     * * @param id identificatore univoco del segreto
     * @param token il token di accesso monouso
     * @return il contenuto decifrato
     * @throws RuntimeException se il link è scaduto, usato o il token è errato
     */
    @Override
    @Transactional
    public String accessSafeLink(UUID id, String token) {
        SafeSendEntryEntity entry = safeSendRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Il link non esiste più o è stato già usato."));

        if (entry.getExpiresAt().isBefore(LocalDateTime.now())) {
            safeSendRepository.delete(entry);
            vaultService.notifyObservers();
            throw new RuntimeException("Questo link è scaduto.");
        }

        if (entry.getTokenHash() == null || !passwordHasher.verify(token, entry.getTokenHash())) {
            throw new RuntimeException("Token non valido o link manomesso.");
        }

        String decrypted = encryptionStrategy.decrypt(entry.getEncryptedContent());

        safeSendRepository.delete(entry);


        vaultService.notifyObservers();

        return decrypted;
    }

    /**
     * Genera un token crittograficamente sicuro codificato in Base64 URL-safe.
     */
    private String generateToken() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}