package com.userAuthentication.application.command;

import com.userAuthentication.domain.model.Product;

/**
 * Command object for user registration
 */
public record RegisterCommand(
    String username,
    String email,
    String password,
    String firstName,
    String lastName,
    Product product
) {}
