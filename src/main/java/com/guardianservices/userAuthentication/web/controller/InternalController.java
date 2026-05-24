package com.guardianservices.userAuthentication.web.controller;

import com.guardianservices.userAuthentication.application.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Internal API endpoints for API Gateway
 * These are not exposed externally
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalController {

    private final TokenService tokenService;

    /**
     * Token validation endpoint for API Gateway
     * Gateway calls this to check if token is revoked
     */
    @PostMapping("/token/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        boolean isBlocked = tokenService.isTokenBlocked(token);

        return ResponseEntity.ok(Map.of("valid", !isBlocked));
    }

    /**
     * Token revocation notification from gateway
     * Called when gateway detects token revocation
     */
    @PostMapping("/token/revoke")
    public ResponseEntity<Void> revokeToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        tokenService.blockToken(token, 3600); // Block for 1 hour

        log.info("Token revoked via internal API");
        return ResponseEntity.ok().build();
    }
}
