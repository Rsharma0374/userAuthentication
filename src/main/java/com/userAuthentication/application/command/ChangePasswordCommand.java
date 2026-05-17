package com.userAuthentication.application.command;

import java.util.UUID;

/**
 * Command object for password change
 */
public record ChangePasswordCommand(
    UUID userId,
    String currentPassword,
    String newPassword
) {}
