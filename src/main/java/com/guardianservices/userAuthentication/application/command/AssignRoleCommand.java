package com.guardianservices.userAuthentication.application.command;

import java.util.UUID;

/**
 * Command object for role assignment
 */
public record AssignRoleCommand(
    UUID userId,
    String roleName,
    UUID assignedBy
) {}
