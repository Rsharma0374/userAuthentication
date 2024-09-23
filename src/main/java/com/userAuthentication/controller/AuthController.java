package com.userAuthentication.controller;

import com.userAuthentication.constant.Constants;
import com.userAuthentication.model.JwtRequest;
import com.userAuthentication.model.JwtResponse;
import com.userAuthentication.request.LoginRequest;
import com.userAuthentication.request.UserCreation;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.service.AuthService;
import com.userAuthentication.service.HomeManager;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private HomeManager homeManager;

    @Autowired
    private AuthService authService;

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/hello")
    public String getResult(){
        return "hello\n";
    }

    @PostMapping("/create-token")
    public ResponseEntity<?> createToken(@RequestBody LoginRequest loginRequest) {
        String token = authService.createToken(loginRequest);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/get-user-from-token")
    public ResponseEntity<?> getUserFromToken(@RequestHeader("Authorization") String token) {
        String user = authService.getUserFromToken(token);
        return ResponseEntity.ok(user);
    }

    @PostMapping(EndPointReferrer.LOGIN)
    public ResponseEntity<BaseResponse> getLogin(
            @RequestBody @NotNull LoginRequest loginRequest,
            HttpServletRequest httpRequest) throws Exception {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.LOGIN);
        return new ResponseEntity<>(homeManager.login(loginRequest, httpRequest), HttpStatus.OK);

    }

    @PostMapping(EndPointReferrer.CREATE_USER)
    public ResponseEntity<BaseResponse> createUser(
            @RequestBody @NotNull UserCreation userCreation) {
        try {
            logger.info(Constants.CONTROLLER_STARTED,EndPointReferrer.CREATE_USER);

            return new ResponseEntity<>(homeManager.createUser(userCreation), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception occurred in request with cause - ", e);
        }
        return null;

    }

}
