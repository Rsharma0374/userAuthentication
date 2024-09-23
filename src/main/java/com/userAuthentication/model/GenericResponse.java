package com.userAuthentication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@ToString
public class GenericResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("sResponseMessage")
    private String responseMessage;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
