package com.userAuthentication.security;

import com.userAuthentication.config.CacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class EncryptDecryptService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptDecryptService.class);
    private static final String SECRET_KEY = "SECRET_KEY";

    public static String encryptText(String password) throws Exception {
        String secretKey = (String) CacheConfig.CACHE.get(SECRET_KEY);
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        byte[] iv = generateRandomIV();
        // Trim or pad to 32 bytes (AES-256)
        keyBytes = Arrays.copyOf(keyBytes, 32);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] encryptedPassword = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + encryptedPassword.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedPassword, 0, combined, iv.length, encryptedPassword.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    private static byte[] generateRandomIV() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        return iv;
    }

    public static String decryptedTextOrReturnSame(String encryptedText) {
        try {
            String secretKey = (String) CacheConfig.CACHE.get(SECRET_KEY);
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            keyBytes = Arrays.copyOf(keyBytes, 32);


            // Base64 decode
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // Extract IV and encrypted password bytes
            byte[] iv = new byte[16];
            byte[] encryptedPasswordBytes = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encryptedPasswordBytes, 0, encryptedPasswordBytes.length);

            // Create secret key and cipher
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

            // Decrypt
            byte[] decryptedPasswordBytes = cipher.doFinal(encryptedPasswordBytes);
            return new String(decryptedPasswordBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Something went wrong decrypting password", e);
            return encryptedText;
        }
    }

    public static String decryptPayload(String encryptedPayload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, (Key) CacheConfig.CACHE.get("secretKey"));
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedPayload);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    public static String hashPassword(String password) {
        int workFactor = 10;
        String salt = BCrypt.gensalt(workFactor);
        return BCrypt.hashpw(password, salt);
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

}
