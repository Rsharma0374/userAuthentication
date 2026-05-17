package com.userAuthentication.application.command;

/**
 * Command object for role creation
 */
public record CreateRoleCommand(
    String roleName,
    String description
) {}
