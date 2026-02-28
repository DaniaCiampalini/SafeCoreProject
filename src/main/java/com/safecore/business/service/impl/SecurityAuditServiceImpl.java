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
            return new AuditResult(100, 0, 0, 0, 0, List.of(), List.of(), List.of());
        }

        // Mappa entry -> password decifrata
        List<PasswordEntryEntity> entriesWithDecrypted = entries.stream()
                .filter(e -> e.getEncryptedPassword() != null)
                .toList();

        // Identifica password deboli con i loro servizi
        List<String> weakPasswordServices = entriesWithDecrypted.stream()
                .filter(e -> {
                    String decrypted = vaultService.decryptPassword(e.getEncryptedPassword());
                    return decrypted != null && strengthEvaluator.evaluate(decrypted) == PasswordStrengthEvaluator.Strength.WEAK;
                })
                .map(PasswordEntryEntity::getServiceName)
                .toList();

        int weakCount = weakPasswordServices.size();

        // Identifica password vecchie (> 1 anno)
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        List<String> oldPasswordServices = entries.stream()
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().isBefore(oneYearAgo))
                .map(PasswordEntryEntity::getServiceName)
                .toList();

        int oldCount = oldPasswordServices.size();

        // Identifica password replicate con i loro servizi
        List<String> decryptedPasswords = entriesWithDecrypted.stream()
                .map(e -> vaultService.decryptPassword(e.getEncryptedPassword()))
                .filter(p -> p != null)
                .toList();

        // Mappa password -> lista di servizi che la usano
        var passwordToServices = new java.util.HashMap<String, List<String>>();
        for (PasswordEntryEntity entry : entriesWithDecrypted) {
            String decrypted = vaultService.decryptPassword(entry.getEncryptedPassword());
            if (decrypted != null) {
                passwordToServices.computeIfAbsent(decrypted, k -> new java.util.ArrayList<>())
                        .add(entry.getServiceName());
            }
        }

        // Trova servizi con password replicate (password usata > 1 volta)
        List<String> reusedPasswordServices = passwordToServices.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .flatMap(e -> e.getValue().stream())
                .distinct()
                .toList();

        int reusedCount = reusedPasswordServices.size();

        double score = calculateScore(weakCount, oldCount, reusedCount, entries.size());

        return new AuditResult(score, weakCount, oldCount, reusedCount, entries.size(),
                weakPasswordServices, reusedPasswordServices, oldPasswordServices);
    }

    private double calculateScore(int weak, int old, int reused, int total) {
        if (total == 0) return 100.0;

        // Calcolo penalità percentuali basate sul totale
        double weakPenalty = (weak * 100.0 / total) * 0.5;      // 50% del peso se tutte deboli
        double oldPenalty = (old * 100.0 / total) * 0.25;       // 25% del peso se tutte vecchie
        double reusedPenalty = (reused * 100.0 / total) * 0.25; // 25% del peso se tutte replicate

        double score = 100.0 - weakPenalty - oldPenalty - reusedPenalty;
        return Math.max(Math.round(score * 10.0) / 10.0, 0.0); // Arrotonda a 1 decimale
    }
}