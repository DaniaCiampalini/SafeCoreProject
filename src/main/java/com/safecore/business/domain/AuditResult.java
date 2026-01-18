package com.safecore.business.domain;

/**
 * Rappresenta il risultato di un controllo di sicurezza sul vault.
 * È un semplice DTO, senza logica di business.
 */

public record AuditResult(
        int score,
        int weakCount,
        int oldCount,
        int reusedCount,
        int totalPasswords
) {
}
