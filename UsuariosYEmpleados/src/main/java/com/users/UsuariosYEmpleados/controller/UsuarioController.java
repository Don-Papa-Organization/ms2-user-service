package com.users.UsuariosYEmpleados.controller;

import com.users.UsuariosYEmpleados.dto.ApiResponse;
import com.users.UsuariosYEmpleados.dto.UserDTO;
import com.users.UsuariosYEmpleados.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UserService userService;

    public UsuarioController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsuarios() {
        List<UserDTO> usuarios = userService.findAll();
        return ResponseEntity.ok(ApiResponse.success(usuarios, "Usuarios cargados correctamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUsuarioById(@PathVariable Integer id) {
        UserDTO usuario = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Usuario cargado correctamente"));
    }
}
