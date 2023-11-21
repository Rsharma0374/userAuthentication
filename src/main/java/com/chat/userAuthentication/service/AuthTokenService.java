package com.chat.userAuthentication.service;

import com.chat.userAuthentication.model.JwtRequest;
import com.chat.userAuthentication.model.JwtResponse;

public interface AuthTokenService {

    JwtResponse getToken(JwtRequest request);
}
