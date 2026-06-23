package com.workfootprint.security;

import com.workfootprint.config.WorkFootprintProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final WorkFootprintProperties properties;

    public JwtService(WorkFootprintProperties properties) {
        this.properties = properties;
    }

    public String createToken(long userId, String username) {
        SecretKey key = getKey();
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(properties.getAuth().getJwtExpireSeconds());
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        SecretKey key = getKey();
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    private SecretKey getKey() {
        String secret = properties.getAuth().getJwtSecret();
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}

