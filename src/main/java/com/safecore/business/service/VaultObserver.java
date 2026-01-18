package com.safecore.business.service;

/**
 * Interfaccia per osservare i cambiamenti nel vault.
 */

public interface VaultObserver {
    void onVaultChanged();
}
