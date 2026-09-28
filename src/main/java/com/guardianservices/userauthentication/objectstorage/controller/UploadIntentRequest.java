package com.guardianservices.userauthentication.objectstorage.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UploadIntentRequest {

    @NotNull
    private com.guardianservices.userauthentication.objectstorage.ObjectPurpose purpose;

    @NotBlank
    @Size(max = 100)
    private String contentType;

    @NotNull
    @Positive
    private Long size;
}