package com.chat.userAuthentication.response.email;

import com.chat.userAuthentication.response.Error;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class ValidateOtpResponse {

    @JsonProperty("bSuccess")
    private boolean success;

    @JsonProperty("aErrors")
    private Error[] errors;

    @JsonProperty("sServerSideValidation")
    private String serverSideValidation;

    @JsonProperty("sMessage")
    private String message;

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

    public String getServerSideValidation() {
        return serverSideValidation;
    }

    public void setServerSideValidation(String serverSideValidation) {
        this.serverSideValidation = serverSideValidation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ValidateOtpResponse{" +
                "success=" + success +
                ", errors=" + Arrays.toString(errors) +
                ", serverSideValidation='" + serverSideValidation + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
