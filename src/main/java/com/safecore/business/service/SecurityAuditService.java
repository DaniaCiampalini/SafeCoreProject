package com.safecore.business.service;

import com.safecore.business.domain.AuditResult;

/**
 * Servizio per eseguire audit di sicurezza sul vault.
 */

public interface SecurityAuditService {
    AuditResult runAudit();
}