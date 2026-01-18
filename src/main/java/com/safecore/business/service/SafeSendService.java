package com.safecore.business.service;

import java.util.UUID;

/**
 * Servizio per la funzionalità Safe Send.
 * Permette di creare link sicuri per condividere segreti monouso.
 */

public interface SafeSendService {
    /**
     * Crea un link sicuro per condividere un segreto.
     * @param content Contenuto in chiaro
     * @param expirationHours Ore prima della scadenza
     * @return URL completo con ID e token
     */
    String createSafeLink(String content, int expirationHours);

    /**
     * Accede a un segreto e lo distrugge dopo la lettura.
     * @param id UUID dell'entry
     * @param token Token segreto monouso
     * @return Contenuto decifrato
     */
    String accessSafeLink(UUID id, String token);
}