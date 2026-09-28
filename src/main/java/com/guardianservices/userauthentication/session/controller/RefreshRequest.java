package com.guardianservices.userauthentication.session.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefreshRequest {

    @NotBlank
    @Size(min = 32, max = 128)
    private String refreshToken;

    private String deviceId;
    private String deviceName;
}