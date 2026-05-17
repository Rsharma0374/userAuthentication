package com.userAuthentication.domain.valueObject;

import jakarta.persistence.Embeddable;
import java.util.UUID;

/**
 * UserId value object as Java 21 record
 */
@Embeddable
public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    public UserId() {
        this(UUID.randomUUID());
    }

    public UserId(String value) {
        this(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
