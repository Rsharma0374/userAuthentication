package com.userAuthentication.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.userAuthentication.constant.ProductName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


/**
 * @author rahul
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @JsonProperty("sUserIdentifier")
    private String userIdentifier;

    @JsonProperty("sSHAPassword")
    private String shaPassword;

    @JsonProperty("sProductName")
    private ProductName productName;

    @JsonProperty("bCrypto")
    private boolean crypto;

    @JsonProperty("sRequestType")
    private String requestType;
}