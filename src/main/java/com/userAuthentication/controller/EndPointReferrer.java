package com.userAuthentication.controller;

public class EndPointReferrer {

    public static final String LOGIN = "user-login";

    public static final String CREATE_USER = "create-user";

    public static final String VALIDATE_2FA_OTP = "validate-tfa-otp";

    public static final String LOGOUT = "logout";

    public static final String GET_TOKEN = "get-token";

    public static final String SEND_EMAIL_OTP = "send-email-otp";

    public static final String  FORGET_PASSWORD = "forget-password";

    public static final String  VALIDATE_OTP_RESET_PASSWORD = "validate-otp-reset-password";

    public static final String  VALIDATE_EMAIL = "validate-email";

    public static final String  VALIDATE_EMAIL_OTP = "validate-email-otp";

    public static final String  VALIDATE_VERIFICATION_OTP = "validate-verification-otp";

    public static final String  CLEAR_REDIS_CACHE = "clear-redis-cache/{sKey}";

    public static final String  GET_REDIS_CACHE = "get-redis-cache-by-key/{sKey}";

}
