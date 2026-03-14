package com.users.UsuariosYEmpleados.controller;

import com.users.UsuariosYEmpleados.domain.dto.UserResponseDTO;
import com.users.UsuariosYEmpleados.domain.dto.UserDTO;
import com.users.UsuariosYEmpleados.enums.TipoUsuario;
import com.users.UsuariosYEmpleados.service.UserService;
import com.users.UsuariosYEmpleados.util.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String correo) {
        try {
            TipoUsuario tipo = tipoUsuario != null ? TipoUsuario.fromString(tipoUsuario) : null;
            List<UserDTO> usuarios = userService.search(tipo, activo, correo);

            List<UserResponseDTO> response = usuarios.stream()
                    .map(u -> new UserResponseDTO(
                            u.getIdUsuario(),
                            u.getCorreo(),
                            u.getTipoUsuario(),
                            u.getActivo()))
                    .collect(Collectors.toList());

            return ResponseUtils.ok(response, "Usuarios obtenidos correctamente");

        } catch (IllegalArgumentException e) {
                return ResponseUtils.error(HttpStatus.BAD_REQUEST,
                    "Tipo de usuario inválido: " + tipoUsuario);
        } catch (Exception error) {
                return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al obtener usuarios");
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
            return ResponseUtils.ok(response, "Usuario obtenido correctamente");
        } catch (RuntimeException ex) {
            return ResponseUtils.error(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception error) {
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener usuario");
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
            return ResponseUtils.ok(response, "Usuario obtenido correctamente");
        } catch (RuntimeException ex) {
            return ResponseUtils.error(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (Exception error) {
            return ResponseUtils.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al obtener usuario por correo");
        }
    }
}
