package com.veterinary.users.config;

import com.veterinary.users.model.Rol;
import com.veterinary.users.model.Usuario;
import com.veterinary.users.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        crearSiNoExiste("admin", "1234", Rol.ADMIN);
        crearSiNoExiste("tecnico", "1234", Rol.TECNICO);
        crearSiNoExiste("cliente", "1234", Rol.CLIENTE);
        crearSiNoExiste("luis", "1234", Rol.TECNICO);
    }

    private void crearSiNoExiste(String username, String password, Rol rol) {
        if (!usuarioRepository.existsByUsername(username)) {
            Usuario usuario = Usuario.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .rol(rol)
                    .build();
            usuarioRepository.save(usuario);
        }
    }
}