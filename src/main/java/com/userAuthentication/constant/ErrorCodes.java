package com.userAuthentication.constant;

public class ErrorCodes {
    public static final String MANDATORY_CONFIGURATION_NOT_FOUND_FOR_THIS_SERVICE = "Mandatory configuration not found for this service.";
    public static final String LIMIT_EXHAUSTED = "LIMIT_EXHAUSTED";
    public static final String OTP_LIMIT_REACHED = "You have exhausted otp send limit. Please try after sometime.";
    public static final String OTP_EXPIRED = "One time password has been expired. Please request new one time password.";
    public static final String OTP_VALIDATE_LIMIT_REACHED = "Maximum OTP limit reached, please request new OTP";
    public static final String OTP_VERIFICATION_FAILED = "OTP verification failed";
    public static final String USER_CREATION_REQUEST_OBJECT_NULL = "User Creation Object cannot be null.";
    public static final String USER_ALREADY_EXIST_ERROR = "User already exists and active with provided username or email";
    public static final String USER_CREATION_FAILED = "User creation failed.";
    public static final String FORGOT_PASSWORD_BAD_REQUEST = "Forgot password request is null or email and username bot are blank";
    public static final String SOMETHING_WENT_WRONG = "Something went wrong, Please contact system administrator";
    public static final String VALIDATE_OTP_BAD_REQUEST = "Validate otp request is null.";
    public static final String SMS_SENDING_FAIL_TRY_AGAIN = "Sms sending failed, try again";
    public static final String LOGIN_BAD_REQUEST = "Login request is null.";
    public static final String CHANGE_PASSWORD_BAD_REQUEST = "Change password request is null or new password or old password are blank";


}
