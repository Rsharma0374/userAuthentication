package com.userAuthentication.response.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.userAuthentication.response.Error;

public class EmailOtpResponse {

    @JsonProperty("bSuccess")
    private boolean success;

    @JsonProperty("aErrors")
    private Error[] errors;

    @JsonProperty("sOtp")
    private String otp;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Error[] getErrors() {
        return errors;
    }

    public void setErrors(Error[] errors) {
        this.errors = errors;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
