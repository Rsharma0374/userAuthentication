package com.userAuthentication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User logged in event record
 */
public record UserLoggedInEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID userId,
        String username,
        String ipAddress,
        String userAgent
) implements DomainEvent {

    public UserLoggedInEvent {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username cannot be blank");
    }

    public UserLoggedInEvent(UUID userId, String username, String ipAddress, String userAgent) {
        this(UUID.randomUUID(), LocalDateTime.now(), userId, username, ipAddress, userAgent);
    }
}