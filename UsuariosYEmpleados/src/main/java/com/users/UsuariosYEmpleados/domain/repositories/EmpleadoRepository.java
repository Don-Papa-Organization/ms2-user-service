package com.users.UsuariosYEmpleados.domain.repositories;

import com.users.UsuariosYEmpleados.domain.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    
    // Métodos básicos proporcionados por JpaRepository
    
    // Métodos custom esenciales
    Optional<Empleado> findByDocumento(String documento);
    Optional<Empleado> findByTelefono(String telefono);
    Optional<Empleado> findByIdUsuario(Integer idUsuario);
    List<Empleado> findByCargo(String cargo);
    List<Empleado> findByNombreContainingIgnoreCase(String nombre);
    boolean existsByDocumento(String documento);
}