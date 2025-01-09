package com.userAuthentication.service.impl;

import com.userAuthentication.configuration.EmailConfiguration;
import com.userAuthentication.constant.*;
import com.userAuthentication.dao.MongoService;
import com.userAuthentication.model.GenericResponse;
import com.userAuthentication.model.email.EmailReqResLog;
import com.userAuthentication.model.email.MailRequest;
import com.userAuthentication.model.email.MailResponse;
import com.userAuthentication.model.user.UserRegistry;
import com.userAuthentication.request.*;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.response.Error;
import com.userAuthentication.response.email.EmailOtpResponse;
import com.userAuthentication.response.login.LoginResponse;
import com.userAuthentication.security.EncryptDecryptService;
import com.userAuthentication.service.HomeManager;
import com.userAuthentication.service.JWTService;
import com.userAuthentication.service.redis.RedisService;
import com.userAuthentication.service.utility.TransportUtils;
import com.userAuthentication.utility.ResponseUtility;
import com.userAuthentication.utility.TokenGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class HomeManagerImpl implements HomeManager {

    private static final Logger logger = LoggerFactory.getLogger(HomeManagerImpl.class);

    @Autowired
    private MongoService mongoService;

    @Value("${connector.email.send.api}")
    private String connectorEmailSendUrl;

    @Autowired
    private RedisService redisService;

    @Autowired
    private JWTService jwtService;

    /**
     * The `login` function in Java handles user authentication by checking the password with SHA encryption and generating
     * an OTP for verification.
     *
     * @param loginRequest The `loginRequest` parameter in the `login` method contains information required for user
     * authentication, such as the username, password, and SHA-encrypted password. It is used to validate the user's
     * credentials during the login process. The method checks if the provided username exists in the system and then
     * compares
     * @param httpRequest The `httpRequest` parameter in the `login` method is of type `HttpServletRequest`. This parameter
     * is used to access information about the HTTP request that triggered the login operation. It can provide details such
     * as request headers, parameters, and other information related to the incoming HTTP request. This information can be
     * @return The method `login` returns a `BaseResponse` object.
     */
    @Override
    public BaseResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest) throws Exception {
        logger.info("Inside login request");
        LoginResponse loginResponse = new LoginResponse();
        try {
            //Check password is correct or not with SHA encryption
            UserRegistry userRegistry = mongoService.getUserByUsername(loginRequest.getUserName());
            logger.info("UserRegistry is {}", userRegistry);

            if (userRegistry == null) {
                loginResponse.setResponse("No User found against provided username");
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, loginResponse);
            }

            String decryptedPassword = EncryptDecryptService.decryptedTextOrReturnSame(userRegistry.getPassword());

            String encryptedPassword = ResponseUtility.encryptThisString(decryptedPassword);

            if (StringUtils.equalsIgnoreCase(encryptedPassword, loginRequest.getShaPassword())) {
                //send Otp to registered email for 2FA
                send2FAOtp(loginResponse, userRegistry);
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

    private void send2FAOtp(LoginResponse loginResponse, UserRegistry userRegistry) {
        logger.debug("Inside send 2FA otp.");
        try {
            EmailOtpRequest emailOtpRequest = new EmailOtpRequest();
            emailOtpRequest.setEmailId(userRegistry.getEmailId());
            emailOtpRequest.setOtpRequired(true);
//            emailOtpRequest.setProductName(userRegistry.getPro);
            emailOtpRequest.setEmailType("2FA_OTP");
            BaseResponse baseResponse = sendEmailOtp(emailOtpRequest);
            logger.warn("BaseResponse received is {}", baseResponse);
            if (null != baseResponse && null != baseResponse.getPayload() && null != baseResponse.getPayload().getT()) {
                EmailOtpResponse emailOtpResponse = (EmailOtpResponse) baseResponse.getPayload().getT();
                if (emailOtpResponse.isSuccess()) {
                    loginResponse.setOtpToken(emailOtpResponse.getOtp());
                    loginResponse.setResponse("Access Granted");
                } else {
                    loginResponse.setResponse("Sending OTP failed. Please contact system administrator.");
                }
            } else {
                loginResponse.setResponse("Sending OTP failed. Please contact system administrator.");
            }

        } catch (Exception e) {
            logger.error("Exception occurred while sending 2FA otp with probable cause - ", e);
        }
    }

//    /**
//     * The `sendVerificationOtp` method sends a verification OTP via email and logs the response and any errors
//     * encountered.
//     *
//     * @param emailOtpRequest The `sendVerificationOtp` method you provided seems to be responsible for sending a
//     * verification OTP via email. Here's a breakdown of the key steps in the method:
//     * @return The method `sendVerificationOtp` is returning an `EmailOtpResponse` object.
//     */
//    private EmailOtpResponse sendVerificationOtp(EmailOtpRequest emailOtpRequest) throws Exception {
//        logger.info("Inside sendVerificationOtp method...");
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//
//        BaseResponse baseResponse = null;
//        MailRequest mailRequest = new MailRequest();
//        MailResponse mailResponse = new MailResponse();
//        EmailReqResLog emailReqResLog = new EmailReqResLog();
//        EmailOtpResponse emailOtpResponse = new EmailOtpResponse();
//        try {
//            EmailConfiguration emailConfiguration = mongoService.getEmailConfigByProductAndType(emailOtpRequest.getEmailType(), emailOtpRequest.getProductName(), emailOtpRequest.isOtpRequired());
//
//
//            //Check email flooding
//            if (!checkEmailFlooding(emailOtpRequest,emailConfiguration.getOtpMaxLimit())) {
//
//
//                String otp = ResponseUtility.generateOtpAgainstLength(6);
//
//                getEmailTextByType(emailConfiguration, emailOtpRequest.getEmailId(), mailRequest, otp);
//                settingEmailReqResLog(emailReqResLog, otp, mailRequest, emailOtpRequest);
//
//
//                mailResponse = (MailResponse) TransportUtils.postJsonRequest(mailRequest, connectorEmailSendUrl, MailResponse.class);
//
//                logger.info("Mail Response : {}", mailResponse);
//                if (mailResponse != null) {
//                    emailReqResLog.setMailResponse(mailResponse);
//                    if (mailResponse.getStatus().equalsIgnoreCase(Constants.SUCCESS)) {
//                        emailOtpResponse.setSuccess(true);
//                    }
//                }
//            } else {
//                logger.error("Limit exhause for email id {}", emailOtpRequest.getEmailId());
//            }
//
//        } catch (Exception ex) {
//            Error error = new Error();
//            error.setMessage(ex.getMessage());
//            logger.error("Exception occurred while sending otp due to - ", ex);
//            emailOtpResponse.setErrors(new Error[] {error});
//        } finally {
//            stopWatch.stop();
//            emailReqResLog.setApiTimeTaken(stopWatch.getLastTaskTimeMillis());
//            mongoService.saveEmailResResLog(emailReqResLog);
//            emailOtpResponse.setOtp(emailReqResLog.getId());
//            return emailOtpResponse;
//        }
//    }
//

    private void settingToken(LoginResponse loginResponse, String encryptedPassword, String username) throws Exception {

        String token = TokenGenerator.generateToken(username);

        loginResponse.setToken(token);
        //setToken in redis
        if (null == redisService) {
            redisService = new RedisService();
        }
        long expiryTime = 1800;
        String redisKey = StringUtils.join(username, FieldSeparator.UNDER_SCORE_STR, ProductName.PASSWORD_MANAGER.getName());
        Object obj = ResponseUtility.redisObject(username, token, expiryTime, null);
        //clear any existing key (One session one login)
        redisService.clearKeyFromRedis(username);
        //add the new key
        redisService.setValueInRedisWithExpiration(redisKey, obj, expiryTime, TimeUnit.SECONDS);
        loginResponse.setServerSideValidation(ResponseUtility.encryptThisString(encryptedPassword + username));

    }

    public BaseResponse createUser(UserCreation userCreation) {
        logger.info("Inside Create user method");
        GenericResponse genericResponse = new GenericResponse();
        BaseResponse baseResponse = null;
        Collection<Error> errors = new ArrayList<>();

        try {
            if (userCreation == null) {
                errors.add(Error.builder()
                        .message(ErrorCodes.USER_CREATION_REQUEST_OBJECT_NULL)
                        .errorCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                        .level(Error.SEVERITY.LOW.name())
                        .build());
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
            }

            if (null != mongoService.getUserByUsernameAndProduct(userCreation.getUserName(), userCreation.getProductName().getName())) {
                errors.add(Error.builder()
                        .message(String.format(ErrorCodes.USER_ALREADY_EXIST_ERROR, userCreation.getUserName()))
                        .errorCode(String.valueOf(HttpStatus.CONFLICT.value()))
                        .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                        .level(Error.SEVERITY.LOW.name())
                        .build());
                return ResponseUtility.getBaseResponse(HttpStatus.CONFLICT, errors);
            }

            boolean success = createAndSaveUserDetails(userCreation);

            if (success) {
                genericResponse.setStatus(StatusConstant.SUCCESS.name());
                genericResponse.setResponseMessage("User Created Successfully");
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, genericResponse);
            } else {
                genericResponse.setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
                genericResponse.setResponseMessage("User creation failed.");
                errors.add(Error.builder()
                        .message(ErrorCodes.USER_CREATION_FAILED)
                        .errorCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                        .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                        .level(Error.SEVERITY.HIGH.name())
                        .build());
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, genericResponse);
            }
        } catch (Exception ex) {
            Error error = new Error();
            error.setMessage(ex.getMessage());
            logger.error("Exception occurred while user creation due to - ", ex);
            return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;

    }

    @Override
    public BaseResponse sendEmailOtp(EmailOtpRequest emailOtpRequest) {
        logger.info("Inside send email otp method for emailType {}", emailOtpRequest.getEmailType());
        BaseResponse baseResponse = null;
        MailRequest mailRequest = new MailRequest();
        MailResponse mailResponse = new MailResponse();
        EmailReqResLog emailReqResLog = new EmailReqResLog();
        EmailOtpResponse emailOtpResponse = new EmailOtpResponse();
        try {
            EmailConfiguration emailConfiguration = mongoService.getEmailConfigByProductAndType(emailOtpRequest.getEmailType(), emailOtpRequest.getProductName().getName(), emailOtpRequest.isOtpRequired());
            logger.debug("Email config is {}", emailConfiguration);

            if (null == emailConfiguration) {
                return ResponseUtility.getBaseResponse(HttpStatus.NO_CONTENT, Constants.CONF_NOT_FOUND);
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
                } else {
                    error.setMessage("Sms sending failed, try again");
                    error.setErrorCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
                    emailOtpResponse.setErrors(new Error[]{error});
                }
            } else {
                error.setMessage("Sms sending failed, try again");
                error.setErrorCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
                emailOtpResponse.setErrors(new Error[]{error});
            }
            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, emailOtpResponse);

        } catch (Exception e) {
            logger.error("Exception occurred while sending otp with probable cause - ", e);
            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(e));
        } finally {
            mongoService.saveEmailResResLog(emailReqResLog);
            emailOtpResponse.setOtp(emailReqResLog.getId());
        }
        return baseResponse;
    }

    @Override
    public BaseResponse validate2faOtp(ValidateOtpRequest validateOtpRequest) {
        logger.info("Inside validate 2Fa Otp");
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
                        loginResponse.setResponse("One time password has been verified successfully .");
                        loginResponse.setEncryptedValue(ResponseUtility.encryptThisString(validateOtpRequest.getOtp() + validateOtpRequest.getOtpId()));
                        loginResponse.setToken(jwtService.generateToken(validateOtpRequest.getUserName()));

                    } else {
                        loginResponse.setResponse("One time passord has been expired . Please request new one time password.");
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

    private boolean createAndSaveUserDetails(UserCreation userCreation) {
        boolean success = false;
        try {
            UserRegistry userRegistry = new UserRegistry(userCreation.getUserName(), userCreation.getEmail(), userCreation.getFullName(), userCreation.getGender(), userCreation.getDateOfBirth(), userCreation.getProductName(), true, new Date(), new Date());
            String encryptedPassword = EncryptDecryptService.encryptText(userCreation.getPassword());
            userRegistry.setPassword(encryptedPassword);
            success = mongoService.saveUserRegistry(userRegistry);

        } catch (Exception e) {
            logger.error("Exception occurred while encrypting password with probable cause - ", e);
        }
        return success;
    }

//    @Override
//    public BaseResponse sendForgotOtp(EmailOtpRequest emailOtpRequest) throws Exception {
//        logger.info("Inside sendOtp method...");
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//
//        BaseResponse baseResponse = null;
//        MailRequest mailRequest = new MailRequest();
//        MailResponse mailResponse = new MailResponse();
//        EmailReqResLog emailReqResLog = new EmailReqResLog();
//        EmailOtpResponse emailOtpResponse = new EmailOtpResponse();
//
//        try {
//            EmailConfiguration emailConfiguration = mongoService.getEmailConfigByProductAndType(emailOtpRequest.getEmailType(), emailOtpRequest.getProductName(), emailOtpRequest.isOtpRequired());
//
//            if (emailConfiguration == null) {
//                return ResponseUtility.getBaseResponse(HttpStatus.NO_CONTENT, Constants.CONF_NOT_FOUND);
//            }
//            if (!mongoService.checkExistenceWithEmail(emailOtpRequest.getEmailId())) {
//                return ResponseUtility.getBaseResponse(HttpStatus.CONFLICT, "No User Found with email: " + emailOtpRequest.getEmailId());
//            }
//
//            //Check email flooding
//            if (checkEmailFlooding(emailOtpRequest,emailConfiguration.getOtpMaxLimit())) {
//                return limitExhausted(emailOtpRequest.getEmailId());
//            }
//            String otp = ResponseUtility.generateOtpAgainstLength(6);
//
//            getEmailTextByType(emailConfiguration, emailOtpRequest.getEmailId(), mailRequest, otp);
//            settingEmailReqResLog(emailReqResLog, otp, mailRequest, emailOtpRequest);
//
//
//            mailResponse = (MailResponse) TransportUtils.postJsonRequest(mailRequest, connectorEmailSendUrl, MailResponse.class);
//
//            logger.info("Mail Response : {}", mailResponse);
//            if (mailResponse != null) {
//                emailReqResLog.setMailResponse(mailResponse);
//                if (mailResponse.getStatus().equalsIgnoreCase(Constants.SUCCESS)) {
//                    emailOtpResponse.setSuccess(true);
//                }
//            }
//
//        } catch (Exception ex) {
//            Error error = new Error();
//            error.setMessage(ex.getMessage());
//            logger.error("Exception occurred while sending otp due to - ", ex);
//            emailOtpResponse.setErrors(new Error[] {error});
//        } finally {
//            stopWatch.stop();
//            emailReqResLog.setApiTimeTaken(stopWatch.getLastTaskTimeMillis());
//            mongoService.saveEmailResResLog(emailReqResLog);
//            emailOtpResponse.setOtp(emailReqResLog.getId());
//        }
//        return ResponseUtility.getBaseResponse(HttpStatus.OK, emailOtpResponse);
//    }

    private void settingEmailReqResLog(EmailReqResLog emailReqResLog, String otp, MailRequest mailRequest, EmailOtpRequest emailOtpRequest) {
        emailReqResLog.setOtp(otp);
        emailReqResLog.setEmailId(emailOtpRequest.getEmailId());
        emailReqResLog.setDateTime(new Date());
        emailReqResLog.setMailMessage(mailRequest.getMessage());
        emailReqResLog.setMailSubject(mailRequest.getSubject());
        emailReqResLog.setMailTo(mailRequest.getTo());
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
        String productName = emailOtpRequest.getProductName().getName();
        String emailType = emailOtpRequest.getEmailType();

        long hitCount = mongoService.getEmailTriggerCount(emailId, productName, emailType);
        return hitCount >= otpMaxLimit;
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
//
//    /**
//     * This Java function validates an OTP and resets a password, handling various scenarios such as expired OTP or
//     * incorrect input.
//     *
//     * @param validateOtpRequest The `validateOtpAndResetPassword` method takes a `ValidateOtpRequest` object as a
//     * parameter. This object likely contains information required to validate an OTP (One Time Password) and reset a
//     * password. The method performs the following steps:
//     * @return The method `validateOtpAndResetPassword` returns a `BaseResponse` object.
//     */
//    @Override
//    public BaseResponse validateOtpAndResetPassword(ValidateOtpRequest validateOtpRequest) {
//        BaseResponse baseResponse = null;
//        ValidateOtpResponse validateOtpResponse = new ValidateOtpResponse();
//        try {
//            EmailReqResLog emailReqResLog = mongoService.getEmailReqResLog(validateOtpRequest);
//            if (null != emailReqResLog) {
//                if (checkOtpExpiration(emailReqResLog)) {
//                    if (emailReqResLog.getOtp().equalsIgnoreCase(validateOtpRequest.getOtp())) {
//                        validateOtpResponse.setSuccess(true);
//                        validateOtpResponse.setServerSideValidation(ResponseUtility.encryptThisString(emailReqResLog.getOtp() + validateOtpRequest.getOtpId()));
//                        validateOtpResponse.setMessage("Otp Validated Successfully");
//                        createAndSendPasswordMail(emailReqResLog.getEmailId(), validateOtpRequest.getProductName());
//                    } else {
//                        validateOtpResponse.setSuccess(false);
//                        validateOtpResponse.setMessage("Incorrect Otp");
//                    }
//                } else {
//                    validateOtpResponse.setSuccess(false);
//                    validateOtpResponse.setMessage("Otp Expired.");
//                }
//                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, validateOtpResponse);
//            } else {
//                Error error = new Error();
//                error.setMessage(Constants.SOMETHING_WENT_WRONG);
//                error.setErrorType("TECHNICAL ERROR");
//                error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
//                validateOtpResponse.setErrors(new Error[]{error});
//                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, validateOtpResponse);
//            }
//
//        } catch (Exception ex) {
//            logger.error("Exception occurred while validation Otp with probable cause - ", ex);
//
//            Error error = new Error();
//            error.setMessage(ex.getMessage());
//            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
//        }
//        return baseResponse;
//    }
//
//    /**
//     * The `createAndSendPasswordMail` function generates a new password, updates it in the database, retrieves email
//     * configuration settings, and sends a password reset email to the user if the user exists.
//     *
//     * Args:
//     *   emailId (String): The `emailId` parameter in the `createAndSendPasswordMail` method is the email address of the
//     * user for whom a password reset email needs to be sent.
//     *   productName (String): productName: The name of the product for which the password reset email is being sent.
//     */
//    private void createAndSendPasswordMail(String emailId, String productName) throws Exception {
//        UserCreation userCreation = mongoService.getUserWithEmail(emailId);
//        MailRequest mailRequest = new MailRequest();
//        MailResponse mailResponse = new MailResponse();
//
//        if (null != userCreation) {
//            String password = ResponseUtility.generateStringAgainstLength(10);
//            mongoService.updatePassword(emailId, password);
//
//            EmailConfiguration emailConfiguration = mongoService.getEmailConfigByProductAndType(Constants.RESET_PASSWORD, productName, false);
//            if (null != emailConfiguration) {
//                getEmailTextByType(emailConfiguration, emailId, mailRequest, password);
//                //To-do set EmailReqResLog
//                mailResponse = (MailResponse) TransportUtils.postJsonRequest(mailRequest, connectorEmailSendUrl, MailResponse.class);
//            }
//        }
//    }
//
//    /**
//     * The function `checkOtpExpiration` checks if an OTP (One-Time Password) has expired by comparing the current time
//     * with a timestamp from an email request/response log.
//     *
//     * Args:
//     *   emailReqResLog (EmailReqResLog): The `emailReqResLog` parameter seems to be an object of type `EmailReqResLog`
//     * which contains information related to email request and response logs. The method `checkOtpExpiration` is checking
//     * if the OTP (One-Time Password) associated with this log has expired by comparing the OTP
//     *
//     * Returns:
//     *   The method `checkOtpExpiration` is returning a boolean value. It checks if the OTP (One Time Password) stored in
//     * the `EmailReqResLog` object is expired by comparing the date and time stored in the object with the date and time
//     * two minutes ago. If the date and time two minutes ago is before the date and time stored in the `EmailReqResLog`
//     * object,
//     */
//    private boolean checkOtpExpiration(EmailReqResLog emailReqResLog) {
//        logger.info("Checking if Otp is expired..");
//        Date currentDate = new Date();
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(currentDate);
//
//        // Subtract 2 minutes
//        calendar.add(Calendar.MINUTE, -2);
//
//        // Get the updated Date
//        Date twoMinutesAgo = calendar.getTime();
//        logger.info(String.valueOf(twoMinutesAgo));
//        logger.info(emailReqResLog.getDateTime().toString());
//
//        return twoMinutesAgo.before(emailReqResLog.getDateTime());
//
//    }
//
//    /**
//     * The function `validateOtp` validates an OTP (One-Time Password) provided in a request and generates a response based
//     * on the validation result.
//     *
//     * @param validateOtpRequest The `validateOtp` method you provided is used to validate an OTP (One-Time Password) based
//     * on the `ValidateOtpRequest` input parameter. The method performs the following steps:
//     * @return The method `validateOtp` returns a `BaseResponse` object.
//     */
//    @Override
//    public BaseResponse validateOtp(ValidateOtpRequest validateOtpRequest) {
//        BaseResponse baseResponse = null;
//        ValidateOtpResponse validateOtpResponse = new ValidateOtpResponse();
//        try {
//            EmailReqResLog emailReqResLog = mongoService.getEmailReqResLog(validateOtpRequest);
//            if (null != emailReqResLog) {
//                if (checkOtpExpiration(emailReqResLog)) {
//                    if (emailReqResLog.getOtp().equalsIgnoreCase(validateOtpRequest.getOtp())) {
//                        validateOtpResponse.setSuccess(true);
//                        validateOtpResponse.setServerSideValidation(ResponseUtility.encryptThisString(emailReqResLog.getOtp() + validateOtpRequest.getOtpId()));
//                        validateOtpResponse.setMessage("Otp Validated Successfully");
//                    } else {
//                        validateOtpResponse.setSuccess(false);
//                        validateOtpResponse.setMessage("Incorrect Otp");
//                    }
//                } else {
//                    validateOtpResponse.setSuccess(false);
//                    validateOtpResponse.setMessage("Otp Expired.");
//                }
//                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, validateOtpResponse);
//            } else {
//                Error error = new Error();
//                error.setMessage(Constants.SOMETHING_WENT_WRONG);
//                error.setErrorType("TECHNICAL ERROR");
//                error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
//                validateOtpResponse.setErrors(new Error[]{error});
//                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, validateOtpResponse);
//            }
//
//        } catch (Exception ex) {
//            logger.error("Exception occurred while validation Otp with probable cause - ", ex);
//
//            Error error = new Error();
//            error.setMessage(ex.getMessage());
//            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
//        }
//        return baseResponse;
//    }
//
//    /**
//     * This Java function retrieves a token from Redis based on a given key and returns a corresponding response.
//     *
//     * @param key The `key` parameter in the `getTokenByKey` method is used to retrieve a token from a Redis service based
//     * on the provided key. The method attempts to fetch the token from Redis using the `redisService` and constructs a
//     * `BaseResponse` object accordingly. If the `redisService` is
//     * @return The method `getTokenByKey` returns a `BaseResponse` object.
//     */
//    @Override
//    public BaseResponse getTokenByKey(String key) {
//        BaseResponse baseResponse = null;
//        try {
//            if (null != redisService) {
//                Object token = redisService.getValueFromRedis(key);
//                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, token);
//
//            } else  {
//                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.NO_CONTENT, "No Token Found");
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception occurred while getting key with probable cause ", e);
//            Error error = new Error();
//            error.setMessage(e.getMessage());
//            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
//        }
//        return baseResponse;
//    }
//
//    /**
//     * The function `clearTokenByKey` clears a key from Redis and returns a response indicating the success or failure of
//     * the operation.
//     *
//     * @param key The `clearTokenByKey` method is used to clear a token from Redis based on the provided key. The `key`
//     * parameter is the identifier for the token that needs to be cleared from the Redis cache.
//     * @return The method `clearTokenByKey` returns a `BaseResponse` object.
//     */
//    @Override
//    public BaseResponse clearTokenByKey(String key) {
//        BaseResponse baseResponse = null;
//        try {
//            if (null != redisService) {
//                redisService.clearKeyFromRedis(key);
//                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, "key clear successful.");
//
//            } else  {
//                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.NO_CONTENT, "No Token Found");
//            }
//
//        } catch (Exception e) {
//            logger.error("Exception occurred while getting key with probable cause ", e);
//            Error error = new Error();
//            error.setMessage(e.getMessage());
//            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
//        }
//        return baseResponse;
//    }

    @Override
    public BaseResponse logout(LogoutRequest logoutRequest, HttpServletRequest httpServletRequest) {
        logger.info("Inside logout method");
        BaseResponse baseResponse = null;
        GenericResponse genericResponse = new GenericResponse();

        try {
            String authHeader = httpServletRequest.getHeader("Authorization");
            String opaqueToken = authHeader.substring(7);
            redisService.clearKeyFromRedis(opaqueToken);
            genericResponse.setResponseMessage("Logout Successful");
            genericResponse.setStatus(String.valueOf(HttpStatus.OK.value()));
            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, genericResponse);

        } catch (Exception e) {
            logger.error("Exception occurred while logout with probable cause ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }
}
