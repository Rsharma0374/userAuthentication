//package com.guardianservices.userAuthentication.application.service;
//
//
//import com.guardianservices.userAuthentication.application.command.EnableMfaCommand;
//import com.guardianservices.userAuthentication.application.command.VerifyMfaCommand;
//import com.guardianservices.userAuthentication.domain.model.User;
//import com.guardianservices.userAuthentication.domain.port.out.UserRepository;
//import com.warrenstrange.googleauth.GoogleAuthenticator;
//import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
///**
// * Service for Multi-Factor Authentication (MFA) management
// * Uses Google Authenticator for TOTP (Time-based One-Time Password)
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class MfaService {
//
//    private final UserRepository userRepository;
//    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
//
//    /**
//     * Enables MFA for a user
//     * Generates a secret key and returns it as a QR code URI
//     *
//     * @param command enable MFA command with user ID
//     * @return secret key and QR code URI for configuring authenticator app
//     */
//    @Transactional
//    public MfaSetupResponse enableMfa(EnableMfaCommand command) {
//        log.debug("Enabling MFA for user: {}", command.userId());
//
//        User user = userRepository.findById(command.userId())
//                .orElseThrow(() -> new RuntimeException("User not found: " + command.userId()));
//
//        // Generate secret key for Google Authenticator
//        GoogleAuthenticatorKey secretKey = googleAuthenticator.createCredentials();
//
//        // Store secret in user record
//        user.setMfaEnabled(true);
//        user.setMfaSecret(secretKey.getKey());
//        userRepository.save(user);
//
//        // Generate QR code URI for authenticator app
//        String qrCodeUri = generateQrCodeUri(secretKey.getKey(), user.getEmail());
//
//        log.info("MFA enabled for user: {}", command.userId());
//
//        return MfaSetupResponse.builder()
//                .secretKey(secretKey.getKey())
//                .qrCodeUri(qrCodeUri)
//                .build();
//    }
//
//    /**
//     * Verifies MFA code during login
//     *
//     * @param command verify MFA command with user ID and code
//     * @return true if code is valid
//     */
//    @Transactional(readOnly = true)
//    public boolean verifyMfaCode(VerifyMfaCommand command) {
//        log.debug("Verifying MFA code for user: {}", command.userId());
//
//        User user = userRepository.findById(command.userId())
//                .orElseThrow(() -> new RuntimeException("User not found: " + command.userId()));
//
//        if (!user.getMfaEnabled()) {
//            log.warn("MFA not enabled for user: {}", command.userId());
//            return false;
//        }
//
//        // Verify the code against the stored secret
//        boolean isValid = googleAuthenticator.authorize(
//                user.getMfaSecret(),
//                Integer.parseInt(command.code())
//        );
//
//        if (isValid) {
//            log.info("MFA code verified for user: {}", command.userId());
//        } else {
//            log.warn("Invalid MFA code for user: {}", command.userId());
//        }
//
//        return isValid;
//    }
//
//    /**
//     * Disables MFA for a user
//     *
//     * @param userId user ID
//     */
//    @Transactional
//    public void disableMfa(UUID userId) {
//        log.debug("Disabling MFA for user: {}", userId);
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
//
//        user.setMfaEnabled(false);
//        user.setMfaSecret(null);
//        userRepository.save(user);
//
//        log.info("MFA disabled for user: {}", userId);
//    }
//
//    /**
//     * Generates QR code URI for Google Authenticator
//     *
//     * @param secret secret key
//     * @param email user email (used as account name)
//     * @return QR code URI
//     */
//    private String generateQrCodeUri(String secret, String email) {
//        String issuer = "UAM-Service";
//        return String.format(
//                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
//                issuer, email, secret, issuer
//        );
//    }
//
//    /**
//     * Response object for MFA setup
//     */
//    @lombok.Builder
//    @lombok.Data
//    public static class MfaSetupResponse {
//        private String secretKey;
//        private String qrCodeUri;
//    }
//}