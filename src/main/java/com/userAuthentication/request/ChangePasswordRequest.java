package com.userAuthentication.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.userAuthentication.constant.ProductName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @JsonProperty("sUserIdentifier")
    private String userIdentifier;

    @JsonProperty("sOldPassword")
    private String oldPassword;

    @JsonProperty("sNewPassword")
    private String newPassword;

    @JsonProperty("sProductName")
    private ProductName productName;
}
