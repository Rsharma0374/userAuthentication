package com.chat.userAuthentication.dao;

import com.chat.userAuthentication.request.UserCreation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;

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
}
