package com.chat.userAuthentication.service.impl;

import com.chat.userAuthentication.config.AuthController;
import com.chat.userAuthentication.dao.MongoService;
import com.chat.userAuthentication.model.JwtRequest;
import com.chat.userAuthentication.model.JwtResponse;
import com.chat.userAuthentication.request.LoginRequest;
import com.chat.userAuthentication.request.UserCreation;
import com.chat.userAuthentication.response.BaseResponse;
import com.chat.userAuthentication.response.Error;
import com.chat.userAuthentication.response.Payload;
import com.chat.userAuthentication.response.Status;
import com.chat.userAuthentication.response.login.LoginResponse;
import com.chat.userAuthentication.service.AuthTokenService;
import com.chat.userAuthentication.service.HomeManager;
import com.chat.userAuthentication.utility.ResponseUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

//import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@Service
public class HomeManagerImpl implements HomeManager {

    private static final Logger logger = LoggerFactory.getLogger(HomeManagerImpl.class);

    @Autowired
    MongoService mongoService;

    @Autowired
    AuthTokenService authTokenService;

    @Override
    public BaseResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest) throws Exception {
        logger.info("Inside login request");
        LoginResponse loginResponse = new LoginResponse();
        try {
            //Check password is correct or not with SHA encryption
            UserCreation userCreation = mongoService.getUserFromUserName(loginRequest.getUserName());

            if (userCreation == null) {
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, "No User found against provided username");
            }

            String encryptedPassword = ResponseUtility.encryptThisString(userCreation.getPassword());

            if (StringUtils.equalsIgnoreCase(encryptedPassword, loginRequest.getShaPassword())) {
                loginResponse.setResponse("Access Granted");
                settingToken(loginResponse);
                return ResponseUtility.getBaseResponse(HttpStatus.OK, loginResponse);
            } else {
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, "Password Incorrect");
            }

        } catch (Exception ex) {
            Error error = new Error();
            error.setMessage(ex.getMessage());
            logger.error("Exception occurred while login due to - ", ex);
            return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }
    }

    private void settingToken(LoginResponse loginResponse) throws Exception {
        JwtRequest request = new JwtRequest();
        request.setEmail("RAHUL");
        request.setPassword("RAHUL");
        JwtResponse response = authTokenService.getToken(request);
        if (response != null) {
            loginResponse.setToken(response.getJwtToken());
        } else {
            throw new Exception("Token Not found");
        }
    }

    public BaseResponse createUser(UserCreation userCreation) {
        logger.info("Inside Create user method");

        try {
            if (userCreation == null) {
                return ResponseUtility.getBaseResponse(HttpStatus.BAD_REQUEST, "User Creation Object cannot be null");
            }

            if (mongoService.checkExistence(userCreation)) {
                return ResponseUtility.getBaseResponse(HttpStatus.CONFLICT, "User already exists and active with username: " + userCreation.getUserName());
            }

            boolean success = mongoService.saveData(userCreation);

            if (success) {
                return ResponseUtility.getBaseResponse(HttpStatus.OK, "User Created Successfully");
            } else {
                return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, "User creation failed..");
            }
        } catch (Exception ex) {
            Error error = new Error();
            error.setMessage(ex.getMessage());
            logger.error("Exception occurred while user creation due to - ", ex);
            return ResponseUtility.getBaseResponse(HttpStatus.INTERNAL_SERVER_ERROR, Collections.singleton(error));
        }

    }
}
