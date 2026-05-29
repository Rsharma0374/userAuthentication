package com.guardianservices.userAuthentication.web.dto.request;

import com.guardianservices.userAuthentication.domain.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ConfirmPasswordResetRequest {
    @NotNull(message = "Request ID is required")
    private UUID requestId;

    @NotBlank(message = "OTP is required")
    private String otp;

    // Optional: Only required if the user exists across multiple products
    // and needs to scope the reset to a specific identity.
    private String product;
}
