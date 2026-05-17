package com.userAuthentication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Role assigned event record
 */
public record RoleAssignedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID userId,
        String roleName,
        UUID assignedBy
) implements DomainEvent {

    public RoleAssignedEvent(UUID userId, String roleName, UUID assignedBy) {
        this(UUID.randomUUID(), LocalDateTime.now(), userId, roleName, assignedBy);
    }
}
