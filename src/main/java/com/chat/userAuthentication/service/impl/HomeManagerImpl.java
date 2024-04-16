package com.chat.userAuthentication.service.impl;

import com.chat.userAuthentication.configuration.EmailConfiguration;
import com.chat.userAuthentication.constant.Constants;
import com.chat.userAuthentication.constant.ProductConstants;
import com.chat.userAuthentication.dao.MongoService;
import com.chat.userAuthentication.model.JwtRequest;
import com.chat.userAuthentication.model.JwtResponse;
import com.chat.userAuthentication.model.email.EmailReqResLog;
import com.chat.userAuthentication.model.email.MailRequest;
import com.chat.userAuthentication.model.email.MailResponse;
import com.chat.userAuthentication.request.EmailOtpRequest;
import com.chat.userAuthentication.request.LoginRequest;
import com.chat.userAuthentication.request.UserCreation;
import com.chat.userAuthentication.request.ValidateOtpRequest;
import com.chat.userAuthentication.response.BaseResponse;
import com.chat.userAuthentication.response.Error;
import com.chat.userAuthentication.response.email.EmailOtpResponse;
import com.chat.userAuthentication.response.email.ValidateOtpResponse;
import com.chat.userAuthentication.response.login.LoginResponse;
import com.chat.userAuthentication.service.AuthTokenService;
import com.chat.userAuthentication.service.HomeManager;
import com.chat.userAuthentication.service.utility.TransportUtils;
import com.chat.userAuthentication.utility.ResponseUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

@Service
public class HomeManagerImpl implements HomeManager {

    private static final Logger logger = LoggerFactory.getLogger(HomeManagerImpl.class);

    @Autowired
    MongoService mongoService;

    @Autowired
    AuthTokenService authTokenService;

    @Value("${connector.email.send.api}")
    private String connectorEmailSendUrl;

