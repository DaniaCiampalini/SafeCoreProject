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

        if (entries == null || entries.isEmpty()) {
            return new AuditResult(100, 0, 0, 0, 0);
        }

        List<String> decryptedPasswords = entries.stream()
                .map(e -> vaultService.decryptPassword(e.getEncryptedPassword()))
                .filter(p -> p != null)
                .toList();

        int weakCount = (int) decryptedPasswords.stream()
                .filter(p -> strengthEvaluator.evaluate(p) == PasswordStrengthEvaluator.Strength.WEAK)
                .count();

        // Controllo date con protezione null
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        int oldCount = (int) entries.stream()
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().isBefore(oneYearAgo))
                .count();

        int reusedCount = (int) decryptedPasswords.stream()
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()))
                .values().stream()
                .filter(count -> count > 1)
                .mapToLong(count -> count)
                .sum();

        int score = calculateScore(weakCount, oldCount, reusedCount);

        return new AuditResult(score, weakCount, oldCount, reusedCount, entries.size());
    }

    private int calculateScore(int weak, int old, int reused) {
        int score = 100 - (weak * 10) - (old * 5) - (reused * 15);
        return Math.max(score, 0);
    }
}