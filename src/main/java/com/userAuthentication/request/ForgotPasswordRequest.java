package com.userAuthentication.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.userAuthentication.constant.ProductName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class ForgotPasswordRequest {

    @JsonProperty("sUserIdentifier")
    private String userIdentifier;

    @JsonProperty("sProductName")
    private ProductName productName;

}
