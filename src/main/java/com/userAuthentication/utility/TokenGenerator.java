package com.userAuthentication.utility;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String generateToken(String userName) {
        String uniqueString = userName + System.currentTimeMillis() + random.nextLong();
        byte[] bytes = uniqueString.getBytes();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String generateHexString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder hexString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            // Generate a random integer between 0 and 15 (0xF)
            int randomValue = random.nextInt(16);
            // Convert it to a hexadecimal character and append it to the string
            hexString.append(Integer.toHexString(randomValue));
        }

        return hexString.toString();
    }

}
