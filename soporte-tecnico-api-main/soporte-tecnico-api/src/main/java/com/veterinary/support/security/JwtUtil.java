package com.veterinary.support.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;

@Component
public class JwtUtil {
    // Debe ser EXACTAMENTE la misma clave que pusiste en el proyecto de usuarios
    private static final String SECRET_WORD = "EstaEsUnaClaveSuperSecretaParaZegel2026Backend";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_WORD.getBytes());

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
    }
    
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    
    public String extractRole(String token) {
        return extractAllClaims(token).get("rol", String.class);
    }
}
