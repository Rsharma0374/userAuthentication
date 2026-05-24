package com.guardianservices.userAuthentication.web.dto.request;

import com.guardianservices.userAuthentication.domain.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateUserRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Role is required")
    private String role;

    // This can be null if the user is being updated to a role without products (e.g., SUPER_ADMIN)
    private Product productName;
}
