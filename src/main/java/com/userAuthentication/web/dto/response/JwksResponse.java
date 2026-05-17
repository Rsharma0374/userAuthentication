package com.userAuthentication.web.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * JWKS response DTO
 */
@Data
@Builder
public class JwksResponse {
    private String[] keys;
}
