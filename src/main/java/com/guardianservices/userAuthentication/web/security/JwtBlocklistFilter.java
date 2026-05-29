package com.guardianservices.userAuthentication.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardianservices.userAuthentication.domain.port.out.TokenBlocklistPort;
import com.guardianservices.userAuthentication.web.dto.response.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * A security filter that intercepts incoming requests to check if the provided JWT
 * access token has been blocklisted (e.g., due to logout).
 * This filter runs before standard Spring Security authentication to fail fast.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtBlocklistFilter extends OncePerRequestFilter {

    private final TokenBlocklistPort tokenBlocklistPort;
    @Qualifier("adminJwtDecoder")
    private final JwtDecoder adminJwtDecoder;

    @Qualifier("userJwtDecoder")
    private final JwtDecoder userJwtDecoder;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // We decode the token to get the JTI without verifying signature here.
                // Spring Security will do the full verification later if this filter passes.
                Jwt jwt = adminJwtDecoder.decode(token);
                String jti = jwt.getId();

                if (jti != null && tokenBlocklistPort.isTokenBlocked(jti)) {
                    log.warn("Attempt to use a blocklisted token. JTI: {}", jti);
                    handleBlockedToken(request, response);
                    return; // Stop the filter chain immediately
                }
            } catch (JwtException e) {
                // If decoding fails, we let it pass through. Spring Security's OAuth2 filter
                // will catch the invalid token and handle it appropriately.
                log.trace("Failed to decode token in blocklist filter. Letting Spring Security handle it.", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleBlockedToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Token has been revoked. Please log in again.")
                .path(request.getRequestURI())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
