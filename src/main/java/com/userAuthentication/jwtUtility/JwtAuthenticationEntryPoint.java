package com.userAuthentication.jwtUtility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.response.Error;
import com.userAuthentication.utility.ResponseUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ResponseUtility responseUtility;

    @SneakyThrows
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response(response, authException);

    }

    private void response(HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        // Construct the JSON response
        BaseResponse baseResponse = new BaseResponse();
        Error error = new Error();
        error.setErrorCode(String.valueOf(HttpServletResponse.SC_UNAUTHORIZED));
        error.setMessage("Access Denied !! " + authException.getMessage());
        error.setErrorType("SYSTEM");
        Collection<Error> errors = new ArrayList<>();
        errors.add(error);
        baseResponse = responseUtility.getBaseResponse(HttpStatus.UNAUTHORIZED, errors);
        // Write the response
        response.getWriter().write(objectMapper.writeValueAsString(baseResponse));
    }
}
