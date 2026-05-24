package com.guardianservices.userAuthentication.application.query;

import java.util.Optional;

/**
 * Query object for role search
 */
public record RoleQuery(
    Optional<String> name,
    Optional<String> description,
    Optional<Integer> page,
    Optional<Integer> size
) {}
