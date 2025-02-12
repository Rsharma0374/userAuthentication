package com.userAuthentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userAuthentication.constant.Constants;
import com.userAuthentication.request.*;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.response.EncryptedResponse;
import com.userAuthentication.security.AESUtil;
import com.userAuthentication.service.HomeManager;
import com.userAuthentication.service.JWTService;
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

    @Autowired
    private JWTService jwtService;

    @GetMapping("/hello")
    public String getResult() {
        return "hello\n";
    }

    @PostMapping(EndPointReferrer.LOGIN)
    public ResponseEntity<BaseResponse> getLogin(
            @RequestBody @NotNull LoginRequest loginRequest,
            HttpServletRequest httpRequest) throws Exception {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.LOGIN);
        BaseResponse baseResponse = homeManager.login(loginRequest, httpRequest);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);

    }


    @PostMapping(EndPointReferrer.CREATE_USER)
    public ResponseEntity<BaseResponse> createUser(
            @RequestBody @NotNull UserCreation userCreation, HttpServletRequest httpRequest) {
        try {
            logger.info(Constants.CONTROLLER_STARTED, EndPointReferrer.CREATE_USER);

            BaseResponse baseResponse = homeManager.createUser(userCreation, httpRequest);
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ", e);
        }
        return null;

    }

    @PostMapping(EndPointReferrer.VALIDATE_2FA_OTP)
    public ResponseEntity<BaseResponse> validate2faOtp(
            @RequestBody @NotNull ValidateOtpRequest validateOtpRequest, HttpServletRequest httpRequest) throws Exception {
        try {
            logger.info(Constants.CONTROLLER_STARTED, EndPointReferrer.VALIDATE_2FA_OTP);

            BaseResponse baseResponse = homeManager.validate2faOtp(validateOtpRequest, httpRequest);
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);

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

            BaseResponse baseResponse = homeManager.logout(logoutRequest, httpServletRequest);
            return new ResponseEntity<>(baseResponse, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ", e);
        }
        return null;

    }

    @PostMapping(EndPointReferrer.FORGET_PASSWORD)
    public ResponseEntity<BaseResponse> forgotPassword(
            @RequestBody @NotNull ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest httpServletRequest) throws Exception {

        logger.info(Constants.CONTROLLER_STARTED, EndPointReferrer.FORGET_PASSWORD);

        BaseResponse baseResponse = homeManager.forgotPassword(forgotPasswordRequest, httpServletRequest);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);

    }

    @GetMapping(EndPointReferrer.VALIDATE_TOKEN + "/{token}" + "/{username}")
    public ResponseEntity<Boolean> validateToken(@PathVariable("token") String token,
                                                 @PathVariable("username") String username) {
        String jwtToken = (String) redisService.getValueFromRedis(token);
        boolean isTokenValid = jwtService.validateToken(jwtToken, username);
        return new ResponseEntity<>(isTokenValid, HttpStatus.OK);
    }


}
