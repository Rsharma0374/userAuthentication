package com.guardianservices.userAuthentication.web.controller;

import com.guardianservices.userAuthentication.application.command.LoginCommand;
import com.guardianservices.userAuthentication.application.command.RegisterCommand;
import com.guardianservices.userAuthentication.application.service.AuthService;
import com.guardianservices.userAuthentication.web.dto.request.LoginRequest;
import com.guardianservices.userAuthentication.web.dto.request.RefreshTokenRequest;
import com.guardianservices.userAuthentication.web.dto.request.RegisterRequest;
import com.guardianservices.userAuthentication.web.dto.response.ApiErrorResponse;
import com.guardianservices.userAuthentication.web.dto.response.TokenResponse;
import com.guardianservices.userAuthentication.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login request for user: {}", request.getUsername());

        try {
            LoginCommand command = new LoginCommand(
                    request.getUsername(),
                    request.getPassword(),
                    null, // ipAddress
                    null  // userAgent
            );

            TokenResponse response = authService.login(command);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during login for user: {}", request.getUsername(), e);
            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .message(e.getMessage())
                    .path("/auth/login")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Registers a new user
     *
     * @param request registration request with user details
     * @return created user response
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Register new user", description = "Creates a new user account")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Register request for user: {}", request.getUsername());

        try {
            RegisterCommand command = new RegisterCommand(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getProductNames()
            );

            var user = authService.register(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
        } catch (Exception e) {
            log.error("Error during registration for user: {}", request.getUsername(), e);
            
            HttpStatus status = HttpStatus.BAD_REQUEST;
            String errorType = "Bad Request";
            
            if (e.getMessage() != null && (e.getMessage().contains("already exists") || e.getMessage().contains("already registered"))) {
                status = HttpStatus.CONFLICT;
                errorType = "Conflict";
            }
            
            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(status.value())
                    .error(errorType)
                    .message(e.getMessage())
                    .path("/auth/register")
                    .build();
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    /**
     * Refreshes access token
     *
     * @param request refresh token request
     * @return new token response
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Gets new access token using refresh token")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Refresh token request");

        try {
            TokenResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during token refresh", e);
            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .message(e.getMessage())
                    .path("/auth/refresh")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Logs out user
     *
     * @param request refresh token request
     * @return no content response
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates refresh token and logs out user")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Logout request");

        try {
            authService.logout(request.getRefreshToken());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error during logout", e);
            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Bad Request")
                    .message(e.getMessage())
                    .path("/auth/logout")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
