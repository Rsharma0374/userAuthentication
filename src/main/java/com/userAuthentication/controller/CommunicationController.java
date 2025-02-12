package com.userAuthentication.controller;

import com.userAuthentication.constant.Constants;
import com.userAuthentication.model.email.MailRequest;
import com.userAuthentication.request.EmailOtpRequest;
import com.userAuthentication.request.EncryptedPayload;
import com.userAuthentication.request.ValidateOtpRequest;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.response.EncryptedResponse;
import com.userAuthentication.service.CommunicationService;
import com.userAuthentication.service.utility.TransportUtils;
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

    @Autowired
    private TransportUtils transportUtils;

    @PostMapping(EndPointReferrer.SEND_EMAIL_OTP)
    private ResponseEntity<BaseResponse> sendEmailOtp(@RequestBody @NotNull EmailOtpRequest emailOtpRequest, HttpServletRequest request) throws Exception {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.SEND_EMAIL_OTP);


        BaseResponse baseResponse = communicationService.sendEmailOtp(emailOtpRequest, request);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);

    }

    @PostMapping(EndPointReferrer.VALIDATE_EMAIL_OTP)
    private ResponseEntity<BaseResponse> validateEmailOtp(@RequestBody @NotNull ValidateOtpRequest validateOtpRequest, HttpServletRequest request) throws Exception {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.VALIDATE_EMAIL_OTP);


        BaseResponse baseResponse = communicationService.validateEmailOtp(validateOtpRequest, request);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping(EndPointReferrer.VALIDATE_OTP_RESET_PASSWORD)
    private ResponseEntity<BaseResponse> validateOtpResetPassword(@RequestBody @NotNull ValidateOtpRequest validateOtpRequest, HttpServletRequest request) throws Exception {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.VALIDATE_OTP_RESET_PASSWORD);

        BaseResponse baseResponse = communicationService.validateOtpResetPassword(validateOtpRequest, request);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);

    }

    @PostMapping("send-email")
    private ResponseEntity<BaseResponse> sendEmail(@RequestBody @NotNull MailRequest mailRequest) throws Exception {

        logger.debug("Inside sendEmail");

        return new ResponseEntity<>(transportUtils.sendEmail(mailRequest), HttpStatus.OK);


    }
}
