package com.guardianservices.userauthentication.session.controller;

import com.guardianservices.userauthentication.session.Session;
import com.guardianservices.userauthentication.session.SessionRevocationReason;
import com.guardianservices.userauthentication.session.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request,
                                      HttpServletRequest httpRequest,
                                      HttpServletResponse httpResponse) {
        // In practice, get refresh token from cookie
        String refreshToken = getRefreshTokenFromCookie(httpRequest);
        if (refreshToken == null && request.getRefreshToken() != null) {
            refreshToken = request.getRefreshToken();
        }

        if (refreshToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token required"));
        }

        SessionService.RefreshTokenResult result = sessionService.rotateRefreshToken(
            refreshToken,
            request.getDeviceId(),
            getClientIp(httpRequest),
            httpRequest.getHeader("User-Agent")
        );

        setRefreshTokenCookie(httpResponse, result.getRefreshToken());

        // Generate new access token (would use JwtService)
        return ResponseEntity.ok(Map.of(
            "accessToken", "new-access-token", // Placeholder
            "sessionId", result.getSession().getId()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest,
                                     HttpServletResponse httpResponse) {
        String refreshToken = getRefreshTokenFromCookie(httpRequest);
        if (refreshToken != null) {
            // Find session by refresh token and revoke
            // This would need a method in SessionService
        }
        
        clearRefreshTokenCookie(httpResponse);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(HttpServletRequest httpRequest) {
        // Get user from security context
        List<Session> sessions = sessionService.getUserSessions(getCurrentUser());
        return ResponseEntity.ok(sessions.stream().map(s -> Map.of(
            "id", s.getId(),
            "deviceId", s.getDeviceId(),
            "deviceName", s.getDeviceName(),
            "ipAddress", s.getIpAddress() != null ? s.getIpAddress().toString() : null,
            "userAgent", s.getUserAgent(),
            "createdAt", s.getCreatedAt(),
            "lastUsedAt", s.getLastUsedAt(),
            "idleExpiresAt", s.getIdleExpiresAt(),
            "absoluteExpiresAt", s.getAbsoluteExpiresAt()
        )).toList());
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<?> revokeSession(@PathVariable UUID id,
                                            HttpServletRequest httpRequest) {
        sessionService.revokeSession(id, SessionRevocationReason.USER_LOGOUT);
        return ResponseEntity.ok(Map.of("message", "Session revoked"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(HttpServletRequest httpRequest) {
        sessionService.revokeAllUserSessions(getCurrentUser(), SessionRevocationReason.USER_LOGOUT_ALL);
        return ResponseEntity.ok(Map.of("message", "All sessions revoked"));
    }

    private java.net.InetAddress getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            try {
                return java.net.InetAddress.getByName(xForwardedFor.split(",")[0].trim());
            } catch (Exception e) {
            }
        }
        try {
            return java.net.InetAddress.getByName(request.getRemoteAddr());
        } catch (Exception e) {
            return null;
        }
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("__Host-refresh".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        response.addHeader("Set-Cookie", 
            "__Host-refresh=" + refreshToken + 
            "; Path=/; HttpOnly; Secure; SameSite=Lax; Max-Age=2592000");
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", 
            "__Host-refresh=; Path=/; HttpOnly; Secure; SameSite=Lax; Max-Age=0");
    }

    private com.guardianservices.userauthentication.account.User getCurrentUser() {
        return null;
    }
}