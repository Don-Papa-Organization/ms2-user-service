package com.users.UsuariosYEmpleados.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;

    public JwtUtils(
            @Value("${JWT_SECRET}") String jwtSecret,
            @Value("${JWT_REFRESH_SECRET}") String jwtRefreshSecret) {
        validateSecret(jwtSecret, "JWT_SECRET");
        validateSecret(jwtRefreshSecret, "JWT_REFRESH_SECRET");
        this.accessSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshSecretKey = Keys.hmacShaKeyFor(jwtRefreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Map<String, Object> payload) {
        return Jwts.builder()
                .setClaims(payload)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 minutos
                .signWith(accessSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Map<String, Object> payload) {
        return Jwts.builder()
                .setClaims(payload)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 días
                .signWith(refreshSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims verifyAccessToken(String token) {
        return Jwts.parser()
                .setSigningKey(accessSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims verifyRefreshToken(String token) {
        return Jwts.parser()
                .setSigningKey(refreshSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private void validateSecret(String secret, String propertyName) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(propertyName + " debe tener al menos 32 caracteres");
        }
    }
}