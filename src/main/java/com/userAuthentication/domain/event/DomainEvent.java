package com.userAuthentication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base domain event as Java 21 sealed record
 */
public sealed interface DomainEvent
        permits UserLoggedInEvent, UserLoggedOutEvent, UserRegisteredEvent,
        TokenRevokedEvent, RoleAssignedEvent, RoleRemovedEvent, PasswordChangedEvent {

    UUID eventId();
    LocalDateTime occurredOn();
}
