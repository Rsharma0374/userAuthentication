package com.guardianservices.userAuthentication.domain.port.in;

import com.guardianservices.userAuthentication.application.command.LoginCommand;
import com.guardianservices.userAuthentication.application.command.RegisterCommand;
import com.guardianservices.userAuthentication.domain.model.User;
import com.guardianservices.userAuthentication.web.dto.response.TokenResponse;

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
