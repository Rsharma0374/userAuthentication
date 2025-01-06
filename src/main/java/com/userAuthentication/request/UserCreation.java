package com.userAuthentication.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.userAuthentication.constant.ProductName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserCreation {

    @JsonProperty("sUserName")
    private String userName;

    @JsonProperty("sEmail")
    private String email;

    @JsonProperty("sPassword")
    private String password;

    @JsonProperty("sFullName")
    private String fullName;

    @JsonProperty("sGender")
    private String gender;

    @JsonProperty("sDateOfBirth")
    private String dateOfBirth;

    @JsonProperty("sProductName")
    private ProductName productName;

}
