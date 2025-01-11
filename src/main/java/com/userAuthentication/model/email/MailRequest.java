package com.userAuthentication.model.email;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MailRequest {

    private String to;

    private String subject;

    private String message;

}
