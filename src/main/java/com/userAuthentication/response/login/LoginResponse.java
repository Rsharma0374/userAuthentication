package com.userAuthentication.response.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
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

    @JsonProperty("sUsername")
    private String username;

}
