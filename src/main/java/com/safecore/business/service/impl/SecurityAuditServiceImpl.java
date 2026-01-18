package com.safecore.business.service.impl;

import com.safecore.business.domain.AuditResult;
import com.safecore.business.service.SecurityAuditService;
import com.safecore.business.service.VaultService;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.security.PasswordStrengthEvaluator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione del servizio di audit della sicurezza.
 * Analizza le password salvate per identificare debolezze.
 */

@Service
public class SecurityAuditServiceImpl implements SecurityAuditService {

    private final VaultService vaultService;
    private final PasswordStrengthEvaluator strengthEvaluator;

    public SecurityAuditServiceImpl(VaultService vaultService,
                                    PasswordStrengthEvaluator strengthEvaluator) {
        this.vaultService = vaultService;
        this.strengthEvaluator = strengthEvaluator;
    }

    @Override
    public AuditResult runAudit() {
        List<PasswordEntryEntity> entries = vaultService.getEntriesForCurrentUser();
        if (entries.isEmpty()) {
            return new AuditResult(100, 0, 0, 0, 0);
        }

        // Decifriamo una sola volta per risparmiare risorse
        List<String> decryptedPasswords = entries.stream()
                .map(e -> vaultService.decryptPassword(e.getEncryptedPassword()))
                .toList();

        long weakCount = decryptedPasswords.stream()
                .filter(p -> strengthEvaluator.evaluate(p) == PasswordStrengthEvaluator.Strength.WEAK)
                .count();

        long oldCount = entries.stream()
                .filter(e -> e.getCreatedAt().isBefore(LocalDateTime.now().minusYears(1)))
                .count();

        // Calcolo duplicati raggruppando per valore
        long reusedCount = decryptedPasswords.stream()
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()))
                .values().stream()
                .filter(count -> count > 1)
                .mapToLong(Long::longValue)
                .sum();

        int score = calculateScore((int) weakCount, (int) oldCount, (int) reusedCount);

        return new AuditResult(
                score,
                (int) weakCount,
                (int) oldCount,
                (int) reusedCount,
                entries.size()
        );
    }

    private int calculateScore(int weak, int old, int reused) {
        int score = 100 - (weak * 10) - (old * 5) - (reused * 15);
        return Math.max(score, 0);
    }
}