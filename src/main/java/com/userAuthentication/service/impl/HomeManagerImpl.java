package com.userAuthentication.service.impl;

import com.userAuthentication.configuration.EmailConfiguration;
import com.userAuthentication.constant.*;
import com.userAuthentication.dao.MongoService;
import com.userAuthentication.model.email.EmailReqResLog;
import com.userAuthentication.model.email.MailRequest;
import com.userAuthentication.model.email.MailResponse;
import com.userAuthentication.model.user.UserRegistry;
//import com.userAuthentication.repository.EmailConfigRepository;
import com.userAuthentication.request.*;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.response.Error;
import com.userAuthentication.response.GenericResponse;
import com.userAuthentication.response.email.EmailOtpResponse;
import com.userAuthentication.response.login.LoginResponse;
import com.userAuthentication.security.EncryptDecryptService;
import com.userAuthentication.service.CommunicationService;
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

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class HomeManagerImpl implements HomeManager {

    private static final Logger logger = LoggerFactory.getLogger(HomeManagerImpl.class);

    @Autowired
    private MongoService mongoService;

    @Value("${connector.email.send.api}")
    private String connectorEmailSendUrl;

    @Value("${pass.manager.user.create.url}")
    private String passManagerUserCreateUrl;

    @Autowired
    private RedisService redisService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private ResponseUtility responseUtility;
    @Autowired
    private TransportUtils transportUtils;

//    @Autowired
//    private EmailConfigRepository emailConfigRepository;

    @Override
    public BaseResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest) throws Exception {
        logger.info("Inside login request");
        LoginResponse loginResponse = new LoginResponse();
        Collection<Error> errors = new ArrayList<>();
        BaseResponse baseResponse = null;
        try {
                if (null == loginRequest) {
                    logger.error(ErrorCodes.LOGIN_BAD_REQUEST);
                    errors.add(Error.builder()
                            .message(ErrorCodes.LOGIN_BAD_REQUEST)
                            .errorCode(String.valueOf(Error.ERROR_TYPE.BAD_REQUEST.toCode()))
                            .errorType(Error.ERROR_TYPE.BAD_REQUEST.toValue())
                            .level(Error.SEVERITY.HIGH.name())
                            .build());
                    baseResponse = responseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                } else {
                    //Check password is correct or not with SHA encryption
                    UserRegistry userRegistry = mongoService.getUserByUsernameorEmailAndProduct(loginRequest.getUserIdentifier(), loginRequest.getUserIdentifier(), loginRequest.getProductName().getName());
                    logger.info("UserRegistry is {}", userRegistry);

                    if (userRegistry == null) {
                        errors.add(Error.builder()
                                .message("Invalid credentials or data")
                                .errorCode(String.valueOf(Error.ERROR_TYPE.DATABASE.toCode()))
                                .errorType(Error.ERROR_TYPE.DATABASE.toValue())
                                .level(Error.SEVERITY.HIGH.name())
                                .build());
                        return responseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                    }

                    String decryptedPassword = EncryptDecryptService.decryptedTextOrReturnSame(userRegistry.getPassword());

                    if (loginRequest.isCrypto()) {
                        String hashedPassword = responseUtility.encryptThisString(decryptedPassword);
                        if (loginRequest.getShaPassword().equals(hashedPassword)) {
                            baseResponse = send2FAOtp(loginResponse, userRegistry, loginRequest.getProductName(), httpRequest);
                        } else {
                            errors.add(Error.builder()
                                    .message("Invalid Credentials")
                                    .errorCode(String.valueOf(Error.ERROR_TYPE.DATABASE.toCode()))
                                    .errorType(Error.ERROR_TYPE.DATABASE.toValue())
                                    .level(Error.SEVERITY.HIGH.name())
                                    .build());
                            baseResponse = responseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                        }
                    }else if (EncryptDecryptService.checkPassword(decryptedPassword, loginRequest.getShaPassword())) {
                        //send Otp to registered email for 2FA
                        baseResponse = send2FAOtp(loginResponse, userRegistry, loginRequest.getProductName(), httpRequest);
                    } else {
                        errors.add(Error.builder()
                                .message("Invalid Credentials")
                                .errorCode(String.valueOf(Error.ERROR_TYPE.DATABASE.toCode()))
                                .errorType(Error.ERROR_TYPE.DATABASE.toValue())
                                .level(Error.SEVERITY.HIGH.name())
                                .build());
                        baseResponse = responseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                    }
                }
        } catch (Exception ex) {
            Error error = new Error();
            error.setMessage(ex.getMessage());
            logger.error("Exception occurred while login due to - ", ex);
            baseResponse = responseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }

    private BaseResponse send2FAOtp(LoginResponse loginResponse, UserRegistry userRegistry, ProductName productName, HttpServletRequest httpRequest) throws Exception {
        logger.debug("Inside send 2FA otp.");
        BaseResponse baseResponse = null;
        try {
            EmailOtpRequest emailOtpRequest = new EmailOtpRequest();
            emailOtpRequest.setEmailId(StringUtils.isNotBlank(userRegistry.getEmailId()) ? userRegistry.getEmailId() : userRegistry.getUserName());
            emailOtpRequest.setOtpRequired(true);
            emailOtpRequest.setProductName(productName);
            emailOtpRequest.setEmailType("2FA_OTP");
            Map<String, String> additionalInfo = new HashMap<>();
            emailOtpRequest.setAdditionalInfo(additionalInfo);
            additionalInfo.put(Constants.FULL_NAME, userRegistry.getFullName());
            baseResponse = communicationService.sendEmailOtp(emailOtpRequest, httpRequest);
            logger.warn("BaseResponse received is {}", baseResponse);
            if (null != baseResponse && null != baseResponse.getPayload() && null != baseResponse.getPayload().getT()) {
                EmailOtpResponse emailOtpResponse = (EmailOtpResponse) baseResponse.getPayload().getT();
                if (emailOtpResponse.isSuccess()) {
                    loginResponse.setOtpToken(emailOtpResponse.getOtp());
                    loginResponse.setResponse(Constants.FURTHER_INSTRUCTION_SENT_ON_EMAIL);
                    loginResponse.setStatus(StatusConstant.SUCCESS.name());
                    loginResponse.setUsername(userRegistry.getUserName());
                    baseResponse = responseUtility.getBaseResponse(HttpStatus.OK, loginResponse);
                }
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending 2FA otp with probable cause - ", e);
            baseResponse = responseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(e));
        }
        return baseResponse;
    }

    private void settingToken(LoginResponse loginResponse, String encryptedPassword, String username) throws Exception {

        String token = TokenGenerator.generateToken(username);

        loginResponse.setToken(token);
        //setToken in redis
        if (null == redisService) {
            redisService = new RedisService();
        }
        long expiryTime = 1800;
        String redisKey = StringUtils.join(username, FieldSeparator.UNDER_SCORE_STR, ProductName.PASSWORD_MANAGER.getName());
        Object obj = responseUtility.redisObject(username, token, expiryTime, null);
        //clear any existing key (One session one login)
        redisService.clearKeyFromRedis(username);
        //add the new key
        redisService.setValueInRedisWithExpiration(redisKey, obj, expiryTime, TimeUnit.SECONDS);
        loginResponse.setServerSideValidation(responseUtility.encryptThisString(encryptedPassword + username));

    }

    public BaseResponse createUser(UserCreation userCreation, HttpServletRequest httpRequest) {
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
                    return responseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                }

                if (null != mongoService.getUserByUsernameorEmailAndProduct(userCreation.getUserName(), userCreation.getEmail(), userCreation.getProductName().getName())) {
                    errors.add(Error.builder()
                            .message(ErrorCodes.USER_ALREADY_EXIST_ERROR)
                            .errorCode(String.valueOf(HttpStatus.CONFLICT.value()))
                            .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                            .level(Error.SEVERITY.LOW.name())
                            .build());
                    return responseUtility.getBaseResponse(HttpStatus.CONFLICT, errors);
                }

                boolean success = createAndSaveUserDetails(userCreation);

                if (success) {
                    genericResponse.setStatus(StatusConstant.SUCCESS.name());
                    genericResponse.setResponseMessage("User Created Successfully");
                    baseResponse = responseUtility.getBaseResponse(HttpStatus.OK, genericResponse);
                } else {
                    genericResponse.setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
                    genericResponse.setResponseMessage("User creation failed.");
                    errors.add(Error.builder()
                            .message(ErrorCodes.USER_CREATION_FAILED)
                            .errorCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                            .errorType(Error.ERROR_TYPE.BUSINESS.toValue())
                            .level(Error.SEVERITY.HIGH.name())
                            .build());
                    baseResponse = responseUtility.getBaseResponse(HttpStatus.OK, genericResponse);
                }

        } catch (Exception ex) {
            Error error = new Error();
            error.setMessage(ex.getMessage());
            logger.error("Exception occurred while user creation due to - ", ex);
            return responseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
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
                return responseUtility.getBaseResponse(HttpStatus.NO_CONTENT, Constants.CONF_NOT_FOUND);
            }

            //Check email flooding
            if (checkEmailFlooding(emailOtpRequest, emailConfiguration.getOtpMaxLimit())) {
                return limitExhausted(emailOtpRequest.getEmailId());
            }
            String otp = responseUtility.generateOtpAgainstLength(6);

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
            baseResponse = responseUtility.getBaseResponse(HttpStatus.OK, emailOtpResponse);

        } catch (Exception e) {
            logger.error("Exception occurred while sending otp with probable cause - ", e);
            baseResponse = responseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(e));
        } finally {
            mongoService.saveEmailResResLog(emailReqResLog);
            emailOtpResponse.setOtp(emailReqResLog.getId());
        }
        return baseResponse;
    }

    @Override
    public BaseResponse validate2faOtp(ValidateOtpRequest validateOtpRequest, HttpServletRequest request) {
        logger.info("Inside validate 2Fa Otp");
        LoginResponse loginResponse = new LoginResponse();
        BaseResponse baseResponse = null;
        Collection<Error> errors = new ArrayList<>();
        try {
                if (null == validateOtpRequest) {
                    logger.error(ErrorCodes.VALIDATE_OTP_BAD_REQUEST);
                    errors.add(Error.builder()
                            .message(ErrorCodes.VALIDATE_OTP_BAD_REQUEST)
                            .errorCode(String.valueOf(Error.ERROR_TYPE.BAD_REQUEST.toCode()))
                            .errorType(Error.ERROR_TYPE.BAD_REQUEST.toValue())
                            .level(Error.SEVERITY.HIGH.name())
                            .build());
                    baseResponse = responseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                } else {
                    baseResponse = communicationService.validateEmailOtp(validateOtpRequest, request);
                    if (null != baseResponse && null != baseResponse.getPayload() && null != baseResponse.getPayload().getT()) {
                        loginResponse = (LoginResponse) baseResponse.getPayload().getT();
                        loginResponse.setToken(jwtService.generateToken(validateOtpRequest.getUserName()));
                    }
                }

        } catch (Exception e) {
            logger.error("Exception occurred while validating 2FA otp with probable cause - ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            baseResponse = responseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }

    private boolean createAndSaveUserDetails(UserCreation userCreation) {
        boolean success = false;
        try {
            UserRegistry userRegistry = new UserRegistry(userCreation.getUserName(), StringUtils.isNotBlank(userCreation.getEmail()) ? userCreation.getEmail() : userCreation.getUserName(), userCreation.getFullName(), userCreation.getGender(), userCreation.getDateOfBirth(), userCreation.getProductName(), true, new Date(), new Date());
            String encryptedPassword = EncryptDecryptService.encryptText(userCreation.getPassword());
            userRegistry.setPassword(encryptedPassword);
            success = mongoService.saveUserRegistry(userRegistry);
            if (success && userCreation.getProductName().getName().equalsIgnoreCase(ProductName.PASSWORD_MANAGER.getName())) {
                //crete user in pass manager
                transportUtils.createUser(userCreation);
            }

        } catch (Exception e) {
            logger.error("Exception occurred while encrypting password with probable cause - ", e);
        }
        return success;
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

    private BaseResponse limitExhausted(String emailId) {

        logger.debug("SMS LIMIT_EXHAUSTED called for emailId - {}", emailId);
        EmailOtpResponse emailOtpResponse = new EmailOtpResponse();

        Error error = new Error();
        error.setMessage(Constants.OTP_LIMIT_REACHED);
        error.setErrorCode("LIMIT_EXHAUSTED");

        emailOtpResponse.setSuccess(false);

        emailOtpResponse.setErrors(new Error[]{error});

        return responseUtility.getBaseResponse(HttpStatus.OK, emailOtpResponse);
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
            baseResponse = responseUtility.getBaseResponse(HttpStatus.OK, genericResponse);

        } catch (Exception e) {
            logger.error("Exception occurred while logout with probable cause ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            baseResponse = responseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }

    @Override
    public BaseResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest httpServletRequest) {
        logger.info("Inside forgotPassword method");
        BaseResponse baseResponse = null;
        EmailOtpResponse emailOtpResponse = new EmailOtpResponse();
        Collection<Error> errors = new ArrayList<>();
        try {
                if (null == forgotPasswordRequest || StringUtils.isBlank(forgotPasswordRequest.getUserIdentifier())) {
                    logger.error(ErrorCodes.FORGOT_PASSWORD_BAD_REQUEST);
                    errors.add(Error.builder()
                            .message(ErrorCodes.FORGOT_PASSWORD_BAD_REQUEST)
                            .errorCode(String.valueOf(Error.ERROR_TYPE.BAD_REQUEST.toCode()))
                            .errorType(Error.ERROR_TYPE.BAD_REQUEST.toValue())
                            .level(Error.SEVERITY.HIGH.name())
                            .build());
                    baseResponse = responseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                } else {
                    UserRegistry userRegistry = mongoService.getUserByUsernameorEmailAndProduct(forgotPasswordRequest.getUserIdentifier(), forgotPasswordRequest.getUserIdentifier(), forgotPasswordRequest.getProductName().getName());
                    if (null!= userRegistry) {
                        EmailOtpRequest emailOtpRequest = new EmailOtpRequest();
                        emailOtpRequest.setEmailId(userRegistry.getEmailId());
                        emailOtpRequest.setProductName(forgotPasswordRequest.getProductName());
                        emailOtpRequest.setOtpRequired(true);
                        emailOtpRequest.setEmailType("FORGOT_PASSWORD_OTP");
                        baseResponse = communicationService.sendEmailOtp(emailOtpRequest, httpServletRequest);

                    } else {
                        emailOtpResponse.setSuccess(true);
                        emailOtpResponse.setOtp(TokenGenerator.generateHexString(24));
                        emailOtpResponse.setMessage(Constants.FURTHER_INSTRUCTION_SENT_ON_EMAIL);
                        baseResponse = responseUtility.getBaseResponse(HttpStatus.OK, emailOtpResponse);
                    }
                }
        } catch (Exception e) {
            logger.error("Exception occurred while forgotPassword with probable cause ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            baseResponse = responseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }

//    @Override
//    public BaseResponse getEmailConfigByType(String type) {
//        EmailConfiguration emailConfiguration = emailConfigRepository.getByEmailType(type);
//        if (null == emailConfiguration) {
//            return responseUtility.getBaseResponse(HttpStatus.NO_CONTENT, responseUtility.getNoContentFoundError());
//        }
//        return responseUtility.getBaseResponse(HttpStatus.OK, emailConfiguration);
//    }

    @Override
    public BaseResponse changePassword(@NotNull ChangePasswordRequest changePasswordRequest, HttpServletRequest httpServletRequest) {
        BaseResponse baseResponse = null;
        GenericResponse genericResponse = new GenericResponse();
        try {
            Collection<Error> errors = new ArrayList<>();
            if (null == changePasswordRequest || StringUtils.isBlank(changePasswordRequest.getOldPassword()) || StringUtils.isBlank(changePasswordRequest.getNewPassword())) {
                logger.error(ErrorCodes.CHANGE_PASSWORD_BAD_REQUEST);
                errors.add(Error.builder()
                        .message(ErrorCodes.CHANGE_PASSWORD_BAD_REQUEST)
                        .errorCode(String.valueOf(Error.ERROR_TYPE.BAD_REQUEST.toCode()))
                        .errorType(Error.ERROR_TYPE.BAD_REQUEST.toValue())
                        .level(Error.SEVERITY.HIGH.name())
                        .build());
                baseResponse = responseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
            } else {
                UserRegistry userRegistry = mongoService.getUserByUsernameorEmailAndProduct(changePasswordRequest.getUserIdentifier(), changePasswordRequest.getUserIdentifier(), changePasswordRequest.getProductName().getName());

                if (null!= userRegistry) {
                    String decryptedPassword = EncryptDecryptService.decryptedTextOrReturnSame(userRegistry.getPassword());
                    if (!decryptedPassword.equalsIgnoreCase(changePasswordRequest.getOldPassword())) {
                        errors.add(Error.builder()
                                .message("Invalid current password.")
                                .errorCode(String.valueOf(Error.ERROR_TYPE.DATABASE.toCode()))
                                .errorType(Error.ERROR_TYPE.DATABASE.toValue())
                                .level(Error.SEVERITY.HIGH.name())
                                .build());
                        baseResponse = responseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);
                    } else {
                        String hashedPassword = EncryptDecryptService.encryptText(changePasswordRequest.getNewPassword());
                        mongoService.updatePasswordByEmailOrUserNameAndProduct(changePasswordRequest.getUserIdentifier(), null, hashedPassword, changePasswordRequest.getProductName().getName());
                        genericResponse.setResponseMessage("Password changed successfully");
                        genericResponse.setStatus(String.valueOf(HttpStatus.OK.value()));
                        baseResponse = responseUtility.getBaseResponse(HttpStatus.OK, genericResponse);
                    }

                } else {
                    String message = String.format("no case match for identifier {} for product {}", changePasswordRequest.getUserIdentifier(), changePasswordRequest.getProductName());
                    logger.error(message);
                    errors.add(Error.builder()
                            .message(message)
                            .errorCode(String.valueOf(Error.ERROR_TYPE.BAD_REQUEST.toCode()))
                            .errorType(Error.ERROR_TYPE.BAD_REQUEST.toValue())
                            .level(Error.SEVERITY.HIGH.name())
                            .build());
                    baseResponse = responseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, errors);

                }
            }
        } catch (Exception e) {
            logger.error("Exception occurred while changePassword with probable cause ", e);
            Error error = new Error();
            error.setMessage(e.getMessage());
            baseResponse = responseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
        return baseResponse;
    }

    @Override
    public String getEmailByUsername(String username, ProductName productName) {
        logger.info("Inside get email bu username for username {} and productName {}", username, productName.getName());
        UserRegistry userRegistry = mongoService.getUserByUsernameAndProduct(username, productName);
        if (null != userRegistry) {
            return userRegistry.getEmailId();
        }
        return null;
    }
}
