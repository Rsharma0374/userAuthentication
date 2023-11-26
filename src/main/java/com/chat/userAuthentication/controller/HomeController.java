package com.chat.userAuthentication.controller;

import com.chat.userAuthentication.request.EmailOtpRequest;
import com.chat.userAuthentication.request.LoginRequest;
import com.chat.userAuthentication.request.UserCreation;
import com.chat.userAuthentication.request.ValidateOtpRequest;
import com.chat.userAuthentication.response.BaseResponse;
import com.chat.userAuthentication.service.HomeManager;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
//import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/user")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private HomeManager homeManager;

    //
    @GetMapping("/hello")
    public String getResult(){
        return "hello";
    }

    @PostMapping(EndPointReferrer.LOGIN)
    public ResponseEntity<BaseResponse> getLogin(
            @Validated(value = {LoginRequest.FetchGrp.class})
            @RequestBody @NotNull LoginRequest loginRequest,
            HttpServletRequest httpRequest) throws Exception {

        logger.debug("{} controller started",EndPointReferrer.LOGIN);

        return new ResponseEntity<>(homeManager.login(loginRequest, httpRequest), HttpStatus.OK);

    }

    @PostMapping(EndPointReferrer.NEW_USER)
    public ResponseEntity<BaseResponse> createUser(
            @RequestBody @NotNull UserCreation userCreation) throws Exception {
        try {
            logger.debug("{} controller started",EndPointReferrer.NEW_USER);

            return new ResponseEntity<>(homeManager.createUser(userCreation), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ");
        }
        return null;

    }

    @PostMapping(EndPointReferrer.SEND_EMAIL_OTP)
    public ResponseEntity<BaseResponse> sendEmailOtp(
            @RequestBody @NotNull EmailOtpRequest emailOtpRequest) {
        try {
            logger.debug("{} controller started",EndPointReferrer.SEND_EMAIL_OTP);

            return new ResponseEntity<>(homeManager.sendOtp(emailOtpRequest), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ");
        }
        return null;

    }

    @PostMapping(EndPointReferrer.VALIDATE_EMAIL_OTP)
    public ResponseEntity<BaseResponse> validateEmailOtp(
            @RequestBody @NotNull ValidateOtpRequest validateOtpRequest) {
        try {
            logger.debug("{} controller started",EndPointReferrer.VALIDATE_EMAIL_OTP);

            return new ResponseEntity<>(homeManager.validateOtp(validateOtpRequest), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ");
        }
        return null;

    }

}
