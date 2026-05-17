package com.userAuthentication.web.controller;

import com.userAuthentication.application.command.LoginCommand;
import com.userAuthentication.application.command.RegisterCommand;
import com.userAuthentication.application.service.AuthService;
import com.userAuthentication.web.dto.request.LoginRequest;
import com.userAuthentication.web.dto.request.RefreshTokenRequest;
import com.userAuthentication.web.dto.request.RegisterRequest;
import com.userAuthentication.web.dto.response.TokenResponse;
import com.userAuthentication.web.dto.response.UserResponse;
import com.userAuthentication.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations
 * Provides endpoints for login, registration, and token management
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    /**
     * Authenticates user and returns access token
     *
     * @param request login request containing credentials
     * @return token response with access and refresh tokens
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns tokens")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login request for user: {}", request.getUsername());

        LoginCommand command = new LoginCommand(
                request.getUsername(),
                request.getPassword(),
                null, // ipAddress
                null  // userAgent
        );

        TokenResponse response = authService.login(command);
        return ResponseEntity.ok(response);
    }

    /**
     * Registers a new user
     *
     * @param request registration request with user details
     * @return created user response
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Register request for user: {}", request.getUsername());

        RegisterCommand command = new RegisterCommand(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
        );

        var user = authService.register(command);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    /**
     * Refreshes access token
     *
     * @param request refresh token request
     * @return new token response
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Gets new access token using refresh token")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Refresh token request");

        TokenResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Logs out user
     *
     * @param request refresh token request
     * @return no content response
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates refresh token and logs out user")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Logout request");

        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
