package com.guardianservices.userAuthentication.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UserManagementException extends RuntimeException {
    public UserManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserManagementException(String message) {
        super(message);
    }
}
