package com.guardianservices.userAuthentication.web.controller.common;

import com.guardianservices.userAuthentication.application.exception.UserNotFoundException;
import com.guardianservices.userAuthentication.application.service.admin.AdminService;
import com.guardianservices.userAuthentication.domain.model.User;
import com.guardianservices.userAuthentication.domain.port.out.AdminRepository;
import com.guardianservices.userAuthentication.domain.port.out.UserRepository;
import com.guardianservices.userAuthentication.web.dto.request.RefreshTokenRequest;
import com.guardianservices.userAuthentication.web.dto.response.ApiErrorResponse;
import com.guardianservices.userAuthentication.web.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {


    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminService adminService;

    @Qualifier("adminJwtDecoder")
    @Autowired
    private JwtDecoder adminJwtDecoder;


    @Qualifier("userJwtDecoder")
    @Autowired
    private JwtDecoder userJwtDecoder;

    /**
     * Refreshes access token
     *
     * @param request refresh token request
     * @return new token response
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh token",
            description = "Gets new access token using refresh token"
    )
    public ResponseEntity<?> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        log.debug("Refresh token request");

        try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return buildUnauthorizedResponse("Missing or invalid Authorization header");
            }

            String accessToken = authHeader.substring(7);

            Jwt jwt = adminJwtDecoder.decode(accessToken);
            if (null == jwt) {
                jwt = userJwtDecoder.decode(accessToken);
            }

            if (jwt == null) {
                return buildUnauthorizedResponse("Invalid or missing access token");
            }
            String username = jwt.getClaimAsString("preferred_username");

            if (username == null || username.isBlank()) {

                log.warn(
                        "Username claim not found in JWT for subject: {}",
                        jwt.getSubject()
                );

                return buildUnauthorizedResponse("Invalid token structure");
            }

            boolean isAdmin = extractRoles(jwt).stream()
                    .anyMatch(role ->
                            role.equals("SUPER_ADMIN")
                                    || role.equals("PRODUCT_ADMIN"));

            log.info(
                    "Token refresh requested for user: {}, isAdmin: {}",
                    username,
                    isAdmin
            );

            TokenResponse response = null;

            if (isAdmin) {

                adminRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "Admin not found for username: " + username
                                ));

                response = adminService.refreshToken(request.getRefreshToken());

            } else {

                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found for username: " + username
                                ));

//                    response = authService.refreshToken(request.getRefreshToken());
            }

            return ResponseEntity.ok(response);

        } catch (JwtException ex) {

            log.error("Invalid JWT token", ex);

            return buildUnauthorizedResponse("Invalid or expired access token");

        } catch (Exception ex) {

            log.error("Error during token refresh", ex);

            return buildUnauthorizedResponse(ex.getMessage());
        }
    }

    private List<String> extractRoles(Jwt jwt) {

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null || realmAccess.get("roles") == null) {
            return Collections.emptyList();
        }

        return (List<String>) realmAccess.get("roles");
    }

    private ResponseEntity<ApiErrorResponse> buildUnauthorizedResponse(String message) {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message(message)
                .path("/auth/refresh")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }
}
