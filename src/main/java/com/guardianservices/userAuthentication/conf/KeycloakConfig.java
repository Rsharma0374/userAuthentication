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

    public static final String KEYKLOAK_SECRET_TYPE = "Keykloak_Secret";
    public static final String KEYKLOAK_URL = "keykloakUrl";
    public static final String KEYKLOAK_REALM = "keykloakRealm";
    public static final String KEYKLOAK_CLIENT_ID = "keykloakClientId";
    public static final String KEYKLOAK_CLIENT_SECRET = "keykloakClientSecret";

    @Autowired
    private InfisicalService infisicalService;

    /**
     * Creates and configures Keycloak admin client bean
     *
     * Uses client credentials flow instead of password grant
     *
     * @return configured Keycloak admin client instance
     */
    @Bean
    public Keycloak keycloakAdminClient() {

        String keykloakUrl = infisicalService.getSecret(KEYKLOAK_URL, KEYKLOAK_SECRET_TYPE);
        String keykloakRealm = infisicalService.getSecret(KEYKLOAK_REALM, KEYKLOAK_SECRET_TYPE);
        String keykloakClientId = infisicalService.getSecret(KEYKLOAK_CLIENT_ID, KEYKLOAK_SECRET_TYPE);
        String keykloakClientSecret = infisicalService.getSecret(KEYKLOAK_CLIENT_SECRET, KEYKLOAK_SECRET_TYPE);

        return KeycloakBuilder.builder()
                .serverUrl(keykloakUrl)
                .realm(keykloakRealm)
                .clientId(keykloakClientId)
                .clientSecret(keykloakClientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}