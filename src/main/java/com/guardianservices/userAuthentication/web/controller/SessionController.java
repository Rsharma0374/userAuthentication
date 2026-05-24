package com.guardianservices.userAuthentication.web.controller;

import com.guardianservices.userAuthentication.application.service.TokenService;
import com.guardianservices.userAuthentication.web.dto.response.SessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for session management
 * Handles user session operations
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Session Management", description = "Endpoints for managing user sessions")
@SecurityRequirement(name = "bearer-jwt")
public class SessionController {

    private final TokenService tokenService;

    /**
     * Retrieves all active sessions for the current user
     *
     * @return list of active sessions
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Get my sessions", description = "Retrieves all active sessions for the current user")
    public ResponseEntity<List<SessionResponse>> getMySessions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());

        log.debug("Retrieving sessions for user: {}", userId);

        // Implementation would fetch sessions from cache or database
        // For now, return empty list
        return ResponseEntity.ok(List.of());
    }

    /**
     * Terminates a specific session
     *
     * @param sessionId session ID to terminate
     * @return no content response
     */
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Terminate session", description = "Terminates a specific user session")
    public ResponseEntity<Void> terminateSession(@PathVariable String sessionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());

        log.debug("Terminating session {} for user: {}", sessionId, userId);

        // Invalidate the token
        tokenService.invalidateToken(userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Terminates all sessions for the current user
     *
     * @return no content response
     */
    @DeleteMapping("/me/all")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Terminate all sessions", description = "Terminates all sessions for the current user")
    public ResponseEntity<Void> terminateAllSessions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(auth.getName());

        log.debug("Terminating all sessions for user: {}", userId);

        // Invalidate all tokens for the user
        tokenService.invalidateToken(userId);

        return ResponseEntity.noContent().build();
    }
}
