package com.guardianservices.userAuthentication.application.command;

import java.util.UUID;

/**
 * Command object for role removal
 */
public record RemoveRoleCommand(
    UUID userId,
    String roleName,
    UUID removedBy
) {}
