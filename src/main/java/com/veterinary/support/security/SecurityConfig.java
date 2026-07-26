package com.veterinary.support.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar @PreAuthorize en los controladores
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Permitimos acceso libre a la consola H2 y a Swagger (Documentación)
                .requestMatchers("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Cualquier otra petición (como crear o editar solicitudes) requerirá token
                .anyRequest().authenticated()
            );
        
        // Habilitar los frames de H2 console
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        // Insertamos nuestro filtro antes del filtro por defecto de Spring
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
