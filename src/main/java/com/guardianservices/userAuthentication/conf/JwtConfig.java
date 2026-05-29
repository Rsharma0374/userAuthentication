package com.guardianservices.userAuthentication.conf;

import com.guardianservices.userAuthentication.application.service.InfisicalService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import static com.guardianservices.userAuthentication.conf.KeycloakConfig.*;

@Configuration
public class JwtConfig {

    private final InfisicalService infisicalService;

    public JwtConfig(InfisicalService infisicalService) {
        this.infisicalService = infisicalService;
    }

    @Bean
    @Qualifier("adminJwtDecoder")
    public JwtDecoder adminJwtDecoder() {
        String jwkSetUri = String.format("%s/realms/%s/protocol/openid-connect/certs",
                infisicalService.getSecret(KEYCLOAK_URL, KEYCLOAK_SECRET_TYPE),
                infisicalService.getSecret(KEYCLOAK_REALM, KEYCLOAK_SECRET_TYPE));
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    @Qualifier("userJwtDecoder")
    public JwtDecoder userJwtDecoder() {
        String jwkSetUri = String.format("%s/realms/%s/protocol/openid-connect/certs",
                infisicalService.getSecret(KEYCLOAK_URL, KEYCLOAK_SECRET_TYPE),
                infisicalService.getSecret(KEYCLOAK_REALM, KEYCLOAK_SECRET_TYPE));
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
