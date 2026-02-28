package com.safecore.business.domain;

import java.util.List;

/**
 * Rappresenta il risultato di un controllo di sicurezza sul vault.
 * Include conteggi e liste dettagliate dei servizi con problemi di sicurezza.
 * Il punteggio è in formato decimale per maggiore precisione.
 */

public record AuditResult(
        double score,
        int weakCount,
        int oldCount,
        int reusedCount,
        int totalPasswords,
        List<String> weakPasswordServices,
        List<String> reusedPasswordServices,
        List<String> oldPasswordServices
) {
}
