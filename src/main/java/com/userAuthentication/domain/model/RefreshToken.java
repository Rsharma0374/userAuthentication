package com.userAuthentication.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RefreshToken entity for storing refresh tokens
 * Used for token rotation and validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    private UUID id;
    private String token;
    private User user;
    private LocalDateTime expiryDate;
    private boolean revoked = false;
    private LocalDateTime createdAt;

    /**
     * Checks if the refresh token is expired
     *
     * @return true if token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
