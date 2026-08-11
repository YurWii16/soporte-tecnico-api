package com.veterinary.users.controller;

import com.veterinary.users.model.Rol;
import com.veterinary.users.model.Usuario;
import com.veterinary.users.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username, @RequestParam String password) {
        Map<String, String> response = new HashMap<>();
        try {
            String token = authService.login(username, password);
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String rol) {

        Map<String, String> response = new HashMap<>();
        try {
            Rol rolEnum = (rol != null) ? Rol.valueOf(rol.toUpperCase()) : Rol.CLIENTE;
            Usuario usuario = authService.registrar(username, password, rolEnum);
            response.put("mensaje", "Usuario registrado correctamente");
            response.put("username", usuario.getUsername());
            response.put("rol", usuario.getRol().name());
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            response.put("error", "Rol inválido. Usa ADMIN, TECNICO o CLIENTE");
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}