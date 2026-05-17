package com.userAuthentication.domain.model;
import jakarta.persistence.*;
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
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String keycloakId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    private Boolean enabled = true;

    private Boolean emailVerified = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    private Boolean mfaEnabled = false;
    private String mfaSecret;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
