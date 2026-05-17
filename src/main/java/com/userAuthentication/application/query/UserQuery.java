package com.userAuthentication.application.query;

import java.util.Optional;

/**
 * Query object for user search
 */
public record UserQuery(
    Optional<String> username,
    Optional<String> email,
    Optional<String> firstName,
    Optional<String> lastName,
    Optional<Boolean> enabled,
    Optional<Boolean> emailVerified,
    Optional<Boolean> mfaEnabled,
    Optional<Integer> page,
    Optional<Integer> size,
    Optional<String> sortBy,
    Optional<String> sortDirection
) {}
