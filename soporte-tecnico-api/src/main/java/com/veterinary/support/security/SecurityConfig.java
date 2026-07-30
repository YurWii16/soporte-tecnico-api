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
                // Permitimos acceso libre a la consola H2, al endpoint de errores y a Swagger
                .requestMatchers(
                    "/h2-console/**",
                    "/error",                 // Permite ver los errores reales en lugar de un 403
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs",           // Ruta raíz del JSON de Swagger
                    "/v3/api-docs/**",        // Sub-recursos de Swagger
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/v1/auth/**",            // Permitir login sin token
                    "/api/v1/auth/**"         // Permitir login sin token (alternativa)
                ).permitAll()
                // Cualquier otra petición (como crear o editar solicitudes) requerirá token Bearer
                .anyRequest().authenticated()
            );
        
        // Habilitar los frames de H2 console
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        // Insertamos nuestro filtro antes del filtro por defecto de Spring
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
