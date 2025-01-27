package com.userAuthentication.controller;

import com.userAuthentication.constant.Constants;
import com.userAuthentication.request.*;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.service.HomeManager;
import com.userAuthentication.utility.ResponseUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private HomeManager homeManager;

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/hello")
    public String getResult(){
        return "hello\n";
    }

    @PostMapping(EndPointReferrer.LOGIN)
    public ResponseEntity<BaseResponse> getLogin(
            @RequestBody @NotNull EncryptedPayload encryptedPayload,
            HttpServletRequest httpRequest) throws Exception {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.LOGIN);
        return new ResponseEntity<>(homeManager.login(encryptedPayload, httpRequest), HttpStatus.OK);

    }

    @PostMapping(EndPointReferrer.CREATE_USER)
    public ResponseEntity<BaseResponse> createUser(
            @RequestBody @NotNull UserCreation userCreation) {
        try {
            logger.info(Constants.CONTROLLER_STARTED,EndPointReferrer.CREATE_USER);

            return new ResponseEntity<>(homeManager.createUser(userCreation), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ", e);
        }
        return null;

    }

    @PostMapping(EndPointReferrer.VALIDATE_2FA_OTP)
    public ResponseEntity<BaseResponse> validate2faOtp(
            @RequestBody @NotNull EncryptedPayload encryptedPayload) {
        try {
            logger.info(Constants.CONTROLLER_STARTED,EndPointReferrer.VALIDATE_2FA_OTP);

            return new ResponseEntity<>(homeManager.validate2faOtp(encryptedPayload), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ", e);
        }
        return null;

    }

    @PostMapping(EndPointReferrer.LOGOUT)
    public ResponseEntity<BaseResponse> logout(
            @RequestBody @NotNull LogoutRequest logoutRequest, HttpServletRequest httpServletRequest) {
        try {
            logger.info(Constants.CONTROLLER_STARTED, EndPointReferrer.LOGOUT);

            return new ResponseEntity<>(homeManager.logout(logoutRequest, httpServletRequest), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ", e);
        }
        return null;

    }

    @PostMapping(EndPointReferrer.FORGET_PASSWORD)
    public ResponseEntity<BaseResponse> forgotPassword(
            @RequestBody @NotNull EncryptedPayload payload) {

            logger.info(Constants.CONTROLLER_STARTED, EndPointReferrer.FORGET_PASSWORD);

            return new ResponseEntity<>(homeManager.forgotPassword(payload), HttpStatus.OK);


    }

    @GetMapping(EndPointReferrer.VALIDATE_TOKEN + "/{token}" )
    public ResponseEntity<BaseResponse> validateToken(@PathVariable("token") String token) {
        BaseResponse baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, "Token validated");
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

}
