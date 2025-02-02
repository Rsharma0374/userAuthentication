package com.userAuthentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userAuthentication.constant.Constants;
import com.userAuthentication.request.*;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.response.EncryptedResponse;
import com.userAuthentication.security.AESUtil;
import com.userAuthentication.service.HomeManager;
import com.userAuthentication.service.redis.RedisService;
import com.userAuthentication.utility.JsonUtils;
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

    @Autowired
    private RedisService redisService;

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private ResponseUtility responseUtility;

    @GetMapping("/hello")
    public String getResult() {
        return "hello\n";
    }

    @PostMapping(EndPointReferrer.LOGIN)
    public ResponseEntity<EncryptedResponse> getLogin(
            @RequestBody @NotNull EncryptedPayload encryptedPayload,
            HttpServletRequest httpRequest) throws Exception {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.LOGIN);
        BaseResponse baseResponse = homeManager.login(encryptedPayload, httpRequest);
        return responseUtility.encryptedResponse(httpRequest, baseResponse);

    }


    @PostMapping(EndPointReferrer.CREATE_USER)
    public ResponseEntity<EncryptedResponse> createUser(
            @RequestBody @NotNull UserCreation userCreation, HttpServletRequest httpRequest) {
        try {
            logger.info(Constants.CONTROLLER_STARTED, EndPointReferrer.CREATE_USER);

            BaseResponse baseResponse = homeManager.createUser(userCreation);
            return responseUtility.encryptedResponse(httpRequest, baseResponse);

        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ", e);
        }
        return null;

    }

    @PostMapping(EndPointReferrer.VALIDATE_2FA_OTP)
    public ResponseEntity<EncryptedResponse> validate2faOtp(
            @RequestBody @NotNull EncryptedPayload encryptedPayload, HttpServletRequest httpRequest) throws Exception {
        try {
            logger.info(Constants.CONTROLLER_STARTED, EndPointReferrer.VALIDATE_2FA_OTP);

            BaseResponse baseResponse = homeManager.validate2faOtp(encryptedPayload, httpRequest);
            return responseUtility.encryptedResponse(httpRequest, baseResponse);

        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ", e);
        }
        return null;

    }

    @PostMapping(EndPointReferrer.LOGOUT)
    public ResponseEntity<EncryptedResponse> logout(
            @RequestBody @NotNull EncryptedPayload encryptedPayload, HttpServletRequest httpServletRequest) {
        try {
            logger.info(Constants.CONTROLLER_STARTED, EndPointReferrer.LOGOUT);

            BaseResponse baseResponse = homeManager.logout(encryptedPayload, httpServletRequest);
            return responseUtility.encryptedResponse(httpServletRequest, baseResponse);
        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ", e);
        }
        return null;

    }

    @PostMapping(EndPointReferrer.FORGET_PASSWORD)
    public ResponseEntity<EncryptedResponse> forgotPassword(
            @RequestBody @NotNull EncryptedPayload payload, HttpServletRequest httpServletRequest) throws Exception {

        logger.info(Constants.CONTROLLER_STARTED, EndPointReferrer.FORGET_PASSWORD);

        BaseResponse baseResponse = homeManager.forgotPassword(payload, httpServletRequest);
        return responseUtility.encryptedResponse(httpServletRequest, baseResponse);

    }

    @GetMapping(EndPointReferrer.VALIDATE_TOKEN + "/{token}")
    public ResponseEntity<BaseResponse> validateToken(@PathVariable("token") String token) {
        BaseResponse baseResponse = responseUtility.getBaseResponse(HttpStatus.OK, "Token validated");
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }


}
