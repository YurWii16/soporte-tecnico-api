package com.veterinary.users.service;

import com.veterinary.users.model.Rol;
import com.veterinary.users.model.Usuario;
import com.veterinary.users.repository.UsuarioRepository;
import com.veterinary.users.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public String login(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return JwtUtil.generateToken(usuario.getUsername(), usuario.getRol().name());
    }

    public Usuario registrar(String username, String password, Rol rol) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new RuntimeException("El usuario ya existe");
        }

        Usuario usuario = Usuario.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .rol(rol != null ? rol : Rol.CLIENTE)
                .build();

        return usuarioRepository.save(usuario);
    }

    public java.util.List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario actualizar(String username, String password, Rol rol) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (password != null && !password.trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }
        
        if (rol != null) {
            usuario.setRol(rol);
        }
        
        return usuarioRepository.save(usuario);
    }

    public void eliminar(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if ("admin".equals(username)) {
            throw new RuntimeException("No se puede eliminar al administrador principal");
        }
        
        usuarioRepository.delete(usuario);
    }
}