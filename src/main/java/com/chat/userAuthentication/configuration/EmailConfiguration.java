package com.chat.userAuthentication.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "emailConfiguration")
public class EmailConfiguration {

    @JsonProperty("sEmailType")
    private String emailType;

    @JsonProperty("sProductName")
    private String productName;

    @JsonProperty("sEmailSubject")
    private String emailSubject;

    @JsonProperty("sEmailBody")
    private String emailBody;

    @JsonProperty("bIsOtpRequired")
    private boolean isOtpRequired;

    @JsonProperty("iOtpMaxLimit")
    private int otpMaxLimit;

    @JsonProperty("sReceiver")
    private String receiver;

    @JsonProperty("bActive")
    private boolean active;

    @JsonProperty("dtInsertDate")
    private Date insertDate;

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

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public boolean isOtpRequired() {
        return isOtpRequired;
    }

    public void setOtpRequired(boolean otpRequired) {
        isOtpRequired = otpRequired;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Date insertDate) {
        this.insertDate = insertDate;
    }

    public int getOtpMaxLimit() {
        return otpMaxLimit;
    }

    public void setOtpMaxLimit(int otpMaxLimit) {
        this.otpMaxLimit = otpMaxLimit;
    }

    @Override
    public String toString() {
        return "EmailConfiguration{" +
                "emailType='" + emailType + '\'' +
                ", productName='" + productName + '\'' +
                ", emailSubject='" + emailSubject + '\'' +
                ", emailBody='" + emailBody + '\'' +
                ", isOtpRequired=" + isOtpRequired +
                ", otpMaxLimit=" + otpMaxLimit +
                ", receiver='" + receiver + '\'' +
                ", active=" + active +
                ", insertDate=" + insertDate +
                '}';
    }

    public String getFormattedSMSText(String... args) {
        String formattedSmsText = String.format(this.emailBody, args);
        return formattedSmsText;
    }

}
