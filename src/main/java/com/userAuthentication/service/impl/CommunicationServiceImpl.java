package com.userAuthentication.service.impl;

import com.userAuthentication.configuration.EmailConfiguration;
import com.userAuthentication.constant.Constants;
import com.userAuthentication.constant.ErrorCodes;
import com.userAuthentication.dao.MongoService;
import com.userAuthentication.model.email.EmailReqResLog;
import com.userAuthentication.model.email.MailRequest;
import com.userAuthentication.model.email.MailResponse;
import com.userAuthentication.request.EmailOtpRequest;
import com.userAuthentication.request.ValidateOtpRequest;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.response.Error;
import com.userAuthentication.response.email.EmailOtpResponse;
import com.userAuthentication.response.login.LoginResponse;
import com.userAuthentication.service.CommunicationService;
import com.userAuthentication.service.redis.RedisService;
import com.userAuthentication.service.utility.TransportUtils;
import com.userAuthentication.utility.ResponseUtility;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.*;

@Service
public class CommunicationServiceImpl implements CommunicationService {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationServiceImpl.class);

    @Autowired
    private MongoService mongoService;

    @Value("${connector.email.send.api}")
    private String connectorEmailSendUrl;

    @Autowired
    private RedisService redisService;

    @Override
    public BaseResponse sendEmailOtp(EmailOtpRequest emailOtpRequest) {
        logger.info("Inside send email otp method for emailType {}", emailOtpRequest.getEmailType());
        BaseResponse baseResponse = null;
        MailRequest mailRequest = new MailRequest();
        MailResponse mailResponse = new MailResponse();
        EmailReqResLog emailReqResLog = new EmailReqResLog();
        EmailOtpResponse emailOtpResponse = new EmailOtpResponse();
        Collection<Error> errors = new ArrayList<>();
        try {
            EmailConfiguration emailConfiguration = mongoService.getEmailConfigByProductAndType(emailOtpRequest.getEmailType(), emailOtpRequest.getProductName().getName(), emailOtpRequest.isOtpRequired());
            logger.debug("Email config is {}", emailConfiguration);

            if (null == emailConfiguration) {
                return ResponseUtility.getBaseResponse(HttpStatus.NO_CONTENT, ResponseUtility.mandatoryConfigurationError());
            }

            //Check email flooding
            if (checkEmailFlooding(emailOtpRequest, emailConfiguration.getOtpMaxLimit())) {
                return limitExhausted(emailOtpRequest.getEmailId());
            }
            String otp = ResponseUtility.generateOtpAgainstLength(6);

            getEmailTextByType(emailConfiguration, emailOtpRequest.getEmailId(), mailRequest, otp);
            settingEmailReqResLog(emailReqResLog, otp, mailRequest, emailOtpRequest);


            mailResponse = (MailResponse) TransportUtils.postJsonRequest(mailRequest, connectorEmailSendUrl, MailResponse.class);

            Error error = new Error();
            logger.info("Mail Response : {}", mailResponse);
            if (mailResponse != null) {
                emailReqResLog.setMailResponseStatus(mailResponse.getStatus());
                if (mailResponse.getStatus().equalsIgnoreCase(Constants.SUCCESS)) {
                    emailOtpResponse.setSuccess(true);
                    emailOtpResponse.setOtp(emailReqResLog.getId());
                    emailReqResLog.setUserToken(String.valueOf(UUID.randomUUID()));
                    baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, emailOtpResponse);
                } else {
                    logger.error("Mail Response is not null");
                    error.setMessage("Sms sending failed, try again");
                    error.setErrorCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
                    errors.add(error);
                    baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors);
                }
            } else {
                logger.error("Mail Response is null");
                error.setMessage("Sms sending failed, try again");
                error.setErrorCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
                errors.add(error);
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors);
            }

        } catch (Exception e) {
            logger.error("Exception occurred while sending otp with probable cause - ", e);
            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(e));
        } finally {
            mongoService.saveEmailResResLog(emailReqResLog);
            emailOtpResponse.setOtp(emailReqResLog.getId());
        }
        return baseResponse;
    }

    private boolean checkEmailFlooding(EmailOtpRequest emailOtpRequest, int otpMaxLimit) {

        logger.debug("Inside checking Email Flooding");
        String emailId = emailOtpRequest.getEmailId();
        String productName = emailOtpRequest.getProductName().getName();
        String emailType = emailOtpRequest.getEmailType();

        long hitCount = mongoService.getEmailTriggerCount(emailId, productName, emailType);
        return hitCount >= otpMaxLimit;
    }

    private BaseResponse limitExhausted(String emailId) {

        logger.debug("SMS LIMIT_EXHAUSTED called for emailId - {}", emailId);

        Collection<Error> errors = new ArrayList<>();
        Error error = new Error();
        error.setMessage(ErrorCodes.OTP_LIMIT_REACHED);
        error.setErrorCode(ErrorCodes.LIMIT_EXHAUSTED);
        errors.add(error);

        return ResponseUtility.getBaseResponse(HttpStatus.TOO_MANY_REQUESTS, errors);
    }

    private void getEmailTextByType(EmailConfiguration emailConfiguration, String emailId, MailRequest mailRequest, String otp) {

        String smsContent = null;
        String emailType = emailConfiguration.getEmailType();
        switch (emailType) {
            case "EMAIL_VERIFICATION", Constants.RESET_PASSWORD, "EMAIL_OTP_SMS", "2FA_OTP" -> {
                smsContent = emailConfiguration.getFormattedSMSText(otp);
            }
            default -> {
                // by default email content will be null
                logger.error("no case match to form sms content for type {}", emailType);
            }
        }
        mailRequest.setTo(emailId);
        mailRequest.setSubject(emailConfiguration.getEmailSubject());
        mailRequest.setMessage(smsContent);
    }

    private void settingEmailReqResLog(EmailReqResLog emailReqResLog, String otp, MailRequest mailRequest, EmailOtpRequest emailOtpRequest) {
        emailReqResLog.setOtp(otp);
        emailReqResLog.setEmailId(emailOtpRequest.getEmailId());
        emailReqResLog.setDateTime(new Date());
        emailReqResLog.setMailMessage(mailRequest.getMessage());
        emailReqResLog.setMailSubject(mailRequest.getSubject());
        emailReqResLog.setMailTo(mailRequest.getTo());
        emailReqResLog.setEmailType(emailOtpRequest.getEmailType());
    }

    @Override
    public BaseResponse validateEmailOtp(@NotNull ValidateOtpRequest validateOtpRequest) {
        logger.info("Inside validateEmailOtp");
        int attemptCount = 0;
        boolean isAttemptValid = false;
        LoginResponse loginResponse = new LoginResponse();
        EmailReqResLog emailReqResLog = mongoService.getEmailReqResLogByUserToken(validateOtpRequest.getOtpId());
        try {
            if (null != emailReqResLog && StringUtils.equalsIgnoreCase(emailReqResLog.getOtp(), validateOtpRequest.getOtp())) {
                isAttemptValid = true;
                attemptCount = emailReqResLog.getTotalAttempt() + 1;

                if (attemptCount <= 3) {

                    long actualTime = emailReqResLog.getDateTime().getTime();
                    long currentTime = System.currentTimeMillis();
                    long difference = Math.abs(currentTime - actualTime);

                    if (difference < 2 * 60 * 1000) {
                        loginResponse.setStatus("SUCCESS");
                        loginResponse.setResponse("One time password has been verified successfully.");
                        loginResponse.setEncryptedValue(ResponseUtility.encryptThisString(validateOtpRequest.getOtp() + validateOtpRequest.getOtpId()));
                    } else {
                        loginResponse.setResponse("One time password has been expired. Please request new one time password.");
                        loginResponse.setStatus("FAILED");
                    }

                } else {
                    loginResponse.setResponse("Maximum OTP limit reached, please request new OTP");
                    loginResponse.setStatus("FAILED");
                }

            } else {
                Collection<Error> errors = new ArrayList<>();
                errors.add(Error.builder()
                        .message("OTP verification failed")
                        .errorCode(String.valueOf(Error.ERROR_TYPE.BUSINESS.toCode()))
                        .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                        .level(Error.SEVERITY.LOW.name())
                        .build());
                return ResponseUtility.getBaseResponse(HttpStatus.FAILED_DEPENDENCY, errors);
            }

        } catch (Exception e) {
            logger.error("Exception occurred while validating 2FA otp with probable cause - ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        } finally {
            if (isAttemptValid) {
                emailReqResLog.setTotalAttempt(attemptCount);
                mongoService.saveEmailOtpReqRes(emailReqResLog);
            }
        }
        return ResponseUtility.getBaseResponse(HttpStatus.OK, loginResponse);
    }
}
