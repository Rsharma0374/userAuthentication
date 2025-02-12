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

    @Autowired
    private RedisService redisService;

    @Autowired
    private ResponseUtility responseUtility;

//    @GetMapping("/key")
//    public ResponseEntity<BaseResponse> getEncryptionKey() throws Exception {
//        String encodedKey = AESUtil.generateKey();
//        UUID uuid = UUID.randomUUID();
//        Map<String, String> map = new HashMap<>();
//        map.put("sKey", encodedKey);
//        map.put("sId", uuid.toString());
//        redisService.setValueInRedisWithExpiration(uuid.toString(),encodedKey, (60 * 60), TimeUnit.SECONDS);
//        return new ResponseEntity<>(responseUtility.getBaseResponse(HttpStatus.OK, map), HttpStatus.OK);
//    }

}
