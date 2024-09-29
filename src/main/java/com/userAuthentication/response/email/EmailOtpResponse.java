package com.userAuthentication.response.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.userAuthentication.response.Error;
import lombok.ToString;

@ToString
public class EmailOtpResponse {

    @JsonProperty("bSuccess")
    private boolean success;

    @JsonProperty("sUserToken")
    private String userToken;

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

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
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
