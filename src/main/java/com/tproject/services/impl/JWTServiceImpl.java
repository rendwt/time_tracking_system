package com.tproject.services.impl;

import com.tproject.entity.Credentials;
import io.jsonwebtoken.*;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

public class JWTServiceImpl {

    private final String KEY = "aW50ZXJlc3R3b2xmaGlkZWhpZGRlbmhlYWRlZHBhbGU=";
    private static volatile JWTServiceImpl instance;

    public static JWTServiceImpl getInstance() {
        JWTServiceImpl localInstance = instance;
        if (localInstance == null) {
            synchronized (JWTServiceImpl.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new JWTServiceImpl();
                }
            }
        }
        return localInstance;
    }

    private Key hmacKey = new SecretKeySpec(Base64.getDecoder().decode(KEY),
            SignatureAlgorithm.HS256.getJcaName());


    public String buildUserToken(Credentials user){

        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime()
                + 60    //duration, min
                * 60000);  //ms multiplier
        String result = Jwts.builder()
                .setIssuer("GP01")
                .claim("user", user.getUsername())  //set user
                .claim("role", user.getRole())      //set role
                .setIssuedAt(createdDate)              //set token creation date
                .setExpiration(expirationDate)         //set token expiration date
                .signWith(hmacKey)
                .compact();
        System.out.println("CREATED TOKEN==============="+result);
        return result;
    }

    public Jws<Claims> verifyUserToken(String jwsString) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(hmacKey)
                .build()
                .parseClaimsJws(jwsString);
    }
}
