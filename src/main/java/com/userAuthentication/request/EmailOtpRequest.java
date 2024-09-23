package com.userAuthentication.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailOtpRequest {

    @JsonProperty("sEmailId")
    private String emailId;

    @JsonProperty("sEmailType")
    private String emailType;

    @JsonProperty("sProductName")
    private String productName;

    @JsonProperty("bOtpRequired")
    private boolean otpRequired;

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getEmailType() {
        return emailType;
    }

    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public boolean isOtpRequired() {
        return otpRequired;
    }

    public void setOtpRequired(boolean otpRequired) {
        this.otpRequired = otpRequired;
    }

    @Override
    public String toString() {
        return "EmailOtpRequest{" +
                "emailId='" + emailId + '\'' +
                ", emailType='" + emailType + '\'' +
                ", productName='" + productName + '\'' +
                ", otpRequired=" + otpRequired +
                '}';
    }
}
