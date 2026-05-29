//package com.guardianservices.userAuthentication.web.controller.user;
//
//import com.guardianservices.userAuthentication.application.command.LoginCommand;
//import com.guardianservices.userAuthentication.application.exception.UserManagementException;
//import com.guardianservices.userAuthentication.application.exception.UserNotFoundException;
//import com.guardianservices.userAuthentication.application.service.AuthService;
//import com.guardianservices.userAuthentication.application.service.UserService;
//import com.guardianservices.userAuthentication.domain.port.out.TokenBlocklistPort;
//import com.guardianservices.userAuthentication.web.dto.request.ConfirmPasswordResetRequest;
//import com.guardianservices.userAuthentication.web.dto.request.LoginRequest;
//import com.guardianservices.userAuthentication.web.dto.request.RefreshTokenRequest;
//import com.guardianservices.userAuthentication.web.dto.request.RequestPasswordResetRequest;
//import com.guardianservices.userAuthentication.web.dto.response.ApiErrorResponse;
//import com.guardianservices.userAuthentication.web.dto.response.RequestPasswordResetResponse;
//import com.guardianservices.userAuthentication.web.dto.response.TokenResponse;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.security.Principal;
//import java.time.Duration;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/portal/auth")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "User Portal Authentication", description = "Endpoints for user authentication and password resets")
//public class UserAuthController {
//
//    @Qualifier("userAuthService")
//    private final AuthService authService;
//
//    private final UserService userService;
//    private final TokenBlocklistPort tokenBlocklistPort;
//
//    @PostMapping("/login")
//    @Operation(summary = "User Login", description = "Authenticates a user and returns tokens.")
//    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
//        try {
//            LoginCommand command = new LoginCommand(request.getUsername(), request.getPassword(), null, null, request.getProduct());
//            TokenResponse response = authService.login(command);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Unauthorized", e.getMessage(), "/portal/auth/login", null));
//        }
//    }
//
//    @PostMapping("/refresh")
//    @Operation(summary = "Refresh Token", description = "Refreshes an access token.")
//    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
//        try {
//            TokenResponse response = authService.refreshToken(request.getRefreshToken());
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Unauthorized", e.getMessage(), "/portal/auth/refresh", null));
//        }
//    }
//
//    @PostMapping("/logout")
//    @PreAuthorize("isAuthenticated()")
//    @Operation(summary = "User Logout", description = "Logs out the user and invalidates tokens.")
//    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request, Principal principal) {
//        try {
//            authService.logout(request.getRefreshToken());
//            if (principal instanceof JwtAuthenticationToken jwtAuthToken) {
//                Jwt jwt = jwtAuthToken.getToken();
//                if (jwt.getId() != null && jwt.getExpiresAt() != null) {
//                    Duration timeUntilExpiry = Duration.between(Instant.now(), jwt.getExpiresAt());
//                    if (!timeUntilExpiry.isNegative()) {
//                        tokenBlocklistPort.blockToken(jwt.getId(), timeUntilExpiry);
//                    }
//                }
//            }
//            return ResponseEntity.noContent().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", "Logout failed.", "/portal/auth/logout", null));
//        }
//    }
//
//    @PostMapping("/request-password-reset")
//    @Operation(summary = "Request Password Reset", description = "Initiates the password reset process.")
//    public ResponseEntity<RequestPasswordResetResponse> requestPasswordReset(@Valid @RequestBody RequestPasswordResetRequest request) {
//        try {
//            UUID requestId = userService.requestPasswordReset(request.getUsernameOrEmail(), request.getProduct());
//            return ResponseEntity.ok(new RequestPasswordResetResponse(requestId, "Password reset OTP has been sent."));
//        } catch (UserNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RequestPasswordResetResponse(null, e.getMessage()));
//        }
//    }
//
//    @PostMapping("/validate-otp-reset-password")
//    @Operation(summary = "Confirm Password Reset", description = "Validates the OTP and resets the password.")
//    public ResponseEntity<?> confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetRequest request, HttpServletRequest httpRequest) {
//        try {
//            userService.confirmPasswordReset(request.getRequestId(), request.getOtp(), request.getProduct(), httpRequest.getRemoteAddr());
//            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
//        } catch (UserManagementException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", e.getMessage(), "/portal/auth/validate-otp-reset-password", null));
//        }
//    }
//}
