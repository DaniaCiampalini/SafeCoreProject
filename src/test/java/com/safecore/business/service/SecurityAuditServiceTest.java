package com.safecore.business.service;

import com.safecore.business.domain.AuditResult;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.security.PasswordStrengthEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityAuditServiceTest {

    private VaultService vaultService;
    private PasswordStrengthEvaluator evaluator;
    private SecurityAuditService auditService;

    @BeforeEach
    void setUp() {
        vaultService = mock(VaultService.class);
        evaluator = mock(PasswordStrengthEvaluator.class);
        auditService = new SecurityAuditService(vaultService, evaluator);
    }

    @Test
    void runAudit_emptyVault_returnsPerfectScore() {
        when(vaultService.getEntriesForCurrentUser()).thenReturn(List.of());

        AuditResult result = auditService.runAudit();

        assertEquals(100, result.score());
        assertEquals(0, result.weakCount());
        assertEquals(0, result.oldCount());
        assertEquals(0, result.reusedCount());
        assertEquals(0, result.totalPasswords());
    }

    @Test
    void runAudit_detectsWeakPassword() {
        PasswordEntryEntity entry = mockEntry("weak", LocalDateTime.now());

        when(vaultService.getEntriesForCurrentUser()).thenReturn(List.of(entry));
        when(vaultService.decryptPassword(any())).thenReturn("weak");
        when(evaluator.evaluate("weak"))
                .thenReturn(PasswordStrengthEvaluator.Strength.WEAK);

        AuditResult result = auditService.runAudit();

        assertEquals(90, result.score()); // -10
        assertEquals(1, result.weakCount());
    }

    @Test
    void runAudit_detectsOldPassword() {
        PasswordEntryEntity entry = mockEntry(
                "old",
                LocalDateTime.now().minusYears(2)
        );

        when(vaultService.getEntriesForCurrentUser()).thenReturn(List.of(entry));
        when(vaultService.decryptPassword(any())).thenReturn("Strong123!");
        when(evaluator.evaluate(any()))
                .thenReturn(PasswordStrengthEvaluator.Strength.STRONG);

        AuditResult result = auditService.runAudit();

        assertEquals(95, result.score()); // -5
        assertEquals(1, result.oldCount());
    }

    @Test
    void runAudit_detectsReusedPasswords() {
        PasswordEntryEntity e1 = mockEntry("same", LocalDateTime.now());
        PasswordEntryEntity e2 = mockEntry("same", LocalDateTime.now());

        when(vaultService.getEntriesForCurrentUser()).thenReturn(List.of(e1, e2));
        when(vaultService.decryptPassword(any())).thenReturn("Reuse123!");
        when(evaluator.evaluate(any()))
                .thenReturn(PasswordStrengthEvaluator.Strength.STRONG);

        AuditResult result = auditService.runAudit();

        assertEquals(70, result.score()); // -30 (2 reused)
        assertEquals(2, result.reusedCount());
    }

    // -------- helper --------

    private PasswordEntryEntity mockEntry(String pwd, LocalDateTime createdAt) {
        PasswordEntryEntity entry = mock(PasswordEntryEntity.class);
        when(entry.getEncryptedPassword()).thenReturn(pwd.getBytes());
        when(entry.getCreatedAt()).thenReturn(createdAt);
        return entry;
    }
}
