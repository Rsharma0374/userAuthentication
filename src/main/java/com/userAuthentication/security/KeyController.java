package com.userAuthentication.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class KeyController {

    private final SecretKey secretKey;

    public KeyController() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        this.secretKey = keyGen.generateKey();
    }

    @GetMapping("/key")
    public ResponseEntity<String> getEncryptionKey() {
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        return ResponseEntity.ok(encodedKey);
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
