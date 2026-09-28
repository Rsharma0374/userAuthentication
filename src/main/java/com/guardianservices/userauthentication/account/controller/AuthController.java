package com.guardianservices.userauthentication.account.controller;

import com.guardianservices.userauthentication.account.service.AccountService;
import com.guardianservices.userauthentication.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
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
        // Client sends raw password, we SHA-256 hash it, then Argon2id in service
        String sha256Password = sha256(request.getPassword());
        String passwordHash = passwordEncoder.encode(sha256Password);
        accountService.register(request.getEmail(), passwordHash);
        return ResponseEntity.accepted().body(Map.of("message", "Registration accepted"));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Map<String, Object>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        accountService.verifyEmail(request.getToken());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/email/resend")
    public ResponseEntity<Map<String, Object>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        accountService.resendVerification(request.getEmail());
        return ResponseEntity.accepted().body(Map.of("message", "If the account exists, a verification email has been sent"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        accountService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.accepted().body(Map.of("message", "If the account exists, a password reset email has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // Client sends raw password, we SHA-256 hash it
        String sha256Password = sha256(request.getPassword());
        String passwordHash = passwordEncoder.encode(sha256Password);
        accountService.resetPassword(request.getToken(), passwordHash);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                               HttpServletRequest httpRequest) {
        // Get user from security context
        // This would be implemented with Spring Security
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/email-change")
    public ResponseEntity<Map<String, Object>> initiateEmailChange(@Valid @RequestBody EmailChangeRequest request,
                                                                    HttpServletRequest httpRequest) {
        // Get user from security context
        return ResponseEntity.accepted().body(Map.of("message", "Email change initiated"));
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}