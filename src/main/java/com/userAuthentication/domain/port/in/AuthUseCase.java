package com.userAuthentication.domain.port.in;

import com.userAuthentication.application.command.LoginCommand;
import com.userAuthentication.application.command.RegisterCommand;
import com.userAuthentication.domain.model.User;
import com.userAuthentication.web.dto.response.TokenResponse;

/**
 * Authentication use case interface
 * Defines authentication-related operations
 */
public interface AuthUseCase {

    /**
     * Authenticates a user and returns tokens
     *
     * @param command login command
     * @return token response
     */
    TokenResponse login(LoginCommand command);

    /**
     * Registers a new user
     *
     * @param command register command
     * @return created user
     */
    User register(RegisterCommand command);

    /**
     * Registers a new admin user
     *
     * @param command register command
     * @return created admin user
     */
    User registerAdmin(RegisterCommand command);

    /**
     * Refreshes access token
     *
     * @param refreshToken refresh token
     * @return new token response
     */
    TokenResponse refreshToken(String refreshToken);

    /**
     * Logs out a user
     *
     * @param refreshToken refresh token to revoke
     */
    void logout(String refreshToken);
}
