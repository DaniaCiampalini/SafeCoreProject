package com.safecore.business.domain;

/**
 * Rappresenta il risultato di un controllo di sicurezza sul vault.
 * È un semplice DTO, senza logica di business.
 */
public record AuditResult(
        int score,
        int weakPasswords,
        int oldPasswords,
        int reusedPasswords,
        int totalPasswords
) {
}
