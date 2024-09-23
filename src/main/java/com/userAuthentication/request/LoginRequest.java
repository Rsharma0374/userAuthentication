package com.userAuthentication.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


/**
 * @author rahul
 */
@Data
public class LoginRequest {

    @JsonProperty("sUserName")
    private String userName;

    @JsonProperty("sSHAPassword")
    private String shaPassword;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getShaPassword() {
        return shaPassword;
    }

    public void setShaPassword(String shaPassword) {
        this.shaPassword = shaPassword;
    }

}