package com.users.UsuariosYEmpleados.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
        @org.springframework.beans.factory.annotation.Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable()) // Desactivar CSRF para APIs sin estado
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                org.springframework.security.config.http.SessionCreationPolicy.STATELESS)) // Sin sesiones
                                .authorizeHttpRequests(auth -> auth
                                                // Rutas públicas
                                                .requestMatchers("/api/auth/**").permitAll() // Login/registro/logout sin autenticación
                                                
                                                // Rutas específicas de empleados - solo administrador
                                                .requestMatchers("/api/empleados").hasRole("administrador") // GET all, POST create
                                                .requestMatchers("/api/empleados/{id}").hasRole("administrador") // GET by id, PUT, DELETE
                                                .requestMatchers("/api/empleados/completo").hasRole("administrador") // POST completo
                                                .requestMatchers("/api/empleados/cargo/**").hasRole("administrador") // GET by cargo
                                                .requestMatchers("/api/empleados/buscar").hasRole("administrador") // GET buscar
                                                .requestMatchers("/api/empleados/verificar-documento/**").hasRole("administrador") // GET verificar
                                                
                                                // Ruta que permite administrador y empleado consultar su propio documento
                                                .requestMatchers("/api/empleados/documento/**").hasAnyRole("administrador", "empleado")
                                                
                                                // Cualquier otra ruta requiere autenticación (sin rol específico)
                                                .anyRequest().authenticated()
                                )
                                .addFilterBefore(jwtAuthenticationFilter,
                                                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
