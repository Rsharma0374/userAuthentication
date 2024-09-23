/**
 * The HomeController class in the userAuthentication package handles various user authentication endpoints and operations.
 */
package com.userAuthentication.controller;

import com.userAuthentication.constant.Constants;
import com.userAuthentication.request.EmailOtpRequest;
import com.userAuthentication.request.LoginRequest;
import com.userAuthentication.request.UserCreation;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.service.HomeManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private HomeManager homeManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/hello")
    public String getResult(){
        return "hello\n";
    }


    @PostMapping(EndPointReferrer.SEND_EMAIL_OTP)
    public ResponseEntity<BaseResponse> sendEmailOtp(
            @RequestBody @NotNull EmailOtpRequest emailOtpRequest) {

        logger.debug(Constants.CONTROLLER_STARTED, EndPointReferrer.SEND_EMAIL_OTP);

        return new ResponseEntity<>(homeManager.sendEmailOtp(emailOtpRequest), HttpStatus.OK);

    }

//    @PostMapping(EndPointReferrer.FORGET_PASSWORD)
//    public ResponseEntity<BaseResponse> sendForgotEmailOtp(
//            @RequestBody @NotNull EmailOtpRequest emailOtpRequest) {
//        try {
//            logger.debug("{} controller started",EndPointReferrer.FORGET_PASSWORD);
//
//            return new ResponseEntity<>(homeManager.sendForgotOtp(emailOtpRequest), HttpStatus.OK);
//
//        } catch (Exception e) {
//            logger.error("Exception occurred in request with cause - ");
//        }
//        return null;
//
//    }
//
//    @PostMapping(EndPointReferrer.VALIDATE_EMAIL_OTP)
//    public ResponseEntity<BaseResponse> validateEmailOtp(
//            @RequestBody @NotNull ValidateOtpRequest validateOtpRequest) {
//        try {
//            logger.debug("{} controller started",EndPointReferrer.VALIDATE_EMAIL_OTP);
//
//            return new ResponseEntity<>(homeManager.validateOtpAndResetPassword(validateOtpRequest), HttpStatus.OK);
//
//        } catch (Exception e) {
//            logger.error("Exception occurred in request with cause - ");
//        }
//        return null;
//
//    }
//
//    /**
//     * This Java function handles a POST request to validate an OTP and returns a ResponseEntity with the result.
//     *
//     * @param validateOtpRequest The `validateOtpRequest` parameter is of type `ValidateOtpRequest`, which is annotated
//     * with `@RequestBody` and `@NotNull`. This means that the `validateOtpRequest` object will be populated with the
//     * request body data from the incoming HTTP request, and it cannot be null
//     * @return The method is returning a `ResponseEntity<BaseResponse>` object.
//     */
//    @PostMapping(EndPointReferrer.VALIDATE_VERIFICATION_OTP)
//    public ResponseEntity<BaseResponse> verifyOtp(
//            @RequestBody @NotNull ValidateOtpRequest validateOtpRequest) {
//        try {
//            logger.debug("{} controller started",EndPointReferrer.VALIDATE_VERIFICATION_OTP);
//
//            return new ResponseEntity<>(homeManager.validateOtp(validateOtpRequest), HttpStatus.OK);
//
//        } catch (Exception e) {
//            logger.error("Exception occurred in request with cause - ");
//        }
//        return null;
//
//    }
//
//    /**
//     * This Java function retrieves a token from a Redis cache based on a given key.
//     *
//     * @param key The `key` parameter in the `getTokenByKey` method is a path variable annotated with `@PathVariable`. It
//     * is of type `String` and is validated using `@NotNull` and `@Valid` annotations to ensure that it is not null and
//     * meets any validation constraints specified for the `String
//     * @return The method `getTokenByKey` is being called with the `key` parameter, and the result is being returned as a
//     * ResponseEntity with a BaseResponse object and HttpStatus OK.
//     */
//    @GetMapping(EndPointReferrer.GET_REDIS_CACHE)
//    public ResponseEntity<BaseResponse> getTokenByKey(@PathVariable("sKey") @NotNull @Valid String key) {
//        return new ResponseEntity<>(homeManager.getTokenByKey(key), HttpStatus.OK);
//    }
//
//    /**
//     * This Java function uses a GET request to clear a token from a Redis cache based on a specified key.
//     *
//     * @param key The `key` parameter in the `clearTokenByKey` method is a path variable that represents the key used to
//     * clear a specific token from the Redis cache. This key is passed in the URL path when making a request to this
//     * endpoint.
//     * @return The method `clearTokenByKey` is being called with the `key` parameter, and the result is being returned as a
//     * `ResponseEntity` with a `BaseResponse` object and an HTTP status of OK (200).
//     */
//    @GetMapping(EndPointReferrer.CLEAR_REDIS_CACHE)
//    public ResponseEntity<BaseResponse> clearTokenByKey(@PathVariable("sKey") @NotNull @Valid String key) {
//        return new ResponseEntity<>(homeManager.clearTokenByKey(key), HttpStatus.OK);
//    }

}
