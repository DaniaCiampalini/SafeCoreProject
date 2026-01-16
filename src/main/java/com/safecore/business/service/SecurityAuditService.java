package com.safecore.business.service;

import com.safecore.business.domain.AuditResult;

public interface SecurityAuditService {
    AuditResult runAudit();
}