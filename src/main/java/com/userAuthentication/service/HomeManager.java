package com.userAuthentication.service;

import com.userAuthentication.request.*;
import com.userAuthentication.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;

import javax.validation.constraints.NotNull;


//import javax.servlet.http.HttpServletRequest;

public interface HomeManager {

    BaseResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest)throws Exception;

    BaseResponse createUser(UserCreation userCreation);

    BaseResponse sendEmailOtp(EmailOtpRequest emailOtpRequest);

    BaseResponse validate2faOtp(ValidateOtpRequest validateOtpRequest);

    BaseResponse logout(LogoutRequest logoutRequest, HttpServletRequest httpServletRequest);

    BaseResponse forgotPassword(EncryptedPayload payload);
//
//    BaseResponse sendForgotOtp(EmailOtpRequest emailOtpRequest) throws Exception;
//
//    BaseResponse validateOtpAndResetPassword(ValidateOtpRequest validateOtpRequest);
//
//    BaseResponse validateOtp(ValidateOtpRequest validateOtpRequest);
//
//    BaseResponse getTokenByKey(String key);
//
//    BaseResponse clearTokenByKey(String key);
}
