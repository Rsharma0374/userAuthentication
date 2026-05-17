package com.userAuthentication.application.command;

/**
 * Command object for password reset
 */
public record ResetPasswordCommand(
    String email,
    String resetToken,
    String newPassword
) {}
