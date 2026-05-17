package com.userAuthentication.domain.valueObject;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

/**
 * Email value object as Java 21 record with validation
 */
@Embeddable
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public Email {
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid email address: " + value);
        }
        value = value.toLowerCase();
    }

    private static boolean isValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
