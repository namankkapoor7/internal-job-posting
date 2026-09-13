package com.example.adminservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET_KEY_STRING = "SuperSecretKeyForInternalJobPostingApplication2026SecureJwtTokenKey!";
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    private static final long EXPIRATION_TIME = 86400000;

    public String generateToken(String email, String role, String employeeId, Long userId, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("employeeId", employeeId);
        claims.put("userId", userId);
        claims.put("name", name);
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
