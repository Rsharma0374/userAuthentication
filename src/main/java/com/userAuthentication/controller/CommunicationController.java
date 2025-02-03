package com.userAuthentication.controller;

import com.userAuthentication.constant.Constants;
import com.userAuthentication.request.EmailOtpRequest;
import com.userAuthentication.request.EncryptedPayload;
import com.userAuthentication.request.ValidateOtpRequest;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.response.EncryptedResponse;
import com.userAuthentication.service.CommunicationService;
import com.userAuthentication.utility.ResponseUtility;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private ResponseUtility responseUtility;

    @PostMapping(EndPointReferrer.SEND_EMAIL_OTP)
    private ResponseEntity<EncryptedResponse> sendEmailOtp(@RequestBody @NotNull EncryptedPayload encryptedPayload, HttpServletRequest request) throws Exception {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.SEND_EMAIL_OTP);

//        return new ResponseEntity<>(communicationService.sendEmailOtp(emailOtpRequest), HttpStatus.OK);

        BaseResponse baseResponse = communicationService.sendEmailOtp(encryptedPayload, request);
        return responseUtility.encryptedResponse(request, baseResponse);

    }

    @PostMapping(EndPointReferrer.VALIDATE_EMAIL_OTP)
    private ResponseEntity<EncryptedResponse> validateEmailOtp(@RequestBody @NotNull EncryptedPayload encryptedPayload, HttpServletRequest request) throws Exception {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.VALIDATE_EMAIL_OTP);

//        return new ResponseEntity<>(communicationService.validateEmailOtp(encryptedPayload, request), HttpStatus.OK);

        BaseResponse baseResponse = communicationService.validateEmailOtp(encryptedPayload, request);
        return responseUtility.encryptedResponse(request, baseResponse);
    }

    @PostMapping(EndPointReferrer.VALIDATE_OTP_RESET_PASSWORD)
    private ResponseEntity<EncryptedResponse> validateOtpResetPassword(@RequestBody @NotNull EncryptedPayload encryptedPayload, HttpServletRequest request) throws Exception {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.VALIDATE_OTP_RESET_PASSWORD);

//        return new ResponseEntity<>(communicationService.validateOtpResetPassword(encryptedPayload, request), HttpStatus.OK);
        BaseResponse baseResponse = communicationService.validateOtpResetPassword(encryptedPayload, request);
        return responseUtility.encryptedResponse(request, baseResponse);

    }
}
