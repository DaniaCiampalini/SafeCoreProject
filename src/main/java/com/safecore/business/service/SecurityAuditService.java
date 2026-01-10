package com.safecore.business.service;

import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.security.PasswordStrengthEvaluator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SecurityAuditService {

    private final VaultService vaultService;
    private final PasswordStrengthEvaluator strengthEvaluator;

    public SecurityAuditService(VaultService vaultService, PasswordStrengthEvaluator strengthEvaluator) {
        this.vaultService = vaultService;
        this.strengthEvaluator = strengthEvaluator;
    }

    public AuditResult runAudit(List<PasswordEntryEntity> entries) {
        int totalScore = 100;
        int issuesCount = 0;
        Map<String, Integer> passwordUsage = new HashMap<>();
        int weakPasswords = 0;
        int oldPasswords = 0;
        int reusedPasswords = 0;

        for (PasswordEntryEntity entry : entries) {
            String decrypted = vaultService.decryptPassword(entry.getEncryptedPassword());
            
            // Check Strength
            if (strengthEvaluator.evaluate(decrypted) == PasswordStrengthEvaluator.Strength.WEAK) {
                weakPasswords++;
            }

            // Check Age (more than 1 year)
            if (entry.getCreatedAt().isBefore(LocalDateTime.now().minusYears(1))) {
                oldPasswords++;
            }

            // Check Reuse
            passwordUsage.put(decrypted, passwordUsage.getOrDefault(decrypted, 0) + 1);
        }

        for (int count : passwordUsage.values()) {
            if (count > 1) {
                reusedPasswords += count;
            }
        }

        // Penalty calculation (simple logic)
        totalScore -= (weakPasswords * 10);
        totalScore -= (oldPasswords * 5);
        totalScore -= (reusedPasswords * 15);

        if (totalScore < 0) totalScore = 0;

        return new AuditResult(totalScore, weakPasswords, oldPasswords, reusedPasswords);
    }

    public static record AuditResult(int score, int weakCount, int oldCount, int reusedCount) {}
}
