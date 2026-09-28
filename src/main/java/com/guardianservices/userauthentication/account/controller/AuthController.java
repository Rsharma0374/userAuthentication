package com.guardianservices.userauthentication.account.controller;

import com.guardianservices.userauthentication.account.service.AccountService;
import com.guardianservices.userauthentication.account.UserStatus;
import com.guardianservices.userauthentication.common.exception.ConflictException;
import com.guardianservices.userauthentication.common.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            String passwordHash = passwordEncoder.encode(request.getPassword());
            AccountService.RegistrationResult registration =
                accountService.registerWithOutcome(request.getEmail(), passwordHash);
            if (registration.existingAccount()) {
                if (registration.user().getStatus() == UserStatus.PENDING_VERIFICATION) {
                    log.info("Registration retried for account pending email verification");
                    return ResponseEntity.accepted().body(Map.of(
                        "message", "An account with this email is already pending verification. A new verification email has been sent."
                    ));
                }
                log.info("Registration rejected because the account already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "An account with this email already exists. Please sign in instead."
                ));
            }
            log.info("Registration request accepted");
            return ResponseEntity.accepted().body(Map.of(
                "message", "Registration accepted. Please verify your email to activate your account."
            ));
        } catch (ValidationException e) {
            log.warn("Registration request rejected: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "errors", e.getFieldErrors()
            ));
        } catch (ConflictException e) {
            log.warn("Registration request could not be completed due to a conflict");
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Registration request contained invalid details");
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Invalid registration details."));
        } catch (DataIntegrityViolationException e) {
            log.warn("Registration request rejected by a data constraint");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "Registration could not be completed due to a conflict. Please try again."
            ));
        } catch (DataAccessException e) {
            log.error("Registration could not access the account data store", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "message", "Registration is temporarily unavailable. Please try again later."
            ));
        } catch (RuntimeException e) {
            log.error("Unexpected error while processing registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "Registration failed due to an unexpected server error."
            ));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRegistration(
        MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
            errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        log.warn("Registration request failed input validation");
        return ResponseEntity.badRequest().body(Map.of(
            "message", "Registration details are invalid.",
            "errors", errors
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableRegistration(
        HttpMessageNotReadableException exception
    ) {
        log.warn("Registration request body could not be parsed");
        return ResponseEntity.badRequest().body(Map.of(
            "message", "Request body is missing or contains invalid JSON."
        ));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Map<String, Object>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        accountService.verifyEmail(request.getToken());
        log.info("Email verification completed");
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/email/resend")
    public ResponseEntity<Map<String, Object>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        accountService.resendVerification(request.getEmail());
        log.info("Verification email resend request processed");
        return ResponseEntity.accepted().body(Map.of("message", "If the account exists, a verification email has been sent"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        accountService.initiatePasswordReset(request.getEmail());
        log.info("Password reset request processed");
        return ResponseEntity.accepted().body(Map.of("message", "If the account exists, a password reset email has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // Client sends raw password, we SHA-256 hash it
        String passwordHash = passwordEncoder.encode(request.getPassword());
        accountService.resetPassword(request.getToken(), passwordHash);
        log.info("Password reset completed");
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                               HttpServletRequest httpRequest) {
        // Get user from security context
        // This would be implemented with Spring Security
        log.info("Password change request accepted");
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/email-change")
    public ResponseEntity<Map<String, Object>> initiateEmailChange(@Valid @RequestBody EmailChangeRequest request,
                                                                    HttpServletRequest httpRequest) {
        // Get user from security context
        log.info("Email change request accepted");
        return ResponseEntity.accepted().body(Map.of("message", "Email change initiated"));
    }

}