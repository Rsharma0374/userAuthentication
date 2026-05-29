package com.guardianservices.userAuthentication.application.command;

import com.guardianservices.userAuthentication.domain.model.Product;

/**
 * Command object for user login
 */
public record LoginCommand(
    String username,
    String password,
    String ipAddress,
    String userAgent,
    String product
) {}
