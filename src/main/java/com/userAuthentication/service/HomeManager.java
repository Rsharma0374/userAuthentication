package com.userAuthentication.service;

import com.userAuthentication.request.EmailOtpRequest;
import com.userAuthentication.request.LoginRequest;
import com.userAuthentication.request.UserCreation;
import com.userAuthentication.request.ValidateOtpRequest;
import com.userAuthentication.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;


//import javax.servlet.http.HttpServletRequest;

public interface HomeManager {

    BaseResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest)throws Exception;

    BaseResponse createUser(UserCreation userCreation);

    BaseResponse sendEmailOtp(EmailOtpRequest emailOtpRequest);

    BaseResponse validate2faOtp(ValidateOtpRequest validateOtpRequest);
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
