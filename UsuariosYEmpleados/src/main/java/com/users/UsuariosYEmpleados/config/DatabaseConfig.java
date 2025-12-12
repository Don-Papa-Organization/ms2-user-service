package com.users.UsuariosYEmpleados.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuración de base de datos simplificada
 * Spring Boot/Hibernate maneja automáticamente la creación de tablas
 * No es necesario crear la BD manualmente (Azure SQL Server ya existe)
 */
@Configuration
public class DatabaseConfig {

    @Value("${app.database.name:don_papa}")
    private String dbName;

    /**
     * Bean JdbcTemplate para operaciones JDBC si es necesario
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        System.out.println("✅ JdbcTemplate configurado para base de datos: " + dbName);
        return new JdbcTemplate(dataSource);
    }
}