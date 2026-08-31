package com.punish.Config;

import java.util.Date;
import java.util.Properties;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtConfig {
    private static String getSecret() {
        try {
            Properties props = Database.loadProperties();
            return props.getProperty("jwt.secret");
        } catch (Exception e) {
            throw new RuntimeException("jwt.secret não configurada");
        }
    }
    private static final long EXPIRATION = 86400000;

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(getSecret().getBytes());
    }

    public static String generateToken(Long userId, String role){
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(getKey())
            .compact();
    }

    public static Claims parseToken(String token){
        return Jwts.parser()
        .verifyWith(getKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
    }
}
