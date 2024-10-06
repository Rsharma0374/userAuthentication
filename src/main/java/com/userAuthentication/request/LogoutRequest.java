package com.userAuthentication.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LogoutRequest {

    @JsonProperty("sUserName")
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "LogoutRequest{" +
                "userName='" + userName + '\'' +
                '}';
    }
}
