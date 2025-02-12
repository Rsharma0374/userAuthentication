package com.userAuthentication.service;

import com.userAuthentication.service.redis.RedisService;
import com.userAuthentication.utility.ResponseUtility;
import com.userAuthentication.utility.TokenGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class JWTService {

    @Autowired
    private RedisService redisService;

    private static String secretKey = "";
    public static final String SECRET_KEY = "SECRET_KEY";
    private static final String USER_AUTH_PROPERTIES_PATH = "/opt/configs/userAuth.properties";

    static {
        Properties properties = ResponseUtility.fetchProperties(USER_AUTH_PROPERTIES_PATH);
        if (null != properties) {
            secretKey = properties.getProperty(SECRET_KEY);
        }
    }
//
//    public JWTService() {
//        try {
//            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
//            SecretKey key = keyGenerator.generateKey();
//            secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }

    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        Date expiryTime = new Date(System.currentTimeMillis() + 60 * 60 * 1000);
        String jwtToken = Jwts.builder()
                .claims()
                .add(claims)
                .subject(userName)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiryTime)
                .and()
                .signWith(getKey())
                .compact();
        String opaqueToken = TokenGenerator.generateHexString(40);

        redisService.setValueInRedisWithExpiration(opaqueToken, jwtToken, (60 * 60), TimeUnit.SECONDS);
        return opaqueToken;
    }

    private SecretKey getKey() {
        byte[] keyByte = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyByte);
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, String requestUsername) {
        final String username = extractUserName(token);
        return (username.equals(requestUsername) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
