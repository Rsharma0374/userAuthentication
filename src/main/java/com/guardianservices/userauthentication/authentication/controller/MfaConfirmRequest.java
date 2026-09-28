package com.guardianservices.userauthentication.authentication.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MfaConfirmRequest {

    @NotBlank
    @Size(min = 6, max = 8)
    private String code;
}