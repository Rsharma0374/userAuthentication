package com.chat.userAuthentication.service;

import com.chat.userAuthentication.request.EmailOtpRequest;
import com.chat.userAuthentication.request.LoginRequest;
import com.chat.userAuthentication.request.UserCreation;
import com.chat.userAuthentication.request.ValidateOtpRequest;
import com.chat.userAuthentication.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;


//import javax.servlet.http.HttpServletRequest;

public interface HomeManager {

    BaseResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest)throws Exception;

    BaseResponse createUser(UserCreation userCreation);

    BaseResponse sendOtp(EmailOtpRequest emailOtpRequest) throws Exception;

    BaseResponse validateOtp(ValidateOtpRequest validateOtpRequest);
}
