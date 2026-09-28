package com.guardianservices.userauthentication.profile.controller;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @Size(max = 100)
    private String displayName;

    @Size(max = 10)
    private String locale;

    @Size(max = 50)
    private String timezone;
}