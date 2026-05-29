//package com.guardianservices.userAuthentication.web.controller.common;
//
//import com.guardianservices.userAuthentication.application.service.TokenService;
//import com.guardianservices.userAuthentication.domain.port.out.TokenBlocklistPort;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtException;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.Instant;
//import java.util.Map;
//
///**
// * Internal API endpoints for API Gateway
// * These are not exposed externally
// */
//@RestController
//@RequestMapping("/internal")
//@RequiredArgsConstructor
//@Slf4j
//public class InternalController {
//
//    private final TokenService tokenService;
//    private final JwtDecoder jwtDecoder;
//    private final TokenBlocklistPort tokenBlocklistPort;
//
//    /**
//     * Token validation endpoint for API Gateway
//     * Gateway calls this to check if a token is structurally valid, not expired, and not revoked.
//     */
//    @PostMapping("/token/validate")
//    public ResponseEntity<Boolean> validateToken(@RequestBody Map<String, String> request) {
//        String token = request.get("token");
//
//        if (token == null || token.isBlank()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
//        }
//
//        try {
//            // 1. Decode and Verify Signature & Expiration (Throws exception if invalid)
//            Jwt jwt = jwtDecoder.decode(token);
//
//            // 2. Check Expiration manually (JwtDecoder usually handles this, but good to be safe)
//            if (jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(Instant.now())) {
//                log.debug("Token is expired.");
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
//            }
//
//            // 3. Check Blocklist (Revocation status)
//            String jti = jwt.getId();
//            if (jti != null && tokenBlocklistPort.isTokenBlocked(jti)) {
//                log.debug("Token is structurally valid but has been revoked (blocklisted).");
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
//            }
//
//            // If it passes all checks, it's valid
//            return ResponseEntity.ok(true);
//
//        } catch (JwtException e) {
//            // Signature invalid, malformed token, or expired
//            log.debug("Token validation failed: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
//        } catch (Exception e) {
//            log.error("Unexpected error during token validation", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
//        }
//    }
//
//    /**
//     * Token revocation notification from gateway
//     * Called when gateway detects token revocation
//     */
//    @PostMapping("/token/revoke")
//    public ResponseEntity<Void> revokeToken(@RequestBody Map<String, String> request) {
//        String token = request.get("token");
//        tokenService.blockToken(token, 3600); // Block for 1 hour
//
//        log.info("Token revoked via internal API");
//        return ResponseEntity.ok().build();
//    }
//}
