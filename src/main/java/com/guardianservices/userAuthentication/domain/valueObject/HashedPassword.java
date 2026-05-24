package com.guardianservices.userAuthentication.domain.valueObject;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * HashedPassword value object using Java 21 record
 */
public record HashedPassword(String value) {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public HashedPassword {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or blank");
        }
    }

    public static HashedPassword fromRawPassword(String rawPassword) {
        return new HashedPassword(ENCODER.encode(rawPassword));
    }

    public static HashedPassword fromHash(String hash) {
        return new HashedPassword(hash);
    }

    public boolean matches(String rawPassword) {
        return ENCODER.matches(rawPassword, value);
    }
}
