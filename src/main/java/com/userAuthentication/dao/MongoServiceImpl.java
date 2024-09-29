package com.userAuthentication.dao;

import com.userAuthentication.configuration.EmailConfiguration;
import com.userAuthentication.constant.Constants;
import com.userAuthentication.model.email.EmailReqResLog;
import com.userAuthentication.model.user.UserRegistry;
import com.userAuthentication.request.UserCreation;
import com.userAuthentication.request.ValidateOtpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
    public boolean saveUserRegistry(UserRegistry userRegistry) {
        logger.info("Inside saveUserRegistry");
        try {
            mongoTemplate.insert(userRegistry);
        } catch (Exception e) {
            logger.error("Exception occurred while saving user registry with probable cause - ", e);
            return false;
        }
        return true;
    }

    @Override
    public UserRegistry getUserByUsername(String userName) {
        logger.info("Inside get User by userName method");
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("userName").is(userName)
                    .and("accountActive").is(true));

            return mongoTemplate.findOne(query, UserRegistry.class);
        } catch (Exception e) {
            logger.error("Exception occurred while getUserByUsername for username {} with probable cause - ", userName, e);
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
                    .and("otpRequired").is(otpRequired)
                    .and(Constants.ACTIVE).is(true));
            return mongoTemplate.findOne(query, EmailConfiguration.class);
        } catch (Exception e) {
            logger.error("Exception occurred while getting email config for {} with productName {} due to probable cause - ", emailType, productName, e);
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
                    .and("mailResponseStatus").is(Constants.SUCCESS));

            logger.info("Query is {}", query);

            return mongoTemplate.count(query, EmailReqResLog.class);

        } catch (Exception e) {
            logger.error("Exception occur while checking Email flooding for EmailType {} , emailId {} with probable cause- ", emailType, emailId, e);
            return 0;
        }

    }

    @Override
    public boolean saveEmailResResLog(EmailReqResLog emailReqResLog) {
        try {
            if (!mongoTemplate.collectionExists(EmailReqResLog.class))
                mongoTemplate.createCollection(EmailReqResLog.class);
            mongoTemplate.insert(emailReqResLog);
        } catch (Exception e) {
            logger.error("Error for save EmailServiceReqResLog with probable cause - ", e);
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

    @Override
    public UserCreation getUserWithEmail(String email) {
        logger.info("Get user with email method");

        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("emailId").is(email)
                    .and("accountActive").is(true));

            return mongoTemplate.findOne(query, UserCreation.class);
        } catch (Exception e) {
            logger.error("Exception occurred due to - ", e);
            return null;
        }
    }

    @Override
    public void updatePassword(String emailId, String password) {
        logger.info("Inside update Password method");

        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("emailId").is(emailId)
                    .and("accountActive").is(true));

            Update update = new Update();
            update.set("password", password);

            mongoTemplate.updateFirst(query, update, UserCreation.class);
        } catch (Exception e) {
            logger.error("Exception occurred due to - ", e);
        }
    }

    @Override
    public EmailReqResLog getEmailReqResLogByUserToken(String userToken) {

        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("userToken").is(userToken));

            logger.info("Query is {}", query);

            return mongoTemplate.findOne(query, EmailReqResLog.class);


        } catch (Exception e) {
            logger.error("Error for saveSmsServiceReqResLog ", e);
            return null;
        }
    }

    @Override
    public void saveEmailOtpReqRes(EmailReqResLog emailReqResLog) {
        try {
            mongoTemplate.save(emailReqResLog);
        } catch (Exception e) {
            logger.error("exception occur during save emailReqResLog for ackid {} with probable cause- ", emailReqResLog.getId(), e);
        }
    }
}
