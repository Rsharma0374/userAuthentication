package com.userAuthentication.application.command;

import java.util.UUID;

/**
 * Command object for password reset
 */
public record ResetPasswordCommand(
    String email,
    String resetToken,
    String newPassword,
    UUID userId
) {}
