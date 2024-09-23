package com.userAuthentication.model.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "emailReqResLog")
public class EmailReqResLog {

    @Id
    @JsonProperty("sId")
    private String id;

    @JsonProperty("dtDateTime")
    private Date dateTime;

    @JsonProperty("sEmailId")
    private String emailId;

    @JsonProperty("sMailTo")
    private String mailTo;

    @JsonProperty("sMailSubject")
    private String mailSubject;

    @JsonProperty("sMailMessage")
    private String mailMessage;

    @JsonProperty("sMailResponseStatus")
    private String mailResponseStatus;

    @JsonProperty("sEmailType")
    private String emailType;

    @JsonProperty("sOtp")
    private String otp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailMessage() {
        return mailMessage;
    }

    public void setMailMessage(String mailMessage) {
        this.mailMessage = mailMessage;
    }

    public String getMailResponseStatus() {
        return mailResponseStatus;
    }

    public void setMailResponseStatus(String mailResponseStatus) {
        this.mailResponseStatus = mailResponseStatus;
    }

    public String getEmailType() {
        return emailType;
    }

    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    @Override
    public String toString() {
        return "EmailReqResLog{" +
                ", dateTime=" + dateTime +
                ", emailId='" + emailId + '\'' +
                ", mailTo='" + mailTo + '\'' +
                ", mailSubject='" + mailSubject + '\'' +
                ", mailMessage='" + mailMessage + '\'' +
                ", mailResponseStatus='" + mailResponseStatus + '\'' +
                ", emailType='" + emailType + '\'' +
                ", otp='" + otp + '\'' +
                '}';
    }
}
