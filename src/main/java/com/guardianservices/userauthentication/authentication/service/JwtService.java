package com.guardianservices.userauthentication.authentication.service;

import com.guardianservices.userauthentication.account.User;
import com.guardianservices.userauthentication.account.repository.UserRoleRepository;
import com.guardianservices.userauthentication.common.exception.UnauthorizedException;
import com.guardianservices.userauthentication.platform.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final AuthProperties authProperties;
    private final UserRoleRepository userRoleRepository;

    private KeyPair keyPair;
    private String keyId;

    public void initialize() {
        try {
            // In production, load from managed KMS/HSM
            // For development, generate a new key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.keyPair = keyGen.generateKeyPair();
            this.keyId = "key-" + UUID.randomUUID().toString().substring(0, 8);
            
            log.info("Initialized JWT signing key: {}", keyId);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate RSA key pair", e);
        }
    }

    public String createAccessToken(User user, String sessionId, List<String> scopes) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime exp = now.plus(authProperties.getJwt().getAccessTokenTtl());

        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
            .issuer(authProperties.getJwt().getIssuer())
            .subject(user.getId().toString())
            .audience().add(authProperties.getJwt().getAudience()).and()
            .issuedAt(Date.from(now.toInstant()))
            .expiration(Date.from(exp.toInstant()))
            .id(jti)
            .claim("sid", sessionId)
            .claim("scopes", scopes)
            .claim("roles", getUserRoles(user))
            .signWith(getPrivateKey())
            .compact();
    }

    public JwtValidationResult validateAccessToken(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                .verifyWith((RSAPublicKey) getPublicKey())
                .requireIssuer(authProperties.getJwt().getIssuer())
                .requireAudience(authProperties.getJwt().getAudience())
                .build()
                .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            
            // Additional validation
            String subject = claims.getSubject();
            String jti = claims.getId();
            String sessionId = claims.get("sid", String.class);
            List<String> scopes = claims.get("scopes", List.class);
            List<String> roles = claims.get("roles", List.class);

            return new JwtValidationResult(
                true,
                subject,
                jti,
                sessionId,
                scopes != null ? scopes : List.of(),
                roles != null ? roles : List.of(),
                claims.getExpiration().toInstant()
            );
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return new JwtValidationResult(false, e.getMessage());
        }
    }

    public String getJwks() {
        // In production, this would return the JWKS with current and previous keys
        // For now, return a simple JWKS with the current key
        RSAPublicKey publicKey = (RSAPublicKey) getPublicKey();
        
        return String.format("""
            {
              "keys": [
                {
                  "kty": "RSA",
                  "use": "sig",
                  "kid": "%s",
                  "alg": "RS256",
                  "n": "%s",
                  "e": "%s"
                }
              ]
            }
            """,
            keyId,
            base64UrlEncode(publicKey.getModulus().toByteArray()),
            base64UrlEncode(publicKey.getPublicExponent().toByteArray())
        );
    }

    private Key getPrivateKey() {
        return keyPair.getPrivate();
    }

    private Key getPublicKey() {
        return keyPair.getPublic();
    }

    private List<String> getUserRoles(User user) {
        return userRoleRepository.findByUser(user).stream()
            .map(ur -> ur.getRole().getName())
            .toList();
    }

    private String base64UrlEncode(byte[] bytes) {
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static class JwtValidationResult {
        private final boolean valid;
        private final String subject;
        private final String jti;
        private final String sessionId;
        private final List<String> scopes;
        private final List<String> roles;
        private final Instant expiry;
        private final String error;

        public JwtValidationResult(boolean valid, String subject, String jti, String sessionId,
                                   List<String> scopes, List<String> roles, Instant expiry) {
            this.valid = valid;
            this.subject = subject;
            this.jti = jti;
            this.sessionId = sessionId;
            this.scopes = scopes;
            this.roles = roles;
            this.expiry = expiry;
            this.error = null;
        }

        public JwtValidationResult(boolean valid, String error) {
            this.valid = valid;
            this.subject = null;
            this.jti = null;
            this.sessionId = null;
            this.scopes = null;
            this.roles = null;
            this.expiry = null;
            this.error = error;
        }

        public boolean isValid() {
            return valid;
        }

        public String getSubject() {
            return subject;
        }

        public String getJti() {
            return jti;
        }

        public String getSessionId() {
            return sessionId;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public List<String> getRoles() {
            return roles;
        }

        public Instant getExpiry() {
            return expiry;
        }

        public String getError() {
            return error;
        }
    }
}