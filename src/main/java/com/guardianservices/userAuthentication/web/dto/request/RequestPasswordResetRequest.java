package com.guardianservices.userAuthentication.web.dto.request;

import com.guardianservices.userAuthentication.domain.model.Product;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestPasswordResetRequest {
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    // Optional: Only required if the user exists across multiple products
    // and needs to scope the reset to a specific identity.
    private String product;
}
