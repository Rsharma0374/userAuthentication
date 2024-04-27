package com.chat.userAuthentication.utility;

import com.chat.userAuthentication.response.BaseResponse;
import com.chat.userAuthentication.response.Payload;
import com.chat.userAuthentication.response.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ResponseUtility {

    private static final Logger logger = LoggerFactory.getLogger(ResponseUtility.class);

    private static ObjectMapper mapper = new ObjectMapper().registerModule(new JodaModule());

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

    public static String generateOtpAgainstLength(int length) {
        // Using numeric values
        String numbers = "0123456789";

        // Using random method
        Random rndm_method = new Random();

        StringBuilder  otp=new StringBuilder();

        for (int i = 0; i < length; i++) {
            otp.append(numbers.charAt(rndm_method.nextInt(numbers.length())));
        }
        return otp.toString();
    }


    public static String generateStringAgainstLength(int length) {
        // Using numeric values
        String numbers = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // Using random method
        Random rndm_method = new Random();

        StringBuilder  string = new StringBuilder();

        for (int i = 0; i < length; i++) {
            string.append(numbers.charAt(rndm_method.nextInt(numbers.length())));
        }
        return string.toString();
    }

    public static String ObjectToString(Object object) throws JsonProcessingException {
        // mapper.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS,false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.writeValueAsString(object);
    }

    public static <T> T StringToObject(String jsonString, Class<?> type) throws IOException {
        return (T)mapper.readValue(jsonString, type);

    }

    public static Object redisObject (String key, String token, long expirationTime, Object obj) {
        Map<String, Object> redisClass = new HashMap<>();
        redisClass.put("key", key);
        redisClass.put("token", token);
        redisClass.put("otherValue", obj);

        // Get current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();

        // Add 1800 seconds (30 minutes) to the current time
        long futureTimeMillis = currentTimeMillis + TimeUnit.SECONDS.toMillis(1800);

        // Convert the future time to Date object
        Date futureDate = new Date(futureTimeMillis);

        // Define the date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        // Format the future time string
        String futureTimeString = dateFormat.format(futureDate);

        redisClass.put("expireTime", futureTimeString);

        return redisClass;
    }
}
