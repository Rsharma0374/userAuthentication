package com.chat.userAuthentication.userController;

import com.chat.userAuthentication.request.LoginRequest;
import com.chat.userAuthentication.request.UserCreation;
import com.chat.userAuthentication.response.BaseResponse;
import com.chat.userAuthentication.service.HomeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private HomeManager homeManager;

    @PostMapping(EndPointReferrer.LOGIN)
    public ResponseEntity<BaseResponse> getLogin(
            @Validated(value = {LoginRequest.FetchGrp.class})
            @RequestBody @NotNull LoginRequest loginRequest,
            HttpServletRequest httpRequest) throws Exception {

        logger.debug("{} controller started",EndPointReferrer.LOGIN);

        return new ResponseEntity<>(homeManager.login(loginRequest, httpRequest), HttpStatus.OK);

    }

    @PostMapping(EndPointReferrer.NEW_USER)
    public ResponseEntity<BaseResponse> createUser(
            @RequestBody @NotNull UserCreation userCreation) throws Exception {
        try {
            logger.debug("{} controller started",EndPointReferrer.NEW_USER);

            return new ResponseEntity<>(homeManager.createUser(userCreation), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ");
        }
        return null;

    }
}
