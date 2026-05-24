package com.guardianservices.userAuthentication.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Token revoked event record
 */
public record TokenRevokedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID userId,
        String tokenId,
        String revocationReason
) implements DomainEvent {

    public TokenRevokedEvent(UUID userId, String tokenId, String revocationReason) {
        this(UUID.randomUUID(), LocalDateTime.now(), userId, tokenId, revocationReason);
    }
}
