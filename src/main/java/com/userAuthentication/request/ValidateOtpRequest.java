package com.userAuthentication.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidateOtpRequest {

    @JsonProperty("sOtp")
    private String otp;

    @JsonProperty("sOtpId")
    private String otpId;

    @JsonProperty("sProductName")
    private String productName;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getOtpId() {
        return otpId;
    }

    public void setOtpId(String otpId) {
        this.otpId = otpId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Override
    public String toString() {
        return "ValidateOtpRequest{" +
                "otp='" + otp + '\'' +
                ", otpId='" + otpId + '\'' +
                ", productName='" + productName + '\'' +
                '}';
    }
}
