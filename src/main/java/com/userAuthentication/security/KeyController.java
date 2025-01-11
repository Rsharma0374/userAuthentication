package com.userAuthentication.security;

import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.utility.ResponseUtility;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class KeyController {

    private final SecretKey secretKey;
    private final IvParameterSpec iv;

    public KeyController() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        this.secretKey = keyGen.generateKey();

        // Generate a 16-byte IV
        byte[] ivBytes = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(ivBytes);
        this.iv = new IvParameterSpec(ivBytes);
    }

    @GetMapping("/key")
    public ResponseEntity<BaseResponse> getEncryptionKey() {
        // Prepend the IV to the secret key
        byte[] ivBytes = iv.getIV();
        byte[] keyBytes = secretKey.getEncoded();
        byte[] combined = new byte[ivBytes.length + keyBytes.length];

        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
        System.arraycopy(keyBytes, 0, combined, ivBytes.length, keyBytes.length);

        // Encode the combined array as Base64
        String encodedKey = Base64.getEncoder().encodeToString(combined);
        return new ResponseEntity<>(ResponseUtility.getBaseResponse(HttpStatus.OK, encodedKey), HttpStatus.OK);

    }

    @PostMapping("/data")
    public ResponseEntity<String> receiveEncryptedData(@RequestBody EncryptedPayload payload) throws Exception {
        String decryptedData = decrypt(payload.getEncryptedPayload());
        return ResponseEntity.ok("Data received: " + decryptedData);
    }

    private String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    static class EncryptedPayload {
        private String encryptedPayload;

        public String getEncryptedPayload() {
            return encryptedPayload;
        }

        public void setEncryptedPayload(String encryptedPayload) {
            this.encryptedPayload = encryptedPayload;
        }
    }
}
