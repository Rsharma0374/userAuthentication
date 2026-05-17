package com.userAuthentication.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userAuthentication.domain.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Kafka consumer for audit events
 * Listens to audit topics and processes events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {

    private final ObjectMapper objectMapper;

    /**
     * Consumes audit events from Kafka
     *
     * @param message JSON message from Kafka
     */
    @KafkaListener(topics = "uam-audit-events", groupId = "uam-audit-consumer")
    public void consumeAuditEvent(String message) {
        try {
            AuditEventPayload payload = objectMapper.readValue(message, AuditEventPayload.class);
            log.info("Received audit event: {} - {}", payload.getEventType(), payload.getEventId());

            // Process audit event (store in database, send to monitoring, etc.)
            processAuditEvent(payload);
        } catch (IOException e) {
            log.error("Failed to deserialize audit event: {}", e.getMessage());
        }
    }

    /**
     * Processes audit event based on type
     *
     * @param payload audit event payload
     */
    private void processAuditEvent(AuditEventPayload payload) {
        switch (payload.getEventType()) {
            case "USER_LOGGED_IN":
                log.debug("User login event processed");
                break;
            case "USER_LOGGED_OUT":
                log.debug("User logout event processed");
                break;
            case "USER_REGISTERED":
                log.debug("User registration event processed");
                break;
            case "TOKEN_REVOKED":
                log.debug("Token revocation event processed");
                break;
            case "ROLE_ASSIGNED":
                log.debug("Role assignment event processed");
                break;
            case "ROLE_REMOVED":
                log.debug("Role removal event processed");
                break;
            case "PASSWORD_CHANGED":
                log.debug("Password change event processed");
                break;
            default:
                log.warn("Unknown event type: {}", payload.getEventType());
        }
    }
}
