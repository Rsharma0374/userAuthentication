package com.guardianservices.userAuthentication.conf;

import com.guardianservices.userAuthentication.application.service.InfisicalService;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Keycloak admin client
 * Provides programmatic access to Keycloak administration APIs
 */
@Configuration
public class KeycloakConfig {

    // --- Secret type constants ---
    public static final String KEYCLOAK_SECRET_TYPE = "Keycloak_Secret";
    // --- Shared ---
    public static final String KEYCLOAK_URL   = "keycloakUrl";
    public static final String KEYCLOAK_REALM = "keycloakRealm";
    // --- Admin Client ---
    public static final String KEYCLOAK_ADMIN_CLIENT_ID     = "keycloakAdminClientId";
    public static final String KEYCLOAK_ADMIN_CLIENT_SECRET = "keycloakAdminClientSecret";
    // --- User Client ---
    public static final String KEYCLOAK_USER_CLIENT_ID      = "keycloakUserClientId";
    public static final String KEYCLOAK_USER_CLIENT_SECRET  = "keycloakUserClientSecret";

    @Autowired
    private InfisicalService infisicalService;

    /**
     * Admin Keycloak client — used for realm management,
     * creating users, assigning roles, etc.
     * Grant type: CLIENT_CREDENTIALS (no human login)
     */
    @Bean(name = "keycloakAdminClient")
    public Keycloak keycloakAdminClient() {
        String url          = infisicalService.getSecret(KEYCLOAK_URL, KEYCLOAK_SECRET_TYPE);
        String realm        = infisicalService.getSecret(KEYCLOAK_REALM, KEYCLOAK_SECRET_TYPE);
        String clientId     = infisicalService.getSecret(KEYCLOAK_ADMIN_CLIENT_ID, KEYCLOAK_SECRET_TYPE);
        String clientSecret = infisicalService.getSecret(KEYCLOAK_ADMIN_CLIENT_SECRET, KEYCLOAK_SECRET_TYPE);
        return KeycloakBuilder.builder()
                .serverUrl(url)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
    /**
     * User Keycloak client — used for user login, token exchange,
     * password resets, etc.
     * Grant type: PASSWORD (acts on behalf of a user)
     */
    @Bean(name = "keycloakUserClient")
    public Keycloak keycloakUserClient() {
        String url          = infisicalService.getSecret(KEYCLOAK_URL, KEYCLOAK_SECRET_TYPE);
        String realm        = infisicalService.getSecret(KEYCLOAK_REALM, KEYCLOAK_SECRET_TYPE);
        String clientId     = infisicalService.getSecret(KEYCLOAK_USER_CLIENT_ID, KEYCLOAK_SECRET_TYPE);
        String clientSecret = infisicalService.getSecret(KEYCLOAK_USER_CLIENT_SECRET, KEYCLOAK_SECRET_TYPE);
        return KeycloakBuilder.builder()
                .serverUrl(url)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)  // or AUTHORIZATION_CODE for web
                .build();
    }
}