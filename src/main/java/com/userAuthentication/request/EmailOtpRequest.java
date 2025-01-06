package com.userAuthentication.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.userAuthentication.constant.ProductName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EmailOtpRequest {

    @JsonProperty("sEmailId")
    private String emailId;

    @JsonProperty("sEmailType")
    private String emailType;

    @JsonProperty("sProductName")
    private ProductName productName;

    @JsonProperty("bOtpRequired")
    private boolean otpRequired;

}
