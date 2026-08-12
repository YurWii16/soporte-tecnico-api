package com.veterinary.users.controller;

import com.veterinary.users.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username, @RequestParam String password) {
        
        // AQUÍ: Para el prototipado rápido validamos las credenciales directamente.
        // (Más adelante puedes conectarlo con tu repositorio para buscar en MySQL)
        
        Map<String, String> response = new HashMap<>();

        if ("admin".equals(username) && "1234".equals(password)) {
            // Es un administrador, le damos su pulsera VIP
            String token = JwtUtil.generateToken(username, "ADMIN");
            response.put("token", token);
            return ResponseEntity.ok(response);
            
        } else if ("tecnico".equals(username) && "1234".equals(password)) {
            // Es un técnico, le damos su pulsera normal
            String token = JwtUtil.generateToken(username, "TECNICO");
            response.put("token", token);
            return ResponseEntity.ok(response);
            
        } else {
            // Credenciales incorrectas
            response.put("error", "Credenciales inválidas");
            return ResponseEntity.status(401).body(response);
        }
    }
}
