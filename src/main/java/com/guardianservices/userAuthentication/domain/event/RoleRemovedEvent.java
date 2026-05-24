package com.guardianservices.userAuthentication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a role is removed from a user
 */
public record RoleRemovedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID userId,
        String roleName,
        UUID removedBy
) implements DomainEvent {

    public RoleRemovedEvent(UUID userId, String roleName, UUID removedBy) {
        this(UUID.randomUUID(), LocalDateTime.now(), userId, roleName, removedBy);
    }
}
