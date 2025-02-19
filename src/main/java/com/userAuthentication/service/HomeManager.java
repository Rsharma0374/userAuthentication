package com.userAuthentication.service;

import com.userAuthentication.request.*;
import com.userAuthentication.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;

import javax.validation.constraints.NotNull;


//import javax.servlet.http.HttpServletRequest;

public interface HomeManager {

    BaseResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest)throws Exception;

    BaseResponse createUser(UserCreation userCreation, HttpServletRequest httpRequest)throws Exception;

    BaseResponse sendEmailOtp(EmailOtpRequest emailOtpRequest);

    BaseResponse validate2faOtp(ValidateOtpRequest validateOtpRequest, HttpServletRequest httpRequest)throws Exception;

    BaseResponse logout(LogoutRequest logoutRequest, HttpServletRequest httpServletRequest);

    BaseResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest httpServletRequest);

//    BaseResponse getEmailConfigByType(String type);

    BaseResponse changePassword(@NotNull ChangePasswordRequest changePasswordRequest, HttpServletRequest httpServletRequest);
}
