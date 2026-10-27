package com.devAssist.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${devassist.jwt.secret}") String secret,
                      @Value("${devassist.jwt.expiration-minutes:60}") long expirationMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("DEVASSIST_JWT_SECRET must be at least 32 bytes");
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMinutes = expirationMinutes;
    }

    public long expirationSeconds() { return expirationMinutes * 60; }

    public String generate(String email) {
        Instant now = Instant.now();
        return Jwts.builder().subject(email).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(key).compact();
    }

    public String extractEmail(String token) { return claims(token).getSubject(); }

    public boolean isValid(String token) {
        try { return claims(token).getExpiration().after(new Date()); }
        catch (RuntimeException ex) { return false; }
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
