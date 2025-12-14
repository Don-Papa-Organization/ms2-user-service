package com.users.UsuariosYEmpleados;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger implements CommandLineRunner {

    @Value("${PORT:4002}")
    private String port;

    @Override
    public void run(String... args) {
        System.out.println("✅ Servidor corriendo en " + port + " - puedes consumir la API Usuarios y Empleados");
    }
}
