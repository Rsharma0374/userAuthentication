package com.userAuthentication.controller;

import com.userAuthentication.constant.Constants;
import com.userAuthentication.request.EmailOtpRequest;
import com.userAuthentication.request.EncryptedPayload;
import com.userAuthentication.request.ValidateOtpRequest;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.service.CommunicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/communications")
public class CommunicationController {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationController.class);

    @Autowired
    private CommunicationService communicationService;

    @PostMapping(EndPointReferrer.SEND_EMAIL_OTP)
    private ResponseEntity<BaseResponse> sendEmailOtp(@RequestBody @NotNull EmailOtpRequest emailOtpRequest) {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.SEND_EMAIL_OTP);

        return new ResponseEntity<>(communicationService.sendEmailOtp(emailOtpRequest), HttpStatus.OK);

    }

    @PostMapping(EndPointReferrer.VALIDATE_EMAIL_OTP)
    private ResponseEntity<BaseResponse> validateEmailOtp(@RequestBody @NotNull EncryptedPayload encryptedPayload) {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.VALIDATE_EMAIL_OTP);

        return new ResponseEntity<>(communicationService.validateEmailOtp(encryptedPayload), HttpStatus.OK);

    }

    @PostMapping(EndPointReferrer.VALIDATE_OTP_RESET_PASSWORD)
    private ResponseEntity<BaseResponse> validateOtpResetPassword(@RequestBody @NotNull EncryptedPayload encryptedPayload) {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.VALIDATE_OTP_RESET_PASSWORD);

        return new ResponseEntity<>(communicationService.validateOtpResetPassword(encryptedPayload), HttpStatus.OK);

    }
}
