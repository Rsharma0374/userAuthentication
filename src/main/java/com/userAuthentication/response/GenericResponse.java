package com.userAuthentication.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse {

    @JsonProperty("sStatus")
    private String status;

    @JsonProperty("sResponseMessage")
    private String responseMessage;
}
