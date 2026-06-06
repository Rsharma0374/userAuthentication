package com.guardianservices.userAuthentication.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private long refreshExpiresIn;
    private String tokenType;
    private UUID userId;
    private String email;
    private Boolean emailVerified;
    private Boolean mfaEnabled;
    private String role;
    private String product;
}
