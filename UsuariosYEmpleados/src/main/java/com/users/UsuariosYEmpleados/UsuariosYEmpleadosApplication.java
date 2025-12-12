package com.users.UsuariosYEmpleados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.users.UsuariosYEmpleados.domain.entity")
@EnableJpaRepositories(basePackages = "com.users.UsuariosYEmpleados.domain.repositories")
public class UsuariosYEmpleadosApplication {
    
    public static void main(String[] args) {
        System.out.println("🚀 Iniciando aplicación UsuariosYEmpleados...");
        
        try {
            SpringApplication.run(UsuariosYEmpleadosApplication.class, args);
            System.out.println("✅ Aplicación iniciada correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}