package com.guardianservices.userauthentication.account.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank
    @Email
    @Size(max = 320)
    private String email;
}