package com.userAuthentication.response.login;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {

    @JsonProperty("sResponse")
    private String response;

    @JsonProperty("sToken")
    private String token;

    @JsonProperty("sServerSideValidation")
    private String serverSideValidation;

    @JsonProperty("sOtpToken")
    private String  otpToken;

    @JsonProperty("sStatus")
    private String status;

    @JsonProperty("sEncryptedValue")
    private String encryptedValue;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getServerSideValidation() {
        return serverSideValidation;
    }

    public void setServerSideValidation(String serverSideValidation) {
        this.serverSideValidation = serverSideValidation;
    }

    public String getOtpToken() {
        return otpToken;
    }

    public void setOtpToken(String otpToken) {
        this.otpToken = otpToken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEncryptedValue() {
        return encryptedValue;
    }

    public void setEncryptedValue(String encryptedValue) {
        this.encryptedValue = encryptedValue;
    }
}
