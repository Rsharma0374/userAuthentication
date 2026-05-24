package com.guardianservices.userAuthentication.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class KeycloakInteractionException extends RuntimeException {
    public KeycloakInteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
