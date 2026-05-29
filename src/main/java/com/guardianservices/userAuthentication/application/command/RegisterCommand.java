package com.guardianservices.userAuthentication.application.command;

import com.guardianservices.userAuthentication.domain.model.Product;

import java.util.Set;

/**
 * Command object for user registration
 */
public record RegisterCommand(
    String username,
    String email,
    String password,
    String firstName,
    String lastName,
    String product
) {}
