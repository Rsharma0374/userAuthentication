package com.guardianservices.userAuthentication.application.command;

import com.guardianservices.userAuthentication.domain.model.Product;
import java.util.UUID;

/**
 * Command object for password change
 */
public record ChangePasswordCommand(
    UUID userId,
    String username,
    String currentPassword,
    String newPassword,
    String product
) {}
