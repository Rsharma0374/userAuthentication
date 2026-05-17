package com.userAuthentication.application.command;

/**
 * Command object for user registration
 */
public record RegisterCommand(
    String username,
    String email,
    String password,
    String firstName,
    String lastName
) {}
