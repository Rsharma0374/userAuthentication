package com.userAuthentication.infrastructure.messaging;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload for audit events sent to Kafka
 */
@Data
@Builder
public class AuditEventPayload {

    private UUID eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String data;
}
