//package com.guardianservices.userAuthentication.web.controller;
//
//import com.guardianservices.userAuthentication.application.command.LoginCommand;
//import com.guardianservices.userAuthentication.application.command.RegisterCommand;
//import com.guardianservices.userAuthentication.application.exception.UserManagementException;
//import com.guardianservices.userAuthentication.application.exception.UserNotFoundException;
//import com.guardianservices.userAuthentication.application.service.AuthService;
//import com.guardianservices.userAuthentication.application.service.UserService;
//import com.guardianservices.userAuthentication.domain.port.out.TokenBlocklistPort;
//import com.guardianservices.userAuthentication.util.HttpRequestUtils;
//import com.guardianservices.userAuthentication.web.dto.request.*;
//import com.guardianservices.userAuthentication.web.dto.response.ApiErrorResponse;
//import com.guardianservices.userAuthentication.web.dto.response.RequestPasswordResetResponse;
//import com.guardianservices.userAuthentication.web.dto.response.TokenResponse;
//import com.guardianservices.userAuthentication.web.mapper.UserMapper;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//import java.time.Duration;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * REST controller for authentication operations
// * Provides endpoints for login, registration, and token management
// */
//@RestController
//@RequestMapping("/auth")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "Authentication", description = "Authentication endpoints")
//public class AuthController {
//
//    private final AuthService authService;
//    private final UserService userService;
//    private final UserMapper userMapper;
//    private final TokenBlocklistPort tokenBlocklistPort;
//    private final HttpRequestUtils httpRequestUtils;
//
//    /**
//     * Authenticates user and returns access token
//     *
//     * @param request login request containing credentials
//     * @return token response with access and refresh tokens
//     */
//    @PostMapping("/login")
//    @Operation(summary = "Login user", description = "Authenticates user and returns tokens")
//    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
//        log.debug("Login request for user: {}", request.getUsername());
//
//        try {
//            LoginCommand command = new LoginCommand(
//                    request.getUsername(),
//                    request.getPassword(),
//                    null, // ipAddress
//                    null,  // userAgent
//                    request.getProduct()
//            );
//
//            TokenResponse response = authService.login(command);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            log.error("Error during login for user: {}", request.getUsername(), e);
//            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
//                    .timestamp(LocalDateTime.now())
//                    .status(HttpStatus.UNAUTHORIZED.value())
//                    .error("Unauthorized")
//                    .message(e.getMessage())
//                    .path("/auth/login")
//                    .build();
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
//        }
//    }
//
//    /**
//     * Registers a new user
//     *
//     * @param request registration request with user details
//     * @return created user response
//     */
//    @PostMapping("/register")
//    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
//    @Operation(summary = "Register new user", description = "Creates a new user account")
//    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
//        log.debug("Register request for user: {}", request.getUsername());
//
//        try {
//            RegisterCommand command = new RegisterCommand(
//                    request.getUsername(),
//                    request.getEmail(),
//                    request.getPassword(),
//                    request.getFirstName(),
//                    request.getLastName(),
//                    request.getProductNames()
//            );
//
//            var user = authService.register(command);
//            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
//        } catch (Exception e) {
//            log.error("Error during registration for user: {}", request.getUsername(), e);
//
//            HttpStatus status = HttpStatus.BAD_REQUEST;
//            String errorType = "Bad Request";
//
//            if (e.getMessage() != null && (e.getMessage().contains("already exists") || e.getMessage().contains("already registered"))) {
//                status = HttpStatus.CONFLICT;
//                errorType = "Conflict";
//            }
//
//            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
//                    .timestamp(LocalDateTime.now())
//                    .status(status.value())
//                    .error(errorType)
//                    .message(e.getMessage())
//                    .path("/auth/register")
//                    .build();
//            return ResponseEntity.status(status).body(errorResponse);
//        }
//    }
//
//    /**
//     * Refreshes access token
//     *
//     * @param request refresh token request
//     * @return new token response
//     */
//    @PostMapping("/refresh")
//    @Operation(summary = "Refresh token", description = "Gets new access token using refresh token")
//    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
//        log.debug("Refresh token request");
//
//        try {
//            TokenResponse response = authService.refreshToken(request.getRefreshToken());
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            log.error("Error during token refresh", e);
//            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
//                    .timestamp(LocalDateTime.now())
//                    .status(HttpStatus.UNAUTHORIZED.value())
//                    .error("Unauthorized")
//                    .message(e.getMessage())
//                    .path("/auth/refresh")
//                    .build();
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
//        }
//    }
//
//    /**
//     * Logs out user.
//     * Invalidates the refresh token in Keycloak and adds the current access token to a blocklist
//     * to prevent it from being used again before it naturally expires.
//     *
//     * @param request refresh token request
//     * @param principal the currently authenticated principal
//     * @return no content response
//     */
//    @PostMapping("/logout")
//    @Operation(summary = "Logout user", description = "Invalidates refresh token and adds access token to blocklist")
//    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request, Principal principal) {
//        log.debug("Logout request received");
//
//        try {
//            // 1. Invalidate Refresh Token in Keycloak
//            authService.logout(request.getRefreshToken());
//
//            // 2. Block the current Access Token
//            if (principal instanceof JwtAuthenticationToken jwtAuthToken) {
//                Jwt jwt = jwtAuthToken.getToken();
//                String jti = jwt.getId();
//                Instant expiresAt = jwt.getExpiresAt();
//
//                if (jti != null && expiresAt != null) {
//                    Duration timeUntilExpiry = Duration.between(Instant.now(), expiresAt);
//                    // Only block if it hasn't already expired
//                    if (!timeUntilExpiry.isNegative()) {
//                        log.debug("Adding access token (JTI: {}) to blocklist for duration: {}", jti, timeUntilExpiry);
//                        tokenBlocklistPort.blockToken(jti, timeUntilExpiry);
//                    }
//                } else {
//                    log.warn("Could not extract JTI or expiration from JWT during logout.");
//                }
//            } else {
//                log.warn("Logout called without a valid JWT Authentication Token.");
//            }
//
//            return ResponseEntity.noContent().build();
//
//        } catch (Exception e) {
//            log.error("Error during logout", e);
//            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
//                    .timestamp(LocalDateTime.now())
//                    .status(HttpStatus.BAD_REQUEST.value())
//                    .error("Bad Request")
//                    .message("Failed to process logout completely. " + e.getMessage())
//                    .path("/auth/logout")
//                    .build();
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//        }
//    }
//
//    @PostMapping("/request-password-reset")
//    @Operation(summary = "Request Password Reset", description = "Initiates the password reset process by generating an OTP and sending it to the user's email.")
//    public ResponseEntity<RequestPasswordResetResponse> requestPasswordReset(@Valid @RequestBody RequestPasswordResetRequest request) {
//        try {
//            UUID requestId = userService.requestPasswordReset(request.getUsernameOrEmail(), request.getProduct());
//            return ResponseEntity.ok(new RequestPasswordResetResponse(requestId, "Password reset OTP has been sent to the registered email address."));
//        } catch (UserNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RequestPasswordResetResponse(null, e.getMessage()));
//        }
//    }
//
//    @PostMapping("/validate-otp-reset-password")
//    @Operation(summary = "Confirm Password Reset", description = "Validates the OTP and resets the user's password.")
//    public ResponseEntity<?> confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetRequest request, HttpServletRequest httpRequest) {
//        try {
//            String ipAddress = httpRequestUtils.getClientIpAddress(httpRequest);
//            userService.confirmPasswordReset(request.getRequestId(), request.getOtp(), request.getProduct(), ipAddress);
//            return ResponseEntity.ok().body(Map.of("message", "Password has been reset successfully."));
//        } catch (UserManagementException e) {
//            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
//                    .timestamp(LocalDateTime.now())
//                    .status(HttpStatus.BAD_REQUEST.value())
//                    .error("Bad Request")
//                    .message(e.getMessage())
//                    .path("/auth/validate-otp-reset-password")
//                    .build();
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//        }
//    }
//}
