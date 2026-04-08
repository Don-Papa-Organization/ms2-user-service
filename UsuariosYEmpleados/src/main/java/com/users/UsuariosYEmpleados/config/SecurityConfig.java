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
                                                .requestMatchers("/api/empleados").hasRole("ADMINISTRADOR") // GET all, POST create
                                                .requestMatchers("/api/empleados/{id}").hasRole("ADMINISTRADOR") // GET by id, PUT, DELETE
                                                .requestMatchers("/api/empleados/completo").hasRole("ADMINISTRADOR") // POST completo
                                                .requestMatchers("/api/empleados/cargo/**").hasRole("ADMINISTRADOR") // GET by cargo
                                                .requestMatchers("/api/empleados/buscar").hasRole("ADMINISTRADOR") // GET buscar
                                                .requestMatchers("/api/empleados/verificar-documento/**").hasRole("ADMINISTRADOR") // GET verificar
                                                
                                                // Rutas de gestión de usuarios y clientes - solo administrador
                                                .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers("/api/clientes/**").hasRole("ADMINISTRADOR")

                                                // Ruta que permite administrador y empleado consultar su propio documento
                                                .requestMatchers("/api/empleados/documento/**").hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                                                
                                                // Cualquier otra ruta requiere autenticación (sin rol específico)
                                                .anyRequest().authenticated()
                                )
                                .addFilterBefore(jwtAuthenticationFilter,
                                                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
