package com.users.UsuariosYEmpleados.domain.repositories;

import com.users.UsuariosYEmpleados.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    
    // Métodos básicos proporcionados por JpaRepository:
    // - save(), findById(), findAll(), deleteById(), etc.
    
    // Métodos custom esenciales
    Optional<Cliente> findByTelefono(String telefono);
    Optional<Cliente> findByIdUsuario(Integer idUsuario);
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
    boolean existsByTelefono(String telefono);
}