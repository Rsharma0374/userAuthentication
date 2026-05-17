package com.userAuthentication.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.userAuthentication.domain.event.*;
import com.userAuthentication.domain.port.out.AuditEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka implementation of audit event publisher
 * Publishes user management events to Kafka topics
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaAuditPublisher implements AuditEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String AUDIT_TOPIC = "uam-audit-events";

    /**
     * Publishes user logged in event
     *
     * @param event user logged in event
     */
    @Override
    public void publishUserLoggedIn(UserLoggedInEvent event) {
        publishEvent("USER_LOGGED_IN", event);
        log.info("Published UserLoggedInEvent for user: {}", event.userId());
    }

    /**
     * Publishes user logged out event
     *
     * @param event user logged out event
     */
    @Override
    public void publishUserLoggedOut(UserLoggedOutEvent event) {
        publishEvent("USER_LOGGED_OUT", event);
        log.info("Published UserLoggedOutEvent for user: {}", event.userId());
    }

    /**
     * Publishes user registered event
     *
     * @param event user registered event
     */
    @Override
    public void publishUserRegistered(UserRegisteredEvent event) {
        publishEvent("USER_REGISTERED", event);
        log.info("Published UserRegisteredEvent for user: {}", event.userId());
    }

    /**
     * Publishes token revoked event
     *
     * @param event token revoked event
     */
    @Override
    public void publishTokenRevoked(TokenRevokedEvent event) {
        publishEvent("TOKEN_REVOKED", event);
        log.info("Published TokenRevokedEvent for user: {}", event.userId());
    }

    /**
     * Publishes role assigned event
     *
     * @param event role assigned event
     */
    @Override
    public void publishRoleAssigned(RoleAssignedEvent event) {
        publishEvent("ROLE_ASSIGNED", event);
        log.info("Published RoleAssignedEvent for user: {}, role: {}",
                event.userId(), event.roleName());
    }

    /**
     * Publishes role removed event
     *
     * @param event role removed event
     */
    @Override
    public void publishRoleRemoved(RoleRemovedEvent event) {
        publishEvent("ROLE_REMOVED", event);
        log.info("Published RoleRemovedEvent for user: {}, role: {}",
                event.userId(), event.roleName());
    }

    /**
     * Publishes password changed event
     *
     * @param event password changed event
     */
    @Override
    public void publishPasswordChanged(PasswordChangedEvent event) {
        publishEvent("PASSWORD_CHANGED", event);
        log.info("Published PasswordChangedEvent for user: {}", event.userId());
    }

    /**
     * Generic method to publish events to Kafka
     *
     * @param eventType type of event
     * @param event event object
     */
    private void publishEvent(String eventType, Object event) {
        try {
            AuditEventPayload payload = AuditEventPayload.builder()
                    .eventId(UUID.randomUUID())
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .data(objectMapper.writeValueAsString(event))
                    .build();

            String message = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(AUDIT_TOPIC, eventType, message);

            log.debug("Event published to Kafka: {}", eventType);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", e.getMessage());
            throw new RuntimeException("Failed to publish audit event", e);
        }
    }
}