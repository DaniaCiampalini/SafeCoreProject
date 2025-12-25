package com.safecore.business.service;

public interface PasswordResetService {

    String requestReset(String email);

    void resetPassword(String email, String token, String newPassword);
}
