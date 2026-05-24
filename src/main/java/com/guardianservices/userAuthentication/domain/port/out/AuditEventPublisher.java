package com.guardianservices.userAuthentication.domain.port.out;

import com.guardianservices.userAuthentication.domain.event.*;
import com.guardianservices.userAuthentication.domain.event.*;

/**
 * Port for publishing audit events
 * Used for broadcasting domain events to external systems
 */
public interface AuditEventPublisher {

    /**
     * Publishes user logged in event
     *
     * @param event user logged in event
     */
    void publishUserLoggedIn(UserLoggedInEvent event);

    /**
     * Publishes user logged out event
     *
     * @param event user logged out event
     */
    void publishUserLoggedOut(UserLoggedOutEvent event);

    /**
     * Publishes user registered event
     *
     * @param event user registered event
     */
    void publishUserRegistered(UserRegisteredEvent event);

    /**
     * Publishes token revoked event
     *
     * @param event token revoked event
     */
    void publishTokenRevoked(TokenRevokedEvent event);

    /**
     * Publishes role assigned event
     *
     * @param event role assigned event
     */
    void publishRoleAssigned(RoleAssignedEvent event);

    /**
     * Publishes role removed event
     *
     * @param event role removed event
     */
    void publishRoleRemoved(RoleRemovedEvent event);

    /**
     * Publishes password changed event
     *
     * @param event password changed event
     */
    void publishPasswordChanged(PasswordChangedEvent event);
}
