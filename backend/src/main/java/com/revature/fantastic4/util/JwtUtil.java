package com.revature.fantastic4.util;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.revature.fantastic4.enums.Role;

import java.util.UUID;
import java.util.Date;

import javax.crypto.SecretKey;



@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey; 


    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());

    }

    public String generateAccessToken(UUID userId, String username, Role role){
        return Jwts.builder()
        .subject(userId.toString())
        .claim("username", username)
        .claim("role", role.name())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
        .signWith(getSecretKey(), Jwts.SIG.HS256)
        .compact();

    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); 
        }
        throw new IllegalArgumentException("Invalid Authorization header. Expected format: Bearer <token>");
    }

    public UUID extractId(String token){
        String userIdString = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return UUID.fromString(userIdString);
    }

    public String extractUsername(String token){
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("username", String.class);
    }

    public Role extractRole(String token){
        String roleString = Jwts.parser()
            .verifyWith(getSecretKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("role", String.class);
        return Role.valueOf(roleString);

    }

}