    @Override
    public BaseResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest) throws Exception {
        logger.info("Inside login request");
        LoginResponse loginResponse = new LoginResponse();
        try {
            //Check password is correct or not with SHA encryption
            UserCreation userCreation = mongoService.getUserFromUserName(loginRequest.getUserName());

            if (userCreation == null) {
                loginResponse.setResponse("No User found against provided username");
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, loginResponse);
            }

            String encryptedPassword = ResponseUtility.encryptThisString(userCreation.getPassword());

            if (StringUtils.equalsIgnoreCase(encryptedPassword, loginRequest.getShaPassword())) {
                loginResponse.setResponse("Access Granted");
                settingToken(loginResponse, encryptedPassword, loginRequest.getUserName());
                EmailOtpRequest emailOtpRequest = new EmailOtpRequest();
                emailOtpRequest.setEmailType("EMAIL_OTP_SMS");
                emailOtpRequest.setOtpRequired(true);
                emailOtpRequest.setProductName(ProductConstants.PASSWORD_MANAGER);
                emailOtpRequest.setEmailId(userCreation.getEmailId());
                EmailOtpResponse emailOtpResponse = sendVerificationOtp(emailOtpRequest);
                if (null != emailOtpResponse && StringUtils.isNoneBlank(emailOtpResponse.getOtp())) {
                    loginResponse.setOtpToken(emailOtpResponse.getOtp());
                }

                return ResponseUtility.getBaseResponse(HttpStatus.OK, loginResponse);
            } else {
                loginResponse.setResponse("Invalid Credentials");
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, loginResponse);
            }

        } catch (Exception ex) {
            Error error = new Error();
            error.setMessage(ex.getMessage());
            logger.error("Exception occurred while login due to - ", ex);
            return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
    }

    private EmailOtpResponse sendVerificationOtp(EmailOtpRequest emailOtpRequest) throws Exception {
        logger.info("Inside sendVerificationOtp method...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        BaseResponse baseResponse = null;
        MailRequest mailRequest = new MailRequest();
        MailResponse mailResponse = new MailResponse();
        EmailReqResLog emailReqResLog = new EmailReqResLog();
        EmailOtpResponse emailOtpResponse = new EmailOtpResponse();
        try {
            EmailConfiguration emailConfiguration = mongoService.getEmailConfigByProductAndType(emailOtpRequest.getEmailType(), emailOtpRequest.getProductName(), emailOtpRequest.isOtpRequired());


            //Check email flooding
            if (!checkEmailFlooding(emailOtpRequest,emailConfiguration.getOtpMaxLimit())) {


                String otp = ResponseUtility.generateOtpAgainstLength(6);

                getEmailTextByType(emailConfiguration, emailOtpRequest.getEmailId(), mailRequest, otp);
                settingEmailReqResLog(emailReqResLog, otp, mailRequest, emailOtpRequest);


                mailResponse = (MailResponse) TransportUtils.postJsonRequest(mailRequest, connectorEmailSendUrl, MailResponse.class);

                logger.info("Mail Response : {}", mailResponse);
                if (mailResponse != null) {
                    emailReqResLog.setMailResponse(mailResponse);
                    if (mailResponse.getStatus().equalsIgnoreCase(Constants.SUCCESS)) {
                        emailOtpResponse.setSuccess(true);
                    }
                }
            } else {
                logger.error("Limit exhause for email id {}", emailOtpRequest.getEmailId());
            }

        } catch (Exception ex) {
            Error error = new Error();
            error.setMessage(ex.getMessage());
            logger.error("Exception occurred while sending otp due to - ", ex);
            emailOtpResponse.setErrors(new Error[] {error});
        } finally {
            stopWatch.stop();
            emailReqResLog.setApiTimeTaken(stopWatch.getLastTaskTimeMillis());
            mongoService.saveEmailResResLog(emailReqResLog);
            emailOtpResponse.setOtp(emailReqResLog.getId());
            return emailOtpResponse;
        }
    }

    private void settingToken(LoginResponse loginResponse, String encryptedPassword, String username) throws Exception {
        JwtRequest request = new JwtRequest();
        request.setEmail("RAHUL");
        request.setPassword("RAHUL");
        JwtResponse response = authTokenService.getToken(request);
        if (response != null) {
            loginResponse.setToken(response.getJwtToken());
            loginResponse.setServerSideValidation(ResponseUtility.encryptThisString(encryptedPassword+username));
        } else {
            throw new Exception("Token Not found");
        }
    }

    public BaseResponse createUser(UserCreation userCreation) {
        logger.info("Inside Create user method");

        try {
            if (userCreation == null) {
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, "User Creation Object cannot be null");
            }

            if (mongoService.checkExistence(userCreation)) {
                return ResponseUtility.getBaseResponse(HttpStatus.CONFLICT, "User already exists and active with username: " + userCreation.getUserName());
            }

            boolean success = mongoService.saveData(userCreation);

            if (success) {
                return ResponseUtility.getBaseResponse(HttpStatus.OK, "User Created Successfully");
            } else {
                return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, "User creation failed..");
            }
        } catch (Exception ex) {
            Error error = new Error();
            error.setMessage(ex.getMessage());
            logger.error("Exception occurred while user creation due to - ", ex);
            return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }

    }

    @Override
    public BaseResponse sendForgotOtp(EmailOtpRequest emailOtpRequest) throws Exception {
        logger.info("Inside sendOtp method...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        BaseResponse baseResponse = null;
        MailRequest mailRequest = new MailRequest();
        MailResponse mailResponse = new MailResponse();
        EmailReqResLog emailReqResLog = new EmailReqResLog();
        EmailOtpResponse emailOtpResponse = new EmailOtpResponse();

        try {
            EmailConfiguration emailConfiguration = mongoService.getEmailConfigByProductAndType(emailOtpRequest.getEmailType(), emailOtpRequest.getProductName(), emailOtpRequest.isOtpRequired());

            if (emailConfiguration == null) {
                return ResponseUtility.getBaseResponse(HttpStatus.NO_CONTENT, Constants.CONF_NOT_FOUND);
            }
            if (!mongoService.checkExistenceWithEmail(emailOtpRequest.getEmailId())) {
                return ResponseUtility.getBaseResponse(HttpStatus.CONFLICT, "No User Found with email: " + emailOtpRequest.getEmailId());
            }

            //Check email flooding
            if (checkEmailFlooding(emailOtpRequest,emailConfiguration.getOtpMaxLimit())) {
                return limitExhausted(emailOtpRequest.getEmailId());
            }
            String otp = ResponseUtility.generateOtpAgainstLength(6);

            getEmailTextByType(emailConfiguration, emailOtpRequest.getEmailId(), mailRequest, otp);
            settingEmailReqResLog(emailReqResLog, otp, mailRequest, emailOtpRequest);


            mailResponse = (MailResponse) TransportUtils.postJsonRequest(mailRequest, connectorEmailSendUrl, MailResponse.class);

            logger.info("Mail Response : {}", mailResponse);
            if (mailResponse != null) {
                emailReqResLog.setMailResponse(mailResponse);
                if (mailResponse.getStatus().equalsIgnoreCase(Constants.SUCCESS)) {
                    emailOtpResponse.setSuccess(true);
                }
            }

        } catch (Exception ex) {
            Error error = new Error();
            error.setMessage(ex.getMessage());
            logger.error("Exception occurred while sending otp due to - ", ex);
            emailOtpResponse.setErrors(new Error[] {error});
        } finally {
            stopWatch.stop();
            emailReqResLog.setApiTimeTaken(stopWatch.getLastTaskTimeMillis());
            mongoService.saveEmailResResLog(emailReqResLog);
            emailOtpResponse.setOtp(emailReqResLog.getId());
        }
        return ResponseUtility.getBaseResponse(HttpStatus.OK, emailOtpResponse);
    }

    private void settingEmailReqResLog(EmailReqResLog emailReqResLog, String otp, MailRequest mailRequest, EmailOtpRequest emailOtpRequest) {
        emailReqResLog.setOtp(otp);
        emailReqResLog.setEmailId(emailOtpRequest.getEmailId());
        emailReqResLog.setDateTime(new Date());
        emailReqResLog.setMailRequest(mailRequest);
        emailReqResLog.setEmailType(emailOtpRequest.getEmailType());
    }

    private BaseResponse limitExhausted(String emailId) {

        logger.debug("SMS LIMIT_EXHAUSTED called for emailId - {}", emailId);
        EmailOtpResponse emailOtpResponse = new EmailOtpResponse();

        Error error = new Error();
        error.setMessage(Constants.OTP_LIMIT_REACHED);
        error.setErrorCode("LIMIT_EXHAUSTED");

        emailOtpResponse.setSuccess(false);

        emailOtpResponse.setErrors(new Error[]{error});

        return ResponseUtility.getBaseResponse(HttpStatus.OK, emailOtpResponse);
    }

    private boolean checkEmailFlooding(EmailOtpRequest emailOtpRequest, int otpMaxLimit) {

        logger.debug("Inside checking Email Flooding");
        String emailId = emailOtpRequest.getEmailId();
        String productName = emailOtpRequest.getProductName();
        String emailType = emailOtpRequest.getEmailType();

        long hitCount = mongoService.getEmailTriggerCount(emailId, productName, emailType);
        return hitCount >= otpMaxLimit;
    }

    private void getEmailTextByType(EmailConfiguration emailConfiguration, String emailId, MailRequest mailRequest, String text) {

        String smsContent = null;
        String emailType = emailConfiguration.getEmailType();
        switch (emailType) {
            case "EMAIL_OTP_SMS": {
                smsContent = emailConfiguration.getFormattedSMSText(text);
            }
            break;

            case Constants.RESET_PASSWORD: {
                smsContent = emailConfiguration.getFormattedSMSText(text);
            }
            default: {
                // by default email content will be null
                logger.error("no case match to form sms content for type {}", emailType);
            }
        }
        mailRequest.setTo(emailId);
        mailRequest.setSubject(emailConfiguration.getEmailSubject());
        mailRequest.setMessage(smsContent);
    }

    @Override
    public BaseResponse validateOtpAndResetPassword(ValidateOtpRequest validateOtpRequest) {
        BaseResponse baseResponse = null;
        ValidateOtpResponse validateOtpResponse = new ValidateOtpResponse();
        try {
            EmailReqResLog emailReqResLog = mongoService.getEmailReqResLog(validateOtpRequest);
            if (null != emailReqResLog) {
                if (checkOtpExpiration(emailReqResLog)) {
                    if (emailReqResLog.getOtp().equalsIgnoreCase(validateOtpRequest.getOtp())) {
                        validateOtpResponse.setSuccess(true);
                        validateOtpResponse.setServerSideValidation(ResponseUtility.encryptThisString(emailReqResLog.getOtp() + validateOtpRequest.getOtpId()));
                        validateOtpResponse.setMessage("Otp Validated Successfully");
                        createAndSendPasswordMail(emailReqResLog.getEmailId(), validateOtpRequest.getProductName());
                    } else {
                        validateOtpResponse.setSuccess(false);
                        validateOtpResponse.setMessage("Incorrect Otp");
                    }
                } else {
                    validateOtpResponse.setSuccess(false);
                    validateOtpResponse.setMessage("Otp Expired.");
                }
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, validateOtpResponse);
            } else {
                Error error = new Error();
                error.setMessage(Constants.SOMETHING_WENT_WRONG);
                error.setErrorType("TECHNICAL ERROR");
                error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
                validateOtpResponse.setErrors(new Error[]{error});
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, validateOtpResponse);
            }

        } catch (Exception ex) {
            logger.error("Exception occurred while validation Otp with probable cause - ", ex);

            Error error = new Error();
            error.setMessage(ex.getMessage());
            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }

    private void createAndSendPasswordMail(String emailId, String productName) throws Exception {
        UserCreation userCreation = mongoService.getUserWithEmail(emailId);
        MailRequest mailRequest = new MailRequest();
        MailResponse mailResponse = new MailResponse();

        if (null != userCreation) {
            String password = ResponseUtility.generateStringAgainstLength(10);
            mongoService.updatePassword(emailId, password);

            EmailConfiguration emailConfiguration = mongoService.getEmailConfigByProductAndType(Constants.RESET_PASSWORD, productName, false);
            if (null != emailConfiguration) {
                getEmailTextByType(emailConfiguration, emailId, mailRequest, password);
                //To-do set EmailReqResLog
                mailResponse = (MailResponse) TransportUtils.postJsonRequest(mailRequest, connectorEmailSendUrl, MailResponse.class);
            }
        }
    }

    private boolean checkOtpExpiration(EmailReqResLog emailReqResLog) {
        logger.info("Checking if Otp is expired..");
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Subtract 2 minutes
        calendar.add(Calendar.MINUTE, -2);

        // Get the updated Date
        Date twoMinutesAgo = calendar.getTime();
        logger.info(String.valueOf(twoMinutesAgo));
        logger.info(emailReqResLog.getDateTime().toString());

        return twoMinutesAgo.before(emailReqResLog.getDateTime());

    }

    @Override
    public BaseResponse validateOtp(ValidateOtpRequest validateOtpRequest) {
        BaseResponse baseResponse = null;
        ValidateOtpResponse validateOtpResponse = new ValidateOtpResponse();
        try {
            EmailReqResLog emailReqResLog = mongoService.getEmailReqResLog(validateOtpRequest);
            if (null != emailReqResLog) {
                if (checkOtpExpiration(emailReqResLog)) {
                    if (emailReqResLog.getOtp().equalsIgnoreCase(validateOtpRequest.getOtp())) {
                        validateOtpResponse.setSuccess(true);
                        validateOtpResponse.setServerSideValidation(ResponseUtility.encryptThisString(emailReqResLog.getOtp() + validateOtpRequest.getOtpId()));
                        validateOtpResponse.setMessage("Otp Validated Successfully");
                    } else {
                        validateOtpResponse.setSuccess(false);
                        validateOtpResponse.setMessage("Incorrect Otp");
                    }
                } else {
                    validateOtpResponse.setSuccess(false);
                    validateOtpResponse.setMessage("Otp Expired.");
                }
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, validateOtpResponse);
            } else {
                Error error = new Error();
                error.setMessage(Constants.SOMETHING_WENT_WRONG);
                error.setErrorType("TECHNICAL ERROR");
                error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
                validateOtpResponse.setErrors(new Error[]{error});
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, validateOtpResponse);
            }

        } catch (Exception ex) {
            logger.error("Exception occurred while validation Otp with probable cause - ", ex);

            Error error = new Error();
            error.setMessage(ex.getMessage());
            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }
}
