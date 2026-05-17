package com.userAuthentication.domain.port.in;

import com.userAuthentication.application.command.EnableMfaCommand;
import com.userAuthentication.application.command.VerifyMfaCommand;

import java.util.UUID;

/**
 * MFA use case interface
 * Defines multi-factor authentication operations
 */
public interface MfaUseCase {

    /**
     * Enables MFA for user
     *
     * @param command enable MFA command
     * @return MFA setup response with secret key
     */
    MfaSetupResponse enableMfa(EnableMfaCommand command);

    /**
     * Verifies MFA code
     *
     * @param command verify MFA command
     * @return true if code is valid
     */
    boolean verifyMfaCode(VerifyMfaCommand command);

    /**
     * Disables MFA for user
     *
     * @param userId user ID
     */
    void disableMfa(UUID userId);

    /**
     * MFA setup response interface
     */
    interface MfaSetupResponse {
        String getSecretKey();
        String getQrCodeUri();
    }
}
