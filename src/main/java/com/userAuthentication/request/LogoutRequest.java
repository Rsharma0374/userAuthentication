package com.userAuthentication.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.userAuthentication.constant.ProductName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LogoutRequest {

    @JsonProperty("sUserName")
    private String userName;

    @JsonProperty("sProductName")
    private ProductName productName;

}
