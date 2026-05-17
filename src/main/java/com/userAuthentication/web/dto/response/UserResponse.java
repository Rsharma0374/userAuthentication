package com.userAuthentication.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean emailVerified;
    private boolean mfaEnabled;
    private String[] roles;
    private LocalDateTime createdAt;
}
