package com.userAuthentication.constant;

public class ErrorCodes {
    public static final String MANDATORY_CONFIGURATION_NOT_FOUND_FOR_THIS_SERVICE = "Mandatory configuration not found for this service.";
    public static final String LIMIT_EXHAUSTED = "LIMIT_EXHAUSTED";
    public static final String OTP_LIMIT_REACHED = "You have exhausted otp send limit. Please try after sometime.";
    public static final String OTP_EXPIRED = "One time password has been expired. Please request new one time password.";
    public static final String OTP_VALIDATE_LIMIT_REACHED = "Maximum OTP limit reached, please request new OTP";
    public static final String OTP_VERIFICATION_FAILED = "OTP verification failed";
    public static final String USER_CREATION_REQUEST_OBJECT_NULL = "User Creation Object cannot be null.";
    public static final String USER_ALREADY_EXIST_ERROR = "User already exists and active with username: %s";
    public static final String USER_CREATION_FAILED = "User creation failed.";

}
