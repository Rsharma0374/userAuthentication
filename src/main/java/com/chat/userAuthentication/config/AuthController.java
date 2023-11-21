package com.chat.userAuthentication.config;

import com.chat.userAuthentication.model.JwtRequest;
import com.chat.userAuthentication.model.JwtResponse;
import com.chat.userAuthentication.security.JwtHelper;
import com.chat.userAuthentication.controller.EndPointReferrer;
import com.chat.userAuthentication.service.AuthTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private JwtHelper helper;

    @Autowired
    AuthTokenService authTokenService;

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);


    @PostMapping(EndPointReferrer.GET_TOKEN)
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {

        return new ResponseEntity<>(authTokenService.getToken(request), HttpStatus.OK);
    }

}
