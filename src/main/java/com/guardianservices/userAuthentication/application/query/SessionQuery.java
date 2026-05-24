package com.guardianservices.userAuthentication.application.query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Query object for session search
 */
public record SessionQuery(
    Optional<UUID> userId,
    Optional<String> deviceInfo,
    Optional<String> ipAddress,
    Optional<LocalDateTime> fromDate,
    Optional<LocalDateTime> toDate,
    Optional<Boolean> isActive
) {}
