package com.users.UsuariosYEmpleados.domain.repositories;

import com.users.UsuariosYEmpleados.domain.entity.ManejadorTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManejadorTokensRepository extends JpaRepository<ManejadorTokens, Integer> {
    
    // Métodos básicos proporcionados por JpaRepository
    
    // Métodos custom esenciales
    List<ManejadorTokens> findAllByToken(String token);
    List<ManejadorTokens> findByIdUsuario(Integer idUsuario);
    List<ManejadorTokens> findAllByIdUsuarioAndToken(Integer idUsuario, String token);
    void deleteByToken(String token);
    void deleteByIdUsuarioAndToken(Integer idUsuario, String token);
}