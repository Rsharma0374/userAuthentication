package com.userAuthentication.response.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.userAuthentication.response.Error;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class EmailOtpResponse {

    @JsonProperty("bSuccess")
    private boolean success;

    @JsonProperty("aErrors")
    private Error[] errors;

    @JsonProperty("sOtp")
    private String otp;

    @JsonProperty("sMessage")
    private String message;

}
