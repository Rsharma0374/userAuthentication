package com.guardianservices.userAuthentication.application.command;

/**
 * Command object for user login
 */
public record LoginCommand(
    String username,
    String password,
    String ipAddress,
    String userAgent
) {}
