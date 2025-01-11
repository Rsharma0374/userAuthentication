package com.userAuthentication.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.userAuthentication.constant.ProductName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Data
@ToString
@NoArgsConstructor
public class ValidateOtpRequest {

    @NonNull
    @NotBlank
    @JsonProperty("sOtp")
    private String otp;

    @NonNull
    @NotBlank
    @JsonProperty("sOtpId")
    private String otpId;

    @NonNull
    @NotBlank
    @JsonProperty("sProductName")
    private ProductName productName;

    @JsonProperty("sUserName")
    private String userName;

}
