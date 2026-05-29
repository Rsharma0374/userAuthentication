//package com.guardianservices.userAuthentication.web.controller.admin;
//
//import com.guardianservices.userAuthentication.web.dto.response.JwksResponse;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.io.ResourceLoader;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * Controller for serving JWKS (JSON Web Key Set)
// * Used for JWT signature verification
// */
//@RestController
//@RequestMapping("/jwks")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "JWKS", description = "JSON Web Key Set endpoints")
//public class JwksController {
//
//    private final ResourceLoader resourceLoader;
//
//    /**
//     * Returns the JWKS for JWT verification
//     * Proxies to Keycloak's JWKS endpoint
//     *
//     * @return JWKS response
//     */
//    @GetMapping
//    @Operation(summary = "Get JWKS", description = "Retrieves the JSON Web Key Set for token verification")
//    public ResponseEntity<JwksResponse> getJwks() {
//        log.debug("Retrieving JWKS");
//
//        // In production, this should fetch from Keycloak's JWKS endpoint
//        // For demo purposes, return empty response
//        JwksResponse response = JwksResponse.builder()
//                .keys(new String[]{})
//                .build();
//
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Returns the public key for JWT verification
//     *
//     * @return public key as string
//     */
//    @GetMapping("/public-key")
//    @Operation(summary = "Get public key", description = "Retrieves the public key for token verification")
//    public ResponseEntity<String> getPublicKey() {
//        log.debug("Retrieving public key");
//
//        // In production, fetch from Keycloak
//        String publicKey = "-----BEGIN PUBLIC KEY-----\n" +
//                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\n" +
//                "-----END PUBLIC KEY-----";
//
//        return ResponseEntity.ok(publicKey);
//    }
//}
