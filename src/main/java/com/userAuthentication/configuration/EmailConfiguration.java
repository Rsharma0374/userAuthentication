package com.userAuthentication.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "emailConfiguration")
@Data
@ToString
public class EmailConfiguration {

    @JsonProperty("sEmailType")
    private String emailType;

    @JsonProperty("sProductName")
    private String productName;

    @JsonProperty("sEmailSubject")
    private String emailSubject;

    @JsonProperty("sEmailBody")
    private String emailBody;

    @JsonProperty("bOtpRequired")
    private boolean otpRequired;

    @JsonProperty("iOtpMaxLimit")
    private int otpMaxLimit;

    @JsonProperty("sReceiver")
    private String receiver;

    @JsonProperty("bActive")
    private boolean active;

    @JsonProperty("dtInsertDate")
    private Date insertDate;

    @JsonProperty("bLimitCheck")
    private boolean limitCheck;


    public String getFormattedSMSText(String... args) {
        String formattedSmsText = String.format(this.emailBody, args);
        return formattedSmsText;
    }

}
