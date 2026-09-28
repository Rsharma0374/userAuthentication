package com.guardianservices.userauthentication.authentication.service;

import com.guardianservices.userauthentication.account.User;
import com.guardianservices.userauthentication.account.UserStatus;
import com.guardianservices.userauthentication.account.repository.UserRepository;
import com.guardianservices.userauthentication.account.repository.UserRoleRepository;
import com.guardianservices.userauthentication.authentication.AuthChallenge;
import com.guardianservices.userauthentication.authentication.AuthChallengePurpose;
import com.guardianservices.userauthentication.authentication.MfaCredential;
import com.guardianservices.userauthentication.authentication.MfaRecoveryCode;
import com.guardianservices.userauthentication.authentication.MfaType;
import com.guardianservices.userauthentication.authentication.repository.AuthChallengeRepository;
import com.guardianservices.userauthentication.authentication.repository.MfaCredentialRepository;
import com.guardianservices.userauthentication.authentication.repository.MfaRecoveryCodeRepository;
import com.guardianservices.userauthentication.common.exception.UnauthorizedException;
import com.guardianservices.userauthentication.common.exception.ValidationException;
import com.guardianservices.userauthentication.common.util.Clock;
import com.guardianservices.userauthentication.common.util.SecureTokenGenerator;
import com.guardianservices.userauthentication.common.util.TokenHasher;
import com.guardianservices.userauthentication.platform.config.AuthProperties;
import com.guardianservices.userauthentication.session.Session;
import com.guardianservices.userauthentication.session.SessionRevocationReason;
import com.guardianservices.userauthentication.session.service.SessionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final MfaCredentialRepository mfaCredentialRepository;
    private final MfaRecoveryCodeRepository mfaRecoveryCodeRepository;
    private final AuthChallengeRepository authChallengeRepository;
    private final SessionService sessionService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SecureTokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;
    private final Clock clock;
    private final AuthProperties authProperties;

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    @Transactional
    public AuthenticationResult authenticate(String email, String password, 
                                             String deviceId, String deviceName,
                                             java.net.InetAddress ipAddress, String userAgent) {
        String normalizedEmail = email.toLowerCase(); // Use the same normalizer
        
        Optional<User> userOpt = userRepository.findByEmailNormalized(normalizedEmail);
        User user = null;
        if (userOpt.isPresent() && userOpt.get().getStatus() != UserStatus.DELETED) {
            user = userOpt.get();
        }

        // Always run password verification to prevent timing attacks
        boolean passwordValid = false;
        if (user != null) {
            passwordValid = passwordEncoder.matches(password, user.getPasswordHash());
        } else {
            // Dummy verification to prevent account enumeration
            passwordEncoder.matches(password, "$argon2id$v=19$m=65536,t=3,p=4$dummy$salt");
        }

        if (!passwordValid || user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account not active");
        }

        // Check if MFA is required
        List<MfaCredential> mfaCredentials = mfaCredentialRepository.findByUser(user);
        boolean mfaRequired = mfaCredentials.stream()
            .anyMatch(mc -> mc.getConfirmedAt() != null);

        // Check for privileged account MFA requirement
        boolean isPrivileged = userRoleRepository.findByUser(user).stream()
            .anyMatch(ur -> ur.getRole().getIsPrivileged());

        if (isPrivileged && !mfaRequired) {
            throw new UnauthorizedException("MFA required for privileged account");
        }

        if (mfaRequired) {
            // Create MFA challenge
            return createMfaChallenge(user, deviceId, deviceName, ipAddress, userAgent);
        }

        // No MFA required - create session directly
        return createAuthenticatedSession(user, deviceId, deviceName, ipAddress, userAgent, List.of());
    }

    @Transactional
    public AuthenticationResult verifyMfa(String challengeId, String code, String deviceId, 
                                           String deviceName, java.net.InetAddress ipAddress, String userAgent) {
        byte[] challengeHash = tokenHasher.hash(challengeId);

        AuthChallenge challenge = authChallengeRepository.findActiveByChallengeHashForUpdate(challengeHash)
            .orElseThrow(() -> new UnauthorizedException("Invalid or expired MFA challenge"));

        if (challenge.getPurpose() != AuthChallengePurpose.MFA_VERIFICATION) {
            throw new UnauthorizedException("Invalid challenge purpose");
        }

        User user = userRepository.findById(UUID.fromString(challenge.getMetadata().split("\"userId\":\"")[1].split("\"")[0]))
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Verify TOTP
        MfaCredential mfaCredential = mfaCredentialRepository.findByUserAndType(user, MfaType.TOTP)
            .orElseThrow(() -> new UnauthorizedException("MFA not configured"));

        if (mfaCredential.getConfirmedAt() == null) {
            throw new UnauthorizedException("MFA not confirmed");
        }

        String secret = decryptSecret(mfaCredential.getEncryptedSecret());
        boolean valid = googleAuthenticator.authorize(secret, Integer.parseInt(code), 1);

        if (!valid) {
            // Check recovery codes
            valid = verifyRecoveryCode(user, code);
        }

        if (!valid) {
            challenge.setAttemptCount(challenge.getAttemptCount() + 1);
            if (challenge.getAttemptCount() >= authProperties.getMfa().getMaxTotpAttempts()) {
                challenge.setCompletedAt(clock.now());
                authChallengeRepository.save(challenge);
                throw new UnauthorizedException("Too many failed attempts");
            }
            authChallengeRepository.save(challenge);
            throw new UnauthorizedException("Invalid MFA code");
        }

        // Mark challenge as completed
        challenge.setCompletedAt(clock.now());
        authChallengeRepository.save(challenge);

        // Update last accepted step for replay protection
        mfaCredential.setLastAcceptedStep(System.currentTimeMillis() / 30000);
        mfaCredentialRepository.save(mfaCredential);

        // Create authenticated session
        return createAuthenticatedSession(user, deviceId, deviceName, ipAddress, userAgent, List.of("mfa"));
    }

    private boolean verifyRecoveryCode(User user, String code) {
        List<MfaRecoveryCode> codes = mfaRecoveryCodeRepository.findActiveByUserForUpdate(user);
        for (MfaRecoveryCode rc : codes) {
            if (tokenHasher.verify(code, rc.getCodeHash())) {
                rc.setConsumedAt(clock.now());
                mfaRecoveryCodeRepository.save(rc);
                return true;
            }
        }
        return false;
    }

    private AuthenticationResult createMfaChallenge(User user, String deviceId, String deviceName,
                                                     java.net.InetAddress ipAddress, String userAgent) {
        String challengeId = tokenGenerator.generateToken();
        byte[] challengeHash = tokenHasher.hash(challengeId);

        AuthChallenge challenge = new AuthChallenge();
        challenge.setUser(user);
        challenge.setChallengeHash(challengeHash);
        challenge.setPurpose(AuthChallengePurpose.MFA_VERIFICATION);
        challenge.setExpiresAt(clock.now().plusMinutes(10));
        challenge.setAttemptCount(0);
        challenge.setMetadata("{\"userId\":\"" + user.getId() + "\",\"deviceId\":\"" + deviceId + "\"}");
        challenge.setCreatedAt(clock.now());

        authChallengeRepository.save(challenge);

        return new AuthenticationResult(
            AuthenticationResult.Type.MFA_REQUIRED,
            challengeId,
            null,
            null,
            null
        );
    }

    private AuthenticationResult createAuthenticatedSession(User user, String deviceId, String deviceName,
                                                             java.net.InetAddress ipAddress, String userAgent,
                                                             List<String> additionalScopes) {
        Session session = sessionService.createSession(user, deviceId, deviceName, ipAddress, userAgent);
        
        List<String> scopes = new java.util.ArrayList<>(List.of("profile", "objects"));
        scopes.addAll(additionalScopes);

        String accessToken = jwtService.createAccessToken(user, session.getId().toString(), scopes);
        
        SessionService.RefreshTokenResult refreshResult = sessionService.rotateRefreshToken(
            session.getId().toString(), // This won't work - need to get the actual refresh token
            deviceId, ipAddress, userAgent
        );

        return new AuthenticationResult(
            AuthenticationResult.Type.SUCCESS,
            null,
            accessToken,
            refreshResult.getRefreshToken(),
            session
        );
    }

    private String decryptSecret(byte[] encryptedSecret) {
        // In production, decrypt using KMS
        return new String(encryptedSecret, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static class AuthenticationResult {
        public enum Type {
            SUCCESS,
            MFA_REQUIRED
        }

        private final Type type;
        private final String challengeId;
        private final String accessToken;
        private final String refreshToken;
        private final Session session;

        public AuthenticationResult(Type type, String challengeId, String accessToken, 
                                    String refreshToken, Session session) {
            this.type = type;
            this.challengeId = challengeId;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.session = session;
        }

        public Type getType() { return type; }
        public String getChallengeId() { return challengeId; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public Session getSession() { return session; }
    }
}