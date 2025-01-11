package com.userAuthentication.response.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Arrays;

@Data
@ToString
public class ValidateOtpResponse {

    @JsonProperty("sStatus")
    private String success;

    @JsonProperty("aErrors")
    private Error[] errors;

    @JsonProperty("sServerSideValidation")
    private String serverSideValidation;

    @JsonProperty("sMessage")
    private String message;

}
