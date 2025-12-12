package com.users.UsuariosYEmpleados.domain.repositories;

import com.users.UsuariosYEmpleados.domain.entity.Usuario;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;
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
}