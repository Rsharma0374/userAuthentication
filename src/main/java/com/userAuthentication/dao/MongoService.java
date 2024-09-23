package com.userAuthentication.dao;

import com.userAuthentication.configuration.EmailConfiguration;
import com.userAuthentication.model.email.EmailReqResLog;
import com.userAuthentication.model.user.UserRegistry;
import com.userAuthentication.request.UserCreation;
import com.userAuthentication.request.ValidateOtpRequest;

public interface MongoService {

    EmailConfiguration getEmailConfigByProductAndType(String emailType, String productName, boolean otpRequired);

    long getEmailTriggerCount(String emailId, String productName, String emailType);

    boolean saveEmailResResLog(EmailReqResLog emailReqResLog);

    EmailReqResLog getEmailReqResLog(ValidateOtpRequest validateOtpRequest);

    boolean checkExistenceWithEmail(String email);

    UserCreation getUserWithEmail(String email);

    void updatePassword(String emailId, String password);

    boolean saveUserRegistry(UserRegistry userRegistry);

    UserRegistry getUserByUsername(String userName);
}
