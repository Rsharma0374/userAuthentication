//package com.guardianservices.userAuthentication.conf;
//
//import com.guardianservices.userAuthentication.application.service.AdminKeycloakService;
//import com.guardianservices.userAuthentication.application.service.AuthService;
//import com.guardianservices.userAuthentication.application.service.InfisicalService;
//import com.guardianservices.userAuthentication.domain.port.out.AuditEventPublisher;
//import com.guardianservices.userAuthentication.domain.port.out.UserRepository;
//import org.keycloak.admin.client.Keycloak;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.client.RestTemplate;
//
//@Configuration
//public class ServiceConfig {
//
//    @Bean(name = "adminAuthService")   // ← correct way to name the bean
//    public AuthService adminAuthService(
//            @Qualifier("keycloakAdminClient") Keycloak keycloakAdminClient,
//            UserRepository userRepository,
//            RestTemplate restTemplate,
//            AuditEventPublisher eventPublisher,
//            InfisicalService infisicalService,
//            AdminKeycloakService adminKeycloakService) {
//        return new AuthService(
//                keycloakAdminClient,
//                userRepository,
//                restTemplate,
//                eventPublisher,
//                infisicalService,
//                adminKeycloakService
//        );
//    }
//    @Bean(name = "userAuthService")    // ← correct way to name the bean
//    public AuthService userAuthService(
//            @Qualifier("keycloakUserClient") Keycloak keycloakUserClient,
//            UserRepository userRepository,
//            RestTemplate restTemplate,
//            AuditEventPublisher eventPublisher,
//            InfisicalService infisicalService) {
//        return new AuthService(
//                keycloakUserClient,
//                userRepository,
//                restTemplate,
//                eventPublisher,
//                infisicalService
//        );
//    }
//}
