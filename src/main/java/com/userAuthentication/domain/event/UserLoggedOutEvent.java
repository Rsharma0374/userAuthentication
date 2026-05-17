package com.userAuthentication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User logged out event record
 */
public record UserLoggedOutEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID userId,
        String username,
        String sessionId
) implements DomainEvent {

    public UserLoggedOutEvent {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username cannot be blank");
    }

    public UserLoggedOutEvent(UUID userId, String username, String sessionId) {
        this(UUID.randomUUID(), LocalDateTime.now(), userId, username, sessionId);
    }
}
