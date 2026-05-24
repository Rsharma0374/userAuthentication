package com.guardianservices.userAuthentication.application.command;

import java.util.UUID;

/**
 * Command object for user logout
 */
public record LogoutCommand(
    UUID userId,
    String refreshToken,
    String sessionId
) {}
