package com.safecore.business.service;

import com.safecore.business.domain.AuditResult;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.security.PasswordStrengthEvaluator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analizza lo stato di sicurezza del vault dell’utente corrente.
 * Valuta password deboli, riutilizzate e obsolete e restituisce un punteggio complessivo.
 */
@Service
public class SecurityAuditService {

    private final VaultService vaultService;
    private final PasswordStrengthEvaluator strengthEvaluator;

    public SecurityAuditService(VaultService vaultService,
                                PasswordStrengthEvaluator strengthEvaluator) {
        this.vaultService = vaultService;
        this.strengthEvaluator = strengthEvaluator;
    }

    /**
     * Esegue un audit completo sul vault dell’utente loggato.
     */
    public AuditResult runAudit() {
        List<PasswordEntryEntity> entries = vaultService.getEntriesForCurrentUser();

        Map<String, Integer> passwordUsage = new HashMap<>();
        int weakPasswords = 0;
        int oldPasswords = 0;
        int reusedPasswords = 0;

        for (PasswordEntryEntity entry : entries) {
            String decrypted = vaultService.decryptPassword(entry.getEncryptedPassword());

            if (strengthEvaluator.evaluate(decrypted)
                    == PasswordStrengthEvaluator.Strength.WEAK) {
                weakPasswords++;
            }

            if (entry.getCreatedAt()
                    .isBefore(LocalDateTime.now().minusYears(1))) {
                oldPasswords++;
            }

            passwordUsage.merge(decrypted, 1, Integer::sum);
        }

        for (int count : passwordUsage.values()) {
            if (count > 1) reusedPasswords += count;
        }

        int score = calculateScore(weakPasswords, oldPasswords, reusedPasswords);

        return new AuditResult(
                score,
                weakPasswords,
                oldPasswords,
                reusedPasswords,
                entries.size()
        );
    }

    /**
     * Calcola il punteggio complessivo di sicurezza (0–100).
     */
    private int calculateScore(int weak, int old, int reused) {
        int score = 100;
        score -= weak * 10;
        score -= old * 5;
        score -= reused * 15;
        return Math.max(score, 0);
    }
}
