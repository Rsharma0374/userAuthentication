package com.userAuthentication.config;
import com.userAuthentication.utility.ResponseUtility;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


@Configuration
public class CacheConfig {
    private static String secretKey = "";
    public static Map<String , Object> CACHE = new ConcurrentHashMap<String,Object>();
    private static final String USER_AUTH_PROPERTIES_PATH = "/opt/configs/userAuth.properties";
    private static final String SECRET_KEY = "SECRET_KEY";

    static {
        Properties properties = ResponseUtility.fetchProperties(USER_AUTH_PROPERTIES_PATH);
        if (null != properties) {
            secretKey = properties.getProperty(SECRET_KEY);
        }

        CACHE.put(SECRET_KEY, secretKey);
    }
}
