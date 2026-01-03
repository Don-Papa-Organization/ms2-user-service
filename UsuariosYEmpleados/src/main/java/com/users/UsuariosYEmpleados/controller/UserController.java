package com.users.UsuariosYEmpleados.controller;

import com.users.UsuariosYEmpleados.domain.dto.UserResponseDTO;
import com.users.UsuariosYEmpleados.domain.dto.UserDTO;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;
import com.users.UsuariosYEmpleados.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String tipoUsuario,
            @RequestParam(required = false) Boolean activo) {
        try {
            List<UserDTO> usuarios;

            if (tipoUsuario != null && activo != null) {
                TipoUsuario tipo = TipoUsuario.fromString(tipoUsuario);
                usuarios = userService.findByTipoUsuario(tipo).stream()
                        .filter(u -> u.getActivo().equals(activo))
                        .collect(Collectors.toList());
            } else if (tipoUsuario != null) {
                TipoUsuario tipo = TipoUsuario.fromString(tipoUsuario);
                usuarios = userService.findByTipoUsuario(tipo);
            } else if (activo != null) {
                usuarios = userService.findByActivo(activo);
            } else {
                usuarios = userService.findAll();
            }

            List<UserResponseDTO> response = usuarios.stream()
                    .map(u -> new UserResponseDTO(
                            u.getIdUsuario(),
                            u.getCorreo(),
                            u.getTipoUsuario(),
                            u.getActivo()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Tipo de usuario inválido: " + tipoUsuario));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al obtener usuarios"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            UserDTO usuario = userService.findById(id);
            UserResponseDTO response = new UserResponseDTO(
                    usuario.getIdUsuario(),
                    usuario.getCorreo(),
                    usuario.getTipoUsuario(),
                    usuario.getActivo());
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al obtener usuario"));
        }
    }

    @GetMapping("/correo/{correo}")
    public ResponseEntity<?> getByCorreo(@PathVariable String correo) {
        try {
            UserDTO usuario = userService.findByCorreo(correo);
            UserResponseDTO response = new UserResponseDTO(
                    usuario.getIdUsuario(),
                    usuario.getCorreo(),
                    usuario.getTipoUsuario(),
                    usuario.getActivo());
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al obtener usuario por correo"));
        }
    }
}
