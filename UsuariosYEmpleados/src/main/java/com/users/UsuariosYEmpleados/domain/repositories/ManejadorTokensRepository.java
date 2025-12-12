package com.users.UsuariosYEmpleados.domain.repositories;

import com.users.UsuariosYEmpleados.domain.entity.ManejadorTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManejadorTokensRepository extends JpaRepository<ManejadorTokens, Integer> {
    
    // Métodos básicos proporcionados por JpaRepository
    
    // Métodos custom esenciales
    Optional<ManejadorTokens> findByToken(String token);
    List<ManejadorTokens> findByIdUsuario(Integer idUsuario);
    Optional<ManejadorTokens> findByIdUsuarioAndToken(Integer idUsuario, String token);
}