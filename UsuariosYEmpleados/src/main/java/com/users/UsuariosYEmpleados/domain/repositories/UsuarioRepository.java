package com.users.UsuariosYEmpleados.domain.repositories;

import com.users.UsuariosYEmpleados.domain.entity.Usuario;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    // Métodos básicos proporcionados por JpaRepository
    
    // Métodos custom esenciales
    Optional<Usuario> findByCorreo(String correo);
    List<Usuario> findByActivo(Boolean activo);
    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);
    boolean existsByCorreo(String correo);

        @Query("SELECT u FROM Usuario u " +
            "WHERE (:tipoUsuario IS NULL OR u.tipoUsuario = :tipoUsuario) " +
            "AND (:activo IS NULL OR u.activo = :activo) " +
            "AND (:correo IS NULL OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :correo, '%')))")
        List<Usuario> search(
            @Param("tipoUsuario") TipoUsuario tipoUsuario,
            @Param("activo") Boolean activo,
            @Param("correo") String correo);
}