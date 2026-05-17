package com.userAuthentication.application.command;

import java.util.UUID;

/**
 * Command object for enabling MFA
 */
public record EnableMfaCommand(UUID userId) {}
