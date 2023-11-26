package com.chat.userAuthentication.dao;

import com.chat.userAuthentication.configuration.EmailConfiguration;
import com.chat.userAuthentication.model.email.EmailReqResLog;
import com.chat.userAuthentication.request.UserCreation;
import com.chat.userAuthentication.request.ValidateOtpRequest;


public interface MongoService {
    boolean saveData(UserCreation userCreation);

    boolean checkExistence(UserCreation userCreation);

    UserCreation getUserFromUserName(String userName);

    EmailConfiguration getEmailConfigByProductAndType(String emailType, String productName, boolean otpRequired);

    long getEmailTriggerCount(String emailId, String productName, String emailType);

    boolean saveEmailResResLog(EmailReqResLog emailReqResLog) throws Exception;

    EmailReqResLog getEmailReqResLog(ValidateOtpRequest validateOtpRequest);
}
