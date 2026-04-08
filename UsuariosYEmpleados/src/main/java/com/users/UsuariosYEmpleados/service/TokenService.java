package com.users.UsuariosYEmpleados.service;

import com.users.UsuariosYEmpleados.domain.entity.ManejadorTokens;
import com.users.UsuariosYEmpleados.domain.entity.Usuario;
import com.users.UsuariosYEmpleados.domain.repositories.ManejadorTokensRepository;
import com.users.UsuariosYEmpleados.domain.repositories.UsuarioRepository;
import com.users.UsuariosYEmpleados.domain.dto.TokenDriverDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TokenService {
    
    private final ManejadorTokensRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;

    public TokenService(ManejadorTokensRepository tokenRepository,
                        UsuarioRepository usuarioRepository) {
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
    }
    
    // ========== Métodos de Búsqueda ==========
    
    public List<TokenDriverDTO> findAll() {
        return tokenRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public TokenDriverDTO findById(Integer id) {
        ManejadorTokens token = tokenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Token no encontrado con id: " + id));
        return convertToDTO(token);
    }
    
    public TokenDriverDTO findByToken(String token) {
        return convertToDTO(requireTokenByValue(token));
    }
    
    public List<TokenDriverDTO> findByUsuarioId(Integer idUsuario) {
        return tokenRepository.findByIdUsuario(idUsuario)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<TokenDriverDTO> findByUsuarioAndToken(Integer idUsuario, String token) {
        return tokenRepository.findAllByIdUsuarioAndToken(idUsuario, token)
            .stream()
            .max(Comparator.comparing(ManejadorTokens::getIdManejadorTokens))
            .map(this::convertToDTO);
    }
    
    // ========== Métodos CRUD ==========
    
    public TokenDriverDTO createToken(Integer idUsuario, int diasExpiracion) {
        return createToken(idUsuario, diasExpiracion, generateToken());
    }

    public TokenDriverDTO createToken(Integer idUsuario, int diasExpiracion, String tokenValue) {
        // Verificar que el usuario existe
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new RuntimeException("Usuario no encontrado con id: " + idUsuario);
        }

        // Evitar duplicados exactos (mismo token para mismo usuario)
        tokenRepository.deleteByIdUsuarioAndToken(idUsuario, tokenValue);
        
        // Crear nuevo token
        ManejadorTokens token = new ManejadorTokens();
        token.setToken(tokenValue);
        token.setCreadoEn(new Date());
        
        // Calcular fecha de expiraci\u00f3n
        Date expiraEn = calculateExpirationDate(diasExpiracion);
        token.setExpiraEn(expiraEn);
        
        token.setIdUsuario(idUsuario);
        
        ManejadorTokens savedToken = tokenRepository.save(token);
        return convertToDTO(savedToken);
    }
    
    public TokenDriverDTO createTokenForUsuario(Usuario usuario, int diasExpiracion) {
        ManejadorTokens token = new ManejadorTokens();
        token.setToken(generateToken());
        token.setCreadoEn(new Date());
        token.setExpiraEn(calculateExpirationDate(diasExpiracion));
        token.setIdUsuario(usuario.getIdUsuario());
        
        ManejadorTokens savedToken = tokenRepository.save(token);
        return convertToDTO(savedToken);
    }
    
    public void deleteToken(Integer id) {
        if (!tokenRepository.existsById(id)) {
            throw new RuntimeException("Token no encontrado con id: " + id);
        }
        tokenRepository.deleteById(id);
    }
    
    public void deleteByToken(String token) {
        tokenRepository.deleteByToken(token);
    }
    
    public void deleteByUsuario(Integer idUsuario) {
        List<ManejadorTokens> tokens = tokenRepository.findByIdUsuario(idUsuario);
        tokenRepository.deleteAll(tokens);
    }
    
    public void deleteExpiredTokens() {
        List<ManejadorTokens> tokens = tokenRepository.findAll();
        Date now = new Date();
        
        List<ManejadorTokens> expiredTokens = tokens.stream()
                .filter(token -> token.getExpiraEn() != null && token.getExpiraEn().before(now))
                .collect(Collectors.toList());
        
        if (!expiredTokens.isEmpty()) {
            tokenRepository.deleteAll(expiredTokens);
        }
    }
    
    // ========== Métodos de Validación ==========
    
    public boolean isValidToken(String token) {
        Optional<ManejadorTokens> tokenOpt = tokenRepository.findAllByToken(token)
                .stream()
                .max(Comparator.comparing(ManejadorTokens::getIdManejadorTokens));

        if (tokenOpt.isEmpty()) {
            return false;
        }

        ManejadorTokens tokenEntity = tokenOpt.get();
        Date now = new Date();

        return tokenEntity.getExpiraEn() == null ||
               tokenEntity.getExpiraEn().after(now);
    }
    
    public boolean isValidTokenForUsuario(String token, Integer idUsuario) {
        Optional<ManejadorTokens> tokenOpt = tokenRepository
            .findAllByIdUsuarioAndToken(idUsuario, token)
            .stream()
            .max(Comparator.comparing(ManejadorTokens::getIdManejadorTokens));
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        ManejadorTokens tokenEntity = tokenOpt.get();
        Date now = new Date();
        
        return tokenEntity.getExpiraEn() == null || 
               tokenEntity.getExpiraEn().after(now);
    }
    
    public TokenDriverDTO refreshToken(String oldToken, int diasExpiracion) {
        // Buscar el token antiguo
        ManejadorTokens oldTokenEntity = requireTokenByValue(oldToken);
        
        // Crear nuevo token con la misma información de usuario
        ManejadorTokens newToken = new ManejadorTokens();
        newToken.setToken(generateToken());
        newToken.setCreadoEn(new Date());
        newToken.setExpiraEn(calculateExpirationDate(diasExpiracion));
        newToken.setIdUsuario(oldTokenEntity.getIdUsuario());
        
        // Eliminar el token antiguo
        tokenRepository.delete(oldTokenEntity);
        
        // Guardar el nuevo
        ManejadorTokens savedToken = tokenRepository.save(newToken);
        return convertToDTO(savedToken);
    }
    
    public TokenDriverDTO extendToken(String token, int diasAdicionales) {
        ManejadorTokens tokenEntity = requireTokenByValue(token);
        
        // Calcular nueva fecha de expiración
        Date nuevaExpiracion;
        if (tokenEntity.getExpiraEn() == null) {
            nuevaExpiracion = calculateExpirationDate(diasAdicionales);
        } else {
            long tiempoActual = tokenEntity.getExpiraEn().getTime();
            long milisegundosAdicionales = diasAdicionales * 24L * 60 * 60 * 1000;
            nuevaExpiracion = new Date(tiempoActual + milisegundosAdicionales);
        }
        
        tokenEntity.setExpiraEn(nuevaExpiracion);
        ManejadorTokens updatedToken = tokenRepository.save(tokenEntity);
        return convertToDTO(updatedToken);
    }
    
    // ========== Métodos de Utilidad ==========
    
    private String generateToken() {
        // Generar un token UUID único
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    private Date calculateExpirationDate(int dias) {
        long tiempoActual = System.currentTimeMillis();
        long milisegundos = dias * 24L * 60 * 60 * 1000;
        return new Date(tiempoActual + milisegundos);
    }
    
    public long countTokensByUsuario(Integer idUsuario) {
        return tokenRepository.findByIdUsuario(idUsuario).size();
    }
    
    public List<TokenDriverDTO> getActiveTokensByUsuario(Integer idUsuario) {
        Date now = new Date();
        
        return tokenRepository.findByIdUsuario(idUsuario)
                .stream()
                .filter(token -> token.getExpiraEn() == null || 
                                token.getExpiraEn().after(now))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // ========== Métodos de Conversión ==========
    
    private TokenDriverDTO convertToDTO(ManejadorTokens token) {
        return new TokenDriverDTO(
            token.getIdManejadorTokens(),
            token.getCreadoEn(),
            token.getExpiraEn(),
            token.getToken(),
            token.getIdUsuario()
        );
    }
    
    private ManejadorTokens requireTokenByValue(String tokenValue) {
        return tokenRepository.findAllByToken(tokenValue)
            .stream()
            .max(Comparator.comparing(ManejadorTokens::getIdManejadorTokens))
                .orElseThrow(() -> new RuntimeException("Token no encontrado"));
    }
}