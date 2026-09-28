package com.guardianservices.userauthentication.authentication.controller;

import com.guardianservices.userauthentication.authentication.service.AuthenticationService;
import com.guardianservices.userauthentication.authentication.service.MfaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final MfaService mfaService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                    HttpServletRequest httpRequest,
                                    HttpServletResponse httpResponse) {
        AuthenticationService.AuthenticationResult result = authenticationService.authenticate(
            request.getEmail(),
            request.getPassword(),
            request.getDeviceId(),
            request.getDeviceName(),
            getClientIp(httpRequest),
            httpRequest.getHeader("User-Agent")
        );

        if (result.getType() == AuthenticationService.AuthenticationResult.Type.MFA_REQUIRED) {
            return ResponseEntity.ok(Map.of(
                "type", "MFA_REQUIRED",
                "challengeId", result.getChallengeId()
            ));
        }

        // Set refresh token in cookie
        setRefreshTokenCookie(httpResponse, result.getRefreshToken());

        return ResponseEntity.ok(Map.of(
            "type", "SUCCESS",
            "accessToken", result.getAccessToken(),
            "sessionId", result.getSession().getId()
        ));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<?> verifyMfa(@Valid @RequestBody MfaVerifyRequest request,
                                        HttpServletRequest httpRequest,
                                        HttpServletResponse httpResponse) {
        AuthenticationService.AuthenticationResult result = authenticationService.verifyMfa(
            request.getChallengeId(),
            request.getCode(),
            request.getDeviceId(),
            request.getDeviceName(),
            getClientIp(httpRequest),
            httpRequest.getHeader("User-Agent")
        );

        setRefreshTokenCookie(httpResponse, result.getRefreshToken());

        return ResponseEntity.ok(Map.of(
            "accessToken", result.getAccessToken(),
            "sessionId", result.getSession().getId()
        ));
    }

    @PostMapping("/mfa/enroll")
    public ResponseEntity<?> enrollMfa(HttpServletRequest httpRequest) {
        // Get user from security context
        MfaService.MfaEnrollmentResult result = mfaService.enrollMfa(getCurrentUser());
        return ResponseEntity.ok(Map.of(
            "secret", result.getSecret(),
            "qrCodeUrl", result.getQrCodeUrl(),
            "recoveryCodes", result.getRecoveryCodes()
        ));
    }

    @PostMapping("/mfa/confirm")
    public ResponseEntity<?> confirmMfaEnrollment(@Valid @RequestBody MfaConfirmRequest request,
                                                   HttpServletRequest httpRequest) {
        mfaService.confirmMfaEnrollment(getCurrentUser(), request.getCode());
        return ResponseEntity.ok(Map.of("message", "MFA enrolled successfully"));
    }

    @PostMapping("/mfa/recovery-codes")
    public ResponseEntity<?> getRecoveryCodes(HttpServletRequest httpRequest) {
        List<String> codes = mfaService.getRecoveryCodes(getCurrentUser());
        return ResponseEntity.ok(Map.of("recoveryCodes", codes));
    }

    @DeleteMapping("/mfa")
    public ResponseEntity<?> disableMfa(HttpServletRequest httpRequest) {
        mfaService.disableMfa(getCurrentUser(), null); // Would need password verification
        return ResponseEntity.ok(Map.of("message", "MFA disabled"));
    }

    private java.net.InetAddress getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            try {
                return java.net.InetAddress.getByName(xForwardedFor.split(",")[0].trim());
            } catch (Exception e) {
                // Fall through
            }
        }
        try {
            return java.net.InetAddress.getByName(request.getRemoteAddr());
        } catch (Exception e) {
            return null;
        }
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        // In production, use proper cookie settings from config
        response.addHeader("Set-Cookie", 
            "__Host-refresh=" + refreshToken + 
            "; Path=/; HttpOnly; Secure; SameSite=Lax; Max-Age=2592000");
    }

    private com.guardianservices.userauthentication.account.User getCurrentUser() {
        // In practice, get from SecurityContext
        return null;
    }
}