package com.guardianservices.userAuthentication.application.command;

import java.util.UUID;

/**
 * Command object for MFA verification
 */
public record VerifyMfaCommand(
    UUID userId,
    String code
) {}
