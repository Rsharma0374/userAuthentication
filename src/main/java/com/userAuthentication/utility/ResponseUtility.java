package com.userAuthentication.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.userAuthentication.constant.ErrorCodes;
import com.userAuthentication.response.*;
import com.userAuthentication.response.Error;
import com.userAuthentication.security.AESUtil;
import com.userAuthentication.service.redis.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class ResponseUtility {

    private static final Logger logger = LoggerFactory.getLogger(ResponseUtility.class);
    public static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static final String NUMBERS = "0123456789";

    private static ObjectMapper mapper = new ObjectMapper().registerModule(new JodaModule());

    @Autowired
    private RedisService redisService;

    public BaseResponse getBaseResponse(HttpStatus httpStatus, Object buzResponse) {
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

    public String encryptThisString(String input) {


        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called to calculate message digest of the input string returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            StringBuilder hashtext = new StringBuilder(no.toString(16));

            // Add preceding 0s to make it 40 bit
            while (hashtext.length() < 40) {
                hashtext.insert(0, "0");
            }

            // return the HashText
            return hashtext.toString();
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            logger.error("Exception occurred at sha conversion due to - ", e);
            throw new RuntimeException(e);
        }
    }

    public String generateOtpAgainstLength(int length) {
        // Using numeric values
        String numbers = NUMBERS;

        // Using random method
        Random rndm_method = new Random();

        StringBuilder  otp=new StringBuilder();

        for (int i = 0; i < length; i++) {
            otp.append(numbers.charAt(rndm_method.nextInt(numbers.length())));
        }
        return otp.toString();
    }


    public String generateStringAgainstLength(int length) {
        // Using numeric values
        String numbers = ALPHANUMERIC;

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

    public Object redisObject (String username, String token, long expirationTime, Object obj) {
        Map<String, Object> redisClass = new HashMap<>();
        redisClass.put("username", username);
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

    public Map<String, Object> convertToMap(Object redisObj) {
        Map<String, Object> redisClass = new HashMap<>();
        Field[] fields = redisObj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                redisClass.put(field.getName(), field.get(redisObj));
            } catch (IllegalAccessException e) {
                logger.error("Exception occurred while converting object to map with probable cause -", e);
            }
        }
        return redisClass;
    }

    public static Properties fetchProperties(String userAuthPropertiesPath) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(userAuthPropertiesPath));
            return properties;
        } catch (IOException e) {
            logger.error("Exception occurred while getting user auth config with probable cause - ", e);
            return null;
        }
    }

    public BaseResponse getBaseResponse(HttpStatus httpStatus, Collection<Error> errors) {
        return BaseResponse.builder()
                .status(
                        Status.builder()
                                .statusCode(httpStatus.value())
                                .statusValue(httpStatus.name()).build())
                .errors(errors)
                .build();
    }

    public Collection<Error> mandatoryConfigurationError() {
        Collection<Error> errors = new ArrayList<>();
        errors.add(Error.builder()
                        .message(ErrorCodes.MANDATORY_CONFIGURATION_NOT_FOUND_FOR_THIS_SERVICE)
                        .errorCode(String.valueOf(Error.ERROR_TYPE.SYSTEM.toCode()))
                        .errorType(Error.ERROR_TYPE.SYSTEM.name())
                        .level(Error.SEVERITY.HIGH.name())
                .build());

        return errors;
    }


}
