package com.users.UsuariosYEmpleados.domain.repositories;

import com.users.UsuariosYEmpleados.domain.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT e FROM Empleado e, Usuario u " +
            "WHERE e.idUsuario = u.idUsuario " +
            "AND (:searchTerm IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(e.documento) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(COALESCE(u.correo, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:activo IS NULL OR u.activo = :activo)")
    List<Empleado> search(@Param("searchTerm") String searchTerm, @Param("activo") Boolean activo);
}