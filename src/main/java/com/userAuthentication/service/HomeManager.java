package com.userAuthentication.service;

import com.userAuthentication.request.*;
import com.userAuthentication.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;

import javax.validation.constraints.NotNull;


//import javax.servlet.http.HttpServletRequest;

public interface HomeManager {

    BaseResponse login(EncryptedPayload encryptedPayload, HttpServletRequest httpRequest)throws Exception;

    BaseResponse createUser(UserCreation userCreation);

    BaseResponse sendEmailOtp(EmailOtpRequest emailOtpRequest);

    BaseResponse validate2faOtp(EncryptedPayload encryptedPayload, HttpServletRequest httpRequest)throws Exception;

    BaseResponse logout(EncryptedPayload encryptedPayload, HttpServletRequest httpServletRequest);

    BaseResponse forgotPassword(EncryptedPayload payload, HttpServletRequest httpServletRequest);

}
