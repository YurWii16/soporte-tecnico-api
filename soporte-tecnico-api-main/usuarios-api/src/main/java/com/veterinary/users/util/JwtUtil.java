package com.veterinary.users.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtUtil {
    
    // Esta es tu firma secreta. (En la vida real se guarda en el application.properties)
    private static final String SECRET_WORD = "EstaEsUnaClaveSuperSecretaParaZegel2026Backend";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_WORD.getBytes());
    
    // El token durará 1 hora (3600000 milisegundos)
    private static final long EXPIRATION_TIME = 3600000; 

    // Método mágico que crea el Token
    public static String generateToken(String username, String rol) {
        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol) // Aquí guardamos si es ADMIN o TECNICO
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }
}
