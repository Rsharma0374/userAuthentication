package com.chat.userAuthentication.response.login;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {

    @JsonProperty("sResponse")
    private String response;

    @JsonProperty("sToken")
    private String token;

    @JsonProperty("sServerSideValidation")
    private String serverSideValidation;

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
}
