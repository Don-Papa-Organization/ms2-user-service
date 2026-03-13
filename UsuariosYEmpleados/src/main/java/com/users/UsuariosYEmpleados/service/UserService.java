package com.users.UsuariosYEmpleados.service;

import com.users.UsuariosYEmpleados.domain.entity.Usuario;
import com.users.UsuariosYEmpleados.domain.repositories.UsuarioRepository;
import com.users.UsuariosYEmpleados.domain.dto.UserDTO;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;
import com.users.UsuariosYEmpleados.util.BCryptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Pattern EMAIL_REGEX = Pattern
            .compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PASSWORD_REGEX = Pattern
            .compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&.]{8,}$");

    private final UsuarioRepository usuarioRepository;
    private final BCryptUtils bCryptUtils;

    public UserService(UsuarioRepository usuarioRepository, BCryptUtils bCryptUtils) {
        this.usuarioRepository = usuarioRepository;
        this.bCryptUtils = bCryptUtils;
    }

    // ========== MÉTODOS DE NEGOCIO ==========

    /**
     * Registra un nuevo usuario con validaciones completas
     */
    public UserDTO register(String correo, String contrasena, TipoUsuario tipoUsuario) {
        // Validaciones de entrada
        validateEmail(correo);
        validatePassword(contrasena);
        
        if (existsByCorreo(correo)) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        // Hash de contraseña
        String hashedPassword = bCryptUtils.hashPassword(contrasena);

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setContrasena(hashedPassword);
        usuario.setTipoUsuario(tipoUsuario != null ? tipoUsuario : TipoUsuario.cliente);
        usuario.setActivo(false); // Requiere verificación de email

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return convertToDTO(usuarioGuardado);
    }

    /**
     * Valida credenciales de login
     */
    public UserDTO validateLogin(String correo, String contrasena) {
        validateEmail(correo);
        
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es requerida");
        }

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos"));

        if (!bCryptUtils.comparePassword(contrasena, usuario.getContrasena())) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }

        if(usuario.getActivo() == false){
            throw new IllegalArgumentException("El usuario no está verificado");
        }

        return convertToDTO(usuario);
    }

    /**
     * Activa un usuario
     */
    public void activateUser(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + idUsuario));
        
        if (usuario.getActivo()) {
            throw new IllegalArgumentException("El usuario ya está verificado");
        }
        
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    /**
     * Cambia la contraseña de un usuario validando la contraseña actual
     */
    public void changePassword(Integer idUsuario, String contrasenaActual, String nuevaContrasena,
            String confirmarContrasena) {
        if (contrasenaActual == null || contrasenaActual.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña actual es requerida");
        }

        if (nuevaContrasena == null || nuevaContrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La nueva contraseña es requerida");
        }

        if (confirmarContrasena == null || confirmarContrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La confirmación de contraseña es requerida");
        }

        if (!nuevaContrasena.equals(confirmarContrasena)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + idUsuario));

        if (!bCryptUtils.comparePassword(contrasenaActual, usuario.getContrasena())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        if (bCryptUtils.comparePassword(nuevaContrasena, usuario.getContrasena())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la anterior");
        }

        validatePassword(nuevaContrasena);

        usuario.setContrasena(bCryptUtils.hashPassword(nuevaContrasena));
        usuarioRepository.save(usuario);
    }

    /**
     * Restablece la contraseña de un usuario sin validar la contraseña actual
     */
    public void resetPassword(Integer idUsuario, String nuevaContrasena, String confirmarContrasena) {
        if (nuevaContrasena == null || nuevaContrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La nueva contraseña es requerida");
        }

        if (confirmarContrasena == null || confirmarContrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La confirmación de contraseña es requerida");
        }

        if (!nuevaContrasena.equals(confirmarContrasena)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + idUsuario));

        if (bCryptUtils.comparePassword(nuevaContrasena, usuario.getContrasena())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la anterior");
        }

        validatePassword(nuevaContrasena);

        usuario.setContrasena(bCryptUtils.hashPassword(nuevaContrasena));
        usuarioRepository.save(usuario);
    }

    // ========== VALIDACIONES PRIVADAS ==========

    private void validateEmail(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo es requerido");
        }
        if (!EMAIL_REGEX.matcher(correo).matches()) {
            throw new IllegalArgumentException("Formato de correo inválido");
        }
    }

    private void validatePassword(String contrasena) {
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es requerida");
        }
        if (!PASSWORD_REGEX.matcher(contrasena).matches()) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número");
        }
    }

    // ========== CONSULTAS ==========

    public List<UserDTO> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO findById(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        return convertToDTO(usuario);
    }

    public UserDTO findByCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con correo: " + correo));
        return convertToDTO(usuario);
    }

    public boolean existsByCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    public List<UserDTO> findByTipoUsuario(TipoUsuario tipoUsuario) {
        return usuarioRepository.findByTipoUsuario(tipoUsuario)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findByActivo(Boolean activo) {
        return usuarioRepository.findByActivo(activo)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO create(UserDTO userDTO) {
        // Validar que no exista duplicado
        if (userDTO.getCorreo() != null && usuarioRepository.existsByCorreo(userDTO.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + userDTO.getCorreo());
        }

        Usuario usuario = new Usuario();
        usuario.setCorreo(userDTO.getCorreo());
        usuario.setContrasena(userDTO.getContrasena());
        usuario.setTipoUsuario(userDTO.getTipoUsuario() != null ? userDTO.getTipoUsuario() : TipoUsuario.cliente);
        usuario.setActivo(userDTO.getActivo() != null ? userDTO.getActivo() : false);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return convertToDTO(usuarioGuardado);
    }

    public UserDTO update(Integer id, UserDTO userDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));

        // Validar cambio de correo
        if (userDTO.getCorreo() != null && 
            !userDTO.getCorreo().equals(usuario.getCorreo()) &&
            usuarioRepository.existsByCorreo(userDTO.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está en uso por otro usuario");
        }

        if (userDTO.getCorreo() != null) usuario.setCorreo(userDTO.getCorreo());
        if (userDTO.getContrasena() != null) usuario.setContrasena(userDTO.getContrasena());
        if (userDTO.getTipoUsuario() != null) usuario.setTipoUsuario(userDTO.getTipoUsuario());
        if (userDTO.getActivo() != null) usuario.setActivo(userDTO.getActivo());

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return convertToDTO(usuarioActualizado);
    }

    public void delete(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private UserDTO convertToDTO(Usuario usuario) {
        // No exponer hash de contraseña en DTO de lectura
        return new UserDTO(
                usuario.getIdUsuario(),
                usuario.getCorreo(),
                null,
                usuario.getTipoUsuario(),
                usuario.getActivo()
        );
    }
}
