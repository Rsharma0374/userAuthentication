package com.userAuthentication.service;

import com.userAuthentication.request.EmailOtpRequest;
import com.userAuthentication.request.EncryptedPayload;
import com.userAuthentication.request.ValidateOtpRequest;
import com.userAuthentication.response.BaseResponse;

import javax.validation.constraints.NotNull;

public interface CommunicationService {

    BaseResponse sendEmailOtp(@NotNull EmailOtpRequest emailOtpRequest);

    BaseResponse validateEmailOtp(@NotNull ValidateOtpRequest validateOtpRequest);

    BaseResponse validateOtpResetPassword(@NotNull EncryptedPayload encryptedPayload);
}
