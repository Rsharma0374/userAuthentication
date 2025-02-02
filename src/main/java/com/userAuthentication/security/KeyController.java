package com.userAuthentication.security;

import com.userAuthentication.config.CacheConfig;
import com.userAuthentication.request.EncryptedPayload;
import com.userAuthentication.response.BaseResponse;
import com.userAuthentication.service.redis.RedisService;
import com.userAuthentication.utility.ResponseUtility;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class KeyController {

//    private final SecretKey secretKey;
//    private final IvParameterSpec iv;
//
//    public KeyController() throws Exception {
//        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
//        keyGen.init(256);
//        this.secretKey = keyGen.generateKey();
//
//        CacheConfig.CACHE.put("secretKey", this.secretKey);
//
//        // Generate a 16-byte IV
//        byte[] ivBytes = new byte[16];
//        SecureRandom secureRandom = new SecureRandom();
//        secureRandom.nextBytes(ivBytes);
//        this.iv = new IvParameterSpec(ivBytes);
//    }
//
//    @GetMapping("/key")
//    public ResponseEntity<BaseResponse> getEncryptionKey() {
//        // Prepend the IV to the secret key
//        byte[] ivBytes = iv.getIV();
//        byte[] keyBytes = secretKey.getEncoded();
//        byte[] combined = new byte[ivBytes.length + keyBytes.length];
//
//        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
//        System.arraycopy(keyBytes, 0, combined, ivBytes.length, keyBytes.length);
//
//        // Encode the combined array as Base64
//        String encodedKey = Base64.getEncoder().encodeToString(combined);
//        return new ResponseEntity<>(ResponseUtility.getBaseResponse(HttpStatus.OK, encodedKey), HttpStatus.OK);
//
//    }
//
//    @PostMapping("/data")
//    public ResponseEntity<String> receiveEncryptedData(@RequestBody EncryptedPayload payload) throws Exception {
//        String decryptedData = AESUtil.decrypt(payload.getEncryptedPayload());
//        return ResponseEntity.ok("Data received: " + decryptedData);
//    }

    @Autowired
    private RedisService redisService;

    @Autowired
    private ResponseUtility responseUtility;

    @GetMapping("/key")
    public ResponseEntity<BaseResponse> getEncryptionKey() throws Exception {
        String encodedKey = AESUtil.generateKey();
        UUID uuid = UUID.randomUUID();
        Map<String, String> map = new HashMap<>();
        map.put("sKey", encodedKey);
        map.put("sId", uuid.toString());
        redisService.setValueInRedisWithExpiration(uuid.toString(),encodedKey, (60 * 60), TimeUnit.SECONDS);
        return new ResponseEntity<>(responseUtility.getBaseResponse(HttpStatus.OK, map), HttpStatus.OK);
    }

//    @PostMapping("/encrypt")
//    public ResponseEntity<String> encryptData(@RequestBody String plainText) throws Exception {
//        return ResponseEntity.ok(AESUtil.encrypt(plainText));
//    }
//
//    @PostMapping("/decrypt")
//    public ResponseEntity<String> decryptData(@RequestBody String encryptedText) throws Exception {
//        return ResponseEntity.ok(AESUtil.decrypt(encryptedText));
//    }
}
