package com.guardianservices.userAuthentication.web.dto.request;

import com.guardianservices.userAuthentication.domain.model.Product;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // Product can be null for SUPER_ADMIN login
    private String product;
}
