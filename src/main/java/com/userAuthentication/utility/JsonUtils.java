package com.userAuthentication.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
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
}
