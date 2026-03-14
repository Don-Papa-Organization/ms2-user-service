package com.users.UsuariosYEmpleados.domain.repositories;

import com.users.UsuariosYEmpleados.domain.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT c FROM Cliente c, Usuario u " +
            "WHERE c.idUsuario = u.idUsuario " +
            "AND (:searchTerm IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(c.direccion) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(COALESCE(u.correo, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:activo IS NULL OR u.activo = :activo)")
    List<Cliente> search(@Param("searchTerm") String searchTerm, @Param("activo") Boolean activo);
}