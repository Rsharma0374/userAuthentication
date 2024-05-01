package com.chat.userAuthentication.service.impl;

import com.chat.userAuthentication.configuration.EmailConfiguration;
import com.chat.userAuthentication.constant.Constants;
import com.chat.userAuthentication.constant.ProductConstants;
import com.chat.userAuthentication.dao.MongoService;
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
import com.chat.userAuthentication.service.redis.RedisService;
import com.chat.userAuthentication.service.utility.TransportUtils;
import com.chat.userAuthentication.utility.ResponseUtility;
import com.chat.userAuthentication.utility.TokenGenerator;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class HomeManagerImpl implements HomeManager {

    private static final Logger logger = LoggerFactory.getLogger(HomeManagerImpl.class);

    @Autowired
    MongoService mongoService;

    @Autowired
    AuthTokenService authTokenService;

    @Value("${connector.email.send.api}")
    private String connectorEmailSendUrl;

    @Autowired
    private RedisService redisService;

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
                if (StringUtils.isNoneBlank(emailOtpResponse.getOtp())) {
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

    /**
     * The `sendVerificationOtp` method sends a verification OTP via email and logs the response and any errors
     * encountered.
     *
     * @param emailOtpRequest The `sendVerificationOtp` method you provided seems to be responsible for sending a
     * verification OTP via email. Here's a breakdown of the key steps in the method:
     * @return The method `sendVerificationOtp` is returning an `EmailOtpResponse` object.
     */
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

    /**
     * This function generates a token, sets it in a login response object, stores it in Redis with an expiry time, and
     * performs server-side validation.
     *
     * @param loginResponse The `loginResponse` parameter is an object of type `LoginResponse` which contains information
     * related to the login response such as user details, authentication status, and possibly a token.
     * @param encryptedPassword The `settingToken` method is responsible for generating a token, setting it in the
     * `loginResponse`, storing it in Redis with an expiry time, and performing server-side validation.
     * @param username The `username` parameter in the `settingToken` method is used to generate a token, set the token in
     * the `loginResponse`, store the token in Redis along with some additional information, clear any existing key
     * associated with the username from Redis, and set a server-side validation in the `login
     */
    private void settingToken(LoginResponse loginResponse, String encryptedPassword, String username) throws Exception {

        String token = TokenGenerator.generateToken(username);

        loginResponse.setToken(token);
        //setToken in redis
        if (null == redisService) {
            redisService = new RedisService();
        }
        long expiryTime = 1800;
        Object obj = ResponseUtility.redisObject(username, token, expiryTime, null);
        //clear any existing key (One session one login)
        redisService.clearKeyFromRedis(username);
        //add the new key
        redisService.setValueInRedisWithExpiration(username, obj, expiryTime, TimeUnit.SECONDS);
        loginResponse.setServerSideValidation(ResponseUtility.encryptThisString(encryptedPassword + username));

    }

    /**
     * The function creates a new user by checking for existing user, saving user data, and handling exceptions.
     *
     * @param userCreation The `createUser` method takes a `UserCreation` object as a parameter. This object likely
     * contains information required to create a new user, such as username, password, email, etc. The method first checks
     * if the `userCreation` object is null and returns a bad request response if it
     * @return The `createUser` method returns a `BaseResponse` object. The content of the response depends on the logic
     * within the method:
     */
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

    /**
     * The `sendForgotOtp` method sends an OTP via email, logs the request and response, and handles errors appropriately.
     *
     * @param emailOtpRequest The `sendForgotOtp` method is responsible for sending a one-time password (OTP) to a user's
     * email address for the purpose of password recovery. Let me break down the key steps in the method:
     * @return The method `sendForgotOtp` is returning a `BaseResponse` object.
     */
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

    /**
     * The function `settingEmailReqResLog` sets various properties of an `EmailReqResLog` object based on input
     * parameters.
     *
     * Args:
     *   emailReqResLog (EmailReqResLog): `EmailReqResLog` object that stores email request and response logs.
     *   otp (String): A string representing the one-time password (OTP) generated for the email verification process.
     *   mailRequest (MailRequest): MailRequest is an object that contains information about an email request, such as the
     * sender, recipient, subject, and body of the email.
     *   emailOtpRequest (EmailOtpRequest): The method `settingEmailReqResLog` takes in four parameters:
     */
    private void settingEmailReqResLog(EmailReqResLog emailReqResLog, String otp, MailRequest mailRequest, EmailOtpRequest emailOtpRequest) {
        emailReqResLog.setOtp(otp);
        emailReqResLog.setEmailId(emailOtpRequest.getEmailId());
        emailReqResLog.setDateTime(new Date());
        emailReqResLog.setMailRequest(mailRequest);
        emailReqResLog.setEmailType(emailOtpRequest.getEmailType());
    }

    /**
     * The function `limitExhausted` generates a response indicating that the OTP limit has been reached for a specific
     * email ID.
     *
     * Args:
     *   emailId (String): The `limitExhausted` method is used to create a response when the OTP limit is reached for a
     * specific email ID. It logs a debug message, creates an `EmailOtpResponse` object, sets an error message indicating
     * that the OTP limit is reached, and returns a response with the
     *
     * Returns:
     *   A BaseResponse object is being returned with an EmailOtpResponse object containing an error message indicating
     * that the OTP limit has been reached.
     */
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

    /**
     * The function `checkEmailFlooding` checks if the number of email triggers for a specific email ID, product name, and
     * email type exceeds a specified limit.
     *
     * Args:
     *   emailOtpRequest (EmailOtpRequest): The `emailOtpRequest` parameter is an object that contains information about an
     * email OTP request. It typically includes the email ID, product name, and email type associated with the request.
     *   otpMaxLimit (int): The `otpMaxLimit` parameter is an integer value that represents the maximum number of times an
     * OTP (One-Time Password) can be sent to a specific email address within a certain time period. The
     * `checkEmailFlooding` method checks if the number of OTP requests sent to a particular email address
     *
     * Returns:
     *   The method is returning a boolean value based on whether the hit count for the given email, product name, and
     * email type is greater than or equal to the specified OTP max limit.
     */
    private boolean checkEmailFlooding(EmailOtpRequest emailOtpRequest, int otpMaxLimit) {

        logger.debug("Inside checking Email Flooding");
        String emailId = emailOtpRequest.getEmailId();
        String productName = emailOtpRequest.getProductName();
        String emailType = emailOtpRequest.getEmailType();

        long hitCount = mongoService.getEmailTriggerCount(emailId, productName, emailType);
        return hitCount >= otpMaxLimit;
    }

    /**
     * The function `getEmailTextByType` takes in email configuration, email ID, mail request, and text to determine and
     * set the SMS content based on the email type.
     *
     * Args:
     *   emailConfiguration (EmailConfiguration): EmailConfiguration object containing email configuration details such as
     * email type, email subject, and methods to format SMS text.
     *   emailId (String): The `emailId` parameter is a `String` representing the email address to which the email will be
     * sent.
     *   mailRequest (MailRequest): The method `getEmailTextByType` takes in several parameters:
     *   text (String): The `text` parameter in the `getEmailTextByType` method is the content that needs to be formatted
     * for the email or SMS message. It is passed to the `emailConfiguration.getFormattedSMSText(text)` method to get the
     * formatted SMS content based on the email type specified in the `
     */
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

    /**
     * This Java function validates an OTP and resets a password, handling various scenarios such as expired OTP or
     * incorrect input.
     *
     * @param validateOtpRequest The `validateOtpAndResetPassword` method takes a `ValidateOtpRequest` object as a
     * parameter. This object likely contains information required to validate an OTP (One Time Password) and reset a
     * password. The method performs the following steps:
     * @return The method `validateOtpAndResetPassword` returns a `BaseResponse` object.
     */
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

    /**
     * The `createAndSendPasswordMail` function generates a new password, updates it in the database, retrieves email
     * configuration settings, and sends a password reset email to the user if the user exists.
     *
     * Args:
     *   emailId (String): The `emailId` parameter in the `createAndSendPasswordMail` method is the email address of the
     * user for whom a password reset email needs to be sent.
     *   productName (String): productName: The name of the product for which the password reset email is being sent.
     */
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

    /**
     * The function `checkOtpExpiration` checks if an OTP (One-Time Password) has expired by comparing the current time
     * with a timestamp from an email request/response log.
     *
     * Args:
     *   emailReqResLog (EmailReqResLog): The `emailReqResLog` parameter seems to be an object of type `EmailReqResLog`
     * which contains information related to email request and response logs. The method `checkOtpExpiration` is checking
     * if the OTP (One-Time Password) associated with this log has expired by comparing the OTP
     *
     * Returns:
     *   The method `checkOtpExpiration` is returning a boolean value. It checks if the OTP (One Time Password) stored in
     * the `EmailReqResLog` object is expired by comparing the date and time stored in the object with the date and time
     * two minutes ago. If the date and time two minutes ago is before the date and time stored in the `EmailReqResLog`
     * object,
     */
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

    /**
     * The function `validateOtp` validates an OTP (One-Time Password) provided in a request and generates a response based
     * on the validation result.
     *
     * @param validateOtpRequest The `validateOtp` method you provided is used to validate an OTP (One-Time Password) based
     * on the `ValidateOtpRequest` input parameter. The method performs the following steps:
     * @return The method `validateOtp` returns a `BaseResponse` object.
     */
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

    /**
     * This Java function retrieves a token from Redis based on a given key and returns a corresponding response.
     *
     * @param key The `key` parameter in the `getTokenByKey` method is used to retrieve a token from a Redis service based
     * on the provided key. The method attempts to fetch the token from Redis using the `redisService` and constructs a
     * `BaseResponse` object accordingly. If the `redisService` is
     * @return The method `getTokenByKey` returns a `BaseResponse` object.
     */
    @Override
    public BaseResponse getTokenByKey(String key) {
        BaseResponse baseResponse = null;
        try {
            if (null != redisService) {
                Object token = redisService.getValueFromRedis(key);
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, token);

            } else  {
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.NO_CONTENT, "No Token Found");
            }

        } catch (Exception e) {
            logger.error("Exception occurred while getting key with probable cause ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }

    /**
     * The function `clearTokenByKey` clears a key from Redis and returns a response indicating the success or failure of
     * the operation.
     *
     * @param key The `clearTokenByKey` method is used to clear a token from Redis based on the provided key. The `key`
     * parameter is the identifier for the token that needs to be cleared from the Redis cache.
     * @return The method `clearTokenByKey` returns a `BaseResponse` object.
     */
    @Override
    public BaseResponse clearTokenByKey(String key) {
        BaseResponse baseResponse = null;
        try {
            if (null != redisService) {
                redisService.clearKeyFromRedis(key);
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.OK, "key clear successful.");

            } else  {
                baseResponse = ResponseUtility.getBaseResponse(HttpStatus.NO_CONTENT, "No Token Found");
            }

        } catch (Exception e) {
            logger.error("Exception occurred while getting key with probable cause ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            baseResponse = ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }

}
