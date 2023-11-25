package com.chat.userAuthentication.model.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection="emailReqResLog")
public class EmailReqResLog {

    @JsonProperty("id")
    @Id
    private String id;

    @JsonProperty("dtDateTime")
    private Date dateTime;

    @JsonProperty("sEmailId")
    private String emailId;

    @JsonProperty("oMailRequest")
    private MailRequest mailRequest;

    @JsonProperty("oMailResponse")
    private MailResponse mailResponse;

    @JsonProperty("sEmailType")
    private String emailType;

    @JsonProperty("lApiTimeTaken")
    private long apiTimeTaken;

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

    public MailRequest getMailRequest() {
        return mailRequest;
    }

    public void setMailRequest(MailRequest mailRequest) {
        this.mailRequest = mailRequest;
    }

    public MailResponse getMailResponse() {
        return mailResponse;
    }

    public void setMailResponse(MailResponse mailResponse) {
        this.mailResponse = mailResponse;
    }

    public String getEmailType() {
        return emailType;
    }

    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }

    public long getApiTimeTaken() {
        return apiTimeTaken;
    }

    public void setApiTimeTaken(long apiTimeTaken) {
        this.apiTimeTaken = apiTimeTaken;
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
                "id='" + id + '\'' +
                ", dateTime=" + dateTime +
                ", emailId='" + emailId + '\'' +
                ", mailRequest=" + mailRequest +
                ", mailResponse=" + mailResponse +
                ", emailType='" + emailType + '\'' +
                ", apiTimeTaken=" + apiTimeTaken +
                ", otp='" + otp + '\'' +
                '}';
    }
}
