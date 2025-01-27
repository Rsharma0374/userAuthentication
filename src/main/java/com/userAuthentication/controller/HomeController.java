/**
 * The HomeController class in the userAuthentication package handles various user authentication endpoints and operations.
 */
package com.userAuthentication.controller;

import com.userAuthentication.constant.Constants;
import com.userAuthentication.request.EmailOtpRequest;
import com.userAuthentication.request.LoginRequest;
import com.userAuthentication.request.UserCreation;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.service.HomeManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private HomeManager homeManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/hello")
    public String getResult(){
        return "hello\n";
    }


    @PostMapping(EndPointReferrer.SEND_EMAIL_OTP)
    public ResponseEntity<BaseResponse> sendEmailOtp(
            @RequestBody @NotNull EmailOtpRequest emailOtpRequest) {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.SEND_EMAIL_OTP);

        return new ResponseEntity<>(homeManager.sendEmailOtp(emailOtpRequest), HttpStatus.OK);

    }


}
