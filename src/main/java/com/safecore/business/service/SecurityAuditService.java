package com.safecore.business.service;

import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.security.PasswordStrengthEvaluator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Questo servizio fa le "pulci" al tuo vault.
 * Analizza tutte le password salvate e ti dice quanto sei messo bene (o male) a sicurezza.
 * Controlla se hai password deboli, se le stai riusando su più siti o se sono troppo vecchie.
 */
@Service
public class SecurityAuditService {

    private final VaultService vaultService;
    private final PasswordStrengthEvaluator strengthEvaluator;

    public SecurityAuditService(VaultService vaultService, PasswordStrengthEvaluator strengthEvaluator) {
        this.vaultService = vaultService;
        this.strengthEvaluator = strengthEvaluator;
    }

    /**
     * Esegue un controllo completo e restituisce un punteggio da 0 a 100.
     */
    public AuditResult runAudit(List<PasswordEntryEntity> entries) {
        int totalScore = 100; // Partiamo dal top
        Map<String, Integer> passwordUsage = new HashMap<>();
        int weakPasswords = 0;
        int oldPasswords = 0;
        int reusedPasswords = 0;

        for (PasswordEntryEntity entry : entries) {
            // Decifriamo momentaneamente la password per analizzarla
            String decrypted = vaultService.decryptPassword(entry.getEncryptedPassword());
            
            // 1. È debole? (Mancano numeri, simboli, ecc.)
            if (strengthEvaluator.evaluate(decrypted) == PasswordStrengthEvaluator.Strength.WEAK) {
                weakPasswords++;
            }

            // 2. È vecchia? (Creata più di un anno fa)
            if (entry.getCreatedAt().isBefore(LocalDateTime.now().minusYears(1))) {
                oldPasswords++;
            }

            // 3. Contiamo quante volte appare ogni password per scovare i duplicati
            passwordUsage.put(decrypted, passwordUsage.getOrDefault(decrypted, 0) + 1);
        }

        // Calcoliamo quanti doppioni ci sono
        for (int count : passwordUsage.values()) {
            if (count > 1) {
                reusedPasswords += count;
            }
        }

        // Togliamo punti per ogni falla trovata
        totalScore -= (weakPasswords * 10);
        totalScore -= (oldPasswords * 5);
        totalScore -= (reusedPasswords * 15);

        if (totalScore < 0) totalScore = 0;

        return new AuditResult(totalScore, weakPasswords, oldPasswords, reusedPasswords);
    }

    /**
     * Un semplice contenitore per i risultati dell'audit.
     */
    public static record AuditResult(int score, int weakCount, int oldCount, int reusedCount) {}
}
