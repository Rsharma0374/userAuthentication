package com.userAuthentication.web.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private long refreshExpiresIn;
    private String tokenType;
}
