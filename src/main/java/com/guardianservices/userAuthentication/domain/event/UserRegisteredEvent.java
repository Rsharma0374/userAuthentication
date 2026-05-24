package com.guardianservices.userAuthentication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User registered event record
 */
public record UserRegisteredEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID userId,
        String username,
        String email,
        String firstName,
        String lastName
) implements DomainEvent {

    public UserRegisteredEvent(UUID userId, String username, String email, String firstName, String lastName) {
        this(UUID.randomUUID(), LocalDateTime.now(), userId, username, email, firstName, lastName);
    }
}