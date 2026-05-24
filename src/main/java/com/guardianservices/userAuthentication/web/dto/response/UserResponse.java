package com.guardianservices.userAuthentication.web.dto.response;

import com.guardianservices.userAuthentication.domain.model.Product;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<Product> products;;
    private LocalDateTime createdAt;
}
