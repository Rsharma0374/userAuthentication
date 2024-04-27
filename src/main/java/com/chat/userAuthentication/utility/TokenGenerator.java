package com.chat.userAuthentication.utility;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String generateToken(String userName) {
        String uniqueString = userName + System.currentTimeMillis() + random.nextLong();
        byte[] bytes = uniqueString.getBytes();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
