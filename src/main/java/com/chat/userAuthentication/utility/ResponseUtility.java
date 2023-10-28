package com.chat.userAuthentication.utility;

import com.chat.userAuthentication.response.BaseResponse;
import com.chat.userAuthentication.response.Payload;
import com.chat.userAuthentication.response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

public class ResponseUtility {

    private static final Logger logger = LoggerFactory.getLogger(ResponseUtility.class);

    public static BaseResponse getBaseResponse(HttpStatus httpStatus, Object buzResponse) {
        logger.info("Inside getBaseResponse method");

        if (null == buzResponse)
            buzResponse = Collections.emptyMap();

        return BaseResponse.builder()
                .payload(new Payload<>(buzResponse))
                .status(
                        Status.builder()
                                .statusCode(httpStatus.value())
                                .statusValue(httpStatus.name()).build())
                .build();
    }

    public static String encryptThisString(String input) {


        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called to calculate message digest of the input string returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 40 bit
            while (hashtext.length() < 40) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            logger.error("Exception occurred at sha conversion due to - ", e);
            throw new RuntimeException(e);
        }

    }
}
