package com.guardianservices.userAuthentication.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User entity using Java 21 features
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;

    private String keycloakId;

    private String username;

    private String email;

    private String firstName;
    private String lastName;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean emailVerified = false;

    private String role;

    private String product;

    @Builder.Default
    private Boolean mfaEnabled = false;
    private String mfaSecret;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Sealed interface for user status using Java 21 sealed classes
     */
    public sealed interface UserStatus permits ActiveStatus, InactiveStatus, SuspendedStatus {
        String getStatus();
        boolean canLogin();
    }

    public static final class ActiveStatus implements UserStatus {
        @Override
        public String getStatus() { return "ACTIVE"; }
        @Override
        public boolean canLogin() { return true; }
    }

    public static final class InactiveStatus implements UserStatus {
        @Override
        public String getStatus() { return "INACTIVE"; }
        @Override
        public boolean canLogin() { return false; }
    }

    public static final class SuspendedStatus implements UserStatus {
        @Override
        public String getStatus() { return "SUSPENDED"; }
        @Override
        public boolean canLogin() { return false; }
    }
}
