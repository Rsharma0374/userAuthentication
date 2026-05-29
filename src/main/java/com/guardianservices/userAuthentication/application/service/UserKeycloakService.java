//package com.guardianservices.userAuthentication.application.service;
//
//import com.guardianservices.userAuthentication.application.exception.KeycloakInteractionException;
//import lombok.extern.slf4j.Slf4j;
//import org.keycloak.OAuth2Constants;
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.KeycloakBuilder;
//import org.keycloak.admin.client.resource.RealmResource;
//import org.keycloak.representations.idm.CredentialRepresentation;
//import org.keycloak.representations.idm.UserRepresentation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Service;
//import static com.guardianservices.userAuthentication.conf.KeycloakConfig.KEYCLOAK_REALM;
//import static com.guardianservices.userAuthentication.conf.KeycloakConfig.KEYCLOAK_SECRET_TYPE;
//import static com.guardianservices.userAuthentication.conf.KeycloakConfig.KEYCLOAK_USER_CLIENT_ID;
//import static com.guardianservices.userAuthentication.conf.KeycloakConfig.KEYCLOAK_USER_CLIENT_SECRET;
//import static com.guardianservices.userAuthentication.conf.KeycloakConfig.KEYCLOAK_URL;
///**
// * User-facing Keycloak service.
// *
// * Handles all operations that a regular authenticated user is allowed to perform:
// *   - Login (credential verification / token issuance)
// *   - Token refresh
// *   - Logout (token invalidation)
// *   - Self-service password change
// *   - OTP-based password reset flow
// *
// * Uses the keycloakUserClient bean which is configured with the PASSWORD grant
// * type and scoped to the user-facing client only — it does NOT have realm-admin
// * permissions and cannot perform privileged operations.
// */
//@Service
//@Slf4j
//public class UserKeycloakService {
//    /**
//     * keycloakUserClient is used specifically for token operations
//     * (login / refresh / logout) via the user-facing Keycloak client.
//     */
//    private final Keycloak keycloakUserClient;
//    /**
//     * keycloakAdminClient is injected here with a narrow scope — it is used
//     * ONLY for the self-service credential update (change-password / reset-password)
//     * which requires Admin API access to call resetPassword() on a specific user.
//     * No other admin operations are exposed through this service.
//     */
//    private final Keycloak keycloakAdminClient;
//    @Autowired
//    private InfisicalService infisicalService;
//    public UserKeycloakService(
//            @Qualifier("keycloakUserClient") Keycloak keycloakUserClient,
//            @Qualifier("keycloakAdminClient") Keycloak keycloakAdminClient
//    ) {
//        this.keycloakUserClient = keycloakUserClient;
//        this.keycloakAdminClient = keycloakAdminClient;
//    }
//    // -------------------------------------------------------------------------
//    // Authentication — Login / Token Operations
//    // -------------------------------------------------------------------------
//    /**
//     * Verifies user credentials and returns an access token from Keycloak.
//     * Called by POST /auth/login
//     *
//     * Creates a short-lived Keycloak instance scoped to the provided credentials
//     * so that the user client remains stateless between requests.
//     *
//     * @param username user's username
//     * @param password user's password
//     * @return raw access token string
//     * @throws KeycloakInteractionException if credentials are invalid
//     */
//    public String login(String username, String password) {
//        log.debug("User: attempting login for: {}", username);
//        try {
//            String url      = infisicalService.getSecret(KEYCLOAK_URL, KEYCLOAK_SECRET_TYPE);
//            String realm    = infisicalService.getSecret(KEYCLOAK_REALM, KEYCLOAK_SECRET_TYPE);
//            String clientId = infisicalService.getSecret(KEYCLOAK_USER_CLIENT_ID, KEYCLOAK_SECRET_TYPE);
//            String secret   = infisicalService.getSecret(KEYCLOAK_USER_CLIENT_SECRET, KEYCLOAK_SECRET_TYPE);
//            // Build a per-request Keycloak instance scoped to this user's credentials
//            Keycloak userSession = KeycloakBuilder.builder()
//                    .serverUrl(url)
//                    .realm(realm)
//                    .clientId(clientId)
//                    .clientSecret(secret)
//                    .username(username)
//                    .password(password)
//                    .grantType(OAuth2Constants.PASSWORD)
//                    .build();
//            String accessToken = userSession.tokenManager().getAccessTokenString();
//            log.info("User: login successful for: {}", username);
//            return accessToken;
//        } catch (Exception e) {
//            log.error("User: login failed for {}: {}", username, e.getMessage());
//            throw new KeycloakInteractionException("Invalid credentials", e);
//        }
//    }
//    /**
//     * Refreshes an access token using a valid refresh token.
//     * Called by POST /auth/refresh
//     *
//     * @param refreshToken the refresh token issued at login
//     * @return new access token string
//     * @throws KeycloakInteractionException if the refresh token is expired or invalid
//     */
//    public String refreshToken(String refreshToken) {
//        log.debug("User: refreshing access token");
//        try {
//            // Keycloak token manager handles refresh internally when the access token expires.
//            // For an explicit refresh endpoint, delegate to the token exchange flow.
//            String newToken = keycloakUserClient.tokenManager().refreshToken().getToken();
//            log.info("User: token refreshed successfully");
//            return newToken;
//        } catch (Exception e) {
//            log.error("User: token refresh failed: {}", e.getMessage());
//            throw new KeycloakInteractionException("Failed to refresh token", e);
//        }
//    }
//    /**
//     * Logs out a user by invalidating their session in Keycloak.
//     * Called by POST /auth/logout
//     *
//     * @param userId Keycloak user ID extracted from the current token
//     */
//    public void logout(String userId) {
//        log.debug("User: logging out user: {}", userId);
//        try {
//            getAdminRealmResource()
//                    .users()
//                    .get(userId)
//                    .logout();
//            log.info("User: user {} logged out — session invalidated", userId);
//        } catch (Exception e) {
//            log.error("User: logout failed for {}: {}", userId, e.getMessage());
//            throw new KeycloakInteractionException("Failed to logout user", e);
//        }
//    }
//    // -------------------------------------------------------------------------
//    // Token Management
//    // -------------------------------------------------------------------------
//    /**
//     * Revokes a specific token, effectively invalidating it.
//     * Called by POST /auth/token/revoke
//     *
//     * In Keycloak, full token revocation is achieved by logging out the session.
//     * This method invalidates all sessions for the given user.
//     *
//     * @param userId Keycloak user ID
//     */
//    public void revokeToken(String userId) {
//        log.debug("User: revoking token for user: {}", userId);
//        try {
//            getAdminRealmResource()
//                    .users()
//                    .get(userId)
//                    .logout();
//            log.info("User: token revoked for user: {}", userId);
//        } catch (Exception e) {
//            log.error("User: token revocation failed for {}: {}", userId, e.getMessage());
//            throw new KeycloakInteractionException("Failed to revoke token", e);
//        }
//    }
//    // -------------------------------------------------------------------------
//    // Self-Service Password Operations
//    // -------------------------------------------------------------------------
//    /**
//     * Changes the authenticated user's own password after verifying their current one.
//     * Called by POST /auth/change-password
//     *
//     * Steps:
//     *  1. Verify current credentials via login attempt.
//     *  2. If valid, update password using the Admin API (only permitted call here).
//     *
//     * @param username        user's username
//     * @param currentPassword current password for re-authentication
//     * @param newPassword     new password to set
//     * @throws KeycloakInteractionException if current credentials are wrong
//     */
//    public void changePassword(String username, String currentPassword, String newPassword) {
//        log.debug("User: changing password for: {}", username);
//        // Step 1 — verify current credentials before allowing the change
//        verifyCredentials(username, currentPassword);
//        // Step 2 — look up user ID and update the password
//        UserRepresentation user = getUserByUsername(username);
//        if (user == null) {
//            throw new KeycloakInteractionException("User not found: " + username, null);
//        }
//        try {
//            CredentialRepresentation credential = buildCredential(newPassword);
//            getAdminRealmResource()
//                    .users()
//                    .get(user.getId())
//                    .resetPassword(credential);
//            log.info("User: password changed successfully for: {}", username);
//        } catch (Exception e) {
//            log.error("User: failed to change password for {}: {}", username, e.getMessage(), e);
//            throw new KeycloakInteractionException("Failed to update password", e);
//        }
//    }
//    /**
//     * Initiates a password reset flow by verifying the user exists.
//     * The actual OTP dispatch is handled by the application layer.
//     * Called by POST /auth/request-password-reset
//     *
//     * @param username username or email for the reset request
//     * @return the UserRepresentation if the user exists, null otherwise
//     */
//    public UserRepresentation requestPasswordReset(String username) {
//        log.debug("User: password reset requested for: {}", username);
//        UserRepresentation user = getUserByUsername(username);
//        if (user == null) {
//            log.warn("User: password reset requested for non-existent user: {}", username);
//        }
//        return user;
//    }
//    /**
//     * Completes password reset after OTP validation.
//     * Called by POST /auth/validate-otp-reset-password
//     * (OTP validation itself is handled in the application layer before calling this)
//     *
//     * @param userId      Keycloak user ID
//     * @param newPassword new password to set after successful OTP verification
//     */
//    public void resetPasswordAfterOtp(String userId, String newPassword) {
//        log.debug("User: finalising OTP password reset for user: {}", userId);
//        try {
//            CredentialRepresentation credential = buildCredential(newPassword);
//            getAdminRealmResource()
//                    .users()
//                    .get(userId)
//                    .resetPassword(credential);
//            log.info("User: OTP password reset completed for user: {}", userId);
//        } catch (Exception e) {
//            log.error("User: OTP password reset failed for {}: {}", userId, e.getMessage(), e);
//            throw new KeycloakInteractionException("Failed to reset password after OTP", e);
//        }
//    }
//    // -------------------------------------------------------------------------
//    // Credential Verification
//    // -------------------------------------------------------------------------
//    /**
//     * Verifies a user's current credentials by attempting a token request.
//     * Used internally before sensitive operations like password change.
//     *
//     * @param username user's username
//     * @param password user's current password
//     * @throws KeycloakInteractionException if credentials are invalid
//     */
//    public void verifyCredentials(String username, String password) {
//        log.debug("User: verifying credentials for: {}", username);
//        try {
//            login(username, password); // reuse login — if it throws, creds are invalid
//            log.info("User: credentials verified for: {}", username);
//        } catch (Exception e) {
//            log.warn("User: credential verification failed for: {}", username);
//            throw new KeycloakInteractionException("Current credentials are invalid", e);
//        }
//    }
//    // -------------------------------------------------------------------------
//    // Private Helpers
//    // -------------------------------------------------------------------------
//    /**
//     * Returns the realm resource using the admin client.
//     * Used only for the narrow set of Admin API calls permitted in this service
//     * (resetPassword, logout). NOT exposed for general admin operations.
//     */
//    private RealmResource getAdminRealmResource() {
//        return keycloakAdminClient.realm(
//                infisicalService.getSecret(KEYCLOAK_REALM, KEYCLOAK_SECRET_TYPE)
//        );
//    }
//    /**
//     * Retrieves a user by username via the Admin API.
//     * Used internally for self-service flows.
//     */
//    private UserRepresentation getUserByUsername(String username) {
//        return getAdminRealmResource()
//                .users()
//                .search(username)
//                .stream()
//                .findFirst()
//                .orElse(null);
//    }
//    private CredentialRepresentation buildCredential(String password) {
//        CredentialRepresentation credential = new CredentialRepresentation();
//        credential.setType(CredentialRepresentation.PASSWORD);
//        credential.setValue(password);
//        credential.setTemporary(false);
//        return credential;
//    }
//}
