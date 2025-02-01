package com.userAuthentication.service.impl;

import com.userAuthentication.configuration.EmailConfiguration;
import com.userAuthentication.constant.Constants;
import com.userAuthentication.constant.ErrorCodes;
import com.userAuthentication.constant.FieldSeparator;
import com.userAuthentication.constant.StatusConstant;
import com.userAuthentication.dao.MongoService;
import com.userAuthentication.model.email.EmailReqResLog;
import com.userAuthentication.model.email.MailRequest;
import com.userAuthentication.model.email.MailResponse;
import com.userAuthentication.model.user.UserRegistry;
import com.userAuthentication.request.EmailOtpRequest;
import com.userAuthentication.request.EncryptedPayload;
import com.userAuthentication.request.ValidateOtpRequest;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.response.Error;
import com.userAuthentication.response.email.EmailOtpResponse;
import com.userAuthentication.response.email.ValidateOtpResponse;
import com.userAuthentication.response.login.LoginResponse;
import com.userAuthentication.security.EncryptDecryptService;
import com.userAuthentication.service.CommunicationService;
import com.userAuthentication.service.redis.RedisService;
import com.userAuthentication.service.utility.TransportUtils;
import com.userAuthentication.utility.JsonUtils;
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
            if (emailConfiguration.isLimitCheck() && checkEmailFlooding(emailOtpRequest, emailConfiguration.getOtpMaxLimit())) {
                return limitExhausted(emailOtpRequest.getEmailId());
            }
            String otp = ResponseUtility.generateOtpAgainstLength(6);

            getEmailTextByType(emailConfiguration, emailOtpRequest.getEmailId(), mailRequest, otp, emailOtpRequest.getAdditionalInfo());
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
                    emailOtpResponse.setMessage(Constants.FURTHER_INSTRUCTION_SENT_ON_EMAIL);
                    baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, emailOtpResponse);
                } else {
                    logger.error("Mail Response is not null");
                    errors.add(Error.builder()
                            .message(ErrorCodes.SMS_SENDING_FAIL_TRY_AGAIN)
                            .errorCode(String.valueOf(Error.ERROR_TYPE.SYSTEM.toCode()))
                            .errorType(Error.ERROR_TYPE.SYSTEM.toValue())
                            .level(Error.SEVERITY.HIGH.name())
                            .build());
                    baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors);
                }
            } else {
                logger.error("Mail Response is null");
                errors.add(Error.builder()
                        .message(ErrorCodes.SMS_SENDING_FAIL_TRY_AGAIN)
                        .errorCode(String.valueOf(Error.ERROR_TYPE.SYSTEM.toCode()))
                        .errorType(Error.ERROR_TYPE.SYSTEM.toValue())
                        .level(Error.SEVERITY.HIGH.name())
                        .build());
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

    private void getEmailTextByType(EmailConfiguration emailConfiguration, String emailId, MailRequest mailRequest, String otp, Map<String, String> requestData) {

        String smsContent = null;
        String emailType = emailConfiguration.getEmailType();
        String fullName = null != requestData ? requestData.get(Constants.FULL_NAME) : FieldSeparator.BLANK;
        String password = null != requestData ? requestData.get(Constants.PASSWORD) : FieldSeparator.BLANK;
        switch (emailType) {
            case "EMAIL_VERIFICATION", "EMAIL_OTP_SMS", "FORGOT_PASSWORD_OTP" -> {
                smsContent = emailConfiguration.getFormattedSMSText(otp);
            }

            case Constants.RESET_PASSWORD -> {
                smsContent = emailConfiguration.getFormattedSMSText(fullName, password);
            }

            case "2FA_OTP" -> {
                smsContent = emailConfiguration.getFormattedSMSText(fullName, otp);
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
    public BaseResponse validateEmailOtp(@NotNull EncryptedPayload encryptedPayload) {
        logger.info("Inside validateEmailOtp");
        int attemptCount = 0;
        boolean isAttemptValid = false;
        LoginResponse loginResponse = new LoginResponse();
        BaseResponse baseResponse = null;
        ValidateOtpRequest validateOtpRequest;
        Collection<Error> errors = new ArrayList<>();
        EmailReqResLog emailReqResLog = null;
        try {
            String decryptedPayload= EncryptDecryptService.decryptPayload(encryptedPayload.getEncryptedPayload());
            if (StringUtils.isNoneBlank(decryptedPayload)) {
                validateOtpRequest = JsonUtils.parseJson(decryptedPayload, ValidateOtpRequest.class);
                if (null == validateOtpRequest) {
                    logger.error(ErrorCodes.VALIDATE_OTP_BAD_REQUEST);
                    errors.add(Error.builder()
                            .message(ErrorCodes.VALIDATE_OTP_BAD_REQUEST)
                            .errorCode(String.valueOf(Error.ERROR_TYPE.BAD_REQUEST.toCode()))
                            .errorType(Error.ERROR_TYPE.BAD_REQUEST.toValue())
                            .level(Error.SEVERITY.HIGH.name())
                            .build());
                    baseResponse = ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                } else {
                    emailReqResLog = mongoService.getEmailReqResLogByOtpId(validateOtpRequest.getOtpId());
                    if (null != emailReqResLog && StringUtils.equalsIgnoreCase(emailReqResLog.getOtp(), validateOtpRequest.getOtp())) {
                        isAttemptValid = true;
                        attemptCount = emailReqResLog.getTotalAttempt() + 1;

                        if (attemptCount <= 3) {

                            long actualTime = emailReqResLog.getDateTime().getTime();
                            long currentTime = System.currentTimeMillis();
                            long difference = Math.abs(currentTime - actualTime);

                            if (difference < 2 * 60 * 1000) {
                                loginResponse.setStatus(StatusConstant.SUCCESS.name());
                                loginResponse.setResponse("One time password has been verified successfully.");
                                loginResponse.setEncryptedValue(ResponseUtility.encryptThisString(emailReqResLog.getOtp() + validateOtpRequest.getOtpId()));
                                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, loginResponse);
                            } else {
                                errors.add(Error.builder()
                                        .message(ErrorCodes.OTP_EXPIRED)
                                        .errorCode(String.valueOf(Error.ERROR_TYPE.BUSINESS.toCode()))
                                        .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                                        .level(Error.SEVERITY.LOW.name())
                                        .build());
                                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.GONE, errors);
                            }

                        } else {
                            errors.add(Error.builder()
                                    .message(ErrorCodes.OTP_VALIDATE_LIMIT_REACHED)
                                    .errorCode(String.valueOf(Error.ERROR_TYPE.BUSINESS.toCode()))
                                    .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                                    .level(Error.SEVERITY.LOW.name())
                                    .build());
                            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.TOO_MANY_REQUESTS, errors);
                        }

                    } else {
                        errors.add(Error.builder()
                                .message(ErrorCodes.OTP_VERIFICATION_FAILED)
                                .errorCode(String.valueOf(Error.ERROR_TYPE.BUSINESS.toCode()))
                                .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                                .level(Error.SEVERITY.LOW.name())
                                .build());
                        baseResponse = ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                    }
                }
            } else {
                logger.error("Error occurred while decrypting the payload");
                errors.add(Error.builder()
                        .message(ErrorCodes.SOMETHING_WENT_WRONG)
                        .errorCode(String.valueOf(Error.ERROR_TYPE.SYSTEM.toCode()))
                        .errorType(Error.ERROR_TYPE.SYSTEM.toValue())
                        .level(Error.SEVERITY.HIGH.name())
                        .build());
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors);
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
        return baseResponse;
    }

    @Override
    public BaseResponse validateOtpResetPassword(EncryptedPayload encryptedPayload) {
        BaseResponse baseResponse = null;
        int attemptCount = 0;
        boolean isAttemptValid = false;
        Collection<Error> errors = new ArrayList<>();

        ValidateOtpResponse validateOtpResponse = new ValidateOtpResponse();
        try {
            String decryptedPayload = EncryptDecryptService.decryptPayload(encryptedPayload.getEncryptedPayload());
            if (StringUtils.isNoneBlank(decryptedPayload)) {
                ValidateOtpRequest validateOtpRequest = JsonUtils.parseJson(decryptedPayload, ValidateOtpRequest.class);
                if (null == validateOtpRequest) {
                    logger.error(ErrorCodes.VALIDATE_OTP_BAD_REQUEST);
                    errors.add(Error.builder()
                            .message(ErrorCodes.VALIDATE_OTP_BAD_REQUEST)
                            .errorCode(String.valueOf(Error.ERROR_TYPE.BAD_REQUEST.toCode()))
                            .errorType(Error.ERROR_TYPE.BAD_REQUEST.toValue())
                            .level(Error.SEVERITY.HIGH.name())
                            .build());
                    baseResponse = ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                } else {

                    EmailReqResLog emailReqResLog = mongoService.getEmailReqResLogByOtpId(validateOtpRequest.getOtpId());
                    if (null != emailReqResLog && StringUtils.equalsIgnoreCase(emailReqResLog.getOtp(), validateOtpRequest.getOtp())) {
                        isAttemptValid = true;
                        attemptCount = emailReqResLog.getTotalAttempt() + 1;
                        if (attemptCount <= 3) {

                            long actualTime = emailReqResLog.getDateTime().getTime();
                            long currentTime = System.currentTimeMillis();
                            long difference = Math.abs(currentTime - actualTime);

                            if (difference < 2 * 60 * 1000) {
                                validateOtpResponse.setSuccess(StatusConstant.SUCCESS.name());
                                validateOtpResponse.setServerSideValidation(ResponseUtility.encryptThisString(emailReqResLog.getOtp() + validateOtpRequest.getOtpId()));
                                baseResponse = createAndSendPasswordMail(emailReqResLog.getEmailId(), validateOtpRequest.getProductName().getName(), validateOtpResponse);
                            } else {
                                errors.add(Error.builder()
                                        .message(ErrorCodes.OTP_EXPIRED)
                                        .errorCode(String.valueOf(Error.ERROR_TYPE.BUSINESS.toCode()))
                                        .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                                        .level(Error.SEVERITY.LOW.name())
                                        .build());
                                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.GONE, errors);
                            }

                        } else {
                            errors.add(Error.builder()
                                    .message(ErrorCodes.OTP_VALIDATE_LIMIT_REACHED)
                                    .errorCode(String.valueOf(Error.ERROR_TYPE.BUSINESS.toCode()))
                                    .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                                    .level(Error.SEVERITY.LOW.name())
                                    .build());
                            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.TOO_MANY_REQUESTS, errors);
                        }
                    } else {
                        errors.add(Error.builder()
                                .message(ErrorCodes.OTP_VERIFICATION_FAILED)
                                .errorCode(String.valueOf(Error.ERROR_TYPE.BUSINESS.toCode()))
                                .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                                .level(Error.SEVERITY.LOW.name())
                                .build());
                        baseResponse = ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                    }
                }

            } else {
                logger.error("Error occurred while decrypting the payload");
                errors.add(Error.builder()
                        .message(ErrorCodes.SOMETHING_WENT_WRONG)
                        .errorCode(String.valueOf(Error.ERROR_TYPE.SYSTEM.toCode()))
                        .errorType(Error.ERROR_TYPE.SYSTEM.toValue())
                        .level(Error.SEVERITY.HIGH.name())
                        .build());
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors);
            }

        } catch (Exception ex) {
            logger.error("Exception occurred while validation Otp with probable cause - ", ex);

            Error error = new Error();
            error.setMessage(ex.getMessage());
            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }

    private BaseResponse createAndSendPasswordMail(String emailId, String productName, ValidateOtpResponse validateOtpResponse) throws Exception {
        UserRegistry userRegistry = mongoService.getUserByUsernameorEmailAndProduct(FieldSeparator.BLANK, emailId, productName);
        MailRequest mailRequest = new MailRequest();
        MailResponse mailResponse = new MailResponse();
        BaseResponse baseResponse = null;
        EmailReqResLog emailReqResLog = new EmailReqResLog();
        Collection<Error> errors = new ArrayList<>();

        try {
            if (null != userRegistry) {
                String password = ResponseUtility.generateStringAgainstLength(10);
                String hashedPassword = EncryptDecryptService.encryptText(password);
                mongoService.updatePasswordByEmailAndProduct(emailId, hashedPassword, productName);

                EmailConfiguration emailConfiguration = mongoService.getEmailConfigByProductAndType(Constants.RESET_PASSWORD, productName, false);
                if (null == emailConfiguration) {
                    return ResponseUtility.getBaseResponse(HttpStatus.NO_CONTENT, ResponseUtility.mandatoryConfigurationError());
                }
                Map<String, String> requestData = new HashMap<>();
                requestData.put(Constants.FULL_NAME, userRegistry.getFullName());
                requestData.put(Constants.PASSWORD, password);
                getEmailTextByType(emailConfiguration, emailId, mailRequest, null, requestData);
                //To-do set EmailReqResLog
                mailResponse = (MailResponse) TransportUtils.postJsonRequest(mailRequest, connectorEmailSendUrl, MailResponse.class);

                Error error = new Error();
                logger.info("Mail Response : {}", mailResponse);
                if (mailResponse != null) {
                    emailReqResLog.setMailResponseStatus(mailResponse.getStatus());
                    if (mailResponse.getStatus().equalsIgnoreCase(Constants.SUCCESS)) {
                        validateOtpResponse.setMessage(Constants.PASSWORD_RESET_SUCCESSFULLY);
                        baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, validateOtpResponse);
                    } else {
                        logger.error("Mail Response is not null");
                        errors.add(Error.builder()
                                .message(ErrorCodes.SMS_SENDING_FAIL_TRY_AGAIN)
                                .errorCode(String.valueOf(Error.ERROR_TYPE.SYSTEM.toCode()))
                                .errorType(Error.ERROR_TYPE.SYSTEM.toValue())
                                .level(Error.SEVERITY.HIGH.name())
                                .build());
                        baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors);
                    }
                } else {
                    logger.error("Mail Response is null");
                    errors.add(Error.builder()
                            .message(ErrorCodes.SMS_SENDING_FAIL_TRY_AGAIN)
                            .errorCode(String.valueOf(Error.ERROR_TYPE.SYSTEM.toCode()))
                            .errorType(Error.ERROR_TYPE.SYSTEM.toValue())
                            .level(Error.SEVERITY.HIGH.name())
                            .build());
                    baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors);
                }
            }

        } catch (Exception e) {
            logger.error("Exception occurred while sending otp with probable cause - ", e);
            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(e));
        }

        return baseResponse;
    }
}
