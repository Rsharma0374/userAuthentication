package com.userAuthentication.service;

import com.userAuthentication.request.EmailOtpRequest;
import com.userAuthentication.request.EncryptedPayload;
import com.userAuthentication.request.ValidateOtpRequest;
import com.userAuthentication.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;

import javax.validation.constraints.NotNull;

public interface CommunicationService {

    BaseResponse sendEmailOtp(@NotNull EncryptedPayload encryptedPayload, HttpServletRequest httpServletRequest);

    BaseResponse validateEmailOtp(@NotNull EncryptedPayload encryptedPayload, HttpServletRequest request);

    BaseResponse validateOtpResetPassword(@NotNull EncryptedPayload encryptedPayload, HttpServletRequest request);
}
