package com.userAuthentication.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EncryptedResponse {

    @JsonProperty("sResponse")
    private String response;
}
