package com.userAuthentication.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userAuthentication.request.EmailOtpRequest;
import com.userAuthentication.response.BaseResponse;
import lombok.Data;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@ToString
public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON to " + clazz.getSimpleName(), e);
        }
    }

    public static <T> T convertToType(Object object, Class<T> clazz) {
        return objectMapper.convertValue(object, clazz);
    }

    public static String toString(BaseResponse baseResponse) {

        try {
            return objectMapper.writeValueAsString(baseResponse);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing String with probable cause ", e);
        }
    }

    public static String objectToString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing String with probable cause ", e);
        }
    }
}
