package com.guardianservices.userauthentication.objectstorage.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompleteUploadRequest {

    @NotBlank
    @Size(min = 36, max = 36)
    private String versionId;
}