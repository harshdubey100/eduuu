package com.eduverse.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long jwtExpirationInMs = 3600000; // 1 Hour

    // 1. Generate token with email and role claims
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", "ROLE_" + role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey)
                .compact();
    }

    // Helper: Dry up code by extracting claims logic into a single method
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 2. Extract email subject from token
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    // 3. Extract role claim from token
    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    // 4. Validate token structure and expiration status
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
