package com.users.UsuariosYEmpleados.service;

import com.users.UsuariosYEmpleados.domain.entity.Usuario;
import com.users.UsuariosYEmpleados.domain.repositories.UsuarioRepository;
import com.users.UsuariosYEmpleados.dto.UserDTO;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UsuarioRepository usuarioRepository;

    public UserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ========== Métodos de Búsqueda ==========

    public List<UserDTO> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO findById(Integer id) {
        return convertToDTO(requireUsuario(id));
    }

    public UserDTO findByCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con correo: " + correo));
        return convertToDTO(usuario);
    }

    /**
     * Método específico para login que incluye la contraseña hasheada
     * SOLO usar para validación de login, nunca exponer en respuestas al cliente
     */
    public UserDTO findByCorreoForLogin(String correo) {
        System.out.println("//////////////////////////// findByCorreoForLogin service");
        System.out.println(correo);
        System.out.println(correo);

        // DEBUG: List all users to see what's in the DB
        System.out.println("--- DUMPING ALL USERS IN DB ---");
        usuarioRepository.findAll().forEach(u -> System.out.println("User in DB: " + u.getCorreo()));
        System.out.println("-------------------------------");

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con correo: " + correo));
        System.out.println("//////////////////////////// findByCorreoForLogin service");
        System.out.println(usuario);
        return convertToDTOWithPassword(usuario);
    }

    public List<UserDTO> findByActivo(Boolean activo) {
        return usuarioRepository.findByActivo(activo)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findByTipoUsuario(TipoUsuario tipoUsuario) {
        return usuarioRepository.findByTipoUsuario(tipoUsuario)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========== Métodos de Verificación ==========

    public boolean existsByCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    public boolean isUsuarioActivo(Integer id) {
        return requireUsuario(id).getActivo();
    }

    // ========== Métodos CRUD ==========

    public UserDTO create(UserDTO userDTO) {
        // Verificar si el correo ya existe
        if (usuarioRepository.existsByCorreo(userDTO.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado: " + userDTO.getCorreo());
        }

        // Crear la entidad
        Usuario usuario = convertToEntity(userDTO);
        usuario.setActivo(true); // Por defecto activo al crear

        // Guardar
        Usuario savedUsuario = usuarioRepository.save(usuario);
        System.out.println("//////////////////////////// USER CREATED ////////////////////////////");
        System.out.println("Saved ID: " + savedUsuario.getIdUsuario());
        System.out.println("Saved Email: " + savedUsuario.getCorreo());
        return convertToDTO(savedUsuario);
    }

    public UserDTO update(Integer id, UserDTO userDTO) {
        // Verificar existencia
        Usuario existingUsuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        // Verificar que el nuevo correo no esté en uso por otro usuario
        if (userDTO.getCorreo() != null &&
                !userDTO.getCorreo().equals(existingUsuario.getCorreo()) &&
                usuarioRepository.existsByCorreo(userDTO.getCorreo())) {
            throw new RuntimeException("El correo ya está en uso por otro usuario");
        }

        // Actualizar campos
        if (userDTO.getCorreo() != null) {
            existingUsuario.setCorreo(userDTO.getCorreo());
        }
        if (userDTO.getContrasena() != null && !userDTO.getContrasena().isEmpty()) {
            existingUsuario.setContrasena(userDTO.getContrasena()); // ¡Encriptar en producción!
        }
        if (userDTO.getTipoUsuario() != null) {
            existingUsuario.setTipoUsuario(userDTO.getTipoUsuario());
        }
        if (userDTO.getActivo() != null) {
            existingUsuario.setActivo(userDTO.getActivo());
        }

        // Guardar cambios
        Usuario updatedUsuario = usuarioRepository.save(existingUsuario);
        return convertToDTO(updatedUsuario);
    }

    public void delete(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    public void activate(Integer id) {
        Usuario usuario = requireUsuario(id);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    public void deactivate(Integer id) {
        Usuario usuario = requireUsuario(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    // ========== Métodos de Conversión ==========

    private UserDTO convertToDTO(Usuario usuario) {
        // Nunca devolver la contraseña en DTOs de respuesta
        return new UserDTO(
                usuario.getIdUsuario(),
                usuario.getCorreo(),
                null,
                usuario.getTipoUsuario(),
                usuario.getActivo());
    }

    /**
     * Conversión a DTO incluyendo la contraseña hasheada
     * SOLO para uso interno en validación de login
     */
    private UserDTO convertToDTOWithPassword(Usuario usuario) {
        return new UserDTO(
                usuario.getIdUsuario(),
                usuario.getCorreo(),
                usuario.getContrasena(), // Incluir contraseña para validación
                usuario.getTipoUsuario(),
                usuario.getActivo());
    }

    private Usuario convertToEntity(UserDTO userDTO) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(userDTO.getCorreo());
        usuario.setContrasena(userDTO.getContrasena()); // ¡Encriptar antes de guardar!
        usuario.setTipoUsuario(userDTO.getTipoUsuario());
        usuario.setActivo(userDTO.getActivo() != null ? userDTO.getActivo() : true);
        return usuario;
    }

    private Usuario requireUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }
}