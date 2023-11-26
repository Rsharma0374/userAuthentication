package com.chat.userAuthentication.dao;

import com.chat.userAuthentication.configuration.EmailConfiguration;
import com.chat.userAuthentication.constant.Constants;
import com.chat.userAuthentication.model.email.EmailReqResLog;
import com.chat.userAuthentication.request.UserCreation;
import com.chat.userAuthentication.request.ValidateOtpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.Date;

@Repository
public class MongoServiceImpl implements MongoService{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    private static final Logger logger = LoggerFactory.getLogger(MongoServiceImpl.class);

    @Override
    public boolean saveData(UserCreation userCreation) {
        try {
            mongoTemplate.insert(userCreation);
        } catch (Exception e) {
            logger.error("Exception - ", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean checkExistence(UserCreation userCreation) {
        logger.info("Inside check existence method");

        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("userName").is(userCreation.getUserName())
                    .and("accountActive").is(true));

            return mongoTemplate.exists(query, "userCreation");
        } catch (Exception e) {
            logger.error("Exception occurred due to - ", e);
            return false;
        }
    }

    @Override
    public UserCreation getUserFromUserName(String userName) {
        logger.info("Inside check getUserFromUserName method");
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("userName").is(userName)
                    .and("accountActive").is(true));
            return mongoTemplate.findOne(query, UserCreation.class);
        } catch (Exception e) {
            logger.error("Exception occurred while getting user due to - ", e);
            return null;
        }
    }

    @Override
    public EmailConfiguration getEmailConfigByProductAndType(String emailType, String productName, boolean otpRequired) {
        logger.info("Inside getEmailConfigByProductAndType method");
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("emailType").is(emailType)
                    .and("productName").is(productName)
                    .and("isOtpRequired").is(otpRequired)
                    .and(Constants.ACTIVE).is(true));
            return mongoTemplate.findOne(query, EmailConfiguration.class);
        } catch (Exception e) {
            logger.error("Exception occurred while getting productName due to - ", e);
            return null;
        }
    }

    @Override
    public long getEmailTriggerCount(String emailId, String productName, String emailType) {
        try {
            Date currentDate = new Date();

            // get one hour back date
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.add(Calendar.HOUR, -1);
            Date oneHourBack = cal.getTime();

            Query query = new Query();
            // need indexing on this query
            query.addCriteria(Criteria.where("dateTime").gte(oneHourBack).lte(currentDate)
                    .and("emailId").is(emailId)
                    .and("emailType").is(emailType)
                    .and("mailResponse.status").is(Constants.SUCCESS));

            logger.info("Query is {}", query);

            return mongoTemplate.count(query, EmailReqResLog.class);

        } catch (Exception e) {
            logger.error("Exception occur while checking Email flooding for EmailType {} , emailId {} with probable cause- ", emailType, emailId, e);
            return 0;
        }

    }

    @Override
    public boolean saveEmailResResLog(EmailReqResLog emailReqResLog) throws Exception {
        try {
            if (!mongoTemplate.collectionExists(EmailReqResLog.class))
                mongoTemplate.createCollection(EmailReqResLog.class);
            mongoTemplate.insert(emailReqResLog);
        } catch (Exception e) {
            logger.error("Error for saveSmsServiceReqResLog ", e);
            throw new Exception(String.format("Error while save email Req Res Log", e.getMessage()));
        }
        return false;
    }

    @Override
    public EmailReqResLog getEmailReqResLog(ValidateOtpRequest validateOtpRequest) {

        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(validateOtpRequest.getOtpId()));

            logger.info("Query is {}", query);

            return mongoTemplate.findOne(query, EmailReqResLog.class);


        } catch (Exception e) {
            logger.error("Error for saveSmsServiceReqResLog ", e);
            return null;
        }
    }

    @Override
    public boolean checkExistenceWithEmail(String email) {
        logger.info("Inside check existence method");

        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("emailId").is(email)
                    .and("accountActive").is(true));

            return mongoTemplate.exists(query, "userCreation");
        } catch (Exception e) {
            logger.error("Exception occurred due to - ", e);
            return false;
        }
    }
}
