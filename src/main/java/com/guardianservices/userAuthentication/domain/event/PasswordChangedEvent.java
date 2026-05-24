package com.guardianservices.userAuthentication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a user changes their password
 */
public record PasswordChangedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID userId,
        String username,
        boolean wasReset
) implements DomainEvent {

    public PasswordChangedEvent(UUID userId, String username, boolean wasReset) {
        this(UUID.randomUUID(), LocalDateTime.now(), userId, username, wasReset);
    }
}
